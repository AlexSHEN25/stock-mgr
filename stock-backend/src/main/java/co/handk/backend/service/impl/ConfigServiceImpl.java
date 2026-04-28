package co.handk.backend.service.impl;

import co.handk.backend.entity.Config;
import co.handk.backend.mapper.ConfigMapper;
import co.handk.backend.service.ConfigService;
import co.handk.common.model.vo.ConfigVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class ConfigServiceImpl extends BaseServiceImpl<ConfigMapper, Config, ConfigVO>
        implements ConfigService {

    @Override
    protected ConfigVO toVO(Config entity) {
        if (entity == null) {
            return null;
        }
        ConfigVO vo = new ConfigVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    protected <D> Config toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        Config entity = new Config();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}