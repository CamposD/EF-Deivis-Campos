package cibertec.com.pe.repository;

import cibertec.com.pe.model.Ciudad;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ICiudadRepository extends JpaRepository<Ciudad, String> {

}
