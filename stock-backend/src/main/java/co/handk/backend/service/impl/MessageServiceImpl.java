package co.handk.backend.service.impl;

import co.handk.backend.entity.Message;
import co.handk.backend.mapper.MessageMapper;
import co.handk.backend.service.MessageService;
import co.handk.common.model.vo.MessageVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class MessageServiceImpl extends BaseServiceImpl<MessageMapper, Message, MessageVO>
        implements MessageService {

    @Override
    protected MessageVO toVO(Message entity) {
        if (entity == null) {
            return null;
        }
        MessageVO vo = new MessageVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    protected <D> Message toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        Message entity = new Message();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}