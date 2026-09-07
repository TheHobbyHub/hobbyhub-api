package br.fatec.hobby_hub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UsuarioAtualizacaoDTO(
        @NotBlank(message = "O nome é obrigatório")
        String nome,

        @Pattern(
                regexp = "^\\d{10,11}$",
                message = "O telefone deve conter 10 ou 11 dígitos numéricos com DDD"
        )
        String telefone
) {}
