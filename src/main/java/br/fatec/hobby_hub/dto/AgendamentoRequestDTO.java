package br.fatec.hobby_hub.dto;

import jakarta.validation.constraints.NotNull;

public record AgendamentoRequestDTO(
        @NotNull(message = "O usuário é obrigatório")
        Long usuarioId,

        @NotNull(message = "O horário da aula é obrigatório")
        Long horarioId
) {}
