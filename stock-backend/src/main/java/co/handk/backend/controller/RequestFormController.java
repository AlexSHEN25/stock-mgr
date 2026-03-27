package co.handk.backend.controller;

import co.handk.backend.entity.RequestForm;
import co.handk.backend.service.RequestFormService;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/requestForm")
public class RequestFormController {

    @Autowired
    private RequestFormService requestFormService;

    @PostMapping
    public Boolean create(@RequestBody RequestForm entity) {
        return requestFormService.create(entity);
    }

    @GetMapping("/{id}")
    public RequestForm get(@PathVariable Long id) {
        return requestFormService.get(id);
    }

    @PutMapping
    public Boolean update(@RequestBody RequestForm entity) {
        return requestFormService.update(entity);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable Long id) {
        return requestFormService.delete(id);
    }

    @GetMapping("/list")
    public List<RequestForm> list() {
        return requestFormService.listAll();
    }

    @GetMapping("/page")
    public PageResult<RequestForm> page(PageQuery query) {
        return requestFormService.pageQuery(query);
    }
}
