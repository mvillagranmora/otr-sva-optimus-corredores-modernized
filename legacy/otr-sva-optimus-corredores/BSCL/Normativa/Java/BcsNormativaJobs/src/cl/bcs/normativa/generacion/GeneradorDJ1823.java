package cl.bcs.normativa.generacion;

import java.io.IOException;
import java.sql.SQLException;

import cl.bcs.normativa.data.Registro;
import cl.bcs.normativa.registros.RegistroDJ1823;

public final class GeneradorDJ1823 extends Generador {
	
	private static final int LARGO_REGISTRO = 242;
	private RegistroDJ1823 registroResumen;
    private long totalRegistros;
    
    public GeneradorDJ1823(final ContextoGeneracion contexto)
            throws SQLException, IOException, SerializadorException {
        super(contexto);
        this.totalRegistros = 0;
        this.registroResumen = new RegistroDJ1823();
    }
    
    private void actualizarAcumuladores(final RegistroDJ1823 registroDJ1823) {
        this.contarCasosInformados();
        this.totalRegistros += 1;
        registroResumen.acumular(registroDJ1823);
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
        ret += "1823";

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
        ret += Serializador.espacios(156);

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
        ret += "1823";

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

        ret += Serializador.espacios(1);
        
        // Codigo de empresa.
        ret += String.format("%02d", getContexto().getCodigoEmpresa());

        // Numero de cliente.
        ret += String.format("%04d", getContexto().getNumeroCliente());

        // Unidad, caja y paquete.
        ret += Serializador.ceros(13);

        // Relleno.
        ret += Serializador.espacios(39);
        
        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 1");
        }
		
        return ret;
    }
    
    public String generarRegistroTipo2(final Registro registro)
            throws SerializadorException {
    	
    	final RegistroDJ1823 registroDJ1823 = new RegistroDJ1823(registro);
        actualizarAcumuladores(registroDJ1823);

        // Tipo de Registro y Numero de Formulario.
        String ret = "21823";
       
        // Codigo de Presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de Declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // Tipo de Declaracion.
        ret += getContexto().getTipoDeclaracion().getCodigo();

        // RUT Declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());

        // RUT Institucion Receptora.
        ret += Serializador.serializarRut(registroDJ1823.getRutInstitucionReceptora());
        
        // Rut Inversionista.
        ret += Serializador.serializarRut(registroDJ1823.getRutCliente());
        
        // Saldos Ahorro Neto ejercicio Hasta 31.12.2014 Monto Positivo
        ret += String.format("%012f", registroDJ1823.getSaldoAhorroNetoHasta31Dic2014MontoPositivo());
        
        // Saldos Ahorro Neto ejercicio Hasta 31.12.2014 Monto Negativo
        ret += String.format("%012f", registroDJ1823.getSaldoAhorroNetoHasta31Dic2014MontoNegativo());
        
        // Monto retiro inv. 57 BIS hasta 31.12.2014
        ret += String.format("%012f", registroDJ1823.getMontoRetiroInversion57BIS());
        
        // Saldos Ahorro Neto ejercicio desde 01.01.2015 Monto Positivo
        ret += String.format("%012f", registroDJ1823.getSaldoAhorroNetoHasta31Dic2014MontoPositivo());
        
        // Saldos Ahorro Neto ejercicio desde 01.01.2015 Monto Negativo
        ret += String.format("%012f", registroDJ1823.getSaldoAhorroNetoHasta31Dic2014MontoNegativo());
       
        // Saldos de arrastre ejercicios siguientes Monto Positivo
        ret += String.format("%012f", registroDJ1823.getSaldoArrastreEjercicioDesde01Ene2015MontoPositivo());
        
        // Saldos de arrastre ejercicios siguientes Monto Negativo
        ret += String.format("%012f", registroDJ1823.getSaldoArrastreEjercicioDesde01Ene2015MontoNegativo());
        
        // Monto de Inversión
        ret += String.format("%012f", registroDJ1823.getMontoRetiroInversion57BIS());
        
        // Monto de Retiro
        ret += String.format("%012f", registroDJ1823.getMontoRetiroInversion57BIS());
        
        //Monto Rentabilidad o ganancia retirada en FFMM Monto Positivo
        ret += String.format("%012f", registroDJ1823.getMontoRentabilidadFFMMEjercicioDesde01Ene2015MontoPositivo());
        
        // Monto Rentabilidad o ganancia retirada en FFMM Monto Negativo
        ret += String.format("%012f", registroDJ1823.getMontoRentabilidadFFMMEjercicioDesde01Ene2015MontoNegativo());
        
        // Monto Rentabilidad o ganancia retirada en Otros instrumentos Monto Positivo
        ret += String.format("%012f", registroDJ1823.getMontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoPositivo());
        
        //  Monto Rentabilidad o ganancia retirada en Otros instrumentos Monto Negativo
        ret += String.format("%012f", registroDJ1823.getMontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoNegativo());
        
        // Numero de Certificado
        ret += String.format("%07d", registroDJ1823.getNumeroCertificado());
        
        // Relleno.
        ret += Serializador.espacios(38);
        
        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 2");
        }

        return ret;
    }
    
    public String generarRegistroTipo3() throws SerializadorException {

        // Tipo de registro y numero de formulario.
        String ret = "31823";

        // Codigo de presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // RUT declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());
        
        // Saldos Ahorro Neto ejercicio Hasta 31.12.2014 Monto Positivo
        ret += String.format("%15f", registroResumen.getSumaSaldoAhorroNetoHasta31Dic2014MontoPositivo());
        
        // Saldos Ahorro Neto ejercicio Hasta 31.12.2014 Monto Negativo
        ret += String.format("%15f", registroResumen.getSumaSaldoAhorroNetoHasta31Dic2014MontoNegativo());
        
        // Monto retiro inv. 57 BIS hasta 31.12.2014
        ret += String.format("%15f", registroResumen.getSumaMontoRetiroEjercicioDesde01Ene2015());
        
        // Saldos Ahorro Neto ejercicio desde 01.01.2015 Monto Positivo
        ret += String.format("%15f", registroResumen.getSumaSaldoAhorroNetoDesde01Ene2015MontoPositivo());
        
        // Saldos Ahorro Neto ejercicio desde 01.01.2015 Monto Negativo
        ret += String.format("%15f", registroResumen.getSumaSaldoAhorroNetoDesde01Ene2015MontoNegativo());
        
        // Saldos de arrastre ejercicios siguientes Monto Positivo
        ret += String.format("%15f", registroResumen.getSumaSaldoArrastreEjercicioDesde01Ene2015MontoPositivo());
        
        // Saldos de arrastre ejercicios siguientes Monto Negativo
        ret += String.format("%15f", registroResumen.getSumaSaldoArrastreEjercicioDesde01Ene2015MontoNegativo());
        
        // Monto de Inversión
        ret += String.format("%15f", registroResumen.getSumaMontoInversionEjercicioDesde01Ene2015());
        
        // Monto de Retiro
        ret += String.format("%15f", registroResumen.getSumaMontoRetiroEjercicioDesde01Ene2015());
        
        // Monto Rentabilidad o ganancia retirada en FFMM Monto Positivo
        ret += String.format("%15f", registroResumen.getMontoRentabilidadFFMMEjercicioDesde01Ene2015MontoPositivo());
        
        // Monto Rentabilidad o ganancia retirada en FFMM Monto Negativo
        ret += String.format("%15f", registroResumen.getMontoRentabilidadFFMMEjercicioDesde01Ene2015MontoNegativo());
        
        // Monto Rentabilidad o ganancia retirada en Otros instrumentos Monto Positivo
        ret += String.format("%15f", registroResumen.getMontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoPositivo());
        
        // Monto Rentabilidad o ganancia retirada en Otros instrumentos Monto Negativo
        ret += String.format("%15f", registroResumen.getMontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoNegativo());
        
        // Total de casos informados.
        ret += String.format("%07d", getCantidadCasosInformados());
        
        // Fecha de presentacion.
        ret += getContexto().getFechaPresentacion();

        // RUT del representante legal.
        ret += Serializador.serializarRut(getContexto().getRutRepresentante());
        
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
