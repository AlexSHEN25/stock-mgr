package co.handk.backend.service.impl;

import co.handk.backend.constant.MessageKeyConstant;
import co.handk.backend.annotation.context.UserContext;
import co.handk.backend.entity.Message;
import co.handk.backend.exception.BusinessException;
import co.handk.backend.exception.LoginException;
import co.handk.backend.mapper.MessageMapper;
import co.handk.backend.service.MessageService;
import co.handk.backend.service.PermissionQueryService;
import co.handk.common.constant.MessageBizConstant;
import co.handk.common.enums.DeleteEnum;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.query.MessageQueryDTO;
import co.handk.common.model.vo.MessageVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl extends BaseServiceImpl<MessageMapper, Message, MessageVO>
        implements MessageService {

    private final PermissionQueryService permissionQueryService;

    private static final String MESSAGE_LOGIN_REQUIRED = "ログインしてください";
    private static final String MESSAGE_NOT_FOUND = "メッセージが見つかりません";
    private static final String MESSAGE_READ_UPDATE_FAILED = "メッセージ既読更新に失敗しました";

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
    public PageResult<MessageVO> page(MessageQueryDTO query) {
        MessageQueryDTO actualQuery = query == null ? new MessageQueryDTO() : query;
        Long userId = UserContext.getUserIdOrDefault();
        boolean queryAll = Boolean.TRUE.equals(actualQuery.getAll())
                || "all".equalsIgnoreCase(actualQuery.getScope());
        if (!queryAll || !permissionQueryService.isSuperAdmin(userId)) {
            actualQuery.setUserId(userId);
        }
        return super.page(actualQuery);
    }

    @Override
    protected <Q> QueryWrapper<Message> buildWrapper(Q dto) {
        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", DeleteEnum.UNDELETED.getCode());
        if (!(dto instanceof MessageQueryDTO query)) {
            return wrapper;
        }
        if (query.getType() != null) {
            wrapper.eq("type", query.getType());
        }
        if (query.getUserId() != null) {
            wrapper.eq("user_id", query.getUserId());
        }
        if (query.getMessage() != null && !query.getMessage().isBlank()) {
            wrapper.like("message", query.getMessage().trim());
        }
        if (query.getSourceId() != null) {
            wrapper.eq("source_id", query.getSourceId());
        }
        if (query.getIsRead() != null) {
            wrapper.eq("is_read", query.getIsRead());
        }
        if (query.getState() != null) {
            wrapper.eq("state", query.getState());
        }
        // all/scope are query control flags, not physical columns in t_message.
        return wrapper;
    }

    @Override
    public boolean read(Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new LoginException(MessageKeyConstant.ERROR_LOGIN_REQUIRED, MESSAGE_LOGIN_REQUIRED);
        }
        Message message = getOne(new LambdaQueryWrapper<Message>()
                .eq(Message::getId, id)
                .eq(Message::getUserId, userId)
                .eq(Message::getDeleted, DeleteEnum.UNDELETED.getCode()));
        if (message == null) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, MESSAGE_NOT_FOUND);
        }
        if (message.getIsRead() != null && message.getIsRead() == MessageBizConstant.IS_READ) {
            return true;
        }
        int affected = baseMapper.update(
                null,
                new LambdaUpdateWrapper<Message>()
                        .eq(Message::getId, id)
                        .eq(Message::getUserId, userId)
                        .eq(Message::getDeleted, DeleteEnum.UNDELETED.getCode())
                        .set(Message::getIsRead, MessageBizConstant.IS_READ)
        );
        if (affected <= 0) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, MESSAGE_READ_UPDATE_FAILED);
        }
        return true;
    }

    @Override
    public int readAllCurrentUser() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new LoginException(MessageKeyConstant.ERROR_LOGIN_REQUIRED, MESSAGE_LOGIN_REQUIRED);
        }
        return baseMapper.update(
                null,
                new LambdaUpdateWrapper<Message>()
                        .eq(Message::getUserId, userId)
                        .eq(Message::getDeleted, DeleteEnum.UNDELETED.getCode())
                        .set(Message::getIsRead, MessageBizConstant.IS_READ)
        );
    }

    @Override
    public long countByReadStatus(Integer isRead) {
        Long userId = UserContext.getUserIdOrDefault();
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<Message>()
                .eq(Message::getUserId, userId)
                .eq(Message::getDeleted, DeleteEnum.UNDELETED.getCode());
        if (isRead != null) {
            wrapper.eq(Message::getIsRead, isRead);
        }
        return baseMapper.selectCount(wrapper);
    }
}
