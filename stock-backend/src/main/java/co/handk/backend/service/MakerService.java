package co.handk.backend.service;

import co.handk.backend.entity.Maker;
import co.handk.common.model.vo.MakerVO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface MakerService extends BaseService<Maker, MakerVO> {
}