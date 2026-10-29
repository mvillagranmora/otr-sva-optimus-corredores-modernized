package cl.bcs.normativa.svc.adapter;

import cl.bcs.normativa.svc.domain.cartola.*;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests para CartolaNormativaController.
 *
 * <p>Usa H2 in-memory. MockMvc construido manualmente (NO @AutoConfigureMockMvc).
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CartolaNormativaControllerTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;
    private final Gson gson = new Gson();

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    @DisplayName("POST /calcular-rentabilidad retorna 200 con lista de rentabilidades")
    void calcularRentabilidad_retornaOk() throws Exception {
        SaldoCuentaCliente caja = new SaldoCuentaCliente();
        caja.idCliente = 1L;
        caja.totalAbonosCajaMTD = new BigDecimal("10000");
        caja.abonosCajaMTDRentabilidad = new BigDecimal("10000");

        SaldoCuentaCliente titulos = new SaldoCuentaCliente();
        titulos.idCliente = 1L;

        SaldoFinalCliente saldo = new SaldoFinalCliente();
        saldo.idCliente = 1L;
        saldo.totalPatrimonioFechaConsulta = new BigDecimal("120000");
        saldo.totalMTDSaldoFinal           = new BigDecimal("100000");

        CalcularRentabilidadEntrada entrada = new CalcularRentabilidadEntrada();
        entrada.idEmpresa        = 1L;
        entrada.esPauta          = false;
        entrada.movimientosCaja  = List.of(caja);
        entrada.movimientosTitulo = List.of(titulos);
        entrada.saldoFinal       = List.of(saldo);
        entrada.listaClientes    = List.of(new ClienteInfo(1L, 1L));

        mockMvc.perform(post("/api/normativa/cartola/calcular-rentabilidad")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(entrada)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.erroresContador").value(0))
                .andExpect(jsonPath("$.salida").isArray())
                .andExpect(jsonPath("$.salida[0].idCliente").value(1));
    }

    @Test
    @DisplayName("POST /calcular-rentabilidad con lista vacía retorna OK sin error")
    void calcularRentabilidad_listaVacia_retornaOk() throws Exception {
        CalcularRentabilidadEntrada entrada = new CalcularRentabilidadEntrada();
        entrada.idEmpresa        = 1L;
        entrada.esPauta          = false;
        entrada.movimientosCaja  = List.of();
        entrada.movimientosTitulo = List.of();
        entrada.saldoFinal       = List.of();
        entrada.listaClientes    = List.of();

        mockMvc.perform(post("/api/normativa/cartola/calcular-rentabilidad")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(entrada)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.erroresContador").value(0))
                .andExpect(jsonPath("$.salida").isArray());
    }
}
