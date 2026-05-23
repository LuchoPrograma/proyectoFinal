package com.example.proyectoFinal.controllers;

import com.example.proyectoFinal.entities.Sala;
import com.example.proyectoFinal.services.SalaServiceImpl;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/salas")
public class SalaController extends BaseControllerImpl<Sala, SalaServiceImpl> {
}