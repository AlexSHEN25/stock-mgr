package co.handk.backend.controller;

import co.handk.backend.entity.RequestItem;
import co.handk.backend.service.RequestItemService;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/requestItem")
public class RequestItemController {

    @Autowired
    private RequestItemService requestItemService;

    @PostMapping
    public Boolean create(@RequestBody RequestItem entity) {
        return requestItemService.create(entity);
    }

    @GetMapping("/{id}")
    public RequestItem get(@PathVariable Long id) {
        return requestItemService.get(id);
    }

    @PutMapping
    public Boolean update(@RequestBody RequestItem entity) {
        return requestItemService.update(entity);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable Long id) {
        return requestItemService.delete(id);
    }

    @GetMapping("/list")
    public List<RequestItem> list() {
        return requestItemService.listAll();
    }

    @GetMapping("/page")
    public PageResult<RequestItem> page(PageQuery query) {
        return requestItemService.pageQuery(query);
    }
}
