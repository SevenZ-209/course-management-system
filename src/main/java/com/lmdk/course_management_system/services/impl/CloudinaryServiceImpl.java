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

    @Override
    public CloudinaryUploadResult uploadImage(MultipartFile file) {

        validateImage(file);

        try {

            Map<?, ?> result =
                    cloudinary.uploader().upload(
                            file.getBytes(),
                            ObjectUtils.asMap(
                                    "resource_type", "image",
                                    "folder", "course-management/avatars",
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
                    "Không thể tải ảnh đại diện lên Cloudinary!"
            );
        }
    }

    @Override
    public void deleteImage(String publicId) {

        if (publicId == null || publicId.isBlank())
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
                    "Không thể xóa ảnh trên Cloudinary!"
            );
        }
    }

    private void validateImage(MultipartFile file) {

        if (file == null || file.isEmpty())
            throw new IllegalArgumentException(
                    "Vui lòng chọn ảnh đại diện!"
            );

        if (file.getSize() > 2 * 1024 * 1024)
            throw new IllegalArgumentException(
                    "Dung lượng ảnh đại diện không được vượt quá 2MB!"
            );

        String fileName = file.getOriginalFilename();

        if (fileName == null || fileName.isBlank())
            throw new IllegalArgumentException(
                    "Ảnh đại diện không hợp lệ!"
            );

        String lowerName = fileName.toLowerCase();

        if (!lowerName.endsWith(".jpg")
                && !lowerName.endsWith(".jpeg")
                && !lowerName.endsWith(".png")
                && !lowerName.endsWith(".gif"))
            throw new IllegalArgumentException(
                    "Chỉ chấp nhận ảnh JPG, JPEG, PNG hoặc GIF!"
            );

        String contentType = file.getContentType();

        if (contentType != null
                && !contentType.startsWith("image/"))
            throw new IllegalArgumentException(
                    "File tải lên không phải hình ảnh hợp lệ!"
            );
    }

    @Override
    public CloudinaryUploadResult uploadCourseImage(MultipartFile file) {
        validateImage(file);

        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "image",
                            "folder", "course-management/courses",
                            "use_filename", true,
                            "unique_filename", true
                    )
            );

            return new CloudinaryUploadResult(
                    result.get("public_id").toString(),
                    result.get("secure_url").toString(),
                    file.getOriginalFilename()
            );
        } catch(Exception ex) {
            throw new IllegalArgumentException(
                    "Không thể tải ảnh khóa học lên Cloudinary!"
            );
        }
    }

    @Override
    public void deleteCourseImage(String publicId) {
        if(publicId == null || publicId.isBlank()) return;

        try {
            cloudinary.uploader().destroy(
                    publicId,
                    ObjectUtils.asMap(
                            "resource_type", "image",
                            "invalidate", true
                    )
            );
        } catch(Exception ex) {
            throw new IllegalArgumentException(
                    "Không thể xóa ảnh khóa học trên Cloudinary!"
            );
        }
    }
}