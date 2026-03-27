package co.handk.backend.controller;

import co.handk.backend.entity.Config;
import co.handk.common.model.dto.ConfigDTO;
import co.handk.backend.service.ConfigService;
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
@RequestMapping("/config")
public class ConfigController {

    @Autowired
    private ConfigService configService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid ConfigDTO dto) {
        return configService.create(dto);
    }

    @GetMapping("/{id}")
    public Config get(@PathVariable @NotNull Long id) {
        return configService.get(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid ConfigDTO dto) {
        return configService.update(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable @NotNull Long id) {
        return configService.delete(id);
    }

    @GetMapping("/list")
    public List<Config> list() {
        return configService.listAll();
    }

    @GetMapping("/page")
    public PageResult<Config> page(@Valid PageQuery query) {
        return configService.pageQuery(query);
    }
}
