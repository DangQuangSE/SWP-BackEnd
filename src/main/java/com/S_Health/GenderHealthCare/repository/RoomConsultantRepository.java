package com.S_Health.GenderHealthCare.repository;

import com.S_Health.GenderHealthCare.entity.Room;
import com.S_Health.GenderHealthCare.entity.RoomConsultant;
import com.S_Health.GenderHealthCare.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomConsultantRepository extends JpaRepository<RoomConsultant, Long> {
    List<RoomConsultant> findByRoomAndIsActiveTrue(Room room);
    List<RoomConsultant> findByConsultantAndIsActiveTrue(User consultant);
    Optional<RoomConsultant> findByRoomAndConsultantAndWorkingDayAndIsActiveTrue(Room room, User consultant, DayOfWeek workingDay);
    boolean existsByRoomAndConsultantAndWorkingDayAndStartTimeAndEndTimeAndIsActiveTrue(Room room, User consultant, DayOfWeek workingDay, LocalTime startTime, LocalTime endTime);
}
