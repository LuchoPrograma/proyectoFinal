package com.example.proyectoFinal.services;

import com.example.proyectoFinal.dto.RegistrarVentaRequest;
import com.example.proyectoFinal.entities.Venta;

public interface VentaService extends BaseService<Venta, Long> {

    /**
     * Registra una venta completa dentro de una transacción atómica:
     * valida al cliente, delega la creación de entradas a FuncionService,
     * persiste el Pago y la Venta, y asigna el cine correspondiente.
     */
    Venta registrarVenta(Long cineId, RegistrarVentaRequest request) throws Exception;
}
