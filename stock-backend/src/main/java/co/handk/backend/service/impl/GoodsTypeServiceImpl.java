package co.handk.backend.service.impl;

import co.handk.backend.entity.GoodsType;
import co.handk.common.model.dto.GoodsTypeDTO;
import co.handk.backend.mapper.GoodsTypeMapper;
import co.handk.backend.service.GoodsTypeService;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.BeanUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GoodsTypeServiceImpl extends ServiceImpl<GoodsTypeMapper, GoodsType> implements GoodsTypeService {

    private final GoodsTypeMapper goodsTypeMapper;

    @Override
    public Boolean create(GoodsTypeDTO dto) {
        GoodsType entity = new GoodsType();
        BeanUtils.copyProperties(dto, entity);
        entity.setId(null);
        return this.save(entity);
    }

    @Override
    public GoodsType get(Long id) {
        GoodsType entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("数据不存在");
        }
        return entity;
    }

    @Override
    public Boolean update(GoodsTypeDTO dto) {
        if (this.getById(dto.getId()) == null) {
            throw new RuntimeException("数据不存在");
        }
        GoodsType entity = new GoodsType();
        BeanUtils.copyProperties(dto, entity);
        return this.updateById(entity);
    }

    @Override
    public Boolean delete(Long id) {
        if (this.getById(id) == null) {
            throw new RuntimeException("数据不存在");
        }
        return this.removeById(id);
    }

    @Override
    public List<GoodsType> listAll() {
        LambdaQueryWrapper<GoodsType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GoodsType::getDeleted, 0).orderByDesc(GoodsType::getUpdateTime);
        return goodsTypeMapper.selectList(wrapper);
    }

    @Override
    public PageResult<GoodsType> pageQuery(PageQuery query) {
        Page<GoodsType> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<GoodsType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GoodsType::getDeleted, 0).orderByDesc(GoodsType::getUpdateTime);
        Page<GoodsType> resultPage = goodsTypeMapper.selectPage(page, wrapper);
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), resultPage.getRecords());
    }
}
