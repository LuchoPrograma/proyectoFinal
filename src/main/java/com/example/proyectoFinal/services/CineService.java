package com.example.proyectoFinal.services;

import com.example.proyectoFinal.dto.RegistrarVentaRequest;
import com.example.proyectoFinal.entities.Cine;
import com.example.proyectoFinal.entities.Cliente;
import com.example.proyectoFinal.entities.Venta;
import java.util.List;

/**
 * Servicio de dominio para la entidad {@link Cine}.
 * <p>
 * Extiende {@link BaseService} con las operaciones CRUD genéricas y agrega
 * operaciones específicas del negocio como el registro de ventas, la
 * consulta de clientes por sucursal y la incorporación de empleados.
 * </p>
 */
public interface CineService extends BaseService<Cine, Long> {

    /**
     * Registra una nueva venta asociada al cine indicado.
     * <p>
     * Delega la lógica de negocio (validación de asientos, creación de entradas,
     * persistencia del pago y la venta) a {@code VentaService}.
     * </p>
     *
     * @param cineId  identificador del cine donde se realiza la venta
     * @param request datos necesarios para registrar la venta (cliente, función,
     *                asientos, precio y tipo de pago)
     * @return la {@link Venta} persistida con todos sus datos asociados
     * @throws Exception si el cine no existe, el cliente no existe, algún asiento
     *                   ya está ocupado u ocurre cualquier otro error de negocio
     */
    Venta registrarVenta(Long cineId, RegistrarVentaRequest request) throws Exception;

    /**
     * Obtiene solo los clientes que tienen ventas registradas en el cine específico.
     * <p>
     * Se utiliza para poblar el selector de clientes en el frontend, mostrando
     * únicamente los clientes con actividad real en esa sucursal.
     * </p>
     *
     * @param cineId identificador del cine cuyas ventas se consultarán
     * @return lista de {@link Cliente} con al menos una venta en el cine indicado;
     *         puede ser vacía si el cine no tiene ventas registradas
     * @throws Exception si ocurre un error al acceder a la base de datos
     */
    List<Cliente> obtenerClientesPorCine(Long cineId) throws Exception;

    /**
     * Agrega un empleado a la nómina del cine indicado.
     * <p>
     * Si ya existe un empleado con el DNI proporcionado en la base de datos, se
     * reutiliza el registro existente y únicamente se lo vincula a la sucursal
     * si aún no lo estaba. Si el DNI no existe, se crea un nuevo {@code Empleado}
     * antes de vincularlo.
     * </p>
     *
     * @param cineId  identificador del cine (sucursal) al que se agregará el empleado
     * @param request datos del empleado a registrar o vincular (nombre, apellido y DNI)
     * @return el {@link com.example.proyectoFinal.entities.Empleado} persistido o
     *         reutilizado, ya vinculado al cine
     * @throws Exception si el cine no existe o si ocurre un error durante la persistencia
     */
    com.example.proyectoFinal.entities.Empleado agregarEmpleado(Long cineId, com.example.proyectoFinal.dto.CrearEmpleadoRequest request) throws Exception;
}
