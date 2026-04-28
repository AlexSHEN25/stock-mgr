package co.handk.backend.service;

import co.handk.backend.entity.Message;
import co.handk.common.model.vo.MessageVO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface MessageService extends BaseService<Message, MessageVO> {
}