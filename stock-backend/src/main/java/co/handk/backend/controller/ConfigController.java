package co.handk.backend.controller;

import co.handk.common.model.vo.ConfigVO;
import co.handk.common.model.dto.create.CreateConfigDTO;
import co.handk.common.model.dto.update.UpdateConfigDTO;
import co.handk.backend.service.ConfigService;
import co.handk.common.model.dto.query.ConfigQueryDTO;
import co.handk.common.model.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
@RestController
@Validated
@RequestMapping("/config")
public class ConfigController {
    @Autowired
    private ConfigService configService;
    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateConfigDTO dto) {
        return configService.create(dto);
    }
    @GetMapping("/{id}")
    public ConfigVO get(@PathVariable @NotNull Long id) {
        return configService.get(id);
    }
    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateConfigDTO dto) {
        return configService.update(dto);
    }
    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable @NotNull Long id) {
        return configService.delete(id);
    }
    @GetMapping("/page")
    public PageResult<ConfigVO> page(@Valid ConfigQueryDTO query) {
        return configService.pageQuery(query);
    }
}
