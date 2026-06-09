package co.handk.backend.service;

import co.handk.backend.entity.SeriesBrandRelation;
import co.handk.common.model.vo.SeriesBrandRelationVO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface SeriesBrandRelationService extends BaseService<SeriesBrandRelation, SeriesBrandRelationVO> {
}
