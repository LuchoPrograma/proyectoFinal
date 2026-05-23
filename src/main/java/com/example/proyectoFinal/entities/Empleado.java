package entities;

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
@Table(name = "empleado")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Audited
public class Empleado extends Base {

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "dni")
    private int dni;

    @ManyToMany(mappedBy = "empleados")
    private List<Cine> cines = new ArrayList<>();
}
