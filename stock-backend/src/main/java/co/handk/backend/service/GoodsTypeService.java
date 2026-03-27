package co.handk.backend.service;

import co.handk.backend.entity.GoodsType;
import co.handk.common.model.dto.GoodsTypeDTO;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Service
@Validated
public interface GoodsTypeService extends IService<GoodsType> {

    Boolean create(@NotNull GoodsTypeDTO dto);

    GoodsType get(@NotNull Long id);

    Boolean update(@NotNull GoodsTypeDTO dto);

    Boolean delete(@NotNull Long id);

    List<GoodsType> listAll();

    PageResult<GoodsType> pageQuery(@NotNull PageQuery query);
}
