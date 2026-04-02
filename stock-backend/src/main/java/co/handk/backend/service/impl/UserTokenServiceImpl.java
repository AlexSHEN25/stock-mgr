package co.handk.backend.service.impl;

import co.handk.backend.util.EnumFieldMapper;

import co.handk.backend.entity.UserToken;
import co.handk.common.model.dto.create.CreateUserTokenDTO;
import co.handk.common.model.dto.update.UpdateUserTokenDTO;
import co.handk.common.model.vo.UserTokenVO;
import co.handk.backend.mapper.UserTokenMapper;
import co.handk.backend.service.UserTokenService;
import co.handk.common.model.dto.query.UserTokenQueryDTO;
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
public class UserTokenServiceImpl extends ServiceImpl<UserTokenMapper, UserToken> implements UserTokenService {

    private final UserTokenMapper userTokenMapper;

    @Override
    public Boolean create(CreateUserTokenDTO dto) {
        UserToken entity = new UserToken();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        entity.setId(null);
        return this.save(entity);
    }

    @Override
    public UserTokenVO get(Long id) {
        UserToken entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("数据不存在");
        }
        UserTokenVO vo = new UserTokenVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public Boolean update(UpdateUserTokenDTO dto) {
        if (this.getById(dto.getId()) == null) {
            throw new RuntimeException("数据不存在");
        }
        UserToken entity = new UserToken();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        return this.updateById(entity);
    }

    @Override
    public Boolean delete(Long id) {
        if (this.getById(id) == null) {
            throw new RuntimeException("数据不存在");
        }
        return this.lambdaUpdate().eq(UserToken::getId, id).set(UserToken::getDeleted, 1).update();
    }

    @Override
    public PageResult<UserTokenVO> pageQuery(UserTokenQueryDTO query) {
        Page<UserToken> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<UserToken> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserToken::getDeleted, 0).orderByDesc(UserToken::getUpdateTime);
        Page<UserToken> resultPage =     userTokenMapper.selectPage(page, wrapper);
        List<UserTokenVO> records = resultPage.getRecords().stream().map(entity -> {
            UserTokenVO vo = new UserTokenVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), records);
    }
}
