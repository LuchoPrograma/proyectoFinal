package com.example.proyectoFinal.services;

import com.example.proyectoFinal.dto.RegistrarVentaRequest;
import com.example.proyectoFinal.entities.Cine;
import com.example.proyectoFinal.entities.Cliente;
import com.example.proyectoFinal.entities.Venta;
import com.example.proyectoFinal.repositories.BaseRepository;
import com.example.proyectoFinal.repositories.CineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CineServiceImpl extends BaseServiceImpl<Cine, Long> implements CineService {

    @Autowired
    private CineRepository repository;

    @Autowired
    private VentaService ventaService;

    public CineServiceImpl(BaseRepository<Cine, Long> baseRepository) {
        super(baseRepository);
    }

    /**
     * Delega el registro de venta a VentaService.
     * CineService ya no gestiona repositorios ajenos a su dominio.
     */
    @Override
    public Venta registrarVenta(Long cineId, RegistrarVentaRequest request) throws Exception {
        return ventaService.registrarVenta(cineId, request);
    }

    @Override
    public List<Cliente> obtenerClientesPorCine(Long cineId) throws Exception {
        try {
            return repository.findClientesByCineId(cineId);
        } catch (Exception e) {
            throw new Exception("Error al obtener clientes del cine: " + e.getMessage());
        }
    }
}
