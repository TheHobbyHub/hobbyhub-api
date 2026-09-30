package br.fatec.hobby_hub.dto;

public record AssinaturaRequestDTO(
        Long usuarioId,
        Long planoId,
        String metodoPagamento
) {}