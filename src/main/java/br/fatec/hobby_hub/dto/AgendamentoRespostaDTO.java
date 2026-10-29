package br.fatec.hobby_hub.dto;

import br.fatec.hobby_hub.infrastructure.entity.Agendamento;
import br.fatec.hobby_hub.infrastructure.entity.Aula;
import br.fatec.hobby_hub.infrastructure.entity.HorarioAula;
import br.fatec.hobby_hub.infrastructure.entity.StatusAgendamento;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record AgendamentoRespostaDTO(
        Long id,
        Long usuarioId,
        Long aulaId,
        Long horarioId,
        String titulo,
        String categoria,
        String imagemUrl,
        String instrutorNome,
        ParceiroDTO parceiro,
        LocalDate data,
        LocalTime horaInicio,
        LocalTime horaFim,
        Integer creditosPagos,
        StatusAgendamento status,
        Boolean alteracaoGratuita,
        LocalDateTime dataAgendamento
) {
    public AgendamentoRespostaDTO(Agendamento agendamento, int horasAlteracaoGratuita) {
        this(agendamento, agendamento.getHorario(), agendamento.getHorario().getAula(),
                LocalDateTime.now(), horasAlteracaoGratuita);
    }

    private AgendamentoRespostaDTO(Agendamento agendamento, HorarioAula horario, Aula aula,
                                   LocalDateTime agora, int horasAlteracaoGratuita) {
        this(
                agendamento.getId(),
                agendamento.getUsuario().getId(),
                aula.getId(),
                horario.getId(),
                aula.getTitulo(),
                aula.getCategoria().getNome(),
                aula.getImagemUrl(),
                aula.getInstrutor().getNome(),
                new ParceiroDTO(aula.getParceiro()),
                horario.getDataHora().toLocalDate(),
                horario.getDataHora().toLocalTime(),
                horario.getDataHoraFim().toLocalTime(),
                agendamento.getCreditosPagos(),
                calcularStatus(agendamento, horario, agora),
                agendamento.getStatus() == StatusAgendamento.AGENDADO
                        && agora.isBefore(horario.getDataHora().minusHours(horasAlteracaoGratuita)),
                agendamento.getDataAgendamento()
        );
    }

    private static StatusAgendamento calcularStatus(Agendamento agendamento, HorarioAula horario, LocalDateTime agora) {
        if (agendamento.getStatus() == StatusAgendamento.AGENDADO && horario.getDataHoraFim().isBefore(agora)) {
            return StatusAgendamento.CONCLUIDO;
        }
        return agendamento.getStatus();
    }
}
