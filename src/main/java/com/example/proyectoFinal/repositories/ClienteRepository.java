package com.example.proyectoFinal.repositories;

import com.example.proyectoFinal.entities.Cliente;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends BaseRepository<Cliente, Long> {
}
