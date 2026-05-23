package com.example.proyectoFinal.services;

import com.example.proyectoFinal.entities.Venta;
import com.example.proyectoFinal.repositories.BaseRepository;
import com.example.proyectoFinal.repositories.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VentaServiceImpl extends BaseServiceImpl<Venta, Long> implements VentaService {

    @Autowired
    private VentaRepository repository;

    public VentaServiceImpl(BaseRepository<Venta, Long> baseRepository) {
        super(baseRepository);
    }
}
