package co.handk.backend.service;

import co.handk.backend.entity.Category;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateCategoryDTO;
import co.handk.common.model.dto.query.CategoryQueryDTO;
import co.handk.common.model.dto.update.UpdateCategoryDTO;
import co.handk.common.model.vo.CategoryVO;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface CategoryService extends IService<Category> {
    Boolean create(@NotNull CreateCategoryDTO dto);

    CategoryVO get(@NotNull Long id);

    Boolean update(@NotNull UpdateCategoryDTO dto);

    Boolean delete(@NotNull Long id);

    PageResult<CategoryVO> pageQuery(@NotNull CategoryQueryDTO query);
}
