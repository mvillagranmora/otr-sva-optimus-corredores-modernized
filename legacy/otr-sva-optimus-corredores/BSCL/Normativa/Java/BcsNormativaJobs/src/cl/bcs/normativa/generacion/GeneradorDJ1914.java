package cl.bcs.normativa.generacion;

import java.io.IOException;
import java.sql.SQLException;

import cl.bcs.normativa.data.Registro;
import cl.bcs.normativa.registros.RegistroDJ1914;

public final class GeneradorDJ1914 extends Generador {
	
	private static final int LARGO_REGISTRO = 204;
	private RegistroDJ1914 registroResumen;
    private long totalRegistros;
    
    public GeneradorDJ1914(final ContextoGeneracion contexto)
            throws SQLException, IOException, SerializadorException {
        super(contexto);
        this.totalRegistros = 0;
        this.registroResumen = new RegistroDJ1914();
    }
    
    private void actualizarAcumuladores(final RegistroDJ1914 registroDJ1914) {
        this.contarCasosInformados();
        this.totalRegistros += 1;
        registroResumen.acumular(registroDJ1914);
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
        ret += String.format("%04d", getContexto().getAnoTributario() + 1L);

        // Numero de formulario.
        ret += "1914";

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
        ret += Serializador.espacios(115);

        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 0");
        }
	
        return ret;
    }
    
    public String generarRegistroTipo1() throws SerializadorException {

        // Tipo de registro y precedencia.
        String ret = "11";
       
        // Ano tributario.
        ret += String.format("%04d", getContexto().getAnoTributario() + 1L) + "00";
        
        // Numero de formulario.
        ret += "1914";

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

        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 1");
        }
		
        return ret;
    }
    
    public String generarRegistroTipo2(final Registro registro)
            throws SerializadorException {
    	
    	final RegistroDJ1914 registroDJ1914 = new RegistroDJ1914(registro);
        actualizarAcumuladores(registroDJ1914);

        // Tipo de Registro y Numero de Formulario.
        String ret = "21914";
       
        // Codigo de Presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de Declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // Tipo de Declaracion.
        ret += getContexto().getTipoDeclaracion().getCodigo();

        // RUT Declarante
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());

        // RUT Inversionista
        ret += Serializador.serializarRut(registroDJ1914.getRutCliente());
        
        // Tipo Instrumento
        ret += String.format("%1s", registroDJ1914.getTipoInstrumento());
        
        // Numero Identificador de la inversion
        ret += String.format("%25s", registroDJ1914.getNumeroIdentificadorInversion());
        
        // Fecha Inversion Original
        ret += Serializador.serializar(registroDJ1914.getFechaInversionOriginal());
     
        // Retiro Monto Nominal
        //ret += String.format("%15f", registroDJ1914.getRetiroInversion54BISMontoNominal()).replace(".", "").replace(" ","0"); 
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1914.getRetiroInversion54BISMontoNominal()).replace(".", ""))).replace(" ","0"); 
        
        // Retiro Interes Nominal
        //ret += String.format("%15f", registroDJ1914.getRetiroInversion54BISInteresPositivo()).replace(".", "").replace(" ","0"); 
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1914.getRetiroInversion54BISInteresPositivo()).replace(".", ""))).replace(" ","0"); 
        
        // Retiro Interes Nominal
        //ret += String.format("%15f", registroDJ1914.getRetiroInversion54BISInteresNegativo()).replace(".", "").replace(" ","0");  
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1914.getRetiroInversion54BISInteresNegativo()).replace(".", ""))).replace(" ","0"); 
        
        // Numero del Certificado
        ret += String.format("%015d", registroDJ1914.getNumeroCertificado()).replace(".", "").replace(" ","0");

        // Relleno
        ret += Serializador.espacios(78);
        
        //if (ret.length() != LARGO_REGISTRO) {
         //   throw new SerializadorException("Largo invalido registro tipo 2");
        //}

        return ret;
    }
    
    public String generarRegistroTipo3() throws SerializadorException {

        // Tipo de registro y numero de formulario.
        String ret = "311914";

        // Codigo de presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // RUT declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());
        
        // Total de casos informados.
        ret += String.format("%010d", getCantidadCasosInformados());
        
        
        // Retiro Monto Nominal
        //ret += String.format("%018f", registroResumen.getSumaRetiroInversion54BISMontoNominal()).replace(".", "").replace(" ","0");
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getSumaRetiroInversion54BISMontoNominal()).replace(".", ""))).replace(" ","0"); 
        
        // Retiro Interes Positivo
        //ret += String.format("%018f", registroResumen.getSumaRetiroInversion54BISInteresPositivo()).replace(".", "").replace(" ","0");
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getSumaRetiroInversion54BISInteresPositivo()).replace(".", ""))).replace(" ","0"); 

        // Retiro Interes Negativo
        //ret += String.format("%018f", registroResumen.getSumaRetiroInversion54BISInteresNegativo()).replace(".", "").replace(" ","0");
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getSumaRetiroInversion54BISInteresNegativo()).replace(".", ""))).replace(" ","0"); 
        

        // Fecha de presentacion.
        ret += getContexto().getFechaPresentacion();

        // RUT del representante legal.
        ret += Serializador.serializarRut(getContexto().getRutRepresentante());

        // Relleno.
        ret += Serializador.espacios(99);

       // if (ret.length() != LARGO_REGISTRO) {
        //    throw new SerializadorException("Largo invalido registro tipo 3");
       // }

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
