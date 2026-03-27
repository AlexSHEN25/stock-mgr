package co.handk.backend.service;

import co.handk.backend.entity.Message;
import co.handk.common.model.dto.MessageDTO;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Service
@Validated
public interface MessageService extends IService<Message> {

    Boolean create(@NotNull MessageDTO dto);

    Message get(@NotNull Long id);

    Boolean update(@NotNull MessageDTO dto);

    Boolean delete(@NotNull Long id);

    List<Message> listAll();

    PageResult<Message> pageQuery(@NotNull PageQuery query);
}
