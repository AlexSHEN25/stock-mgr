package co.handk.backend.service.impl;

import org.apache.commons.lang3.StringUtils;

import co.handk.backend.util.PageSortUtil;

import co.handk.backend.util.EnumFieldMapper;

import co.handk.backend.entity.OperateLog;
import co.handk.common.model.dto.create.CreateOperateLogDTO;
import co.handk.common.model.dto.update.UpdateOperateLogDTO;
import co.handk.common.model.vo.OperateLogVO;
import co.handk.backend.mapper.OperateLogMapper;
import co.handk.backend.service.OperateLogService;
import co.handk.common.model.dto.query.OperateLogQueryDTO;
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
public class OperateLogServiceImpl extends ServiceImpl<OperateLogMapper, OperateLog> implements OperateLogService {

    private final OperateLogMapper operateLogMapper;

    @Override
    public Boolean create(CreateOperateLogDTO dto) {
        OperateLog entity = new OperateLog();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        entity.setId(null);
        return this.save(entity);
    }

    @Override
    public OperateLogVO get(Long id) {
        OperateLog entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("数据不存在");
        }
        OperateLogVO vo = new OperateLogVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public Boolean update(UpdateOperateLogDTO dto) {
        if (this.getById(dto.getId()) == null) {
            throw new RuntimeException("数据不存在");
        }
        OperateLog entity = new OperateLog();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        return this.updateById(entity);
    }

    @Override
    public Boolean delete(Long id) {
        if (this.getById(id) == null) {
            throw new RuntimeException("数据不存在");
        }
        return this.lambdaUpdate().eq(OperateLog::getId, id).set(OperateLog::getDeleted, co.handk.common.enums.DeleteEnum.DELETED.getCode()).update();
    }

    @Override
    public PageResult<OperateLogVO> pageQuery(OperateLogQueryDTO query) {
        Page<OperateLog> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<OperateLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OperateLog::getDeleted, co.handk.common.enums.DeleteEnum.UNDELETED.getCode())
                .eq(query.getUserId() != null, OperateLog::getUserId, query.getUserId())
                .like(StringUtils.isNotBlank(query.getUsername()), OperateLog::getUsername, query.getUsername())
                .like(StringUtils.isNotBlank(query.getModule()), OperateLog::getModule, query.getModule())
                .like(StringUtils.isNotBlank(query.getOperation()), OperateLog::getOperation, query.getOperation())
                .like(StringUtils.isNotBlank(query.getMethod()), OperateLog::getMethod, query.getMethod())
                .like(StringUtils.isNotBlank(query.getRequestUrl()), OperateLog::getRequestUrl, query.getRequestUrl())
                .like(StringUtils.isNotBlank(query.getRequestIp()), OperateLog::getRequestIp, query.getRequestIp())
                .like(StringUtils.isNotBlank(query.getRequestParam()), OperateLog::getRequestParam, query.getRequestParam())
                .like(StringUtils.isNotBlank(query.getResponseData()), OperateLog::getResponseData, query.getResponseData())
                .eq(query.getStatus() != null, OperateLog::getStatus, (query.getStatus() == null ? null : query.getStatus().getCode()))
                .like(StringUtils.isNotBlank(query.getErrorMsg()), OperateLog::getErrorMsg, query.getErrorMsg())
                .eq(query.getCostTime() != null, OperateLog::getCostTime, query.getCostTime());
        PageSortUtil.applyTimeSort(wrapper, query, OperateLog::getCreateTime, OperateLog::getUpdateTime);
        Page<OperateLog> resultPage =     operateLogMapper.selectPage(page, wrapper);
        List<OperateLogVO> records = resultPage.getRecords().stream().map(entity -> {
            OperateLogVO vo = new OperateLogVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), records);
    }
}
