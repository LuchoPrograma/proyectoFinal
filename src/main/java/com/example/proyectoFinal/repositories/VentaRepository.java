package com.example.proyectoFinal.repositories;

import com.example.proyectoFinal.entities.Venta;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface VentaRepository extends BaseRepository<Venta, Long> {

    /**
     * Setea fk_cine directamente en la tabla venta sin cargar ni cascadear
     * a través del árbol del Cine. Esto evita el bug de nulls en ventas
     * previas que ocurría cuando el cascade ALL procesaba ventas existentes
     * con colecciones lazy no inicializadas.
     */
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "UPDATE venta SET fk_cine = :cineId WHERE id = :id")
    void assignToCine(@Param("id") Long id, @Param("cineId") Long cineId);

}
