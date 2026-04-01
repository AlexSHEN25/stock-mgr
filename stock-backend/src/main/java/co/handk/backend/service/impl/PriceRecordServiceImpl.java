package co.handk.backend.service.impl;

import co.handk.backend.entity.PriceRecord;
import co.handk.common.model.dto.PriceRecordDTO;
import co.handk.common.model.vo.PriceRecordVO;
import co.handk.backend.mapper.PriceRecordMapper;
import co.handk.backend.service.PriceRecordService;
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
public class PriceRecordServiceImpl extends ServiceImpl<PriceRecordMapper, PriceRecord> implements PriceRecordService {

    private final PriceRecordMapper priceRecordMapper;

    @Override
    public Boolean create(PriceRecordDTO dto) {
        PriceRecord entity = new PriceRecord();
        BeanUtils.copyProperties(dto, entity);
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
    public Boolean update(PriceRecordDTO dto) {
        if (this.getById(dto.getId()) == null) {
            throw new RuntimeException("数据不存在");
        }
        PriceRecord entity = new PriceRecord();
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
    public List<PriceRecordVO> listAll() {
        LambdaQueryWrapper<PriceRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PriceRecord::getDeleted, 0).orderByDesc(PriceRecord::getUpdateTime);
        return     priceRecordMapper.selectList(wrapper).stream().map(entity -> {
            PriceRecordVO vo = new PriceRecordVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public PageResult<PriceRecordVO> pageQuery(PageQuery query) {
        Page<PriceRecord> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<PriceRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PriceRecord::getDeleted, 0).orderByDesc(PriceRecord::getUpdateTime);
        Page<PriceRecord> resultPage =     priceRecordMapper.selectPage(page, wrapper);
        List<PriceRecordVO> records = resultPage.getRecords().stream().map(entity -> {
            PriceRecordVO vo = new PriceRecordVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), records);
    }
}
