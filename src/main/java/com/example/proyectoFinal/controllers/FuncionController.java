package com.example.proyectoFinal.controllers;

import com.example.proyectoFinal.entities.Funcion;
import com.example.proyectoFinal.services.FuncionServiceImpl;
import com.example.proyectoFinal.dto.ProgramarFuncionRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para la gestión de funciones de cine.
 *
 * <p>Extiende el controlador base {@link BaseControllerImpl} con las operaciones CRUD
 * genéricas sobre la entidad {@link Funcion}, e incorpora el endpoint especializado
 * para programar nuevas funciones aplicando las reglas de negocio correspondientes.</p>
 *
 * <p>Todos los endpoints de este controlador están expuestos bajo la ruta
 * {@code /api/v1/funciones} y aceptan solicitudes de cualquier origen (CORS abierto).</p>
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/funciones")
public class FuncionController extends BaseControllerImpl<Funcion, FuncionServiceImpl> {

    /**
     * Programa una nueva función de cine en una sala y horario determinados.
     *
     * <p>Delega en el servicio la validación y aplicación de las reglas de negocio,
     * que incluyen, entre otras: verificar que no exista otra función en la misma sala
     * y franja horaria, comprobar la disponibilidad de la película y asignar la
     * capacidad de butacas de acuerdo a la sala seleccionada.</p>
     *
     * @param request objeto con los datos necesarios para programar la función
     *                (identificador de sala, identificador de película, fecha,
     *                hora de inicio, precio de entrada, etc.).
     * @return {@link ResponseEntity} con la {@link Funcion} programada en caso de éxito
     *         (HTTP 200), o un mensaje de error en formato JSON (HTTP 400) si no se
     *         cumplen las reglas de negocio o ocurre alguna excepción durante el
     *         procesamiento.
     */
    @PostMapping("/programar")
    public ResponseEntity<?> programarFuncion(@RequestBody ProgramarFuncionRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(servicio.programarFuncion(request));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}