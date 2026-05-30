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

    /**
     * ManyToOne: varias ventas pueden referenciar el mismo Pago.
     * El Pago debe persistirse antes de la Venta (sin cascade aquí).
     */
    @ManyToOne
    @JoinColumn(name = "fk_pago")
    private Pago pago;

    /**
     * ManyToOne: una venta pertenece a un único cliente.
     * Cardinalidad: 1..* Ventas → 1 Cliente.
     */
    @ManyToOne
    @JoinColumn(name = "fk_cliente")
    private Cliente cliente;

    /**
     * ManyToOne: una venta está asociada a una única función.
     * Relación directa según UML — las entradas se rastrean
     * indirectamente a través de la función (funcion → funcion_entrada).
     */
    @ManyToOne
    @JoinColumn(name = "fk_funcion")
    private Funcion funcion;
}
