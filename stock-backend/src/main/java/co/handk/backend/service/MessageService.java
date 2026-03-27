package co.handk.backend.service;

import co.handk.backend.entity.Message;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MessageService extends IService<Message> {

    Boolean create(Message entity);

    Message get(Long id);

    Boolean update(Message entity);

    Boolean delete(Long id);

    List<Message> listAll();

    PageResult<Message> pageQuery(PageQuery query);
}
