package pe.gob.pj.springrest.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "facturas")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Factura {

    @Id
    // Generalmente, el ID no necesita validaciones de formato aquí,
    // ya que Spring y JPA lo gestionan, pero si lo pasas en el cuerpo POST/PUT,
    // podrías añadir un @NotNull si es obligatorio.
    private Integer numero;

    @EqualsAndHashCode.Exclude
    @NotBlank(message = "El concepto no puede estar vacío.") // Valida que no sea null ni cadena vacía ni solo espacios.
    @Size(min = 3, max = 100, message = "El concepto debe tener entre 3 y 100 caracteres.")
    private String concepto;

    @EqualsAndHashCode.Exclude
    @NotNull(message = "El importe es obligatorio.") // Valida que el objeto Double no sea null.
    @DecimalMin(value = "0.01", message = "El importe debe ser mayor que cero.") // Valida que el valor mínimo sea 0.01.
    private Double importe;
}
