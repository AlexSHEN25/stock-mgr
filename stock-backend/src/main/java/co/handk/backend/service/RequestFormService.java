package co.handk.backend.service;

import co.handk.backend.entity.RequestForm;
import co.handk.common.model.vo.RequestFormVO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface RequestFormService extends BaseService<RequestForm, RequestFormVO> {
}