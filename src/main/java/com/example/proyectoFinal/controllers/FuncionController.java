package com.example.proyectoFinal.controllers;

import com.example.proyectoFinal.entities.Funcion;
import com.example.proyectoFinal.services.FuncionServiceImpl;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/funciones")
public class FuncionController extends BaseControllerImpl<Funcion, FuncionServiceImpl> {
}