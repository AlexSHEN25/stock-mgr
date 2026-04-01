package co.handk.backend.service.impl;

import co.handk.backend.entity.Maker;
import co.handk.common.model.dto.MakerDTO;
import co.handk.common.model.vo.MakerVO;
import co.handk.backend.mapper.MakerMapper;
import co.handk.backend.service.MakerService;
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
public class MakerServiceImpl extends ServiceImpl<MakerMapper, Maker> implements MakerService {

    private final MakerMapper makerMapper;

    @Override
    public Boolean create(MakerDTO dto) {
        Maker entity = new Maker();
        BeanUtils.copyProperties(dto, entity);
        entity.setId(null);
        return this.save(entity);
    }

    @Override
    public MakerVO get(Long id) {
        Maker entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("数据不存在");
        }
        MakerVO vo = new MakerVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public Boolean update(MakerDTO dto) {
        if (this.getById(dto.getId()) == null) {
            throw new RuntimeException("数据不存在");
        }
        Maker entity = new Maker();
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
    public List<MakerVO> listAll() {
        LambdaQueryWrapper<Maker> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Maker::getDeleted, 0).orderByDesc(Maker::getUpdateTime);
        return     makerMapper.selectList(wrapper).stream().map(entity -> {
            MakerVO vo = new MakerVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public PageResult<MakerVO> pageQuery(PageQuery query) {
        Page<Maker> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Maker> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Maker::getDeleted, 0).orderByDesc(Maker::getUpdateTime);
        Page<Maker> resultPage =     makerMapper.selectPage(page, wrapper);
        List<MakerVO> records = resultPage.getRecords().stream().map(entity -> {
            MakerVO vo = new MakerVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), records);
    }
}
