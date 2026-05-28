package com.example.proyectoFinal.services;

import com.example.proyectoFinal.dto.RegistrarVentaRequest;
import com.example.proyectoFinal.entities.*;
import com.example.proyectoFinal.repositories.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class CineServiceImpl extends BaseServiceImpl<Cine, Long> implements CineService {

    @Autowired
    private CineRepository repository;

    @Autowired
    private FuncionRepository funcionRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    public CineServiceImpl(BaseRepository<Cine, Long> baseRepository) {
        super(baseRepository);
    }

    /**
     * Registra una venta de forma atómica dentro de una única transacción,
     * cargando todas las entidades desde la base de datos (managed entities).
     * Esto evita el error de Hibernate "Multiple representations of the same entity"
     * que ocurría al enviar el árbol completo del Cine con entidades duplicadas.
     */
    @Override
    @Transactional
    public Venta registrarVenta(Long cineId, RegistrarVentaRequest request) throws Exception {
        // 1. Cargar entidades manejadas (managed) desde la BD
        Cine cine = baseRepository.findById(cineId)
                .orElseThrow(() -> new Exception("No existe el cine con id: " + cineId));

        Funcion funcion = funcionRepository.findById(request.getFuncionId())
                .orElseThrow(() -> new Exception("No existe la función con id: " + request.getFuncionId()));

        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new Exception("No existe el cliente con id: " + request.getClienteId()));

        // 2. Validar que los asientos no estén ya ocupados
        List<String> asientosOcupados = new ArrayList<>();
        for (Entrada e : funcion.getEntradas()) {
            asientosOcupados.add(e.getAsiento());
        }
        for (String asiento : request.getAsientos()) {
            if (asientosOcupados.contains(asiento)) {
                throw new Exception("El asiento " + asiento + " ya está ocupado en esta función.");
            }
        }

        // 3. Crear las Entradas nuevas (sin ID — serán persistidas por cascade)
        List<Entrada> nuevasEntradas = new ArrayList<>();
        for (String asiento : request.getAsientos()) {
            Entrada entrada = new Entrada();
            entrada.setPrecio(request.getPrecioUnitario());
            entrada.setAsiento(asiento);
            nuevasEntradas.add(entrada);
        }

        // 4. Agregar las entradas a la función (actualiza la tabla funcion_entrada)
        //    Usando las MISMAS instancias Java para evitar duplicados en el contexto JPA
        funcion.getEntradas().addAll(nuevasEntradas);
        funcionRepository.save(funcion);

        // 5. Crear el Pago
        Pago pago = new Pago();
        pago.setMonto(request.getAsientos().size() * request.getPrecioUnitario());
        pago.setTipo(TipoPago.valueOf(request.getTipoPago().toUpperCase()));

        // 6. Crear la Venta con las MISMAS instancias de Entrada (ya persistidas arriba)
        Venta venta = new Venta();
        venta.setFecha(parseFecha(request.getFecha()));
        venta.setCliente(cliente);
        venta.setPago(pago);
        venta.setEntradas(new ArrayList<>(nuevasEntradas));

        // 7. Agregar la venta al cine y guardar (cascade persiste Pago y actualiza FKs de Entrada)
        cine.getVentas().add(venta);
        baseRepository.save(cine);

        return venta;
    }

    private Date parseFecha(String fechaStr) {
        try {
            LocalDate ld = LocalDate.parse(fechaStr);
            return Date.from(ld.atTime(12, 0).toInstant(ZoneOffset.UTC));
        } catch (Exception e) {
            return new Date();
        }
    }
}
