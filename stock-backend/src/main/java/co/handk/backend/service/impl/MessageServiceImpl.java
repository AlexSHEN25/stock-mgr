package co.handk.backend.service.impl;

import co.handk.backend.context.UserContext;
import co.handk.backend.entity.Message;
import co.handk.backend.mapper.MessageMapper;
import co.handk.backend.service.MessageService;
import co.handk.common.constant.NumberConstant;
import co.handk.common.enums.DeleteEnum;
import co.handk.common.model.vo.MessageVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class MessageServiceImpl extends BaseServiceImpl<MessageMapper, Message, MessageVO>
        implements MessageService {
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_IS_READ = "is_read";
    private static final String COLUMN_DELETED = "deleted";

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

    @Override
    public boolean read(Long id) {
        Long userId = UserContext.getUserIdOrDefault();
        int affected = baseMapper.update(
                null,
                new UpdateWrapper<Message>()
                        .eq(COLUMN_ID, id)
                        .eq(COLUMN_USER_ID, userId)
                        .eq(COLUMN_DELETED, DeleteEnum.UNDELETED.getCode())
                        .set(COLUMN_IS_READ, NumberConstant.ONE)
        );
        return affected > NumberConstant.ZERO;
    }

    @Override
    public int readAllCurrentUser() {
        Long userId = UserContext.getUserIdOrDefault();
        return baseMapper.update(
                null,
                new UpdateWrapper<Message>()
                        .eq(COLUMN_USER_ID, userId)
                        .eq(COLUMN_DELETED, DeleteEnum.UNDELETED.getCode())
                        .set(COLUMN_IS_READ, NumberConstant.ONE)
        );
    }

    @Override
    public long countByReadStatus(Integer isRead) {
        Long userId = UserContext.getUserIdOrDefault();
        QueryWrapper<Message> wrapper = new QueryWrapper<Message>()
                .eq(COLUMN_USER_ID, userId)
                .eq(COLUMN_DELETED, DeleteEnum.UNDELETED.getCode());
        if (isRead != null) {
            wrapper.eq(COLUMN_IS_READ, isRead);
        }
        return baseMapper.selectCount(wrapper);
    }
}
