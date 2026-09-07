package br.fatec.hobby_hub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RedefinirSenhaDTO(
        @NotBlank(message = "O e-mail é obrigatório")
        String email,

        @NotBlank(message = "O código é obrigatório")
        String codigo,

        @NotBlank(message = "A nova senha é obrigatória")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$",
                message = "A senha deve conter ao menos 8 caracteres, incluindo letra maiúscula, minúscula, número e caractere especial (@$!%*?&#)"
        )
        String novaSenha
) {}