package entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cliente")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Audited
public class Cliente extends Base {

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "email")
    private String email;

    @OneToMany(mappedBy = "cliente")
    private List<Venta> ventas = new ArrayList<>();
}
