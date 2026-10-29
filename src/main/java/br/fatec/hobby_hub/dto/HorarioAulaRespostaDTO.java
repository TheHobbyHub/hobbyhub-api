package br.fatec.hobby_hub.dto;

import br.fatec.hobby_hub.infrastructure.entity.HorarioAula;

import java.time.LocalDate;
import java.time.LocalTime;

public record HorarioAulaRespostaDTO(
        Long id,
        Long aulaId,
        LocalDate data,
        LocalTime horaInicio,
        LocalTime horaFim,
        Integer capacidade,
        Integer vagasDisponiveis,
        Boolean disponivel
) {
    public HorarioAulaRespostaDTO(HorarioAula horario, Integer vagasDisponiveis) {
        this(
                horario.getId(),
                horario.getAula().getId(),
                horario.getDataHora().toLocalDate(),
                horario.getDataHora().toLocalTime(),
                horario.getDataHoraFim().toLocalTime(),
                horario.getCapacidade(),
                vagasDisponiveis,
                vagasDisponiveis > 0
        );
    }
}
