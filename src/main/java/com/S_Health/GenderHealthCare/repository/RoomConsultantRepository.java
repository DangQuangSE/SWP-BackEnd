package com.S_Health.GenderHealthCare.repository;

import com.S_Health.GenderHealthCare.entity.Room;
import com.S_Health.GenderHealthCare.entity.RoomConsultant;
import com.S_Health.GenderHealthCare.entity.Specialization;
import com.S_Health.GenderHealthCare.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomConsultantRepository extends JpaRepository<RoomConsultant, Long> {
    List<RoomConsultant> findByRoomAndIsActiveTrue(Room room);
    List<RoomConsultant> findByConsultantAndIsActiveTrue(User consultant);
    Optional<RoomConsultant> findByRoomAndConsultantAndIsActiveTrue(Room room, User consultant);
    boolean existsByRoomAndConsultantAndStartTimeAndEndTimeAndIsActiveTrue(Room room, User consultant, LocalTime startTime, LocalTime endTime);

    // Tìm tất cả RoomConsultant theo phòng có chuyên môn cụ thể
    @Query("SELECT rc FROM RoomConsultant rc WHERE rc.room.specialization = ?1 AND rc.isActive = true")
    List<RoomConsultant> findByRoomSpecializationAndIsActiveTrue(Specialization specialization);

    // Tìm tất cả RoomConsultant cho bác sĩ có chuyên môn cụ thể
    @Query("SELECT rc FROM RoomConsultant rc JOIN rc.consultant c JOIN c.specializations s WHERE s = ?1 AND rc.isActive = true")
    List<RoomConsultant> findByConsultantSpecializationAndIsActiveTrue(Specialization specialization);
}
