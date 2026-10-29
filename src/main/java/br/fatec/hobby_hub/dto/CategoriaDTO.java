package br.fatec.hobby_hub.dto;

import br.fatec.hobby_hub.infrastructure.entity.Categoria;

public record CategoriaDTO(
        Long id,
        String nome,
        String icone
) {
    public CategoriaDTO(Categoria categoria) {
        this(categoria.getId(), categoria.getNome(), categoria.getIcone());
    }
}
