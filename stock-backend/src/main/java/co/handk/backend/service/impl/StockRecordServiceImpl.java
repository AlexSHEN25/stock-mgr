package co.handk.backend.service.impl;

import co.handk.backend.util.EnumFieldMapper;

import co.handk.backend.entity.StockRecord;
import co.handk.common.model.dto.create.CreateStockRecordDTO;
import co.handk.common.model.dto.update.UpdateStockRecordDTO;
import co.handk.common.model.vo.StockRecordVO;
import co.handk.backend.mapper.StockRecordMapper;
import co.handk.backend.service.StockRecordService;
import co.handk.common.model.dto.query.StockRecordQueryDTO;
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
public class StockRecordServiceImpl extends ServiceImpl<StockRecordMapper, StockRecord> implements StockRecordService {

    private final StockRecordMapper stockRecordMapper;

    @Override
    public Boolean create(CreateStockRecordDTO dto) {
        StockRecord entity = new StockRecord();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        entity.setId(null);
        return this.save(entity);
    }

    @Override
    public StockRecordVO get(Long id) {
        StockRecord entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("数据不存在");
        }
        StockRecordVO vo = new StockRecordVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public Boolean update(UpdateStockRecordDTO dto) {
        if (this.getById(dto.getId()) == null) {
            throw new RuntimeException("数据不存在");
        }
        StockRecord entity = new StockRecord();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        return this.updateById(entity);
    }

    @Override
    public Boolean delete(Long id) {
        if (this.getById(id) == null) {
            throw new RuntimeException("数据不存在");
        }
        return this.lambdaUpdate().eq(StockRecord::getId, id).set(StockRecord::getDeleted, 1).update();
    }

    @Override
    public PageResult<StockRecordVO> pageQuery(StockRecordQueryDTO query) {
        Page<StockRecord> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<StockRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockRecord::getDeleted, 0).orderByDesc(StockRecord::getUpdateTime);
        Page<StockRecord> resultPage =     stockRecordMapper.selectPage(page, wrapper);
        List<StockRecordVO> records = resultPage.getRecords().stream().map(entity -> {
            StockRecordVO vo = new StockRecordVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), records);
    }
}
