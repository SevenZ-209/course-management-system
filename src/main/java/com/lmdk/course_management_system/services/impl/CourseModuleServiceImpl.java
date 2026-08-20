package com.lmdk.course_management_system.services.impl;

import com.lmdk.course_management_system.pojo.CourseModule;
import com.lmdk.course_management_system.repository.CourseModuleRepository;
import com.lmdk.course_management_system.services.CourseModuleService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CourseModuleServiceImpl implements CourseModuleService {

    private final CourseModuleRepository moduleRepository;

    @Override
    public CourseModule getModuleById(Integer id) {
        return moduleRepository.getModuleById(id);
    }

    @Override
    public CourseModule addModule(CourseModule module) {
        validateModule(module);

        if (moduleRepository.existsOrderNumber(
                module.getCourse().getId(),
                module.getOrderNumber()
        ))
            throw new IllegalArgumentException("Thứ tự module đã tồn tại trong khóa học!");

        if (module.getStatus() == null)
            module.setStatus(CourseModule.ModuleStatus.ACTIVE);

        return moduleRepository.addModule(module);
    }

    @Override
    public void updateModule(CourseModule module) {
        validateModule(module);

        if (moduleRepository.existsOrderNumberExceptId(
                module.getCourse().getId(),
                module.getOrderNumber(),
                module.getId()
        ))
            throw new IllegalArgumentException("Thứ tự module đã tồn tại trong khóa học!");

        moduleRepository.updateModule(module);
    }

    @Override
    public List<CourseModule> getModules(Map<String, String> params) {
        return moduleRepository.getModules(params);
    }

    @Override
    public List<CourseModule> getModulesByCourse(Integer courseId) {
        return moduleRepository.getModulesByCourse(courseId);
    }

    @Override
    public long countModules(Map<String, String> params) {
        return moduleRepository.countModules(params);
    }

    private void validateModule(CourseModule module) {
        if (module.getName() == null || module.getName().trim().isBlank())
            throw new IllegalArgumentException("Tên module không được để trống!");

        if (module.getCourse() == null)
            throw new IllegalArgumentException("Vui lòng chọn khóa học!");

        if (module.getOrderNumber() == null || module.getOrderNumber() < 1)
            throw new IllegalArgumentException("Thứ tự module phải lớn hơn 0!");

        module.setName(module.getName().trim());
    }

    @Override
    public List<CourseModule> getAllModules() {
        return moduleRepository.getAllModules();
    }
}