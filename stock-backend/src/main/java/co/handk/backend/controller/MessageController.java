package co.handk.backend.controller;

import co.handk.backend.service.MessageService;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateMessageDTO;
import co.handk.common.model.dto.query.MessageQueryDTO;
import co.handk.common.model.dto.update.UpdateMessageDTO;
import co.handk.common.model.vo.MessageVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/message")
public class MessageController {
    @Autowired
    private MessageService messageService;
    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateMessageDTO dto) {
        return messageService.saveByDto(dto);
    }
    @GetMapping("/{id}")
    public MessageVO get(@PathVariable("id") @NotNull Long id) {
        return messageService.getVOById(id);
    }
    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateMessageDTO dto) {
        return messageService.updateByDto(dto);
    }
    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable("id") @NotNull Long id) {
        return messageService.deleteByIdLogic(id) > 0;
    }
    @GetMapping("/page")
    public PageResult<MessageVO> page(@Valid MessageQueryDTO query) {
        return messageService.page(query);
    }
}

