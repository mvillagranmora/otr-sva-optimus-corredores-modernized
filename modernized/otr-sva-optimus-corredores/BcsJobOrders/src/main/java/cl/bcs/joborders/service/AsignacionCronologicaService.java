package cl.bcs.joborders.service;

import cl.bcs.joborders.domain.Asignacion;
import cl.bcs.joborders.domain.Orden;
import cl.bcs.joborders.domain.Transaccion;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Algoritmo de asignación cronológica O(n²).
 * Replica exacta de BcsJobOrderChronologicAllocation.transaction():116-380.
 * Todos los métodos son public static para facilitar el testing puro.
 */
@Service
public class AsignacionCronologicaService {

    /**
     * B-JOB-01: Réplica de BcsJobOrderChronologicAllocation.compararFechasConDate().
     * Retorna true si fecha1 <= fechaActual (antes o igual).
     */
    public static boolean compararFechasConDate(String fecha1, String fechaActual) {
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date d1 = fmt.parse(fecha1);
            Date d2 = fmt.parse(fechaActual);
            if (d1.before(d2)) return true;
            if (d2.before(d1)) return false;
            return true; // iguales → true (legacy else branch)
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * B-JOB-02..10: Algoritmo de asignación cronológica.
     * Procesa las listas en memoria; no accede a HANA.
     * Las listas de entrada son inmutables; el estado mutable se gestiona internamente.
     */
    public static List<Asignacion> asignar(List<Orden> ordenes, List<Transaccion> transacciones) {
        // Wrappers mutables internos (el algoritmo consume cantidadDisponible iterando)
        var mo = new ArrayList<double[]>(ordenes.size());   // [cantDisp, cantAsig]
        var mt = new ArrayList<double[]>(transacciones.size());
        for (var o : ordenes) mo.add(new double[]{ o.cantidadDisponible(), o.cantidadAsignada() });
        for (var t : transacciones) mt.add(new double[]{ t.cantidadDisponible(), t.cantidadAsignada() });

        var resultado = new ArrayList<Asignacion>();
        var contador  = new AtomicInteger(0);

        for (int oi = 0; oi < ordenes.size(); oi++) {
            Orden ord = ordenes.get(oi);
            double[] wOrd = mo.get(oi);

            for (int ti = 0; ti < transacciones.size(); ti++) {
                Transaccion trx = transacciones.get(ti);
                double[] wTrx  = mt.get(ti);

                // B-JOB-01: fecha
                if (!compararFechasConDate(ord.fechaOperacion(), trx.fechaOperacionTransaccion())) continue;
                // B-JOB-02: clasificacion (case-insensitive)
                if (!trx.idClasificacionTipoInstrumento().equalsIgnoreCase(ord.idClasificacionTipoInstrumento())) continue;
                // B-JOB-02: idInstrumento (int ==)
                if (trx.idInstrumento() != ord.idInstrumento()) continue;
                // B-JOB-02: tipo instrumento (case-insensitive)
                if (!trx.idTipoInstrumento().equalsIgnoreCase(ord.idTipoInstrumento())) continue;
                // B-JOB-10: bolsa — "N" indica sin bolsa específica; usa equals para evitar problema de interning con strings de HANA
                if (!trx.idBolsa().equalsIgnoreCase(ord.idBolsa()) && !"N".equals(ord.especificaBolsa())) continue;
                // B-JOB-02: tipo operacion (equals exacto — legacy usa .equals())
                if (!ord.idTipoOperacion().equals(trx.idTipoOperacion())) continue;
                // B-JOB-02: condicion liquidacion (case-insensitive)
                if (!trx.idCondicionDeLiquidacion().equalsIgnoreCase(ord.idCondicionDeLiquidacion())) continue;

                if (ord.idTipoOperacion().contains("COM")) {
                    // B-JOB-03: tipo L — CANARY: >= (no >)
                    if (ord.idCodigoTipoPrecio().equalsIgnoreCase("L") && ord.precio() >= trx.precio()) {
                        if (wOrd[0] > 0 && ord.precio() > 0 && wTrx[0] > 0 && trx.precio() > 0)
                            asignarCantidad(wOrd, wTrx, ord, trx, resultado, contador);
                    }
                    // B-JOB-04: tipo M
                    if (ord.idCodigoTipoPrecio().equalsIgnoreCase("M")) {
                        if (wOrd[0] > 0 && ord.precio() > 0 && wTrx[0] > 0 && trx.precio() > 0)
                            asignarCantidad(wOrd, wTrx, ord, trx, resultado, contador);
                    }
                }

                if (ord.idTipoOperacion().contains("VEN")) {
                    // B-JOB-05: tipo L
                    if (ord.idCodigoTipoPrecio().equalsIgnoreCase("L") && ord.precio() >= trx.precio()) {
                        if (wOrd[0] > 0 && ord.precio() > 0 && wTrx[0] > 0 && trx.precio() > 0)
                            asignarCantidad(wOrd, wTrx, ord, trx, resultado, contador);
                    }
                    // B-JOB-06: tipo M
                    if (ord.idCodigoTipoPrecio().equalsIgnoreCase("M")) {
                        if (wOrd[0] > 0 && ord.precio() > 0 && wTrx[0] > 0 && trx.precio() > 0)
                            asignarCantidad(wOrd, wTrx, ord, trx, resultado, contador);
                    }
                }
            }
        }
        return resultado;
    }

    // B-JOB-07..09: decisión de cantidad y construcción de Asignacion
    private static void asignarCantidad(double[] wOrd, double[] wTrx,
                                         Orden ord, Transaccion trx,
                                         List<Asignacion> lista, AtomicInteger contador) {
        double cdOrd = wOrd[0], caOrd = wOrd[1];
        double cdTrx = wTrx[0], caTrx = wTrx[1];

        // B-JOB-07: ord.cantDisp <= trx.cantDisp → consumir orden completa
        if (cdOrd <= cdTrx) {
            int monto = (int)((int) cdOrd * trx.precio()); // B-JOB-09: doble cast int
            lista.add(new Asignacion(contador.getAndIncrement(), "ANF", (int) cdOrd,
                trx.fechaOperacionTransaccion(), trx.idTransaccionPaso(), ord.idOrdenPaso(), monto));
            wOrd[0] = 0;
            wOrd[1] = caOrd + cdOrd;
            wTrx[0] = cdTrx - cdOrd;
            wTrx[1] = caTrx + cdOrd;
        }
        // B-JOB-08: ord.cantDisp > trx.cantDisp → consumir transacción completa
        else {
            int monto = (int)((int) cdTrx * trx.precio()); // B-JOB-09: usa cdTrx (cantidad real asignada)
            lista.add(new Asignacion(contador.getAndIncrement(), "ANF", (int) cdTrx,
                trx.fechaOperacionTransaccion(), trx.idTransaccionPaso(), ord.idOrdenPaso(), monto));
            wOrd[0] = cdOrd - cdTrx;
            wOrd[1] = caOrd + cdTrx;
            wTrx[0] = 0;
            wTrx[1] = caTrx + cdTrx;
        }
    }
}
