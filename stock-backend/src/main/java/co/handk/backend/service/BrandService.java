package co.handk.backend.service;

import co.handk.backend.entity.Brand;
import co.handk.common.model.dto.BrandTreeSaveDTO;
import co.handk.common.model.vo.BrandTreeNodeVO;
import co.handk.common.model.vo.BrandVO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Validated
public interface BrandService extends BaseService<Brand, BrandVO> {

    String uploadImage(MultipartFile file);

    String replaceImage(Long id, MultipartFile file);

    List<BrandTreeNodeVO> listTree();

    BrandTreeNodeVO getTreeDetail(Long id);

    Long saveTree(BrandTreeSaveDTO dto);

}
