package com.S_Health.GenderHealthCare.service.MedicalService;

import com.S_Health.GenderHealthCare.dto.ResultDTO;
import com.S_Health.GenderHealthCare.dto.request.service.ResultRequest;
import com.S_Health.GenderHealthCare.entity.AppointmentDetail;
import com.S_Health.GenderHealthCare.entity.MedicalResult;
import com.S_Health.GenderHealthCare.entity.User;
import com.S_Health.GenderHealthCare.exception.exceptions.BadRequestException;
import com.S_Health.GenderHealthCare.repository.AppointmentDetailRepository;
import com.S_Health.GenderHealthCare.repository.AuthenticationRepository;
import com.S_Health.GenderHealthCare.repository.MedicalResultRepository;
import com.S_Health.GenderHealthCare.utils.AuthUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MedicalResultService {
    @Autowired
    MedicalResultRepository medicalResultRepository;
    @Autowired
    AppointmentDetailRepository appointmentDetailRepository;
    @Autowired
    AuthenticationRepository authenticationRepository;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    AuthUtil authUtil;

    public ResultDTO saveResult(ResultRequest request) {
        User writer = authenticationRepository.findById(authUtil.getCurrentUserId())
                .orElseThrow(() -> new BadRequestException("Không thể tìm thấy người nhập!"));
        AppointmentDetail appointmentDetail = appointmentDetailRepository.findById(request.getDetail_id())
                .orElseThrow(() -> new BadRequestException("không tìm thấy lịch hẹn chi tiết!"));
        MedicalResult medicalResult = MedicalResult.builder()
                .appointmentDetail(appointmentDetail)
                .consultant(writer)
                .description(request.getDescription())
                .diagnosis(request.getDiagnosis())
                .treatmentPlan(request.getTreatmentPlan())
                .isActive(true)
                .build();
        medicalResultRepository.save(medicalResult);
        return modelMapper.map(medicalResult, ResultDTO.class);
    }
    public ResultDTO getResultById(Long id) {
        MedicalResult result = medicalResultRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy kết quả hoặc đã bị xóa!"));

        return modelMapper.map(result, ResultDTO.class);
    }
    public List<ResultDTO> getAllResultsByAppointmentDetail(Long appointmentDetailId) {
        List<MedicalResult> results = medicalResultRepository
                .findAllByAppointmentDetailIdAndIsActiveTrue(appointmentDetailId);

        return results.stream()
                .map(result -> modelMapper.map(result, ResultDTO.class))
                .toList();
    }
    public ResultDTO updateResult(Long id, ResultRequest request) {
        User writer = authenticationRepository.findById(authUtil.getCurrentUserId())
                .orElseThrow(() -> new BadRequestException("Không thể tìm thấy người nhập!"));
        MedicalResult result = medicalResultRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy kết quả để cập nhật!"));
        result.setConsultant(writer);
        result.setDescription(request.getDescription());
        result.setDiagnosis(request.getDiagnosis());
        result.setTreatmentPlan(request.getTreatmentPlan());

        medicalResultRepository.save(result);
        return modelMapper.map(result, ResultDTO.class);
    }
    public void deleteResult(Long id) {
        MedicalResult result = medicalResultRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy kết quả để xóa!"));

        result.setIsActive(false);
        medicalResultRepository.save(result);
    }
}