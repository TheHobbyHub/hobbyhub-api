package br.fatec.hobby_hub.dto;

import br.fatec.hobby_hub.infrastructure.entity.Parceiro;

public record ParceiroDTO(
        Long id,
        String nome,
        EnderecoDTO endereco
) {
    public ParceiroDTO(Parceiro parceiro) {
        this(parceiro.getId(), parceiro.getNome(), new EnderecoDTO(parceiro.getEndereco()));
    }
}
