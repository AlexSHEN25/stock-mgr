package co.handk.backend.service.impl;

import co.handk.backend.entity.Message;
import co.handk.common.model.dto.MessageDTO;
import co.handk.common.model.vo.MessageVO;
import co.handk.backend.mapper.MessageMapper;
import co.handk.backend.service.MessageService;
import co.handk.common.model.PageQuery;
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
    public Boolean create(MessageDTO dto) {
        Message entity = new Message();
        BeanUtils.copyProperties(dto, entity);
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
    public Boolean update(MessageDTO dto) {
        if (this.getById(dto.getId()) == null) {
            throw new RuntimeException("数据不存在");
        }
        Message entity = new Message();
        BeanUtils.copyProperties(dto, entity);
        return this.updateById(entity);
    }

    @Override
    public Boolean delete(Long id) {
        if (this.getById(id) == null) {
            throw new RuntimeException("数据不存在");
        }
        return this.removeById(id);
    }

    @Override
    public List<MessageVO> listAll() {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getDeleted, 0).orderByDesc(Message::getUpdateTime);
        return     messageMapper.selectList(wrapper).stream().map(entity -> {
            MessageVO vo = new MessageVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public PageResult<MessageVO> pageQuery(PageQuery query) {
        Page<Message> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getDeleted, 0).orderByDesc(Message::getUpdateTime);
        Page<Message> resultPage =     messageMapper.selectPage(page, wrapper);
        List<MessageVO> records = resultPage.getRecords().stream().map(entity -> {
            MessageVO vo = new MessageVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), records);
    }
}
