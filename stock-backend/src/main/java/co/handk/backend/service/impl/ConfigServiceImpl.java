package co.handk.backend.service.impl;

import org.apache.commons.lang3.StringUtils;

import co.handk.backend.util.PageSortUtil;

import co.handk.backend.util.EnumFieldMapper;

import co.handk.backend.entity.Config;
import co.handk.common.model.dto.create.CreateConfigDTO;
import co.handk.common.model.dto.update.UpdateConfigDTO;
import co.handk.common.model.vo.ConfigVO;
import co.handk.backend.mapper.ConfigMapper;
import co.handk.backend.service.ConfigService;
import co.handk.common.model.dto.query.ConfigQueryDTO;
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
public class ConfigServiceImpl extends ServiceImpl<ConfigMapper, Config> implements ConfigService {

    private final ConfigMapper configMapper;

    @Override
    public Boolean create(CreateConfigDTO dto) {
        Config entity = new Config();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        entity.setId(null);
        return this.save(entity);
    }

    @Override
    public ConfigVO get(Long id) {
        Config entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("数据不存在");
        }
        ConfigVO vo = new ConfigVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public Boolean update(UpdateConfigDTO dto) {
        if (this.getById(dto.getId()) == null) {
            throw new RuntimeException("数据不存在");
        }
        Config entity = new Config();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        return this.updateById(entity);
    }

    @Override
    public Boolean delete(Long id) {
        if (this.getById(id) == null) {
            throw new RuntimeException("数据不存在");
        }
        return this.lambdaUpdate().eq(Config::getId, id).set(Config::getDeleted, co.handk.common.enums.DeleteEnum.DELETED.getCode()).update();
    }

    @Override
    public PageResult<ConfigVO> pageQuery(ConfigQueryDTO query) {
        Page<Config> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Config> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Config::getDeleted, co.handk.common.enums.DeleteEnum.UNDELETED.getCode())
                .like(StringUtils.isNotBlank(query.getName()), Config::getName, query.getName())
                .like(StringUtils.isNotBlank(query.getGroup()), Config::getGroup, query.getGroup())
                .like(StringUtils.isNotBlank(query.getTitle()), Config::getTitle, query.getTitle())
                .like(StringUtils.isNotBlank(query.getTip()), Config::getTip, query.getTip())
                .like(StringUtils.isNotBlank(query.getType()), Config::getType, query.getType())
                .like(StringUtils.isNotBlank(query.getValue()), Config::getValue, query.getValue())
                .like(StringUtils.isNotBlank(query.getContent()), Config::getContent, query.getContent());
        PageSortUtil.applyTimeSort(wrapper, query, Config::getCreateTime, Config::getUpdateTime);
        Page<Config> resultPage =     configMapper.selectPage(page, wrapper);
        List<ConfigVO> records = resultPage.getRecords().stream().map(entity -> {
            ConfigVO vo = new ConfigVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), records);
    }
}
