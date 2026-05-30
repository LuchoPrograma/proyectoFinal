package com.example.proyectoFinal.services;

import com.example.proyectoFinal.dto.RegistrarVentaRequest;
import com.example.proyectoFinal.entities.Cine;
import com.example.proyectoFinal.entities.Cliente;
import com.example.proyectoFinal.entities.Venta;
import java.util.List;

public interface CineService extends BaseService<Cine, Long> {
    Venta registrarVenta(Long cineId, RegistrarVentaRequest request) throws Exception;
    
    /**
     * Obtiene solo los clientes que tienen ventas registradas en el cine específico.
     * Se utiliza para el selector de clientes en el frontend.
     */
    List<Cliente> obtenerClientesPorCine(Long cineId) throws Exception;
}
