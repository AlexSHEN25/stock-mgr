package co.handk.backend.controller;

import co.handk.backend.entity.Series;
import co.handk.common.model.dto.SeriesDTO;
import co.handk.backend.service.SeriesService;
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
@RequestMapping("/series")
public class SeriesController {

    @Autowired
    private SeriesService seriesService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid SeriesDTO dto) {
        return seriesService.create(dto);
    }

    @GetMapping("/{id}")
    public Series get(@PathVariable @NotNull Long id) {
        return seriesService.get(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid SeriesDTO dto) {
        return seriesService.update(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable @NotNull Long id) {
        return seriesService.delete(id);
    }

    @GetMapping("/list")
    public List<Series> list() {
        return seriesService.listAll();
    }

    @GetMapping("/page")
    public PageResult<Series> page(@Valid PageQuery query) {
        return seriesService.pageQuery(query);
    }
}
