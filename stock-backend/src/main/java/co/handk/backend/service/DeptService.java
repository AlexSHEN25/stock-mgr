package co.handk.backend.service;

import co.handk.backend.entity.Dept;
import co.handk.common.model.vo.DeptVO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface DeptService extends BaseService<Dept, DeptVO> {
}