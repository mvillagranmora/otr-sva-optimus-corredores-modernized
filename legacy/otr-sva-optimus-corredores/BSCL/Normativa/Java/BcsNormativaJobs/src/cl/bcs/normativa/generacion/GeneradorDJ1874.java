package cl.bcs.normativa.generacion;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cl.bcs.normativa.data.Registro;
import cl.bcs.normativa.registros.RegistroDJ1874;

public final class GeneradorDJ1874 extends Generador {
	
	private static final int LARGO_REGISTRO = 266;
	private RegistroDJ1874 registroResumen;
    private long totalRegistros;
    
    public GeneradorDJ1874(final ContextoGeneracion contexto)
            throws SQLException, IOException, SerializadorException {
        super(contexto);
        this.totalRegistros = 0;
        this.registroResumen = new RegistroDJ1874();
    }
    
    private void actualizarAcumuladores(final RegistroDJ1874 registroDJ1874) {
        this.contarCasosInformados();
        this.totalRegistros += 1;
        registroResumen.acumular(registroDJ1874, getContexto().getRutDeclarante());
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
        ret += "1874";

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
        
        // Periodo tributario.
        ret += String.format("%04d", getContexto().getAnoTributario()+1L) + "00";
        
        // Relleno.
        ret += Serializador.espacios(174);
        
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
        ret += "001874";

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
         
        // Tipo declarante
        ret += String.format("%02d", 1);
        
        // Fono.
        ret += String.format("5602%07d", getContexto().getTelefonoDeclaracion() % 10000000);
        // Fax.
        ret += "56020000000";
        
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
        
        // Relleno.
        ret += Serializador.espacios(1);
        
        // Codigo de empresa.
        ret += String.format("%02d", getContexto().getCodigoEmpresa());

        // Numero de cliente.
        ret += String.format("%04d", getContexto().getNumeroCliente());
        
        // Unidad, caja y paquete.
        ret += Serializador.ceros(13);

        // Relleno.
        ret += Serializador.espacios(59);
        
        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 1");
        }
		
        return ret;
    }
    
    public String generarRegistroTipo2(final Registro registro)
            throws SerializadorException {
    	
    	final RegistroDJ1874 registroDJ1874 = new RegistroDJ1874(registro);
        actualizarAcumuladores(registroDJ1874);

        // Tipo de Registro y Numero de Formulario.
        String ret = "21874";
       
        // Codigo de Presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de Declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // Tipo de Declaracion.
        ret += getContexto().getTipoDeclaracion().getCodigo();
        	
        // RUT Declarante
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());
        
        // RUT emisor.
        ret += Serializador.serializarRut(registroDJ1874.getRutEmisor());
        
        // RUT inversionista.
        ret += Serializador.serializarRut(registroDJ1874.getRutInversionista());
        
        // Tipo Contribuyente
        ret += String.format("%2s", registroDJ1874.getTipoContribuyente()).replace(".", "").replace(" ","0");
        
        // Nemotecnico svs
        //ret += String.format("%25s", registroDJ1874.getNemotecnicoSVS());
        String Nemo=registroDJ1874.getNemotecnicoSVS();
        ret += String.format("%-25s",  (Nemo).length() > 25 ?  (Nemo).substring(0,25) :  (Nemo));
        
        // Fecha Operacion
        ret += Serializador.serializar(registroDJ1874.getFechaOperacion());
        
        // Tipo Transaccion
        ret += String.format("%2s", registroDJ1874.getMonedaUnidadReajuste()).replace(".", "").replace(" ","0");
        
        // Valores Transados
        ret += String.format("%8s", registroDJ1874.getNumeroCuentaDCV()).replace(".", "").replace(" ","0");
        
        // Movimiento Saldo Inicial
        ret += String.format("%015.0f", registroDJ1874.getMovimientoSaldoInicial()).replace(".", "");
        
        // Movimiento Adquisicion
         ret += String.format("%015.0f", registroDJ1874.getMovimientoAdquision()).replace(".", "");
        
        // Movimiento Enajenacion
         ret += String.format("%015.0f", registroDJ1874.getMovimientoEnajenacion()).replace(".", "");
        
        // Movimiento Rescate Anticipado
         ret += String.format("%015.0f", registroDJ1874.getMovimientoRescateAnticipado()).replace(".", "");
        
        // Movimiento Saldo Final
         ret += String.format("%015.0f", registroDJ1874.getMovimientoSaldoFinal()).replace(".", "");
        
        // Saldo Final Nominal
         ret += String.format("%015.0f", registroDJ1874.getSaldoFinalNominal()).replace(".", "");
        
        // Interes Devengado Moneda Unidad Reajuste
        ret += String.format("%015.0f", registroDJ1874.getInteresDevengadoMonedaUnidadReajuste()).replace(".", "");
        
        // Interes Devengado Pesos
        ret += String.format("%015.0f", registroDJ1874.getInteresDevengadoPesos()).replace(".", "");
        
        // Resultado Enajenacion Moneda Unidad Reajuste
        ret += String.format("%015.0f", registroDJ1874.getResultadoEnajenacionMonedaUnidadReajuste()).replace(".", "");
        
        // Resultado Enajenacion Pesos
        ret += String.format("%015.0f", registroDJ1874.getResultadoEnajenacionPesos()).replace(".", "");
        
        // Retencion Moneda Unidad Reajuste
        ret += String.format("%015.0f", registroDJ1874.getRetencionMonedaUnidadReajuste()).replace(".", "");
        
        
        // Retencion Pesos
        ret += String.format("%015.0f", registroDJ1874.getRetencionPesos()).replace(".", "");
        
        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 2");
        }

        return ret;
    }
    
    public String generarRegistroTipo3() throws SerializadorException {

        // Tipo de registro y numero de formulario.
        String ret = "311874";

        // Codigo de presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // RUT declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());
        
        // Total de casos informados.
        ret += String.format("%07d", getCantidadCasosInformados());
        
        // Interes Moneda
        //ret += String.format("%018d", Double.valueOf(registroResumen.getSumaInteresDevengadoMonedaUnidadReajuste()).intValue());
        
        // Interes Pesos de terceros
        ret += String.format("%18s",(String.format("%018.0f", registroResumen.getSumaInteresDevengadoPesosTerceros()).replace(".", ""))).replace(" ","0");
        
        // Intereses Peso propios
        ret += String.format("%18s",(String.format("%018.0f", registroResumen.getSumaInteresDevengadoPesosPropios()).replace(".", ""))).replace(" ","0");
        
        // Resultado Moneda
        //ret += String.format("%018d", Double.valueOf(registroResumen.getSumaResultadoEnajenacionMonedaUnidadReajuste()).intValue());
        
        // Resultado Moneda
        ret += String.format("%18s",(String.format("%018.0f", registroResumen.getSumaResultadoEnajenacionPesosTerceros()).replace(".", ""))).replace(" ","0");
        
        // Resultado Pesos
        ret += String.format("%18s",(String.format("%018.0f", registroResumen.getSumaResultadoEnajenacionPesosPropios()).replace(".", ""))).replace(" ","0");
        
        // Retencion Moneda
        ret += String.format("%18s",(String.format("%018.0f", registroResumen.getSumaRetencionMonedaUnidadReajuste()).replace(".", ""))).replace(" ","0");
        
        // Retencion Pesos
        ret += String.format("%18s",(String.format("%018.0f", registroResumen.getSumaRetencionPesos()).replace(".", ""))).replace(" ","0");
        
        // Fecha de presentacion.
        ret += getContexto().getFechaPresentacion();

        // RUT del representante legal.
        ret += Serializador.serializarRut(getContexto().getRutRepresentante());

        // Relleno.
        ret += Serializador.espacios(110);
        
        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 3");
        }

        return ret;
    }
    
    @Override
    public void generarResumen() throws SerializadorException {
        println(generarRegistroTipo3());
    }
    
    private long getCantidadTotalRegistros() {
        return getCantidadCasosInformados() + 3;
    }
}
