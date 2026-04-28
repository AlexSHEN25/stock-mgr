package co.handk.backend.service.impl;

import co.handk.backend.entity.PriceRecord;
import co.handk.backend.mapper.PriceRecordMapper;
import co.handk.backend.service.PriceRecordService;
import co.handk.common.model.vo.PriceRecordVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class PriceRecordServiceImpl extends BaseServiceImpl<PriceRecordMapper, PriceRecord, PriceRecordVO>
        implements PriceRecordService {

    @Override
    protected PriceRecordVO toVO(PriceRecord entity) {
        if (entity == null) {
            return null;
        }
        PriceRecordVO vo = new PriceRecordVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    protected <D> PriceRecord toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        PriceRecord entity = new PriceRecord();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}