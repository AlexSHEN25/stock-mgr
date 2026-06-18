package co.handk.backend.service.impl;

import co.handk.backend.constant.MessageKeyConstant;
import co.handk.backend.constant.UploadBizType;
import co.handk.backend.entity.Brand;
import co.handk.backend.entity.BrandMakerRelation;
import co.handk.backend.entity.Maker;
import co.handk.backend.entity.Series;
import co.handk.backend.entity.SeriesBrandRelation;
import co.handk.backend.exception.BusinessException;
import co.handk.backend.mapper.BrandMapper;
import co.handk.backend.mapper.BrandMakerRelationMapper;
import co.handk.backend.mapper.MakerMapper;
import co.handk.backend.mapper.SeriesBrandRelationMapper;
import co.handk.backend.mapper.SeriesMapper;
import co.handk.backend.service.BrandMakerRelationService;
import co.handk.backend.service.BrandService;
import co.handk.backend.service.FileStorageService;
import co.handk.backend.service.SeriesBrandRelationService;
import co.handk.common.model.dto.create.CreateBrandDTO;
import co.handk.common.model.dto.update.UpdateBrandDTO;
import co.handk.common.model.vo.BrandVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BrandServiceImpl extends BaseServiceImpl<BrandMapper, Brand, BrandVO>
        implements BrandService {

    private final FileStorageService fileStorageService;
    private final SeriesBrandRelationService seriesBrandRelationService;
    private final BrandMakerRelationService brandMakerRelationService;
    private final SeriesMapper seriesMapper;
    private final MakerMapper makerMapper;
    private final SeriesBrandRelationMapper seriesBrandRelationMapper;
    private final BrandMakerRelationMapper brandMakerRelationMapper;

    @Override
    protected BrandVO toVO(Brand entity) {
        if (entity == null) {
            return null;
        }
        BrandVO vo = new BrandVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setImage(fileStorageService.toApiPath(UploadBizType.BRAND, vo.getImage()));
        fillRelationIds(entity.getId(), vo);
        return vo;
    }

    @Override
    protected <D> Brand toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        Brand entity = new Brand();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <C> boolean saveByDto(C dto) {
        if (dto instanceof CreateBrandDTO createDto) {
            createDto.setImage(fileStorageService.normalize(UploadBizType.BRAND, createDto.getImage()));
            Brand brand = toEntity(createDto);
            boolean saved = this.save(brand);
            if (!saved) {
                return false;
            }
            syncSeriesRelations(brand.getId(), createDto.getSeriesIds());
            syncMakerRelations(brand.getId(), createDto.getMakerIds());
            return true;
        }
        return super.saveByDto(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <U> boolean updateByDto(U dto) {
        if (dto instanceof UpdateBrandDTO updateDto) {
            updateDto.setImage(fileStorageService.normalize(UploadBizType.BRAND, updateDto.getImage()));
            boolean updated = super.updateByDto(updateDto);
            if (!updated) {
                return false;
            }
            syncSeriesRelations(updateDto.getId(), updateDto.getSeriesIds());
            syncMakerRelations(updateDto.getId(), updateDto.getMakerIds());
            return true;
        }
        return super.updateByDto(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIdLogic(Long id) {
        int deleted = super.deleteByIdLogic(id);
        if (deleted > 0) {
            seriesBrandRelationService.update(null, new UpdateWrapper<SeriesBrandRelation>()
                    .eq("brand_id", id)
                    .set("deleted", 1));
            brandMakerRelationService.update(null, new UpdateWrapper<BrandMakerRelation>()
                    .eq("brand_id", id)
                    .set("deleted", 1));
        }
        return deleted;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteBatchLogic(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        int rows = 0;
        for (Long id : ids) {
            rows += deleteByIdLogic(id);
        }
        return rows;
    }

    @Override
    public String uploadImage(MultipartFile file) {
        String imagePath = fileStorageService.upload(UploadBizType.BRAND, file, null);
        return fileStorageService.toApiPath(UploadBizType.BRAND, imagePath);
    }

    @Override
    public String replaceImage(Long id, MultipartFile file) {
        if (id == null) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "ブランドIDは必須です");
        }
        Brand existed = this.getByIdNotDeleted(id);
        if (existed == null) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "ブランドが存在しません");
        }
        String imagePath = fileStorageService.upload(UploadBizType.BRAND, file, existed.getImage());
        existed.setImage(imagePath);
        this.updateById(existed);
        return fileStorageService.toApiPath(UploadBizType.BRAND, imagePath);
    }

    private void fillRelationIds(Long brandId, BrandVO vo) {
        if (brandId == null || vo == null) {
            return;
        }
        List<Long> seriesIds = seriesBrandRelationService.list(new QueryWrapper<SeriesBrandRelation>()
                        .eq("brand_id", brandId)
                        .eq("deleted", 0)
                        .orderByAsc("id"))
                .stream()
                .map(SeriesBrandRelation::getSeriesId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        List<Long> makerIds = brandMakerRelationService.list(new QueryWrapper<BrandMakerRelation>()
                        .eq("brand_id", brandId)
                        .eq("deleted", 0)
                        .orderByAsc("id"))
                .stream()
                .map(BrandMakerRelation::getMakerId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        vo.setSeriesIds(seriesIds);
        vo.setMakerIds(makerIds);
        vo.setSeriesNames(resolveSeriesNames(seriesIds));
        vo.setMakerNames(resolveMakerNames(makerIds));
    }

    private void syncSeriesRelations(Long brandId, List<Long> seriesIds) {
        List<SeriesBrandRelation> existing = seriesBrandRelationService.list(new QueryWrapper<SeriesBrandRelation>()
                .eq("brand_id", brandId));
        syncSeriesBrandRelations(existing, brandId, normalizeIds(seriesIds));
    }

    private void syncMakerRelations(Long brandId, List<Long> makerIds) {
        List<BrandMakerRelation> existing = brandMakerRelationService.list(new QueryWrapper<BrandMakerRelation>()
                .eq("brand_id", brandId));
        syncBrandMakerRelations(existing, brandId, normalizeIds(makerIds));
    }

    private void syncSeriesBrandRelations(List<SeriesBrandRelation> existing, Long brandId, Set<Long> targetSeriesIds) {
        Set<Long> activeIds = existing.stream()
                .filter(item -> item.getDeleted() != null && item.getDeleted() == 0)
                .map(SeriesBrandRelation::getSeriesId)
                .collect(Collectors.toSet());

        for (SeriesBrandRelation relation : existing) {
            Long seriesId = relation.getSeriesId();
            if (seriesId == null) {
                continue;
            }
            if (!targetSeriesIds.contains(seriesId) && relation.getDeleted() != null && relation.getDeleted() == 0) {
                relation.setDeleted(1);
                seriesBrandRelationService.updateById(relation);
                continue;
            }
            if (targetSeriesIds.contains(seriesId) && relation.getDeleted() != null && relation.getDeleted() != 0) {
                relation.setDeleted(0);
                seriesBrandRelationService.updateById(relation);
            }
        }

        for (Long seriesId : targetSeriesIds) {
            if (activeIds.contains(seriesId)) {
                continue;
            }
            ensureSeriesBrandRelation(brandId, seriesId);
        }
    }

    private void syncBrandMakerRelations(List<BrandMakerRelation> existing, Long brandId, Set<Long> targetMakerIds) {
        Set<Long> activeIds = existing.stream()
                .filter(item -> item.getDeleted() != null && item.getDeleted() == 0)
                .map(BrandMakerRelation::getMakerId)
                .collect(Collectors.toSet());

        for (BrandMakerRelation relation : existing) {
            Long makerId = relation.getMakerId();
            if (makerId == null) {
                continue;
            }
            if (!targetMakerIds.contains(makerId) && relation.getDeleted() != null && relation.getDeleted() == 0) {
                relation.setDeleted(1);
                brandMakerRelationService.updateById(relation);
                continue;
            }
            if (targetMakerIds.contains(makerId) && relation.getDeleted() != null && relation.getDeleted() != 0) {
                relation.setDeleted(0);
                brandMakerRelationService.updateById(relation);
            }
        }

        for (Long makerId : targetMakerIds) {
            if (activeIds.contains(makerId)) {
                continue;
            }
            ensureBrandMakerRelation(brandId, makerId);
        }
    }

    private Set<Long> normalizeIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new LinkedHashSet<>();
        }
        return ids.stream()
                .filter(id -> id != null)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private List<String> resolveSeriesNames(List<Long> seriesIds) {
        if (seriesIds == null || seriesIds.isEmpty()) {
            return List.of();
        }
        return seriesMapper.selectList(new QueryWrapper<Series>()
                        .in("id", seriesIds)
                        .eq("deleted", 0)
                        .orderByAsc("id"))
                .stream()
                .map(Series::getName)
                .filter(name -> name != null && !name.isBlank())
                .toList();
    }

    private List<String> resolveMakerNames(List<Long> makerIds) {
        if (makerIds == null || makerIds.isEmpty()) {
            return List.of();
        }
        return makerMapper.selectList(new QueryWrapper<Maker>()
                        .in("id", makerIds)
                        .eq("deleted", 0)
                        .orderByAsc("id"))
                .stream()
                .map(Maker::getName)
                .filter(name -> name != null && !name.isBlank())
                .toList();
    }

    private void ensureSeriesBrandRelation(Long brandId, Long seriesId) {
        seriesBrandRelationMapper.upsertRelation(seriesId, brandId, co.handk.backend.annotation.context.UserContext.getUserIdOrDefault());
    }

    private void ensureBrandMakerRelation(Long brandId, Long makerId) {
        brandMakerRelationMapper.upsertRelation(brandId, makerId, co.handk.backend.annotation.context.UserContext.getUserIdOrDefault());
    }
}
