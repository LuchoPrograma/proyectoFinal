package com.example.proyectoFinal.services;

import com.example.proyectoFinal.entities.Empleado;
import com.example.proyectoFinal.repositories.BaseRepository;
import com.example.proyectoFinal.repositories.EmpleadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmpleadoServiceImpl extends BaseServiceImpl<Empleado, Long> implements EmpleadoService {

    @Autowired
    private EmpleadoRepository repository;

    public EmpleadoServiceImpl(BaseRepository<Empleado, Long> baseRepository) {
        super(baseRepository);
    }
}
