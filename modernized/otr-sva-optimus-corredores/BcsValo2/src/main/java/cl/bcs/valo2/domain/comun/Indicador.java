package cl.bcs.valo2.domain.comun;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Indicador(String codigo, BigDecimal valor, LocalDate fecha) {}
