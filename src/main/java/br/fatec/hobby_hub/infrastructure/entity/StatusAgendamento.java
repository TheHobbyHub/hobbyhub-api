package br.fatec.hobby_hub.infrastructure.entity;

public enum StatusAgendamento {
    AGENDADO,
    CANCELADO,
    // Não é gravado no banco: é calculado na resposta quando a aula agendada já terminou
    CONCLUIDO
}
