package com.example.proyectoFinal.services;

import com.example.proyectoFinal.entities.Cliente;
import com.example.proyectoFinal.repositories.BaseRepository;
import com.example.proyectoFinal.repositories.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClienteServiceImpl extends BaseServiceImpl<Cliente, Long> implements ClienteService {

    @Autowired
    private ClienteRepository repository;

    public ClienteServiceImpl(BaseRepository<Cliente, Long> baseRepository) {
        super(baseRepository);
    }
}
