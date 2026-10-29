package cl.bcs.valo2.domain.comun;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Paridad(String monedaOrigen, String monedaDestino, BigDecimal valor, LocalDate fecha) {}
