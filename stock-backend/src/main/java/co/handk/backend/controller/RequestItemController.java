package co.handk.backend.controller;

import co.handk.backend.entity.RequestItem;
import co.handk.common.model.dto.RequestItemDTO;
import co.handk.backend.service.RequestItemService;
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
@RequestMapping("/requestItem")
public class RequestItemController {

    @Autowired
    private RequestItemService requestItemService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid RequestItemDTO dto) {
        return requestItemService.create(dto);
    }

    @GetMapping("/{id}")
    public RequestItem get(@PathVariable @NotNull Long id) {
        return requestItemService.get(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid RequestItemDTO dto) {
        return requestItemService.update(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable @NotNull Long id) {
        return requestItemService.delete(id);
    }

    @GetMapping("/list")
    public List<RequestItem> list() {
        return requestItemService.listAll();
    }

    @GetMapping("/page")
    public PageResult<RequestItem> page(@Valid PageQuery query) {
        return requestItemService.pageQuery(query);
    }
}
