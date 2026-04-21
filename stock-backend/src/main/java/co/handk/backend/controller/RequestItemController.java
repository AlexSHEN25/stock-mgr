package co.handk.backend.controller;

import co.handk.backend.service.RequestItemService;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateRequestItemDTO;
import co.handk.common.model.dto.query.RequestItemQueryDTO;
import co.handk.common.model.dto.update.UpdateRequestItemDTO;
import co.handk.common.model.vo.RequestItemVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/requestItem")
public class RequestItemController {
    @Autowired
    private RequestItemService requestItemService;
    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateRequestItemDTO dto) {
        return requestItemService.create(dto);
    }
    @GetMapping("/{id}")
    public RequestItemVO get(@PathVariable @NotNull Long id) {
        return requestItemService.get(id);
    }
    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateRequestItemDTO dto) {
        return requestItemService.update(dto);
    }
    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable @NotNull Long id) {
        return requestItemService.delete(id);
    }
    @GetMapping("/page")
    public PageResult<RequestItemVO> page(@Valid RequestItemQueryDTO query) {
        return requestItemService.pageQuery(query);
    }
}
