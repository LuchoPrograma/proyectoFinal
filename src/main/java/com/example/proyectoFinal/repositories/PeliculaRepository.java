package com.example.proyectoFinal.repositories;

import com.example.proyectoFinal.entities.Pelicula;
import org.springframework.stereotype.Repository;

@Repository
public interface PeliculaRepository extends BaseRepository<Pelicula, Long> {
}
