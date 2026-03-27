package co.handk.backend.service;

import co.handk.backend.entity.Brand;
import co.handk.common.model.dto.BrandDTO;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Service
@Validated
public interface BrandService extends IService<Brand> {

    Boolean create(@NotNull BrandDTO dto);

    Brand get(@NotNull Long id);

    Boolean update(@NotNull BrandDTO dto);

    Boolean delete(@NotNull Long id);

    List<Brand> listAll();

    PageResult<Brand> pageQuery(@NotNull PageQuery query);
}
