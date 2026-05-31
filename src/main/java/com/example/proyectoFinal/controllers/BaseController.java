package com.example.proyectoFinal.controllers;

import com.example.proyectoFinal.entities.Base;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.Serializable;

/**
 * Interfaz genérica que define el contrato REST para todos los controladores del sistema.
 *
 * <p>Establece los métodos HTTP estándar (GET, POST, PUT, DELETE) que deben implementar
 * los controladores concretos, promoviendo la uniformidad en los endpoints de la API.
 * Está parametrizada por dos tipos:</p>
 * <ul>
 *   <li>{@code E}  — la entidad JPA que maneja el controlador, debe extender {@link Base}.</li>
 *   <li>{@code ID} — el tipo del identificador de la entidad, debe implementar {@link Serializable}.</li>
 * </ul>
 *
 * <p>Todos los métodos retornan {@link ResponseEntity}{@code <?>} para permitir flexibilidad
 * en el tipo de cuerpo de la respuesta HTTP, incluyendo mensajes de error en formato JSON.</p>
 *
 * @param <E>  tipo de la entidad gestionada, debe extender {@link Base}
 * @param <ID> tipo del identificador de la entidad, debe implementar {@link Serializable}
 */
public interface BaseController<E extends Base, ID extends Serializable> {

    /**
     * Retorna todas las entidades disponibles sin aplicar paginación.
     *
     * <p>Corresponde a una petición {@code GET} sobre la ruta base del recurso.</p>
     *
     * @return {@link ResponseEntity} con la lista completa de entidades y estado HTTP 200,
     *         o un mensaje de error con estado HTTP apropiado en caso de fallo.
     */
    ResponseEntity<?> getAll();

    /**
     * Retorna un subconjunto paginado de las entidades disponibles.
     *
     * <p>Corresponde a una petición {@code GET} sobre la ruta paginada del recurso.
     * Los parámetros de paginación (página, tamaño, ordenación) se reciben como
     * parámetros de consulta y Spring los resuelve automáticamente en el objeto
     * {@link Pageable}.</p>
     *
     * @param pageable parámetros de paginación y ordenación.
     * @return {@link ResponseEntity} con la página de entidades y estado HTTP 200,
     *         o un mensaje de error con estado HTTP apropiado en caso de fallo.
     */
    ResponseEntity<?> getAll(Pageable pageable);

    /**
     * Retorna la entidad cuyo identificador coincide con el valor especificado en la URL.
     *
     * <p>Corresponde a una petición {@code GET} sobre la ruta {@code /{id}}.</p>
     *
     * @param id identificador de la entidad a recuperar, extraído de la ruta ({@link PathVariable}).
     * @return {@link ResponseEntity} con la entidad encontrada y estado HTTP 200,
     *         o un mensaje de error con estado HTTP 404 si no existe.
     */
    ResponseEntity<?> getOne(@PathVariable ID id);

    /**
     * Persiste una nueva entidad recibida en el cuerpo de la petición.
     *
     * <p>Corresponde a una petición {@code POST} sobre la ruta base del recurso.</p>
     *
     * @param entity instancia de la entidad a crear, deserializada desde el cuerpo JSON
     *               de la petición ({@link RequestBody}); no debe ser {@code null}.
     * @return {@link ResponseEntity} con la entidad creada (incluyendo su nuevo {@code id})
     *         y estado HTTP 200, o un mensaje de error con estado HTTP apropiado en caso de fallo.
     */
    ResponseEntity<?> save(@RequestBody E entity);

    /**
     * Actualiza los datos de una entidad existente identificada por su {@code id} en la URL.
     *
     * <p>Corresponde a una petición {@code PUT} sobre la ruta {@code /{id}}.</p>
     *
     * @param id     identificador de la entidad a actualizar, extraído de la ruta ({@link PathVariable}).
     * @param entity instancia con los nuevos datos, deserializada desde el cuerpo JSON
     *               de la petición ({@link RequestBody}); no debe ser {@code null}.
     * @return {@link ResponseEntity} con la entidad actualizada y estado HTTP 200,
     *         o un mensaje de error con estado HTTP apropiado en caso de fallo.
     */
    ResponseEntity<?> update(@PathVariable ID id, @RequestBody E entity);

    /**
     * Elimina la entidad identificada por el {@code id} especificado en la URL.
     *
     * <p>Corresponde a una petición {@code DELETE} sobre la ruta {@code /{id}}.</p>
     *
     * @param id identificador de la entidad a eliminar, extraído de la ruta ({@link PathVariable}).
     * @return {@link ResponseEntity} con {@code true} si la entidad fue eliminada y estado
     *         HTTP 204 (No Content), o un mensaje de error con estado HTTP apropiado en caso de fallo.
     */
    ResponseEntity<?> delete(@PathVariable ID id);
}
