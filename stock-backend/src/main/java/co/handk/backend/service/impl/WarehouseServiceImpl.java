package co.handk.backend.service.impl;

import co.handk.backend.entity.Warehouse;
import co.handk.backend.mapper.WarehouseMapper;
import co.handk.backend.service.WarehouseService;
import co.handk.common.model.vo.WarehouseVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class WarehouseServiceImpl extends BaseServiceImpl<WarehouseMapper, Warehouse, WarehouseVO>
        implements WarehouseService {

    @Override
    protected WarehouseVO toVO(Warehouse entity) {
        if (entity == null) {
            return null;
        }
        WarehouseVO vo = new WarehouseVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    protected <D> Warehouse toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        Warehouse entity = new Warehouse();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}