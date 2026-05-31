package com.example.proyectoFinal.services;

import com.example.proyectoFinal.entities.Entrada;
import com.example.proyectoFinal.entities.Funcion;
import com.example.proyectoFinal.repositories.BaseRepository;
import com.example.proyectoFinal.repositories.FuncionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import com.example.proyectoFinal.dto.ProgramarFuncionRequest;
import com.example.proyectoFinal.entities.Pelicula;
import com.example.proyectoFinal.entities.Sala;
import com.example.proyectoFinal.entities.Genero;
import com.example.proyectoFinal.repositories.PeliculaRepository;
import com.example.proyectoFinal.repositories.SalaRepository;
import com.example.proyectoFinal.repositories.CineRepository;
import com.example.proyectoFinal.entities.Cine;

@Service
public class FuncionServiceImpl extends BaseServiceImpl<Funcion, Long> implements FuncionService {

    @Autowired
    private FuncionRepository repository;

    @Autowired
    private SalaRepository salaRepository;

    @Autowired
    private PeliculaRepository peliculaRepository;

    @Autowired
    private CineRepository cineRepository;

    public FuncionServiceImpl(BaseRepository<Funcion, Long> baseRepository) {
        super(baseRepository);
    }

    /**
     * Valida que ninguno de los asientos solicitados esté ya ocupado en la función,
     * crea las nuevas Entradas y las asocia a la función vía funcion_entrada.
     *
     * @param funcionId      ID de la función donde se compran las entradas
     * @param asientos       Lista de códigos de asiento (ej: "A1", "B3")
     * @param precioUnitario Precio por entrada
     * @return La función actualizada con las nuevas entradas incluidas
     */
    @Override
    @Transactional
    public Funcion agregarEntradas(Long funcionId, List<String> asientos, double precioUnitario) throws Exception {
        Funcion funcion = repository.findById(funcionId)
                .orElseThrow(() -> new Exception("No existe la función con id: " + funcionId));

        // Obtener asientos ya ocupados
        List<String> asientosOcupados = new ArrayList<>();
        for (Entrada e : funcion.getEntradas()) {
            asientosOcupados.add(e.getAsiento());
        }

        // Validar conflictos
        for (String asiento : asientos) {
            if (asientosOcupados.contains(asiento)) {
                throw new Exception("El asiento " + asiento + " ya está ocupado en esta función.");
            }
        }

        // Crear y asociar nuevas entradas
        List<Entrada> nuevasEntradas = new ArrayList<>();
        for (String asiento : asientos) {
            Entrada entrada = new Entrada();
            entrada.setPrecio(precioUnitario);
            entrada.setAsiento(asiento);
            nuevasEntradas.add(entrada);
        }

        funcion.getEntradas().addAll(nuevasEntradas);
        return repository.save(funcion);
    }

    @Override
    @Transactional
    public Funcion programarFuncion(ProgramarFuncionRequest request) throws Exception {
        Sala sala = salaRepository.findById(request.getSalaId())
                .orElseThrow(() -> new Exception("No existe la sala con id: " + request.getSalaId()));

        LocalTime nuevoHorario = LocalTime.parse(request.getHorario(), DateTimeFormatter.ofPattern("HH:mm"));

        for (Funcion f : sala.getFunciones()) {
            LocalTime horarioExistente = LocalTime.parse(f.getHorario(), DateTimeFormatter.ofPattern("HH:mm"));
            long diffMinutos = Math.abs(ChronoUnit.MINUTES.between(nuevoHorario, horarioExistente));
            if (diffMinutos < 180) {
                throw new Exception("Solo se pueden agendar funciones cada 3 horas por sala");
            }
        }

        Pelicula pelicula;
        if (request.getPeliculaId() != null) {
            pelicula = peliculaRepository.findById(request.getPeliculaId())
                    .orElseThrow(() -> new Exception("No existe la pelicula con id: " + request.getPeliculaId()));
        } else {
            Cine cine = cineRepository.findById(request.getCineId())
                    .orElseThrow(() -> new Exception("No existe el cine con id: " + request.getCineId()));
                    
            pelicula = new Pelicula();
            pelicula.setTitulo(request.getNuevaPeliculaTitulo());
            if (request.getNuevaPeliculaGenero() != null) {
                pelicula.setGenero(Genero.valueOf(request.getNuevaPeliculaGenero().toUpperCase()));
            }
            pelicula = peliculaRepository.save(pelicula);
            
            cine.getPeliculas().add(pelicula);
            cineRepository.save(cine);
        }

        Funcion funcion = new Funcion();
        funcion.setHorario(request.getHorario());
        funcion.setPelicula(pelicula);

        Funcion funcionGuardada = repository.save(funcion);

        sala.getFunciones().add(funcionGuardada);
        salaRepository.save(sala);

        return funcionGuardada;
    }
}
