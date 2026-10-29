package br.fatec.hobby_hub.controller;

import br.fatec.hobby_hub.dto.AgendamentoRequestDTO;
import br.fatec.hobby_hub.dto.AgendamentoRespostaDTO;
import br.fatec.hobby_hub.dto.ReagendamentoRequestDTO;
import br.fatec.hobby_hub.services.AgendamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/agendamentos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AgendamentoController {

    private final AgendamentoService service;

    @PostMapping
    public ResponseEntity<AgendamentoRespostaDTO> agendar(@RequestBody @Valid AgendamentoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.agendar(dto));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<AgendamentoRespostaDTO>> listarPorUsuario(@PathVariable Long usuarioId,
                                                                         @RequestParam(required = false) String tipo) {
        return ResponseEntity.ok(service.listarPorUsuario(usuarioId, tipo));
    }

    @PutMapping("/{id}/reagendar")
    public ResponseEntity<AgendamentoRespostaDTO> reagendar(@PathVariable Long id, @RequestBody @Valid ReagendamentoRequestDTO dto) {
        return ResponseEntity.ok(service.reagendar(id, dto));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<AgendamentoRespostaDTO> cancelar(@PathVariable Long id, @RequestParam Long usuarioId) {
        return ResponseEntity.ok(service.cancelar(id, usuarioId));
    }
}
