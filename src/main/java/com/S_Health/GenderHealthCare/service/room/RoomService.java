package com.S_Health.GenderHealthCare.service.room;

import com.S_Health.GenderHealthCare.dto.UserDTO;
import com.S_Health.GenderHealthCare.dto.request.room.RoomConsultantRequest;
import com.S_Health.GenderHealthCare.dto.request.room.RoomRequest;
import com.S_Health.GenderHealthCare.dto.response.RoomConsultantDTO;
import com.S_Health.GenderHealthCare.dto.response.RoomDTO;
import com.S_Health.GenderHealthCare.entity.Room;
import com.S_Health.GenderHealthCare.entity.RoomConsultant;
import com.S_Health.GenderHealthCare.entity.Specialization;
import com.S_Health.GenderHealthCare.entity.User;
import com.S_Health.GenderHealthCare.enums.UserRole;
import com.S_Health.GenderHealthCare.exception.exceptions.BadRequestException;
import com.S_Health.GenderHealthCare.repository.RoomConsultantRepository;
import com.S_Health.GenderHealthCare.repository.RoomRepository;
import com.S_Health.GenderHealthCare.repository.SpecializationRepository;
import com.S_Health.GenderHealthCare.repository.AuthenticationRepository;
import com.S_Health.GenderHealthCare.utils.AuthUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomService {
    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomConsultantRepository roomConsultantRepository;

    @Autowired
    private SpecializationRepository specializationRepository;

    @Autowired
    private AuthenticationRepository authenticationRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AuthUtil authUtil;

    @Transactional
    public RoomDTO createRoom(RoomRequest request) {
        // Validate request
        if (roomRepository.existsByNameAndIsActiveTrue(request.getName())) {
            throw new BadRequestException("Phòng với tên này đã tồn tại");
        }

        Specialization specialization = specializationRepository.findById(request.getSpecializationId())
                .orElseThrow(() -> new BadRequestException("Không tìm thấy chuyên môn"));
        Room room = new Room();
        room.setName(request.getName());
        room.setDescription(request.getDescription());
//        room.setLocation(request.getLocation());
        room.setSpecialization(specialization);
        room.setActive(true);

        Room savedRoom = roomRepository.save(room);

        return convertToDTO(savedRoom);
    }

    public List<RoomDTO> getAllRooms() {
        return roomRepository.findByIsActiveTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<RoomDTO> getRoomsBySpecialization(Long specializationId) {
        Specialization specialization = specializationRepository.findById(specializationId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy chuyên môn"));

        return roomRepository.findBySpecializationAndIsActiveTrue(specialization).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public RoomDTO getRoomById(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy phòng"));

        if (!room.isActive()) {
            throw new BadRequestException("Phòng không còn hoạt động");
        }

        return convertToDTO(room);
    }

    @Transactional
    public RoomDTO updateRoom(Long roomId, RoomRequest request) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy phòng"));

        if (!room.isActive()) {
            throw new BadRequestException("Phòng không còn hoạt động");
        }

        if (!room.getName().equals(request.getName()) &&
                roomRepository.existsByNameAndIsActiveTrue(request.getName())) {
            throw new BadRequestException("Phòng với tên này đã tồn tại");
        }
        // Get specialization if changed
        if (!(room.getSpecialization().getId() == (request.getSpecializationId()))) {
            Specialization specialization = specializationRepository.findById(request.getSpecializationId())
                    .orElseThrow(() -> new BadRequestException("Không tìm thấy chuyên môn"));
            room.setSpecialization(specialization);
        }

        room.setName(request.getName());
        room.setDescription(request.getDescription());
//        room.setLocation(request.getLocation());
        room.setUpdatedAt(LocalDateTime.now());

        Room updatedRoom = roomRepository.save(room);

        return convertToDTO(updatedRoom);
    }

    @Transactional
    public void deleteRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy phòng"));

        if (!room.isActive()) {
            throw new BadRequestException("Phòng đã bị xóa trước đó");
        }

        // Deactivate all consultant assignments
        List<RoomConsultant> consultants = roomConsultantRepository.findByRoomAndIsActiveTrue(room);
        for (RoomConsultant consultant : consultants) {
            consultant.setActive(false);
            consultant.setUpdatedAt(LocalDateTime.now());
        }
        roomConsultantRepository.saveAll(consultants);

        // Deactivate room
        room.setActive(false);
        room.setUpdatedAt(LocalDateTime.now());
        roomRepository.save(room);
    }

    @Transactional
    public RoomConsultantDTO addConsultantToRoom(Long roomId, RoomConsultantRequest request) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy phòng"));

        if (!room.isActive()) {
            throw new BadRequestException("Phòng không còn hoạt động");
        }

        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new BadRequestException("Giờ bắt đầu phải trước giờ kết thúc");
        }
        User consultant = authenticationRepository.findById(request.getConsultantId())
                .orElseThrow(() -> new BadRequestException("Không tìm thấy bác sĩ"));

        if (consultant.getRole() != UserRole.CONSULTANT) {
            throw new BadRequestException("Người dùng không phải là bác sĩ");
        }

        boolean hasSpecialization = consultant.getSpecializations().stream()
                .anyMatch(spec -> spec.getId() == (room.getSpecialization().getId()));

        if (!hasSpecialization) {
            throw new BadRequestException("Bác sĩ không có chuyên môn phù hợp với phòng này");
        }
        if (roomConsultantRepository.existsByRoomAndConsultantAndStartTimeAndEndTimeAndIsActiveTrue(
                room, consultant, request.getStartTime(), request.getEndTime())) {
            throw new BadRequestException("Lịch làm việc này đã tồn tại");
        }
        RoomConsultant roomConsultant = new RoomConsultant();
        roomConsultant.setRoom(room);
        roomConsultant.setConsultant(consultant);
        roomConsultant.setStartTime(request.getStartTime());
        roomConsultant.setEndTime(request.getEndTime());
        roomConsultant.setActive(true);

        RoomConsultant savedAssignment = roomConsultantRepository.save(roomConsultant);

        return convertToConsultantDTO(savedAssignment);
    }

    @Transactional
    public void removeConsultantFromRoom(Long roomId, Long assignmentId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy phòng"));
        RoomConsultant assignment = roomConsultantRepository.findById(assignmentId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy lịch làm việc"));
        if (!(assignment.getRoom().getId() == (room.getId()))) {
            throw new BadRequestException("Lịch làm việc không thuộc phòng này");
        }
        if (!assignment.isActive()) {
            throw new BadRequestException("Lịch làm việc đã bị xóa trước đó");
        }
        // Deactivate assignment
        assignment.setActive(false);
        assignment.setUpdatedAt(LocalDateTime.now());
        roomConsultantRepository.save(assignment);
    }

    public List<RoomConsultantDTO> getConsultantsInRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy phòng"));

        return roomConsultantRepository.findByRoomAndIsActiveTrue(room).stream()
                .map(this::convertToConsultantDTO)
                .collect(Collectors.toList());
    }

    private RoomDTO convertToDTO(Room room) {
        RoomDTO dto = modelMapper.map(room, RoomDTO.class);

        // Get active consultants
        List<RoomConsultantDTO> consultants = roomConsultantRepository.findByRoomAndIsActiveTrue(room).stream()
                .map(this::convertToConsultantDTO)
                .collect(Collectors.toList());

        dto.setConsultants(consultants);
        return dto;
    }

    private RoomConsultantDTO convertToConsultantDTO(RoomConsultant roomConsultant) {
        RoomConsultantDTO dto = modelMapper.map(roomConsultant, RoomConsultantDTO.class);
        dto.setConsultant(modelMapper.map(roomConsultant.getConsultant(), UserDTO.class));
        return dto;
    }
}
