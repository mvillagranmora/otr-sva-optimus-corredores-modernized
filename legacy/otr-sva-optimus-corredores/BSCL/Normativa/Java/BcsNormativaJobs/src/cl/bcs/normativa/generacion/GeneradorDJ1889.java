package cl.bcs.normativa.generacion;

import java.io.IOException;
import java.sql.SQLException;

import cl.bcs.normativa.data.Registro;
import cl.bcs.normativa.registros.RegistroDJ1889;

public final class GeneradorDJ1889 extends Generador {
	
	private static final int LARGO_REGISTRO = 113;
	private final RegistroDJ1889 registroResumen;
    private long totalRegistros;
    
    public GeneradorDJ1889(final ContextoGeneracion contexto)
            throws SQLException, IOException, SerializadorException {
        super(contexto);
        this.totalRegistros = 0;
        this.registroResumen = new RegistroDJ1889();
    }
    
    private void actualizarAcumuladores(final RegistroDJ1889 registroDJ1889) {
        this.contarCasosInformados();
        this.totalRegistros += 1;
        registroResumen.acumular(registroDJ1889);
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
        println(generarRegistroTipo1Complemento2());
    }
    
    @Override
    public String generarRegistroTipo0() throws SerializadorException {

        // Tipo de registro.
        String ret = "0";
        
        // Ano tributario.
        ret += String.format("%04d", getContexto().getAnoTributario()+1L);

        // Numero de formulario.
        ret += "1889";

        // RUT declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());
        
        // Total de registros a transmitir.
        ret += String.format("%07d", getCantidadTotalRegistros());

     	// Codigo Certificacion
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
        ret += Serializador.espacios(27);

        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 0");
        }
	
        return ret;
    }
    
    public String generarRegistroTipo1() throws SerializadorException {

        // Tipo de registro y precedencia.
        String ret = "11";
       
        // Ano tributario.
        ret += String.format("%04d", getContexto().getAnoTributario()+1L);
   
        // Mes tributario.
        //ret += String.format("%02d", getContexto().getMesTributario());
        
        // Numero de formulario.
        ret += "1889";

        // Codigo de presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // Tipo de declaracion.
        ret += getContexto().getTipoDeclaracion().getCodigo();

        //Rectificadoria
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

         // Nombre o Razon Social
        String RazonSocial=getContexto().getRazonSocial();
        RazonSocial = Serializador.removerCaracteresEspeciales(RazonSocial);
        ret += String.format("%-30s",  (RazonSocial).length() > 30 ?  (RazonSocial).substring(0,30) :  (RazonSocial));
        
        // Relleno.
        ret += Serializador.espacios(32);

        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 1");
        }
		
        return ret;
    }
    
    
    public String generarRegistroTipo1Complemento() throws SerializadorException {

        // TipoRegistro.
        String ret = "12";
        
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

        // Relleno.
        ret += Serializador.espacios(1);
        
        // Codigo certificacion
        	// Codigo de empresa.
        	ret += String.format("%02d", getContexto().getCodigoEmpresa());

        	// Numero de cliente.
        	ret += String.format("%04d", getContexto().getNumeroCliente());

        // Relleno.	
        //ret += Serializador.ceros(2);
        ret += Serializador.espacios(2);
        
        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 1 Complemento");
        }
		
        return ret;
    }
    
    public String generarRegistroTipo1Complemento2() throws SerializadorException {

        // TipoRegistro.
        String ret = "13";
        
        //localizacion del docto	
        // Unidad, caja y paquete.
        ret += Serializador.ceros(13);
        
        // Relleno.	
        //ret += Serializador.ceros(98);
        ret += Serializador.espacios(98);
        
        
        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 1 Complemento 2");
        }
		
        return ret;
    }
    
    public String generarRegistroTipo2(final Registro registro)
            throws SerializadorException {
    	
    	final RegistroDJ1889 registroDJ1889 = new RegistroDJ1889(registro);
        actualizarAcumuladores(registroDJ1889);

        // Tipo de Registro y Numero de Formulario.
        String ret = "21889";
        
        // Codigo de Presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de Declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // Tipo de Declaracion.
        ret += getContexto().getTipoDeclaracion().getCodigo();

        // RUT Declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());

        
        // Rut Afiliado
        ret += Serializador.serializarRut(registroDJ1889.getRutCliente());
        
        // Tipo Ahorro.
        ret += String.format("%1d", registroDJ1889.getTipoAhorro());
       
        // Retiros efectuados Ctas.Ahorro Voluntario
	        // Monto Anual Nominal de los Retiros
	        //ret += String.format("%12d", Double.valueOf(registroDJ1889.getMontoNominal()).intValue()); CGO
	        ret += String.format( "%12s" , (String.format("%012.0f", registroDJ1889.getMontoNominal()).replace(".", ""))).replace(" ","0");
	        
	    // Renta Nominal Anual det. sobre los Retiros
	    //ret += String.format("%12d", Double.valueOf(registroDJ1889.getRentabilidadPositiva()).intValue());CGO
	    //ret += String.format("%12d", Double.valueOf(registroDJ1889.getRentabilidadNegativa()).intValue());CGO

	    ret += String.format( "%12s" , (String.format("%012.0f", registroDJ1889.getRentabilidadPositiva()).replace(".", ""))).replace(" ","0");
	    ret += String.format( "%12s" , (String.format("%012.0f", registroDJ1889.getRentabilidadNegativa()).replace(".", ""))).replace(" ","0");	    
        
        // Renta Anual Actualizada
	    //ret += String.format("%12d", Double.valueOf(registroDJ1889.getRentabilidadActualizadaPositiva()).intValue());CGO
	    //ret += String.format("%12d", Double.valueOf(registroDJ1889.getRentabilidadActualizadaNegativa()).intValue());CGO
	    
	    ret += String.format( "%12s" , (String.format("%012.0f", registroDJ1889.getRentabilidadActualizadaPositiva()).replace(".", ""))).replace(" ","0");
	    ret += String.format( "%12s" , (String.format("%012.0f", registroDJ1889.getRentabilidadActualizadaNegativa()).replace(".", ""))).replace(" ","0");	    
	        
	    // Número de Certificado
	    ret += String.format("%07d", registroDJ1889.getNumeroCertificado());
         
	    //localizacion del docto	
        // Unidad, caja y paquete.
        ret += Serializador.ceros(13);
        
        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 2, Rut: " + registroDJ1889.getRutCliente() + " ");
            
        }
        

        return ret;
    }
    
    public String generarRegistroTipo3() throws SerializadorException {

        // Tipo de registro y numero de formulario.
        String ret = "311889";

        // Codigo de presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // RUT declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());
        
        // Total Retiros efectuados Ctas.Ahorro Voluntario
        //ret += String.format("%15d", Double.valueOf(registroResumen.getSumaMontoNominal()).intValue());CGO
        ret += String.format( "%15s" , (String.format("%015.0f", registroResumen.getSumaMontoNominal()).replace(".", ""))).replace(" ","0");        
        
        // Renta Nominal Anual det. sobre los Retiros
        //ret += String.format("%15d", Double.valueOf(registroResumen.getSumaRentabilidadPositiva()).intValue());CGO
        //ret += String.format("%15d", Double.valueOf(registroResumen.getSumaRentabilidadNegativa()).intValue());CGO
        
        ret += String.format( "%15s" , (String.format("%015.0f", registroResumen.getSumaRentabilidadPositiva()).replace(".", ""))).replace(" ","0");
        ret += String.format( "%15s" , (String.format("%015.0f", registroResumen.getSumaRentabilidadNegativa()).replace(".", ""))).replace(" ","0");        
        
        // Renta Anual Actualizada
        //ret += String.format("%15d", Double.valueOf(registroResumen.getSumaRentabilidadActualizadaPositiva()).intValue());CGO
        //ret += String.format("%15d", Double.valueOf(registroResumen.getSumaRentabilidadActualizadaNegativa()).intValue());CGO

        ret += String.format( "%15s" , (String.format("%015.0f", registroResumen.getSumaRentabilidadActualizadaPositiva()).replace(".", ""))).replace(" ","0");
        ret += String.format( "%15s" , (String.format("%015.0f", registroResumen.getSumaRentabilidadActualizadaNegativa()).replace(".", ""))).replace(" ","0");        
        
        // Total de casos informados.
        ret += String.format("%07d", getCantidadCasosInformados());
        
        // Relleno.
        ret += Serializador.espacios(8);

        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 3");
        }

        return ret;
    }
    
    public String generarRegistroTipo3Complemento() throws SerializadorException {

        // TipoRegistro.
        String ret = "32";
        
        // Fecha de presentacion.
        ret += getContexto().getFechaPresentacion();

        // RUT del representante legal.
        ret += Serializador.serializarRut(getContexto().getRutRepresentante());
        
        //espacios
        ret += Serializador.espacios(93);
        
        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 3 Complemento");
        }
		
        return ret;
    }
    
    @Override
    public void generarResumen() throws SerializadorException {
        println(generarRegistroTipo3());
        print(generarRegistroTipo3Complemento());
        println("");
    }
    
    private long getCantidadTotalRegistros() {
        return getCantidadCasosInformados() + 6;
    }
}
