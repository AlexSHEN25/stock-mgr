package co.handk.backend.service;

import co.handk.backend.entity.Series;
import co.handk.common.model.dto.SeriesDTO;
import co.handk.common.model.vo.SeriesVO;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Service
@Validated
public interface SeriesService extends IService<Series> {

    Boolean create(@NotNull SeriesDTO dto);

    SeriesVO get(@NotNull Long id);

    Boolean update(@NotNull SeriesDTO dto);

    Boolean delete(@NotNull Long id);
    List<SeriesVO> listAll();

    PageResult<SeriesVO> pageQuery(@NotNull PageQuery query);
}
