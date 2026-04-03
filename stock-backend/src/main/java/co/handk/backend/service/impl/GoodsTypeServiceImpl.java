package co.handk.backend.service.impl;

import org.apache.commons.lang3.StringUtils;

import co.handk.backend.util.PageSortUtil;

import co.handk.backend.util.EnumFieldMapper;

import co.handk.backend.entity.GoodsType;
import co.handk.common.model.dto.create.CreateGoodsTypeDTO;
import co.handk.common.model.dto.update.UpdateGoodsTypeDTO;
import co.handk.common.model.vo.GoodsTypeVO;
import co.handk.backend.mapper.GoodsTypeMapper;
import co.handk.backend.service.GoodsTypeService;
import co.handk.common.model.dto.query.GoodsTypeQueryDTO;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoodsTypeServiceImpl extends ServiceImpl<GoodsTypeMapper, GoodsType> implements GoodsTypeService {

    private final GoodsTypeMapper goodsTypeMapper;

    @Override
    public Boolean create(CreateGoodsTypeDTO dto) {
        GoodsType entity = new GoodsType();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        entity.setId(null);
        return this.save(entity);
    }

    @Override
    public GoodsTypeVO get(Long id) {
        GoodsType entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("数据不存在");
        }
        GoodsTypeVO vo = new GoodsTypeVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public Boolean update(UpdateGoodsTypeDTO dto) {
        if (this.getById(dto.getId()) == null) {
            throw new RuntimeException("数据不存在");
        }
        GoodsType entity = new GoodsType();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        return this.updateById(entity);
    }

    @Override
    public Boolean delete(Long id) {
        if (this.getById(id) == null) {
            throw new RuntimeException("数据不存在");
        }
        return this.lambdaUpdate().eq(GoodsType::getId, id).set(GoodsType::getDeleted, co.handk.common.enums.DeleteEnum.DELETED.getCode()).update();
    }

    @Override
    public PageResult<GoodsTypeVO> pageQuery(GoodsTypeQueryDTO query) {
        Page<GoodsType> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<GoodsType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GoodsType::getDeleted, co.handk.common.enums.DeleteEnum.UNDELETED.getCode())
                .like(StringUtils.isNotBlank(query.getName()), GoodsType::getName, query.getName())
                .eq(query.getStatus() != null, GoodsType::getStatus, (query.getStatus() == null ? null : query.getStatus().getCode()));
        PageSortUtil.applyTimeSort(wrapper, query, GoodsType::getCreateTime, GoodsType::getUpdateTime);
        Page<GoodsType> resultPage =     goodsTypeMapper.selectPage(page, wrapper);
        List<GoodsTypeVO> records = resultPage.getRecords().stream().map(entity -> {
            GoodsTypeVO vo = new GoodsTypeVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), records);
    }
}
