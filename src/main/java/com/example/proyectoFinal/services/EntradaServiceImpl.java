package com.example.proyectoFinal.services;

import com.example.proyectoFinal.entities.Entrada;
import com.example.proyectoFinal.repositories.BaseRepository;
import com.example.proyectoFinal.repositories.EntradaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EntradaServiceImpl extends BaseServiceImpl<Entrada, Long> implements EntradaService {

    @Autowired
    private EntradaRepository repository;

    public EntradaServiceImpl(BaseRepository<Entrada, Long> baseRepository) {
        super(baseRepository);
    }
}
