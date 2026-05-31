package com.example.proyectoFinal.entities;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Clase base genérica de la que heredan todas las entidades del sistema.
 *
 * <p>Define el campo {@code id} como clave primaria autogenerada mediante la estrategia
 * {@link GenerationType#IDENTITY}, delegando la generación del valor al motor de base de datos.
 * Al estar anotada con {@link MappedSuperclass}, JPA no crea una tabla propia para esta clase,
 * sino que sus atributos se mapean directamente en las tablas de las subclases.</p>
 *
 * <p>Implementa {@link Serializable} para garantizar la compatibilidad con el contexto de
 * persistencia de JPA y permitir que las instancias puedan ser serializadas cuando sea necesario
 * (por ejemplo, en caché distribuida o en transferencias de sesión).</p>
 *
 * <p>Las anotaciones de Lombok ({@code @Getter}, {@code @Setter}, {@code @NoArgsConstructor},
 * {@code @AllArgsConstructor}) generan en tiempo de compilación los métodos de acceso y
 * los constructores estándar.</p>
 */
@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Base implements Serializable {

    /**
     * Identificador único de la entidad.
     *
     * <p>Actúa como clave primaria en la tabla correspondiente de la base de datos.
     * Su valor es generado automáticamente por el motor de base de datos usando la
     * estrategia {@code IDENTITY} (auto-increment), por lo que no debe asignarse
     * manualmente al crear una nueva instancia.</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
