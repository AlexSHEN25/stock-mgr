package co.handk.backend.service.impl;

import co.handk.backend.entity.Customer;
import co.handk.backend.mapper.CustomerMapper;
import co.handk.backend.service.CustomerService;
import co.handk.common.model.vo.CustomerVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class CustomerServiceImpl extends BaseServiceImpl<CustomerMapper, Customer, CustomerVO>
        implements CustomerService {

    @Override
    protected CustomerVO toVO(Customer entity) {
        if (entity == null) {
            return null;
        }
        CustomerVO vo = new CustomerVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    protected <D> Customer toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        Customer entity = new Customer();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}