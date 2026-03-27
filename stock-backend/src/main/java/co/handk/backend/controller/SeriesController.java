package co.handk.backend.controller;

import co.handk.backend.entity.Series;
import co.handk.backend.service.SeriesService;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/series")
public class SeriesController {

    @Autowired
    private SeriesService seriesService;

    @PostMapping
    public Boolean create(@RequestBody Series entity) {
        return seriesService.create(entity);
    }

    @GetMapping("/{id}")
    public Series get(@PathVariable Long id) {
        return seriesService.get(id);
    }

    @PutMapping
    public Boolean update(@RequestBody Series entity) {
        return seriesService.update(entity);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable Long id) {
        return seriesService.delete(id);
    }

    @GetMapping("/list")
    public List<Series> list() {
        return seriesService.listAll();
    }

    @GetMapping("/page")
    public PageResult<Series> page(PageQuery query) {
        return seriesService.pageQuery(query);
    }
}
