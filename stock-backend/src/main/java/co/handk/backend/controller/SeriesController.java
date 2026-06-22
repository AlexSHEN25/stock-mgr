package co.handk.backend.controller;

import co.handk.backend.service.SeriesService;
import co.handk.common.constant.NumberConstant;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateSeriesDTO;
import co.handk.common.model.dto.query.SeriesQueryDTO;
import co.handk.common.model.dto.update.UpdateSeriesDTO;
import co.handk.common.model.vo.SeriesVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/series")
@RequiredArgsConstructor
public class SeriesController {
    private final SeriesService seriesService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateSeriesDTO dto) {
        return seriesService.saveByDto(dto);
    }

    @GetMapping("/{id}")
    public SeriesVO get(@PathVariable("id") @NotNull Long id) {
        return seriesService.getVOById(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateSeriesDTO dto) {
        return seriesService.updateByDto(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable("id") @NotNull Long id) {
        return seriesService.deleteByIdLogic(id) > NumberConstant.ZERO;
    }

    @DeleteMapping("/batch")
    public Boolean deleteBatch(@RequestBody @NotNull List<Long> ids) {
        return seriesService.deleteBatchLogic(ids) > NumberConstant.ZERO;
    }

    @GetMapping("/page")
    public PageResult<SeriesVO> page(@Valid SeriesQueryDTO query) {
        return seriesService.page(query);
    }
}
