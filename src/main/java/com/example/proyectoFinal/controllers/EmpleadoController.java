package com.example.proyectoFinal.controllers;

import com.example.proyectoFinal.entities.Empleado;
import com.example.proyectoFinal.services.EmpleadoServiceImpl;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/empleados")
public class EmpleadoController extends BaseControllerImpl<Empleado, EmpleadoServiceImpl> {
}