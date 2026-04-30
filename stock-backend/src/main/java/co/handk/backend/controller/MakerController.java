package co.handk.backend.controller;

import co.handk.backend.service.MakerService;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateMakerDTO;
import co.handk.common.model.dto.query.MakerQueryDTO;
import co.handk.common.model.dto.update.UpdateMakerDTO;
import co.handk.common.model.vo.MakerVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/maker")
public class MakerController {
    @Autowired
    private MakerService makerService;
    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateMakerDTO dto) {
        return makerService.saveByDto(dto);
    }
    @GetMapping("/{id}")
    public MakerVO get(@PathVariable("id") @NotNull Long id) {
        return makerService.getVOById(id);
    }
    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateMakerDTO dto) {
        return makerService.updateByDto(dto);
    }
    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable("id") @NotNull Long id) {
        return makerService.deleteByIdLogic(id) > 0;
    }
    @GetMapping("/page")
    public PageResult<MakerVO> page(@Valid MakerQueryDTO query) {
        return makerService.page(query);
    }
}

