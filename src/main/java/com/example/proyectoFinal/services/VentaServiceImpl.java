package com.example.proyectoFinal.services;

import com.example.proyectoFinal.dto.RegistrarVentaRequest;
import com.example.proyectoFinal.entities.*;
import com.example.proyectoFinal.repositories.BaseRepository;
import com.example.proyectoFinal.repositories.ClienteRepository;
import com.example.proyectoFinal.repositories.PagoRepository;
import com.example.proyectoFinal.repositories.VentaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;

/**
 * Implementación de {@link VentaService} que gestiona el registro atómico de
 * ventas de entradas de cine.
 * <p>
 * Coordina la interacción entre {@link FuncionService} (para la validación de
 * asientos y creación de entradas), {@link PagoRepository} (persistencia del pago)
 * y {@link VentaRepository} (persistencia de la venta y asignación de sucursal).
 * </p>
 */
@Service
public class VentaServiceImpl extends BaseServiceImpl<Venta, Long> implements VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private FuncionService funcionService;

    /**
     * Construye el servicio inyectando el repositorio base requerido por
     * {@link BaseServiceImpl}.
     *
     * @param baseRepository repositorio genérico para la entidad {@link Venta}
     */
    public VentaServiceImpl(BaseRepository<Venta, Long> baseRepository) {
        super(baseRepository);
    }

    /**
     * Registra una venta de entradas de forma atómica, ejecutando los siguientes pasos:
     * <ol>
     *   <li>Carga y valida que el cliente exista en la base de datos.</li>
     *   <li>Delega a {@link FuncionService} la validación de asientos y la creación
     *       de las {@code Entrada}s, que son persistidas asociadas a la función.</li>
     *   <li>Crea y persiste el {@link Pago} de forma independiente (sin cascade
     *       desde {@code Venta}), calculando el monto total como la cantidad de
     *       asientos multiplicada por el precio unitario.</li>
     *   <li>Crea y persiste la {@link Venta} con referencias directas al cliente,
     *       la función y el pago.</li>
     *   <li>Asigna {@code fk_cine} mediante una query nativa para no disparar
     *       cascades sobre ventas previas asociadas al mismo cine.</li>
     * </ol>
     *
     * @param cineId  identificador del cine (sucursal) donde se realiza la venta
     * @param request datos de la venta: {@code clienteId}, {@code funcionId},
     *                {@code asientos}, {@code precioUnitario}, {@code tipoPago}
     *                y {@code fecha}
     * @return la {@link Venta} persistida con todos sus datos asociados
     * @throws Exception si el cliente no existe, la función no existe, algún asiento
     *                   ya está ocupado, el tipo de pago es inválido, o si ocurre
     *                   cualquier otro error durante la transacción
     */
    @Override
    @Transactional
    public Venta registrarVenta(Long cineId, RegistrarVentaRequest request) throws Exception {
        // 1. Cargar y validar el cliente
        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new Exception("No existe el cliente con id: " + request.getClienteId()));


        // 3. Delegar validación de asientos y creación de entradas a FuncionService
        //    FuncionService persiste las entradas asociadas a la función
        Funcion funcion = funcionService.agregarEntradas(
                request.getFuncionId(),
                request.getAsientos(),
                request.getPrecioUnitario());

        // 4. Crear y persistir el Pago de forma independiente
        Pago pago = new Pago();
        pago.setMonto(request.getAsientos().size() * request.getPrecioUnitario());
        pago.setTipo(TipoPago.valueOf(request.getTipoPago().toUpperCase()));
        pagoRepository.save(pago);

        // 5. Crear y persistir la Venta
        //    Venta referencia directamente la Funcion (sin lista de entradas propia)
        Venta venta = new Venta();
        venta.setFecha(parseFecha(request.getFecha()));
        venta.setCliente(cliente);
        venta.setPago(pago);
        venta.setFuncion(funcion);
        Venta ventaGuardada = ventaRepository.save(venta);

        // 6. Asignar fk_cine vía query nativa para no disparar cascade sobre otras ventas
        ventaRepository.assignToCine(ventaGuardada.getId(), cineId);

        return ventaGuardada;
    }

    /**
     * Convierte una cadena de texto con formato {@code "yyyy-MM-dd"} a un objeto
     * {@link Date} con hora fija a las 12:00 UTC.
     * <p>
     * La hora se fija al mediodía para minimizar problemas de zona horaria al
     * serializar o comparar fechas. Si la cadena no puede parsearse, se devuelve
     * la fecha y hora actuales como valor de reserva.
     * </p>
     *
     * @param fechaStr fecha en formato {@code "yyyy-MM-dd"}; puede ser {@code null}
     *                 o tener un formato incorrecto
     * @return un {@link Date} correspondiente a la fecha indicada a las 12:00 UTC,
     *         o la fecha actual si el parseo falla
     */
    private Date parseFecha(String fechaStr) {
        try {
            LocalDate ld = LocalDate.parse(fechaStr);
            return Date.from(ld.atTime(12, 0).toInstant(ZoneOffset.UTC));
        } catch (Exception e) {
            return new Date();
        }
    }
}
