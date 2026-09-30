package br.fatec.hobby_hub.services;

import br.fatec.hobby_hub.dto.AssinaturaRequestDTO;
import br.fatec.hobby_hub.infrastructure.entity.Assinatura;
import br.fatec.hobby_hub.infrastructure.entity.Plano;
import br.fatec.hobby_hub.infrastructure.entity.Usuario;
import br.fatec.hobby_hub.infrastructure.repository.AssinaturaRepository;
import br.fatec.hobby_hub.infrastructure.repository.PlanoRepository;
import br.fatec.hobby_hub.infrastructure.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssinaturaService {

    private final AssinaturaRepository assinaturaRepository;
    private final PlanoRepository planoRepository;
    private final UsuarioRepository usuarioRepository;

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

        usuario.setSaldoCreditos(usuario.getSaldoCreditos() + plano.getCreditosMensais());
        usuarioRepository.save(usuario);

        return "Assinatura ativada com sucesso!";
    }

    private String simularGatewayPagamento(String inputFrontend) {
        if ("recusado".equalsIgnoreCase(inputFrontend)) return "RECUSADO";
        if ("timeout".equalsIgnoreCase(inputFrontend)) return "FALHA_SISTEMA";
        return "APROVADO";
    }
}