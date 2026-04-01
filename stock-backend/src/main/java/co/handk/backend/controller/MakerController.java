package co.handk.backend.controller;

import co.handk.backend.entity.Maker;
import co.handk.common.model.vo.MakerVO;
import co.handk.common.model.dto.MakerDTO;
import co.handk.backend.service.MakerService;
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
@RequestMapping("/maker")
public class MakerController {

    @Autowired
    private MakerService makerService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid MakerDTO dto) {
        return makerService.create(dto);
    }

    @GetMapping("/{id}")
    public MakerVO get(@PathVariable @NotNull Long id) {
        return makerService.get(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid MakerDTO dto) {
        return makerService.update(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable @NotNull Long id) {
        return makerService.delete(id);
    }

    @GetMapping("/list")
    public List<MakerVO> list() {
        return makerService.listAll();
    }

    @GetMapping("/page")
    public PageResult<MakerVO> page(@Valid PageQuery query) {
        return makerService.pageQuery(query);
    }
}
