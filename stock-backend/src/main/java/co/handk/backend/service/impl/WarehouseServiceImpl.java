package co.handk.backend.service.impl;

import co.handk.backend.entity.Warehouse;
import co.handk.backend.mapper.WarehouseMapper;
import co.handk.backend.service.WarehouseService;
import co.handk.backend.util.EnumFieldMapper;
import co.handk.backend.util.PageSortUtil;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateWarehouseDTO;
import co.handk.common.model.dto.query.WarehouseQueryDTO;
import co.handk.common.model.dto.update.UpdateWarehouseDTO;
import co.handk.common.model.vo.WarehouseVO;
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
public class WarehouseServiceImpl extends ServiceImpl<WarehouseMapper, Warehouse> implements WarehouseService {

    private final WarehouseMapper warehouseMapper;

    @Override
    public Boolean create(CreateWarehouseDTO dto) {
        Warehouse entity = new Warehouse();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
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
    public Boolean update(UpdateWarehouseDTO dto) {
        if (this.getById(dto.getId()) == null) {
            throw new RuntimeException("数据不存在");
        }
        Warehouse entity = new Warehouse();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        return this.updateById(entity);
    }

    @Override
    public Boolean delete(Long id) {
        if (this.getById(id) == null) {
            throw new RuntimeException("数据不存在");
        }
        return this.lambdaUpdate().eq(Warehouse::getId, id).set(Warehouse::getDeleted, co.handk.common.enums.DeleteEnum.DELETED.getCode()).update();
    }

    @Override
    public PageResult<WarehouseVO> pageQuery(WarehouseQueryDTO query) {
        Page<Warehouse> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Warehouse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Warehouse::getDeleted, co.handk.common.enums.DeleteEnum.UNDELETED.getCode())
                .like(StringUtils.isNotBlank(query.getName()), Warehouse::getName, query.getName())
                .like(StringUtils.isNotBlank(query.getCode()), Warehouse::getCode, query.getCode())
                .like(StringUtils.isNotBlank(query.getAddress()), Warehouse::getAddress, query.getAddress())
                .eq(query.getManagerId() != null, Warehouse::getManagerId, query.getManagerId())
                .eq(query.getStatus() != null, Warehouse::getStatus, (query.getStatus() == null ? null : query.getStatus().getCode()));
        PageSortUtil.applyTimeSort(wrapper, query, Warehouse::getCreateTime, Warehouse::getUpdateTime);
        Page<Warehouse> resultPage =     warehouseMapper.selectPage(page, wrapper);
        List<WarehouseVO> records = resultPage.getRecords().stream().map(entity -> {
            WarehouseVO vo = new WarehouseVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), records);
    }
}
