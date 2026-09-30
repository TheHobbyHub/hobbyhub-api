package br.fatec.hobby_hub.services;

import br.fatec.hobby_hub.dto.RedefinirSenhaDTO;
import br.fatec.hobby_hub.dto.UsuarioAtualizacaoDTO;
import br.fatec.hobby_hub.dto.UsuarioCadastroDTO;
import br.fatec.hobby_hub.dto.UsuarioRespostaDTO;
import br.fatec.hobby_hub.infrastructure.entity.StatusUsuario;
import br.fatec.hobby_hub.infrastructure.entity.Usuario;
import br.fatec.hobby_hub.infrastructure.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UsuarioRespostaDTO cadastrar(UsuarioCadastroDTO dto){
        if (repository.existsByEmail(dto.email())){
            throw new RuntimeException("Email já cadastrado");
        }
        if (repository.existsByCpf(dto.cpf())){
            throw new RuntimeException("CPF já cadastrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dto.nome());
        usuario.setSobrenome(dto.sobrenome());
        usuario.setEmail(dto.email());
        usuario.setCpf(dto.cpf());
        usuario.setTelefone(dto.telefone());
        usuario.setSenha(passwordEncoder.encode(dto.senha()));
        usuario.setStatus(StatusUsuario.ATIVO);

        Usuario salvo = repository.save(usuario);
        return new UsuarioRespostaDTO(salvo);
    }

    public List<UsuarioRespostaDTO> listarTodos() {
        return repository.findAll()
                .stream()
                .map(UsuarioRespostaDTO::new)
                .toList();
    }

    public UsuarioRespostaDTO buscarPorId(Long id) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        return new UsuarioRespostaDTO(usuario);
    }

    public UsuarioRespostaDTO alterarDados(Long id, UsuarioAtualizacaoDTO dto) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        usuario.setNome(dto.nome());
        usuario.setTelefone(dto.telefone());
        return new UsuarioRespostaDTO(repository.save(usuario));
    }

    public void excluir(Long id) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        repository.delete(usuario);
    }

    public boolean autenticar(String email, String senha) {
        Usuario usuario = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Credenciais inválidas"));

        return passwordEncoder.matches(senha, usuario.getSenha());
    }

    public void solicitarCodigoRecuperacao(String email) {
        repository.findByEmail(email).ifPresent(usuario -> {

            SecureRandom random = new SecureRandom();
            String codigo = String.valueOf(random.nextInt(900000) + 100000);

            usuario.setCodigoRecuperacao(codigo);
            usuario.setCodigoExpiracao(LocalDateTime.now().plusMinutes(10));
            repository.save(usuario);

            emailService.enviarCodigoRecuperacao(usuario.getEmail(), codigo);
        });
    }

    public void redefinirSenhaComCodigo(RedefinirSenhaDTO dto) {
        Usuario usuario = repository.findByEmail(dto.email())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (usuario.getCodigoRecuperacao() == null || !usuario.getCodigoRecuperacao().equals(dto.codigo())) {
            throw new RuntimeException("Código de recuperação inválido");
        }

        if (usuario.getCodigoExpiracao() == null || usuario.getCodigoExpiracao().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Código expirado. Solicite outro código");
        }

        usuario.setSenha(passwordEncoder.encode(dto.novaSenha()));
        usuario.setCodigoRecuperacao(null);
        usuario.setCodigoExpiracao(null);
        repository.save(usuario);
    }

}