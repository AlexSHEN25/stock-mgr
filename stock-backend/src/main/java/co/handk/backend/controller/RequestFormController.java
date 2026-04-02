package co.handk.backend.controller;

import co.handk.api.RequestFormApi;
import co.handk.common.model.vo.RequestFormVO;
import co.handk.common.model.dto.create.CreateRequestFormDTO;
import co.handk.common.model.dto.update.UpdateRequestFormDTO;
import co.handk.backend.service.RequestFormService;
import co.handk.common.model.dto.query.RequestFormQueryDTO;
import co.handk.common.model.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
@RestController
@Validated
@RequestMapping("/requestForm")
public class RequestFormController implements RequestFormApi {
    @Autowired
    private RequestFormService requestFormService;
    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateRequestFormDTO dto) {
        return requestFormService.create(dto);
    }
    @GetMapping("/{id}")
    public RequestFormVO get(@PathVariable @NotNull Long id) {
        return requestFormService.get(id);
    }
    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateRequestFormDTO dto) {
        return requestFormService.update(dto);
    }
    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable @NotNull Long id) {
        return requestFormService.delete(id);
    }
    @GetMapping("/page")
    public PageResult<RequestFormVO> page(@Valid RequestFormQueryDTO query) {
        return requestFormService.pageQuery(query);
    }
}
