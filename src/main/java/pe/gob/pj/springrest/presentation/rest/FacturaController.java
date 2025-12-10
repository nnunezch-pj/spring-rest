package pe.gob.pj.springrest.presentation.rest;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.gob.pj.springrest.application.service.FacturaService;
import pe.gob.pj.springrest.domain.model.Factura;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/facturas")
@AllArgsConstructor
public class FacturaController {

    private static final Logger LOG = LoggerFactory.getLogger(FacturaController.class);

    private final FacturaService service;

    // 1. READ ALL (GET /facturas)
    @GetMapping
    public ResponseEntity<List<Factura>> buscarTodas() {
        LOG.info("REST request to get all Facturas");
        List<Factura> facturas = service.buscarTodas();
        return ResponseEntity.ok(facturas);
    }

    // 2. READ BY ID (GET /facturas/{numero})
    @GetMapping("/{numero}")
    public ResponseEntity<Factura> buscarPorNumero(@PathVariable Integer numero) {
        LOG.info("REST request to get Factura : {}", numero);

        Optional<Factura> factura = service.buscarPorId(numero);

        // Si la factura existe, devuelve 200 OK, si no, 404 NOT FOUND
        return factura.map(response -> ResponseEntity.ok().body(response))
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. CREATE (POST /facturas)
    @PostMapping
    public ResponseEntity<Factura> crearFactura(@Valid @RequestBody Factura factura) {
        LOG.info("REST request to save Factura : {}", factura.getNumero());

        // El campo ID se llama 'numero' en tu Entity.
        // Si el cliente envía un número de factura, asumimos que intenta crear una existente
        if (factura.getNumero() != null) {
            // En un sistema real, esto podría requerir un manejo más sofisticado (ej. verificar existencia primero)
            // Devolvemos 400 Bad Request
            return ResponseEntity.badRequest().body(null);
        }

        Factura result = service.guardar(factura);

        // Devuelve 201 Created
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    // 4. UPDATE (PUT /facturas/{numero})
    @PutMapping("/{numero}")
    public ResponseEntity<Factura> actualizarFactura(@PathVariable Integer numero, @Valid @RequestBody Factura facturaDetalles) {
        LOG.info("REST request to update Factura : {}", numero);

        // Aseguramos que el ID del path coincida con el ID del cuerpo
        if (!numero.equals(facturaDetalles.getNumero())) {
            return ResponseEntity.badRequest().body(null); // 400 Bad Request
        }

        Factura result = service.guardar(facturaDetalles);

        // Devuelve 200 OK con la entidad actualizada
        return ResponseEntity.ok(result);
    }

    // 5. DELETE (DELETE /facturas/{numero})
    @DeleteMapping("/{numero}")
    public ResponseEntity<Void> eliminarFactura(@PathVariable Integer numero) {
        LOG.info("REST request to delete Factura : {}", numero);

        service.eliminar(numero);

        // Si la eliminación fue exitosa, devuelve 204 No Content.
        // El @ControllerAdvice maneja la excepción si el 'numero' no existe y devuelve 404.
        return ResponseEntity.noContent().build();
    }
}