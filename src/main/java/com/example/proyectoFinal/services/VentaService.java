package com.example.proyectoFinal.services;

import com.example.proyectoFinal.dto.RegistrarVentaRequest;
import com.example.proyectoFinal.entities.Venta;

/**
 * Servicio de dominio para la entidad {@link Venta}.
 * <p>
 * Extiende {@link BaseService} con las operaciones CRUD genéricas y agrega
 * la operación principal del negocio: el registro atómico de una venta completa,
 * coordinando la validación de asientos, la creación del pago y la asignación
 * de la sucursal correspondiente.
 * </p>
 */
public interface VentaService extends BaseService<Venta, Long> {

    /**
     * Registra una venta completa dentro de una transacción atómica.
     * <p>
     * El proceso incluye los siguientes pasos:
     * <ol>
     *   <li>Validar que el cliente exista en la base de datos.</li>
     *   <li>Delegar a {@code FuncionService} la validación de que los asientos no
     *       estén ocupados y la creación de las {@code Entrada}s correspondientes.</li>
     *   <li>Crear y persistir el {@code Pago} de forma independiente (sin cascade
     *       desde {@code Venta}).</li>
     *   <li>Crear y persistir la {@code Venta} con referencias directas al cliente,
     *       la función y el pago.</li>
     *   <li>Asignar la clave foránea {@code fk_cine} mediante una query nativa para
     *       evitar disparar cascades sobre ventas previas del mismo cine.</li>
     * </ol>
     * </p>
     *
     * @param cineId  identificador del cine (sucursal) donde se realiza la venta
     * @param request datos de la venta: cliente, función, asientos, precio unitario,
     *                tipo de pago y fecha
     * @return la {@link Venta} persistida con todos sus datos asociados
     * @throws Exception si el cliente o la función no existen, si algún asiento ya
     *                   está ocupado, si el tipo de pago es inválido, o si ocurre
     *                   cualquier otro error durante la transacción
     */
    Venta registrarVenta(Long cineId, RegistrarVentaRequest request) throws Exception;
}
