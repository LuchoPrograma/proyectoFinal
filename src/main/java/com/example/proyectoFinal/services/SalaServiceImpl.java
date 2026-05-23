package com.example.proyectoFinal.services;

import com.example.proyectoFinal.entities.Sala;
import com.example.proyectoFinal.repositories.BaseRepository;
import com.example.proyectoFinal.repositories.SalaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SalaServiceImpl extends BaseServiceImpl<Sala, Long> implements SalaService {

    @Autowired
    private SalaRepository repository;

    public SalaServiceImpl(BaseRepository<Sala, Long> baseRepository) {
        super(baseRepository);
    }
}
