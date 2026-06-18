package co.handk.backend.service.impl;

import co.handk.backend.annotation.JoinQueryConfig;
import co.handk.backend.annotation.JoinTable;
import co.handk.backend.enums.JoinType;
import co.handk.backend.entity.Brand;
import co.handk.backend.entity.Series;
import co.handk.backend.entity.SeriesBrandRelation;
import co.handk.backend.mapper.BrandMapper;
import co.handk.backend.mapper.SeriesMapper;
import co.handk.backend.mapper.SeriesBrandRelationMapper;
import co.handk.backend.service.SeriesService;
import co.handk.backend.service.SeriesBrandRelationService;
import co.handk.common.model.dto.create.CreateSeriesDTO;
import co.handk.common.model.PageResult;
import co.handk.common.model.PageQuery;
import co.handk.common.model.dto.update.UpdateSeriesDTO;
import co.handk.common.model.vo.SeriesVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@JoinQueryConfig(
        baseTable = "t_series",
        baseAlias = "t",
        joins = {
                @JoinTable(type = JoinType.LEFT, table = "t_brand", alias = "b", on = "b.id = t.brand_id")
        }
)
public class SeriesServiceImpl extends BaseServiceImpl<SeriesMapper, Series, SeriesVO>
        implements SeriesService {

    private final SeriesBrandRelationService seriesBrandRelationService;
    private final BrandMapper brandMapper;
    private final SeriesBrandRelationMapper seriesBrandRelationMapper;

    @Override
    protected SeriesVO toVO(Series entity) {
        if (entity == null) {
            return null;
        }
        SeriesVO vo = new SeriesVO();
        BeanUtils.copyProperties(entity, vo);
        fillBrandIds(entity.getId(), vo);
        return vo;
    }

    @Override
    protected <D> Series toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        Series entity = new Series();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }

    @Override
    public <Q> List<SeriesVO> list(Q dto) {
        List<SeriesVO> records = super.list(dto);
        fillBrandIds(records);
        return records;
    }

    @Override
    public <Q extends PageQuery> PageResult<SeriesVO> page(Q dto) {
        PageResult<SeriesVO> result = super.page(dto);
        fillBrandIds(result == null ? null : result.getRecords());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <C> boolean saveByDto(C dto) {
        if (dto instanceof CreateSeriesDTO createDto) {
            Series entity = toEntity(createDto);
            entity.setBrandId(resolvePrimaryBrandId(createDto.getBrandId(), createDto.getBrandIds()));
            boolean saved = this.save(entity);
            if (!saved) {
                return false;
            }
            syncSeriesBrands(entity.getId(), createDto.getBrandId(), createDto.getBrandIds());
            return true;
        }
        return super.saveByDto(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <U> boolean updateByDto(U dto) {
        if (dto instanceof UpdateSeriesDTO updateDto) {
            updateDto.setBrandId(resolvePrimaryBrandId(updateDto.getBrandId(), updateDto.getBrandIds()));
            boolean updated = super.updateByDto(updateDto);
            if (!updated) {
                return false;
            }
            syncSeriesBrands(updateDto.getId(), updateDto.getBrandId(), updateDto.getBrandIds());
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
                    .eq("series_id", id)
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

    private void fillBrandIds(Long seriesId, SeriesVO vo) {
        if (seriesId == null || vo == null) {
            return;
        }
        List<Long> brandIds = seriesBrandRelationService.list(new QueryWrapper<SeriesBrandRelation>()
                        .eq("series_id", seriesId)
                        .eq("deleted", 0)
                        .orderByAsc("id"))
                .stream()
                .map(SeriesBrandRelation::getBrandId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        vo.setBrandIds(brandIds);
        vo.setBrandNames(resolveBrandNames(brandIds));
    }

    private void fillBrandIds(List<SeriesVO> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        for (SeriesVO record : records) {
            if (record != null) {
                fillBrandIds(record.getId(), record);
            }
        }
    }

    private void syncSeriesBrands(Long seriesId, Long primaryBrandId, List<Long> brandIds) {
        Set<Long> targetBrandIds = normalizeIds(brandIds);
        if (primaryBrandId != null) {
            targetBrandIds.add(primaryBrandId);
        }
        List<SeriesBrandRelation> existing = seriesBrandRelationService.list(new QueryWrapper<SeriesBrandRelation>()
                .eq("series_id", seriesId));
        Set<Long> activeBrandIds = existing.stream()
                .filter(item -> item.getDeleted() != null && item.getDeleted() == 0)
                .map(SeriesBrandRelation::getBrandId)
                .collect(Collectors.toSet());

        for (SeriesBrandRelation relation : existing) {
            Long brandId = relation.getBrandId();
            if (brandId == null) {
                continue;
            }
            if (!targetBrandIds.contains(brandId) && relation.getDeleted() != null && relation.getDeleted() == 0) {
                relation.setDeleted(1);
                seriesBrandRelationService.updateById(relation);
                continue;
            }
            if (targetBrandIds.contains(brandId) && relation.getDeleted() != null && relation.getDeleted() != 0) {
                relation.setDeleted(0);
                seriesBrandRelationService.updateById(relation);
            }
        }

        for (Long brandId : targetBrandIds) {
            if (activeBrandIds.contains(brandId)) {
                continue;
            }
            ensureSeriesBrandRelation(seriesId, brandId);
        }
    }

    private Long resolvePrimaryBrandId(Long brandId, List<Long> brandIds) {
        if (brandId != null) {
            return brandId;
        }
        if (brandIds == null || brandIds.isEmpty()) {
            return null;
        }
        return brandIds.stream().filter(id -> id != null).findFirst().orElse(null);
    }

    private Set<Long> normalizeIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new LinkedHashSet<>();
        }
        return ids.stream()
                .filter(id -> id != null)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private List<String> resolveBrandNames(List<Long> brandIds) {
        if (brandIds == null || brandIds.isEmpty()) {
            return List.of();
        }
        return brandMapper.selectList(new QueryWrapper<Brand>()
                        .in("id", brandIds)
                        .eq("deleted", 0)
                        .orderByAsc("id"))
                .stream()
                .map(Brand::getName)
                .filter(name -> name != null && !name.isBlank())
                .toList();
    }

    private void ensureSeriesBrandRelation(Long seriesId, Long brandId) {
        seriesBrandRelationMapper.upsertRelation(seriesId, brandId, co.handk.backend.annotation.context.UserContext.getUserIdOrDefault());
    }
}
