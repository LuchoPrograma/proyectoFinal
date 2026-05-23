package com.example.proyectoFinal.services;

import com.example.proyectoFinal.entities.Pelicula;
import com.example.proyectoFinal.repositories.BaseRepository;
import com.example.proyectoFinal.repositories.PeliculaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PeliculaServiceImpl extends BaseServiceImpl<Pelicula, Long> implements PeliculaService {

    @Autowired
    private PeliculaRepository repository;

    public PeliculaServiceImpl(BaseRepository<Pelicula, Long> baseRepository) {
        super(baseRepository);
    }
}
