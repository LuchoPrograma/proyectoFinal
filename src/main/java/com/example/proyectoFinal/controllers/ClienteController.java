package com.example.proyectoFinal.controllers;

import com.example.proyectoFinal.entities.Cliente;
import com.example.proyectoFinal.services.ClienteServiceImpl;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/clientes")
public class ClienteController extends BaseControllerImpl<Cliente, ClienteServiceImpl> {
}