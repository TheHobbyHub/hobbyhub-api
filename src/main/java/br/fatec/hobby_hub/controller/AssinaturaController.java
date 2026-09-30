package br.fatec.hobby_hub.controller;

import br.fatec.hobby_hub.dto.AssinaturaRequestDTO;
import br.fatec.hobby_hub.infrastructure.entity.Plano;
import br.fatec.hobby_hub.services.AssinaturaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/assinaturas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AssinaturaController {

    private final AssinaturaService service;

    @GetMapping("/planos")
    public ResponseEntity<List<Plano>> listarPlanos() {
        return ResponseEntity.ok(service.listarPlanos());
    }

    @PostMapping("/assinar")
    public ResponseEntity<Map<String, String>> assinar(@RequestBody AssinaturaRequestDTO dto) {
        try {
            String resultado = service.realizarAssinatura(dto);
            return ResponseEntity.ok(Map.of("mensagem", resultado));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
}