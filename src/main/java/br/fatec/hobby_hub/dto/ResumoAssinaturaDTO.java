package br.fatec.hobby_hub.dto;

import java.time.LocalDate;

// Dados do card de assinatura da Home
public record ResumoAssinaturaDTO(
        Boolean possuiAssinatura,
        Long planoId,
        String planoNome,
        Integer creditosMensais,
        Integer saldoCreditos,
        LocalDate dataInicioCiclo,
        LocalDate dataRenovacao,
        Long aulasConcluidasNoCiclo
) {}
