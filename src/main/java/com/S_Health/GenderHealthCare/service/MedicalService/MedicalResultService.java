package com.S_Health.GenderHealthCare.service.MedicalService;

import com.S_Health.GenderHealthCare.dto.request.service.ResultRequest;
import com.S_Health.GenderHealthCare.entity.AppointmentDetail;
import com.S_Health.GenderHealthCare.repository.AppointmentDetailRepository;
import com.S_Health.GenderHealthCare.repository.MedicalResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MedicalResultService {
    @Autowired
    MedicalResultRepository medicalResultRepository;
    @Autowired
    AppointmentDetailRepository appointmentDetailRepository;
    public void saveResult(ResultRequest request){
        AppointmentDetail appointmentDetail = appointmentDetailRepository.findById(request.getAppointment_id()).orElseThrow(()-> new IllegalArgumentException("Không tìm thấy lịch hẹn!"));
    }
}