package ar.edu.utn.frc.dsi.bolsines.repositorio;

import ar.edu.utn.frc.dsi.bolsines.entidad.Bolsin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BolsinRepository extends JpaRepository<Bolsin, Long> {
}
