package barCode;

import java.awt.Color;
import java.awt.Image;
import java.sql.Timestamp;

import com.lowagie.text.pdf.BarcodePDF417;

public class PDF417 {

    private final BarcodePDF417 barcode;

    public PDF417() {
        barcode = new BarcodePDF417();
    }

    public Image generate(
            final String text) {
        barcode.setText(text);
        return barcode.createAwtImage(Color.BLACK, Color.WHITE);
    }

    public String getFormatoFecha(
            final Timestamp fecha) {

        final StringBuffer cadena = new StringBuffer("");
        if (fecha != null) {
            Integer dia = null;
            Integer mes = null;
            Integer anno = null;

            dia = new Integer(fecha.getDate());
            mes = new Integer(fecha.getMonth() + 1);
            anno = new Integer(fecha.getYear() + 1900);

            if (dia.toString().length() == 1) {
                cadena.append('0');
            }

            cadena.append(dia);

            if (mes.toString().length() == 1) {
                cadena.append('0');
            }

            cadena.append(mes);
            cadena.append(anno);
        }

        return cadena.toString();
    }

    public String getFormatoMonto(
            final String monto,
            String separador,
            final int numCaracteres) {

        String cadena = new String();
        if (!monto.equals("") && monto != null) {
            if (separador.equals(".")) {
                separador = "\\.";
            }

            final String[] arg = monto.split(separador);
            String decimales = null;
            cadena = arg[0];
            decimales = arg[1];

            final int numRellenoIzq = 4 - decimales.length();

            cadena = cadena + getRelleno(numRellenoIzq);

            final int numRellenoDer = numCaracteres - cadena.length();

            cadena = getRelleno(numRellenoDer) + cadena;
        }

        return cadena;
    }

    public String getFormatoNumeroComprobante(
            final String numeroComprobante,
            final int numCaracteres) {

        String cadena = new String();
        if (!numeroComprobante.equals("") && numeroComprobante != null) {
            final int numRelleno = numCaracteres - numeroComprobante.length();

            cadena = getRelleno(numRelleno) + numeroComprobante;
        }

        return cadena;
    }

    public String getFormatoRut(
            final String rutCorredora,
            final String separador,
            final int numCaracteres) {

        String cadena = new String();
        if (!rutCorredora.equals("") && rutCorredora != null) {
            final String[] arg = rutCorredora.split(separador);

            cadena = arg[0] + arg[1];

            final int numRelleno = numCaracteres - cadena.length();
            final String relleno = getRelleno(numRelleno);

            cadena = relleno + cadena;

        }

        return cadena;
    }

    public String getRelleno(
            final int veces) {

        int decr = veces;
        final StringBuffer relleno = new StringBuffer();
        while (decr > 0) {
            relleno.append('0');
            decr--;
        }

        return relleno.toString();
    }

}
