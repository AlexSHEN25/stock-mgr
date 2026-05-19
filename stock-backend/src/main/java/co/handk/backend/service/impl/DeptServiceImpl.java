package co.handk.backend.service.impl;

import co.handk.backend.entity.Dept;
import co.handk.backend.entity.User;
import co.handk.backend.mapper.DeptMapper;
import co.handk.backend.mapper.UserMapper;
import co.handk.backend.service.DeptService;
import co.handk.common.enums.DeleteEnum;
import co.handk.common.enums.StatusEnum;
import co.handk.common.model.dto.create.CreateDeptDTO;
import co.handk.common.model.dto.query.DeptQueryDTO;
import co.handk.common.model.dto.update.UpdateDeptDTO;
import co.handk.common.model.vo.DeptVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class DeptServiceImpl extends BaseServiceImpl<DeptMapper, Dept, DeptVO>
        implements DeptService {

    private final UserMapper userMapper;

    @Override
    protected DeptVO toVO(Dept entity) {
        if (entity == null) {
            return null;
        }
        DeptVO vo = new DeptVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setLeaderId(entity.getLeaderId());
        if (entity.getLeaderId() != null) {
            User leadUser = userMapper.selectById(entity.getLeaderId());
            if (leadUser != null && DeleteEnum.UNDELETED.getCode().equals(leadUser.getDeleted())) {
                vo.setLeaderName(leadUser.getUsername());
            }
        }
        return vo;
    }

    @Override
    protected <D> Dept toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        Dept entity = new Dept();
        BeanUtils.copyProperties(dto, entity);
        if (dto instanceof CreateDeptDTO createDeptDTO) {
            entity.setLeaderId(createDeptDTO.getLeaderId());
        } else if (dto instanceof UpdateDeptDTO updateDeptDTO) {
            entity.setLeaderId(updateDeptDTO.getLeaderId());
        }
        return entity;
    }

    @Override
    protected <Q> QueryWrapper<Dept> buildWrapper(Q dto) {
        QueryWrapper<Dept> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", DeleteEnum.UNDELETED.getCode());
        if (!(dto instanceof DeptQueryDTO queryDTO)) {
            return wrapper;
        }
        String name = queryDTO.getName();
        String code = queryDTO.getCode();
        Long leaderId = queryDTO.getLeaderId();
        StatusEnum status = queryDTO.getStatus();

        wrapper.like(StringUtils.hasText(name), "name", name == null ? null : name.trim())
                .like(StringUtils.hasText(code), "code", code == null ? null : code.trim())
                .eq(leaderId != null, "leader_id", leaderId)
                .eq(queryDTO.getSort() != null, "sort", queryDTO.getSort())
                .eq(status != null, "status", status == null ? null : status.getCode());
        return wrapper;
    }
}
