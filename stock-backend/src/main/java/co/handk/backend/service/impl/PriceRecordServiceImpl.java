package co.handk.backend.service.impl;

import co.handk.backend.entity.PriceRecord;
import co.handk.backend.mapper.PriceRecordMapper;
import co.handk.backend.service.PriceRecordService;
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
public class PriceRecordServiceImpl extends ServiceImpl<PriceRecordMapper, PriceRecord> implements PriceRecordService {

    private final PriceRecordMapper priceRecordMapper;

    @Override
    public Boolean create(PriceRecord entity) {
        if (entity == null) {
            throw new RuntimeException("请求参数不能为空");
        }
        entity.setId(null);
        return this.save(entity);
    }

    @Override
    public PriceRecord get(Long id) {
        PriceRecord entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("数据不存在");
        }
        return entity;
    }

    @Override
    public Boolean update(PriceRecord entity) {
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
    public List<PriceRecord> listAll() {
        LambdaQueryWrapper<PriceRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PriceRecord::getDeleted, 0).orderByDesc(PriceRecord::getUpdateTime);
        return priceRecordMapper.selectList(wrapper);
    }

    @Override
    public PageResult<PriceRecord> pageQuery(PageQuery query) {
        Page<PriceRecord> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<PriceRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PriceRecord::getDeleted, 0).orderByDesc(PriceRecord::getUpdateTime);
        Page<PriceRecord> resultPage = priceRecordMapper.selectPage(page, wrapper);
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), resultPage.getRecords());
    }
}
