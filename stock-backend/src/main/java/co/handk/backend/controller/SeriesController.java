package co.handk.backend.controller;

import co.handk.api.SeriesApi;
import co.handk.common.model.vo.SeriesVO;
import co.handk.common.model.dto.create.CreateSeriesDTO;
import co.handk.common.model.dto.update.UpdateSeriesDTO;
import co.handk.backend.service.SeriesService;
import co.handk.common.model.dto.query.SeriesQueryDTO;
import co.handk.common.model.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
@RestController
@Validated
@RequestMapping("/series")
public class SeriesController implements SeriesApi {
    @Autowired
    private SeriesService seriesService;
    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateSeriesDTO dto) {
        return seriesService.create(dto);
    }
    @GetMapping("/{id}")
    public SeriesVO get(@PathVariable @NotNull Long id) {
        return seriesService.get(id);
    }
    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateSeriesDTO dto) {
        return seriesService.update(dto);
    }
    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable @NotNull Long id) {
        return seriesService.delete(id);
    }
    @GetMapping("/page")
    public PageResult<SeriesVO> page(@Valid SeriesQueryDTO query) {
        return seriesService.pageQuery(query);
    }
}
