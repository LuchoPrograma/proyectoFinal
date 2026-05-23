package com.example.proyectoFinal.controllers;

import com.example.proyectoFinal.entities.Pelicula;
import com.example.proyectoFinal.services.PeliculaServiceImpl;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/peliculas")
public class PeliculaController extends BaseControllerImpl<Pelicula, PeliculaServiceImpl> {
}