package com.example.proyectoFinal.services;

import com.example.proyectoFinal.dto.RegistrarVentaRequest;
import com.example.proyectoFinal.entities.Cine;
import com.example.proyectoFinal.entities.Cliente;
import com.example.proyectoFinal.entities.Venta;
import com.example.proyectoFinal.repositories.BaseRepository;
import com.example.proyectoFinal.repositories.CineRepository;
import com.example.proyectoFinal.repositories.EmpleadoRepository;
import com.example.proyectoFinal.dto.CrearEmpleadoRequest;
import com.example.proyectoFinal.entities.Empleado;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CineServiceImpl extends BaseServiceImpl<Cine, Long> implements CineService {

    @Autowired
    private CineRepository repository;

    @Autowired
    private VentaService ventaService;

    @Autowired
    private EmpleadoRepository empleadoRepository;

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

    @Override
    public Empleado agregarEmpleado(Long cineId, CrearEmpleadoRequest request) throws Exception {
        Cine cine = repository.findById(cineId)
                .orElseThrow(() -> new Exception("No existe el cine con id: " + cineId));

        Empleado empleado;
        java.util.Optional<Empleado> empOpt = empleadoRepository.findByDni(request.getDni());
        if (empOpt.isPresent()) {
            // Empleado ya existe en la BD, lo usamos y lo vinculamos a la sucursal actual si no está vinculado
            empleado = empOpt.get();
        } else {
            // Nuevo empleado
            empleado = new Empleado();
            empleado.setNombre(request.getNombre().trim() + " " + request.getApellido().trim());
            empleado.setDni(request.getDni());
            empleado = empleadoRepository.save(empleado);
        }

        // Check if already in this cine
        boolean alreadyLinked = cine.getEmpleados().stream().anyMatch(e -> e.getDni() == request.getDni());
        if (!alreadyLinked) {
            cine.getEmpleados().add(empleado);
            repository.save(cine);
        }

        return empleado;
    }
}
