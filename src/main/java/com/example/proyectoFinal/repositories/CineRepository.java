package com.example.proyectoFinal.repositories;

import com.example.proyectoFinal.entities.Cine;
import com.example.proyectoFinal.entities.Cliente;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CineRepository extends BaseRepository<Cine, Long> {

    /**
     * Devuelve los clientes distintos que tienen al menos una venta
     * en el cine indicado. Así el selector de clientes solo muestra
     * los clientes propios de la sucursal seleccionada.
     */
    @Query("SELECT DISTINCT v.cliente FROM Cine c JOIN c.ventas v " +
           "WHERE c.id = :cineId AND v.cliente IS NOT NULL")
    List<Cliente> findClientesByCineId(@Param("cineId") Long cineId);
}
