package co.handk.backend.controller;

import co.handk.backend.entity.RequestForm;
import co.handk.common.model.dto.RequestFormDTO;
import co.handk.backend.service.RequestFormService;
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
@RequestMapping("/requestForm")
public class RequestFormController {

    @Autowired
    private RequestFormService requestFormService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid RequestFormDTO dto) {
        return requestFormService.create(dto);
    }

    @GetMapping("/{id}")
    public RequestForm get(@PathVariable @NotNull Long id) {
        return requestFormService.get(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid RequestFormDTO dto) {
        return requestFormService.update(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable @NotNull Long id) {
        return requestFormService.delete(id);
    }

    @GetMapping("/list")
    public List<RequestForm> list() {
        return requestFormService.listAll();
    }

    @GetMapping("/page")
    public PageResult<RequestForm> page(@Valid PageQuery query) {
        return requestFormService.pageQuery(query);
    }
}
