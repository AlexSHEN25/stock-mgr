package co.handk.backend.service;

import co.handk.backend.entity.Customer;
import co.handk.common.model.dto.query.CustomerQueryDTO;
import co.handk.common.model.vo.CustomerImportResultVO;
import co.handk.common.model.vo.CustomerVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

@Service
@Validated
public interface CustomerService extends BaseService<Customer, CustomerVO> {
    void exportCustomers(CustomerQueryDTO query, HttpServletResponse response);

    void downloadImportTemplate(HttpServletResponse response);

    CustomerImportResultVO importCustomers(MultipartFile file);
}
