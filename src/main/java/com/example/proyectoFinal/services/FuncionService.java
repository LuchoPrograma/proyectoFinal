package com.example.proyectoFinal.services;

import com.example.proyectoFinal.entities.Funcion;

import java.util.List;

/**
 * Servicio de dominio para la entidad {@link Funcion}.
 * <p>
 * Extiende {@link BaseService} con las operaciones CRUD genéricas y agrega
 * operaciones específicas del negocio como la reserva de asientos y la
 * programación de nuevas funciones en una sala.
 * </p>
 */
public interface FuncionService extends BaseService<Funcion, Long> {

    /**
     * Valida que los asientos solicitados no estén ocupados y agrega las nuevas
     * entradas a la función indicada.
     * <p>
     * Por cada asiento de la lista se crea una {@code Entrada} con el precio
     * unitario especificado. Si alguno de los asientos ya figura como ocupado
     * en la función, la operación completa es rechazada con una excepción.
     * Retorna la función actualizada con todas sus entradas.
     * </p>
     *
     * @param funcionId      identificador de la función donde se compran las entradas
     * @param asientos       lista de códigos de asiento a reservar (p. ej. {@code "A1"}, {@code "B3"})
     * @param precioUnitario precio en pesos por cada entrada
     * @return la {@link Funcion} actualizada con las nuevas entradas asociadas
     * @throws Exception si la función no existe, si algún asiento ya está ocupado
     *                   o si ocurre un error durante la persistencia
     */
    Funcion agregarEntradas(Long funcionId, List<String> asientos, double precioUnitario) throws Exception;

    /**
     * Agenda una nueva función para una sala, verificando que exista al menos
     * 3 horas de diferencia con cada función ya programada en esa sala.
     * <p>
     * Si se proporciona un {@code peliculaId} existente, la función se asocia a
     * esa película. De lo contrario, se crea una nueva {@code Pelicula} con el
     * título y género indicados, y se la vincula al catálogo del cine antes de
     * asociarla a la función.
     * </p>
     *
     * @param request datos necesarios para programar la función: sala, horario,
     *                cine, y película existente o datos para crear una nueva
     * @return la {@link Funcion} recién creada y persistida, ya vinculada a la sala
     * @throws Exception si la sala o el cine no existen, si la película indicada
     *                   no existe, si no se respeta el intervalo mínimo de 3 horas
     *                   entre funciones, o si ocurre un error durante la persistencia
     */
    Funcion programarFuncion(com.example.proyectoFinal.dto.ProgramarFuncionRequest request) throws Exception;
}
