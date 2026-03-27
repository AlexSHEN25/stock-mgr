package co.handk.backend.service.impl;

import co.handk.backend.entity.Dept;
import co.handk.common.model.dto.DeptDTO;
import co.handk.backend.mapper.DeptMapper;
import co.handk.backend.service.DeptService;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.BeanUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeptServiceImpl extends ServiceImpl<DeptMapper, Dept> implements DeptService {

    private final DeptMapper deptMapper;

    @Override
    public Boolean create(DeptDTO dto) {
        Dept entity = new Dept();
        BeanUtils.copyProperties(dto, entity);
        entity.setId(null);
        return this.save(entity);
    }

    @Override
    public Dept get(Long id) {
        Dept entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("数据不存在");
        }
        return entity;
    }

    @Override
    public Boolean update(DeptDTO dto) {
        if (this.getById(dto.getId()) == null) {
            throw new RuntimeException("数据不存在");
        }
        Dept entity = new Dept();
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
    public List<Dept> listAll() {
        LambdaQueryWrapper<Dept> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dept::getDeleted, 0).orderByDesc(Dept::getUpdateTime);
        return deptMapper.selectList(wrapper);
    }

    @Override
    public PageResult<Dept> pageQuery(PageQuery query) {
        Page<Dept> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Dept> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dept::getDeleted, 0).orderByDesc(Dept::getUpdateTime);
        Page<Dept> resultPage = deptMapper.selectPage(page, wrapper);
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), resultPage.getRecords());
    }
}
