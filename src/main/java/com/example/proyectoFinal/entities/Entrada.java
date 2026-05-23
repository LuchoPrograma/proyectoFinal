package com.example.proyectoFinal.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

@Entity
@Table(name = "entrada")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Audited
public class Entrada extends Base {

    @Column(name = "precio")
    private double precio;

    @Column(name = "asiento")
    private String asiento;

    @ManyToMany(mappedBy = "entradas")
    private List<Funcion> funciones = new ArrayList<>();
}
