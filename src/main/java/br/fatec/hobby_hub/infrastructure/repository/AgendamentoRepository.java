package br.fatec.hobby_hub.infrastructure.repository;

import br.fatec.hobby_hub.infrastructure.entity.Agendamento;
import br.fatec.hobby_hub.infrastructure.entity.StatusAgendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {
    List<Agendamento> findByUsuarioIdOrderByHorarioDataHoraAsc(Long usuarioId);
    List<Agendamento> findByUsuarioIdAndStatus(Long usuarioId, StatusAgendamento status);
    long countByHorarioIdAndStatus(Long horarioId, StatusAgendamento status);
    boolean existsByUsuarioIdAndHorarioIdAndStatus(Long usuarioId, Long horarioId, StatusAgendamento status);
    long countByUsuarioIdAndStatusAndHorarioDataHoraBetween(Long usuarioId, StatusAgendamento status,
                                                            LocalDateTime inicio, LocalDateTime fim);
}
