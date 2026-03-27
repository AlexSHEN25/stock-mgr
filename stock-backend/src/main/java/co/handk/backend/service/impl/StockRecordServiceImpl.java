package co.handk.backend.service.impl;

import co.handk.backend.entity.StockRecord;
import co.handk.common.model.dto.StockRecordDTO;
import co.handk.backend.mapper.StockRecordMapper;
import co.handk.backend.service.StockRecordService;
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
public class StockRecordServiceImpl extends ServiceImpl<StockRecordMapper, StockRecord> implements StockRecordService {

    private final StockRecordMapper stockRecordMapper;

    @Override
    public Boolean create(StockRecordDTO dto) {
        StockRecord entity = new StockRecord();
        BeanUtils.copyProperties(dto, entity);
        entity.setId(null);
        return this.save(entity);
    }

    @Override
    public StockRecord get(Long id) {
        StockRecord entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("数据不存在");
        }
        return entity;
    }

    @Override
    public Boolean update(StockRecordDTO dto) {
        if (this.getById(dto.getId()) == null) {
            throw new RuntimeException("数据不存在");
        }
        StockRecord entity = new StockRecord();
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
    public List<StockRecord> listAll() {
        LambdaQueryWrapper<StockRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockRecord::getDeleted, 0).orderByDesc(StockRecord::getUpdateTime);
        return stockRecordMapper.selectList(wrapper);
    }

    @Override
    public PageResult<StockRecord> pageQuery(PageQuery query) {
        Page<StockRecord> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<StockRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockRecord::getDeleted, 0).orderByDesc(StockRecord::getUpdateTime);
        Page<StockRecord> resultPage = stockRecordMapper.selectPage(page, wrapper);
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), resultPage.getRecords());
    }
}
