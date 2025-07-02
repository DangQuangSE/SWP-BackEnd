package com.S_Health.GenderHealthCare.service.MedicalService;

import com.S_Health.GenderHealthCare.dto.ResultDTO;
import com.S_Health.GenderHealthCare.dto.request.service.ResultRequest;
import com.S_Health.GenderHealthCare.entity.Appointment;
import com.S_Health.GenderHealthCare.entity.AppointmentDetail;
import com.S_Health.GenderHealthCare.entity.MedicalProfile;
import com.S_Health.GenderHealthCare.entity.MedicalResult;
import com.S_Health.GenderHealthCare.entity.User;
import com.S_Health.GenderHealthCare.enums.AppointmentStatus;
import com.S_Health.GenderHealthCare.enums.ResultType;
import com.S_Health.GenderHealthCare.exception.exceptions.BadRequestException;
import com.S_Health.GenderHealthCare.repository.AppointmentDetailRepository;
import com.S_Health.GenderHealthCare.repository.AppointmentRepository;
import com.S_Health.GenderHealthCare.repository.AuthenticationRepository;
import com.S_Health.GenderHealthCare.repository.MedicalProfileRepository;
import com.S_Health.GenderHealthCare.repository.MedicalResultRepository;
import com.S_Health.GenderHealthCare.service.appointment.AppointmentStatusCalculator;
import com.S_Health.GenderHealthCare.utils.AuthUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public ResultDTO saveResult(ResultRequest request) {
        User writer = authenticationRepository.findById(authUtil.getCurrentUserId())
                .orElseThrow(() -> new BadRequestException("Không thể tìm thấy người nhập!"));
        AppointmentDetail appointmentDetail = appointmentDetailRepository.findById(request.getAppointmentDetailId())
                .orElseThrow(() -> new BadRequestException("Không tìm thấy chi tiết cuộc hẹn!"));

        MedicalResult medicalResult = MedicalResult.builder()
                .appointmentDetail(appointmentDetail)
                .consultant(writer)
                .resultType(request.getResultType())
                .description(request.getDescription())
                .diagnosis(request.getDiagnosis())
                .treatmentPlan(request.getTreatmentPlan())
                // Thông tin xét nghiệm (nếu có)
                .testName(request.getTestName())
                .testResult(request.getTestResult())
                .normalRange(request.getNormalRange())
                .testMethod(request.getTestMethod())
                .specimenType(request.getSpecimenType())
                .testStatus(request.getTestStatus())
                .sampleCollectedAt(request.getSampleCollectedAt())
                .labNotes(request.getLabNotes())
                .isActive(true)
                .build();

        // Lưu kết quả khám
        medicalResultRepository.save(medicalResult);

        // Tự động cập nhật thông tin y tế từ kết quả xét nghiệm
        updateMedicalProfileFromTestResult(medicalResult, appointmentDetail);

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

        // Map sang ResultDTO với đầy đủ thông tin
        return mapToFullResultDTO(medicalResult);
    }

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
                .orElseThrow(() -> new BadRequestException("Không tìm thấy kết quả hoặc đã bị xóa!"));

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

    /**
     * Tự động cập nhật thông tin y tế từ kết quả xét nghiệm
     */
    private void updateMedicalProfileFromTestResult(MedicalResult result, AppointmentDetail appointmentDetail) {
        try {
            if (result.getResultType() != ResultType.LAB_TEST) {
                return; // Chỉ xử lý kết quả xét nghiệm
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
            boolean needUpdate = false;

            // Tự động cập nhật nhóm máu từ xét nghiệm máu
            if (result.getTestName() != null &&
                (result.getTestName().toLowerCase().contains("nhóm máu") ||
                result.getTestName().toLowerCase().contains("blood type"))) {

                String bloodType = extractBloodTypeFromResult(result.getTestResult());
                if (bloodType != null && (profile.getBloodType() == null || profile.getBloodType().isEmpty())) {
                    profile.setBloodType(bloodType);
                    needUpdate = true;
                }
            }

            // Có thể thêm logic khác để extract thông tin từ các xét nghiệm khác
            // Ví dụ: HbA1c → cập nhật chronic conditions về tiểu đường
            // Cholesterol → cập nhật về tim mạch, etc.

            if (needUpdate) {
                profile.setLastUpdatedBy(result.getConsultant().getId());
                medicalProfileRepository.save(profile);
            }

        } catch (Exception e) {
            // Log error nhưng không throw để không ảnh hưởng đến việc lưu kết quả
            System.err.println("Error updating medical profile from test result: " + e.getMessage());
        }
    }
    /**
     * Extract nhóm máu từ kết quả xét nghiệm
     */
    private String extractBloodTypeFromResult(String testResult) {
        if (testResult == null || testResult.trim().isEmpty()) {
            return null;
        }

        // Tìm pattern nhóm máu: A+, A-, B+, B-, AB+, AB-, O+, O-
        String result = testResult.toUpperCase().trim();

        if (result.matches(".*\\b(A|B|AB|O)[+-]\\b.*")) {
            // Extract nhóm máu từ string
            if (result.contains("A+")) return "A+";
            if (result.contains("A-")) return "A-";
            if (result.contains("B+")) return "B+";
            if (result.contains("B-")) return "B-";
            if (result.contains("AB+")) return "AB+";
            if (result.contains("AB-")) return "AB-";
            if (result.contains("O+")) return "O+";
            if (result.contains("O-")) return "O-";
        }
        return null;
    }
}