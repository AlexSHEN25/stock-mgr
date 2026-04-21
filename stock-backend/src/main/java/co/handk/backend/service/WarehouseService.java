package co.handk.backend.service;

import co.handk.backend.entity.Warehouse;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateWarehouseDTO;
import co.handk.common.model.dto.query.WarehouseQueryDTO;
import co.handk.common.model.dto.update.UpdateWarehouseDTO;
import co.handk.common.model.vo.WarehouseVO;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface WarehouseService extends IService<Warehouse> {
    Boolean create(@NotNull CreateWarehouseDTO dto);
    WarehouseVO get(@NotNull Long id);
    Boolean update(@NotNull UpdateWarehouseDTO dto);
    Boolean delete(@NotNull Long id);
    PageResult<WarehouseVO> pageQuery(@NotNull WarehouseQueryDTO query);
}
