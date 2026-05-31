package com.example.proyectoFinal.services;

import com.example.proyectoFinal.entities.Base;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;
import java.util.List;

/**
 * Interfaz genérica del servicio base para las operaciones CRUD del sistema.
 *
 * <p>Define el contrato de negocio que deben cumplir todos los servicios concretos.
 * Está parametrizada por dos tipos:</p>
 * <ul>
 *   <li>{@code E} — la entidad JPA sobre la que opera el servicio, que debe extender {@link Base}.</li>
 *   <li>{@code ID} — el tipo del identificador de la entidad, que debe implementar {@link Serializable}.</li>
 * </ul>
 *
 * <p>Cada método declara {@link Exception} para que las implementaciones puedan propagar
 * tanto errores de persistencia como errores de negocio sin restricciones de tipo.</p>
 *
 * @param <E>  tipo de la entidad gestionada, debe extender {@link Base}
 * @param <ID> tipo del identificador de la entidad, debe implementar {@link Serializable}
 */
public interface BaseService<E extends Base, ID extends Serializable> {

    /**
     * Recupera todas las entidades existentes en la base de datos sin paginación.
     *
     * @return lista con todas las instancias de {@code E}; puede estar vacía si no hay registros.
     * @throws Exception si ocurre un error durante la consulta a la base de datos.
     */
    List<E> findAll() throws Exception;

    /**
     * Recupera un subconjunto paginado de todas las entidades existentes.
     *
     * <p>Útil para evitar cargar grandes volúmenes de datos en memoria de una sola vez.</p>
     *
     * @param pageable objeto que encapsula los parámetros de paginación (número de página,
     *                 tamaño y criterios de ordenación).
     * @return página ({@link Page}) de entidades {@code E} según los parámetros indicados.
     * @throws Exception si ocurre un error durante la consulta paginada.
     */
    Page<E> findAll(Pageable pageable) throws Exception;

    /**
     * Busca y retorna la entidad cuyo identificador coincide con el valor proporcionado.
     *
     * @param id identificador único de la entidad a buscar.
     * @return la entidad {@code E} correspondiente al {@code id} indicado.
     * @throws Exception si no existe ningún registro con ese identificador, o si ocurre
     *                   un error en la consulta.
     */
    E findById(ID id) throws Exception;

    /**
     * Persiste una nueva entidad en la base de datos.
     *
     * <p>Si la entidad ya posee un {@code id} asignado, el comportamiento depende
     * del repositorio subyacente (puede generar un conflicto de clave primaria).</p>
     *
     * @param entity instancia de {@code E} a guardar; no debe ser {@code null}.
     * @return la entidad guardada, con el {@code id} generado por la base de datos.
     * @throws Exception si ocurre un error durante la operación de inserción.
     */
    E save(E entity) throws Exception;

    /**
     * Actualiza los datos de una entidad existente identificada por su {@code id}.
     *
     * <p>La implementación típica recupera la entidad original para validar su existencia,
     * asigna el mismo {@code id} a la entidad entrante y la persiste sobreescribiendo
     * los valores anteriores.</p>
     *
     * @param id     identificador de la entidad a actualizar.
     * @param entity instancia de {@code E} con los nuevos valores; no debe ser {@code null}.
     * @return la entidad actualizada y persistida.
     * @throws Exception si no existe registro con el {@code id} indicado, o si ocurre
     *                   un error durante la actualización.
     */
    E update(ID id, E entity) throws Exception;

    /**
     * Elimina la entidad identificada por el {@code id} proporcionado.
     *
     * @param id identificador de la entidad a eliminar.
     * @return {@code true} si la entidad existía y fue eliminada correctamente;
     *         {@code false} si no se encontró ningún registro con ese identificador.
     * @throws Exception si ocurre un error durante la operación de eliminación.
     */
    boolean delete(ID id) throws Exception;
}
