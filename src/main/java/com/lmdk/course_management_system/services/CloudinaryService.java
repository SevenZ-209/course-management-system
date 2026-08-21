package com.lmdk.course_management_system.services;

import com.lmdk.course_management_system.dto.cloudinary.CloudinaryUploadResult;

import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {

    CloudinaryUploadResult uploadPdf(MultipartFile file);

    void deletePdf(String publicId);

    CloudinaryUploadResult uploadImage(MultipartFile file);

    void deleteImage(String publicId);
}