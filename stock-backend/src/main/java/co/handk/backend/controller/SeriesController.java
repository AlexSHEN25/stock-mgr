package co.handk.backend.controller;

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
public class SeriesController {
    @Autowired
    private SeriesService seriesService;
    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateSeriesDTO dto) {
        return seriesService.saveByDto(dto);
    }
    @GetMapping("/{id}")
    public SeriesVO get(@PathVariable @NotNull Long id) {
        return seriesService.getVOById(id);
    }
    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateSeriesDTO dto) {
        return seriesService.updateByDto(dto);
    }
    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable @NotNull Long id) {
        return seriesService.deleteByIdLogic(id) > 0;
    }
    @GetMapping("/page")
    public PageResult<SeriesVO> page(@Valid SeriesQueryDTO query) {
        return seriesService.page(query);
    }
}

