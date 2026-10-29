package cl.bcs.valo2.data.cargadores;

import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import cl.bcs.sql.utils.SqlUtil;
import cl.bcs.utils.DateUtil;
import cl.bcs.valo2.data.archivos.ArchivoBonos;
import cl.bcs.valo2.entidades.Bono;

public class GeneradorCargaBonos {

    private static String createInsertBonos(
            final Bono bono) {
        final StringBuilder sb = new StringBuilder(256);
        sb.append("INSERT INTO ")
                .append(SqlUtil.toQuotedString("BSCL.Plataforma.BD::INS.InstrumentoBono"))
                .append('(').append(SqlUtil.toQuotedString("IdInstrumento")).append(",")
                .append(SqlUtil.toQuotedString("FechaDevengo")).append(",")
                .append(SqlUtil.toQuotedString("AjusteFinDeMes")).append(",")
                .append(SqlUtil.toQuotedString("CallConfirmado")).append(",")
                .append(SqlUtil.toQuotedString("ConvencionDias")).append(",")
                .append(SqlUtil.toQuotedString("CuponeraVariable")).append(",")
                .append(SqlUtil.toQuotedString("TasaEmision")).append(",")
                .append(SqlUtil.toQuotedString("IdTipoInflacion")).append(",")
                .append(SqlUtil.toQuotedString("TenorInflacion")).append(",")
                .append(SqlUtil.toQuotedString("TipoLibor")).append(",")
                .append(SqlUtil.toQuotedString("TipoCalculoInteresesCupon")).append(",")
                .append(SqlUtil.toQuotedString("TipoCalculoInteresesDevengados")).append(",")
                .append(SqlUtil.toQuotedString("IdCalendario")).append(",")
                .append(SqlUtil.toQuotedString("FechaPrepago")).append(",")
                .append(SqlUtil.toQuotedString("CUSIP")).append(",")
                .append(SqlUtil.toQuotedString("COMMON")).append(",")
                .append(SqlUtil.toQuotedString("SEDOL")).append(",")
                .append(SqlUtil.toQuotedString("TickerBloomberg")).append(",")
                .append(SqlUtil.toQuotedString("MercadoLocal")).append(",")
                .append(SqlUtil.toQuotedString("MercadoExtrabursatil")).append(",")
                .append(SqlUtil.toQuotedString("MercadoExtranjero")).append(",")
                .append(SqlUtil.toQuotedString("PresenciaBursatil")).append(",")
                .append(SqlUtil.toQuotedString("IdPaisTransaccion")).append(",")
                .append(SqlUtil.toQuotedString("ValorBolsa")).append(",")
                .append(SqlUtil.toQuotedString("ValorBloomberg")).append(",")
                .append(SqlUtil.toQuotedString("DurationBolsa")).append(",")
                .append(SqlUtil.toQuotedString("DurationBloomberg")).append(",")
                .append(SqlUtil.toQuotedString("FechaValorizacion")).append(",")
                .append(SqlUtil.toQuotedString("ValorNominal")).append(",")
                .append(SqlUtil.toQuotedString("PrecioValorizacion")).append(") VALUES(")
                .append("SEQUENCIA").append(',').append(toSingleQuotedDate(bono.getFechaDevengo()))
                .append(',').append(toSingleQuotedString(bono.isAjusteFinDeMes() ? "S" : "N"))
                .append(',').append(toSingleQuotedString(bono.isCallConfirmado() ? "S" : "N"))
                .append(',').append(toSingleQuotedString(bono.getConvencionDias())).append(',')
                .append(toSingleQuotedString(bono.isCuponeraVariable() ? "S" : "N")).append(',')
                .append(bono.getTasaEmision()).append(',')
                .append(toSingleQuotedString(bono.getTipoInflacion())).append(',')
                .append(toSingleQuotedString(bono.getTenorInflacion())).append(',')
                .append(toSingleQuotedString(bono.getTipoLibor())).append(',')
                .append(bono.getTipoCalculoInteresesCupon()).append(',')
                .append(bono.getTipoCalculoInteresesDevengados()).append(',')
                .append(toSingleQuotedString(bono.getCalendario())).append(',').append("null")
                .append(',').append(toSingleQuotedString(bono.getNemo())).append(',').append("null")
                .append(',').append("null").append(',').append("null").append(',')
                .append(toSingleQuotedString("N")).append(',').append(toSingleQuotedString("N"))
                .append(',').append(toSingleQuotedString("N")).append(',')
                .append(toSingleQuotedString("N")).append(',').append("0").append(',').append("0")
                .append(',').append("0").append(',').append("0").append(',').append("0").append(',')
                .append("null").append(',').append("0").append(',').append("0").append(");");
        return sb.toString();
    }

    private static String createInsertInstrumentos(
            final Bono bono) {
        final StringBuilder sb = new StringBuilder(256);
        sb.append("INSERT INTO ")
                .append(SqlUtil.toQuotedString("BSCL.Plataforma.BD::INS.Instrumento")).append('(')
                .append(SqlUtil.toQuotedString("IdInstrumento")).append(',')
                .append(SqlUtil.toQuotedString("Nemotecnico")).append(',')
                .append(SqlUtil.toQuotedString("ISIN")).append(',')
                .append(SqlUtil.toQuotedString("CorteMinimo")).append(',')
                .append(SqlUtil.toQuotedString("EstaInscrito")).append(',')
                .append(SqlUtil.toQuotedString("EsDivisible")).append(',')
                .append(SqlUtil.toQuotedString("RazonSocial")).append(',')
                .append(SqlUtil.toQuotedString("Lista")).append(',')
                .append(SqlUtil.toQuotedString("FechaEmision")).append(',')
                .append(SqlUtil.toQuotedString("FechaVencimiento")).append(',')
                .append(SqlUtil.toQuotedString("IdInstrumentoRelacionado")).append(',')
                .append(SqlUtil.toQuotedString("IdEmisor")).append(',')
                .append(SqlUtil.toQuotedString("IdTipoReajuste")).append(',')
                .append(SqlUtil.toQuotedString("Reajustable")).append(',')
                .append(SqlUtil.toQuotedString("IdMoneda")).append(',')
                .append(SqlUtil.toQuotedString("IdTipoInstrumento")).append(',')
                .append(SqlUtil.toQuotedString("IdEstado")).append(',')
                .append(SqlUtil.toQuotedString("DecimalesPrecio")).append(',')
                .append(SqlUtil.toQuotedString("Tamano")).append(',')
                .append(SqlUtil.toQuotedString("FechaInicioVigencia")).append(',')
                .append(SqlUtil.toQuotedString("TasaFiscal")).append(',')
                .append(SqlUtil.toQuotedString("IndArt104")).append(',')
                .append(SqlUtil.toQuotedString("IdEntidadClasificadoraRiesgo")).append(',')
                .append(SqlUtil.toQuotedString("ValorClasificacionRiesgo")).append(',')
                .append(SqlUtil.toQuotedString("TipoValorizacion")).append(',')
                .append(SqlUtil.toQuotedString("Tasa")).append(',')
                .append(SqlUtil.toQuotedString("Precio")).append(',')
                .append(SqlUtil.toQuotedString("ValorizaPor")).append(',')
                .append(SqlUtil.toQuotedString("TieneCupon")).append(',')
                .append(SqlUtil.toQuotedString("NumeroAccionesCirculacion")).append(',')
                .append(SqlUtil.toQuotedString("NumeroCuotasCirculacionMesActual")).append(',')
                .append(SqlUtil.toQuotedString("IdPaisEmisor")).append(',')
                .append(SqlUtil.toQuotedString("IndicadorLiquidaDCV")).append(',')
                .append(SqlUtil.toQuotedString("Serie")).append(',')
                .append(SqlUtil.toQuotedString("IndicadorBolson")).append(',')
                .append(SqlUtil.toQuotedString("FrecuenciaCupon")).append(',')
                .append(SqlUtil.toQuotedString("PrecioReferencia")).append(',')
                .append(SqlUtil.toQuotedString("FechaReferencia")).append(',')
                .append(SqlUtil.toQuotedString("NumeroCupones")).append(") VALUES(")
                .append("SEQUENCIA").append(',').append(toSingleQuotedString(bono.getNemo()))
                .append(',').append(toSingleQuotedString("")).append(',').append("0").append(',')
                .append(toSingleQuotedString("N")).append(',').append(toSingleQuotedString("N"))
                .append(',').append(toSingleQuotedString(bono.getNemo())).append(',')
                .append(toSingleQuotedString(bono.getIsin())).append(',')
                .append(toSingleQuotedDate(bono.getFechaDevengo())).append(',')
                .append(toSingleQuotedDate(bono.getFechaVencimiento())).append(',').append("0")
                .append(',').append(toSingleQuotedString("")).append(',').append("null").append(',')
                .append(toSingleQuotedString("N")).append(',')
                .append(toSingleQuotedString(bono.getMonedaEmision())).append(',')
                .append(toSingleQuotedString("BON")).append(',').append(toSingleQuotedString("ACT"))
                .append(',').append("0").append(',').append("0").append(',')
                .append(toSingleQuotedDate(bono.getFechaDevengo())).append(',').append("0")
                .append(',').append(toSingleQuotedString("N")).append(',')
                .append(toSingleQuotedString("")).append(',').append(toSingleQuotedString(""))
                .append(',').append(toSingleQuotedString("INTER")).append(',').append("0")
                .append(',').append("100").append(',').append(toSingleQuotedString("P")).append(',')
                .append(toSingleQuotedString("S")).append(',').append("0").append(',').append("0")
                .append(',').append("0").append(',').append(toSingleQuotedString("N")).append(',')
                .append(toSingleQuotedString("")).append(',').append(toSingleQuotedString("N"))
                .append(',').append(bono.getPeriodoPago()).append(',').append("0").append(',')
                .append(toSingleQuotedDate(DateUtil.getDate(1, Calendar.JANUARY, 1))).append(',')
                .append(new Integer(bono.getCantidadCupones()).toString()).append(')').append(';');
        return sb.toString();
    }

    public static void main(
            final String[] args)
                    throws IOException,
                    SQLException {
        final ArchivoBonos archivoBonos = new ArchivoBonos("bonos.csv");
        final Collection<Bono> bonos = archivoBonos.getBonos().values();
        try (final PrintStream out = new PrintStream("insert_instrumentos.sql")) {
            for (final Bono bono : bonos) {
                out.println(createInsertInstrumentos(bono));
            }
        }

        try (final PrintStream out = new PrintStream("insert_bonos.sql")) {
            for (final Bono bono : bonos) {
                out.println(createInsertBonos(bono));
            }
        }
    }

    private static String toSingleQuotedDate(
            final Date date) {
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        return "'" + df.format(date) + "'";
    }

    private static String toSingleQuotedString(
            final String str) {
        return "'" + str + "'";
    }
}
