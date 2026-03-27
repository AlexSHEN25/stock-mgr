package co.handk.backend.service.impl;

import co.handk.backend.entity.Config;
import co.handk.common.model.dto.ConfigDTO;
import co.handk.backend.mapper.ConfigMapper;
import co.handk.backend.service.ConfigService;
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
public class ConfigServiceImpl extends ServiceImpl<ConfigMapper, Config> implements ConfigService {

    private final ConfigMapper configMapper;

    @Override
    public Boolean create(ConfigDTO dto) {
        Config entity = new Config();
        BeanUtils.copyProperties(dto, entity);
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
    public Boolean update(ConfigDTO dto) {
        if (this.getById(dto.getId()) == null) {
            throw new RuntimeException("数据不存在");
        }
        Config entity = new Config();
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
