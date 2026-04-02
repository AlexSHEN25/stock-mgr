package co.handk.backend.service;
import co.handk.backend.entity.Message;
import co.handk.common.model.dto.create.CreateMessageDTO;
import co.handk.common.model.dto.update.UpdateMessageDTO;
import co.handk.common.model.vo.MessageVO;
import co.handk.common.model.dto.query.MessageQueryDTO;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotNull;
@Service
@Validated
public interface MessageService extends IService<Message> {
    Boolean create(@NotNull CreateMessageDTO dto);
    MessageVO get(@NotNull Long id);
    Boolean update(@NotNull UpdateMessageDTO dto);
    Boolean delete(@NotNull Long id);
    PageResult<MessageVO> pageQuery(@NotNull MessageQueryDTO query);
}
