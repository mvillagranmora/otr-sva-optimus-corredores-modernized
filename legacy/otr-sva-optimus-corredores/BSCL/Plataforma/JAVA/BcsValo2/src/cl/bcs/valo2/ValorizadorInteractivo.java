package cl.bcs.valo2;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import cl.bcs.utils.PropertiesUtil;
import cl.bcs.valo2.entidades.Cupon;
import cl.bcs.valo2.fechas.Fecha;
import cl.bcs.valo2.fechas.contadores.ContadorDias;
import cl.bcs.valo2.fechas.contadores.ContadorDiasParams;

public final class ValorizadorInteractivo {
    private static final Map<String, String> MENU_BONOS = new TreeMap<String, String>();
    private static final Map<String, String> MENU_DAYCOUNT = new TreeMap<String, String>();
    private static final Map<String, String> MENU_MAIN = new TreeMap<String, String>();
    private static final Map<String, String> MENU_PAGARES = new TreeMap<String, String>();

    static {
        MENU_MAIN.put("B", "Valorizacion Bonos");
        MENU_MAIN.put("C", "Calculo de Cupones");
        MENU_MAIN.put("D", "Calculo de Dias");
        MENU_MAIN.put("P", "Valorizacion Pagares");
    }

    static {
        MENU_PAGARES.put("C", "Calculo de Captacion");
        MENU_PAGARES.put("R", "Calculo de Rescate");
        MENU_PAGARES.put("T", "Calculo de Tasa");
    }

    static {
        MENU_BONOS.put("M", "Calculo por Monto");
        MENU_BONOS.put("P", "Calculo por Precio");
        MENU_BONOS.put("T", "Calculo por Tasa");
    }

    static {
        MENU_DAYCOUNT.put("A", "30/360");
        MENU_DAYCOUNT.put("B", "30/365");
        MENU_DAYCOUNT.put("C", "ACT/360");
        MENU_DAYCOUNT.put("D", "ACT/365");
        MENU_DAYCOUNT.put("E", "ACT/ACT ISDA");
        MENU_DAYCOUNT.put("F", "BUS/252");
        MENU_DAYCOUNT.put("G", "ISMA 30/360");
        MENU_DAYCOUNT.put("H", "NL/365");
    }

    public static void main(
            final String[] args)
                    throws IOException,
                    SQLException {
        final Properties props = PropertiesUtil.newProperties("valorizador.properties");
        final ValorizadorInteractivo valorizadorInteractivo = new ValorizadorInteractivo(props);
        while (valorizadorInteractivo.menuMain()) {
            ;
        }
    }

    private final ContadorDias contadorDias;
    private final Cuponera cuponera;
    private final ValorizadorBonos valorizadorBonos;
    private final ValorizadorPagares valorizadorPagares;

    public ValorizadorInteractivo(
            final Properties props) throws SQLException {
        final ValorizadorContexto contexto = new ValorizadorContexto(props);
        this.contadorDias = new ContadorDias(contexto);
        this.cuponera = new Cuponera(contexto);
        this.valorizadorBonos = new ValorizadorBonos(contexto);
        this.valorizadorPagares = new ValorizadorPagares(contexto);
    }

    private boolean menuBonos()
            throws IOException {
        final MenuInteractivo menu = new MenuInteractivo("VALORIZADOR BONOS", MENU_BONOS);
        final String option = menu.selectOption();
        if (option == null || option.isEmpty()) {
            return false;
        }

        final ValorizadorBonosParams params = new ValorizadorBonosParams();
        params.setNemo(MenuInteractivo.readString("Nemo", "COLOM0628").toUpperCase());
        params.setFechaValorizacion(
                MenuInteractivo.readDate("Fecha Valorizacion", Fecha.hoy().sumarDias(1).toDate()));

        params.setValorNominal(
                MenuInteractivo.readBigDecimal("Valor Nominal", new BigDecimal("1000000.0")));

        if (option.charAt(0) == 'M') {
            params.setMontoTotal(
                    MenuInteractivo.readBigDecimal("Monto Total", new BigDecimal("1000000.0")));
        } else if (option.charAt(0) == 'P') {
            params.setPrecio(MenuInteractivo.readBigDecimal("Precio", new BigDecimal("100.0")));
        } else {
            params.setTasa(MenuInteractivo.readBigDecimal("Tasa%", new BigDecimal("5.0")));
        }

        valorizadorBonos.valorizar(params);
        System.out.println(params.toPrintableString());

        return true;
    }

    private boolean menuContadorDias()
            throws IOException {
        final MenuInteractivo menu = new MenuInteractivo("CONTADOR DIAS", MENU_DAYCOUNT);
        final String option = menu.selectOption();
        if (option == null || option.isEmpty()) {
            return false;
        }

        final String convencionDias = MENU_DAYCOUNT.get(option);
        final Date fecha1 = MenuInteractivo.readDate("Fecha 1", Fecha.hoy().toDate());
        final Date fecha2 = MenuInteractivo.readDate("Fecha 2", Fecha.hoy().toDate());
        final boolean ajusteEOM = MenuInteractivo.readBoolean("Ajuste Fin de Mes");

        Date fechaVencimiento = null;
        if (ajusteEOM) {
            fechaVencimiento = MenuInteractivo.readDate("Fecha Vencimiento", Fecha.hoy().toDate());
        }

        String calendario = null;
        if ("BUS/252".equals(convencionDias)) {
            calendario = MenuInteractivo.readString("Calendario", "BR");
        }

        final ContadorDiasParams contadorDiasParams = new ContadorDiasParams();
        contadorDiasParams.setConvencionDias(convencionDias);
        contadorDiasParams.setAjusteEOM(ajusteEOM);
        contadorDiasParams.setFechaVencimiento(Fecha.getInstance(fechaVencimiento));
        contadorDiasParams.setCalendario(calendario);

        final int days = contadorDias.calcularDiasCorridos(Fecha.getInstance(fecha1),
                Fecha.getInstance(fecha2), contadorDiasParams);
        final double years = contadorDias.calcularFraccionAno(Fecha.getInstance(fecha1),
                Fecha.getInstance(fecha2), contadorDiasParams);
        System.out.println("Anos: " + years);
        System.out.println("Dias: " + days);

        return true;
    }

    private boolean menuMain() {
        final MenuInteractivo menu = new MenuInteractivo("MENU PRINCIPAL", MENU_MAIN);
        final String option = menu.selectOption();
        if (option == null || option.isEmpty()) {
            return false;
        }

        try {
            if (option.charAt(0) == 'B') {
                while (menuBonos()) {
                    ;
                }
            } else if (option.charAt(0) == 'C') {
                opcionCupones();
            } else if (option.charAt(0) == 'D') {
                while (menuContadorDias()) {
                    ;
                }
            } else {
                while (menuPagares()) {
                    ;
                }
            }
        } catch (final Exception ex) {
            System.out.println(ex.getMessage());
        }

        return true;
    }

    private boolean menuPagares()
            throws IOException {
        final MenuInteractivo menu = new MenuInteractivo("VALORIZADOR PAGARES", MENU_PAGARES);
        final String option = menu.selectOption();
        if (option == null || option.isEmpty()) {
            return false;
        }

        final ValorizadorPagaresParams params = new ValorizadorPagaresParams();
        params.setNemo(MenuInteractivo.readString("Nemo", "PAGARE").toUpperCase());
        params.setFechaCaptacion(MenuInteractivo.readDate("Fecha Captacion", Fecha.hoy().toDate()));
        params.setFechaVencimiento(
                MenuInteractivo.readDate("Fecha Vencimiento", Fecha.hoy().sumarDias(30).toDate()));
        params.setDiasVencimiento(MenuInteractivo.readInteger("Dias Vencimiento", 0));
        params.setDiasAno(MenuInteractivo.readInteger("Dias Ano", 360));
        params.setTipoInteres(MenuInteractivo.readInteger("Tipo Interes", 0));

        if (option.charAt(0) == 'R' || option.charAt(0) == 'T') {
            params.setValorCaptacion(
                    MenuInteractivo.readBigDecimal("Valor Captacion", new BigDecimal("1000000.0")));
        }

        if (option.charAt(0) == 'C' || option.charAt(0) == 'T') {
            params.setValorRescate(
                    MenuInteractivo.readBigDecimal("Valor Rescate", new BigDecimal("1500000.0")));
        }

        if (option.charAt(0) == 'C' || option.charAt(0) == 'R') {
            params.setTasa(MenuInteractivo.readBigDecimal("Tasa%", new BigDecimal("5.0")));
        }

        valorizadorPagares.valorizar(params);
        System.out.println(params.toPrintableString());

        return true;
    }

    public void opcionCupones()
            throws IOException {
        final ValorizadorBonosParams params = new ValorizadorBonosParams();
        params.setNemo(MenuInteractivo.readString("Nemo", "COLOM0628").toUpperCase());
        params.setFechaValorizacion(
                MenuInteractivo.readDate("Fecha Liquidacion", Fecha.hoy().sumarDias(1).toDate()));
        params.setValorNominal(
                MenuInteractivo.readBigDecimal("Valor Nominal", new BigDecimal("1000000.0")));

        final List<Cupon> cupones = cuponera.obtenerCupones(params.getNemo(),
                params.getFechaValorizacion(), params.getValorNominal());
        Cuponera.print(System.out, cupones);
    }

}
