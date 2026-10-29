package cl.bcs.normativa.generacion;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cl.bcs.normativa.data.Registro;
import cl.bcs.normativa.registros.RegistroDJ1946;

public final class GeneradorDJ1946 extends Generador {

    private static final int LARGO_REGISTRO = 1260;
	private RegistroDJ1946 registroResumen;
    private long totalRegistros;
    private double sumaRentaBruta;
    private double sumaImpuesto;
    private double sumaCredito;
    private double sumaOtrosCreditos;
    private double sumaRentaLiquida;
    private double sumaTotalCreditos;

    public GeneradorDJ1946(final ContextoGeneracion contexto)
            throws SQLException, IOException, SerializadorException {
        super(contexto);
        this.totalRegistros = 0;
        this.registroResumen = new RegistroDJ1946();

        this.sumaRentaBruta = 0;
        this.sumaImpuesto = 0;
        this.sumaCredito = 0;
        this.sumaOtrosCreditos = 0;
        this.sumaTotalCreditos = 0;
        this.sumaRentaLiquida = 0;
    }

    private void actualizarAcumuladores(final RegistroDJ1946 registroDJ1946) {
        this.contarCasosInformados();
        this.totalRegistros += 1;
        //registroResumen.acumular(registroDJ1946);

        this.sumaRentaBruta += Math.round(registroDJ1946.getRentaBruta());
        this.sumaImpuesto += Math.round(registroDJ1946.getImpuesto());
        this.sumaCredito += Math.round(registroDJ1946.getCreditoIDPC());
        this.sumaOtrosCreditos += Math.round(registroDJ1946.getOtrosCreditos());
        this.sumaTotalCreditos += Math.round(this.sumaCredito + this.sumaOtrosCreditos);
        this.sumaRentaLiquida += Math.round(registroDJ1946.getRentaLiquida());
    }

    @Override
    public void generarDetalle(final Registro registro) throws SerializadorException {
        println(generarRegistroTipo2(registro));
    }

    @Override
    public void generarEncabezado() throws SerializadorException {
    	
    	if (getContexto().getModoPresentacion().getCodigo().equalsIgnoreCase("I"))
    	{
    		println(generarRegistroTipo0());
    	};
        
        println(generarRegistroTipo1());
    }

    @Override
    public String generarRegistroTipo0() throws SerializadorException {

        // Tipo de registro.
        String ret = "0";

        // Ano tributario.
        ret += String.format("%04d", getContexto().getAnoTributario()+1L);

        // Numero de formulario.
        ret += "1946";

        // RUT declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());
        
        // Total de registros a transmitir.
        ret += String.format("%010d", getCantidadTotalRegistros());

        // Codigo empresa.
        ret += String.format("%02d", getContexto().getCodigoEmpresa());

        // Numero de cliente.
        ret += String.format("%04d", getContexto().getNumeroCliente());

        // Checksum declarante.
        ret += Serializador.ceros(10);

        // Checksum SII.
        ret += Serializador.ceros(10);

        // Codigo de presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Codigo del tipo de declaracion.
        ret += getContexto().getTipoDeclaracion().getCodigo();

        // Folio de declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // Fecha/hora envio (ddMMyyyyhhmmss).
        ret += Serializador.ceros(16);

        // Numero de version.
        ret += String.format("%02d", getContexto().getNumeroVersion());
        
        // Numero de atencion.
        ret += Serializador.ceros(8);
        
        // Relleno.
        ret += Serializador.espacios(1171);
        
        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 0");
        }

	
        return ret;
    }


    public String generarRegistroTipo1() throws SerializadorException {

        // Tipo de registro y precedencia.
        String ret = "11";
       
		// Ano tributario.
        ret += String.format("%06d", getContexto().getAnoTributario()+1L);
           
        // Numero de formulario.
        ret += "1946";

        // Codigo de presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // Tipo de declaracion.
        ret += getContexto().getTipoDeclaracion().getCodigo();

        // RUT declarante anterior.
        ret += getContexto().getRutDeclaranteAnterior();

        // Codigo de presentacion anterior.
        ret += getContexto().getCodigoModoPresentacionAnterior();

        // Folio de declaracion anterior.
        ret += String.format("%07d", getContexto().getFolioDeclaracionAnterior());
        
        // Relleno.
        ret += Serializador.espacios(6);

        // RUT declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());
        
        // Razon social.
       String RazonSocial=getContexto().getRazonSocial();
        RazonSocial = Serializador.removerCaracteresEspeciales(RazonSocial);
        ret += String.format("%-30s",  (RazonSocial).length() > 30 ?  (RazonSocial).substring(0,30) :  (RazonSocial));
        
        // Direccion postal.
        String DireccionDeclaracion=getContexto().getDireccionDeclaracion();
        DireccionDeclaracion = Serializador.removerCaracteresEspeciales(DireccionDeclaracion);
        ret += String.format("%-35s",  (DireccionDeclaracion).length() > 35 ?  (DireccionDeclaracion).substring(0,35) :  (DireccionDeclaracion));

        // Comuna.
        String ComunaDeclaracion=getContexto().getComunaDeclaracion();
        ComunaDeclaracion = Serializador.removerCaracteresEspeciales(ComunaDeclaracion);
        ret += String.format("%-15s",  (ComunaDeclaracion).length() > 15 ?  (ComunaDeclaracion).substring(0,15) :  (ComunaDeclaracion));
        
        // Correo electronico.
        String EmailDeclaracion=getContexto().getEmailDeclaracion();
        ret += String.format("%30s",  (EmailDeclaracion).length() > 30 ?  (EmailDeclaracion).substring(0,30) :  (EmailDeclaracion));
        
        // Fax.
        ret += "56020000000";

        // Fono.
        ret += String.format("5602%07d", getContexto().getTelefonoDeclaracion() % 1000000);

        // Codigo de empresa.
        ret += String.format("%02d", getContexto().getCodigoEmpresa());

        // Numero de cliente.
        ret += String.format("%04d", getContexto().getNumeroCliente());

        // Unidad, caja y paquete.
        ret += Serializador.ceros(13);

        // Relleno.
        ret += Serializador.espacios(1056);
        
        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 1");
        }

        return ret;
    }


    /* Contenido de la DJ */
    public String generarRegistroTipo2(final Registro registro)
            throws SerializadorException {
    	
        /* Obtiene los datos de la clase RegistroDJ1946 */
    	final RegistroDJ1946 registroDJ1946 = new RegistroDJ1946(registro);
        actualizarAcumuladores(registroDJ1946);

        String numberDe;
        String numberEn;
        String numberD;
        String numberE;
      
        // Tipo de Registro y Numero de Formulario.
        String ret = "21946";

        // Codigo de Presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de Declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // Tipo de Declaracion.
        ret += getContexto().getTipoDeclaracion().getCodigo();
        
        // RUT Declarante
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());

        
        /* Antecedentes de las rentas derivadas de operaciones */
        // Operación
        ret += String.format("%02d", registroDJ1946.getOperacion());

        // Tipo Renta
        ret += String.format("%02d", registroDJ1946.getTipoRenta());

        // Renta Bruta
        ret += String.format("%015d", Double.valueOf(registroDJ1946.getRentaBruta()).intValue());
        //ret += String.format("%15s", (String.format("%15.0f", registroDJ1946.getRentaBruta()).replace(".", ""))).replace(" ","0");
        //ret += String.format("%15s", registroDJ1946.getRentaBruta());

        // Tasa de Retención
        //Toma la parte entera (Sin aproximar enteros)
        numberEn = String.valueOf(registroDJ1946.getTasaDeRetencion());
        ret += String.format("%3s", numberEn.substring(0, numberEn.indexOf("."))).replace(".", "").replace(" ", "0");
        //Toma la parte decimal (2 decimales)
        numberDe = String.valueOf(registroDJ1946.getTasaDeRetencion());
        String RestoDecimal = "0";
        numberDe = numberDe.substring(numberDe.indexOf(".")).replace(".", "").replace(" ", "0");
        ret += numberDe.length() < 2 ? numberDe + RestoDecimal : numberDe;

        // Impuesto
        String imp = String.format("%15d", Double.valueOf(registroDJ1946.getImpuesto()).intValue());
        ret += imp.replaceAll("\\s", "0");
        //ret += String.format("%15d", Double.valueOf(registroDJ1946.getImpuesto()).intValue()); //Esta linea trae el dato pero sin sin rellenar con 000 los campos vacios
    
        // Renta Liquida
        String rtl = String.format("%15d", Double.valueOf(registroDJ1946.getRentaLiquida()).intValue());
        ret += rtl.replaceAll("\\s", "0");

        // Crédito IDPC
        String cidpc = String.format("%15d", Double.valueOf(registroDJ1946.getCreditoIDPC()).intValue());
        ret += cidpc.replaceAll("\\s", "0");

        // Otros Crédito
        String oct = String.format("%15d", Double.valueOf(registroDJ1946.getOtrosCreditos()).intValue());
        ret += oct.replaceAll("\\s", "0");


        // Antecedentes del F50
        // F50 Código
         ret += String.format("%03d", registroDJ1946.getF50Codigo());

        // F50 Folio
        ret += String.format("%015d", registroDJ1946.getF50Folio());


        //Antecedentes del pago
        // Fecha de Pago
        ret += Serializador.serializar(registroDJ1946.getFechaPago());

        // Mondeda
        ret += String.format("%3s", registroDJ1946.getMonedaPago());

        // Tipo de Cambio
        //Toma la parte entera (Sin aproximar enteros)
        numberE = String.valueOf(registroDJ1946.getTipoCambioPago());
        ret += String.format("%4s", numberE.substring(0, numberE.indexOf("."))).replace(".", "").replace(" ", "0");
        //Toma la parte decimal (2 decimales)
        numberD = String.valueOf(registroDJ1946.getTipoCambioPago());
        String RestoDecimales = "0";
        numberD = numberD.substring(numberD.indexOf(".")).replace(".", "").replace(" ", "0");
        ret += numberD.length() < 2 ? numberD + RestoDecimales : numberD;


        // Antecedentes del pagador de las Rentas
        // Tipo Juridico
        ret += String.format("%01d", registroDJ1946.getPagadorTipoJuridico());

        // Razon Social o Apellidos
        ret += String.format("%150s", registroDJ1946.getPagadorRazonSocial());

        // Nombre
        ret += String.format("%40s", registroDJ1946.getPagadorNombre());

        // Rut
        String rut = String.format("%9s", registroDJ1946.getPagadorRut()).replace("-", "");
        String restoRut = "0";
        ret += rut.length() < 9 ? restoRut + rut : rut;

        // Domicilio
        ret += String.format("%180s", registroDJ1946.getPagadorDomicilio());

        // Ciudad
        ret += String.format("%30s", registroDJ1946.getPagadorCiudad());

        // Relacion
        ret += String.format("%02d", registroDJ1946.getPagadorRelacion());

        // Grupo o Holding Empresarial
        ret += String.format("%60s", registroDJ1946.getPagadorHolding());


        // Antecedentes del Perceptor Beneficiario Efectivo de la Renta
        // Tipo Juridico
        ret += String.format("%01d", registroDJ1946.getPerceptorTipoJuridico());

        // Pais Residencia
        ret += String.format("%2s", registroDJ1946.getPerceptorPaisResidencia());

        // Razon Social o Apellidos
        ret += String.format("%150s", registroDJ1946.getPerceptorRazonSocial());

        // Nombre
        ret += String.format("%40s", registroDJ1946.getPerceptorNombre());

        // Numero Identificacion Tributaria
        ret += String.format("%15s", registroDJ1946.getNumPerIdenTri_C51());

        // Rut
        String rutPer = String.format("%9s", registroDJ1946.getPerceptorRut()).replace("-", "");
        String restoRutPer = "0";
        ret += rutPer.length() < 9 ? restoRut + rutPer : rutPer;

        // Fecha Nacimiento
        ret += Serializador.serializar(registroDJ1946.getPerceptorFechaNacimiento());

        // Tipo Domicilio
        ret += String.format("%01d", registroDJ1946.getPerceptorTipoDomicilio());

        // Domicilio
        ret += String.format("%180s", registroDJ1946.getPerceptorDomicilio());

        // Ciudad
        ret += String.format("%30s", registroDJ1946.getPerceptorCiudad());

        // Pais
        ret += String.format("%2s", registroDJ1946.getPerceptorPais());


        // Antecedentes del Agente, Mandatario o Intermediario
        // Tipo Juridico
        ret += String.format("%01d", registroDJ1946.getIntermediarioTipoJuridico());

        // Pais Residencia
        ret += String.format("%2s", registroDJ1946.getIntermediarioPaisResidencia());

        // Razon Social o Apellidos
        ret += String.format("%150s", registroDJ1946.getIntermediarioRazonSocial());

        // Nombre
        ret += String.format("%40s", registroDJ1946.getIntermediarioNombre());

        // Numero Identificacion Tributaria
        ret += String.format("%15s", registroDJ1946.getNumInterIdenTri_C52());

        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 2");
        }
       

        return ret;
    }


    public String generarRegistroTipo3() throws SerializadorException {

        // Tipo de registro y numero de formulario.
        String ret = "311946";

        // Codigo de Presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de declaracion
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // RUT declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());

        // Suma Rentas Brutas
        ret += String.format("%20s" , (String.format("%020.0f",sumaRentaBruta).replace(".", ""))).replace(" ","0");

        // Total Impuestos
        ret += String.format("%20s" , (String.format("%020.0f",sumaImpuesto).replace(".", ""))).replace(" ","0");

        // Total Creditos
        ret += String.format("%20s" , (String.format("%020.0f",sumaTotalCreditos).replace(".", ""))).replace(" ","0");

        // Total Rentas Liquidas
        ret += String.format("%20s" , (String.format("%020.0f",sumaRentaLiquida).replace(".", ""))).replace(" ","0");

        // Total Casos Informados
        ret += String.format("%010d", getCantidadCasosInformados());

        // Fecha Presentacion
        ret += getContexto().getFechaPresentacion();

        // Rut Representante Legal
        ret += Serializador.serializarRut(getContexto().getRutRepresentante());

        // Relleno.
        ret += Serializador.espacios(1129);

        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 3");
        }

        return ret;
    }

    @Override
    public void generarResumen() throws SerializadorException {
        print(generarRegistroTipo3());
    }
    
    private long getCantidadTotalRegistros() {
        return getCantidadCasosInformados() + 3;
    }
}