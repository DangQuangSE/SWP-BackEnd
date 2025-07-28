package com.S_Health.GenderHealthCare.service.MedicalService;

import com.S_Health.GenderHealthCare.dto.ResultDTO;
import com.S_Health.GenderHealthCare.dto.request.service.ConsultationResultRequest;
import com.S_Health.GenderHealthCare.dto.request.service.LabTestResultRequest;
import com.S_Health.GenderHealthCare.dto.request.service.ResultRequest;
import com.S_Health.GenderHealthCare.entity.*;
import com.S_Health.GenderHealthCare.enums.AppointmentStatus;
import com.S_Health.GenderHealthCare.enums.ResultType;
import com.S_Health.GenderHealthCare.exception.exceptions.AppException;
import com.S_Health.GenderHealthCare.repository.*;
import com.S_Health.GenderHealthCare.service.appointment.AppointmentStatusCalculator;
import com.S_Health.GenderHealthCare.utils.AuthUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MedicalResultService {
    @Autowired
    MedicalResultRepository medicalResultRepository;
    @Autowired
    AppointmentDetailRepository appointmentDetailRepository;
    @Autowired
    AppointmentRepository appointmentRepository;
    @Autowired
    AuthenticationRepository authenticationRepository;
    @Autowired
    MedicalProfileRepository medicalProfileRepository;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    AuthUtil authUtil;
    @Autowired
    AppointmentStatusCalculator statusCalculator;
    @Autowired
    TreatmentProtocolRepository treatmentProtocolRepository;

    // === API MỚI - RIÊNG BIỆT CHO TỪNG LOẠI ===

    /**
     * Lưu kết quả tư vấn khám bệnh
     */
    public ResultDTO saveConsultationResult(ConsultationResultRequest request) {
        User writer = authenticationRepository.findById(authUtil.getCurrentUserId())
                .orElseThrow(() -> new AppException("Không thể tìm thấy người nhập!"));
        AppointmentDetail appointmentDetail = appointmentDetailRepository.findById(request.getAppointmentDetailId())
                .orElseThrow(() -> new AppException("Không tìm thấy chi tiết cuộc hẹn!"));
        TreatmentProtocol protocol = treatmentProtocolRepository.findById(request.getTreatmentProtocolId())
                .orElseThrow(()-> new AppException("không tìm thấy phác đồ!"));


        MedicalResult medicalResult = MedicalResult.builder()
                .appointmentDetail(appointmentDetail)
                .consultant(writer)
                .resultType(ResultType.CONSULTATION)  // Cố định là CONSULTATION
                .description(request.getDescription())
                .diagnosis(request.getDiagnosis())
                .treatmentPlan(request.getTreatmentPlan())
                .treatmentProtocol(protocol)
                // Không set các field xét nghiệm (để null)
                .isActive(true)
                .build();

        // Lưu kết quả khám
        medicalResultRepository.save(medicalResult);

        // Cập nhật trạng thái appointment
        updateAppointmentStatus(appointmentDetail);

        return mapToFullResultDTO(medicalResult);
    }

    /**
     * Lưu kết quả xét nghiệm
     */
    public ResultDTO saveLabTestResult(LabTestResultRequest request) {
        User writer = authenticationRepository.findById(authUtil.getCurrentUserId())
                .orElseThrow(() -> new AppException("Không thể tìm thấy người nhập!"));
        AppointmentDetail appointmentDetail = appointmentDetailRepository.findById(request.getAppointmentDetailId())
                .orElseThrow(() -> new AppException("Không tìm thấy chi tiết cuộc hẹn!"));
        TreatmentProtocol protocol = treatmentProtocolRepository.findById(request.getTreatmentProtocolId())
                .orElseThrow(()-> new AppException("không tìm thấy phác đồ!"));

        MedicalResult medicalResult = MedicalResult.builder()
                .appointmentDetail(appointmentDetail)
                .consultant(writer)
                .resultType(ResultType.LAB_TEST)  // Cố định là LAB_TEST
                .description(request.getDescription())
                .diagnosis(request.getDiagnosis())
                .treatmentPlan(request.getTreatmentPlan())
                // Thông tin xét nghiệm
                .testName(request.getTestName())
                .testResult(request.getTestResult())
                .normalRange(request.getNormalRange())
                .testMethod(request.getTestMethod())
                .specimenType(request.getSpecimenType())
                .testStatus(request.getTestStatus())
                .sampleCollectedAt(request.getSampleCollectedAt())
                .labNotes(request.getLabNotes())
                .treatmentProtocol(protocol)
                .isActive(true)
                .build();

        // Lưu kết quả xét nghiệm
        medicalResultRepository.save(medicalResult);

        // Tự động cập nhật thông tin y tế từ kết quả xét nghiệm
        updateMedicalProfileFromTestResult(medicalResult, appointmentDetail);

        // Cập nhật trạng thái appointment
        updateAppointmentStatus(appointmentDetail);

        return mapToFullResultDTO(medicalResult);
    }

    // === API CŨ - GIỮ LẠI ĐỂ BACKWARD COMPATIBILITY ===

    /**
     * Helper method để map MedicalResult sang ResultDTO với đầy đủ thông tin
     */
    private ResultDTO mapToFullResultDTO(MedicalResult result) {
        ResultDTO dto = modelMapper.map(result, ResultDTO.class);

        // Thêm thông tin liên quan
        AppointmentDetail appointmentDetail = result.getAppointmentDetail();
        if (appointmentDetail != null) {
            dto.setAppointmentDetailId(appointmentDetail.getId());
            dto.setServiceName(appointmentDetail.getService().getName());

            if (appointmentDetail.getAppointment() != null &&
                    appointmentDetail.getAppointment().getCustomer() != null) {
                dto.setPatientName(appointmentDetail.getAppointment().getCustomer().getFullname());
            }
        }

        if (result.getConsultant() != null) {
            dto.setConsultantName(result.getConsultant().getFullname());
        }

        return dto;
    }

    public ResultDTO getResultById(Long id) {
        MedicalResult result = medicalResultRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new AppException("Không tìm thấy kết quả hoặc đã bị xóa!"));

        return mapToFullResultDTO(result);
    }

    public List<ResultDTO> getAllResultsByAppointmentDetail(Long appointmentDetailId) {
        List<MedicalResult> results = medicalResultRepository
                .findAllByAppointmentDetailIdAndIsActiveTrue(appointmentDetailId);

        return results.stream()
                .map(this::mapToFullResultDTO)
                .toList();
    }

    public ResultDTO updateResult(Long id, ResultRequest request) {
        User writer = authenticationRepository.findById(authUtil.getCurrentUserId())
                .orElseThrow(() -> new AppException("Không thể tìm thấy người nhập!"));
        MedicalResult result = medicalResultRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new AppException("Không tìm thấy kết quả để cập nhật!"));
        result.setConsultant(writer);
        result.setDescription(request.getDescription());
        result.setDiagnosis(request.getDiagnosis());
        result.setTreatmentPlan(request.getTreatmentPlan());

        medicalResultRepository.save(result);
        return modelMapper.map(result, ResultDTO.class);
    }

    public void deleteResult(Long id) {
        MedicalResult result = medicalResultRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new AppException("Không tìm thấy kết quả để xóa!"));

        result.setIsActive(false);
        medicalResultRepository.save(result);
    }

    private void updateMedicalProfileFromTestResult(MedicalResult result, AppointmentDetail appointmentDetail) {
        try {
            if (result.getResultType() != ResultType.LAB_TEST) {
                return;
            }
            // Tìm MedicalProfile của bệnh nhân
            User customer = appointmentDetail.getAppointment().getCustomer();
            com.S_Health.GenderHealthCare.entity.Service service = appointmentDetail.getService();

            Optional<MedicalProfile> profileOpt = medicalProfileRepository
                    .findByCustomerAndServiceAndIsActiveTrue(customer, service);

            if (profileOpt.isEmpty()) {
                return; // Không có profile thì không cập nhật
            }
            MedicalProfile profile = profileOpt.get();

            //profile.setLastUpdatedBy(result.getConsultant().getId());
            medicalProfileRepository.save(profile);
        } catch (Exception e) {
            // Log error nhưng không throw để không ảnh hưởng đến việc lưu kết quả
            throw new AppException("lỗi khi update Medical Profile từ Result: " + e.getMessage());
        }
    }
    private void updateAppointmentStatus(AppointmentDetail appointmentDetail) {
        // Tự động cập nhật trạng thái AppointmentDetail thành COMPLETED
        appointmentDetail.setStatus(AppointmentStatus.COMPLETED);
        appointmentDetail.setUpdate_at(LocalDateTime.now());
        appointmentDetailRepository.save(appointmentDetail);

        // Tính toán lại trạng thái Appointment dựa trên tất cả AppointmentDetail
        Appointment appointment = appointmentDetail.getAppointment();
        if (appointment != null) {
            List<AppointmentDetail> allDetails = appointmentDetailRepository
                    .findByAppointmentAndIsActiveTrue(appointment);

            // Tính toán status mới theo quy tắc
            AppointmentStatus newStatus = statusCalculator.calculateStatus(allDetails);
            AppointmentStatus oldStatus = appointment.getStatus();

            if (oldStatus != newStatus) {
                appointment.setStatus(newStatus);
                appointment.setUpdate_at(LocalDateTime.now());
                appointmentRepository.save(appointment);
            }
        }
    }
}