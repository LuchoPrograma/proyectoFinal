package com.example.proyectoFinal.services;

import com.example.proyectoFinal.entities.Funcion;
import com.example.proyectoFinal.repositories.BaseRepository;
import com.example.proyectoFinal.repositories.FuncionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FuncionServiceImpl extends BaseServiceImpl<Funcion, Long> implements FuncionService {

    @Autowired
    private FuncionRepository repository;

    public FuncionServiceImpl(BaseRepository<Funcion, Long> baseRepository) {
        super(baseRepository);
    }
}
