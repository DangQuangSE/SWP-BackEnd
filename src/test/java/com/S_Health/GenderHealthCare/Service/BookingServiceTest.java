package com.S_Health.GenderHealthCare.Service;

import com.S_Health.GenderHealthCare.dto.request.service.BookingRequest;
import com.S_Health.GenderHealthCare.dto.response.BookingResponse;
import com.S_Health.GenderHealthCare.entity.*;
import com.S_Health.GenderHealthCare.enums.AppointmentStatus;
import com.S_Health.GenderHealthCare.enums.ServiceType;
import com.S_Health.GenderHealthCare.enums.SlotStatus;
import com.S_Health.GenderHealthCare.repository.*;
import com.S_Health.GenderHealthCare.service.MedicalService.BookingService;
import com.S_Health.GenderHealthCare.service.medicalProfile.MedicalProfileService;
import com.S_Health.GenderHealthCare.service.schedule.ServiceSlotPoolService;
import com.S_Health.GenderHealthCare.utils.AuthUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.modelmapper.ModelMapper;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class BookingServiceTest {

    @InjectMocks
    private BookingService bookingService;

    @Mock private ServiceSlotPoolService serviceSlotPoolService;
    @Mock private ServiceRepository serviceRepository;
    @Mock private AuthenticationRepository authenticationRepository;
    @Mock private AppointmentDetailRepository appointmentDetailRepository;
    @Mock private AppointmentRepository appointmentRepository;
    @Mock private ServiceSlotPoolRepository serviceSlotPoolRepository;
    @Mock private ConsultantSlotRepository consultantSlotRepository;
    @Mock private MedicalProfileService medicalProfileService;
    @Mock private MedicalProfileRepository medicalProfileRepository;
    @Mock private AuthUtil authUtil;
    @Mock private ModelMapper modelMapper;
    @Mock private RoomRepository roomRepository;
    @Mock private RoomConsultantRepository roomConsultantRepository;

    private BookingRequest bookingRequest;
    private com.S_Health.GenderHealthCare.entity.Service service;
    private ServiceSlotPool slotPool;
    private User customer;
    private ConsultantSlot consultantSlot;

    @BeforeEach
    void setUp() {
        bookingRequest = new BookingRequest();
        bookingRequest.setPreferredDate(LocalDate.now().plusDays(1));
        bookingRequest.setSlot(LocalTime.of(9, 0));
        bookingRequest.setService_id(1L);
        bookingRequest.setSlot_id(10L);
        bookingRequest.setNote("Test booking");

        service = new com.S_Health.GenderHealthCare.entity.Service();
        service.setId(1L);
        service.setName("Test Service");
        service.setIsCombo(false);
        service.setType(ServiceType.CONSULTING_ON);
        service.setPrice(500.0);

        slotPool = new ServiceSlotPool();
        slotPool.setId(10L);
        slotPool.setDate(bookingRequest.getPreferredDate());
        slotPool.setStartTime(bookingRequest.getSlot());

        customer = new User();
        customer.setId(5L);
        customer.setFullname("Test User");

        consultantSlot = new ConsultantSlot();
        consultantSlot.setAvailableBooking(2);
        consultantSlot.setCurrentBooking(0);
        consultantSlot.setMaxBooking(2);
        consultantSlot.setStatus(SlotStatus.ACTIVE);
    }

    @Test
    void testBookingServiceSuccess() {
        when(authUtil.getCurrentUserId()).thenReturn(customer.getId());
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(service));
        when(serviceSlotPoolRepository.findById(10L)).thenReturn(Optional.of(slotPool));
        when(authenticationRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(appointmentDetailRepository.existsByAppointment_Customer_IdAndSlotTime(anyLong(), any())).thenReturn(false);
        when(serviceSlotPoolService.getConsultantInSpecialization(anyLong())).thenReturn(List.of(customer));
        when(consultantSlotRepository.findByConsultantAndDateAndStartTimeAndStatus(any(), any(), any(), any()))
                .thenReturn(Optional.of(consultantSlot));

        when(modelMapper.map(any(), eq(com.S_Health.GenderHealthCare.dto.AppointmentDetailDTO.class)))
                .thenReturn(new com.S_Health.GenderHealthCare.dto.AppointmentDetailDTO());

        BookingResponse response = bookingService.bookingService(bookingRequest);

        assertNotNull(response);
        assertEquals(customer.getFullname(), response.getCustomerName());
        assertEquals(AppointmentStatus.PENDING, response.getStatus());
        assertEquals(bookingRequest.getPreferredDate(), response.getDate());

        verify(appointmentRepository, times(2)).save(any());
        verify(consultantSlotRepository).saveAll(any());
        verify(medicalProfileService).createMedicalProfile(any());
    }

    @Test
    void testBookingWithInvalidSlot_throwsException() {
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(service));
        when(serviceSlotPoolRepository.findById(10L)).thenReturn(Optional.of(slotPool));
        bookingRequest.setSlot(LocalTime.of(15, 0)); // lệch so với slotPool

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> bookingService.bookingService(bookingRequest));

        assertEquals("Khung giờ không khớp với ngày/giờ yêu cầu!", ex.getMessage());
    }
}
