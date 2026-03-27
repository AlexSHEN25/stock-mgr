package co.handk.backend.service.impl;

import co.handk.backend.entity.Brand;
import co.handk.common.model.dto.BrandDTO;
import co.handk.backend.mapper.BrandMapper;
import co.handk.backend.service.BrandService;
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
public class BrandServiceImpl extends ServiceImpl<BrandMapper, Brand> implements BrandService {

    private final BrandMapper brandMapper;

    @Override
    public Boolean create(BrandDTO dto) {
        Brand entity = new Brand();
        BeanUtils.copyProperties(dto, entity);
        entity.setId(null);
        return this.save(entity);
    }

    @Override
    public Brand get(Long id) {
        Brand entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("数据不存在");
        }
        return entity;
    }

    @Override
    public Boolean update(BrandDTO dto) {
        if (this.getById(dto.getId()) == null) {
            throw new RuntimeException("数据不存在");
        }
        Brand entity = new Brand();
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
    public List<Brand> listAll() {
        LambdaQueryWrapper<Brand> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Brand::getDeleted, 0).orderByDesc(Brand::getUpdateTime);
        return brandMapper.selectList(wrapper);
    }

    @Override
    public PageResult<Brand> pageQuery(PageQuery query) {
        Page<Brand> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Brand> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Brand::getDeleted, 0).orderByDesc(Brand::getUpdateTime);
        Page<Brand> resultPage = brandMapper.selectPage(page, wrapper);
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), resultPage.getRecords());
    }
}
