package co.handk.backend.service.impl;

import co.handk.backend.entity.Category;
import co.handk.backend.mapper.CategoryMapper;
import co.handk.backend.service.CategoryService;
import co.handk.common.model.vo.CategoryVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends BaseServiceImpl<CategoryMapper, Category, CategoryVO>
        implements CategoryService {

    @Override
    protected CategoryVO toVO(Category entity) {
        if (entity == null) {
            return null;
        }
        CategoryVO vo = new CategoryVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    protected <D> Category toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        Category entity = new Category();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}