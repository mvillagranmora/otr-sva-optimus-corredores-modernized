package cl.bcs.normativa.cartolas.procesos;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import cl.bcs.normativa.cartolas.batch.Batch;
import cl.bcs.normativa.cartolas.dal.NominaService;
import cl.bcs.normativa.cartolas.dal.ParametroService;
import cl.bcs.normativa.cartolas.entidades.EstadoProceso;
import cl.bcs.normativa.cartolas.entidades.Nomina;
import cl.bcs.normativa.cartolas.entidades.NominaException;
import cl.bcs.normativa.cartolas.entidades.Opcion;
import cl.bcs.normativa.cartolas.entidades.TipoProceso;
import cl.bcs.normativa.procesos.Proceso;
import cl.bcs.normativa.procesos.ProcesoException;
import cl.bcs.utils.DateUtil;

public class ProcesoProgramacionCartolas implements Proceso {

    /**
     * Instancia del Logger.
     */
    private static final Logger LOGGER = Logger
            .getLogger(ProcesoProgramacionCartolas.class.getName());

    private static String getPeriodoCartolas() {
        Date date = DateUtil.getStartOfMonth(new Date());
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_YEAR, -1);
        date = new Date(cal.getTimeInMillis());
        final DateFormat df = new SimpleDateFormat("yyyy-MM");
        return df.format(date);
    }

    private final ContextoCartolas contexto;

    public ProcesoProgramacionCartolas(
            final Connection conn,
            final Properties props) throws SQLException {
        this.contexto = new ContextoCartolas(conn, props, 0l);
    }

    private boolean estanLasCondiciones(
            final Map<String, String> parametros) {
        final boolean habilitado = Boolean.valueOf(parametros.get("Habilitado"));
        if (habilitado) {
            final int dd = Integer.valueOf(parametros.get("DiaEjecucion"));
            final String hhmm = parametros.get("HoraEjecucion");
            final String[] arr = hhmm.split("\\:");
            final int hh = Integer.valueOf(arr[0]);
            final int mm = Integer.valueOf(arr[1]);
            final Calendar cal = Calendar.getInstance();
            final int day = cal.get(Calendar.DAY_OF_MONTH);
            if (day == dd) {
                final int hour = cal.get(Calendar.HOUR);
                if (hour == hh) {
                    final int minute = cal.get(Calendar.MINUTE);
                    return minute > mm && minute < mm + 1;
                }
            }
        }

        return false;
    }

    @Override
    public void procesar()
            throws ProcesoException {
        try {
            final List<Nomina> nominas = NominaService
                    .getNominasProgramadas(contexto.getConnection(), contexto.getSchema());
            for (final Nomina nomina : nominas) {
                final Map<String, String> parametros = ParametroService.lookupByIdNomina(
                        contexto.getConnection(), contexto.getSchema(), nomina.getId());
                if (parametros.isEmpty()) {
                    throw new NominaException(
                            "Nomina programada sin parametros: id=" + nomina.getId());
                }

                if (estanLasCondiciones(parametros)) {
                    nomina.setPeriodo(getPeriodoCartolas());
                    final Nomina recurrente = NominaService.buscarRecurrente(
                            contexto.getConnection(), contexto.getSchema(), nomina);
                    if (recurrente == null) {
                        programar(nomina, parametros);
                    }
                }
            }
        } catch (ParseException | SQLException ex) {
            final String msg = "Error al programar nominas de cartolas";
            LOGGER.log(Level.SEVERE, msg, ex);
            throw new ProcesoException(msg, ex);
        }
    }

    private void programar(
            final Nomina nomina,
            final Map<String, String> map)
                    throws SQLException,
                    ParseException {

        nomina.setId(0);
        nomina.setEstadoProceso(EstadoProceso.INGRESADO);
        nomina.setTipoProceso(TipoProceso.RECURRENTE);
        nomina.setCertificada(Opcion.NO);
        nomina.setComentario("Proceso segun programa");

        final long idUsuario = 0;

        map.put("idEmpresa", String.format("%d", nomina.getIdEmpresa()));
        map.put("idUsuario", String.format("%d", idUsuario));
        map.put("schema", contexto.getSchema());

        NominaService.save(contexto, nomina);
        map.put("idNomina", String.format("%d", nomina.getId()));

        Batch.scheduleProcess(contexto.getConnection(), nomina.getIdEmpresa(), idUsuario,
                contexto.getSchema(), map);
    }
}
