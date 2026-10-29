package cl.bcs.contabilidad.domain.service;

import cl.bcs.contabilidad.domain.port.out.ContabilidadRepository;
import cl.bcs.contabilidad.domain.service.devengo.CarteraValorizada;
import cl.bcs.contabilidad.domain.service.devengo.DevengoCalculator;
import cl.bcs.contabilidad.domain.service.devengo.TipoDevengo;
import cl.bcs.contabilidad.domain.service.soporte.Campo;
import cl.bcs.contabilidad.domain.service.soporte.Contexto;
import cl.bcs.contabilidad.domain.service.soporte.DatosMovimiento;
import cl.bcs.contabilidad.domain.service.soporte.Tablas;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * Base of the three accrual centralizations over the valuation report CarteraValorizadaIFRF (WP-C, CONT-C-20..47):
 * legacy DevengoATasaCompraCarteraPactada.java:64-130, DevengoATasaPacto.java:65-131 and
 * DevengoCarteraPropiaLibreYGarantia.java:67-131, which differ only in their SQL files and data fields.
 *
 * <p>Flow: the report rows of the empresa and informe are read twice (movement and event projections) and derived by
 * {@link DevengoCalculator}; the datos query yields one TabDatosMovimiento per field per row; {@code llenarResultado}
 * mirrors the movements. Nothing is written here: on a real run the tables go to
 * {@link ContabilidadRepository#persistirCentralizacion} once (see {@link CentralizadorSql}). These types read nothing
 * from the platform, so no {@code PlataformaPort} is needed.
 */
public abstract class CentralizaDevengoCartera extends CentralizadorSql {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final DevengoCalculator calculator;
    private final TipoDevengo tipo;
    private final List<Campo> campos;

    protected CentralizaDevengoCartera(ContabilidadRepository repository, DevengoCalculator calculator,
                                       TipoDevengo tipo, List<Campo> campos) {
        super(null, repository);
        this.calculator = calculator;
        this.tipo = tipo;
        this.campos = campos;
    }

    @Override
    protected Tablas calcular(Contexto ctx) {
        var entrada = ctx.entrada();
        Long idTipo = ctx.idTipo();
        Tablas t = new Tablas();

        for (CarteraValorizada c : repository.queryCarteraValorizada(tipo.sqlMovimientos(), ctx.idEmpresa(), entrada.idInforme())) {
            calculator.movimiento(tipo, idTipo, ctx.fechaContable(), c).ifPresent(t::movimiento);
        }
        for (CarteraValorizada c : repository.queryCarteraValorizada(tipo.sqlEventos(), ctx.idEmpresa(), entrada.idInforme())) {
            t.eventos(calculator.eventos(tipo, idTipo, c));
        }

        MapSqlParameterSource params = parametros("p_IdEmpresa", ctx.idEmpresa(), "p_IdInforme", entrada.idInforme());
        if (tipo.datosPorFechaContable()) params.addValue("p_FechaContable", ctx.fechaContable());
        t.datos(DatosMovimiento.de(idTipo, filas(tipo.sqlDatos(), params), campos));
        return t;
    }

    /**
     * {@code Fecha.stringToSqlDate(mov.X)}: yyyy-MM-dd. Legacy dereferenced the date (SimpleDateFormat.format(null)) and
     * the type aborted with a NullPointerException; the same exception is kept, naming the column.
     */
    protected static Campo fecha(String columna) {
        return Campo.de(columna, f -> Objects.requireNonNull(f.fecha(columna),
                () -> "Columna " + columna + " nula: no se puede formatear el dato").format(FECHA));
    }

    /** {@code mov.X} of a String field: null becomes "". */
    protected static Campo texto(String columna) {
        return Campo.texto(columna);
    }

    /** {@code String.valueOf(mov.X)}: null becomes the literal "null". */
    protected static Campo valorDe(String columna) {
        return Campo.valorDe(columna);
    }
}
