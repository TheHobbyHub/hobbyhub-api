package br.fatec.hobby_hub.controller;

import br.fatec.hobby_hub.dto.EsqueciSenhaDTO;
import br.fatec.hobby_hub.dto.LoginDTO;
import br.fatec.hobby_hub.dto.RedefinirSenhaDTO;
import br.fatec.hobby_hub.dto.UsuarioAtualizacaoDTO;
import br.fatec.hobby_hub.dto.UsuarioCadastroDTO;
import br.fatec.hobby_hub.dto.UsuarioRespostaDTO;
import br.fatec.hobby_hub.services.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioService service;

    @PostMapping
    public ResponseEntity<UsuarioRespostaDTO> cadastrar(@RequestBody @Valid UsuarioCadastroDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.cadastrar(dto));
    }

    @GetMapping
    public ResponseEntity<List<UsuarioRespostaDTO>> listar() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioRespostaDTO> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioRespostaDTO> alterarDados(@PathVariable Long id, @RequestBody @Valid UsuarioAtualizacaoDTO dto) {
        return ResponseEntity.ok(service.alterarDados(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody @Valid LoginDTO dto) {
        boolean sucesso = service.autenticar(dto.email(), dto.senha());
        if (sucesso) {

            UsuarioRespostaDTO usuarioLogado = service.listarTodos().stream()
                    .filter(u -> u.email().equalsIgnoreCase(dto.email()))
                    .findFirst()
                    .orElse(null);
            return ResponseEntity.ok(usuarioLogado);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("mensagem", "Credenciais inválidas"));
    }

    @PostMapping("/esqueci-senha")
    public ResponseEntity<Map<String, String>> esqueciSenha(@RequestBody @Valid EsqueciSenhaDTO dto) {
        service.solicitarCodigoRecuperacao(dto.email());
        return ResponseEntity.ok(Map.of("mensagem", "Código de recuperação enviado para o e-mail informado."));
    }

    @PostMapping("/redefinir-senha")
    public ResponseEntity<Map<String, String>> redefinirSenha(@RequestBody @Valid RedefinirSenhaDTO dto) {
        service.redefinirSenhaComCodigo(dto);
        return ResponseEntity.ok(Map.of("mensagem", "Senha alterada com sucesso!"));
    }
}