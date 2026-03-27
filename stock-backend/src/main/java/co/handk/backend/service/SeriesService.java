package co.handk.backend.service;

import co.handk.backend.entity.Series;
import co.handk.common.model.dto.SeriesDTO;
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

    Series get(@NotNull Long id);

    Boolean update(@NotNull SeriesDTO dto);

    Boolean delete(@NotNull Long id);

    List<Series> listAll();

    PageResult<Series> pageQuery(@NotNull PageQuery query);
}
