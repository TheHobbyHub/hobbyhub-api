package br.fatec.hobby_hub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UsuarioCadastroDTO(
        @NotBlank(message = "O nome é obrigatório")
        String nome,

        @NotBlank(message = "O sobrenome é obrigatório")
        String sobrenome,

        @NotBlank(message = "O e-mail é obrigatório")
        @Pattern(
                regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
                message = "Informe um e-mail válido (ex: exemplo@dominio.com)"
        )
        String email,

        @NotBlank(message = "A senha é obrigatória")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$",
                message = "A senha deve conter ao menos 8 caracteres, incluindo letra maiúscula, minúscula, número e caractere especial (@$!%*?&#)"
        )
        String senha,

        @NotBlank(message = "O CPF é obrigatório")
        @Pattern(
                regexp = "^\\d{11}$",
                message = "O CPF deve conter exatamente 11 dígitos numéricos"
        )
        String cpf,

        @NotBlank(message = "O telefone é obrigatório")
        @Pattern(
                regexp = "^\\d{10,11}$",
                message = "O telefone deve conter 10 ou 11 dígitos numéricos com DDD"
        )
        String telefone
) {}
