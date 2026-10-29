package cl.bcs.normativa.generacion;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import org.apache.commons.lang3.StringUtils;

import cl.bcs.normativa.data.Registro;
import cl.bcs.normativa.registros.RegistroDJ1862;

public final class GeneradorDJ1862 extends Generador {
	
	private static final int LARGO_REGISTRO = 220;
	private RegistroDJ1862 registroResumen;
    private long totalRegistros;
    private double sumaMontoEnPesosPositivo;
    private double sumaMontoEnPesosNegativo;
    
    
    public GeneradorDJ1862(final ContextoGeneracion contexto)
            throws SQLException, IOException, SerializadorException {
        super(contexto);
        this.totalRegistros = 0;
        this.registroResumen = new RegistroDJ1862();
        this.sumaMontoEnPesosPositivo = 0;
        this.sumaMontoEnPesosNegativo = 0;
    }
    
    private void actualizarAcumuladores(final RegistroDJ1862 registroDJ1862) {
        this.contarCasosInformados();
        this.totalRegistros += 1;
        
		if(Integer.parseInt(registroDJ1862.getCodigoConcepto())>10000 && Integer.parseInt(registroDJ1862.getCodigoConcepto())<11495){
	        this.sumaMontoEnPesosPositivo += Math.round(registroDJ1862.getSumaMontoEnPesosPositivo());
		}else if (Integer.parseInt(registroDJ1862.getCodigoConcepto())>20000 && Integer.parseInt(registroDJ1862.getCodigoConcepto())<21499){
			this.sumaMontoEnPesosNegativo += Math.round(registroDJ1862.getSumaMontoEnPesosNegativo());
		}
      
        
        registroResumen.acumular(registroDJ1862);
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
        ret += "1862";

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
        ret += Serializador.espacios(134);

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
           
        // Numero de formulario.
        ret += "1862";

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
        ret += String.format("5602%07d", getContexto().getTelefonoDeclaracion() % 10000000);
        
     // Relleno.
        ret += Serializador.espacios(1);
        
        // Codigo de empresa.
        ret += String.format("%02d", getContexto().getCodigoEmpresa());

        // Numero de cliente.
        ret += String.format("%04d", getContexto().getNumeroCliente());
        
        // Unidad, caja y paquete.
        ret += Serializador.ceros(13);

        // Relleno.
        ret += Serializador.espacios(17);
        
        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 1");
        }
		
        return ret;
    }
    
    public String generarRegistroTipo2(final Registro registro)
            throws SerializadorException {
    	
    	final RegistroDJ1862 registroDJ1862 = new RegistroDJ1862(registro);
        actualizarAcumuladores(registroDJ1862);

        // Tipo de Registro y Numero de Formulario.
        String ret = "21862";
       
        // Codigo de Presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de Declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // Tipo de Declaracion.
        ret += getContexto().getTipoDeclaracion().getCodigo();
        	
        // RUT Declarante
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());
        
        // RUT cliente.
        ret += Serializador.serializarRut(registroDJ1862.getRutCliente());
        
        // Tax ID
        ret += String.format("%15s", registroDJ1862.getTaxID());
        
        // Pais Otorgante Identificador
        if(registroDJ1862.getPaisOtorganteIdentificador() == "")
        	ret += "000";
        else
        	ret += String.format("%03d", Integer.parseInt(registroDJ1862.getPaisOtorganteIdentificador()));

        // Nombre o Razon Social
        String NombreRazonSocial= registroDJ1862.getNombre();
        NombreRazonSocial=StringUtils.stripAccents(NombreRazonSocial);
        ret += String.format("%30s",  (NombreRazonSocial).length() > 30 ?  (NombreRazonSocial).substring(0,30) :  (NombreRazonSocial));
        
        // Codigo Moneda
        ret += String.format("%04d", Integer.parseInt(registroDJ1862.getCodigoMoneda()));
        
        // Monto en Pesos
        //ret += String.format("%15s", new BigDecimal(registroDJ1862.getMontoEnPesos()).toPlainString());
        ret += String.format( "%15s" , (String.format("%015.0f", registroDJ1862.getMontoEnPesos()).replace(".", ""))).replace(" ","0");
        
        // Numero Transaccion
        ret += String.format("%15s", registroDJ1862.getNumeroTransaccion());

        // Codigo Modalidad Operacion
        ret += String.format("%2s", registroDJ1862.getCodigoModalidadOperacion());

        // Codigo Concepto
        ret += String.format("%5s", registroDJ1862.getCodigoConcepto().substring(0, 5));

        // Fecha de la Operacion
        ret += Serializador.serializar(registroDJ1862.getFechaOperacion());
        
        // Nombre Contraparte
        String NombreContraparte= registroDJ1862.getNombreContraparte();
        NombreContraparte=StringUtils.stripAccents(NombreContraparte);
        ret += String.format("%30s",  (NombreContraparte).length() > 30 ?  (NombreContraparte).substring(0,30) :  (NombreContraparte));
        
        // Tax ID
        ret += String.format("%15s", registroDJ1862.getTaxIDContraparte());
        
        // Codigo Pais Contraparte
        ret += String.format("%03d", Integer.parseInt(registroDJ1862.getCodigoPaisContraparte()));
        
        // Banco Contraparte
         String BancoContraparte= registroDJ1862.getBancoContraparte();
        BancoContraparte=StringUtils.stripAccents(BancoContraparte);
        ret += String.format("%30s",  (BancoContraparte).length() > 30 ?  (BancoContraparte).substring(0,30) :  (BancoContraparte));
        
        // Unidad, caja y paquete.
        ret += Serializador.ceros(13);

        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 2");
        }

        return ret;
    }
    
    public String generarRegistroTipo3() throws SerializadorException {

        // Tipo de registro y numero de formulario.
        String ret = "311862";

        // Codigo de presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // RUT declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());
        
        // Total de casos informados.
        ret += String.format("%07d", getCantidadCasosInformados());
        
        // Total Ingresos Informados
        //ret += String.format("%15s", new BigDecimal(registroResumen.getSumaMontoEnPesosPositivo()).toPlainString());
        //ret += String.format( "%15s" , (String.format("%015.0f", registroResumen.getSumaMontoEnPesosPositivo()).replace(".", ""))).replace(" ","0");
        ret += String.format("%15s" , (String.format("%015.0f",sumaMontoEnPesosPositivo).replace(".", ""))).replace(" ","0");
        // Total Egresos Informados
        //ret += String.format("%15s", new BigDecimal(registroResumen.getSumaMontoEnPesosNegativo()).toPlainString());
        //ret += String.format( "%15s" , (String.format("%015.0f", registroResumen.getSumaMontoEnPesosNegativo()).replace(".", ""))).replace(" ","0");
        ret += String.format("%15s" , (String.format("%015.0f",sumaMontoEnPesosNegativo).replace(".", ""))).replace(" ","0");


        // Fecha de presentacion.
        ret += getContexto().getFechaPresentacion();

        // RUT del representante legal.
        ret += Serializador.serializarRut(getContexto().getRutRepresentante());

        // Relleno.
        ret += Serializador.espacios(142);

        /*if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 3");
        }*/

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
