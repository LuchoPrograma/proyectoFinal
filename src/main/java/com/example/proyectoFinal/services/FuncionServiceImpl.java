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

@Service
public class FuncionServiceImpl extends BaseServiceImpl<Funcion, Long> implements FuncionService {

    @Autowired
    private FuncionRepository repository;

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
}
