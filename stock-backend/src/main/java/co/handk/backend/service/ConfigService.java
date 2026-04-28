package co.handk.backend.service;

import co.handk.backend.entity.Config;
import co.handk.common.model.vo.ConfigVO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface ConfigService extends BaseService<Config, ConfigVO> {
}