package cl.bcs.normativa.generacion;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cl.bcs.normativa.data.Registro;
import cl.bcs.normativa.registros.RegistroDJ1870;

public final class GeneradorDJ1870 extends Generador {
	
	private static final int LARGO_REGISTRO = 177;
//	private static final int LARGO_REGISTRO2 = 354;
	private RegistroDJ1870 registroResumen;
    private long totalRegistros;
    
    public GeneradorDJ1870(final ContextoGeneracion contexto)
            throws SQLException, IOException, SerializadorException {
        super(contexto);
        this.totalRegistros = 0;
        this.registroResumen = new RegistroDJ1870();
    }
    
    private void actualizarAcumuladores(final RegistroDJ1870 registroDJ1870) {
        this.contarCasosInformados();
        this.totalRegistros += 1;
        registroResumen.acumular(registroDJ1870);
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
        ret += "1870";

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
        ret += Serializador.espacios(91);
        
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
        ret += "1870";

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
        ret += String.format("%30s",  (RazonSocial).length() > 30 ?  (RazonSocial).substring(0,30) :  (RazonSocial));        
        
        // Direccion postal.
        String DireccionDeclaracion=getContexto().getDireccionDeclaracion();
        ret += String.format("%35s",  (DireccionDeclaracion).length() > 35 ?  (DireccionDeclaracion).substring(0,35) :  (DireccionDeclaracion));

        // Comuna.
        String ComunaDeclaracion=getContexto().getComunaDeclaracion();
        ret += String.format("%15s",  (ComunaDeclaracion).length() > 15 ?  (ComunaDeclaracion).substring(0,15) :  (ComunaDeclaracion));

        // Correo electronico.
        String EmailDeclaracion=getContexto().getEmailDeclaracion();
        ret += String.format("%30s",  (EmailDeclaracion).length() > 30 ?  (EmailDeclaracion).substring(0,30) :  (EmailDeclaracion));
        
        // Fax.
        ret += "56020000000";

        // Relleno.
        ret += Serializador.espacios(5);
        
        /*if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 1");
        }*/
		
        return ret;
    }
    
    public String generarRegistroTipo1Complemento() throws SerializadorException {

        // TipoRegistro.
        String ret = "12";
        
        // Fono.
        ret += String.format("5602%07d", getContexto().getTelefonoDeclaracion() % 10000000);

        // Codigo de empresa.
        ret += String.format("%02d", getContexto().getCodigoEmpresa());

        // Numero de cliente.
        ret += String.format("%04d", getContexto().getNumeroCliente());
        
        // Unidad, caja y paquete.
        ret += Serializador.ceros(13);

        // Relleno.
        ret += Serializador.espacios(145);
        
        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 1 Complemento");
        }
		
        return ret;
    }
    
    /* Contenido de la DJ */
    public String generarRegistroTipo2(final Registro registro)
            throws SerializadorException {
    	
        /* Estas 2 lineas generan error */
    	final RegistroDJ1870 registroDJ1870 = new RegistroDJ1870(registro);
        actualizarAcumuladores(registroDJ1870);
      
        String numberD;
        String numberE;
        String valoresT;
        String dec;

        // Tipo de Registro y Numero de Formulario.
        String ret = "21870";
       
        // Codigo de Presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de Declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // Tipo de Declaracion.
        ret += getContexto().getTipoDeclaracion().getCodigo();
        
        // RUT Declarante
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());
        
        // RUT cliente.
        ret += Serializador.serializarRut(registroDJ1870.getDatosPersonaRut());
        
        // Identificador otorgado en el extanjero
        ret += String.format("%30s", registroDJ1870.getDatosPersonaIndentificadorExtranjero());
        
        // Pais otorgante
        ret += String.format("%2s", registroDJ1870.getDatosPersonaPaisOtorganteIdentificador());
            
        // Nombre o Razon Social
        String RazonSocial=registroDJ1870.getDatosPersonaNombreRazonSocial();
        RazonSocial = Serializador.removerCaracteresEspeciales(RazonSocial);
        ret += String.format("%-30s",  (RazonSocial).length() > 30 ?  (RazonSocial).substring(0,30) :  (RazonSocial));
          
      
        // Tipo Transaccion
        ret += String.format("%1s", registroDJ1870.getTipoTransaccionDJ());
               
        // Valores Transados
        //ret += String.format("%1s", (String.format("%01.0f", registroDJ1870.getValoresTransados()).replace(".", ""))).replace(" ","0");
 
        ret += String.format("%1s", registroDJ1870.getValoresTransados());

        // Fecha Transaccion
        //dia
        //String d = String.format("%2s", registroDJ1870.getFechaTransaccionDia());
        // ret += d.replaceAll("\\s", "0");

        //dia
        //ret += registroDJ1870.getFechaTransaccionDia();
        String dia = registroDJ1870.getFechaTransaccionDia();
        String restoDia = "0";
        ret += dia.length() < 2 ? restoDia + dia : dia;   // Si el mes es 1 lo convierte a 01 para completas los 2 espacios que se solicitan 

        //mes
        //ret += registroDJ1870.getFechaTransaccionMes();
        String mes = registroDJ1870.getFechaTransaccionMes();
        String restoMes = "0";
        ret += mes.length() < 2 ? restoMes + mes : mes;   // Si el dia es 1 lo convierte a 01 para completas los 2 espacios que se solicitan 

        //anio
        ret += registroDJ1870.getFechaTransaccionAnio();
       
        // Tipo Documento
        ret += String.format("%1s", registroDJ1870.getDocumentoTipoDocumento());

        // Numero Documento
        String doc =String.format("%15s", registroDJ1870.getDocumentoNumero());
        ret +=doc.replaceAll("\\s", "0");
        //ret += String.format("%15s", registroDJ1870.getDocumentoNumero());

        // Codigo Moneda
        ret += String.format("%-5s", registroDJ1870.getCodigoMonedaTransada());
        
        //Monto Compra
        //ret += (String.format("%015.00f", new BigDecimal(registroDJ1870.getMontoCompras()).toPlainString())).replace(" ","0");
        ret += String.format( "%15s" , (String.format("%015.0f", registroDJ1870.getMontoCompras()).replace(".", ""))).replace(" ","0");
        //Monto Ventas
        //ret += (String.format("%015.00f", new BigDecimal(registroDJ1870.getMontoVentas()).toPlainString())).replace(" ","0");
        ret += String.format( "%15s" , (String.format("%015.0f", registroDJ1870.getMontoVentas()).replace(".", ""))).replace(" ","0");


        // Tipo Cambio
        numberE = String.valueOf(registroDJ1870.getTipoCambio());
        ret += String.format("%7s", numberE.substring(0, numberE.indexOf("."))).replace(".", "").replace(" ", "0");


        numberD = String.valueOf(registroDJ1870.getTipoCambio());
        String RestoDecimal = "0";
        numberD = numberD.substring(numberD.indexOf(".")).replace(".", "").replace(" ", "0");
        ret += numberD.length() < 2 ? numberD + RestoDecimal : numberD;

        // Unidad, caja y paquete.
        ret += Serializador.ceros(13);

        
        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 2");
        }
       

        return ret; 
    }
    
    public String generarRegistroTipo3() throws SerializadorException {

        // Tipo de registro y numero de formulario.
        String ret = "31870";

        // Codigo de presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // RUT declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());
        
        // Total de casos informados.
        ret += String.format("%07d", getCantidadCasosInformados());
        
        // Suma Compras
        ret += String.format("%18s" , (String.format("%018.0f",registroResumen.getSumaMontoCompras()).replace(".", ""))).replace(" ","0");
        
        // Suma Ventas
        ret += String.format("%18s" , (String.format("%018.0f",registroResumen.getSumaMontoVentas()).replace(".", ""))).replace(" ","0");
        
        
        // Fecha de presentacion.
        ret += getContexto().getFechaPresentacion();

        // RUT del representante legal.
        ret += Serializador.serializarRut(getContexto().getRutRepresentante());

        // Relleno.
        ret += Serializador.espacios(94);
                
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
        return getCantidadCasosInformados() + 4;
    }
}
