package co.handk.backend.service.impl;

import co.handk.backend.entity.StockOrderItem;
import co.handk.backend.mapper.StockOrderItemMapper;
import co.handk.backend.service.StockOrderItemService;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class StockOrderItemServiceImpl extends ServiceImpl<StockOrderItemMapper, StockOrderItem> implements StockOrderItemService {

    private final StockOrderItemMapper stockOrderItemMapper;

    @Override
    public Boolean create(StockOrderItem entity) {
        if (entity == null) {
            throw new RuntimeException("请求参数不能为空");
        }
        entity.setId(null);
        return this.save(entity);
    }

    @Override
    public StockOrderItem get(Long id) {
        StockOrderItem entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("数据不存在");
        }
        return entity;
    }

    @Override
    public Boolean update(StockOrderItem entity) {
        if (entity == null || Objects.isNull(entity.getId())) {
            throw new RuntimeException("ID不能为空");
        }
        if (this.getById(entity.getId()) == null) {
            throw new RuntimeException("数据不存在");
        }
        return this.updateById(entity);
    }

    @Override
    public Boolean delete(Long id) {
        if (Objects.isNull(id)) {
            throw new RuntimeException("ID不能为空");
        }
        if (this.getById(id) == null) {
            throw new RuntimeException("数据不存在");
        }
        return this.removeById(id);
    }

    @Override
    public List<StockOrderItem> listAll() {
        LambdaQueryWrapper<StockOrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockOrderItem::getDeleted, 0).orderByDesc(StockOrderItem::getUpdateTime);
        return stockOrderItemMapper.selectList(wrapper);
    }

    @Override
    public PageResult<StockOrderItem> pageQuery(PageQuery query) {
        Page<StockOrderItem> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<StockOrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockOrderItem::getDeleted, 0).orderByDesc(StockOrderItem::getUpdateTime);
        Page<StockOrderItem> resultPage = stockOrderItemMapper.selectPage(page, wrapper);
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), resultPage.getRecords());
    }
}
