package co.handk.backend.service.impl;

import co.handk.backend.entity.UserRole;
import co.handk.common.model.dto.UserRoleDTO;
import co.handk.common.model.vo.UserRoleVO;
import co.handk.backend.mapper.UserRoleMapper;
import co.handk.backend.service.UserRoleService;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements UserRoleService {

    private final UserRoleMapper userRoleMapper;

    @Override
    public Boolean create(UserRoleDTO dto) {
        UserRole entity = new UserRole();
        BeanUtils.copyProperties(dto, entity);
        entity.setId(null);
        return this.save(entity);
    }

    @Override
    public UserRoleVO get(Long id) {
        UserRole entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("数据不存在");
        }
        UserRoleVO vo = new UserRoleVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public Boolean update(UserRoleDTO dto) {
        if (this.getById(dto.getId()) == null) {
            throw new RuntimeException("数据不存在");
        }
        UserRole entity = new UserRole();
        BeanUtils.copyProperties(dto, entity);
        return this.updateById(entity);
    }

    @Override
    public Boolean delete(Long id) {
        if (this.getById(id) == null) {
            throw new RuntimeException("数据不存在");
        }
        return this.removeById(id);
    }

    @Override
    public List<UserRoleVO> listAll() {
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getDeleted, 0).orderByDesc(UserRole::getUpdateTime);
        return     userRoleMapper.selectList(wrapper).stream().map(entity -> {
            UserRoleVO vo = new UserRoleVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public PageResult<UserRoleVO> pageQuery(PageQuery query) {
        Page<UserRole> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getDeleted, 0).orderByDesc(UserRole::getUpdateTime);
        Page<UserRole> resultPage =     userRoleMapper.selectPage(page, wrapper);
        List<UserRoleVO> records = resultPage.getRecords().stream().map(entity -> {
            UserRoleVO vo = new UserRoleVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), records);
    }
}
