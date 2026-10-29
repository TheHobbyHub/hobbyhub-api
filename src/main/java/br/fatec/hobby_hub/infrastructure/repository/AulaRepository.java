package br.fatec.hobby_hub.infrastructure.repository;

import br.fatec.hobby_hub.infrastructure.entity.Aula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AulaRepository extends JpaRepository<Aula, Long> {
}
