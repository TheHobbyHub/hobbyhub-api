package br.fatec.hobby_hub.dto;

import br.fatec.hobby_hub.infrastructure.entity.Aula;
import br.fatec.hobby_hub.infrastructure.entity.Plano;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

// Dados completos da tela de Detalhes da Aula
public record AulaRespostaDTO(
        Long id,
        String titulo,
        String descricao,
        Long categoriaId,
        String categoria,
        String subcategoria,
        String nivel,
        Integer duracaoMinutos,
        String imagemUrl,
        ParceiroDTO parceiro,
        InstrutorDTO instrutor,
        List<String> planosAceitos,
        Boolean inclusoNoPlano,
        String planoMinimo
) {
    public AulaRespostaDTO(Aula aula, Boolean inclusoNoPlano, String planoMinimo) {
        this(
                aula.getId(),
                aula.getTitulo(),
                aula.getDescricao(),
                aula.getCategoria().getId(),
                aula.getCategoria().getNome(),
                aula.getSubcategoria(),
                aula.getNivel(),
                aula.getDuracaoMinutos(),
                aula.getImagemUrl(),
                new ParceiroDTO(aula.getParceiro()),
                new InstrutorDTO(aula.getInstrutor()),
                aula.getPlanosAceitos().stream()
                        .sorted(Comparator.comparing(Plano::getPrecoMensal, Comparator.nullsLast(BigDecimal::compareTo)))
                        .map(Plano::getNome)
                        .toList(),
                inclusoNoPlano,
                planoMinimo
        );
    }
}
