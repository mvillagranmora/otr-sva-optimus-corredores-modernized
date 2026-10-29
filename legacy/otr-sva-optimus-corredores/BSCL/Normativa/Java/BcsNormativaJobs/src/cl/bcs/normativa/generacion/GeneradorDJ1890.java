package cl.bcs.normativa.generacion;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cl.bcs.normativa.data.Registro;
import cl.bcs.normativa.registros.RegistroDJ1890;

public final class GeneradorDJ1890 extends Generador {
	
	private static final int LARGO_REGISTRO = 172;
	private static final int LARGO_REGISTRO2 = 344;
	private RegistroDJ1890 registroResumen;
    private long totalRegistros;
    
    public GeneradorDJ1890(final ContextoGeneracion contexto)
            throws SQLException, IOException, SerializadorException {
        super(contexto);
        this.totalRegistros = 0;
        this.registroResumen = new RegistroDJ1890();
    }
    
    private void actualizarAcumuladores(final RegistroDJ1890 registroDJ1890) {
        this.contarCasosInformados();
        this.totalRegistros += 1;
        registroResumen.acumular(registroDJ1890);
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
        println(generarRegistroTipo1Complemento());
    }
    
    @Override
    public String generarRegistroTipo0() throws SerializadorException {

        // Tipo de registro.
        String ret = "0";
        
        // Ano tributario.
        ret += String.format("%04d", getContexto().getAnoTributario());

        // Numero de formulario.
        ret += "1890";

        // RUT declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());
        
        // Total de registros a transmitir.
        ret += String.format("%07d", getCantidadTotalRegistros());

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
        ret += Serializador.espacios(86);

        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 0");
        }
	
        return ret;
    }
    
    public String generarRegistroTipo1() throws SerializadorException {

        // Tipo de registro y precedencia.
        String ret = "11";
       
        // Ano tributario.
        ret += String.format("%04d", getContexto().getAnoTributario());
           
        // Numero de formulario.
        ret += "1890";

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

        // Relleno.
        ret += Serializador.espacios(56);
        
        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 1");
        }
		
        return ret;
    }
    
    public String generarRegistroTipo1Complemento() throws SerializadorException {

        String ret = "12";
        		
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
        ret += String.format("5602%07d", getContexto().getTelefonoDeclaracion() % 1000000);
        
        // Relleno.
        ret += Serializador.espacios(1);
        
        // Codigo de empresa.
        ret += String.format("%02d", getContexto().getCodigoEmpresa());

        // Numero de cliente.
        ret += String.format("%04d", getContexto().getNumeroCliente());
        
        // Unidad, caja y paquete.
        ret += Serializador.ceros(13);
        
        // Relleno.
        ret += Serializador.espacios(83);
        
        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 1");
        }
		
        return ret;
    }
    
    public String generarRegistroTipo2(final Registro registro)
            throws SerializadorException {
    	
    	final RegistroDJ1890 registroDJ1890 = new RegistroDJ1890(registro);
        actualizarAcumuladores(registroDJ1890);

        // Tipo de Registro y Numero de Formulario.
        String ret = "21890";
       
        // Codigo de Presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de Declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // Tipo de Declaracion.
        ret += getContexto().getTipoDeclaracion().getCodigo();
        	
        // RUT Declarante
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());
        
        // RUT inversionista.
        ret += Serializador.serializarRut(registroDJ1890.getRutInversionista());
        
        // Intereses Reales Depositos Positivo
        ret += String.format("%015d", Double.valueOf(registroDJ1890.getInteresesRealesDepositosPositivo()).intValue());
        
        // Intereses Reales Depositos Negativo
        ret += String.format("%015d", Double.valueOf(registroDJ1890.getInteresesRealesDepositosNegativo()).intValue());
        
        // Intereses Reales Mantencion Positivo
        ret += String.format("%015d", Double.valueOf(registroDJ1890.getInteresesRealesMantencionPositivo()).intValue());
        
        // Intereses Reales Mantencion Negativo
        ret += String.format("%015d", Double.valueOf(registroDJ1890.getInteresesRealesMantencionNegativo()).intValue());
        
        // Intereses Reales Operaciones Captacion Positivo
        ret += String.format("%015d", Double.valueOf(registroDJ1890.getInteresesRealesOperacionesCaptacionPositivo()).intValue());
        
        // Saldo Final Nominal
        ret += String.format("%015d", Double.valueOf(registroDJ1890.getInteresesRealesOperacionesCaptacionNegativo()).intValue());
        
        // Intereses Moneda Nacional Extranjera Nominal
        ret += String.format("%015d", Double.valueOf(registroDJ1890.getInteresesMonedaNacionalExtranjeraNominal()).intValue());
        
        // Impuesto Adicional Monda Nacional Extranjera Nominal
        ret += String.format("%015d", Double.valueOf(registroDJ1890.getImpuestoAdicionalMondaNacionalExtranjeraNominal()).intValue());
        
        // Numero Certificado
        ret += String.format("%7s", registroDJ1890.getNumeroCertificado());
        
        ret += Serializador.ceros(13);
        
        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 2");
        }

        return ret;
    }
    
    public String generarRegistroTipo3() throws SerializadorException {

        // Tipo de registro y numero de formulario.
        String ret = "311890";

        // Codigo de presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // RUT declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());
        
        ret += String.format("%018d", Double.valueOf(registroResumen.getSumaInteresesRealesDepositosPositivo()).intValue());
        
        
        ret += String.format("%018d", Double.valueOf(registroResumen.getSumaInteresesRealesDepositosNegativo()).intValue());
        
      
        ret += String.format("%018d", Double.valueOf(registroResumen.getSumaInteresesRealesMantencionPositivo()).intValue());
        
        
        ret += String.format("%018d", Double.valueOf(registroResumen.getSumaInteresesRealesMantencionPositivo()).intValue());
        
       
        ret += String.format("%018d", Double.valueOf(registroResumen.getSumaInteresesRealesOperacionesCaptacionPositivo()).intValue());
        
    
        ret += String.format("%018d", Double.valueOf(registroResumen.getSumaInteresesRealesOperacionesCaptacionNegativo()).intValue());
        
        
        ret += String.format("%018d", Double.valueOf(registroResumen.getSumaInteresesMonedaNacionalExtranjeraNominal()).intValue());
        
     
        ret += String.format("%018d", Double.valueOf(registroResumen.getSumaImpuestoAdicionalMondaNacionalExtranjeraNominal()).intValue());
        
        // Relleno.
        ret += Serializador.espacios(5);
        
        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 3");
        }

        return ret;
    }
    
    public String generarRegistroTipo3A() throws SerializadorException {

        // Tipo de registro y numero de formulario.
        String ret = "32";
        
        // Total de casos informados.
        ret += String.format("%07d", getCantidadCasosInformados());
        
        // Fecha de presentacion.
        ret += getContexto().getFechaPresentacion();
        
        // RUT del representante legal.
        ret += Serializador.serializarRut(getContexto().getRutRepresentante());
        
        // Relleno.
        ret += Serializador.espacios(145);
        
        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 3");
        }

        return ret;
    }
    
    @Override
    public void generarResumen() throws SerializadorException {
    	println(generarRegistroTipo3());
    	print(generarRegistroTipo3A());
    }
    
    private long getCantidadTotalRegistros() {
        return getCantidadCasosInformados() + 3;
    }
}
