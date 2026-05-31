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

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/funciones")
public class FuncionController extends BaseControllerImpl<Funcion, FuncionServiceImpl> {

    @PostMapping("/programar")
    public ResponseEntity<?> programarFuncion(@RequestBody ProgramarFuncionRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(servicio.programarFuncion(request));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}