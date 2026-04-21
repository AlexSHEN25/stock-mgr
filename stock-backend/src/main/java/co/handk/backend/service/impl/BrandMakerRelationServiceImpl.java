package co.handk.backend.service.impl;

import co.handk.backend.entity.BrandMakerRelation;
import co.handk.backend.mapper.BrandMakerRelationMapper;
import co.handk.backend.service.BrandMakerRelationService;
import co.handk.backend.util.EnumFieldMapper;
import co.handk.backend.util.PageSortUtil;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateBrandMakerRelationDTO;
import co.handk.common.model.dto.query.BrandMakerRelationQueryDTO;
import co.handk.common.model.dto.update.UpdateBrandMakerRelationDTO;
import co.handk.common.model.vo.BrandMakerRelationVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BrandMakerRelationServiceImpl extends ServiceImpl<BrandMakerRelationMapper, BrandMakerRelation> implements BrandMakerRelationService {

    private final BrandMakerRelationMapper brandMakerRelationMapper;

    @Override
    public Boolean create(CreateBrandMakerRelationDTO dto) {
        BrandMakerRelation entity = new BrandMakerRelation();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        entity.setId(null);
        return this.save(entity);
    }

    @Override
    public BrandMakerRelationVO get(Long id) {
        BrandMakerRelation entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("数据不存在");
        }
        BrandMakerRelationVO vo = new BrandMakerRelationVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public Boolean update(UpdateBrandMakerRelationDTO dto) {
        if (this.getById(dto.getId()) == null) {
            throw new RuntimeException("数据不存在");
        }
        BrandMakerRelation entity = new BrandMakerRelation();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        return this.updateById(entity);
    }

    @Override
    public Boolean delete(Long id) {
        if (this.getById(id) == null) {
            throw new RuntimeException("数据不存在");
        }
        return this.lambdaUpdate().eq(BrandMakerRelation::getId, id)
                .set(BrandMakerRelation::getDeleted, co.handk.common.enums.DeleteEnum.DELETED.getCode()).update();
    }

    @Override
    public PageResult<BrandMakerRelationVO> pageQuery(BrandMakerRelationQueryDTO query) {
        Page<BrandMakerRelation> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<BrandMakerRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BrandMakerRelation::getDeleted, co.handk.common.enums.DeleteEnum.UNDELETED.getCode())
                .eq(query.getBrandId() != null, BrandMakerRelation::getBrandId, query.getBrandId())
                .eq(query.getMakerId() != null, BrandMakerRelation::getMakerId, query.getMakerId());
        PageSortUtil.applyTimeSort(wrapper, query, BrandMakerRelation::getCreateTime, BrandMakerRelation::getUpdateTime);
        Page<BrandMakerRelation> resultPage = brandMakerRelationMapper.selectPage(page, wrapper);
        List<BrandMakerRelationVO> records = resultPage.getRecords().stream().map(entity -> {
            BrandMakerRelationVO vo = new BrandMakerRelationVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), records);
    }
}
