package pe.gob.pj.springrest.persistence;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import pe.gob.pj.springrest.domain.model.Factura;
import pe.gob.pj.springrest.infraestructure.persistence.FacturaRepository;

import java.util.List;

@SpringBootTest
@Sql(scripts = {"/schema.sql", "/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD) // Carga el esquema y datos iniciales justo antes de cada test
public class FacturaRepositoryTest {

    @Autowired
    FacturaRepository repository;

    @Test
    void deberiaRecuperarFacturasDesdeLaBaseDeDatos() {
        List<Factura> lista = repository.buscarTodas();

        // Verifica que hay 2 facturas
        assertEquals(2, lista.size(), "La lista de facturas debería tener 2 elementos");

        // Verifica que contiene una factura con número 1
        assertThat(
                "La lista debería contener una factura con número 1",
                lista,
                hasItem(hasProperty("numero", equalTo(1)))
        );

        // Verifica que contiene una factura con número 2 y concepto "Televisor"
        assertThat(
                "Debe existir una factura con número 2 y concepto 'Televisor'",
                lista,
                hasItem(allOf(
                        hasProperty("numero", equalTo(2)),
                        hasProperty("concepto", equalTo("Televisor")),
                        hasProperty("importe", equalTo(50.0))
                ))
        );
    }

}