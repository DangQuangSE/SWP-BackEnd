package com.S_Health.GenderHealthCare.Service;

import com.S_Health.GenderHealthCare.dto.AppointmentDetailDTO;
import com.S_Health.GenderHealthCare.dto.request.service.BookingRequest;
import com.S_Health.GenderHealthCare.dto.response.BookingResponse;
import com.S_Health.GenderHealthCare.entity.*;
import com.S_Health.GenderHealthCare.enums.ServiceType;
import com.S_Health.GenderHealthCare.enums.SlotStatus;
import com.S_Health.GenderHealthCare.exception.exceptions.AppException;
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
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
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

    private BookingRequest request;
    private Appointment appointment;
    private com.S_Health.GenderHealthCare.entity.Service subService;
    private ServiceSlotPool slotPool;
    private User consultant;

    @BeforeEach
    void setUp() {
        request = new BookingRequest();
        request.setPreferredDate(LocalDate.now().plusDays(1));
        request.setSlot(LocalTime.of(9, 0));
        request.setService_id(1L);
        request.setSlot_id(10L);
        request.setNote("Test");

        appointment = new Appointment();
        appointment.setId(1L);

        subService = new com.S_Health.GenderHealthCare.entity.Service();
        subService.setId(1L);
        subService.setName("Tư vấn tâm lý");
        subService.setType(ServiceType.CONSULTING_ON);

        slotPool = new ServiceSlotPool();
        slotPool.setDate(request.getPreferredDate());
        slotPool.setStartTime(request.getSlot());

        consultant = new User();
        consultant.setId(2L);
        consultant.setFullname("Dr. Strange");
    }

    @Test
    void testCreateAppointmentDetail_NoConsultantAvailable_ShouldThrow() {
        when(serviceSlotPoolService.getConsultantInSpecialization(anyLong()))
            .thenReturn(List.of());

        AppException ex = assertThrows(AppException.class, () ->
            bookingService.createAppointmentDetail(request, appointment, subService));

        assertTrue(ex.getMessage().contains("Không tìm thấy tư vấn viên"));
    }

    @Test
    void testFindAvailableConsultant_ReturnsConsultant() {
        ConsultantSlot slot = new ConsultantSlot();
        slot.setAvailableBooking(1);
        slot.setStatus(SlotStatus.ACTIVE);

        when(consultantSlotRepository.findByConsultantAndDateAndStartTimeAndStatus(any(), any(), any(), any()))
            .thenReturn(Optional.of(slot));

        User found = bookingService.findAvailableConsultant(request, List.of(consultant));
        assertEquals(consultant.getId(), found.getId());
    }

    @Test
    void testFindAvailableConsultant_ThrowsIfNoneAvailable() {
        when(consultantSlotRepository.findByConsultantAndDateAndStartTimeAndStatus(any(), any(), any(), any()))
            .thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () ->
            bookingService.findAvailableConsultant(request, List.of(consultant)));
        assertTrue(ex.getMessage().contains("Không tìm thấy tư vấn viên"));
    }

    @Test
    void testUpdateServiceSlotPool_CalculatesCorrectly() {
        ConsultantSlot s1 = new ConsultantSlot();
        s1.setMaxBooking(2); s1.setCurrentBooking(1);

        ConsultantSlot s2 = new ConsultantSlot();
        s2.setMaxBooking(3); s2.setCurrentBooking(2);

        ServiceSlotPool pool = new ServiceSlotPool();
        bookingService.updateServiceSlotPool(pool, List.of(s1, s2));

        assertEquals(5, pool.getMaxBooking());
        assertEquals(3, pool.getCurrentBooking());
        assertEquals(2, pool.getAvailableBooking());
        assertTrue(pool.getIsActive());
    }

    @Test
    void testFindAvailableConsultant_SlotNull_ShouldThrow() {
        when(consultantSlotRepository.findByConsultantAndDateAndStartTimeAndStatus(any(), any(), any(), any()))
            .thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class,
            () -> bookingService.findAvailableConsultant(request, List.of(consultant)));

        assertEquals("Không tìm thấy tư vấn viên nào khả dụng cho thời gian đã chọn!", ex.getMessage());
    }

    @Test
    void testIsTimeSlotInWorkingHours_True() throws Exception {
        Method method = BookingService.class.getDeclaredMethod("isTimeSlotInWorkingHours",
            LocalTime.class, LocalTime.class, LocalTime.class);
        method.setAccessible(true);
        boolean result = (boolean) method.invoke(bookingService,
            LocalTime.of(10, 0), LocalTime.of(9, 0), LocalTime.of(11, 0));
        assertTrue(result);
    }

    @Test
    void testIsTimeSlotInWorkingHours_False() throws Exception {
        Method method = BookingService.class.getDeclaredMethod("isTimeSlotInWorkingHours",
            LocalTime.class, LocalTime.class, LocalTime.class);
        method.setAccessible(true);
        boolean result = (boolean) method.invoke(bookingService,
            LocalTime.of(8, 0), LocalTime.of(9, 0), LocalTime.of(11, 0));
        assertFalse(result);
    }

    @Test
    void testValidateEntities_ServiceNotFound_ShouldThrow() {
        when(serviceRepository.findById(anyLong())).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class,
            () -> bookingService.validateAndFetchBookingEntities(request, 1L));
        assertEquals("Không tìm thấy dịch vụ!", ex.getMessage());
    }

    @Test
    void testValidateEntities_SlotNotFound_ShouldThrow() {
        when(serviceRepository.findById(anyLong())).thenReturn(Optional.of(subService));
        when(serviceSlotPoolRepository.findById(anyLong())).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class,
            () -> bookingService.validateAndFetchBookingEntities(request, 1L));
        assertEquals("Không tìm thấy khung giờ này!", ex.getMessage());
    }

    @Test
    void testValidateEntities_SlotDateMismatch_ShouldThrow() {
        ServiceSlotPool pool = new ServiceSlotPool();
        pool.setDate(LocalDate.now().plusDays(2));
        pool.setStartTime(LocalTime.of(10, 0));

        when(serviceRepository.findById(anyLong())).thenReturn(Optional.of(subService));
        when(serviceSlotPoolRepository.findById(anyLong())).thenReturn(Optional.of(pool));

        AppException ex = assertThrows(AppException.class,
            () -> bookingService.validateAndFetchBookingEntities(request, 1L));
        assertEquals("Khung giờ không khớp với ngày/giờ yêu cầu!", ex.getMessage());
    }

    @Test
    void testValidateEntities_AlreadyBooked_ShouldThrow() {
        when(serviceRepository.findById(anyLong())).thenReturn(Optional.of(subService));
        when(serviceSlotPoolRepository.findById(anyLong())).thenReturn(Optional.of(slotPool));
        when(authenticationRepository.findById(anyLong())).thenReturn(Optional.of(consultant));
        when(appointmentDetailRepository.existsByAppointment_Customer_IdAndSlotTime(anyLong(), any())).thenReturn(true);

        AppException ex = assertThrows(AppException.class,
            () -> bookingService.validateAndFetchBookingEntities(request, 1L));
        assertEquals("Bạn đã có lịch hẹn vào khung giờ này", ex.getMessage());
    }

    @Test
    void testValidateEntities_ComboService_Success() {
        subService.setIsCombo(true);
        ComboItem comboItem = new ComboItem();
        com.S_Health.GenderHealthCare.entity.Service sub = new com.S_Health.GenderHealthCare.entity.Service();
        sub.setId(99L);
        comboItem.setSubService(sub);
        subService.setComboItems(List.of(comboItem));

        when(serviceRepository.findById(anyLong())).thenReturn(Optional.of(subService));
        when(serviceSlotPoolRepository.findById(anyLong())).thenReturn(Optional.of(slotPool));
        when(authenticationRepository.findById(anyLong())).thenReturn(Optional.of(consultant));
        when(appointmentDetailRepository.existsByAppointment_Customer_IdAndSlotTime(anyLong(), any())).thenReturn(false);

        BookingService.BookingContext context = bookingService.validateAndFetchBookingEntities(request, 1L);
        assertEquals(1, context.services().size());
        assertEquals(99L, context.services().get(0).getId());
    }

    @Test
    void testBookingService_Success() {
        subService.setIsCombo(false);
ConsultantSlot slot = new ConsultantSlot();
slot.setAvailableBooking(1);
slot.setCurrentBooking(0);
slot.setMaxBooking(1);
slot.setStatus(SlotStatus.ACTIVE);
        when(authUtil.getCurrentUserId()).thenReturn(1L);
        when(serviceRepository.findById(anyLong())).thenReturn(Optional.of(subService));
        when(serviceSlotPoolRepository.findById(anyLong())).thenReturn(Optional.of(slotPool));
        when(authenticationRepository.findById(anyLong())).thenReturn(Optional.of(consultant));
        when(appointmentDetailRepository.existsByAppointment_Customer_IdAndSlotTime(anyLong(), any())).thenReturn(false);
        when(serviceSlotPoolService.getConsultantInSpecialization(anyLong())).thenReturn(List.of(consultant));
        
when(consultantSlotRepository.findByConsultantAndDateAndStartTimeAndStatus(any(), any(), any(), eq(SlotStatus.ACTIVE)))
    .thenReturn(Optional.of(slot));
        when(modelMapper.map(any(), eq(AppointmentDetailDTO.class)))
            .thenReturn(new AppointmentDetailDTO());
        when(appointmentRepository.save(any())).thenReturn(appointment);

        BookingResponse response = bookingService.bookingService(request);

        assertNotNull(response);
        assertEquals(1L, response.getAppointmentId());
        assertEquals("Test", response.getNote());
    }
}
