package co.handk.backend.service.impl;

import co.handk.backend.entity.Goods;
import co.handk.backend.mapper.GoodsMapper;
import co.handk.backend.service.GoodsService;
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
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {

    private final GoodsMapper goodsMapper;

    @Override
    public Boolean create(Goods entity) {
        if (entity == null) {
            throw new RuntimeException("请求参数不能为空");
        }
        entity.setId(null);
        return this.save(entity);
    }

    @Override
    public Goods get(Long id) {
        Goods entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("数据不存在");
        }
        return entity;
    }

    @Override
    public Boolean update(Goods entity) {
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
    public List<Goods> listAll() {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Goods::getDeleted, 0).orderByDesc(Goods::getUpdateTime);
        return goodsMapper.selectList(wrapper);
    }

    @Override
    public PageResult<Goods> pageQuery(PageQuery query) {
        Page<Goods> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Goods::getDeleted, 0).orderByDesc(Goods::getUpdateTime);
        Page<Goods> resultPage = goodsMapper.selectPage(page, wrapper);
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), resultPage.getRecords());
    }
}
