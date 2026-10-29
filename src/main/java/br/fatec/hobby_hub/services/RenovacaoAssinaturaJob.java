package br.fatec.hobby_hub.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RenovacaoAssinaturaJob {

    private final AssinaturaService assinaturaService;

    // Roda ao subir a aplicação (para renovar o que venceu enquanto ela estava desligada) e todo dia à meia-noite
    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "0 0 0 * * *")
    public void renovar() {
        int renovadas = assinaturaService.renovarAssinaturasVencidas();
        if (renovadas > 0) {
            log.info("{} assinatura(s) renovada(s) automaticamente", renovadas);
        }
    }
}
