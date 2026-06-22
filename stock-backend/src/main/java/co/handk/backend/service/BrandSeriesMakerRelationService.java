package co.handk.backend.service;

import co.handk.backend.entity.BrandSeriesMakerRelation;
import co.handk.common.model.vo.BrandSeriesMakerRelationVO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface BrandSeriesMakerRelationService extends BaseService<BrandSeriesMakerRelation, BrandSeriesMakerRelationVO> {
}
