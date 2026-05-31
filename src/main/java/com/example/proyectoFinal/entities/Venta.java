package com.example.proyectoFinal.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.util.Date;

@Entity
@Table(name = "venta")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Audited
public class Venta extends Base {

    @Column(name = "fecha")
    private Date fecha;

    @ManyToOne
    @JoinColumn(name = "fk_pago")
    private Pago pago;

    @ManyToOne
    @JoinColumn(name = "fk_cliente")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "fk_funcion")
    private Funcion funcion;
}
