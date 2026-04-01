package co.handk.backend.service.impl;

import co.handk.backend.entity.RequestItem;
import co.handk.common.model.dto.RequestItemDTO;
import co.handk.common.model.vo.RequestItemVO;
import co.handk.backend.mapper.RequestItemMapper;
import co.handk.backend.service.RequestItemService;
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
public class RequestItemServiceImpl extends ServiceImpl<RequestItemMapper, RequestItem> implements RequestItemService {

    private final RequestItemMapper requestItemMapper;

    @Override
    public Boolean create(RequestItemDTO dto) {
        RequestItem entity = new RequestItem();
        BeanUtils.copyProperties(dto, entity);
        entity.setId(null);
        return this.save(entity);
    }

    @Override
    public RequestItem get(Long id) {
        RequestItem entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("数据不存在");
        }
        return entity;
    }

    @Override
    public Boolean update(RequestItemDTO dto) {
        if (this.getById(dto.getId()) == null) {
            throw new RuntimeException("数据不存在");
        }
        RequestItem entity = new RequestItem();
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
    public List<RequestItemVO> listAll() {
        LambdaQueryWrapper<RequestItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RequestItem::getDeleted, 0).orderByDesc(RequestItem::getUpdateTime);
        return     requestItemMapper.selectList(wrapper).stream().map(entity -> {
            RequestItemVO vo = new RequestItemVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public PageResult<RequestItemVO> pageQuery(PageQuery query) {
        Page<RequestItem> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<RequestItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RequestItem::getDeleted, 0).orderByDesc(RequestItem::getUpdateTime);
        Page<RequestItem> resultPage =     requestItemMapper.selectPage(page, wrapper);
        List<RequestItemVO> records = resultPage.getRecords().stream().map(entity -> {
            RequestItemVO vo = new RequestItemVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), records);
    }
}
