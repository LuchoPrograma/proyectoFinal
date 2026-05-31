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

/**
 * Implementación de {@link CineService} que gestiona las operaciones de negocio
 * relacionadas con la entidad {@link Cine}.
 * <p>
 * Extiende {@link BaseServiceImpl} para heredar el CRUD genérico y delega las
 * operaciones de venta a {@link VentaService}, manteniendo así la separación de
 * responsabilidades entre dominios.
 * </p>
 */
@Service
public class CineServiceImpl extends BaseServiceImpl<Cine, Long> implements CineService {

    @Autowired
    private CineRepository repository;

    @Autowired
    private VentaService ventaService;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    /**
     * Construye el servicio inyectando el repositorio base requerido por
     * {@link BaseServiceImpl}.
     *
     * @param baseRepository repositorio genérico para la entidad {@link Cine}
     */
    public CineServiceImpl(BaseRepository<Cine, Long> baseRepository) {
        super(baseRepository);
    }

    /**
     * Delega el registro de venta a {@link VentaService}.
     * <p>
     * {@code CineService} no gestiona repositorios ajenos a su dominio; toda la
     * lógica transaccional de ventas (validación de asientos, creación de entradas
     * y persistencia del pago) es responsabilidad exclusiva de {@code VentaService}.
     * </p>
     *
     * @param cineId  identificador del cine donde se efectúa la venta
     * @param request datos de la venta a registrar
     * @return la {@link Venta} persistida
     * @throws Exception si se produce cualquier error durante el registro de la venta
     */
    @Override
    public Venta registrarVenta(Long cineId, RegistrarVentaRequest request) throws Exception {
        return ventaService.registrarVenta(cineId, request);
    }

    /**
     * Retorna los clientes que tienen al menos una venta registrada en el cine indicado.
     * <p>
     * Invoca al repositorio para ejecutar la consulta filtrada por {@code cineId},
     * envolviendo cualquier excepción en un mensaje descriptivo.
     * </p>
     *
     * @param cineId identificador del cine a consultar
     * @return lista de {@link Cliente} con ventas en ese cine; puede estar vacía
     * @throws Exception si ocurre un error al acceder a la base de datos
     */
    @Override
    public List<Cliente> obtenerClientesPorCine(Long cineId) throws Exception {
        try {
            return repository.findClientesByCineId(cineId);
        } catch (Exception e) {
            throw new Exception("Error al obtener clientes del cine: " + e.getMessage());
        }
    }

    /**
     * Agrega un empleado a la sucursal indicada, reutilizando el registro existente
     * si el DNI ya figura en la base de datos.
     * <p>
     * La lógica sigue dos caminos:
     * <ol>
     *   <li><strong>DNI existente:</strong> se recupera el {@link Empleado} ya
     *       persistido y se lo vincula a la sucursal sin duplicar el registro.</li>
     *   <li><strong>DNI nuevo:</strong> se crea un nuevo {@link Empleado} con nombre,
     *       apellido y DNI tomados del request y se persiste antes de vincularlo.</li>
     * </ol>
     * En ambos casos se verifica que el empleado no esté ya vinculado al cine para
     * evitar duplicados en la relación muchos-a-muchos.
     * </p>
     *
     * @param cineId  identificador del cine al que se vinculará el empleado
     * @param request datos del empleado: nombre, apellido y DNI
     * @return el {@link Empleado} vinculado (nuevo o reutilizado)
     * @throws Exception si el cine no existe o si ocurre un error durante la persistencia
     */
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

        // Verificar si el empleado ya está vinculado a este cine para evitar duplicados
        boolean alreadyLinked = cine.getEmpleados().stream().anyMatch(e -> e.getDni() == request.getDni());
        if (!alreadyLinked) {
            cine.getEmpleados().add(empleado);
            repository.save(cine);
        }

        return empleado;
    }
}
