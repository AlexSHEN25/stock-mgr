package co.handk.backend.service.impl;

import co.handk.backend.entity.CustomerLevel;
import co.handk.backend.mapper.CustomerLevelMapper;
import co.handk.backend.service.CustomerLevelService;
import co.handk.common.model.vo.CustomerLevelVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class CustomerLevelServiceImpl extends BaseServiceImpl<CustomerLevelMapper, CustomerLevel, CustomerLevelVO>
        implements CustomerLevelService {

    @Override
    protected CustomerLevelVO toVO(CustomerLevel entity) {
        if (entity == null) {
            return null;
        }
        CustomerLevelVO vo = new CustomerLevelVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    protected <D> CustomerLevel toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        CustomerLevel entity = new CustomerLevel();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}