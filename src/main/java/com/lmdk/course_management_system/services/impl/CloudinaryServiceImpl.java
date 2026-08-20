package com.lmdk.course_management_system.services.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.lmdk.course_management_system.dto.cloudinary.CloudinaryUploadResult;
import com.lmdk.course_management_system.services.CloudinaryService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Override
    public CloudinaryUploadResult uploadPdf(MultipartFile file) {

        validatePdf(file);

        try {

            Map<?, ?> result =
                    cloudinary.uploader().upload(
                            file.getBytes(),
                            ObjectUtils.asMap(
                                    "resource_type", "image",
                                    "folder", "course-management/lessons",
                                    "use_filename", true,
                                    "unique_filename", true
                            )
                    );

            return new CloudinaryUploadResult(
                    result.get("public_id").toString(),
                    result.get("secure_url").toString(),
                    file.getOriginalFilename()
            );

        } catch (Exception ex) {

            throw new IllegalArgumentException(
                    "Không thể tải file PDF lên Cloudinary!"
            );
        }
    }

    @Override
    public void deletePdf(String publicId) {

        if(publicId == null || publicId.isBlank())
            return;

        try {

            cloudinary.uploader().destroy(
                    publicId,
                    ObjectUtils.asMap(
                            "resource_type", "image",
                            "invalidate", true
                    )
            );

        } catch (Exception ex) {

            throw new IllegalArgumentException(
                    "Không thể xóa file PDF trên Cloudinary!"
            );
        }
    }

    private void validatePdf(MultipartFile file) {

        if(file == null || file.isEmpty())
            throw new IllegalArgumentException(
                    "Vui lòng chọn file PDF!"
            );

        String fileName = file.getOriginalFilename();

        if(fileName == null
                || !fileName.toLowerCase().endsWith(".pdf"))
            throw new IllegalArgumentException(
                    "Chỉ được phép tải file PDF!"
            );

        String contentType = file.getContentType();

        if(contentType != null
                && !contentType.equalsIgnoreCase("application/pdf"))
            throw new IllegalArgumentException(
                    "File tải lên không phải PDF hợp lệ!"
            );
    }
}