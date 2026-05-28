package com.example.proyectoFinal.services;

import com.example.proyectoFinal.dto.RegistrarVentaRequest;
import com.example.proyectoFinal.entities.Cine;
import com.example.proyectoFinal.entities.Venta;

public interface CineService extends BaseService<Cine, Long> {
    Venta registrarVenta(Long cineId, RegistrarVentaRequest request) throws Exception;
}
