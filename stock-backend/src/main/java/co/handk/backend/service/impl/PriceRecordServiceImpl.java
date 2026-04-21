package co.handk.backend.service.impl;

import co.handk.backend.entity.PriceRecord;
import co.handk.backend.mapper.PriceRecordMapper;
import co.handk.backend.service.PriceRecordService;
import co.handk.backend.util.EnumFieldMapper;
import co.handk.backend.util.PageSortUtil;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreatePriceRecordDTO;
import co.handk.common.model.dto.query.PriceRecordQueryDTO;
import co.handk.common.model.dto.update.UpdatePriceRecordDTO;
import co.handk.common.model.vo.PriceRecordVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PriceRecordServiceImpl extends ServiceImpl<PriceRecordMapper, PriceRecord> implements PriceRecordService {

    private final PriceRecordMapper priceRecordMapper;

    @Override
    public Boolean create(CreatePriceRecordDTO dto) {
        PriceRecord entity = new PriceRecord();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        entity.setId(null);
        return this.save(entity);
    }

    @Override
    public PriceRecordVO get(Long id) {
        PriceRecord entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("数据不存在");
        }
        PriceRecordVO vo = new PriceRecordVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public Boolean update(UpdatePriceRecordDTO dto) {
        if (this.getById(dto.getId()) == null) {
            throw new RuntimeException("数据不存在");
        }
        PriceRecord entity = new PriceRecord();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        return this.updateById(entity);
    }

    @Override
    public Boolean delete(Long id) {
        if (this.getById(id) == null) {
            throw new RuntimeException("数据不存在");
        }
        return this.lambdaUpdate().eq(PriceRecord::getId, id).set(PriceRecord::getDeleted, co.handk.common.enums.DeleteEnum.DELETED.getCode()).update();
    }

    @Override
    public PageResult<PriceRecordVO> pageQuery(PriceRecordQueryDTO query) {
        Page<PriceRecord> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<PriceRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PriceRecord::getDeleted, co.handk.common.enums.DeleteEnum.UNDELETED.getCode())
                .eq(query.getGoodsId() != null, PriceRecord::getGoodsId, query.getGoodsId())
                .like(StringUtils.isNotBlank(query.getGoodsName()), PriceRecord::getGoodsName, query.getGoodsName())
                .like(StringUtils.isNotBlank(query.getEnglishName()), PriceRecord::getEnglishName, query.getEnglishName())
                .eq(query.getSkuId() != null, PriceRecord::getSkuId, query.getSkuId())
                .like(StringUtils.isNotBlank(query.getSkuCode()), PriceRecord::getSkuCode, query.getSkuCode())
                .eq(query.getOldPrice() != null, PriceRecord::getOldPrice, query.getOldPrice())
                .eq(query.getNewPrice() != null, PriceRecord::getNewPrice, query.getNewPrice())
                .eq(StringUtils.isNotBlank(query.getCurrency()), PriceRecord::getCurrency, query.getCurrency())
                .eq(query.getDiscount() != null, PriceRecord::getDiscount, query.getDiscount())
                .eq(query.getPriceUpdateTime() != null, PriceRecord::getPriceUpdateTime, query.getPriceUpdateTime())
                .eq(query.getOperatorId() != null, PriceRecord::getOperatorId, query.getOperatorId())
                .like(StringUtils.isNotBlank(query.getOperatorName()), PriceRecord::getOperatorName, query.getOperatorName());
        PageSortUtil.applyTimeSort(wrapper, query, PriceRecord::getCreateTime, PriceRecord::getUpdateTime);
        Page<PriceRecord> resultPage =     priceRecordMapper.selectPage(page, wrapper);
        List<PriceRecordVO> records = resultPage.getRecords().stream().map(entity -> {
            PriceRecordVO vo = new PriceRecordVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), records);
    }
}

