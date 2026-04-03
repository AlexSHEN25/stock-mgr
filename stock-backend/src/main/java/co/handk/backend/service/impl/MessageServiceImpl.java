package co.handk.backend.service.impl;

import org.apache.commons.lang3.StringUtils;

import co.handk.backend.util.PageSortUtil;

import co.handk.backend.util.EnumFieldMapper;

import co.handk.backend.entity.Message;
import co.handk.common.model.dto.create.CreateMessageDTO;
import co.handk.common.model.dto.update.UpdateMessageDTO;
import co.handk.common.model.vo.MessageVO;
import co.handk.backend.mapper.MessageMapper;
import co.handk.backend.service.MessageService;
import co.handk.common.model.dto.query.MessageQueryDTO;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    private final MessageMapper messageMapper;

    @Override
    public Boolean create(CreateMessageDTO dto) {
        Message entity = new Message();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        entity.setId(null);
        return this.save(entity);
    }

    @Override
    public MessageVO get(Long id) {
        Message entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("数据不存在");
        }
        MessageVO vo = new MessageVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public Boolean update(UpdateMessageDTO dto) {
        if (this.getById(dto.getId()) == null) {
            throw new RuntimeException("数据不存在");
        }
        Message entity = new Message();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        return this.updateById(entity);
    }

    @Override
    public Boolean delete(Long id) {
        if (this.getById(id) == null) {
            throw new RuntimeException("数据不存在");
        }
        return this.lambdaUpdate().eq(Message::getId, id).set(Message::getDeleted, co.handk.common.enums.DeleteEnum.DELETED.getCode()).update();
    }

    @Override
    public PageResult<MessageVO> pageQuery(MessageQueryDTO query) {
        Page<Message> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getDeleted, co.handk.common.enums.DeleteEnum.UNDELETED.getCode())
                .eq(query.getType() != null, Message::getType, query.getType())
                .eq(query.getUserId() != null, Message::getUserId, query.getUserId())
                .like(StringUtils.isNotBlank(query.getMessage()), Message::getMessage, query.getMessage())
                .eq(query.getSourceId() != null, Message::getSourceId, query.getSourceId())
                .eq(query.getIsRead() != null, Message::getIsRead, query.getIsRead())
                .eq(query.getState() != null, Message::getState, query.getState());
        PageSortUtil.applyTimeSort(wrapper, query, Message::getCreateTime, Message::getUpdateTime);
        Page<Message> resultPage =     messageMapper.selectPage(page, wrapper);
        List<MessageVO> records = resultPage.getRecords().stream().map(entity -> {
            MessageVO vo = new MessageVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), records);
    }
}
