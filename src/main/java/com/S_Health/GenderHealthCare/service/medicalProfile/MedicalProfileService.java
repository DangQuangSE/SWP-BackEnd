package com.S_Health.GenderHealthCare.service.medicalProfile;

import com.S_Health.GenderHealthCare.dto.*;
import com.S_Health.GenderHealthCare.dto.request.MedicalInfoUpdateRequest;
import com.S_Health.GenderHealthCare.dto.response.MedicalProfileDTO;
import com.S_Health.GenderHealthCare.entity.*;
import com.S_Health.GenderHealthCare.enums.ResultType;
import com.S_Health.GenderHealthCare.enums.ServiceType;
import com.S_Health.GenderHealthCare.enums.TestStatus;
import com.S_Health.GenderHealthCare.enums.UserRole;
import com.S_Health.GenderHealthCare.exception.exceptions.AppException;
import com.S_Health.GenderHealthCare.repository.*;
import com.S_Health.GenderHealthCare.service.appointment.AppointmentService;
import com.S_Health.GenderHealthCare.utils.AuthUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.data.domain.*;
import org.springframework.data.support.PageableExecutionUtils;

@Service
public class MedicalProfileService {
    @Autowired
    MedicalProfileRepository medicalProfileRepository;
    @Autowired
    ServiceRepository serviceRepository;
    @Autowired
    AppointmentRepository appointmentRepository;
    @Autowired
    AuthenticationRepository authenticationRepository;
    @Autowired
    AppointmentService appointmentService;
    @Autowired
    AppointmentDetailRepository appointmentDetailRepository;
    @Autowired
    MedicalResultRepository medicalResultRepository;
    @Autowired
    AuthUtil authUtil;
    @Autowired
    ModelMapper modelMapper;

    public void createMedicalProfile(Appointment appointment) {
        User user = authUtil.getCurrentUser();
        com.S_Health.GenderHealthCare.entity.Service service = serviceRepository.findById(appointment.getService().getId())
                .orElseThrow(() -> new RuntimeException("Service not found"));
        // Tìm MedicalProfile đã tồn tại
        Optional<MedicalProfile> existingProfile = medicalProfileRepository
                .findByCustomerAndServiceAndIsActiveTrue(user, service);
        MedicalProfile medicalProfile;
        List<Appointment> appointments = new ArrayList<>();
        appointments.add(appointment);
        if (existingProfile.isPresent()) {
            medicalProfile = existingProfile.get();
        } else {
            // Tạo mới nếu chưa tồn tại
            medicalProfile = new MedicalProfile();
            medicalProfile.setCustomer(user);
            medicalProfile.setService(service);
            medicalProfile = medicalProfileRepository.save(medicalProfile);
        }
        // Gán medicalProfile cho appointment và lưu
        appointment.setMedicalProfile(medicalProfile);
        appointmentRepository.save(appointment);
        medicalProfile.setAppointments(appointments);
        medicalProfileRepository.save(medicalProfile);
    }

    public MedicalProfileDTO getMyProfile(Long serviceId) {
        User user = authUtil.getCurrentUser();
        com.S_Health.GenderHealthCare.entity.Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new AppException("Không tìm thấy dịch vụ này!"));
        MedicalProfile medicalProfile = medicalProfileRepository.findByCustomerAndServiceAndIsActiveTrue(user, service)
                .orElseThrow(() -> new AppException("Không tìm thấy hồ sơ khám bệnh!"));
        return modelMapper.map(medicalProfile, MedicalProfileDTO.class);
    }
    /**
     * Xem lịch sử khám bệnh cần thiết của bệnh nhân (cho bác sĩ)
     */
    public PatientMedicalHistoryDTO getPatientHistory(Long patientId, int page, int size) {
        User currentDoctor = authUtil.getCurrentUser();

        // Kiểm tra quyền truy cập
        if (currentDoctor.getRole() != UserRole.CONSULTANT) {
            throw new AppException("Chỉ bác sĩ mới có thể xem lịch sử khám bệnh");
        }

        // Lấy thông tin bệnh nhân
        User patient = authenticationRepository.findById(patientId)
                .orElseThrow(() -> new AppException("Không tìm thấy bệnh nhân"));

        // Kiểm tra bác sĩ có quyền xem bệnh nhân này không
        boolean hasAccess = appointmentDetailRepository
                .existsByConsultantIdAndAppointmentCustomerId(currentDoctor.getId(), patientId);
        if (!hasAccess) {
            throw new AppException("Bạn không có quyền xem hồ sơ bệnh nhân này");
        }

        // Approach mới: Lấy 5 appointments gần nhất của bệnh nhân (đơn giản và hiệu quả)
        List<Appointment> recentAppointments = appointmentRepository
                .findAll().stream()
                .filter(appointment -> appointment.getCustomer().getId() == patientId)
                .filter(appointment -> appointment.getIsActive())
                .sorted((a1, a2) -> a2.getCreated_at().compareTo(a1.getCreated_at()))
                .limit(5) // Chỉ lấy 5 appointments gần nhất
                .collect(Collectors.toList());

        // Tạo Page từ list (không cần phân trang phức tạp vì chỉ có 5 records)
        Pageable pageable = PageRequest.of(0, 5);
        Page<Appointment> appointmentPage = PageableExecutionUtils.getPage(
                recentAppointments, pageable, () -> recentAppointments.size());

        // Lấy medical results từ các appointment details của 5 appointments này
        List<RecentTestResultDTO> recentTests = buildRecentTestsFromAppointments(recentAppointments);

        return PatientMedicalHistoryDTO.builder()
                .patientInfo(buildPatientInfo(patient))
                .appointments(buildAppointmentHistory(appointmentPage))
                .recentTests(recentTests)
                .totalVisits(recentAppointments.size())
                .build();
    }



    // Helper methods theo approach mới - đơn giản và hiệu quả
    private PatientBasicInfoDTO buildPatientInfo(User patient) {
        int age = patient.getDateOfBirth() != null ?
                Period.between(patient.getDateOfBirth(), LocalDate.now()).getYears() : 0;

        // Lấy thông tin y tế từ MedicalProfile mới nhất (aggregate từ tất cả services)
        List<MedicalProfile> profiles = medicalProfileRepository
                .findByCustomerIdAndIsActiveTrue(patient.getId());

        MedicalProfile latestProfile = profiles.stream()
                .filter(p -> p.getAllergies() != null || p.getFamilyHistory() != null ||
                           p.getLifestyleNotes() != null || p.getSpecialNotes() != null)
                .max((p1, p2) -> p1.getUpdatedAt().compareTo(p2.getUpdatedAt()))
                .orElse(null);

        return PatientBasicInfoDTO.builder()
                .fullname(patient.getFullname())
                .age(age)
                .gender(patient.getGender() != null ? patient.getGender().toString() : null)
                .email(patient.getEmail())
                .phone(patient.getPhone())
                // Thông tin y tế quan trọng
                .allergies(latestProfile != null ? latestProfile.getAllergies() : null)
                .familyHistory(latestProfile != null ? latestProfile.getFamilyHistory() : null)
                .lifestyleNotes(latestProfile != null ? latestProfile.getLifestyleNotes() : null)
                .specialNotes(latestProfile != null ? latestProfile.getSpecialNotes() : null)
                .build();
    }



    private Page<AppointmentHistoryDTO> buildAppointmentHistory(Page<Appointment> appointmentPage) {
        return appointmentPage.map(appointment -> {
            // Lấy AppointmentDetail đầu tiên để lấy thông tin
            AppointmentDetail firstDetail = appointment.getAppointmentDetails().stream()
                    .filter(detail -> detail.getIsActive())
                    .findFirst()
                    .orElse(null);

            String doctorName = firstDetail != null ?
                    firstDetail.getConsultant().getFullname() : "Chưa phân công";

            String roomName = getRoomDisplayName(firstDetail);

            // Lấy diagnosis từ MedicalResult nếu có
            String diagnosis = appointment.getAppointmentDetails().stream()
                    .filter(detail -> detail.getIsActive())
                    .map(detail -> medicalResultRepository.findByAppointmentDetail(detail))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(MedicalResult::getDiagnosis)
                    .filter(d -> d != null && !d.trim().isEmpty())
                    .findFirst()
                    .orElse("Chưa có chẩn đoán");

            return AppointmentHistoryDTO.builder()
                    .date(appointment.getPreferredDate())
                    .service(appointment.getService().getName())
                    .doctor(doctorName)
                    .room(roomName)
                    .status(appointment.getStatus().toString())
                    .diagnosis(diagnosis)
                    .build();
        });
    }

    /**
     * Approach mới: Lấy medical results từ 5 appointments gần nhất
     */
    private List<RecentTestResultDTO> buildRecentTestsFromAppointments(List<Appointment> recentAppointments) {
        try {
            List<RecentTestResultDTO> results = new ArrayList<>();

            for (Appointment appointment : recentAppointments) {
                // Lấy tất cả appointment details của appointment này
                List<AppointmentDetail> details = appointment.getAppointmentDetails().stream()
                        .filter(detail -> detail.getIsActive())
                        .collect(Collectors.toList());

                // Lấy medical results từ các appointment details
                for (AppointmentDetail detail : details) {
                    Optional<MedicalResult> resultOpt = medicalResultRepository.findByAppointmentDetail(detail);
                    if (resultOpt.isPresent()) {
                        MedicalResult result = resultOpt.get();

                        // Chỉ lấy LAB_TEST results
                        if (result.getResultType() == ResultType.LAB_TEST &&
                            result.getTestName() != null && !result.getTestName().trim().isEmpty()) {

                            results.add(RecentTestResultDTO.builder()
                                    .testName(result.getTestName())
                                    .result(result.getTestResult())
                                    .testDate(result.getCreatedAt().toLocalDate())
                                    .isAbnormal(result.getTestStatus() == TestStatus.ABNORMAL ||
                                               result.getTestStatus() == TestStatus.CRITICAL)
                                    .build());
                        }
                    }
                }
            }

            // Sort theo ngày mới nhất và limit 5
            return results.stream()
                    .sorted((r1, r2) -> r2.getTestDate().compareTo(r1.getTestDate()))
                    .limit(5)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("Error building recent tests from appointments: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Cập nhật thông tin y tế cơ bản khi check-in (cho staff)
     */
    public MedicalProfile updateMedicalInfo(MedicalInfoUpdateRequest request) {
        User currentStaff = authUtil.getCurrentUser();

        // Kiểm tra quyền (chỉ staff và admin)
        if (currentStaff.getRole() != UserRole.STAFF && currentStaff.getRole() != UserRole.ADMIN) {
            throw new AppException("Chỉ staff mới có thể cập nhật thông tin y tế");
        }

        // Lấy customer và service
        User customer = authenticationRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new AppException("Không tìm thấy bệnh nhân"));

        com.S_Health.GenderHealthCare.entity.Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new AppException("Không tìm thấy dịch vụ"));

        // Tìm hoặc tạo medical profile
        MedicalProfile profile = medicalProfileRepository
                .findByCustomerAndServiceAndIsActiveTrue(customer, service)
                .orElse(MedicalProfile.builder()
                        .customer(customer)
                        .service(service)
                        .isActive(true)
                        .build());

        // Cập nhật thông tin cơ bản mà staff được phép nhập
        profile.setAllergies(request.getAllergies());
        profile.setChronicConditions(request.getChronicConditions());
        profile.setFamilyHistory(request.getFamilyHistory());
        profile.setLifestyleNotes(request.getLifestyleNotes());
        profile.setSpecialNotes(request.getSpecialNotes());
        profile.setEmergencyContact(request.getEmergencyContact());
        profile.setLastUpdatedBy(currentStaff.getId());

        return medicalProfileRepository.save(profile);
    }

    /**
     * Lấy thông tin y tế để hiển thị cho bác sĩ
     */
    public MedicalProfile getMedicalInfoForDoctor(Long customerId, Long serviceId) {
        User customer = authenticationRepository.findById(customerId)
                .orElseThrow(() -> new AppException("Không tìm thấy bệnh nhân"));

        com.S_Health.GenderHealthCare.entity.Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new AppException("Không tìm thấy dịch vụ"));

        return medicalProfileRepository
                .findByCustomerAndServiceAndIsActiveTrue(customer, service)
                .orElse(null); // Trả về null nếu chưa có thông tin
    }

    /**
     * Lấy tên phòng để hiển thị, xử lý trường hợp consulting online
     */
    private String getRoomDisplayName(AppointmentDetail appointmentDetail) {
        if (appointmentDetail == null) {
            return "Chưa phân công";
        }

        // Kiểm tra nếu là consulting online
        if (appointmentDetail.getService() != null &&
            appointmentDetail.getService().getType() == ServiceType.CONSULTING_ON) {
            return "Tư vấn trực tuyến";
        }

        // Trường hợp khác, hiển thị tên phòng
        if (appointmentDetail.getRoom() != null) {
            return appointmentDetail.getRoom().getName();
        }

        return "Chưa phân phòng";
    }
}

