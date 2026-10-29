package cl.bcs.normativa.generacion;

import java.io.IOException;
import java.sql.SQLException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import cl.bcs.normativa.data.Registro;
import cl.bcs.normativa.registros.RegistroDJ1829;

public final class GeneradorDJ1829 extends Generador {
	
	private  int LARGO_REGISTRO = 409;
	private RegistroDJ1829 registroResumen;
    private long totalRegistros;
    private long versionDJ;
    
    public GeneradorDJ1829(final ContextoGeneracion contexto)
            throws SQLException, IOException, SerializadorException {
        super(contexto);
        this.totalRegistros = 0;
        this.registroResumen = new RegistroDJ1829();
        this.versionDJ=getContexto().getAnoTributario();
        if (versionDJ >= 2025){
            LARGO_REGISTRO += 14;
            }
        }
    
    
    private void actualizarAcumuladores(final RegistroDJ1829 registroDJ1829) {
        this.contarCasosInformados();
        this.totalRegistros += 1;
        registroResumen.acumular(registroDJ1829);
    }
    
    @Override
    public void generarDetalle(final Registro registro) throws SerializadorException {
        if (versionDJ >= 2025){
            println(generarRegistroTipo2_2025(registro));
        }
        else {
            println(generarRegistroTipo2(registro));
        }
    }  
    
    @Override
    public void generarEncabezado() throws SerializadorException {
    	
    	if (getContexto().getModoPresentacion().getCodigo().equalsIgnoreCase("I"))
    	{   
            if (versionDJ >= 2025){
                println(generarRegistroTipo0_2025());
            }
            else {
                println(generarRegistroTipo0());
            }
    	};
        
        if (versionDJ >= 2025){
            println(generarRegistroTipo1_2025());
        }
        else {
            
            println(generarRegistroTipo1());
        }
        
    }
    
    @Override
    public String generarRegistroTipo0() throws SerializadorException {

        // Tipo de registro.
        String ret = "0";
        
        // Ano tributario.
        ret += String.format("%04d", getContexto().getAnoTributario()+1L);

        // Numero de formulario.
        ret += "1829";

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
        
        //Periodo tributario
        ret += String.format("%04d", getContexto().getAnoTributario()) + "00";
        
        // Relleno.
        ret += Serializador.espacios(314);

        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 0");
        }
	
        return ret;
    }
    
    public String generarRegistroTipo1() throws SerializadorException {

        // Tipo de registro y precedencia.
        String ret = "11";
       
        // Ano tributario.
        ret += String.format("%04d", getContexto().getAnoTributario()+1L) + "00";
           
        // Numero de formulario.
        ret += "1829";

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
        ret += String.format("5602%07d", getContexto().getTelefonoDeclaracion() % 10000000);

        // Codigo de empresa.
        ret += String.format("%02d", getContexto().getCodigoEmpresa());

        // Numero de cliente.
        ret += String.format("%04d", getContexto().getNumeroCliente());

        // Unidad, caja y paquete.
        ret += Serializador.ceros(13);

        // Relleno.
        ret += Serializador.espacios(205);

        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 1");
        }
		
        return ret;
    }
    
    public String generarRegistroTipo2(final Registro registro)
            throws SerializadorException {
    	
    	final RegistroDJ1829 registroDJ1829 = new RegistroDJ1829(registro);
        actualizarAcumuladores(registroDJ1829);

        // Tipo de Registro y Numero de Formulario.
        String ret = "21829";
       
        // Codigo de Presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de Declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // Tipo de Declaracion.
        ret += getContexto().getTipoDeclaracion().getCodigo();

        // RUT Declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());

        // RUT Contraparte.
        // ret += Serializador.serializarRut(registroDJ1829.getRutContraparte());
        String rut = Serializador.serializarRut(registroDJ1829.getRutContraparte());
        ret += (rut != null && rut.length() > 9) ? rut.substring(0, 9) : rut;
        
        // Tax ID Contraparte.
        ret += String.format("%-15s", registroDJ1829.getTaxIDContraparte());
        
        // Codigo Pais Contraparte.
        // ret += String.format("%2s", registroDJ1829.getCodigoPaisContraparte());
        ret += String.format("%-2.2s", registroDJ1829.getCodigoPaisContraparte());
        
        // Tipo Relacion Contraparte
        //ret += String.format("%2s", registroDJ1829.getTipoRelacionContraparte());
        String trc = String.format("%2d", Double.valueOf(registroDJ1829.getTipoRelacionContraparte()).intValue());
        // ret += trc.replaceAll("\\s", "0");
        trc = trc.replaceAll("\\s", "0");

        if (trc.length() > 2) {
            trc = trc.substring(0, 2);
        }

        ret += trc;
        
        // Modalidad Contratacion
        // ret += String.format("%1s", registroDJ1829.getModalidadContratacion());
        ret += String.format("%-1.1s", registroDJ1829.getModalidadContratacion());
        
        // Acuerdo Marco Tipo
        ret += String.format("%-1s", registroDJ1829.getAcuerdoMarcoTipo());   
       
        // Acuerdo Marco Numero      
        ret += String.format("%10d", registroDJ1829.getAcuerdoMarcoNumero().intValue()).replace(".", "").replace(" ","0");;  

        // Acuerdo Marco Suscripcion   

        String AcuerdoMarcoFechaSuscripcion = Serializador.serializar(registroDJ1829.getAcuerdoMarcoFechaSuscripcion());
        if (AcuerdoMarcoFechaSuscripcion != null && AcuerdoMarcoFechaSuscripcion.length() == 8 && !AcuerdoMarcoFechaSuscripcion.equals("01010001") && !AcuerdoMarcoFechaSuscripcion.equals("01011900") )  {
            // ddMMyyyy 
            AcuerdoMarcoFechaSuscripcion = 
                                    AcuerdoMarcoFechaSuscripcion.substring(0, 2)      // dd
                                    +AcuerdoMarcoFechaSuscripcion.substring(2, 4)        // MM
                                    +AcuerdoMarcoFechaSuscripcion.substring(4, 8);     // yyyy
        } else {
            AcuerdoMarcoFechaSuscripcion = "00000000";
        }
        ret += AcuerdoMarcoFechaSuscripcion;  
        
        // Contrato Numero
        ret += String.format("%10d", registroDJ1829.getContratoNumero().intValue()).replace(".", "").replace(" ","0");

        // Contrato Fecha Suscripcion      
        ret += Serializador.serializar(registroDJ1829.getContratoFechaSuscripcion());  
        
        // Contrato Vencido en el Ejercicio  
        ret += String.format("%-1s", registroDJ1829.getContratoVencidoEjercicio());
        
        // Contrato Estado         
        ret += String.format("%-1s", registroDJ1829.getContratoEstado());
        
        // Contrato Tipo
        ret += String.format("%2s", registroDJ1829.getContratoTipo()).replace(" ","0");;

        // Contrato Nombre del Instrumento
        //ret += String.format("%20s", registroDJ1829.getContratoNombreInstrumento());
        String Instrumento=registroDJ1829.getContratoNombreInstrumento();
        ret += String.format("%-20s",  (Instrumento).length() > 20 ?  (Instrumento).substring(0,20) :  (Instrumento));        
        
        // Contrato Modalidad de Cumplimiento
        ret += String.format("%1s", registroDJ1829.getContratoModalidadCumplimiento());
        
        // Contrato posicion del declarante    **
        //ret += String.format("%1s", registroDJ1829.getContratoPosicionDeclarante());
        String cpd = String.format("%1d", Double.valueOf(registroDJ1829.getContratoPosicionDeclarante()).intValue());
        ret += cpd.replaceAll("\\s", "0");
         
        // Activo Subyacente Tipo
        ret += String.format("%1s", registroDJ1829.getActivoSubyacenteTipo());
        
        // Activo Subyacente Codigo
        // ret += String.format("%3s", registroDJ1829.getActivoSubyacenteCodigo());
        ret += String.format("%-3.3s", registroDJ1829.getActivoSubyacenteCodigo());
        
        // Activo Subyacente Otros
        // ret += String.format("%3s", registroDJ1829.getActivoSubyacenteOtros());
        ret += String.format("%-10.10s", registroDJ1829.getActivoSubyacenteOtros());

        // Activo Subyacente Tasa / Spread
        if(registroDJ1829.getActivoSubyacenteTasaSpread() >= 0)	ret += "0";
 
        ret += String.format("%02d", Double.valueOf(registroDJ1829.getActivoSubyacenteTasaSpread()).intValue()).replace(".", "").replace(" ","0");;
        ret += String.format("%04d", String.valueOf(registroDJ1829.getActivoSubyacenteTasaSpread()).split(".").length == 2 ? Integer.valueOf(String.valueOf(registroDJ1829.getActivoSubyacenteTasaSpread()).split(".")[1]) : 0);
        
        // Segundo Activo Subyacente Tipo
        ret += String.format("%1s", registroDJ1829.getSegundoActivoSubyacenteTipo()).replace(" ","0");
        
        // Segundo Activo Subyacente Codigo
        // ret += String.format("%3s", registroDJ1829.getSegundoActivoSubyacenteCodigo());
        ret += String.format("%-3.3s", registroDJ1829.getSegundoActivoSubyacenteCodigo());
        
        // Segundo Activo Subyacente Otros
        // ret += String.format("%3s", registroDJ1829.getSegundoActivoSubyacenteOtros());
        ret += String.format("%-10.10s", registroDJ1829.getSegundoActivoSubyacenteOtros());
        
        // Segundo Activo Subyacente Tasa / Spread
        if(registroDJ1829.getSegundoActivoSubyacenteTasaSpread() >= 0)	ret += "0";
        
        ret += String.format("%02d", Double.valueOf(registroDJ1829.getSegundoActivoSubyacenteTasaSpread()).intValue()).replace(".", "").replace(" ","0");;
        ret += String.format("%04d", String.valueOf(registroDJ1829.getSegundoActivoSubyacenteTasaSpread()).split(".").length == 2 ? Integer.valueOf(String.valueOf(registroDJ1829.getSegundoActivoSubyacenteTasaSpread()).split(".")[1]) : 0);

        //Codigo Precio Futuro
        ret += String.format("%1s", registroDJ1829.getPrecioFuturoCodigo()).replace(" ","0");
        
        //Precio Futuro Precio
        ret += String.format("%015d",registroDJ1829.getPrecioFuturoPrecio().setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValue());

        

        //Moneda
        // ret += String.format("%3s", registroDJ1829.getPrecioFuturoMoneda());
        ret += String.format("%-3.3s", registroDJ1829.getPrecioFuturoMoneda());

        // Unidad
        ret += String.format("%2s", registroDJ1829.getUnidad()).replace(" ","0");

        // Monto Contratado
        //ret += String.format("%015f", registroDJ1829.getMontoContratado());
        ret += String.format( "%15s" , (String.format("%015.0f", registroDJ1829.getMontoContratado()).replace(".", ""))).replace(" ","0");

        
        // Segunda Unidad
        ret += String.format("%2s", registroDJ1829.getSegundaUnidad()).replace(" ","0");
        
        // Segundo Monto Nocional
        //ret += String.format("%015f", registroDJ1829.getSegundoMontoNocional());
        ret += String.format( "%15s" , (String.format("%015.0f", registroDJ1829.getSegundoMontoNocional()).replace(".", ""))).replace(" ","0");

        
        // Fecha de Vencimiento
        ret += Serializador.serializar(registroDJ1829.getFechaVencimiento());
        
        // Fecha de Liquidacion
        String FechaLiquidacion = Serializador.serializar(registroDJ1829.getFechaLiquidacion());
        if (FechaLiquidacion != null && FechaLiquidacion.length() == 8 && !FechaLiquidacion.equals("01010001") && !FechaLiquidacion.equals("01011900") )  {
            // ddMMyyyy 
            FechaLiquidacion = 
                                    FechaLiquidacion.substring(0, 2)      // dd
                                    +FechaLiquidacion.substring(2, 4)        // MM
                                    +FechaLiquidacion.substring(4, 8);     // yyyy
        } else {
            FechaLiquidacion = "00000000";
        }
        ret += FechaLiquidacion;
        
        // Precio Mercado Codigo
        ret += String.format("%1s", registroDJ1829.getPrecioMercadoCodigo());
        
        // Precio Mercado Precio
        ret += String.format("%015d",registroDJ1829.getPrecioMercadoPrecio().setScale(4, RoundingMode.HALF_UP).movePointRight(4).longValue());


        // Valor Justo
        //ret += String.format("%015f", registroDJ1829.getValorJusto());
        ret += String.format( "%15s" , (String.format("%015.0f", registroDJ1829.getValorJusto()).replace(".", ""))).replace(" ","0");


        // Resultado del ejercicio
        //ret += String.format("%015f", registroDJ1829.getResultadoEjercicio());
        ret += String.format( "%15s" , (String.format("%015.0f", registroDJ1829.getResultadoEjercicio()).replace(".", ""))).replace(" ","0");


        // Cuenta Contable del ejercicio
        //ret += String.format("%15s", registroDJ1829.getCuentaContableResultadoEjercicio());
        String Resultado=registroDJ1829.getCuentaContableResultadoEjercicio();
        ret += String.format("%-15s",  (Resultado).length() > 15 ?  (Resultado).substring(0,15) :  (Resultado));

        // Efecto Patrimonio
        //ret += String.format("%015f", registroDJ1829.getEfectoEnPatrimonio());
        ret += String.format( "%15s" , (String.format("%015.0f", registroDJ1829.getEfectoEnPatrimonio()).replace(".", ""))).replace(" ","0");


        // Cuenta Contable patrimonio
        //ret += String.format("%15s", registroDJ1829.getCuentaContableRegistroPatrimonio());
        String Patrimonio=registroDJ1829.getCuentaContableResultadoEjercicio();
        ret += String.format("%-15s",  (Patrimonio).length() > 15 ?  (Patrimonio).substring(0,15) :  (Patrimonio));

        // Comision Pactada
        //ret += String.format("%015f", registroDJ1829.getComisionPactada());
        ret += String.format( "%15s" , (String.format("%015.0f", registroDJ1829.getComisionPactada()).replace(".", ""))).replace(" ","0");


        // Prima total
        //ret += String.format("%015f", registroDJ1829.getPrimaTotal());
        ret += String.format( "%15s" , (String.format("%015.0f", registroDJ1829.getPrimaTotal()).replace(".", ""))).replace(" ","0");


        // Inversion inicial
        //ret += String.format("%015f", registroDJ1829.getInversionInicial());
        ret += String.format( "%15s" , (String.format("%015.0f", registroDJ1829.getInversionInicial()).replace(".", ""))).replace(" ","0");


        // Otros Gastos Contrato
        //ret += String.format("%015f", registroDJ1829.getOtrosGastosContrato());
        ret += String.format( "%15s" , (String.format("%015.0f", registroDJ1829.getOtrosGastosContrato()).replace(".", ""))).replace(" ","0");

        // Otros Ingresos Contrato
        //ret += String.format("%015f", registroDJ1829.getOtrosIngresosContrato());
        ret += String.format( "%15s" , (String.format("%015.0f", registroDJ1829.getOtrosIngresosContrato()).replace(".", ""))).replace(" ","0");

        // Pagos Exterior Monto
        //ret += String.format("%015f", registroDJ1829.getPagosExteriorMonto());
        ret += String.format( "%15s" , (String.format("%015.0f", registroDJ1829.getPagosExteriorMonto()).replace(".", ""))).replace(" ","0");

        // Pagos Exterior Modalidad
        ret += String.format("%1s", registroDJ1829.getPagosExteriorModalidadPago());

        // Resultado del ejercicio
        //ret += String.format("%015f", registroDJ1829.getSaldoGarantiasAlCierre());
        ret += String.format( "%15s" , (String.format("%015.0f", registroDJ1829.getSaldoGarantiasAlCierre()).replace(".", ""))).replace(" ","0");


        /*if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 2");
        }*/

        return ret;
    }
    
    public String generarRegistroTipo3() throws SerializadorException {

        // Tipo de registro y numero de formulario.
        String ret = "311829";

        // Codigo de presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // RUT declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());
        
        // Inversion Nominal
        //ret += String.format("%18f", registroResumen.getSumaResultadoEjercicio());
        ret += String.format( "%18s" , (String.format("%018.0f", registroResumen.getSumaResultadoEjercicio()).replace(".", ""))).replace(" ","0");
        
        // Rescate Nominal
        //ret += String.format("%18f", registroResumen.getSumaEfectoPatrimonio());
        ret += String.format( "%18s" , (String.format("%018.0f", registroResumen.getSumaEfectoPatrimonio()).replace(".", ""))).replace(" ","0");
        
        
        // Total de casos informados.
        ret += String.format("%010d", getCantidadCasosInformados());
        
        // Fecha de presentacion.
        ret += getContexto().getFechaPresentacion();

        // RUT del representante legal.
        ret += Serializador.serializarRut(getContexto().getRutRepresentante());

        // Relleno.
        ret += Serializador.espacios(322);

        /*if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 3");
        }*/

        return ret;
    }


    public String generarRegistroTipo0_2025() throws SerializadorException {

        // Tipo de registro.
        String ret = "0";
        
        // Ano tributario.
        ret += String.format("%04d", getContexto().getAnoTributario()+1L);

        // Numero de formulario.
        ret += "1829";

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
        
        //Periodo tributario
        ret += String.format("%04d", getContexto().getAnoTributario()+1L) + "00";
        
        // Relleno.
        ret += Serializador.espacios(328);

        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 0");
        }
	
        return ret;
    }
    
    public String generarRegistroTipo1_2025() throws SerializadorException {

        // Tipo de registro y precedencia.
        String ret = "11";
       
        // Ano tributario.
        ret += String.format("%04d", getContexto().getAnoTributario()+1L) + "00";
           
        // Numero de formulario.
        ret += "1829";

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
        ret += String.format("5602%07d", getContexto().getTelefonoDeclaracion() % 10000000);

        // Codigo de empresa.
        ret += String.format("%02d", getContexto().getCodigoEmpresa());

        // Numero de cliente.
        ret += String.format("%04d", getContexto().getNumeroCliente());

        // Unidad, caja y paquete.
        ret += Serializador.ceros(13);

        // Relleno.
        ret += Serializador.espacios(219);

        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 1");
        }
		
        return ret;
    }
    
    public String generarRegistroTipo2_2025(final Registro registro) throws SerializadorException {
    	
    	final RegistroDJ1829 registroDJ1829 = new RegistroDJ1829(registro);
        actualizarAcumuladores(registroDJ1829);

        // Tipo de Registro y Numero de Formulario.
        String ret = "21829";
       
        // Codigo de Presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de Declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // Tipo de Declaracion.
        ret += getContexto().getTipoDeclaracion().getCodigo();

        // RUT Declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());

        // RUT Contraparte.
        // ret += Serializador.serializarRut(registroDJ1829.getRutContraparte());
        String rut = Serializador.serializarRut(registroDJ1829.getRutContraparte());
        ret += (rut != null && rut.length() > 9) ? rut.substring(0, 9) : rut;
        
        // Tax ID Contraparte.
        ret += String.format("%-15s", registroDJ1829.getTaxIDContraparte());
        
        // Codigo Pais Contraparte.
        // ret += String.format("%2s", registroDJ1829.getCodigoPaisContraparte());
        ret += String.format("%-2.2s", registroDJ1829.getCodigoPaisContraparte());
        
        // Tipo Relacion Contraparte
        //ret += String.format("%2s", registroDJ1829.getTipoRelacionContraparte());
        String trc = String.format("%2d", Double.valueOf(registroDJ1829.getTipoRelacionContraparte()).intValue());
        // ret += trc.replaceAll("\\s", "0");
        trc = trc.replaceAll("\\s", "0");

        if (trc.length() > 2) {
            trc = trc.substring(0, 2);
        }

        ret += trc;
        
        // Modalidad Contratacion
        // ret += String.format("%1s", registroDJ1829.getModalidadContratacion());
        ret += String.format("%1.1s", registroDJ1829.getModalidadContratacion());
        
        // Acuerdo Marco Tipo
        ret += String.format("%1s", registroDJ1829.getAcuerdoMarcoTipo());   
       
        // Acuerdo Marco Numero      
        ret += String.format("%10d", registroDJ1829.getAcuerdoMarcoNumero().intValue()).replace(".", "").replace(" ","0");;  

        // Acuerdo Marco Suscripcion   
        String AcuerdoMarcoFechaSuscripcion = Serializador.serializar(registroDJ1829.getAcuerdoMarcoFechaSuscripcion());
        if (AcuerdoMarcoFechaSuscripcion != null && AcuerdoMarcoFechaSuscripcion.length() == 8 && !AcuerdoMarcoFechaSuscripcion.equals("01010001") && !AcuerdoMarcoFechaSuscripcion.equals("01011900") ) {
            // ddMMyyyy 
            AcuerdoMarcoFechaSuscripcion = 
                                    AcuerdoMarcoFechaSuscripcion.substring(0, 2)      // dd
                                    +AcuerdoMarcoFechaSuscripcion.substring(2, 4)        // MM
                                    +AcuerdoMarcoFechaSuscripcion.substring(4, 8);   // yyyy
        } else {
            AcuerdoMarcoFechaSuscripcion = "00000000";
        }
        ret += AcuerdoMarcoFechaSuscripcion;
        
        // Contrato Numero
        ret += String.format("%10d", registroDJ1829.getContratoNumero().intValue()).replace(".", "").replace(" ","0");

        // Contrato Fecha Suscripcion      
        ret += Serializador.serializar(registroDJ1829.getContratoFechaSuscripcion());  
        
        // Contrato Vencido en el Ejercicio  
        ret += String.format("%1s", registroDJ1829.getContratoVencidoEjercicio());
        
        // Contrato Estado         
        ret += String.format("%1s", registroDJ1829.getContratoEstado());
        
        // Contrato Tipo
        ret += String.format("%2s", registroDJ1829.getContratoTipo()).replace(" ","0");;

        // Contrato Nombre del Instrumento
        //ret += String.format("%20s", registroDJ1829.getContratoNombreInstrumento());
        String Instrumento=registroDJ1829.getContratoNombreInstrumento();
        ret += String.format("%-20s",  (Instrumento).length() > 20 ?  (Instrumento).substring(0,20) :  (Instrumento));
        
        
        // Contrato Modalidad de Cumplimiento
        ret += String.format("%1s", registroDJ1829.getContratoModalidadCumplimiento());
        
        // Contrato posicion del declarante    **
        //ret += String.format("%1s", registroDJ1829.getContratoPosicionDeclarante());
        String cpd = String.format("%1d", Double.valueOf(registroDJ1829.getContratoPosicionDeclarante()).intValue());
        ret += cpd.replaceAll("\\s", "0");
         
        // Activo Subyacente Tipo
        ret += String.format("%1s", registroDJ1829.getActivoSubyacenteTipo());
        
        // Activo Subyacente Codigo
        // ret += String.format("%3s", registroDJ1829.getActivoSubyacenteCodigo());
        ret += String.format("%-3.3s", registroDJ1829.getActivoSubyacenteCodigo());
        
        // Activo Subyacente Otros
        // ret += String.format("%10s", registroDJ1829.getActivoSubyacenteOtros());
        ret += String.format("%-10.10s", registroDJ1829.getActivoSubyacenteOtros());

        // Activo Subyacente Tasa / Spread
        if(registroDJ1829.getActivoSubyacenteTasaSpread() >= 0)	ret += "0";
 
        ret += String.format("%02d", Double.valueOf(registroDJ1829.getActivoSubyacenteTasaSpread()).intValue()).replace(".", "").replace(" ","0");;
        ret += String.format("%04d", String.valueOf(registroDJ1829.getActivoSubyacenteTasaSpread()).split(".").length == 2 ? Integer.valueOf(String.valueOf(registroDJ1829.getActivoSubyacenteTasaSpread()).split(".")[1]) : 0);
        
        // Segundo Activo Subyacente Tipo
        ret += String.format("%1s", registroDJ1829.getSegundoActivoSubyacenteTipo()).replace(" ","0");
        
        // Segundo Activo Subyacente Codigo
        // ret += String.format("%3s", registroDJ1829.getSegundoActivoSubyacenteCodigo());
        ret += String.format("%-3.3s", registroDJ1829.getSegundoActivoSubyacenteCodigo());
        
        // Segundo Activo Subyacente Otros
        // ret += String.format("%10s", registroDJ1829.getSegundoActivoSubyacenteOtros());
        ret += String.format("%-10.10s", registroDJ1829.getSegundoActivoSubyacenteOtros());
        
        // Segundo Activo Subyacente Tasa / Spread
        if(registroDJ1829.getSegundoActivoSubyacenteTasaSpread() >= 0)	ret += "0";
        
        ret += String.format("%02d", Double.valueOf(registroDJ1829.getSegundoActivoSubyacenteTasaSpread()).intValue()).replace(".", "").replace(" ","0");;
        ret += String.format("%04d", String.valueOf(registroDJ1829.getSegundoActivoSubyacenteTasaSpread()).split(".").length == 2 ? Integer.valueOf(String.valueOf(registroDJ1829.getSegundoActivoSubyacenteTasaSpread()).split(".")[1]) : 0);

        //Codigo Precio Futuro
        ret += String.format("%1s", registroDJ1829.getPrecioFuturoCodigo()).replace(" ","0");
        
        //Precio Futuro Precio
        ret += String.format("%015d",registroDJ1829.getPrecioFuturoPrecio().setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValue());

        

        //Moneda
        // ret += String.format("%3s", registroDJ1829.getPrecioFuturoMoneda());
        ret += String.format("%-3.3s", registroDJ1829.getPrecioFuturoMoneda());

        // Unidad
        ret += String.format("%2s", registroDJ1829.getUnidad()).replace(" ","0");

        // Monto Contratado
        //ret += String.format("%015f", registroDJ1829.getMontoContratado());
        ret += String.format( "%15s" , (String.format("%015.0f", registroDJ1829.getMontoContratado()).replace(".", ""))).replace(" ","0");

        
        // Segunda Unidad
        ret += String.format("%2s", registroDJ1829.getSegundaUnidad()).replace(" ","0");
        
        // Segundo Monto Nocional
        //ret += String.format("%015f", registroDJ1829.getSegundoMontoNocional());
        ret += String.format( "%15s" , (String.format("%015.0f", registroDJ1829.getSegundoMontoNocional()).replace(".", ""))).replace(" ","0");

        
        // Fecha de Vencimiento
        ret += Serializador.serializar(registroDJ1829.getFechaVencimiento());
        
        // Fecha de Liquidacion
        String FechaLiquidacion = Serializador.serializar(registroDJ1829.getFechaLiquidacion());
        if (FechaLiquidacion != null && FechaLiquidacion.length() == 8 && !FechaLiquidacion.equals("01010001") && !FechaLiquidacion.equals("01011900") )  {
            // ddMMyyyy 
            FechaLiquidacion = 
                                    FechaLiquidacion.substring(0, 2)      // dd
                                    +FechaLiquidacion.substring(2, 4)        // MM
                                    +FechaLiquidacion.substring(4, 8);     // yyyy
        } else {
            FechaLiquidacion = "00000000";
        }
        ret += FechaLiquidacion;
        
        // Precio Mercado Codigo
        ret += String.format("%1s", registroDJ1829.getPrecioMercadoCodigo());
        
        // Precio Mercado Precio
        ret += String.format("%015d",registroDJ1829.getPrecioMercadoPrecio().setScale(4, RoundingMode.HALF_UP).movePointRight(4).longValue());


        // Valor Justo
        //ret += String.format("%015f", registroDJ1829.getValorJusto());
        ret += String.format( "%15s" , (String.format("%015.0f", registroDJ1829.getValorJusto()).replace(".", ""))).replace(" ","0");


        // Resultado del ejercicio
        //ret += String.format("%015f", registroDJ1829.getResultadoEjercicio());
        ret += String.format( "%15s" , (String.format("%015.0f", registroDJ1829.getResultadoEjercicio()).replace(".", ""))).replace(" ","0");


        // Cuenta Contable del ejercicio
        //ret += String.format("%15s", registroDJ1829.getCuentaContableResultadoEjercicio());
        String Resultado=registroDJ1829.getCuentaContableResultadoEjercicio();
        ret += String.format("%-15s",  (Resultado).length() > 15 ?  (Resultado).substring(0,15) :  (Resultado));

        // Efecto Patrimonio
        //ret += String.format("%015f", registroDJ1829.getEfectoEnPatrimonio());
        ret += String.format( "%15s" , (String.format("%015.0f", registroDJ1829.getEfectoEnPatrimonio()).replace(".", ""))).replace(" ","0");


        // Cuenta Contable patrimonio
        //ret += String.format("%15s", registroDJ1829.getCuentaContableRegistroPatrimonio());
        String Patrimonio=registroDJ1829.getCuentaContableRegistroPatrimonio();
        ret += String.format("%-15s",  (Patrimonio).length() > 15 ?  (Patrimonio).substring(0,15) :  (Patrimonio));

        // Comision Pactada
        //ret += String.format("%015f", registroDJ1829.getComisionPactada());
        ret += String.format( "%15s" , (String.format("%015.0f", registroDJ1829.getComisionPactada()).replace(".", ""))).replace(" ","0");


        // Prima total
        //ret += String.format("%015f", registroDJ1829.getPrimaTotal());
        ret += String.format( "%15s" , (String.format("%015.0f", registroDJ1829.getPrimaTotal()).replace(".", ""))).replace(" ","0");


        // Inversion inicial
        //ret += String.format("%015f", registroDJ1829.getInversionInicial());
        ret += String.format( "%15s" , (String.format("%015.0f", registroDJ1829.getInversionInicial()).replace(".", ""))).replace(" ","0");


        // Otros Gastos Contrato
        //ret += String.format("%015f", registroDJ1829.getOtrosGastosContrato());
        ret += String.format( "%15s" , (String.format("%015.0f", registroDJ1829.getOtrosGastosContrato()).replace(".", ""))).replace(" ","0");

        // Otros Ingresos Contrato
        //ret += String.format("%015f", registroDJ1829.getOtrosIngresosContrato());
        ret += String.format( "%15s" , (String.format("%015.0f", registroDJ1829.getOtrosIngresosContrato()).replace(".", ""))).replace(" ","0");

        // Pagos Exterior Monto
        //ret += String.format("%015f", registroDJ1829.getPagosExteriorMonto());
        ret += String.format( "%15s" , (String.format("%015.0f", registroDJ1829.getPagosExteriorMonto()).replace(".", ""))).replace(" ","0");

        // Pagos Exterior Modalidad
        ret += String.format("%1s", registroDJ1829.getPagosExteriorModalidadPago());

        // Resultado del ejercicio
        //ret += String.format("%015f", registroDJ1829.getSaldoGarantiasAlCierre());
        ret += String.format( "%15s" , (String.format("%015.0f", registroDJ1829.getSaldoGarantiasAlCierre()).replace(".", ""))).replace(" ","0");


        /*if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 2");
        }*/

        return ret;
    }
    
    public String generarRegistroTipo3_2025() throws SerializadorException {

        // Tipo de registro y numero de formulario.
        String ret = "311829";

        // Codigo de presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // RUT declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());
        
        // Inversion Nominal
        //ret += String.format("%18f", registroResumen.getSumaResultadoEjercicio());
        ret += String.format( "%18s" , (String.format("%018.0f", registroResumen.getSumaResultadoEjercicio()).replace(".", ""))).replace(" ","0");
        
        // Rescate Nominal
        //ret += String.format("%18f", registroResumen.getSumaEfectoPatrimonio());
        ret += String.format( "%18s" , (String.format("%018.0f", registroResumen.getSumaEfectoPatrimonio()).replace(".", ""))).replace(" ","0");
        
        
        // Total de casos informados.
        ret += String.format("%010d", getCantidadCasosInformados());
        
        // Fecha de presentacion.
        ret += getContexto().getFechaPresentacion();

        // RUT del representante legal.
        ret += Serializador.serializarRut(getContexto().getRutRepresentante());

        // Relleno.
        ret += Serializador.espacios(336);

        /*if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 3");
        }*/

        return ret;
    }
    
    @Override
    public void generarResumen() throws SerializadorException {
        if (versionDJ >= 2025){
            print(generarRegistroTipo3_2025());
            println("");
        }
        else{
            print(generarRegistroTipo3());
            println("");
        }
        
    }
    
    private long getCantidadTotalRegistros() {
        return getCantidadCasosInformados() + 3;
    }
}
