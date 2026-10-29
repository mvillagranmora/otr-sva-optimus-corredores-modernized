package cl.bcs.normativa.generacion;

import java.io.IOException;
import java.sql.SQLException;

import cl.bcs.normativa.data.Registro;
import cl.bcs.normativa.registros.RegistroDJ1871;

public final class GeneradorDJ1871 extends Generador {
	
	private static final int LARGO_REGISTRO = 203;
	private final RegistroDJ1871 registroResumen;
    private long totalRegistros;
    
    public GeneradorDJ1871(final ContextoGeneracion contexto)
            throws SQLException, IOException, SerializadorException {
        super(contexto);
        this.totalRegistros = 0;
        this.registroResumen = new RegistroDJ1871();
    }
    
    private void actualizarAcumuladores(final RegistroDJ1871 registroDJ1871) {
        this.contarCasosInformados();
        this.totalRegistros += 1;
        registroResumen.acumular(registroDJ1871);
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
        ret += "1871";

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
        ret += Serializador.espacios(117);

        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 0");
        }
	
        return ret;
    }
    
    public String generarRegistroTipo1() throws SerializadorException {

        // Tipo de registro y precedencia.
        String ret = "11";
       
        // Ano tributario.
        ret += String.format("%04d", getContexto().getAnoTributario() + 1L);
   
        // Mes tributario.
        //ret += String.format("%02d", getContexto().getMesTributario());
        
        // Numero de formulario.
        ret += "1871";

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

        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 1");
        }
		
        return ret;
    }
    
    public String generarRegistroTipo2(final Registro registro)
            throws SerializadorException {
    	
    	final RegistroDJ1871 registroDJ1871 = new RegistroDJ1871(registro);
        actualizarAcumuladores(registroDJ1871);

        // Tipo de Registro y Numero de Formulario.
        String ret = "21871";
        
        // Codigo de Presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de Declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // Tipo de Declaracion.
        ret += getContexto().getTipoDeclaracion().getCodigo();

        // RUT Declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());

        
        // Rut Trabajador
        ret += Serializador.serializarRut(registroDJ1871.getRutCliente());
        
        // Ano Ahorro.
        ret += String.format("%04d", registroDJ1871.getAnoAhorro());
       
        // Ahorros acogidos al inciso segundo del Art 42 bis de la LIR
	        // Ahorro previsional voluntario colectivo (ahorro trabajador)
	        //ret += String.format("%12d", Double.valueOf(registroDJ1871.getAhorrosTrabajador()).intValue());
	        ret += String.format( "%12s" , (String.format("%012.0f", registroDJ1871.getAhorrosTrabajador()).replace(".", ""))).replace(" ","0");
	        
	        // Deposito ahorro previsional voluntario
	        //ret += String.format("%12d", Double.valueOf(registroDJ1871.getAhorrosDeposito()).intValue());
	        ret += String.format( "%12s" , (String.format("%012.0f", registroDJ1871.getAhorrosDeposito()).replace(".", ""))).replace(" ","0");
	        
	        // Cotizaciones voluntarias
	        //ret += String.format("%12d", Double.valueOf(registroDJ1871.getAhorrosCotizaciones()).intValue());
	        ret += String.format( "%12s" , (String.format("%012.0f", registroDJ1871.getAhorrosCotizaciones()).replace(".", ""))).replace(" ","0");
        
        // Retiros correspondientes a depósitos realizados en el periodo que se informa acogidos al inciso segundo del Art 42 de la LIR
	        // Ahorro previsional voluntario colectivo (ahorro trabajador)
	        //ret += String.format("%12d", Double.valueOf(registroDJ1871.getRetirosTrabajador()).intValue());
	        ret += String.format( "%12s" , (String.format("%012.0f", registroDJ1871.getRetirosTrabajador()).replace(".", ""))).replace(" ","0");
	        
	        // Deposito ahorro previsional voluntario
	        //ret += String.format("%12d", Double.valueOf(registroDJ1871.getRetirosDeposito()).intValue());
	        ret += String.format( "%12s" , (String.format("%012.0f", registroDJ1871.getRetirosDeposito()).replace(".", ""))).replace(" ","0");
	        
	        // Cotizaciones voluntarias
	        //ret += String.format("%12d", Double.valueOf(registroDJ1871.getRetirosCotizaciones()).intValue());
	        ret += String.format( "%12s" , (String.format("%012.0f", registroDJ1871.getRetirosCotizaciones()).replace(".", ""))).replace(" ","0");
         
        // Relleno.
        ret += Serializador.espacios(95);
        
        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 2");
        }

        return ret;
    }
    
    public String generarRegistroTipo3() throws SerializadorException {

        // Tipo de registro y numero de formulario.
        String ret = "311871";

        // Codigo de presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // RUT declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());
        
        // Total ahorros acogidos al inciso segundo del Art 42 BIS de la LIR
        //ret += String.format("%15d", Double.valueOf(registroResumen.getSumaAhorros()).intValue());
        ret += String.format( "%15s" , (String.format("%015.0f", registroResumen.getSumaAhorros()).replace(".", ""))).replace(" ","0");
        
        // Total retiros correspondientes a depositos realizados en el periodo que se informa acogidos al inciso segundo del Art 42 Bis de la LIR
       // ret += String.format("%15d", Double.valueOf(registroResumen.getSumaRetiros()).intValue());
        ret += String.format( "%15s" , (String.format("%015.0f", registroResumen.getSumaRetiros()).replace(".", ""))).replace(" ","0");
        
        // Total de casos informados.
        ret += String.format("%07d", getCantidadCasosInformados());
        
        // Fecha de presentacion.
        ret += getContexto().getFechaPresentacion();

        // RUT del representante legal.
        ret += Serializador.serializarRut(getContexto().getRutRepresentante());

        // Relleno.
        ret += Serializador.espacios(125);

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
