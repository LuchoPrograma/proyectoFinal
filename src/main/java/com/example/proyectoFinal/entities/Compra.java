package entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Compra extends Base {

    private Date fecha;
    private List<Insumo> insumos = new ArrayList<>();
    private List<Proveedor> proveedores = new ArrayList<>();
}
