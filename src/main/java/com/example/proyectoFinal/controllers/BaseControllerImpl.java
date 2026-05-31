package com.example.proyectoFinal.controllers;

import com.example.proyectoFinal.entities.Base;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.proyectoFinal.services.BaseServiceImpl;

/**
 * Implementación genérica y abstracta de {@link BaseController} que proporciona
 * los endpoints REST estándar para cualquier entidad del sistema.
 *
 * <p>Centraliza la lógica común de manejo HTTP (códigos de estado, captura de excepciones
 * y formato de errores) evitando la duplicación de código en los controladores concretos.
 * Al ser {@code abstract}, no puede instanciarse directamente; cada controlador concreto
 * debe extenderla especificando la entidad {@code E} y el servicio {@code S} correspondientes.</p>
 *
 * <p>El servicio concreto se inyecta automáticamente por Spring mediante {@link Autowired}
 * usando el subtipo {@code S}, lo que permite que cada controlador hijo reciba el servicio
 * específico de su entidad sin necesidad de redefinir la inyección.</p>
 *
 * <p>Está parametrizada por dos tipos:</p>
 * <ul>
 *   <li>{@code E} — la entidad JPA gestionada, debe extender {@link Base}.</li>
 *   <li>{@code S} — el servicio concreto, debe extender {@link BaseServiceImpl}{@code <E, Long>}.</li>
 * </ul>
 *
 * @param <E> tipo de la entidad gestionada, debe extender {@link Base}
 * @param <S> tipo del servicio concreto, debe extender {@link BaseServiceImpl}
 */
public abstract class BaseControllerImpl<E extends Base, S extends BaseServiceImpl<E, Long>>
        implements BaseController<E, Long> {

    /**
     * Servicio concreto inyectado por Spring que implementa la lógica de negocio
     * y persistencia para la entidad {@code E}.
     *
     * <p>La supresión de advertencias {@code "SpringJavaInjectionPointsAutowiringInspection"}
     * es necesaria porque el IDE no puede resolver el tipo genérico {@code S} en tiempo
     * de análisis estático, aunque Spring sí lo resuelve correctamente en tiempo de ejecución.</p>
     */
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    protected S servicio;

    /**
     * Endpoint {@code GET /} — retorna todas las entidades sin paginación.
     *
     * <p>Delega la consulta al método {@link BaseServiceImpl#findAll()} del servicio inyectado.
     * En caso de éxito retorna HTTP 200 con la lista completa; ante cualquier error retorna
     * HTTP 404 con un mensaje de error en formato JSON.</p>
     *
     * @return {@link ResponseEntity} con la lista de entidades (HTTP 200) o mensaje de error (HTTP 404).
     */
    @GetMapping("")
    public ResponseEntity<?> getAll() {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(servicio.findAll());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\":\"Error, por favor intente mas tarde.\"}");
        }
    }

    /**
     * Endpoint {@code GET /paged} — retorna un subconjunto paginado de las entidades.
     *
     * <p>Los parámetros de paginación ({@code page}, {@code size}, {@code sort}) se envían
     * como parámetros de consulta en la URL y Spring los resuelve automáticamente en el
     * objeto {@link Pageable}. En caso de éxito retorna HTTP 200; ante cualquier error
     * retorna HTTP 404 con un mensaje de error en formato JSON.</p>
     *
     * @param pageable parámetros de paginación y ordenación resueltos por Spring MVC.
     * @return {@link ResponseEntity} con la página de entidades (HTTP 200) o mensaje de error (HTTP 404).
     */
    @GetMapping("/paged")
    public ResponseEntity<?> getAll(Pageable pageable) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(servicio.findAll(pageable));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\":\"Error, por favor intente mas tarde.\"}");
        }
    }

    /**
     * Endpoint {@code GET /{id}} — retorna la entidad cuyo identificador coincide con el de la URL.
     *
     * <p>Delega la búsqueda al método {@link BaseServiceImpl#findById(Serializable)}. En caso
     * de éxito retorna HTTP 200 con la entidad; si no se encuentra o hay un error retorna
     * HTTP 404 con un mensaje de error en formato JSON.</p>
     *
     * @param id identificador de la entidad a recuperar, extraído de la variable de ruta.
     * @return {@link ResponseEntity} con la entidad (HTTP 200) o mensaje de error (HTTP 404).
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable Long id) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(servicio.findById(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\":\"Error, por favor intente mas tarde.\"}");
        }
    }

    /**
     * Endpoint {@code POST /} — crea y persiste una nueva entidad.
     *
     * <p>Deserializa la entidad desde el cuerpo JSON de la petición y la pasa al método
     * {@link BaseServiceImpl#save(Base)} del servicio. En caso de éxito retorna HTTP 200
     * con la entidad guardada (incluyendo su {@code id} generado); ante cualquier error
     * retorna HTTP 400 con un mensaje de error en formato JSON.</p>
     *
     * @param entity entidad a crear, deserializada del cuerpo JSON de la petición.
     * @return {@link ResponseEntity} con la entidad creada (HTTP 200) o mensaje de error (HTTP 400).
     */
    @PostMapping("")
    public ResponseEntity<?> save(@RequestBody E entity) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(servicio.save(entity));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"Error, por favor intente mas tarde.\"}");
        }
    }

    /**
     * Endpoint {@code PUT /{id}} — actualiza los datos de una entidad existente.
     *
     * <p>Combina el {@code id} de la variable de ruta con los nuevos datos del cuerpo JSON
     * y los pasa al método {@link BaseServiceImpl#update(Serializable, Base)} del servicio.
     * En caso de éxito retorna HTTP 200 con la entidad actualizada; ante cualquier error
     * retorna HTTP 400 con un mensaje de error en formato JSON.</p>
     *
     * @param id     identificador de la entidad a actualizar, extraído de la variable de ruta.
     * @param entity entidad con los nuevos valores, deserializada del cuerpo JSON de la petición.
     * @return {@link ResponseEntity} con la entidad actualizada (HTTP 200) o mensaje de error (HTTP 400).
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody E entity) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(servicio.update(id, entity));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"Error, por favor intente mas tarde.\"}");
        }
    }

    /**
     * Endpoint {@code DELETE /{id}} — elimina la entidad cuyo identificador coincide con el de la URL.
     *
     * <p>Delega la eliminación al método {@link BaseServiceImpl#delete(Serializable)} del servicio.
     * En caso de éxito retorna HTTP 204 (No Content) con el resultado booleano de la operación;
     * ante cualquier error retorna HTTP 400 con un mensaje de error en formato JSON.</p>
     *
     * @param id identificador de la entidad a eliminar, extraído de la variable de ruta.
     * @return {@link ResponseEntity} con {@code true}/{@code false} (HTTP 204) o mensaje de error (HTTP 400).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(servicio.delete(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"Error, por favor intente mas tarde.\"}");
        }
    }
}
