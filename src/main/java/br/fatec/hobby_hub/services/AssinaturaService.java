package br.fatec.hobby_hub.services;

import br.fatec.hobby_hub.dto.AssinaturaRequestDTO;
import br.fatec.hobby_hub.dto.ResumoAssinaturaDTO;
import br.fatec.hobby_hub.infrastructure.entity.Assinatura;
import br.fatec.hobby_hub.infrastructure.entity.Plano;
import br.fatec.hobby_hub.infrastructure.entity.StatusAgendamento;
import br.fatec.hobby_hub.infrastructure.entity.Usuario;
import br.fatec.hobby_hub.infrastructure.repository.AgendamentoRepository;
import br.fatec.hobby_hub.infrastructure.repository.AssinaturaRepository;
import br.fatec.hobby_hub.infrastructure.repository.PlanoRepository;
import br.fatec.hobby_hub.infrastructure.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AssinaturaService {

    private final AssinaturaRepository assinaturaRepository;
    private final PlanoRepository planoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AgendamentoRepository agendamentoRepository;

    public List<Plano> listarPlanos() {
        return planoRepository.findAll();
    }

    @Transactional
    public String realizarAssinatura(AssinaturaRequestDTO dto) {
        Usuario usuario = usuarioRepository.findById(dto.usuarioId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        Plano plano = planoRepository.findById(dto.planoId())
                .orElseThrow(() -> new RuntimeException("Plano não encontrado"));

        List<Assinatura> assinaturasExistentes = assinaturaRepository
                .findByUsuarioIdAndStatusIn(usuario.getId(), List.of("ATIVA", "PENDENTE"));

        Assinatura assinatura = null;

        for (Assinatura ass : assinaturasExistentes) {
            if ("ATIVA".equals(ass.getStatus())) {
                throw new RuntimeException("O usuário já possui uma assinatura ativa.");
            }
            if ("PENDENTE".equals(ass.getStatus())) {
                assinatura = ass;
            }
        }

        if (assinatura == null) {
            assinatura = new Assinatura();
            assinatura.setUsuario(usuario);
        }

        assinatura.setPlano(plano);
        assinatura.setDataInicio(LocalDate.now());
        assinatura.setDataVencimento(LocalDate.now().plusMonths(1));

        String statusPagamento = simularGatewayPagamento(dto.metodoPagamento());

        if ("RECUSADO".equals(statusPagamento)) {
            throw new RuntimeException("Pagamento negado pela operadora. Tente utilizar outro método.");
        }

        if ("FALHA_SISTEMA".equals(statusPagamento)) {
            assinatura.setStatus("PENDENTE");
            assinaturaRepository.save(assinatura);
            return "Sistema de pagamento indisponível. Pedido salvo como Pendente. Tente concluir mais tarde.";
        }

        assinatura.setStatus("ATIVA");
        assinaturaRepository.save(assinatura);

        // Os créditos não acumulam: o saldo é sempre o valor contratado no plano
        usuario.setSaldoCreditos(plano.getCreditosMensais());
        usuarioRepository.save(usuario);

        return "Assinatura ativada com sucesso!";
    }

    // Renova automaticamente as assinaturas cujo ciclo terminou, voltando o saldo para os créditos do plano
    @Transactional
    public int renovarAssinaturasVencidas() {
        LocalDate hoje = LocalDate.now();
        List<Assinatura> vencidas = assinaturaRepository.findByStatusAndDataVencimentoLessThanEqual("ATIVA", hoje);

        for (Assinatura assinatura : vencidas) {
            // Se a aplicação ficou desligada por mais de um mês, avança até o ciclo atual
            while (!assinatura.getDataVencimento().isAfter(hoje)) {
                assinatura.setDataInicio(assinatura.getDataVencimento());
                assinatura.setDataVencimento(assinatura.getDataVencimento().plusMonths(1));
            }
            assinaturaRepository.save(assinatura);

            Usuario usuario = assinatura.getUsuario();
            usuario.setSaldoCreditos(assinatura.getPlano().getCreditosMensais());
            usuarioRepository.save(usuario);
        }

        return vencidas.size();
    }

    public Optional<Assinatura> buscarAssinaturaAtiva(Long usuarioId) {
        return assinaturaRepository.findByUsuarioIdAndStatusIn(usuarioId, List.of("ATIVA"))
                .stream()
                .findFirst();
    }

    public Plano buscarPlanoAtivo(Long usuarioId) {
        return buscarAssinaturaAtiva(usuarioId)
                .map(Assinatura::getPlano)
                .orElseThrow(() -> new RuntimeException("Você não possui uma assinatura ativa. Assine um plano para agendar aulas."));
    }

    public ResumoAssinaturaDTO resumo(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return buscarAssinaturaAtiva(usuarioId)
                .map(assinatura -> {
                    long concluidas = agendamentoRepository.countByUsuarioIdAndStatusAndHorarioDataHoraBetween(
                            usuarioId, StatusAgendamento.AGENDADO,
                            assinatura.getDataInicio().atStartOfDay(), LocalDateTime.now());
                    return new ResumoAssinaturaDTO(
                            true,
                            assinatura.getPlano().getId(),
                            assinatura.getPlano().getNome(),
                            assinatura.getPlano().getCreditosMensais(),
                            usuario.getSaldoCreditos(),
                            assinatura.getDataInicio(),
                            assinatura.getDataVencimento(),
                            concluidas
                    );
                })
                .orElseGet(() -> new ResumoAssinaturaDTO(false, null, null, null,
                        usuario.getSaldoCreditos(), null, null, 0L));
    }

    private String simularGatewayPagamento(String inputFrontend) {
        if ("recusado".equalsIgnoreCase(inputFrontend)) return "RECUSADO";
        if ("timeout".equalsIgnoreCase(inputFrontend)) return "FALHA_SISTEMA";
        return "APROVADO";
    }
}