package com.S_Health.GenderHealthCare.api;

import com.S_Health.GenderHealthCare.dto.request.room.RoomConsultantRequest;
import com.S_Health.GenderHealthCare.dto.request.room.RoomRequest;
import com.S_Health.GenderHealthCare.dto.response.RoomConsultantDTO;
import com.S_Health.GenderHealthCare.dto.response.RoomDTO;
import com.S_Health.GenderHealthCare.service.room.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@SecurityRequirement(name = "api")
public class RoomAPI {
    @Autowired
    private RoomService roomService;

    @PostMapping
    @Operation(summary = "Tạo phòng mới", description = "Tạo phòng mới với chuyên môn cụ thể")
    public ResponseEntity<RoomDTO> createRoom(@Valid @RequestBody RoomRequest request) {
        return ResponseEntity.ok(roomService.createRoom(request));
    }

    @GetMapping
    @Operation(summary = "Lấy tất cả phòng", description = "Lấy danh sách tất cả các phòng đang hoạt động")
    public ResponseEntity<List<RoomDTO>> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    @GetMapping("/specialization/{specializationId}")
    @Operation(summary = "Lấy phòng theo chuyên môn", description = "Lấy danh sách các phòng theo chuyên môn")
    public ResponseEntity<List<RoomDTO>> getRoomsBySpecialization(@PathVariable Long specializationId) {
        return ResponseEntity.ok(roomService.getRoomsBySpecialization(specializationId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy phòng theo id", description = "Lấy thông tin phòng theo id")
    public ResponseEntity<RoomDTO> getRoomById(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoomById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật phòng", description = "Cập nhật thông tin phòng")
    public ResponseEntity<RoomDTO> updateRoom(
            @PathVariable Long id,
            @Valid @RequestBody RoomRequest request) {
        return ResponseEntity.ok(roomService.updateRoom(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa phòng", description = "Xóa phòng (soft delete)")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{roomId}/consultants")
    @Operation(summary = "Thêm bác sĩ vào phòng", description = "Thêm bác sĩ vào phòng với lịch làm việc cụ thể")
    public ResponseEntity<RoomConsultantDTO> addConsultantToRoom(
            @PathVariable Long roomId,
            @Valid @RequestBody RoomConsultantRequest request) {
        return ResponseEntity.ok(roomService.addConsultantToRoom(roomId, request));
    }

    @DeleteMapping("/{roomId}/consultants/{assignmentId}")
    @Operation(summary = "Xóa bác sĩ khỏi phòng", description = "Xóa lịch làm việc của bác sĩ trong phòng")
    public ResponseEntity<Void> removeConsultantFromRoom(
            @PathVariable Long roomId,
            @PathVariable Long assignmentId) {
        roomService.removeConsultantFromRoom(roomId, assignmentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{roomId}/consultants")
    @Operation(summary = "Lấy danh sách bác sĩ trong phòng", description = "Lấy danh sách bác sĩ và lịch làm việc trong phòng")
    public ResponseEntity<List<RoomConsultantDTO>> getConsultantsInRoom(@PathVariable Long roomId) {
        return ResponseEntity.ok(roomService.getConsultantsInRoom(roomId));
    }
}
