package co.handk.backend.service.impl;

import co.handk.backend.entity.OperateLog;
import co.handk.backend.mapper.OperateLogMapper;
import co.handk.backend.service.OperateLogService;
import co.handk.common.model.vo.OperateLogVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class OperateLogServiceImpl extends BaseServiceImpl<OperateLogMapper, OperateLog, OperateLogVO>
        implements OperateLogService {

    @Override
    protected OperateLogVO toVO(OperateLog entity) {
        if (entity == null) {
            return null;
        }
        OperateLogVO vo = new OperateLogVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    protected <D> OperateLog toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        OperateLog entity = new OperateLog();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}