package cl.bcs.normativa.generacion;

import java.io.IOException;
import java.sql.SQLException;

import cl.bcs.normativa.data.Registro;
import cl.bcs.normativa.registros.RegistroDJ1899;

public final class GeneradorDJ1899 extends Generador {
	
	private static final int LARGO_REGISTRO = 216;
	private final RegistroDJ1899 registroResumen;
    private long totalRegistros;
	private double acumuladorMontoIncisoPrimeroActivo;
    private double acumuladorMontoIncisoPrimeroPensionado;
    
    public GeneradorDJ1899(final ContextoGeneracion contexto)
            throws SQLException, IOException, SerializadorException {
        super(contexto);
        this.totalRegistros = 0;
        this.registroResumen = new RegistroDJ1899();
		this.acumuladorMontoIncisoPrimeroActivo=0;
		this.acumuladorMontoIncisoPrimeroPensionado=0;
    }
    
    private void actualizarAcumuladores(final RegistroDJ1899 registroDJ1899) {
        this.contarCasosInformados();
        this.totalRegistros += 1;

		if(registroDJ1899.getActivoPensionado().equals("1")){
			this.acumuladorMontoIncisoPrimeroActivo += Math.round(registroDJ1899.getMontoRetiroPorAhorrosIncisoPrimero());
		}
		else{
			this.acumuladorMontoIncisoPrimeroPensionado += Math.round(registroDJ1899.getMontoRetiroPorAhorrosIncisoPrimero());
		}

        registroResumen.acumular(registroDJ1899);
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
        ret += "1899";

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
        ret += Serializador.espacios(130);



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
        ret += "1899";

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

        // Relleno.
        ret += Serializador.espacios(1);

        // Codigo certificacion
    		// Codigo de empresa.
    		ret += String.format("%02d", getContexto().getCodigoEmpresa());

    		// Numero de cliente.
    		ret += String.format("%04d", getContexto().getNumeroCliente());

    	//localizacion del docto	
            // Unidad, caja y paquete.
            ret += Serializador.ceros(13);
        
        //Periodo tributario
        ret += String.format("%04d", getContexto().getAnoTributario()) + "00";
            
        // Relleno.
        ret += Serializador.espacios(7);

        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 1");
        }
		
        return ret;
    }
    
    public String generarRegistroTipo2(final Registro registro)
            throws SerializadorException {
    	
    	final RegistroDJ1899 registroDJ1899 = new RegistroDJ1899(registro);
        actualizarAcumuladores(registroDJ1899);
        String numberD;
        String numberE;
    	
        // Tipo de Registro y Numero de Formulario.
        String ret = "21899";

        // Codigo de Presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de Declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // Tipo de Declaracion.
        ret += getContexto().getTipoDeclaracion().getCodigo();

        // RUT Declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());

        // Rut Afiliado o Pensionado
        ret += Serializador.serializarRut(registroDJ1899.getRutCliente());
        
        // Activo o Pensionado.
        ret += String.format(registroDJ1899.getActivoPensionado());
		//ret += "1";

        // Rut Empleador
		if(registroDJ1899.getRutEmpleador().length() > 0)
			ret += Serializador.serializarRut(registroDJ1899.getRutEmpleador());
		else
			ret += Serializador.ceros(9);
       
        
        // Depósitos Convenidos con el empleador en UF
		numberE = String.valueOf(registroDJ1899.getDepositoEmpleador());
        ret += String.format("%10s", numberE.substring(0, numberE.indexOf("."))).replace(".", "").replace(" ", "0");
		ret += String.format("%3s", String.format("%.3f", registroDJ1899.getDepositoEmpleador()%1).substring(2));
		//ret += String.format("%3s", (String.format("%.3f", registroDJ1899.getDepositoEmpleador()%1).substring(2))).replace(" ","0");
		  
        //Ahorro Previsional voluntario Colectivo
			//Aporte Empleador
			numberE = String.valueOf(registroDJ1899.getAporteEmpleador());
            ret += String.format("%10s", numberE.substring(0, numberE.indexOf("."))).replace(".", "").replace(" ", "0");
			ret += String.format("%3s", String.format("%.3f", registroDJ1899.getAporteEmpleador()%1).substring(2));

			//Traspaso Empleador
			numberE = String.valueOf(registroDJ1899.getTraspasoEmpleador());
            ret += String.format("%10s", numberE.substring(0, numberE.indexOf("."))).replace(".", "").replace(" ", "0");
			ret += String.format("%3s", String.format("%.3f", registroDJ1899.getTraspasoEmpleador()%1).substring(2));

			//Retiro Empleador
			ret += String.format( "%10s" , (String.format("%010.0f", registroDJ1899.getRetiroEmpleadorActualizado()).replace(".", ""))).replace(" ","0");
			
			//Monto Retención 15%
			ret += String.format( "%10s" , (String.format("%010.0f", registroDJ1899.getRetiroEmpleadorActualizado()).replace(".", ""))).replace(" ","0");
        
	    //Ahorros del trabajador acogidos al inciso primero Art.42
	        //Modalidad Indirecta vía Empleador
			numberE = String.valueOf(registroDJ1899.getAhorrosModalidadIndirectaEmpleador());
            ret += String.format("%10s", numberE.substring(0, numberE.indexOf("."))).replace(".", "").replace(" ", "0");
			ret += String.format("%3s", String.format("%.3f", registroDJ1899.getAhorrosModalidadIndirectaEmpleador()%1).substring(2));

	        //Modalidad Directa vía Trabajador
			numberE = String.valueOf(registroDJ1899.getAhorrosModalidadDirectaTrabajador());
            ret += String.format("%10s", numberE.substring(0, numberE.indexOf("."))).replace(".", "").replace(" ", "0");
			ret += String.format("%3s", String.format("%.3f", registroDJ1899.getAhorrosModalidadDirectaTrabajador()%1).substring(2));


	    //Depositos de ahorro Previsional Voluntario (DAPV) o Cotizacioes acogidos al Inciso primero del Art. 42 BIS de la LIR
	    //DAPV o ctizaciones Voluntarias enterado en calidad del trabajador Dependiente del Art 42 de la LIR en UF
	        //Modalidad Indirecta Via Empleador
	    	numberE = String.valueOf(registroDJ1899.getCotizacionesModalidadIndirectaEmpleador());
            ret += String.format("%10s", numberE.substring(0, numberE.indexOf("."))).replace(".", "").replace(" ", "0");
			ret += String.format("%3s", String.format("%.3f", registroDJ1899.getCotizacionesModalidadIndirectaEmpleador()%1).substring(2));

	       	//Modalidad Directa Via Trabajador
			numberE = String.valueOf(registroDJ1899.getCotizacionesModalidadDirectaTrabajador());
            ret += String.format("%10s", numberE.substring(0, numberE.indexOf("."))).replace(".", "").replace(" ", "0");
			ret += String.format("%3s", String.format("%.3f", registroDJ1899.getCotizacionesModalidadDirectaTrabajador()%1).substring(2));
	    	
	    //DAPV o cotizaciones Voluntarias enteradas en calidad de trabajador Independiente del Art 42 de la LIR en UF
	        //DAPV o cotizaciones Voluntarias
	    	numberE = String.valueOf(registroDJ1899.getCotizacionesTrabajadorIndependiente());
            ret += String.format("%10s", numberE.substring(0, numberE.indexOf("."))).replace(".", "").replace(" ", "0");
			ret += String.format("%3s", String.format("%.3f", registroDJ1899.getCotizacionesTrabajadorIndependiente()%1).substring(2));
	    
	    //Monto de los retiros efectuados con cargo a los ahorros previsionales voluntarios realizados, Actualizados
	        //Monto del Retiro proveniente de Ahorros Acogidos a Beneficio Tributario del inciso primero del Art. 42
	        ret += String.format( "%12s" , (String.format("%012.0f", registroDJ1899.getMontoRetiroPorAhorrosIncisoPrimero()).replace(".", ""))).replace(" ","0");
	        
	        //Retencion del impuesto de 15% practicada sobre retiros
			ret += String.format("%12s", (String.format("%012.0f", registroDJ1899.getRetencionImpuestoSobreRetiros()).replace(".", ""))).replace(" ","0");

	        //Monto del Retiro proveniente de Ahorros Acogidos a Beneficio Tributario del inciso segundo del Art. 42
	        ret += String.format( "%12s" , (String.format("%012.0f", registroDJ1899.getMontoRetiroPorAhorrosIncisoSegundo()).replace(".", ""))).replace(" ","0");
         
	        //ret += "0000000";
	        //ret += "0000000";
	    ret += String.format("%07d", registroDJ1899.getNumeroCertificadoTrabajador());    
		ret += String.format("%07d", registroDJ1899.getNumeroCertificadoEmpleador());
	    
	    /* if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 2");
        }
    	*/  	
        return ret;
    }
    
    public String generarRegistroTipo3() throws SerializadorException {
    	String numberD;
    	String numberE;
        // Tipo de registro y numero de formulario.
        String ret = "311899";

        // Codigo de presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // RUT declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());
        
        // Depósitos convenidos con el empleador en UF
		numberE = String.valueOf(registroResumen.getSumaDepositoEmpleador());
        ret += String.format("%15s", numberE.substring(0, numberE.indexOf("."))).replace(".", "").replace(" ", "0");
		ret += String.format("%3s", String.format("%.3f", registroResumen.getSumaDepositoEmpleador()%1).substring(2));

		//Ahorro Previsional voluntario Colectivo
	        //Aporte Empleador
			numberE = String.valueOf(registroResumen.getSumaAporteEmpleador());
            ret += String.format("%15s", numberE.substring(0, numberE.indexOf("."))).replace(".", "").replace(" ", "0");
			ret += String.format("%3s", String.format("%.3f", registroResumen.getSumaAporteEmpleador()%1).substring(2));
	        
	        //Traspaso Empleador (Parte Entera)
			numberE = String.valueOf(registroResumen.getSumaTraspasoEmpleador());
            ret += String.format("%15s", numberE.substring(0, numberE.indexOf("."))).replace(".", "").replace(" ", "0");
			ret += String.format("%3s", String.format("%.3f", registroResumen.getSumaTraspasoEmpleador()%1).substring(2));
	        
	        //Retiro Empleador
	        ret += String.format( "%15s" , (String.format("%015.0f", registroResumen.getSumaRetiroEmpleadorActualizado()).replace(".", ""))).replace(" ","0");
	        
	        //Monto Retención 15%
	        ret += String.format( "%15s" , (String.format("%015.0f", registroResumen.getSumaMontoRetencionActualizado()).replace(".", ""))).replace(" ","0");
        
        //Ahorros del trabajador acogidos al inciso primero Art.42
	        //Modalidad Indirecta vía Empleador (parte Entera)
			numberE = String.valueOf(registroResumen.getSumaAhorrosModalidadIndirectaEmpleador());
            ret += String.format("%15s", numberE.substring(0, numberE.indexOf("."))).replace(".", "").replace(" ", "0");
			ret += String.format("%3s", String.format("%.3f", registroResumen.getSumaAhorrosModalidadIndirectaEmpleador()%1).substring(2));
	        
	        //Modalidad Directa vía Trabajador (parte Entera)
			numberE = String.valueOf(registroResumen.getSumaAhorrosModalidadDirectaTrabajador());
            ret += String.format("%15s", numberE.substring(0, numberE.indexOf("."))).replace(".", "").replace(" ", "0");
			ret += String.format("%3s", String.format("%.3f", registroResumen.getSumaAhorrosModalidadDirectaTrabajador()%1).substring(2));
	        
	    //Depositos de ahorro Previsional Voluntario (DAPV) o Cotizacioes acogidos al Inciso primero del Art. 42 BIS de la LIR
	    //DAPV o cotizaciones Voluntarias enterado en calidad del trabajador Dependiente del Art 42 de la LIR en UF
	        //Modalidad Indirecta Via Empleador(Parte Entera)
			numberE = String.valueOf(registroResumen.getSumaCotizacionesModalidadIndirectaEmpleador());
            ret += String.format("%15s", numberE.substring(0, numberE.indexOf("."))).replace(".", "").replace(" ", "0");
			ret += String.format("%3s", String.format("%.3f", registroResumen.getSumaCotizacionesModalidadIndirectaEmpleador()%1).substring(2));
	        
	        //Modalidad Directa Via Trabajador (Parte Entera)
			numberE = String.valueOf(registroResumen.getSumaCotizacionesModalidadDirectaTrabajador());
            ret += String.format("%15s", numberE.substring(0, numberE.indexOf("."))).replace(".", "").replace(" ", "0");
			ret += String.format("%3s", String.format("%.3f", registroResumen.getSumaCotizacionesModalidadDirectaTrabajador()%1).substring(2));
	        
	    //DAPV o cotizaciones Voluntarias enteradas en calidad de trabajador Independiente del Art 42 de la LIR en UF  
	        //DAPV o cotizaciones Voluntarias (Parte Entera)
			numberE = String.valueOf(registroResumen.getSumaCotizacionesTrabajadorIndependiente());
            ret += String.format("%15s", numberE.substring(0, numberE.indexOf("."))).replace(".", "").replace(" ", "0");
			ret += String.format("%3s", String.format("%.3f", registroResumen.getSumaCotizacionesTrabajadorIndependiente()%1).substring(2));
	        
        // Relleno.
        ret += Serializador.espacios(19);

        /* if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 3");
        }*/

        return ret;
    }
    
    public String generarRegistroTipo3Complemento() throws SerializadorException {

        // TipoRegistro.
        String ret = "32";
        
        //Monto de los retiros efectuados con cargo a los ahorros previsionales voluntarios realizados, Actualizados
		//Monto del Retiro proveniente de Ahorros Acogidos a Beneficio Tributario del inciso primero del Art. 42
		//ret += String.format("%15d", Double.valueOf(registroResumen.getSumaMontoRetiroPorAhorrosIncisoPrimero()).intValue());
		ret += String.format("%15s" , (String.format("%015.0f",acumuladorMontoIncisoPrimeroActivo).replace(".", ""))).replace(" ","0");
		
		ret += String.format("%15s" , (String.format("%015.0f",acumuladorMontoIncisoPrimeroPensionado).replace(".", ""))).replace(" ","0");
    
        	
	    //Retencion del impuesto de 15% practicada sobre retiros
	    //ret += String.format("%15d", Double.valueOf(registroResumen.getSumaRetencionImpuestoSobreRetiros()).intValue());
	    ret += String.format( "%15s" , (String.format("%015.0f", registroResumen.getSumaRetencionImpuestoSobreRetiros()).replace(".", ""))).replace(" ","0");
	    
	    //Monto del Retiro proveniente de Ahorros Acogidos a Beneficio Tributario del inciso segundo del Art. 42
	    //ret += String.format("%15d", Double.valueOf(registroResumen.getSumaMontoRetiroPorAhorrosIncisoSegundo()).intValue());
	    ret += String.format( "%15s" , (String.format("%015.0f", registroResumen.getSumaMontoRetiroPorAhorrosIncisoSegundo()).replace(".", ""))).replace(" ","0");

	    // Total de casos informados.
        ret += String.format("%07d", getCantidadCasosInformados());
        
        // Fecha de presentacion.
        ret += getContexto().getFechaPresentacion();

        // RUT del representante legal.
        ret += Serializador.serializarRut(getContexto().getRutRepresentante());
        
        ret += Serializador.espacios(129);
        
        //if (ret.length() != LARGO_REGISTRO) {
          //  throw new SerializadorException("Largo invalido registro tipo 3 Complemento");
        //}
		
        return ret;
    }
    
    @Override
    public void generarResumen() throws SerializadorException {
        println(generarRegistroTipo3());
        print(generarRegistroTipo3Complemento());
		println("");
    }
    
    private long getCantidadTotalRegistros() {
        return getCantidadCasosInformados() + 4;
    }
}
