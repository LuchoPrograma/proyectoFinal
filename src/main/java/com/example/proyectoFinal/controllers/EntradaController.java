package com.example.proyectoFinal.controllers;

import com.example.proyectoFinal.entities.Entrada;
import com.example.proyectoFinal.services.EntradaServiceImpl;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/entradas")
public class EntradaController extends BaseControllerImpl<Entrada, EntradaServiceImpl> {
}