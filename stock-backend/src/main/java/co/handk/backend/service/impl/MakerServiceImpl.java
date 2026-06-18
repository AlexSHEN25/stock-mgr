package co.handk.backend.service.impl;

import co.handk.backend.entity.Maker;
import co.handk.backend.entity.BrandMakerRelation;
import co.handk.backend.entity.Brand;
import co.handk.backend.mapper.BrandMapper;
import co.handk.backend.mapper.BrandMakerRelationMapper;
import co.handk.backend.mapper.MakerMapper;
import co.handk.backend.service.BrandMakerRelationService;
import co.handk.backend.service.MakerService;
import co.handk.common.model.dto.create.CreateMakerDTO;
import co.handk.common.model.dto.update.UpdateMakerDTO;
import co.handk.common.model.vo.MakerVO;
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
public class MakerServiceImpl extends BaseServiceImpl<MakerMapper, Maker, MakerVO>
        implements MakerService {

    private final BrandMakerRelationService brandMakerRelationService;
    private final BrandMapper brandMapper;
    private final BrandMakerRelationMapper brandMakerRelationMapper;

    @Override
    protected MakerVO toVO(Maker entity) {
        if (entity == null) {
            return null;
        }
        MakerVO vo = new MakerVO();
        BeanUtils.copyProperties(entity, vo);
        fillBrandIds(entity.getId(), vo);
        return vo;
    }

    @Override
    protected <D> Maker toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        Maker entity = new Maker();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <C> boolean saveByDto(C dto) {
        if (dto instanceof CreateMakerDTO createDto) {
            Maker entity = toEntity(createDto);
            boolean saved = this.save(entity);
            if (!saved) {
                return false;
            }
            syncBrandRelations(entity.getId(), createDto.getBrandIds());
            return true;
        }
        return super.saveByDto(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <U> boolean updateByDto(U dto) {
        if (dto instanceof UpdateMakerDTO updateDto) {
            boolean updated = super.updateByDto(updateDto);
            if (!updated) {
                return false;
            }
            syncBrandRelations(updateDto.getId(), updateDto.getBrandIds());
            return true;
        }
        return super.updateByDto(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIdLogic(Long id) {
        int deleted = super.deleteByIdLogic(id);
        if (deleted > 0) {
            brandMakerRelationService.update(null, new UpdateWrapper<BrandMakerRelation>()
                    .eq("maker_id", id)
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

    private void fillBrandIds(Long makerId, MakerVO vo) {
        if (makerId == null || vo == null) {
            return;
        }
        List<Long> brandIds = brandMakerRelationService.list(new QueryWrapper<BrandMakerRelation>()
                        .eq("maker_id", makerId)
                        .eq("deleted", 0)
                        .orderByAsc("id"))
                .stream()
                .map(BrandMakerRelation::getBrandId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        vo.setBrandIds(brandIds);
        vo.setBrandNames(resolveBrandNames(brandIds));
    }

    private void syncBrandRelations(Long makerId, List<Long> brandIds) {
        Set<Long> targetBrandIds = normalizeIds(brandIds);
        List<BrandMakerRelation> existing = brandMakerRelationService.list(new QueryWrapper<BrandMakerRelation>()
                .eq("maker_id", makerId));
        Set<Long> activeBrandIds = existing.stream()
                .filter(item -> item.getDeleted() != null && item.getDeleted() == 0)
                .map(BrandMakerRelation::getBrandId)
                .collect(Collectors.toSet());

        for (BrandMakerRelation relation : existing) {
            Long brandId = relation.getBrandId();
            if (brandId == null) {
                continue;
            }
            if (!targetBrandIds.contains(brandId) && relation.getDeleted() != null && relation.getDeleted() == 0) {
                relation.setDeleted(1);
                brandMakerRelationService.updateById(relation);
                continue;
            }
            if (targetBrandIds.contains(brandId) && relation.getDeleted() != null && relation.getDeleted() != 0) {
                relation.setDeleted(0);
                brandMakerRelationService.updateById(relation);
            }
        }

        for (Long brandId : targetBrandIds) {
            if (activeBrandIds.contains(brandId)) {
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

    private void ensureBrandMakerRelation(Long brandId, Long makerId) {
        brandMakerRelationMapper.upsertRelation(brandId, makerId, co.handk.backend.annotation.context.UserContext.getUserIdOrDefault());
    }
}
