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

/**
 * Implementación de {@link FuncionService} que gestiona las operaciones de negocio
 * relacionadas con la entidad {@link Funcion}.
 * <p>
 * Extiende {@link BaseServiceImpl} para heredar el CRUD genérico y coordina la
 * interacción con los repositorios de {@link Sala}, {@link Pelicula} y {@link Cine}
 * para garantizar la coherencia del modelo de dominio.
 * </p>
 */
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

    /**
     * Construye el servicio inyectando el repositorio base requerido por
     * {@link BaseServiceImpl}.
     *
     * @param baseRepository repositorio genérico para la entidad {@link Funcion}
     */
    public FuncionServiceImpl(BaseRepository<Funcion, Long> baseRepository) {
        super(baseRepository);
    }

    /**
     * Valida que ninguno de los asientos solicitados esté ya ocupado en la función,
     * crea las nuevas {@link Entrada}s y las asocia a la función vía {@code funcion_entrada}.
     *
     * @param funcionId      identificador de la función donde se compran las entradas
     * @param asientos       lista de códigos de asiento a reservar (p. ej. {@code "A1"}, {@code "B3"})
     * @param precioUnitario precio en pesos por cada entrada
     * @return la {@link Funcion} actualizada con las nuevas entradas incluidas
     * @throws Exception si la función no existe, si algún asiento ya está ocupado
     *                   o si ocurre un error durante la persistencia
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

    /**
     * Agenda una nueva función para la sala indicada, garantizando que el horario
     * solicitado respete un intervalo mínimo de 3 horas con respecto a cualquier
     * función ya programada en esa sala.
     * <p>
     * <strong>Validación de horario:</strong> se compara el nuevo horario contra
     * todos los horarios existentes en la sala usando {@link ChronoUnit#MINUTES}.
     * Si la diferencia absoluta con cualquiera de ellos es menor a 180 minutos,
     * la operación se rechaza con una excepción.
     * </p>
     * <p>
     * <strong>Resolución de película:</strong>
     * <ul>
     *   <li>Si {@code request.getPeliculaId()} no es {@code null}, se busca la
     *       película existente en el repositorio y se la asocia a la nueva función.</li>
     *   <li>Si {@code peliculaId} es {@code null}, se crea una nueva {@link Pelicula}
     *       con el título y el género proporcionados en el request, se persiste y se
     *       agrega al catálogo del cine antes de asociarla a la función.</li>
     * </ul>
     * </p>
     * <p>
     * <strong>Guardado en cascada:</strong> la función se persiste primero de forma
     * independiente y luego se la agrega a la colección de funciones de la sala,
     * guardando la sala para actualizar la relación. Esto evita problemas de cascade
     * involuntario sobre otras entidades.
     * </p>
     *
     * @param request datos para programar la función: identificador de sala, horario
     *                (formato {@code "HH:mm"}), identificador del cine, y película
     *                existente ({@code peliculaId}) o datos para crear una nueva
     *                ({@code nuevaPeliculaTitulo} y {@code nuevaPeliculaGenero})
     * @return la {@link Funcion} recién creada y persistida, ya vinculada a la sala
     * @throws Exception si la sala no existe, si el cine no existe (cuando se crea
     *                   película nueva), si la película indicada no existe, si el
     *                   horario no cumple el intervalo mínimo de 3 horas, o si ocurre
     *                   un error durante la persistencia
     */
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
