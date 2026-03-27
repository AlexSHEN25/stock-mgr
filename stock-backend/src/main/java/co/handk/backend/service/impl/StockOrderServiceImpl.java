package co.handk.backend.service.impl;

import co.handk.backend.entity.StockOrder;
import co.handk.backend.mapper.StockOrderMapper;
import co.handk.backend.service.StockOrderService;
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
public class StockOrderServiceImpl extends ServiceImpl<StockOrderMapper, StockOrder> implements StockOrderService {

    private final StockOrderMapper stockOrderMapper;

    @Override
    public Boolean create(StockOrder entity) {
        if (entity == null) {
            throw new RuntimeException("请求参数不能为空");
        }
        entity.setId(null);
        return this.save(entity);
    }

    @Override
    public StockOrder get(Long id) {
        StockOrder entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("数据不存在");
        }
        return entity;
    }

    @Override
    public Boolean update(StockOrder entity) {
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
    public List<StockOrder> listAll() {
        LambdaQueryWrapper<StockOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockOrder::getDeleted, 0).orderByDesc(StockOrder::getUpdateTime);
        return stockOrderMapper.selectList(wrapper);
    }

    @Override
    public PageResult<StockOrder> pageQuery(PageQuery query) {
        Page<StockOrder> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<StockOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockOrder::getDeleted, 0).orderByDesc(StockOrder::getUpdateTime);
        Page<StockOrder> resultPage = stockOrderMapper.selectPage(page, wrapper);
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), resultPage.getRecords());
    }
}
