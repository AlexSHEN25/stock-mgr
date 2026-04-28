package co.handk.backend.service;

import co.handk.backend.entity.Series;
import co.handk.common.model.vo.SeriesVO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface SeriesService extends BaseService<Series, SeriesVO> {
}