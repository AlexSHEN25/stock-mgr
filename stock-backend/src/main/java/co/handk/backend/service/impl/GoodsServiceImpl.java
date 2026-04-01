package co.handk.backend.service.impl;

import co.handk.backend.entity.Goods;
import co.handk.common.model.dto.GoodsDTO;
import co.handk.common.model.vo.GoodsVO;
import co.handk.backend.mapper.GoodsMapper;
import co.handk.backend.service.GoodsService;
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
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {

    private final GoodsMapper goodsMapper;

    @Override
    public Boolean create(GoodsDTO dto) {
        Goods entity = new Goods();
        BeanUtils.copyProperties(dto, entity);
        entity.setId(null);
        return this.save(entity);
    }

    @Override
    public GoodsVO get(Long id) {
        Goods entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("数据不存在");
        }
        GoodsVO vo = new GoodsVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public Boolean update(GoodsDTO dto) {
        if (this.getById(dto.getId()) == null) {
            throw new RuntimeException("数据不存在");
        }
        Goods entity = new Goods();
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
    public List<GoodsVO> listAll() {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Goods::getDeleted, 0).orderByDesc(Goods::getUpdateTime);
        return     goodsMapper.selectList(wrapper).stream().map(entity -> {
            GoodsVO vo = new GoodsVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public PageResult<GoodsVO> pageQuery(PageQuery query) {
        Page<Goods> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Goods::getDeleted, 0).orderByDesc(Goods::getUpdateTime);
        Page<Goods> resultPage =     goodsMapper.selectPage(page, wrapper);
        List<GoodsVO> records = resultPage.getRecords().stream().map(entity -> {
            GoodsVO vo = new GoodsVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), records);
    }
}
