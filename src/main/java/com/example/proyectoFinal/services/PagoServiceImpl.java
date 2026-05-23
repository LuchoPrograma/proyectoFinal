package com.example.proyectoFinal.services;

import com.example.proyectoFinal.entities.Pago;
import com.example.proyectoFinal.repositories.BaseRepository;
import com.example.proyectoFinal.repositories.PagoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PagoServiceImpl extends BaseServiceImpl<Pago, Long> implements PagoService {

    @Autowired
    private PagoRepository repository;

    public PagoServiceImpl(BaseRepository<Pago, Long> baseRepository) {
        super(baseRepository);
    }
}
