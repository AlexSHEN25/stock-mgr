package co.handk.backend.service;

import co.handk.backend.entity.RequestItem;
import co.handk.common.model.vo.RequestItemVO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface RequestItemService extends BaseService<RequestItem, RequestItemVO> {
}