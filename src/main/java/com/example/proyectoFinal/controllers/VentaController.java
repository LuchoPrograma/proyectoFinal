package com.example.proyectoFinal.controllers;

import com.example.proyectoFinal.entities.Venta;
import com.example.proyectoFinal.services.VentaServiceImpl;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/ventas")
public class VentaController extends BaseControllerImpl<Venta, VentaServiceImpl> {
}