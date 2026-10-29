package cl.bcs.tesoreria.jobs.job.archivo;

import java.util.Date;
import java.util.List;

/**
 * The parts of the BancoEstado instruction file that {@code GenerarArchivoPagos} and {@code GenerarArchivoPagoBancoEstado} write
 * identically (legacy GenerarArchivoPagos.java:126-206 and 240-283, the same lines in GenerarArchivoPagoBancoEstado.java). The legacy
 * kept ONE array variable {@code lineaCvs} and reassigned or mutated it between rows; this class keeps it as {@link #lineaCvs} so
 * that quirk survives: with a second header row the first statement of the loop indexes the 2-column array of the previous row
 * ({@code ArrayIndexOutOfBoundsException: Index 4 out of bounds for length 2}, pinned by PG-B09).
 */
public final class BloqueBancoEstado {

    /** Legacy {@code RegistrosEncabezado} (Select_Encabezado.sql). */
    public static class Encabezado {
        public Integer TipoRegistroOptimus;
        public String TipoRegistro;
        public String Relleno;
        public Long NumeroNomina;
        public String RutOrdenate;
        public String NombreOrdenate;
        public String NumeroConvenio;
        public String CuentaCargo;
        public String MonedaNomina;
        public Date FechaOperacion;
        public String NombreUsuario;
        public String NombreBanco;
    }

    private final ArchivoPago archivo;
    /** The legacy {@code lineaCvs}. */
    public String[] lineaCvs = new String[10];

    public BloqueBancoEstado(ArchivoPago archivo) {
        this.archivo = archivo;
    }

    /** {@code lineaCvs = new String[n]}. */
    public String[] nueva(int columnas) {
        lineaCvs = new String[columnas];
        return lineaCvs;
    }

    /** {@code p_Archivo.escribir(lineaCvs)}. */
    public void escribir() {
        archivo.escribir(lineaCvs);
    }

    /** {@code p_Archivo.escribir(String[])} of the row just built: {@code nueva(columnas)} with one cell set, then written. */
    private void celda(int columnas, int indice, String texto) {
        nueva(columnas)[indice] = texto;
        escribir();
    }

    /** :128-186: one instruction header per nomina; {@code etiquetaNomina} is the label of column 2. */
    public void encabezado(Encabezado e, String fecha, String etiquetaNomina) {
        lineaCvs[4] = "Instruccion de Cargo en Cuenta Corriente/Pago";
        lineaCvs[9] = "VB CERTIFICADO";
        escribir();
        celda(9, 4, "y");
        celda(9, 4, "Transferencia de Fondos a Cuenta en Otro Banco T+0");
        nueva(2)[0] = " ";
        escribir();
        lineaCvs[1] = "Los abajo firmantes en representacion de " + e.NombreOrdenate + " RUT N " + e.RutOrdenate
                + " instruimos al " + e.NombreBanco + ", transferir con fecha";
        escribir();
        celda(10, 1, fecha + " los fondos.");
        celda(7, 1, " ");
        escribir();
        celda(8, 1, "Registro de Encabezado");
        celda(2, 1, " ");
        nueva(10);
        lineaCvs[2] = etiquetaNomina;
        lineaCvs[3] = "RUT Ordenante";
        lineaCvs[4] = "Nombre del Ordenante";
        lineaCvs[5] = "Numero de Convenio";
        lineaCvs[6] = "Cuenta de Cargo";
        lineaCvs[7] = "Moneda de la Nomina";
        lineaCvs[8] = "Fecha de Operacion";
        lineaCvs[9] = "Nombre de Usuario";
        escribir();
        celda(2, 1, " ");
        nueva(10);
        lineaCvs[0] = "1";
        lineaCvs[2] = e.NumeroNomina.toString();
        lineaCvs[3] = e.RutOrdenate;
        lineaCvs[4] = e.NombreOrdenate;
        lineaCvs[5] = e.NumeroConvenio;
        lineaCvs[6] = e.CuentaCargo;
        lineaCvs[7] = e.MonedaNomina;
        lineaCvs[8] = e.FechaOperacion.toString();
        lineaCvs[9] = e.NombreUsuario;
        escribir();
        celda(2, 1, " ");
    }

    /** :188-206: the title rows of the detail section. */
    public void tituloDetalle() {
        celda(2, 1, "Registro de Detalle");
        celda(2, 1, " ");
        nueva(13);
        lineaCvs[1] = "Tipo De Pago";
        lineaCvs[2] = "Ref Detalle Pago";
        lineaCvs[3] = "RUT Beneficiario";
        lineaCvs[4] = "Nombre Beneficiario";
        lineaCvs[5] = "Monto Remesa";
        lineaCvs[6] = "Cta. Cte. Beneficiario";
        lineaCvs[7] = "Banco del Beneficiario";
        lineaCvs[8] = "Informacion Remesa";
        lineaCvs[9] = "Codigo Dep. Dcv. Comprador";
        lineaCvs[10] = "Codigo Dep. Dcv. Vendedor";
        lineaCvs[11] = "Clave Medio Pago";
        escribir();
    }

    /** :240-252: the title rows of the control section. */
    public void tituloControl() {
        celda(2, 1, " ");
        celda(2, 1, "Registro de Control");
        celda(2, 1, " ");
        nueva(6);
        lineaCvs[1] = "Total Detalles";
        lineaCvs[5] = "Monto Nomina";
        escribir();
        celda(2, 1, " ");
    }

    /** :253-283: the control row just built in {@link #lineaCvs}, then the closing text and signature block of each nomina. */
    public void controlYPie(List<Encabezado> encabezados) {
        escribir();
        celda(2, 1, " ");
        for (Encabezado e : encabezados) {
            celda(12, 1, "Ademas solicitamos cargar la Cuenta Corriente N " + e.CuentaCargo + " que " + e.NombreOrdenate
                    + "mantiene en " + e.NombreBanco);
            celda(7, 1, "Declaramos que los datos expresados en el formulario y la instruccion de cargo son de nuestra exclusiva responsabilidad. Liberamos al Banco del Estado de Chile");
            for (int i = 0; i < 5; i++) {
                archivo.escribir(" ");
            }
            nueva(8);
            lineaCvs[1] = "Firma y Timbre Apoderado";
            lineaCvs[7] = "Firma y Timbre Apoderado";
            escribir();
        }
        archivo.cerrar();
    }
}
