package co.handk.backend.service;
import co.handk.backend.entity.RequestItem;
import co.handk.common.model.dto.create.CreateRequestItemDTO;
import co.handk.common.model.dto.update.UpdateRequestItemDTO;
import co.handk.common.model.vo.RequestItemVO;
import co.handk.common.model.dto.query.RequestItemQueryDTO;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotNull;
@Service
@Validated
public interface RequestItemService extends IService<RequestItem> {
    Boolean create(@NotNull CreateRequestItemDTO dto);
    RequestItemVO get(@NotNull Long id);
    Boolean update(@NotNull UpdateRequestItemDTO dto);
    Boolean delete(@NotNull Long id);
    PageResult<RequestItemVO> pageQuery(@NotNull RequestItemQueryDTO query);
}
