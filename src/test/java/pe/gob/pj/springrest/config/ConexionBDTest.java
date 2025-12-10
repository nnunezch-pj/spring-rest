package pe.gob.pj.springrest.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;

@SpringBootTest
public class ConexionBDTest {
    @Autowired
    private DataSource dataSource;

    @Test
    void testConexionBaseDeDatos() throws Exception {
        // Si la configuración es correcta, se abrirá una conexión sin lanzar excepción
        try (var conn = dataSource.getConnection()) {
            System.out.println("✅ Conectado a la base de datos: " + conn.getMetaData().getURL());
        }
    }
}