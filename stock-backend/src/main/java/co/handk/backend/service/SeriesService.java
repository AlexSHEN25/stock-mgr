package co.handk.backend.service;

import co.handk.backend.entity.Series;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateSeriesDTO;
import co.handk.common.model.dto.query.SeriesQueryDTO;
import co.handk.common.model.dto.update.UpdateSeriesDTO;
import co.handk.common.model.vo.SeriesVO;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface SeriesService extends IService<Series> {
    Boolean create(@NotNull CreateSeriesDTO dto);
    SeriesVO get(@NotNull Long id);
    Boolean update(@NotNull UpdateSeriesDTO dto);
    Boolean delete(@NotNull Long id);
    PageResult<SeriesVO> pageQuery(@NotNull SeriesQueryDTO query);
}
