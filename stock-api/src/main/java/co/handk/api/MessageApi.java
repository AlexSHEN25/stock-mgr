package co.handk.api;

import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateMessageDTO;
import co.handk.common.model.dto.query.MessageQueryDTO;
import co.handk.common.model.dto.update.UpdateMessageDTO;
import co.handk.common.model.vo.MessageVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RequestMapping("/message")
public interface MessageApi {
    @PostMapping
    Boolean create(@RequestBody @NotNull @Valid CreateMessageDTO dto);
    @GetMapping("/{id}")
    MessageVO get(@PathVariable @NotNull Long id);
    @PutMapping
    Boolean update(@RequestBody @NotNull @Valid UpdateMessageDTO dto);
    @DeleteMapping("/{id}")
    Boolean delete(@PathVariable @NotNull Long id);
    @GetMapping("/page")
    PageResult<MessageVO> page(@Valid MessageQueryDTO query);
}
