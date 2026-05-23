package entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

@Entity
@Table(name = "pago")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Audited
public class Pago extends Base {

    @Column(name = "monto")
    private double monto;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo")
    private TipoPago tipo;
}
