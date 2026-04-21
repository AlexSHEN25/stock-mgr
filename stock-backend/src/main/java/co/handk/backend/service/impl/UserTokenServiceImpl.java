package co.handk.backend.service.impl;

import co.handk.backend.entity.UserToken;
import co.handk.backend.mapper.UserTokenMapper;
import co.handk.backend.service.UserTokenService;
import co.handk.backend.util.EnumFieldMapper;
import co.handk.backend.util.PageSortUtil;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateUserTokenDTO;
import co.handk.common.model.dto.query.UserTokenQueryDTO;
import co.handk.common.model.dto.update.UpdateUserTokenDTO;
import co.handk.common.model.vo.UserTokenVO;
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
        return this.lambdaUpdate().eq(UserToken::getId, id).set(UserToken::getDeleted, co.handk.common.enums.DeleteEnum.DELETED.getCode()).update();
    }

    @Override
    public PageResult<UserTokenVO> pageQuery(UserTokenQueryDTO query) {
        Page<UserToken> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<UserToken> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserToken::getDeleted, co.handk.common.enums.DeleteEnum.UNDELETED.getCode())
                .like(StringUtils.isNotBlank(query.getToken()), UserToken::getToken, query.getToken())
                .eq(query.getUserId() != null, UserToken::getUserId, query.getUserId())
                .eq(query.getLoginTime() != null, UserToken::getLoginTime, query.getLoginTime())
                .eq(query.getExpireTime() != null, UserToken::getExpireTime, query.getExpireTime())
                .like(StringUtils.isNotBlank(query.getLoginIp()), UserToken::getLoginIp, query.getLoginIp())
                .eq(query.getStatus() != null, UserToken::getStatus, (query.getStatus() == null ? null : query.getStatus().getCode()));
        PageSortUtil.applyTimeSort(wrapper, query, UserToken::getCreateTime, UserToken::getUpdateTime);
        Page<UserToken> resultPage =     userTokenMapper.selectPage(page, wrapper);
        List<UserTokenVO> records = resultPage.getRecords().stream().map(entity -> {
            UserTokenVO vo = new UserTokenVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), records);
    }
}
