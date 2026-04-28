package co.handk.backend.service;

import co.handk.backend.entity.Warehouse;
import co.handk.common.model.vo.WarehouseVO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface WarehouseService extends BaseService<Warehouse, WarehouseVO> {
}