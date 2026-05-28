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
}