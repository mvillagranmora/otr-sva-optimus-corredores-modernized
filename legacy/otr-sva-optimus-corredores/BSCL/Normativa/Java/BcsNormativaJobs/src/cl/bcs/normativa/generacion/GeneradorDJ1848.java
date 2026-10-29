package cl.bcs.normativa.generacion;

import java.io.IOException;
import java.sql.SQLException;

import cl.bcs.normativa.data.Registro;
import cl.bcs.normativa.registros.RegistroDJ1848;

public final class GeneradorDJ1848 extends Generador {
	
	private static final int LARGO_REGISTRO = 320;
	private RegistroDJ1848 registroResumen;
    private long totalRegistros;
    
    public GeneradorDJ1848(final ContextoGeneracion contexto)
            throws SQLException, IOException, SerializadorException {
        super(contexto);
        this.totalRegistros = 0;
        this.registroResumen = new RegistroDJ1848();
    }
    
    private void actualizarAcumuladores(final RegistroDJ1848 registroDJ1848) {
        this.contarCasosInformados();
        this.totalRegistros += 1;
        registroResumen.acumular(registroDJ1848);
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
        ret += String.format("%04d", getContexto().getAnoTributario());

        // Numero de formulario.
        ret += "1848";

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

        // Ano tributario.
        ret += String.format("%04d", getContexto().getAnoTributario()) + "00";
        
        // Relleno.
        ret += Serializador.espacios(225);
        
        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 0");
        }
	
        return ret;
    }
    
    public String generarRegistroTipo1() throws SerializadorException {

        // Tipo de registro y precedencia.
        String ret = "11";
       
        // Ano tributario.
        ret += String.format("%04d", getContexto().getAnoTributario()) + "00";
        
        // Numero de formulario.
        ret += "1848";

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
        ret += String.format("5602%07d", getContexto().getTelefonoDeclaracion() % 1000000);

        // Codigo de empresa.
        ret += String.format("%02d", getContexto().getCodigoEmpresa());

        // Numero de cliente.
        ret += String.format("%04d", getContexto().getNumeroCliente());

        // Unidad, caja y paquete.
        ret += Serializador.ceros(13);

        // Relleno.
        ret += Serializador.espacios(116);
        
        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 1");
        }
		
        return ret;
    }
    
    public String generarRegistroTipo2(final Registro registro)
            throws SerializadorException {
    	
    	final RegistroDJ1848 registroDJ1848 = new RegistroDJ1848(registro);
        actualizarAcumuladores(registroDJ1848);

        // Tipo de Registro y Numero de Formulario.
        String ret = "21848";
       
        // Codigo de Presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de Declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // Tipo de Declaracion.
        ret += getContexto().getTipoDeclaracion().getCodigo();

        // RUT Declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());

        // Inversionista Extranjero
        ret += Serializador.serializarRut(registroDJ1848.getInversionistaExtranjeroRut());
        
        // País de Residencia
        ret += String.format("%2s", registroDJ1848.getInversionistaPaisResidencia());
        
        // Modalidad de actuacion
        ret += String.format("%1s", registroDJ1848.getInversionistaExtranjeroModalidadActuacion());
        
        // Forma de declaración
        ret += String.format("%01d", registroDJ1848.getTransaccionesFormaDeclaracion());
        
        // Tipo de Operación o situación del inversionista
        ret += String.format("%2s", registroDJ1848.getTransaccionesTipoOperacionInversionista());
        
        // Tipo de Instrumento o Contrato
        ret += String.format("%2s", registroDJ1848.getTransaccionesTipoInstrumentoContrato());
     
        // Fecha de la Operación o fecha cambio situación del inversionista
        ret += Serializador.serializar(registroDJ1848.getTransaccionesFechaOperacionSituacionInversionista());  
        
        // Monto de la Operación (en pesos)
        ret += String.format("%015d", Double.valueOf(registroDJ1848.getTransaccionesMontoOperacionPesos()).intValue());   
                
        // Moneda o unidad original de operación
        ret += String.format("%3s", registroDJ1848.getTransaccionesMonedaOriginal());  
        
        // Monto de la Operación en moneda o unidad original 
        ret += String.format("%013d", Double.valueOf(registroDJ1848.getTransaccionesMontoOperacionMonedaOriginal()).intValue());
        ret += String.format("%02d", String.valueOf(registroDJ1848.getTransaccionesMontoOperacionMonedaOriginal()).split(".").length == 2 ? Integer.valueOf(String.valueOf(registroDJ1848.getTransaccionesMontoOperacionMonedaOriginal()).split(".")[1]) : 0);
         
        // Gastos en pesos
        ret += String.format("%015d", Double.valueOf(registroDJ1848.getTransaccionesGastos()).intValue());   
   
        // Contrato Vencido en el Ejercicio  
        ret += String.format("%2s", registroDJ1848.getCumplimientoTipoInversionista());
        
        // Fecha de la Operación o fecha cambio situación del inversionista
        ret += Serializador.serializar(registroDJ1848.getCumplimientoFechaContrato());  
        
        //Tipo Instrumento o contrato de origen
        ret += String.format("%2d",registroDJ1848.getCumplimientoTipoInstrumento());
        
        //Fecha Instrumento
        ret += Serializador.serializar(registroDJ1848.getCumplimientoFechaInstrumento());  
        
        //Folio DJ1848 origen operacion
        ret += String.format("%10d",registroDJ1848.getCumplimientoFolioDJ1848());
        
        //Saldo inversión 
        ret += String.format("%012d",registroDJ1848.getCumplimientoSaldoInversion().intValue());
        
        //Reorganización
        ret += String.format("%012d",registroDJ1848.getCumplimientoReorganizacion().intValue());
        
        // RUT Responsable del Entero del Impuesto
        ret += Serializador.serializarRut(registroDJ1848.getRetencionesRutResponsaleImpuesto());
        
        // Base Imponible (pesos)
        ret += String.format("%15d", Double.valueOf(registroDJ1848.getRetencionesBaseImponible()).intValue());
        
        // Tasa de Impuesto    
        ret += String.format("%3d", Double.valueOf(registroDJ1848.getRetencionesTasaImpuesto()).intValue());
        
        // Crédito Impuesto 1° Categoría o inciso 4° artículo 108 LIR (pesos)
        ret += String.format("%15d", Double.valueOf(registroDJ1848.getRetencionesCreditoImpuesto()).intValue());
        
        // Impuesto Retenido y/o pagado (en pesos)
        ret += String.format("%15d", Double.valueOf(registroDJ1848.getRetencionesImpuesto()).intValue());
        
        // Folio F50
        ret += String.format("%10s", registroDJ1848.getRetencionesFolioF50());
        
        // Periodo tributario F50
        ret += String.format("%6d", registroDJ1848.getRetencionesPeriodoTributarioF50());
        
        // Norma que establece el beneficio tributario
        ret += String.format("%02d", Integer.parseInt(registroDJ1848.getBeneficiosNormaBeneficioTributario()));
        
        // Referencia legal otros beneficios tributarios
        ret += String.format("%80s", registroDJ1848.getBeneficiosReferenciaLegalOtrosBeneficiosTributarios());
        
        // Monto exento o beneficiado (pesos)
        ret += String.format("%15d", Double.valueOf(registroDJ1848.getBeneficiosMontoExento()).intValue());
        
        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 2");
        }

        return ret;
    }
    
    public String generarRegistroTipo3() throws SerializadorException {

        // Tipo de registro y numero de formulario.
        String ret = "311848";

        // Codigo de presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // RUT declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());
        
        // Total de casos informados.
        ret += String.format("%010d", getCantidadCasosInformados());
        
        // Monto Total de Impuestos retenidos y/o pagados (pesos)
        ret += String.format("%018d", Double.valueOf(registroResumen.getSumaRetencionesImpuesto()).intValue());
        
        // Monto total exento o beneficiado (pesos)
        ret += String.format("%018d", Double.valueOf(registroResumen.getSumaBeneficiosMontoExento()).intValue());
        
        // Fecha de presentacion.
        ret += getContexto().getFechaPresentacion();

        // RUT del representante legal.
        ret += Serializador.serializarRut(getContexto().getRutRepresentante());

        // Relleno.
        ret += Serializador.espacios(233);

        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 3");
        }

        return ret;
    }
    
    @Override
    public void generarResumen() throws SerializadorException {
        print(generarRegistroTipo3());
    }
    
    private long getCantidadTotalRegistros() {
        return getCantidadCasosInformados() + 3;
    }
}
