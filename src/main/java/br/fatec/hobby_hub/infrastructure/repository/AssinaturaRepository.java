package br.fatec.hobby_hub.infrastructure.repository;

import br.fatec.hobby_hub.infrastructure.entity.Assinatura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssinaturaRepository extends JpaRepository<Assinatura, Long> {
    Optional<Assinatura> findByUsuarioId(Long usuarioId);
    List<Assinatura> findByUsuarioIdAndStatusIn(Long usuarioId, List<String> status);
}