package co.handk.backend.service;

import co.handk.backend.entity.Series;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface SeriesService extends IService<Series> {

    Boolean create(Series entity);

    Series get(Long id);

    Boolean update(Series entity);

    Boolean delete(Long id);

    List<Series> listAll();

    PageResult<Series> pageQuery(PageQuery query);
}
