package co.handk.backend.service;

import co.handk.backend.entity.OperateLog;
import co.handk.common.model.vo.OperateLogVO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface OperateLogService extends BaseService<OperateLog, OperateLogVO> {
}