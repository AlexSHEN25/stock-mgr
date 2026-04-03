package co.handk.backend.service.impl;

import co.handk.backend.util.EnumFieldMapper;
import co.handk.backend.util.PageSortUtil;

import co.handk.backend.entity.Stock;
import co.handk.backend.entity.StockRecord;
import co.handk.backend.mapper.StockMapper;
import co.handk.backend.mapper.StockRecordMapper;
import co.handk.backend.service.StockService;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateStockDTO;
import co.handk.common.model.dto.query.StockQueryDTO;
import co.handk.common.model.dto.update.UpdateStockDTO;
import co.handk.common.model.vo.StockPageVO;
import co.handk.common.model.vo.StockVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 库存 Service 实现
 */
@Service
@RequiredArgsConstructor
public class StockServiceImpl extends ServiceImpl<StockMapper, Stock> implements StockService {

    private final StockMapper stockMapper;
    private final StockRecordMapper stockRecordMapper;

    @Override
    public Boolean create(CreateStockDTO dto) {
        Stock entity = new Stock();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        entity.setId(null);
        return this.save(entity);
    }

    @Override
    public StockVO get(Long id) {
        Stock entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("库存不存在");
        }
        StockVO vo = new StockVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public Boolean update(UpdateStockDTO dto) {
        if (this.getById(dto.getId()) == null) {
            throw new RuntimeException("库存不存在");
        }
        Stock entity = new Stock();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        return this.updateById(entity);
    }

    @Override
    public Boolean delete(Long id) {
        if (this.getById(id) == null) {
            throw new RuntimeException("库存不存在");
        }
        return this.lambdaUpdate().eq(Stock::getId, id).set(Stock::getDeleted, co.handk.common.enums.DeleteEnum.DELETED.getCode()).update();
    }

    @Override
    public PageResult<StockPageVO> pageQuery(StockQueryDTO dto) {

        // 1. 构建分页对象
        Page<Stock> page = new Page<>(dto.getPageNum(), dto.getPageSize());

        // 2. 构建查询条件
        LambdaQueryWrapper<Stock> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(dto.getWarehouseId() != null, Stock::getWarehouseId, dto.getWarehouseId())
                .eq(dto.getStatus() != null, Stock::getStatus, (dto.getStatus() == null ? null : dto.getStatus().getCode()))
                .like(StringUtils.isNotBlank(dto.getGoodsName()), Stock::getGoodsName, dto.getGoodsName())
                .like(StringUtils.isNotBlank(dto.getSku()), Stock::getSku, dto.getSku())
                .eq(Stock::getDeleted, co.handk.common.enums.DeleteEnum.UNDELETED.getCode());
        PageSortUtil.applyTimeSort(wrapper, dto, Stock::getCreateTime, Stock::getUpdateTime);

        // 3. 执行分页查询
        Page<Stock> resultPage = stockMapper.selectPage(page, wrapper);

        // 4. 转换为 VO
        List<StockPageVO> records = resultPage.getRecords().stream().map(stock -> {
            StockPageVO vo = new StockPageVO();
            BeanUtils.copyProperties(stock, vo);

            int currentQty = stock.getCurrentQty() == null ? 0 : stock.getCurrentQty();
            int lockQty = stock.getLockQty() == null ? 0 : stock.getLockQty();
            vo.setAvailableQty(currentQty - lockQty);

            return vo;
        }).collect(Collectors.toList());

        // 5. 返回统一分页结果
        return PageResult.build(resultPage.getTotal(), dto.getPageNum(), dto.getPageSize(), records);

    }

    @Override
    public Boolean undo(Long stockRecordId) {
        return applyStockRecordQty(stockRecordId, true);
    }

    @Override
    public Boolean redo(Long stockRecordId) {
        return applyStockRecordQty(stockRecordId, false);
    }

    private Boolean applyStockRecordQty(Long stockRecordId, boolean undo) {
        StockRecord record = stockRecordMapper.selectById(stockRecordId);
        if (record == null) {
            throw new RuntimeException("库存流水不存在");
        }
        Stock stock = this.getById(record.getStockId());
        if (stock == null) {
            throw new RuntimeException("库存不存在");
        }
        stock.setCurrentQty(undo ? record.getBeforeQty() : record.getAfterQty());
        return this.updateById(stock);
    }
}
