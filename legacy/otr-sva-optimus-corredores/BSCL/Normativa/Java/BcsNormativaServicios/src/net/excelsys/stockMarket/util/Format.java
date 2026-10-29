package net.excelsys.stockMarket.util;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

public class Format {

    public String dateFormat(
            final Object fecha) {
        String fechaSalida = "";

        if (fecha instanceof String) {
            fechaSalida = formatDate(fecha.toString());
        }

        if (fecha instanceof Date) {
            fechaSalida = formatDate((Date) fecha);
        }

        if (fecha instanceof Timestamp) {
            fechaSalida = formatDate(new Date(((Timestamp) fecha).getTime()));
        }

        return fechaSalida;
    }

    public String dateFormat(
            final Object fecha,
            String formatInput,
            String formatOutput) {
        String fechaSalida = "";

        if (formatInput == null) {
            formatInput = "";
        }

        if (formatOutput == null) {
            formatOutput = "";
        }

        if (fecha instanceof String) {
            if ("".equals(formatInput) && "".equals(formatOutput)) {
                fechaSalida = formatDate(fecha.toString());
            }

            if (!"".equals(formatInput) && !"".equals(formatOutput)) {
                fechaSalida = formatDate(fecha.toString(), formatInput, formatOutput);
            }
        }

        if (fecha instanceof Date) {
            if ("".equals(formatOutput)) {
                fechaSalida = formatDate((Date) fecha);
            }

            if (!"".equals(formatOutput)) {
                fechaSalida = formatDate((Date) fecha, formatOutput);
            }

        }

        if (fecha instanceof Timestamp) {
            if ("".equals(formatOutput)) {
                fechaSalida = formatDate(new Date(((Timestamp) fecha).getTime()));
            }

            if (!"".equals(formatOutput)) {
                fechaSalida = formatDate(new Date(((Timestamp) fecha).getTime()), formatOutput);
            }
        }

        return fechaSalida;
    }

    private String formatDate(
            final Date fecha) {
        String fechaAux = "";
        final SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy");
        if (fecha == null) {
            fechaAux = "";
        } else {
            fechaAux = sdf2.format(fecha);
        }

        return fechaAux;
    }

    private String formatDate(
            final Date fecha,
            final String formatOutput) {
        String fechaAux = "";
        final SimpleDateFormat sdf2 = new SimpleDateFormat(formatOutput);
        if (fecha == null) {
            fechaAux = "";
        } else {
            fechaAux = sdf2.format(fecha);
        }

        return fechaAux;
    }

    public String formatDate(
            String fecha) {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        final SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy");

        if (fecha == null) {
            fecha = "";
        } else {
            try {
                fecha = sdf2.format(sdf.parse(fecha));
            } catch (ParseException e) {
                fecha = "";
            }
        }

        return fecha;
    }

    private String formatDate(
            String fecha,
            final String formatInput,
            final String formatOutput) {
        final SimpleDateFormat sdf = new SimpleDateFormat(formatInput);
        final SimpleDateFormat sdf2 = new SimpleDateFormat(formatOutput);

        if (fecha == null) {
            fecha = "";
        } else {
            try {
                fecha = sdf2.format(sdf.parse(fecha));
            } catch (ParseException e) {
                fecha = "";
            }
        }

        return fecha;
    }

    private double formatDoubleWithLocale(
            final String valor,
            final Locale loc) {
        // Locale.ENGLISH
        // Locale.GERMANY
        double value = 0;
        try {
            value = NumberFormat.getNumberInstance(loc).parse(valor).doubleValue();
        } catch (ParseException e) {
            value = 0;
        }

        return value;
    }

    private String formatNumber(
            final double value,
            final Locale loc) {
        final NumberFormat nf = NumberFormat.getNumberInstance(loc);
        final DecimalFormat df = (DecimalFormat) nf;
        df.applyPattern("###,###.###");
        return df.format(value);
    }

    private String formatNumber(
            final Object value,
            final Locale loc) {
        final NumberFormat nf = NumberFormat.getNumberInstance(loc);
        final DecimalFormat df = (DecimalFormat) nf;
        df.applyPattern("###,###.###");
        return df.format(value);
    }

    private String formatNumber(
            final String pattern,
            final double value,
            final Locale loc) {
        final NumberFormat nf = NumberFormat.getNumberInstance(loc);
        final DecimalFormat df = (DecimalFormat) nf;
        df.applyPattern(pattern);
        return df.format(value);
    }

    private String formatNumber(
            final String pattern,
            final Object value,
            final Locale loc) {
        final NumberFormat nf = NumberFormat.getNumberInstance(loc);
        final DecimalFormat df = (DecimalFormat) nf;
        df.applyPattern(pattern);
        return df.format(value);
    }

    public double formatNumberRound(
            final double value,
            final int cantDecimales) {
        String mask = "";
        if (cantDecimales > 0) {
            mask = ".";
            for (int x = 0; x < cantDecimales; x++) {
                mask = mask + "#";
            }
        }

        return Double.parseDouble(formatNumber("###" + mask, value, Locale.ENGLISH));
    }

    public double formatNumberRoundUSD(
            final double value) {
        return Double.parseDouble(formatNumber("###.##", value, Locale.ENGLISH));
    }

    public String formatNumberRoundUSDtoString(
            final double value) {
        return formatNumber("###.##", value, Locale.ENGLISH);
    }

    private String formatNumberTwoDecimal(
            final double value,
            final Locale loc) {
        final NumberFormat nf = NumberFormat.getNumberInstance(loc);
        final DecimalFormat df = (DecimalFormat) nf;
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);
        return df.format(value);
    }

    private String formatNumberTwoDecimal(
            final Object value,
            final Locale loc) {
        final NumberFormat nf = NumberFormat.getNumberInstance(loc);
        final DecimalFormat df = (DecimalFormat) nf;
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);
        return df.format(value);
    }

    public String formatRut(
            final Object objRut) {
        String aux = "";
        try {
            final MessageFormat format = new MessageFormat("{0,number,integer}-{1}", Locale.GERMAN);
            final Object obj[] = format.parse(StringUtils.deleteWhitespace(ObjToString(objRut)));
            final Object objaux[] = new Object[] { obj[0] };
            format.applyPattern("{0,number}");
            aux = format.format(objaux) + '-' + obj[1];
        } catch (ParseException ex) {
            return aux;
        }

        return aux;
    }

    protected String formatValue(
            final Object valueToFormat) {
        final Object value = valueToFormat;

        if (valueToFormat == null) {
            return "";
        }

        return formatRut(value);
    }

    public String numberFormat(
            final Object numero) {
        String resp = "0";

        try {
            if (numero instanceof String) {
                double num = 0;

                num = formatDoubleWithLocale(numero.toString(), Locale.GERMANY);
                resp = formatNumber(num, Locale.GERMANY);
            } else {
                resp = formatNumber(numero, Locale.GERMANY);
            }
        } catch (Exception ex) {
            resp = "0";
        }

        return resp;
    }

    public String numberFormat(
            final Object numero,
            final Locale loc,
            String formatOutput) {
        String resp = "0";

        try {
            if (formatOutput == null) {
                formatOutput = "";
            }

            if (numero instanceof String) {
                double num = 0;

                num = formatDoubleWithLocale(numero.toString(), loc);

                if ("".equals(formatOutput)) {
                    resp = formatNumber(num, loc);
                }

                if (!"".equals(formatOutput)) {
                    resp = formatNumber(formatOutput, num, loc);
                }
            } else {
                if ("".equals(formatOutput)) {
                    resp = formatNumber(numero, loc);
                }

                if (!"".equals(formatOutput)) {
                    resp = formatNumber(formatOutput, numero, loc);
                }
            }

        } catch (Exception ex) {
            resp = "0";
        }

        return resp;
    }

    public String numberFormatClp(
            final Object numero) {
        String resp = "0";

        try {
            if (numero instanceof String) {
                double num = 0;
                num = formatDoubleWithLocale(numero.toString(), Locale.GERMANY);
                resp = formatNumber("#,##0", num, Locale.GERMANY);
            } else {
                resp = formatNumber("#,##0", numero, Locale.GERMANY);
            }
        } catch (Exception ex) {
            // ex.printStackTrace();
            resp = "0";
        }

        return resp;
    }

    public String numberFormatPref(
            final Object numero) {
        return numberFormatPref(numero, 3);
    }

    public String numberFormatPref(
            final Object numero,
            final int cantDecimal) {
        String resp = "0";

        try {
            if (numero instanceof String) {
                double num = 0;
                num = formatDoubleWithLocale(numero.toString(), Locale.GERMANY);
                resp = formatNumber(num, Locale.GERMANY);
                if (num > 0) {
                    resp = "+ " + resp;
                }
            } else {
                double aux = 0;

                if (numero instanceof Integer) {
                    aux = ((Integer) numero).intValue();
                }

                if (numero instanceof Double) {
                    aux = ((Double) numero).doubleValue();
                }

                if (numero instanceof BigDecimal) {
                    aux = ((BigDecimal) numero).doubleValue();
                }

                if (numero instanceof Long) {
                    aux = ((Long) numero).longValue();
                }

                resp = formatNumber(numero, Locale.GERMANY);

                if (aux > 0) {
                    resp = "+ " + resp;
                }
            }

        } catch (Exception ex) {
            resp = "0";
        }

        return resp;
    }

    public String numberFormatTwoDecimal(
            final Object numero) {
        String resp = "0";

        try {

            if (numero instanceof String) {
                double num = 0;
                num = formatDoubleWithLocale(numero.toString(), Locale.GERMANY);
                resp = formatNumberTwoDecimal(num, Locale.GERMANY);
                if (num > 0) {
                    resp = "+ " + resp;
                }
            } else {
                double aux = 0;

                if (numero instanceof Integer) {
                    aux = ((Integer) numero).intValue();
                }

                if (numero instanceof Double) {
                    aux = ((Double) numero).doubleValue();
                }

                if (numero instanceof BigDecimal) {
                    aux = ((BigDecimal) numero).doubleValue();
                }

                if (numero instanceof Long) {
                    aux = ((Long) numero).longValue();
                }

                resp = formatNumberTwoDecimal(numero, Locale.GERMANY);

                if (aux > 0) {
                    resp = "+ " + resp;
                }

            }

        } catch (Exception ex) {
            resp = "0";
        }

        return resp;
    }

    public String numberFormatUsd(
            final Object numero) {
        String resp = "0";

        try {
            if (numero instanceof String) {
                double num = 0;
                num = formatDoubleWithLocale(numero.toString(), Locale.GERMANY);
                resp = formatNumber("#,##0.00##", num, Locale.GERMANY);
            } else {
                resp = formatNumber("#,##0.00##", numero, Locale.GERMANY);
            }
        } catch (Exception ex) {
            resp = "0";
        }

        return resp;
    }

    public String numberFormatCartolaRFIF(
            final Object numero) {
        String resp = "0";

        try {
            if (numero instanceof String) {
                double num = 0;
                num = formatDoubleWithLocale(numero.toString(), Locale.GERMANY);
                resp = formatNumber("#,##0.0000", num, Locale.GERMANY);
            } else {
                resp = formatNumber("#,##0.0000", numero, Locale.GERMANY);
            }
        } catch (Exception ex) {
            resp = "0";
        }

        return resp;
    }
    
    public String numberFormatCartolaFFMM(
            final Object numero) {
        String resp = "0";

        try {
            if (numero instanceof String) {
                double num = 0;
                num = formatDoubleWithLocale(numero.toString(), Locale.GERMANY);
                resp = formatNumber("#,##0.000000", num, Locale.GERMANY);
            } else {
                resp = formatNumber("#,##0.000000", numero, Locale.GERMANY);
            }
        } catch (Exception ex) {
            resp = "0";
        }

        return resp;
    }    
    
    private String ObjToString(
            final Object numero) {
        String resp = "";
        if (numero instanceof String) {
            resp = numero.toString().trim();
        } else {
            double aux = 0;
            if (numero instanceof Integer) {
                aux = ((Integer) numero).intValue();
            } else if (numero instanceof Double) {
                aux = ((Double) numero).doubleValue();
            } else if (numero instanceof BigDecimal) {
                aux = ((BigDecimal) numero).doubleValue();
            } else if (numero instanceof Long) {
                aux = ((Long) numero).longValue();
            }

            resp = String.valueOf(new Double(aux).intValue());
        }

        return resp;
    }

}
