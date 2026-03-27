package co.handk.backend.service.impl;

import co.handk.backend.entity.Stock;
import co.handk.backend.mapper.StockMapper;
import co.handk.backend.service.StockService;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.StockPageQueryDTO;
import co.handk.common.model.vo.StockPageVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 库存 Service 实现
 */
@Service
@RequiredArgsConstructor
public class StockServiceImpl extends ServiceImpl<StockMapper, Stock> implements StockService {

    private final StockMapper stockMapper;

    @Override
    public Boolean create(Stock stock) {
        if (stock == null) {
            throw new RuntimeException("请求参数不能为空");
        }
        stock.setId(null);
        return this.save(stock);
    }

    @Override
    public Stock get(Long id) {
        Stock stock = this.getById(id);
        if (stock == null) {
            throw new RuntimeException("库存不存在");
        }
        return stock;
    }

    @Override
    public Boolean update(Stock stock) {
        if (stock == null || Objects.isNull(stock.getId())) {
            throw new RuntimeException("库存ID不能为空");
        }
        if (this.getById(stock.getId()) == null) {
            throw new RuntimeException("库存不存在");
        }
        return this.updateById(stock);
    }

    @Override
    public Boolean delete(Long id) {
        if (Objects.isNull(id)) {
            throw new RuntimeException("库存ID不能为空");
        }
        if (this.getById(id) == null) {
            throw new RuntimeException("库存不存在");
        }
        return this.removeById(id);
    }

    @Override
    public List<Stock> listAll() {
        LambdaQueryWrapper<Stock> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Stock::getDeleted, 0).orderByDesc(Stock::getUpdateTime);
        return stockMapper.selectList(wrapper);
    }

    @Override
    public PageResult<StockPageVO> pageQuery(StockPageQueryDTO dto) {

        // 1. 构建分页对象
        Page<Stock> page = new Page<>(dto.getPageNum(), dto.getPageSize());

        // 2. 构建查询条件
        LambdaQueryWrapper<Stock> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(dto.getWarehouseId() != null, Stock::getWarehouseId, dto.getWarehouseId())
                .eq(dto.getStatus() != null, Stock::getStatus, dto.getStatus())
                .like(StringUtils.isNotBlank(dto.getGoodsName()), Stock::getGoodsName, dto.getGoodsName())
                .like(StringUtils.isNotBlank(dto.getSku()), Stock::getSku, dto.getSku())
                .eq(Stock::getDeleted, 0)
                .orderByDesc(Stock::getUpdateTime);

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
}
