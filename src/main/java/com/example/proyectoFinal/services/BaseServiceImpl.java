package com.example.proyectoFinal.services;

import com.example.proyectoFinal.entities.Base;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.proyectoFinal.repositories.BaseRepository;

import java.io.Serializable;
import java.util.List;

/**
 * Implementación genérica y abstracta de {@link BaseService} que delega las operaciones
 * CRUD al repositorio JPA correspondiente.
 *
 * <p>Esta clase proporciona una implementación reutilizable de las operaciones básicas de
 * persistencia para cualquier entidad del sistema que extienda {@link Base}. Al ser
 * {@code abstract}, no puede instanciarse directamente; cada servicio concreto debe
 * extenderla e inyectar su propio repositorio a través del constructor.</p>
 *
 * <p>Todas las operaciones están anotadas con {@link Transactional}, lo que garantiza
 * que cada método se ejecute dentro de una transacción de base de datos. Si se produce
 * cualquier error, la transacción se revierte automáticamente.</p>
 *
 * @param <E>  tipo de la entidad gestionada, debe extender {@link Base}
 * @param <ID> tipo del identificador de la entidad, debe implementar {@link Serializable}
 */
public abstract class BaseServiceImpl<E extends Base, ID extends Serializable> implements BaseService<E, ID> {

    /**
     * Repositorio JPA utilizado para ejecutar las operaciones de persistencia.
     * Las subclases concretas reciben su repositorio específico a través del constructor
     * y lo almacenan en este campo protegido.
     */
    protected BaseRepository<E, ID> baseRepository;

    /**
     * Construye una nueva instancia del servicio base inyectando el repositorio JPA.
     *
     * <p>Las subclases deben invocar este constructor con {@code super(repository)}
     * para inicializar el repositorio antes de usar cualquier operación de persistencia.</p>
     *
     * @param baseRepository repositorio JPA asociado a la entidad {@code E}; no debe ser {@code null}.
     */
    public BaseServiceImpl(BaseRepository<E, ID> baseRepository) {
        this.baseRepository = baseRepository;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Devuelve una página de entidades según los parámetros de paginación y ordenación
     * especificados en el objeto {@code pageable}. La consulta se ejecuta dentro de una
     * transacción de lectura.</p>
     *
     * @param pageable parámetros de paginación y ordenación.
     * @return página de entidades {@code E}.
     * @throws Exception si ocurre un error durante la consulta paginada.
     */
    @Transactional
    @Override
    public Page<E> findAll(Pageable pageable) throws Exception {
        try {
            return baseRepository.findAll(pageable);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Recupera la totalidad de los registros de la entidad {@code E} sin aplicar
     * paginación. Debe usarse con precaución en tablas con grandes volúmenes de datos.</p>
     *
     * @return lista completa de entidades {@code E}.
     * @throws Exception si ocurre un error durante la consulta.
     */
    @Override
    @Transactional
    public List<E> findAll() throws Exception {
        try {
            return baseRepository.findAll();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Busca la entidad por su identificador. Si no existe ningún registro con el
     * {@code id} proporcionado, lanza una excepción con un mensaje descriptivo.</p>
     *
     * @param id identificador único de la entidad a buscar.
     * @return la entidad encontrada.
     * @throws Exception si no existe registro con ese {@code id}, o si ocurre un error en la consulta.
     */
    @Override
    @Transactional
    public E findById(ID id) throws Exception {
        try {
            return baseRepository.findById(id)
                    .orElseThrow(() -> new Exception("No existe registro con id: " + id));
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Persiste la entidad recibida en la base de datos. Si la entidad no tiene
     * {@code id} asignado, el motor de base de datos genera uno nuevo automáticamente.</p>
     *
     * @param entity entidad a guardar; no debe ser {@code null}.
     * @return la entidad persistida, incluyendo el {@code id} generado.
     * @throws Exception si ocurre un error durante la operación de inserción.
     */
    @Override
    @Transactional
    public E save(E entity) throws Exception {
        try {
            return baseRepository.save(entity);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Primero verifica que la entidad con el {@code id} indicado exista, invocando
     * {@link #findById(Serializable)}. Luego asigna ese mismo {@code id} a la entidad
     * entrante para garantizar que JPA realice una operación de actualización (y no una
     * inserción) al invocar {@code save}.</p>
     *
     * @param id     identificador de la entidad que se desea actualizar.
     * @param entity entidad con los nuevos datos a persistir; no debe ser {@code null}.
     * @return la entidad actualizada.
     * @throws Exception si no existe registro con el {@code id} indicado, o si ocurre
     *                   un error durante la actualización.
     */
    @Override
    @Transactional
    public E update(ID id, E entity) throws Exception {
        try {
            E existing = findById(id);
            entity.setId(existing.getId());
            return baseRepository.save(entity);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Comprueba la existencia del registro antes de intentar eliminarlo. Si no existe,
     * retorna {@code false} sin lanzar excepción. Si existe, lo elimina y retorna
     * {@code true}.</p>
     *
     * @param id identificador de la entidad a eliminar.
     * @return {@code true} si la entidad fue eliminada; {@code false} si no se encontró.
     * @throws Exception si ocurre un error inesperado durante la operación de eliminación.
     */
    @Override
    @Transactional
    public boolean delete(ID id) throws Exception {
        try {
            if (!baseRepository.existsById(id)) {
                return false;
            }
            baseRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
}
