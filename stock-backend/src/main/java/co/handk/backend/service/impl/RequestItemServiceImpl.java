package co.handk.backend.service.impl;

import co.handk.backend.entity.RequestItem;
import co.handk.backend.mapper.RequestItemMapper;
import co.handk.backend.service.RequestItemService;
import co.handk.common.model.vo.RequestItemVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class RequestItemServiceImpl extends BaseServiceImpl<RequestItemMapper, RequestItem, RequestItemVO>
        implements RequestItemService {

    @Override
    protected RequestItemVO toVO(RequestItem entity) {
        if (entity == null) {
            return null;
        }
        RequestItemVO vo = new RequestItemVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    protected <D> RequestItem toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        RequestItem entity = new RequestItem();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}