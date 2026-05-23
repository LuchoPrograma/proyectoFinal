package com.example.proyectoFinal.controllers;

import com.example.proyectoFinal.entities.Cine;
import com.example.proyectoFinal.services.CineServiceImpl;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/cines")
public class CineController extends BaseControllerImpl<Cine, CineServiceImpl> {
}