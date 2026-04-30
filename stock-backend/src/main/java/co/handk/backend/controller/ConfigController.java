package co.handk.backend.controller;

import co.handk.backend.service.ConfigService;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateConfigDTO;
import co.handk.common.model.dto.query.ConfigQueryDTO;
import co.handk.common.model.dto.update.UpdateConfigDTO;
import co.handk.common.model.vo.ConfigVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/config")
public class ConfigController {
    @Autowired
    private ConfigService configService;
    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateConfigDTO dto) {
        return configService.saveByDto(dto);
    }
    @GetMapping("/{id}")
    public ConfigVO get(@PathVariable("id") @NotNull Long id) {
        return configService.getVOById(id);
    }
    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateConfigDTO dto) {
        return configService.updateByDto(dto);
    }
    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable("id") @NotNull Long id) {
        return configService.deleteByIdLogic(id) > 0;
    }
    @GetMapping("/page")
    public PageResult<ConfigVO> page(@Valid ConfigQueryDTO query) {
        return configService.page(query);
    }
}

