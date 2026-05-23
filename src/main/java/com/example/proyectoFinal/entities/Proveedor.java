package com.example.proyectoFinal.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Proveedor extends Base {

    private String nombre;
    private String direccion;
    private String telefono;
}
