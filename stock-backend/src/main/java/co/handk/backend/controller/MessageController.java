package co.handk.backend.controller;

import co.handk.backend.entity.Message;
import co.handk.backend.service.MessageService;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @PostMapping
    public Boolean create(@RequestBody Message entity) {
        return messageService.create(entity);
    }

    @GetMapping("/{id}")
    public Message get(@PathVariable Long id) {
        return messageService.get(id);
    }

    @PutMapping
    public Boolean update(@RequestBody Message entity) {
        return messageService.update(entity);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable Long id) {
        return messageService.delete(id);
    }

    @GetMapping("/list")
    public List<Message> list() {
        return messageService.listAll();
    }

    @GetMapping("/page")
    public PageResult<Message> page(PageQuery query) {
        return messageService.pageQuery(query);
    }
}
