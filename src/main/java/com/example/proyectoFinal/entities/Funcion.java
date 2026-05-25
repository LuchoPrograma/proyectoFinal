package com.example.proyectoFinal.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "funcion")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Audited
public class Funcion extends Base {

    @Column(name = "horario")
    private String horario;

    @ManyToOne
    @JoinColumn(name = "fk_pelicula")
    private Pelicula pelicula;

    @JsonIgnoreProperties("funciones")
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "funcion_entrada",
            joinColumns = @JoinColumn(name = "funcion_id"),
            inverseJoinColumns = @JoinColumn(name = "entrada_id")
    )
    private List<Entrada> entradas = new ArrayList<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "funciones")
    private List<Sala> salas = new ArrayList<>();
}
