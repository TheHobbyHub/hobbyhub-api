package br.fatec.hobby_hub.infrastructure.repository;

import br.fatec.hobby_hub.infrastructure.entity.HorarioAula;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HorarioAulaRepository extends JpaRepository<HorarioAula, Long> {
    List<HorarioAula> findByAulaIdAndDataHoraAfterOrderByDataHoraAsc(Long aulaId, LocalDateTime dataHora);

    // Trava o horário durante o agendamento para que reservas simultâneas não ultrapassem a capacidade
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT h FROM HorarioAula h WHERE h.id = :id")
    Optional<HorarioAula> findByIdComLock(@Param("id") Long id);
}
