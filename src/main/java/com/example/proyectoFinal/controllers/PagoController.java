package com.example.proyectoFinal.controllers;

import com.example.proyectoFinal.entities.Pago;
import com.example.proyectoFinal.services.PagoServiceImpl;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/pagos")
public class PagoController extends BaseControllerImpl<Pago, PagoServiceImpl> {
}