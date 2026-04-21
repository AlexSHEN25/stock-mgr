package co.handk.backend.service.impl;

import co.handk.backend.entity.Role;
import co.handk.backend.mapper.RoleMapper;
import co.handk.backend.service.RoleService;
import co.handk.backend.util.EnumFieldMapper;
import co.handk.backend.util.PageSortUtil;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateRoleDTO;
import co.handk.common.model.dto.query.RoleQueryDTO;
import co.handk.common.model.dto.update.UpdateRoleDTO;
import co.handk.common.model.vo.RoleVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    private final RoleMapper roleMapper;

    @Override
    public Boolean create(CreateRoleDTO dto) {
        Role entity = new Role();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        entity.setId(null);
        return this.save(entity);
    }

    @Override
    public RoleVO get(Long id) {
        Role entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("数据不存在");
        }
        RoleVO vo = new RoleVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public Boolean update(UpdateRoleDTO dto) {
        if (this.getById(dto.getId()) == null) {
            throw new RuntimeException("数据不存在");
        }
        Role entity = new Role();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        return this.updateById(entity);
    }

    @Override
    public Boolean delete(Long id) {
        if (this.getById(id) == null) {
            throw new RuntimeException("数据不存在");
        }
        return this.lambdaUpdate().eq(Role::getId, id).set(Role::getDeleted, co.handk.common.enums.DeleteEnum.DELETED.getCode()).update();
    }

    @Override
    public PageResult<RoleVO> pageQuery(RoleQueryDTO query) {
        Page<Role> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getDeleted, co.handk.common.enums.DeleteEnum.UNDELETED.getCode())
                .like(StringUtils.isNotBlank(query.getName()), Role::getName, query.getName())
                .like(StringUtils.isNotBlank(query.getCode()), Role::getCode, query.getCode())
                .like(StringUtils.isNotBlank(query.getRemark()), Role::getRemark, query.getRemark())
                .eq(query.getStatus() != null, Role::getStatus, (query.getStatus() == null ? null : query.getStatus().getCode()));
        PageSortUtil.applyTimeSort(wrapper, query, Role::getCreateTime, Role::getUpdateTime);
        Page<Role> resultPage =     roleMapper.selectPage(page, wrapper);
        List<RoleVO> records = resultPage.getRecords().stream().map(entity -> {
            RoleVO vo = new RoleVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), records);
    }
}
