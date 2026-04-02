package co.handk.backend.service;
import co.handk.backend.entity.RequestForm;
import co.handk.common.model.dto.create.CreateRequestFormDTO;
import co.handk.common.model.dto.update.UpdateRequestFormDTO;
import co.handk.common.model.vo.RequestFormVO;
import co.handk.common.model.dto.query.RequestFormQueryDTO;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotNull;
@Service
@Validated
public interface RequestFormService extends IService<RequestForm> {
    Boolean create(@NotNull CreateRequestFormDTO dto);
    RequestFormVO get(@NotNull Long id);
    Boolean update(@NotNull UpdateRequestFormDTO dto);
    Boolean delete(@NotNull Long id);
    PageResult<RequestFormVO> pageQuery(@NotNull RequestFormQueryDTO query);
}
