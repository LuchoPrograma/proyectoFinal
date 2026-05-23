package com.example.proyectoFinal.services;

import com.example.proyectoFinal.entities.Cine;
import com.example.proyectoFinal.repositories.BaseRepository;
import com.example.proyectoFinal.repositories.CineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CineServiceImpl extends BaseServiceImpl<Cine, Long> implements CineService {

    @Autowired
    private CineRepository repository;

    public CineServiceImpl(BaseRepository<Cine, Long> baseRepository) {
        super(baseRepository);
    }
}
