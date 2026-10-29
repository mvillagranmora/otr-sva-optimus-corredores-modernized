package cl.bcs.normativa.generacion;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cl.bcs.normativa.data.Registro;
import cl.bcs.normativa.registros.RegistroDJ1894;

public final class GeneradorDJ1894 extends Generador {
	
	private static final int LARGO_REGISTRO = 169;
	private RegistroDJ1894 registroResumen;
    private long totalRegistros;
    
    public GeneradorDJ1894(final ContextoGeneracion contexto)
            throws SQLException, IOException, SerializadorException {
        super(contexto);
        this.totalRegistros = 0;
        this.registroResumen = new RegistroDJ1894();
    }
    
    private void actualizarAcumuladores(final RegistroDJ1894 registroDJ1894) {
        this.contarCasosInformados();
        this.totalRegistros += 1;
        registroResumen.acumular(registroDJ1894);
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
        ret += String.format("%04d", getContexto().getAnoTributario()+1L);

        // Numero de formulario.
        ret += "1894";

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
        ret += Serializador.espacios(83);
        
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
   
        // Numero de formulario.
        ret += "1894";

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
        
        // Relleno.
        ret += Serializador.espacios(88);
       
        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 1");
        }
		
        return ret;
    }
    
    public String generarRegistroTipo1Complemento() throws SerializadorException {

        String ret = "12";
        
        //Tipo registro
        //ret += "12";
        
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
        ret += String.format("5602%07d", getContexto().getTelefonoDeclaracion() % 1000000);

        // Relleno.
        // ret += Serializador.espacios(1);

        // Codigo de empresa.
        ret += String.format("%02d", getContexto().getCodigoEmpresa());

        // Numero de cliente.
        ret += String.format("%04d", getContexto().getNumeroCliente());

        // Unidad, caja y paquete.
        ret += Serializador.ceros(13);

        // Relleno.
        ret += Serializador.espacios(46);

        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 1 Complemento");
        }
		
        return ret;
    }
    
    public String generarRegistroTipo2(final Registro registro)
            throws SerializadorException {
    	
    	final RegistroDJ1894 registroDJ1894 = new RegistroDJ1894(registro);
        actualizarAcumuladores(registroDJ1894);

        String fechaInversion;
        String fechaRescate;

        // Tipo de Registro y Numero de Formulario.
        String ret = "21894";
       
        // Codigo de Presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de Declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // Tipo de Declaracion.
        ret += getContexto().getTipoDeclaracion().getCodigo();

        // RUT Declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());



        // Tipo Declarante.
        ret += String.format("%1s", registroDJ1894.getTipoDeclarante());
        
        // Rut Emisor Instrumento
        if (registroDJ1894.getRutEmisor().length() > 1){
        	ret += Serializador.serializarRut(registroDJ1894.getRutEmisor());
        }else{
        	 ret += Serializador.ceros(9);
        };

        // Rut Participe.
        ret += Serializador.serializarRut(registroDJ1894.getRutParticipe());

        // Rut Fondo Mutuo.
        ret += Serializador.serializarRut(registroDJ1894.getRutFondo());

        // Run Fondo Mutuo.
        ret += Serializador.serializarRut(registroDJ1894.getRunFondo());

        // Tipo Operacion      
        String tipoOperacion = String.format("%2s", registroDJ1894.getTipoOperacion()).replace(" ","0");
        String restoOperacion = "0";
        ret += tipoOperacion.length() < 2 ? restoOperacion + tipoOperacion : tipoOperacion;

        //Seccion B1
        // Fecha Inversion    
        //ret += Serializador.serializar(registroDJ1894.getFechaInversion());
        fechaInversion = Serializador.serializar(registroDJ1894.getFechaInversion());
        String fechaValidaInversion = "00000000";
        ret += fechaInversion.equals("01011900") ? fechaValidaInversion : fechaInversion;
        
        // Inversion Nominal        
        //ret += String.format("%015f", registroDJ1894.getMontoNominalInversion()); 
        ret += String.format( "%15s" , (String.format("%015.0f", registroDJ1894.getMontoNominalInversionActualizado()).replace(".", ""))).replace(" ","0");


        //Seccion B2
        // Fecha de Rescate
        //ret += Serializador.serializar(registroDJ1894.getFechaRescate());
        fechaRescate = Serializador.serializar(registroDJ1894.getFechaRescate());
        String fechaValidaRescate = "00000000";
        ret += fechaRescate.equals("01011900") ? fechaValidaRescate : fechaRescate;
        
        // Rescate Nominal       
        //ret += String.format("%015f", registroDJ1894.getMontoNominalRescate());
        ret += String.format( "%15s" , (String.format("%015.0f", registroDJ1894.getMontoNominalRescate()).replace(".", ""))).replace(" ","0");
        
        // Mayor Valor Actualizado
        //ret += (String.format("%015f", registroDJ1894.getMayorValorActualizado()));
        ret += String.format( "%15s" , (String.format("%015.0f", registroDJ1894.getMayorValorActualizado()).replace(".", ""))).replace(" ","0");
        
        // Menor Valor Actualizado      
        //ret += (String.format("%015f", registroDJ1894.getMenorValorActualizado()));
        ret += String.format( "%15s" , (String.format("%015.0f", registroDJ1894.getMenorValorActualizado()).replace(".", ""))).replace(" ","0");
        

        //Seccion B3
        // Rut Sociedad Administradora Receptor
        ret += registroDJ1894.getRutAdminitradoraFondoReceptor().isEmpty() ? Serializador.serializarRut(getContexto().getRutDeclarante()) : Serializador.serializarRut(registroDJ1894.getRutAdminitradoraFondoReceptor());
        
        // Monto Reinvertido a valor de la liquidacion     
        //ret += String.format("%015f", registroDJ1894.getMontoReinvertido());
        ret += String.format( "%15s" , (String.format("%015.0f", registroDJ1894.getMontoReinvertido()).replace(".", ""))).replace(" ","0");
        
        // Porcentaje del activo        
        //ret += String.format("%03d", Double.valueOf(registroDJ1894.getPorcentajePromedioAnualAcciones()).intValue());
        //ret += String.format("%03d", String.valueOf(registroDJ1894.getPorcentajePromedioAnualAcciones()).split(".").length == 2 ? Integer.valueOf(String.valueOf(registroDJ1894.getPorcentajePromedioAnualAcciones()).split(".")[1]) : 0);
        
        // Numero Certificado         
        ret += String.format("%07d", registroDJ1894.getNumeroCertificado());

        
        //Relleno.
        //ret += Serializador.espacios(9);
        
        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 2");
        }

        return ret;
    }
    
    public String generarRegistroTipo3() throws SerializadorException {

        // Tipo de registro y numero de formulario.
        String ret = "311894";

        // Codigo de presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // RUT declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());
        
        // Inversion Nominal
        //ret += String.format("%18f", registroResumen.getSumaInversionNominal());
  		ret += String.format( "%18s" , (String.format("%018.0f", registroResumen.getSumaInversionNominal()).replace(".", ""))).replace(" ","0");
        
        // Rescate Nominal
        //ret += String.format("%18f", registroResumen.getSumaRescateNominal());
        ret += String.format( "%18s" , (String.format("%018.0f",  registroResumen.getSumaRescateNominal()).replace(".", ""))).replace(" ","0");
        
        // Mayor Valor Actualizado
        //ret += String.format("%18f", registroResumen.getSumaMayorValor());
        ret += String.format( "%18s" , (String.format("%018.0f",  registroResumen.getSumaMayorValor()).replace(".", ""))).replace(" ","0");

        // Menor Valor Actualizado
        //ret += String.format("%18f", registroResumen.getSumaMenorValor());
        ret += String.format( "%18s" , (String.format("%018.0f",  registroResumen.getSumaMenorValor()).replace(".", ""))).replace(" ","0");
        
        // Menor Valor Actualizado
        //ret += String.format("%18f", registroResumen.getSumaMontoReinvertido());
        ret += String.format( "%18s" , (String.format("%018.0f",  registroResumen.getSumaMontoReinvertido()).replace(".", ""))).replace(" ","0");
        
        // Total de casos informados.
        ret += String.format("%07d", getCantidadCasosInformados());        
        
        // Fecha de presentacion.
        ret += getContexto().getFechaPresentacion();

        // RUT del representante legal.
        ret += Serializador.serializarRut(getContexto().getRutRepresentante());

        // Relleno.
        ret += Serializador.espacios(31);

        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 3");
        }

        return ret;
    }

    
    @Override
    public void generarResumen() throws SerializadorException {
        println(generarRegistroTipo3());
        //println("");
    }
    
    private long getCantidadTotalRegistros() {
        return getCantidadCasosInformados() + 4;
    }
}
