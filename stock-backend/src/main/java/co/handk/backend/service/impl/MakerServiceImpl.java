package co.handk.backend.service.impl;

import co.handk.backend.entity.Maker;
import co.handk.backend.mapper.MakerMapper;
import co.handk.backend.service.MakerService;
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
public class MakerServiceImpl extends ServiceImpl<MakerMapper, Maker> implements MakerService {

    private final MakerMapper makerMapper;

    @Override
    public Boolean create(Maker entity) {
        if (entity == null) {
            throw new RuntimeException("请求参数不能为空");
        }
        entity.setId(null);
        return this.save(entity);
    }

    @Override
    public Maker get(Long id) {
        Maker entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("数据不存在");
        }
        return entity;
    }

    @Override
    public Boolean update(Maker entity) {
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
    public List<Maker> listAll() {
        LambdaQueryWrapper<Maker> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Maker::getDeleted, 0).orderByDesc(Maker::getUpdateTime);
        return makerMapper.selectList(wrapper);
    }

    @Override
    public PageResult<Maker> pageQuery(PageQuery query) {
        Page<Maker> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Maker> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Maker::getDeleted, 0).orderByDesc(Maker::getUpdateTime);
        Page<Maker> resultPage = makerMapper.selectPage(page, wrapper);
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), resultPage.getRecords());
    }
}
