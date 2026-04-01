package co.handk.backend.service.impl;

import co.handk.backend.entity.OperateLog;
import co.handk.common.model.dto.OperateLogDTO;
import co.handk.common.model.vo.OperateLogVO;
import co.handk.backend.mapper.OperateLogMapper;
import co.handk.backend.service.OperateLogService;
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
public class OperateLogServiceImpl extends ServiceImpl<OperateLogMapper, OperateLog> implements OperateLogService {

    private final OperateLogMapper operateLogMapper;

    @Override
    public Boolean create(OperateLogDTO dto) {
        OperateLog entity = new OperateLog();
        BeanUtils.copyProperties(dto, entity);
        entity.setId(null);
        return this.save(entity);
    }

    @Override
    public OperateLog get(Long id) {
        OperateLog entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("数据不存在");
        }
        return entity;
    }

    @Override
    public Boolean update(OperateLogDTO dto) {
        if (this.getById(dto.getId()) == null) {
            throw new RuntimeException("数据不存在");
        }
        OperateLog entity = new OperateLog();
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
    public List<OperateLogVO> listAll() {
        LambdaQueryWrapper<OperateLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OperateLog::getDeleted, 0).orderByDesc(OperateLog::getUpdateTime);
        return     operateLogMapper.selectList(wrapper).stream().map(entity -> {
            OperateLogVO vo = new OperateLogVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public PageResult<OperateLogVO> pageQuery(PageQuery query) {
        Page<OperateLog> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<OperateLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OperateLog::getDeleted, 0).orderByDesc(OperateLog::getUpdateTime);
        Page<OperateLog> resultPage =     operateLogMapper.selectPage(page, wrapper);
        List<OperateLogVO> records = resultPage.getRecords().stream().map(entity -> {
            OperateLogVO vo = new OperateLogVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), records);
    }
}
