package br.fatec.hobby_hub.dto;

import br.fatec.hobby_hub.infrastructure.entity.Aula;
import br.fatec.hobby_hub.infrastructure.entity.Endereco;

// Dados do card de aula usado na Home e no Explorar
public record AulaResumoDTO(
        Long id,
        String titulo,
        Long categoriaId,
        String categoria,
        String imagemUrl,
        String parceiroNome,
        String bairro,
        String cidade,
        String estado,
        Integer duracaoMinutos,
        Boolean destaque,
        Boolean inclusoNoPlano,
        String planoMinimo
) {
    public AulaResumoDTO(Aula aula, Boolean inclusoNoPlano, String planoMinimo) {
        this(aula, aula.getParceiro().getEndereco(), inclusoNoPlano, planoMinimo);
    }

    private AulaResumoDTO(Aula aula, Endereco endereco, Boolean inclusoNoPlano, String planoMinimo) {
        this(
                aula.getId(),
                aula.getTitulo(),
                aula.getCategoria().getId(),
                aula.getCategoria().getNome(),
                aula.getImagemUrl(),
                aula.getParceiro().getNome(),
                endereco.getBairro(),
                endereco.getCidade(),
                endereco.getEstado(),
                aula.getDuracaoMinutos(),
                aula.getDestaque(),
                inclusoNoPlano,
                planoMinimo
        );
    }
}
