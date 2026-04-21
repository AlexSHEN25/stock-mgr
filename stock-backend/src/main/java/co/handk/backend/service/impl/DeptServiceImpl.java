package co.handk.backend.service.impl;

import co.handk.backend.entity.Dept;
import co.handk.backend.mapper.DeptMapper;
import co.handk.backend.service.DeptService;
import co.handk.backend.util.EnumFieldMapper;
import co.handk.backend.util.PageSortUtil;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateDeptDTO;
import co.handk.common.model.dto.query.DeptQueryDTO;
import co.handk.common.model.dto.update.UpdateDeptDTO;
import co.handk.common.model.vo.DeptVO;
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
public class DeptServiceImpl extends ServiceImpl<DeptMapper, Dept> implements DeptService {

    private final DeptMapper deptMapper;

    @Override
    public Boolean create(CreateDeptDTO dto) {
        Dept entity = new Dept();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        entity.setId(null);
        return this.save(entity);
    }

    @Override
    public DeptVO get(Long id) {
        Dept entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("数据不存在");
        }
        DeptVO vo = new DeptVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public Boolean update(UpdateDeptDTO dto) {
        if (this.getById(dto.getId()) == null) {
            throw new RuntimeException("数据不存在");
        }
        Dept entity = new Dept();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        return this.updateById(entity);
    }

    @Override
    public Boolean delete(Long id) {
        if (this.getById(id) == null) {
            throw new RuntimeException("数据不存在");
        }
        return this.lambdaUpdate().eq(Dept::getId, id).set(Dept::getDeleted, co.handk.common.enums.DeleteEnum.DELETED.getCode()).update();
    }

    @Override
    public PageResult<DeptVO> pageQuery(DeptQueryDTO query) {
        Page<Dept> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Dept> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dept::getDeleted, co.handk.common.enums.DeleteEnum.UNDELETED.getCode())
                .eq(query.getParentId() != null, Dept::getParentId, query.getParentId())
                .like(StringUtils.isNotBlank(query.getName()), Dept::getName, query.getName())
                .like(StringUtils.isNotBlank(query.getCode()), Dept::getCode, query.getCode())
                .eq(query.getLeaderId() != null, Dept::getLeaderId, query.getLeaderId())
                .eq(query.getSort() != null, Dept::getSort, query.getSort())
                .eq(query.getStatus() != null, Dept::getStatus, (query.getStatus() == null ? null : query.getStatus().getCode()));
        PageSortUtil.applyTimeSort(wrapper, query, Dept::getCreateTime, Dept::getUpdateTime);
        Page<Dept> resultPage =     deptMapper.selectPage(page, wrapper);
        List<DeptVO> records = resultPage.getRecords().stream().map(entity -> {
            DeptVO vo = new DeptVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), records);
    }
}
