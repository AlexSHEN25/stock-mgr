package co.handk.backend.service.impl;

import co.handk.backend.entity.Category;
import co.handk.backend.mapper.CategoryMapper;
import co.handk.backend.service.CategoryService;
import co.handk.backend.util.EnumFieldMapper;
import co.handk.backend.util.PageSortUtil;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateCategoryDTO;
import co.handk.common.model.dto.query.CategoryQueryDTO;
import co.handk.common.model.dto.update.UpdateCategoryDTO;
import co.handk.common.model.vo.CategoryVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    public Boolean create(CreateCategoryDTO dto) {
        Category entity = new Category();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        entity.setId(null);
        return this.save(entity);
    }

    @Override
    public CategoryVO get(Long id) {
        Category entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("謨ｰ謐ｮ荳榊ｭ伜惠");
        }
        CategoryVO vo = new CategoryVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public Boolean update(UpdateCategoryDTO dto) {
        if (this.getById(dto.getId()) == null) {
            throw new RuntimeException("謨ｰ謐ｮ荳榊ｭ伜惠");
        }
        Category entity = new Category();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        return this.updateById(entity);
    }

    @Override
    public Boolean delete(Long id) {
        if (this.getById(id) == null) {
            throw new RuntimeException("謨ｰ謐ｮ荳榊ｭ伜惠");
        }
        return this.lambdaUpdate().eq(Category::getId, id)
                .set(Category::getDeleted, co.handk.common.enums.DeleteEnum.DELETED.getCode()).update();
    }

    @Override
    public PageResult<CategoryVO> pageQuery(CategoryQueryDTO query) {
        Page<Category> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getDeleted, co.handk.common.enums.DeleteEnum.UNDELETED.getCode())
                .like(StringUtils.isNotBlank(query.getName()), Category::getName, query.getName())
                .eq(query.getStatus() != null, Category::getStatus, (query.getStatus() == null ? null : query.getStatus().getCode()));
        PageSortUtil.applyTimeSort(wrapper, query, Category::getCreateTime, Category::getUpdateTime);
        Page<Category> resultPage = categoryMapper.selectPage(page, wrapper);
        List<CategoryVO> records = resultPage.getRecords().stream().map(entity -> {
            CategoryVO vo = new CategoryVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), records);
    }
}
