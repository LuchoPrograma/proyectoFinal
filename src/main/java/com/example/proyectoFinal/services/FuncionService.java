package com.example.proyectoFinal.services;

import com.example.proyectoFinal.entities.Funcion;

import java.util.List;

public interface FuncionService extends BaseService<Funcion, Long> {

    /**
     * Valida que los asientos no estén ocupados y agrega las nuevas entradas
     * a la función indicada. Retorna la función actualizada.
     */
    Funcion agregarEntradas(Long funcionId, List<String> asientos, double precioUnitario) throws Exception;

    /**
     * Agenda una nueva función para una sala, verificando que haya al menos 3 horas de diferencia
     * con las funciones existentes. Si la película es nueva, la crea.
     */
    Funcion programarFuncion(com.example.proyectoFinal.dto.ProgramarFuncionRequest request) throws Exception;
}
