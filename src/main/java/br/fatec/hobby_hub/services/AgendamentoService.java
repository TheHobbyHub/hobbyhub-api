package br.fatec.hobby_hub.services;

import br.fatec.hobby_hub.dto.AgendamentoRequestDTO;
import br.fatec.hobby_hub.dto.AgendamentoRespostaDTO;
import br.fatec.hobby_hub.dto.ReagendamentoRequestDTO;
import br.fatec.hobby_hub.infrastructure.entity.Agendamento;
import br.fatec.hobby_hub.infrastructure.entity.Assinatura;
import br.fatec.hobby_hub.infrastructure.entity.HorarioAula;
import br.fatec.hobby_hub.infrastructure.entity.Plano;
import br.fatec.hobby_hub.infrastructure.entity.StatusAgendamento;
import br.fatec.hobby_hub.infrastructure.entity.StatusUsuario;
import br.fatec.hobby_hub.infrastructure.entity.Usuario;
import br.fatec.hobby_hub.infrastructure.repository.AgendamentoRepository;
import br.fatec.hobby_hub.infrastructure.repository.HorarioAulaRepository;
import br.fatec.hobby_hub.infrastructure.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgendamentoService {

    private static final int HORAS_ALTERACAO_GRATUITA = 24;
    private static final int CREDITOS_POR_AULA = 1;

    private final AgendamentoRepository agendamentoRepository;
    private final HorarioAulaRepository horarioAulaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AssinaturaService assinaturaService;
    private final AulaService aulaService;

    @Transactional
    public AgendamentoRespostaDTO agendar(AgendamentoRequestDTO dto) {
        Usuario usuario = usuarioRepository.findById(dto.usuarioId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (usuario.getStatus() != StatusUsuario.ATIVO) {
            throw new RuntimeException("Usuário não está ativo.");
        }

        Plano plano = assinaturaService.buscarPlanoAtivo(usuario.getId());
        HorarioAula horario = buscarHorarioDisponivel(dto.horarioId(), usuario.getId(), plano);
        validarConflitoDeHorario(usuario.getId(), horario, null);

        debitarCredito(usuario);

        Agendamento agendamento = new Agendamento();
        agendamento.setUsuario(usuario);
        agendamento.setHorario(horario);
        agendamento.setDataAgendamento(LocalDateTime.now());
        agendamento.setCreditosPagos(CREDITOS_POR_AULA);
        agendamento.setStatus(StatusAgendamento.AGENDADO);

        return paraResposta(agendamentoRepository.save(agendamento));
    }

    // tipo: "proximas" (aulas que ainda vão acontecer), "concluidas" (histórico) ou vazio (todos, inclusive cancelados)
    @Transactional(readOnly = true)
    public List<AgendamentoRespostaDTO> listarPorUsuario(Long usuarioId, String tipo) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new RuntimeException("Usuário não encontrado");
        }

        List<AgendamentoRespostaDTO> agendamentos = agendamentoRepository.findByUsuarioIdOrderByHorarioDataHoraAsc(usuarioId)
                .stream()
                .map(this::paraResposta)
                .toList();

        if ("proximas".equalsIgnoreCase(tipo)) {
            return agendamentos.stream()
                    .filter(a -> a.status() == StatusAgendamento.AGENDADO)
                    .toList();
        }
        if ("concluidas".equalsIgnoreCase(tipo)) {
            return agendamentos.stream()
                    .filter(a -> a.status() == StatusAgendamento.CONCLUIDO)
                    .sorted(Comparator.comparing(AgendamentoRespostaDTO::data)
                            .thenComparing(AgendamentoRespostaDTO::horaInicio)
                            .reversed())
                    .toList();
        }
        return agendamentos;
    }

    // Só é permitido reagendar com 24h ou mais de antecedência; depois disso a vaga fica mantida e o crédito, descontado
    @Transactional
    public AgendamentoRespostaDTO reagendar(Long agendamentoId, ReagendamentoRequestDTO dto) {
        Agendamento agendamento = buscarAgendamentoAlteravel(agendamentoId, dto.usuarioId());

        if (!isAlteracaoGratuita(agendamento)) {
            throw new RuntimeException("O reagendamento só é permitido com no mínimo " + HORAS_ALTERACAO_GRATUITA
                    + " horas de antecedência. Sua vaga continua reservada e o crédito será descontado.");
        }

        if (agendamento.getHorario().getId().equals(dto.novoHorarioId())) {
            throw new RuntimeException("O novo horário deve ser diferente do horário atual.");
        }

        Plano plano = assinaturaService.buscarPlanoAtivo(dto.usuarioId());
        HorarioAula novoHorario = buscarHorarioDisponivel(dto.novoHorarioId(), dto.usuarioId(), plano);

        if (!novoHorario.getAula().getId().equals(agendamento.getHorario().getAula().getId())) {
            throw new RuntimeException("O reagendamento só pode ser feito para outro horário da mesma aula.");
        }

        validarConflitoDeHorario(dto.usuarioId(), novoHorario, agendamento.getId());

        agendamento.setHorario(novoHorario);
        return paraResposta(agendamentoRepository.save(agendamento));
    }

    // Com 24h ou mais de antecedência o crédito é devolvido; depois disso o crédito fica retido
    @Transactional
    public AgendamentoRespostaDTO cancelar(Long agendamentoId, Long usuarioId) {
        Agendamento agendamento = buscarAgendamentoAlteravel(agendamentoId, usuarioId);

        if (isAlteracaoGratuita(agendamento)) {
            estornarCredito(agendamento);
        }

        agendamento.setStatus(StatusAgendamento.CANCELADO);
        return paraResposta(agendamentoRepository.save(agendamento));
    }

    private HorarioAula buscarHorarioDisponivel(Long horarioId, Long usuarioId, Plano plano) {
        HorarioAula horario = horarioAulaRepository.findByIdComLock(horarioId)
                .orElseThrow(() -> new RuntimeException("Horário não encontrado"));

        if (!horario.getDataHora().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Este horário já passou. Escolha outro horário.");
        }

        if (!aulaService.aceitaPlano(horario.getAula(), plano)) {
            throw new RuntimeException("Esta aula não está disponível no seu plano (" + plano.getNome() + ").");
        }

        if (agendamentoRepository.existsByUsuarioIdAndHorarioIdAndStatus(usuarioId, horarioId, StatusAgendamento.AGENDADO)) {
            throw new RuntimeException("Você já está agendado neste horário.");
        }

        if (aulaService.calcularVagasDisponiveis(horario) <= 0) {
            throw new RuntimeException("Não há vagas disponíveis neste horário.");
        }

        return horario;
    }

    // O aluno não pode ter duas aulas que se sobrepõem no mesmo dia e horário
    private void validarConflitoDeHorario(Long usuarioId, HorarioAula horario, Long agendamentoIgnoradoId) {
        agendamentoRepository.findByUsuarioIdAndStatus(usuarioId, StatusAgendamento.AGENDADO).stream()
                .filter(a -> !a.getId().equals(agendamentoIgnoradoId))
                .filter(a -> a.getHorario().getDataHora().isBefore(horario.getDataHoraFim())
                        && horario.getDataHora().isBefore(a.getHorario().getDataHoraFim()))
                .findFirst()
                .ifPresent(conflito -> {
                    throw new RuntimeException("Você já tem a aula \"" + conflito.getHorario().getAula().getTitulo()
                            + "\" agendada nesse dia e horário.");
                });
    }

    private Agendamento buscarAgendamentoAlteravel(Long agendamentoId, Long usuarioId) {
        Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

        if (!agendamento.getUsuario().getId().equals(usuarioId)) {
            throw new RuntimeException("Este agendamento não pertence ao usuário informado.");
        }

        if (agendamento.getStatus() == StatusAgendamento.CANCELADO) {
            throw new RuntimeException("Este agendamento já foi cancelado.");
        }

        if (!agendamento.getHorario().getDataHora().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Não é possível alterar uma aula que já começou ou já terminou.");
        }

        return agendamento;
    }

    private boolean isAlteracaoGratuita(Agendamento agendamento) {
        LocalDateTime limite = agendamento.getHorario().getDataHora().minusHours(HORAS_ALTERACAO_GRATUITA);
        return LocalDateTime.now().isBefore(limite);
    }

    private void debitarCredito(Usuario usuario) {
        if (usuario.getSaldoCreditos() < CREDITOS_POR_AULA) {
            throw new RuntimeException("Saldo de créditos insuficiente.");
        }
        usuario.setSaldoCreditos(usuario.getSaldoCreditos() - CREDITOS_POR_AULA);
        usuarioRepository.save(usuario);
    }

    // O estorno nunca deixa o saldo acima dos créditos do plano, pois os créditos não acumulam entre ciclos
    private void estornarCredito(Agendamento agendamento) {
        Usuario usuario = agendamento.getUsuario();
        int saldoEstornado = usuario.getSaldoCreditos() + agendamento.getCreditosPagos();
        int limite = assinaturaService.buscarAssinaturaAtiva(usuario.getId())
                .map(Assinatura::getPlano)
                .map(Plano::getCreditosMensais)
                .orElse(saldoEstornado);

        usuario.setSaldoCreditos(Math.min(saldoEstornado, limite));
        usuarioRepository.save(usuario);
    }

    private AgendamentoRespostaDTO paraResposta(Agendamento agendamento) {
        return new AgendamentoRespostaDTO(agendamento, HORAS_ALTERACAO_GRATUITA);
    }
}
