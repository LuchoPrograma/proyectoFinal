package com.example.proyectoFinal.repositories;

import com.example.proyectoFinal.entities.Empleado;
import org.springframework.stereotype.Repository;

@Repository
public interface EmpleadoRepository extends BaseRepository<Empleado, Long> {
}
