package co.handk.backend.service;

import co.handk.backend.entity.CustomerLevel;
import co.handk.common.model.vo.CustomerLevelVO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface CustomerLevelService extends BaseService<CustomerLevel, CustomerLevelVO> {
}