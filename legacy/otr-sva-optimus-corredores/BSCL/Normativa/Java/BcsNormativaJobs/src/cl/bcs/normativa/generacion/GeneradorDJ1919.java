package cl.bcs.normativa.generacion;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cl.bcs.normativa.data.Registro;
import cl.bcs.normativa.registros.RegistroDJ1919;

public final class GeneradorDJ1919 extends Generador {
	
	private static final int LARGO_REGISTRO = 230;
	private static final int LARGO_REGISTRO2 = 344;
	private RegistroDJ1919 registroResumen;
    private long totalRegistros;
    
    public GeneradorDJ1919(final ContextoGeneracion contexto)
            throws SQLException, IOException, SerializadorException {
        super(contexto);
        this.totalRegistros = 0;
        this.registroResumen = new RegistroDJ1919();
    }
    
    private void actualizarAcumuladores(final RegistroDJ1919 registroDJ1919) {
        this.contarCasosInformados();
        this.totalRegistros += 1;
        registroResumen.acumular(registroDJ1919);
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
        ret += "1919";

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
        ret += Serializador.espacios(141);
        
        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 0");
        }
	
        return ret;
    }
    
    public String generarRegistroTipo1() throws SerializadorException {

        // Tipo de registro y precedencia.
        String ret = "11";
       
        // Ano tributario.
        ret += String.format("%04d", getContexto().getAnoTributario()+1L) + "00";
           
        // Numero de formulario.
        ret += "1919";

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
        ret += String.format("%-30s",  (EmailDeclaracion).length() > 30 ?  (EmailDeclaracion).substring(0,30) :  (EmailDeclaracion));
        
        // Fax.
        ret += "56020000000";
        
        // Fono.
        ret += String.format("5602%07d", getContexto().getTelefonoDeclaracion() % 10000000);
        
        // Codigo de empresa.
        ret += String.format("%02d", getContexto().getCodigoEmpresa());

        // Numero de cliente.
        ret += String.format("%04d", getContexto().getNumeroCliente());
        
        // Unidad, caja y paquete.
        ret += Serializador.ceros(13);

        // Relleno.
        ret += Serializador.espacios(26);
        
        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 1");
        }
		
        return ret;
    }
    
    public String generarRegistroTipo2(final Registro registro)
            throws SerializadorException {
    	
    	final RegistroDJ1919 registroDJ1919 = new RegistroDJ1919(registro);
        actualizarAcumuladores(registroDJ1919);

        String propiedadE;
        String propiedadD;
        String emisionE;
        String emisionD;
        String colocacionE;
        String colocacionD;
        String adquisicionE;
        String adquisicionD;
        String enajenacionE;
        String enajenacionD;
        String enajenacionMayorE;
        String enajenacionMayorD;


        // Tipo de Registro y Numero de Formulario.
        String ret = "21919";
       
        // Codigo de Presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de Declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // Tipo de Declaracion.
        ret += getContexto().getTipoDeclaracion().getCodigo();
        	
        // RUT Declarante
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());
        
        // RUT emisor.
        ret += Serializador.serializarRut(registroDJ1919.getRutEmisor());
        
        // Tax ID
        String taxId = String.format("%15s", registroDJ1919.getTaxID()).replace("-", "");
        String restoTax = " ";
        ret += restoTax + taxId;
        
        // Rut Inversionista
        ret += Serializador.serializarRut(registroDJ1919.getRutCliente());
        
        // Pais constitucion
        ret += String.format("%2s", registroDJ1919.getCodigoPaisConstitucion());
        
        // Pais residencia
        ret += String.format("%2s", registroDJ1919.getCodigoPaisResidencia());
        
        // % De Propiedad o Participación directa o indirecta en el capital o Utilidades del Emisor
        //ret += String.format("%03d", registroDJ1919.getPorcentajePropiedadDirectaIndirecta());
        //ret += String.format("%02d", String.valueOf(registroDJ1919.getPorcentajePropiedadDirectaIndirecta()).split(".").length == 2 ? Integer.valueOf(String.valueOf(registroDJ1919.getPorcentajePropiedadDirectaIndirecta()).split(".")[1]) : 0);
        
        // % De Propiedad o Participación directa o indirecta en el capital o Utilidades del Emisor
        //Toma la parte entera (Sin aproximar enteros)
        propiedadE = String.valueOf(registroDJ1919.getPorcentajePropiedadDirectaIndirecta());
        ret += String.format("%3s", propiedadE.substring(0, propiedadE.indexOf("."))).replace(".", "").replace(" ", "0");
        //Toma la parte decimal (2 decimales)
        propiedadD = String.valueOf(registroDJ1919.getPorcentajePropiedadDirectaIndirecta());
        String RestoPropiedad = "0";
        propiedadD = propiedadD.substring(propiedadD.indexOf(".")).replace(".", "").replace(" ", "0");
        ret += propiedadD = propiedadD.length() > 2 ? propiedadD.substring(0, propiedadD.length()-1) : propiedadD.length() < 2 ? RestoPropiedad + propiedadD : propiedadD;
 
 
        // Participacion
        ret += String.format("%1d", registroDJ1919.getParticipacionDirectaIndirectaControl());
        
        // Nemotecnico
        ret += String.format("%25s", registroDJ1919.getNemotecnicoSVS());
        
        // Fecha operacion
        ret += Serializador.serializar(registroDJ1919.getFechaOperacion());
        
        // Numero Cuenta DCV
        ret += String.format("%10s", registroDJ1919.getNumeroCuentaDCV());
        
        // Moneda
        ret += String.format("%1s", registroDJ1919.getMoneda());
        
        
        // Valor de emisión en moneda o unidad de reajuste
        //Toma la parte entera (Sin aproximar enteros)
        emisionE = String.valueOf(registroDJ1919.getValorEmision());
        ret += String.format("%13s", emisionE.substring(0, emisionE.indexOf("."))).replace(".", "").replace(" ", "0");
        //Toma la parte decimal (2 decimales)
        emisionD = String.valueOf(registroDJ1919.getValorEmision());
        String RestoEmision = "0";
        emisionD = emisionD.substring(emisionD.indexOf(".")).replace(".", "").replace(" ", "0");
        ret += emisionD.length() < 2 ? emisionD + RestoEmision : emisionD;


        // Valor de colocación en moneda o unidad de reajuste
        //Toma la parte entera (Sin aproximar enteros)
        colocacionE = String.valueOf(registroDJ1919.getValorColocacion());
        ret += String.format("%13s", colocacionE.substring(0, colocacionE.indexOf("."))).replace(".", "").replace(" ", "0");
        //Toma la parte decimal (2 decimales)
        colocacionD = String.valueOf(registroDJ1919.getValorColocacion());
        String RestoColocacion = "0";
        colocacionD = colocacionD.substring(colocacionD.indexOf(".")).replace(".", "").replace(" ", "0");
        ret += colocacionD.length() < 2 ? colocacionD + RestoColocacion : colocacionD;
        
        
        // Valor de Adquisición en moneda o unidad de reajuste
        //Toma la parte entera (Sin aproximar enteros)
        adquisicionE = String.valueOf(registroDJ1919.getValorAdquisicion());
        ret += String.format("%13s", adquisicionE.substring(0, adquisicionE.indexOf("."))).replace(".", "").replace(" ", "0");
        //Toma la parte decimal (2 decimales)
        adquisicionD = String.valueOf(registroDJ1919.getValorAdquisicion());
        String RestoAdquisicion = "0";
        adquisicionD = adquisicionD.substring(adquisicionD.indexOf(".")).replace(".", "").replace(" ", "0");
        ret += adquisicionD.length() < 2 ? adquisicionD + RestoAdquisicion : adquisicionD;


        // Valor Adquisision pesos
        ret += String.format("%015d", Double.valueOf(registroDJ1919.getValorAdquisicionEnPesos()).intValue());
        
        // Valor de Enajenación en moneda o unidad de reajuste
        //Toma la parte entera (Sin aproximar enteros)
        enajenacionE = String.valueOf(registroDJ1919.getValorEnajenacion());
        ret += String.format("%13s", enajenacionE.substring(0, enajenacionE.indexOf("."))).replace(".", "").replace(" ", "0");
        //Toma la parte decimal (2 decimales)
        enajenacionD = String.valueOf(registroDJ1919.getValorEnajenacion());
        String RestoEnajenacion = "0";
        enajenacionD = enajenacionD.substring(enajenacionD.indexOf(".")).replace(".", "").replace(" ", "0");
        ret += enajenacionD.length() < 2 ? enajenacionD + RestoEnajenacion : enajenacionD;
        
        // Valor Enajenacion Pesos
        ret += String.format("%015d", Double.valueOf(registroDJ1919.getValorEnajenacionEnPesos()).intValue());
     

        // Mayor Valor Enajenación En moneda o unidad de reajuste
        //Toma la parte entera (Sin aproximar enteros)
        enajenacionMayorE = String.valueOf(registroDJ1919.getMayorValorEnajenacion());
        ret += String.format("%13s", enajenacionMayorE.substring(0, enajenacionMayorE.indexOf("."))).replace(".", "").replace(" ", "0");
        //Toma la parte decimal (2 decimales)
        enajenacionMayorD = String.valueOf(registroDJ1919.getMayorValorEnajenacion());
        String RestoEnajenacionMayor = "0";
        enajenacionMayorD = enajenacionMayorD.substring(enajenacionMayorD.indexOf(".")).replace(".", "").replace(" ", "0");
        ret += enajenacionMayorD.length() < 2 ? enajenacionMayorD + RestoEnajenacionMayor : enajenacionMayorD;
        
        // Mayor Valor Enajenación En pesos
        ret += String.format("%015d", Double.valueOf(registroDJ1919.getMayorValorEnajenacionEnPesos()).intValue());
        

        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 2");
        }

        return ret;
    }
    
    public String generarRegistroTipo3() throws SerializadorException {

        String totalE;
        String totalD;

        // Tipo de registro y numero de formulario.
        String ret = "311919";

        // Codigo de presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // RUT declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());
        
        // Total de Operaciones Informadas
        ret += String.format("%010d", getCantidadCasosInformados());
        
        // Total Mayor Valor Enajenacion Moneda
        //Toma la parte entera (Sin aproximar enteros)
        totalE = String.valueOf(registroResumen.getSumaMayorValorEnajenacion());
        ret += String.format("%16s", totalE.substring(0, totalE.indexOf("."))).replace(".", "").replace(" ", "0");
        //Toma la parte decimal (2 decimales)
        totalD = String.valueOf(registroResumen.getSumaMayorValorEnajenacion());
        String RestoTotal = "0";
        totalD = totalD.substring(totalD.indexOf(".")).replace(".", "").replace(" ", "0");
        ret += totalD.length() < 2 ? totalD + RestoTotal : totalD;

        
        // Total Mayor Valor Enajenacion Pesos
        ret += String.format("%018d", Double.valueOf(registroResumen.getSumaMayorValorEnajenacionEnPesos()).intValue());
        
        // Fecha de presentacion.
        ret += getContexto().getFechaPresentacion();

        // RUT del representante legal.
        ret += Serializador.serializarRut(getContexto().getRutRepresentante());
        
     // Relleno.
        ret += Serializador.espacios(143);
        
        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 3");
        }

        return ret;
    }
    
    @Override
    public void generarResumen() throws SerializadorException {
        print(generarRegistroTipo3());
        println("");
    }
    
    private long getCantidadTotalRegistros() {
        return getCantidadCasosInformados() + 3;
    }
}
