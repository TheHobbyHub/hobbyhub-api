package br.fatec.hobby_hub.services;

import br.fatec.hobby_hub.dto.AulaRespostaDTO;
import br.fatec.hobby_hub.dto.CategoriaDTO;
import br.fatec.hobby_hub.dto.AulaResumoDTO;
import br.fatec.hobby_hub.dto.HorarioAulaRespostaDTO;
import br.fatec.hobby_hub.infrastructure.entity.Assinatura;
import br.fatec.hobby_hub.infrastructure.entity.Aula;
import br.fatec.hobby_hub.infrastructure.entity.HorarioAula;
import br.fatec.hobby_hub.infrastructure.entity.Plano;
import br.fatec.hobby_hub.infrastructure.entity.StatusAgendamento;
import br.fatec.hobby_hub.infrastructure.repository.AgendamentoRepository;
import br.fatec.hobby_hub.infrastructure.repository.AulaRepository;
import br.fatec.hobby_hub.infrastructure.repository.CategoriaRepository;
import br.fatec.hobby_hub.infrastructure.repository.HorarioAulaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AulaService {

    private final AulaRepository aulaRepository;
    private final CategoriaRepository categoriaRepository;
    private final HorarioAulaRepository horarioAulaRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final AssinaturaService assinaturaService;

    @Transactional(readOnly = true)
    public List<AulaResumoDTO> listar(Long usuarioId, Long categoriaId, String busca, String cidade,
                                      Boolean somenteInclusasNoPlano, Boolean somenteDestaques) {
        Optional<Plano> plano = buscarPlanoDoUsuario(usuarioId);

        return aulaRepository.findAll().stream()
                .filter(aula -> categoriaId == null || aula.getCategoria().getId().equals(categoriaId))
                .filter(aula -> vazio(cidade) || aula.getParceiro().getEndereco().getCidade().equalsIgnoreCase(cidade))
                .filter(aula -> !Boolean.TRUE.equals(somenteDestaques) || aula.getDestaque())
                .filter(aula -> vazio(busca) || correspondeABusca(aula, busca))
                .map(aula -> new AulaResumoDTO(aula, inclusoNoPlano(aula, usuarioId, plano), planoMinimo(aula)))
                .filter(dto -> !Boolean.TRUE.equals(somenteInclusasNoPlano) || Boolean.TRUE.equals(dto.inclusoNoPlano()))
                .sorted(Comparator.comparing(AulaResumoDTO::titulo))
                .toList();
    }

    public List<CategoriaDTO> listarCategorias() {
        return categoriaRepository.findAllByOrderByNomeAsc()
                .stream()
                .map(CategoriaDTO::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public AulaRespostaDTO buscarPorId(Long id, Long usuarioId) {
        Aula aula = buscarAula(id);
        return new AulaRespostaDTO(aula, inclusoNoPlano(aula, usuarioId, buscarPlanoDoUsuario(usuarioId)), planoMinimo(aula));
    }

    // Retorna todos os horários futuros, inclusive os lotados, para a tela exibir "Horário indisponível"
    @Transactional(readOnly = true)
    public List<HorarioAulaRespostaDTO> listarHorarios(Long aulaId) {
        buscarAula(aulaId);

        return horarioAulaRepository.findByAulaIdAndDataHoraAfterOrderByDataHoraAsc(aulaId, LocalDateTime.now())
                .stream()
                .map(horario -> new HorarioAulaRespostaDTO(horario, calcularVagasDisponiveis(horario)))
                .toList();
    }

    public int calcularVagasDisponiveis(HorarioAula horario) {
        long ocupadas = agendamentoRepository.countByHorarioIdAndStatus(horario.getId(), StatusAgendamento.AGENDADO);
        return (int) Math.max(0, horario.getCapacidade() - ocupadas);
    }

    public boolean aceitaPlano(Aula aula, Plano plano) {
        return aula.getPlanosAceitos().stream()
                .anyMatch(p -> p.getId().equals(plano.getId()));
    }

    private Optional<Plano> buscarPlanoDoUsuario(Long usuarioId) {
        if (usuarioId == null) {
            return Optional.empty();
        }
        return assinaturaService.buscarAssinaturaAtiva(usuarioId).map(Assinatura::getPlano);
    }

    // Sem usuarioId não há como saber o plano, então o campo vem nulo
    private Boolean inclusoNoPlano(Aula aula, Long usuarioId, Optional<Plano> plano) {
        if (usuarioId == null) {
            return null;
        }
        return plano.map(p -> aceitaPlano(aula, p)).orElse(false);
    }

    // Plano mais barato que dá acesso à aula, usado em "Disponível a partir do Plano X"
    private String planoMinimo(Aula aula) {
        return aula.getPlanosAceitos().stream()
                .min(Comparator.comparing(Plano::getPrecoMensal))
                .map(Plano::getNome)
                .orElse(null);
    }

    private boolean correspondeABusca(Aula aula, String busca) {
        String termo = normalizar(busca);
        return normalizar(aula.getTitulo()).contains(termo)
                || normalizar(aula.getCategoria().getNome()).contains(termo)
                || normalizar(aula.getSubcategoria()).contains(termo)
                || normalizar(aula.getParceiro().getNome()).contains(termo);
    }

    // Ignora maiúsculas e acentos, para "ceramica" encontrar "Cerâmica"
    private String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();
    }

    private boolean vazio(String texto) {
        return texto == null || texto.isBlank();
    }

    private Aula buscarAula(Long id) {
        return aulaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Aula não encontrada"));
    }
}
