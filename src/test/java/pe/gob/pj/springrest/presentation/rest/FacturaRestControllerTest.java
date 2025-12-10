package pe.gob.pj.springrest.presentation.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest//(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) // Levanta el servidor en un puerto aleatorio
public class FacturaRestControllerTest {
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void buscarTodasRestTest() throws Exception {
        mockMvc.perform(get("/facturas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numero").value(2));
    }

}