package com.example.proyectoFinal.controllers;

import com.example.proyectoFinal.dto.RegistrarVentaRequest;
import com.example.proyectoFinal.entities.Cine;
import com.example.proyectoFinal.services.CineServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/cines")
public class CineController extends BaseControllerImpl<Cine, CineServiceImpl> {

    /**
     * Endpoint para obtener los clientes registrados en un cine específico.
     * Solo devuelve los clientes que tienen ventas en ese cine.
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
     * Endpoint dedicado para registrar una venta en un cine.
     * Evita el error "Multiple representations of the same entity" de Hibernate
     * que ocurría al enviar el árbol completo del Cine via PUT.
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