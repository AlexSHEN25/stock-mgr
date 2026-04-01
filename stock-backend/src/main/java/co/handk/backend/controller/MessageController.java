package co.handk.backend.controller;

import co.handk.backend.entity.Message;
import co.handk.common.model.vo.MessageVO;
import co.handk.common.model.dto.MessageDTO;
import co.handk.backend.service.MessageService;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@Validated
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid MessageDTO dto) {
        return messageService.create(dto);
    }

    @GetMapping("/{id}")
    public MessageVO get(@PathVariable @NotNull Long id) {
        return messageService.get(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid MessageDTO dto) {
        return messageService.update(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable @NotNull Long id) {
        return messageService.delete(id);
    }

    @GetMapping("/list")
    public List<MessageVO> list() {
        return messageService.listAll();
    }

    @GetMapping("/page")
    public PageResult<MessageVO> page(@Valid PageQuery query) {
        return messageService.pageQuery(query);
    }
}
