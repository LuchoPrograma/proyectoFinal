package com.example.proyectoFinal.services;

import com.example.proyectoFinal.entities.Funcion;

import java.util.List;

public interface FuncionService extends BaseService<Funcion, Long> {

    /**
     * Valida que los asientos no estén ocupados y agrega las nuevas entradas
     * a la función indicada. Retorna la función actualizada.
     */
    Funcion agregarEntradas(Long funcionId, List<String> asientos, double precioUnitario) throws Exception;
}
