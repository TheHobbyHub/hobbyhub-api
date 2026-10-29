package br.fatec.hobby_hub.dto;

import br.fatec.hobby_hub.infrastructure.entity.Instrutor;

public record InstrutorDTO(
        Long id,
        String nome,
        String especialidade,
        String fotoUrl
) {
    public InstrutorDTO(Instrutor instrutor) {
        this(
                instrutor.getId(),
                instrutor.getNome(),
                instrutor.getEspecialidade(),
                instrutor.getFotoUrl()
        );
    }
}
