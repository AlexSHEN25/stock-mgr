package co.handk.backend.service.impl;

import co.handk.backend.entity.Warehouse;
import co.handk.common.model.dto.WarehouseDTO;
import co.handk.common.model.vo.WarehouseVO;
import co.handk.backend.mapper.WarehouseMapper;
import co.handk.backend.service.WarehouseService;
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
public class WarehouseServiceImpl extends ServiceImpl<WarehouseMapper, Warehouse> implements WarehouseService {

    private final WarehouseMapper warehouseMapper;

    @Override
    public Boolean create(WarehouseDTO dto) {
        Warehouse entity = new Warehouse();
        BeanUtils.copyProperties(dto, entity);
        entity.setId(null);
        return this.save(entity);
    }

    @Override
    public WarehouseVO get(Long id) {
        Warehouse entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("数据不存在");
        }
        WarehouseVO vo = new WarehouseVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public Boolean update(WarehouseDTO dto) {
        if (this.getById(dto.getId()) == null) {
            throw new RuntimeException("数据不存在");
        }
        Warehouse entity = new Warehouse();
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
    public List<WarehouseVO> listAll() {
        LambdaQueryWrapper<Warehouse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Warehouse::getDeleted, 0).orderByDesc(Warehouse::getUpdateTime);
        return     warehouseMapper.selectList(wrapper).stream().map(entity -> {
            WarehouseVO vo = new WarehouseVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public PageResult<WarehouseVO> pageQuery(PageQuery query) {
        Page<Warehouse> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Warehouse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Warehouse::getDeleted, 0).orderByDesc(Warehouse::getUpdateTime);
        Page<Warehouse> resultPage =     warehouseMapper.selectPage(page, wrapper);
        List<WarehouseVO> records = resultPage.getRecords().stream().map(entity -> {
            WarehouseVO vo = new WarehouseVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), records);
    }
}
