package co.handk.backend.controller;

import co.handk.backend.constant.UploadBizType;
import co.handk.backend.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
public class FileUploadController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public String upload(@RequestParam("bizType") UploadBizType bizType,
                         @RequestPart("file") MultipartFile file) {
        String imagePath = fileStorageService.upload(bizType, file, null);
        return fileStorageService.toApiPath(bizType, imagePath);
    }

    @PutMapping("/upload")
    public String replace(@RequestParam("bizType") UploadBizType bizType,
                          @RequestParam(value = "oldPath", required = false) String oldPath,
                          @RequestPart("file") MultipartFile file) {
        String imagePath = fileStorageService.upload(bizType, file, oldPath);
        return fileStorageService.toApiPath(bizType, imagePath);
    }
}
