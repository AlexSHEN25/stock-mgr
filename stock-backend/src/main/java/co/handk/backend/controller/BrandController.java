package co.handk.backend.controller;

import co.handk.backend.service.BrandService;
import co.handk.common.constant.NumberConstant;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.BrandHierarchySaveDTO;
import co.handk.common.model.dto.BrandTreeSaveDTO;
import co.handk.common.model.dto.create.CreateBrandDTO;
import co.handk.common.model.dto.query.BrandHierarchyQueryDTO;
import co.handk.common.model.dto.query.BrandQueryDTO;
import co.handk.common.model.dto.update.UpdateBrandDTO;
import co.handk.common.model.vo.BrandHierarchyVO;
import co.handk.common.model.vo.BrandTreeNodeVO;
import co.handk.common.model.vo.BrandVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Validated
@RequestMapping("/brand")
@RequiredArgsConstructor
public class BrandController {
    private final BrandService brandService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateBrandDTO dto) {
        return brandService.saveByDto(dto);
    }

    @PostMapping("/upload")
    public String upload(@RequestPart("file") MultipartFile file) {
        return brandService.uploadImage(file);
    }

    @PutMapping("/{id:\\d+}/upload")
    public String replaceUpload(@PathVariable("id") Long id, @RequestPart("file") MultipartFile file) {
        return brandService.replaceImage(id, file);
    }

    @GetMapping("/{id}")
    public BrandVO get(@PathVariable("id") @NotNull Long id) {
        return brandService.getVOById(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateBrandDTO dto) {
        return brandService.updateByDto(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable("id") @NotNull Long id) {
        return brandService.deleteByIdLogic(id) > NumberConstant.ZERO;
    }

    @DeleteMapping("/batch")
    public Boolean deleteBatch(@RequestBody @NotNull List<Long> ids) {
        return brandService.deleteBatchLogic(ids) > NumberConstant.ZERO;
    }

    @GetMapping("/page")
    public PageResult<BrandVO> page(@Valid BrandQueryDTO query) {
        return brandService.page(query);
    }

    @GetMapping("/hierarchy/page")
    public PageResult<BrandHierarchyVO> hierarchyPage(@Valid BrandHierarchyQueryDTO query) {
        return brandService.pageHierarchy(query);
    }

    @GetMapping("/hierarchy/{key}")
    public BrandHierarchyVO hierarchyDetail(@PathVariable("key") @NotNull String key) {
        return brandService.getHierarchy(key);
    }

    @PostMapping("/hierarchy")
    public BrandHierarchyVO createHierarchy(@RequestBody @NotNull @Valid BrandHierarchySaveDTO dto) {
        dto.setId(null);
        return brandService.saveHierarchy(dto);
    }

    @PutMapping("/hierarchy")
    public BrandHierarchyVO updateHierarchy(@RequestBody @NotNull @Valid BrandHierarchySaveDTO dto) {
        return brandService.updateHierarchy(dto);
    }

    @DeleteMapping("/hierarchy/{key}")
    public Boolean deleteHierarchy(@PathVariable("key") @NotNull String key) {
        return brandService.deleteHierarchy(key);
    }

    @GetMapping("/tree")
    public List<BrandTreeNodeVO> tree() {
        return brandService.listTree();
    }

    @GetMapping("/tree/{id}")
    public BrandTreeNodeVO treeDetail(@PathVariable("id") @NotNull Long id) {
        return brandService.getTreeDetail(id);
    }

    @PostMapping("/tree")
    public Long createTree(@RequestBody @NotNull @Valid BrandTreeSaveDTO dto) {
        dto.setId(null);
        return brandService.saveTree(dto);
    }

    @PutMapping("/tree")
    public Long updateTree(@RequestBody @NotNull @Valid BrandTreeSaveDTO dto) {
        return brandService.saveTree(dto);
    }
}
