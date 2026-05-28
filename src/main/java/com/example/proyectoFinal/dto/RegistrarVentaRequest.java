package com.example.proyectoFinal.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RegistrarVentaRequest {
    private Long clienteId;
    private Long funcionId;
    private List<String> asientos;
    private double precioUnitario;
    private String tipoPago;   // "TARJETA" | "EFECTIVO"
    private String fecha;       // formato: "2026-05-28"
}
