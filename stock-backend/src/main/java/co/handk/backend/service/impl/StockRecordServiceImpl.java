package co.handk.backend.service.impl;

import co.handk.backend.entity.StockRecord;
import co.handk.backend.mapper.StockRecordMapper;
import co.handk.backend.service.StockRecordService;
import co.handk.common.model.vo.StockRecordVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class StockRecordServiceImpl extends BaseServiceImpl<StockRecordMapper, StockRecord, StockRecordVO>
        implements StockRecordService {

    @Override
    protected StockRecordVO toVO(StockRecord entity) {
        if (entity == null) {
            return null;
        }
        StockRecordVO vo = new StockRecordVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    protected <D> StockRecord toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        StockRecord entity = new StockRecord();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}