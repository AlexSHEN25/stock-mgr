package co.handk.backend.service;

import co.handk.backend.entity.BrandMakerRelation;
import co.handk.common.model.vo.BrandMakerRelationVO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface BrandMakerRelationService extends BaseService<BrandMakerRelation, BrandMakerRelationVO> {
}