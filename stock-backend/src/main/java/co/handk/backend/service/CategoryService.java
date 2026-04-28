package co.handk.backend.service;

import co.handk.backend.entity.Category;
import co.handk.common.model.vo.CategoryVO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface CategoryService extends BaseService<Category, CategoryVO> {
}