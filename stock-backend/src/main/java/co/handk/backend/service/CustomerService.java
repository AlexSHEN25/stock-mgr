package co.handk.backend.service;

import co.handk.backend.entity.Customer;
import co.handk.common.model.vo.CustomerVO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface CustomerService extends BaseService<Customer, CustomerVO> {
}