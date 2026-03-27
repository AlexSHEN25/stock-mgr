package co.handk.backend.service.impl;

import co.handk.backend.entity.Config;
import co.handk.backend.mapper.ConfigMapper;
import co.handk.backend.service.ConfigService;
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
public class ConfigServiceImpl extends ServiceImpl<ConfigMapper, Config> implements ConfigService {

    private final ConfigMapper configMapper;

    @Override
    public Boolean create(Config entity) {
        if (entity == null) {
            throw new RuntimeException("请求参数不能为空");
        }
        entity.setId(null);
        return this.save(entity);
    }

    @Override
    public Config get(Long id) {
        Config entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("数据不存在");
        }
        return entity;
    }

    @Override
    public Boolean update(Config entity) {
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
    public List<Config> listAll() {
        LambdaQueryWrapper<Config> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Config::getDeleted, 0).orderByDesc(Config::getUpdateTime);
        return configMapper.selectList(wrapper);
    }

    @Override
    public PageResult<Config> pageQuery(PageQuery query) {
        Page<Config> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Config> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Config::getDeleted, 0).orderByDesc(Config::getUpdateTime);
        Page<Config> resultPage = configMapper.selectPage(page, wrapper);
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), resultPage.getRecords());
    }
}
