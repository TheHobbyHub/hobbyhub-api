package br.fatec.hobby_hub.dto;

import jakarta.validation.constraints.NotNull;

public record ReagendamentoRequestDTO(
        @NotNull(message = "O usuário é obrigatório")
        Long usuarioId,

        @NotNull(message = "O novo horário é obrigatório")
        Long novoHorarioId
) {}
