package co.handk.backend.controller;

import co.handk.backend.entity.Config;
import co.handk.backend.service.ConfigService;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/config")
public class ConfigController {

    @Autowired
    private ConfigService configService;

    @PostMapping
    public Boolean create(@RequestBody Config entity) {
        return configService.create(entity);
    }

    @GetMapping("/{id}")
    public Config get(@PathVariable Long id) {
        return configService.get(id);
    }

    @PutMapping
    public Boolean update(@RequestBody Config entity) {
        return configService.update(entity);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable Long id) {
        return configService.delete(id);
    }

    @GetMapping("/list")
    public List<Config> list() {
        return configService.listAll();
    }

    @GetMapping("/page")
    public PageResult<Config> page(PageQuery query) {
        return configService.pageQuery(query);
    }
}
