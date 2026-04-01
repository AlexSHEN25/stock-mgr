package co.handk.backend.service;

import co.handk.backend.entity.Warehouse;
import co.handk.common.model.dto.WarehouseDTO;
import co.handk.common.model.vo.WarehouseVO;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Service
@Validated
public interface WarehouseService extends IService<Warehouse> {

    Boolean create(@NotNull WarehouseDTO dto);

    WarehouseVO get(@NotNull Long id);

    Boolean update(@NotNull WarehouseDTO dto);

    Boolean delete(@NotNull Long id);
    List<WarehouseVO> listAll();

    PageResult<WarehouseVO> pageQuery(@NotNull PageQuery query);
}
