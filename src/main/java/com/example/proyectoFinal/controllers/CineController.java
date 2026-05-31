package com.example.proyectoFinal.controllers;

import com.example.proyectoFinal.dto.RegistrarVentaRequest;
import com.example.proyectoFinal.entities.Cine;
import com.example.proyectoFinal.services.CineServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la gestión de cines.
 *
 * <p>Extiende el controlador base {@link BaseControllerImpl} con las operaciones CRUD
 * genéricas sobre la entidad {@link Cine}, y agrega endpoints específicos para
 * la gestión de clientes, ventas y empleados asociados a una sucursal de cine.</p>
 *
 * <p>Todos los endpoints de este controlador están expuestos bajo la ruta
 * {@code /api/v1/cines} y aceptan solicitudes de cualquier origen (CORS abierto).</p>
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/cines")
public class CineController extends BaseControllerImpl<Cine, CineServiceImpl> {

    /**
     * Obtiene los clientes registrados en un cine específico.
     *
     * <p>Solo devuelve aquellos clientes que tienen al menos una venta asociada
     * a la sucursal de cine indicada. Clientes sin historial de compras en ese
     * cine no serán incluidos en el resultado.</p>
     *
     * @param id identificador único del cine cuya lista de clientes se desea consultar.
     * @return {@link ResponseEntity} con la lista de clientes del cine en caso de éxito
     *         (HTTP 200), o un mensaje de error en formato JSON (HTTP 400) si ocurre
     *         alguna excepción durante el procesamiento.
     */
    @GetMapping("/{id}/clientes")
    public ResponseEntity<?> obtenerClientesPorCine(@PathVariable Long id) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(servicio.obtenerClientesPorCine(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    /**
     * Registra una nueva venta en un cine específico.
     *
     * <p>Este endpoint fue diseñado de forma dedicada para evitar el error
     * <em>"Multiple representations of the same entity"</em> de Hibernate, que se
     * producía al enviar el árbol completo del objeto {@link Cine} mediante una
     * solicitud PUT. Al usar un DTO especializado ({@link RegistrarVentaRequest}),
     * se desacopla la estructura de la entidad del payload de la petición.</p>
     *
     * @param id      identificador único del cine en el que se registra la venta.
     * @param request objeto con los datos necesarios para crear la venta
     *                (entradas, cliente, función, etc.).
     * @return {@link ResponseEntity} con la venta registrada en caso de éxito
     *         (HTTP 200), o un mensaje de error en formato JSON (HTTP 400) si
     *         ocurre alguna excepción durante el procesamiento.
     */
    @PostMapping("/{id}/ventas")
    public ResponseEntity<?> registrarVenta(@PathVariable Long id,
                                             @RequestBody RegistrarVentaRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(servicio.registrarVenta(id, request));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    /**
     * Agrega un empleado a la sucursal de cine indicada.
     *
     * <p>Implementa una lógica de upsert basada en el DNI del empleado: si ya existe
     * un empleado registrado con el DNI proporcionado en el sistema, se reutiliza ese
     * registro; de lo contrario, se crea un nuevo empleado. En ambos casos, el empleado
     * queda vinculado a la sucursal de cine identificada por {@code id}.</p>
     *
     * @param id      identificador único de la sucursal de cine a la que se vinculará
     *                el empleado.
     * @param request objeto con los datos del empleado a crear o reutilizar
     *                (nombre, DNI, rol, etc.).
     * @return {@link ResponseEntity} con el empleado creado o actualizado en caso de
     *         éxito (HTTP 200), o un mensaje de error en formato JSON (HTTP 400) si
     *         ocurre alguna excepción durante el procesamiento.
     */
    @PostMapping("/{id}/empleados")
    public ResponseEntity<?> agregarEmpleado(@PathVariable Long id,
                                             @RequestBody com.example.proyectoFinal.dto.CrearEmpleadoRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(servicio.agregarEmpleado(id, request));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}