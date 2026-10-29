package br.fatec.hobby_hub.controller;

import br.fatec.hobby_hub.dto.AulaRespostaDTO;
import br.fatec.hobby_hub.dto.AulaResumoDTO;
import br.fatec.hobby_hub.dto.CategoriaDTO;
import br.fatec.hobby_hub.dto.HorarioAulaRespostaDTO;
import br.fatec.hobby_hub.services.AulaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/aulas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AulaController {

    private final AulaService service;

    @GetMapping
    public ResponseEntity<List<AulaResumoDTO>> listar(@RequestParam(required = false) Long usuarioId,
                                                      @RequestParam(required = false) Long categoriaId,
                                                      @RequestParam(required = false) String busca,
                                                      @RequestParam(required = false) String cidade,
                                                      @RequestParam(required = false) Boolean inclusoNoPlano,
                                                      @RequestParam(required = false) Boolean destaque) {
        return ResponseEntity.ok(service.listar(usuarioId, categoriaId, busca, cidade, inclusoNoPlano, destaque));
    }

    @GetMapping("/categorias")
    public ResponseEntity<List<CategoriaDTO>> listarCategorias() {
        return ResponseEntity.ok(service.listarCategorias());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AulaRespostaDTO> buscar(@PathVariable Long id, @RequestParam(required = false) Long usuarioId) {
        return ResponseEntity.ok(service.buscarPorId(id, usuarioId));
    }

    @GetMapping("/{id}/horarios")
    public ResponseEntity<List<HorarioAulaRespostaDTO>> listarHorarios(@PathVariable Long id) {
        return ResponseEntity.ok(service.listarHorarios(id));
    }
}
