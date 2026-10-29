package cl.bcs.normativa.generacion;

import java.io.IOException;
import java.sql.SQLException;

import cl.bcs.normativa.data.Registro;
import cl.bcs.normativa.registros.RegistroDJ1932;

public final class GeneradorDJ1932 extends Generador {
	
	private static final int LARGO_REGISTRO = 205;
	private final RegistroDJ1932 registroResumen;
    private long totalRegistros;
    
    public GeneradorDJ1932(final ContextoGeneracion contexto)
            throws SQLException, IOException, SerializadorException {
        super(contexto);
        this.totalRegistros = 0;
        this.registroResumen = new RegistroDJ1932();
    }
    
    private void actualizarAcumuladores(final RegistroDJ1932 registroDJ1932) {
        this.contarCasosInformados();
        this.totalRegistros += 1;
        registroResumen.acumular(registroDJ1932);
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
        ret += "1932";

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
        
        // Relleno.
        ret += Serializador.espacios(116);

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
   
         // Mes tributario.
        //ret += String.format("%02d", getContexto().getMesTributario());
        
       // Numero de formulario.
        ret += "1932";

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
        
       // Fono.
        ret += String.format("5602%07d", getContexto().getTelefonoDeclaracion() % 10000000);

        // Fax.
        ret += "56020000000";

        // Codigo de empresa.
        ret += String.format("%02d", getContexto().getCodigoEmpresa());

        // Numero de cliente.
        ret += String.format("%04d", getContexto().getNumeroCliente());

        // FALTA AGREGAR A CONTEXTO
        // Calidad del declarante
        ret+= "1";
        		
        // Unidad, caja y paquete.
        ret += Serializador.ceros(13);

        /*if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 1");
        }
		*/
        return ret;
    }
    
    public String generarRegistroTipo2(final Registro registro)
            throws SerializadorException {
    	
    	final RegistroDJ1932 registroDJ1932 = new RegistroDJ1932(registro);
        actualizarAcumuladores(registroDJ1932);

        // Tipo de Registro y Numero de Formulario.
        String ret = "21932";
       
        // Codigo de Presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

       // Folio de Declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

       // Tipo de Declaracion.
        ret += getContexto().getTipoDeclaracion().getCodigo();

        // RUT Declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());

        // Mes.
        ret += String.format("%02d", registroDJ1932.getMes());
        
       
        // Rut Depositante
        ret += Serializador.serializarRut(registroDJ1932.getRutDepositanteInversionistaPrestamista());
        
        // Tax ID
        //ret += String.format("%15s", registroDJ1932.getTaxID()).replace("-", "");
        ret += String.format("%15s", Serializador.serializarRut(registroDJ1932.getTaxID()));
        
        
        
       // Pais
        ret += String.format("%2s", registroDJ1932.getPais());
        
        // Monto deposito
        //ret += String.format("%15d", registroDJ1932.getDepositosInversionesPrestamos()); 
        //ret += String.format("%15d", Double.valueOf(registroDJ1932.getDepositosInversionesPrestamos()).intValue());
        ret += String.format( "%15s" , (String.format("%015.0f", registroDJ1932.getDepositosInversionesPrestamos()).replace(".", ""))).replace(" ","0");
        
        // Rescates o Devoluciones
        //ret += String.format("%15d", registroDJ1932.getRescatesDevoluciones()); VER
        //ret += String.format("%15d", Double.valueOf(registroDJ1932.getRescatesDevoluciones()).intValue());
        ret += String.format( "%15s" , (String.format("%015.0f", registroDJ1932.getRescatesDevoluciones()).replace(".", ""))).replace(" ","0");
        
         // Intereses percibidas
        //ret += String.format("%15d", registroDJ1932.getInteresesPercibidas());
        //ret += String.format("%15d", Double.valueOf(registroDJ1932.getInteresesPercibidas()).intValue());
        ret += String.format( "%15s" , (String.format("%015.0f", registroDJ1932.getInteresesPercibidas()).replace(".", ""))).replace(" ","0");
        
        // Dividendos percibidas
        //ret += String.format("%15d", registroDJ1932.getDividendosPercibidas());
        //ret += String.format("%15d", Double.valueOf(registroDJ1932.getDividendosPercibidas()).intValue());
        ret += String.format( "%15s" , (String.format("%015.0f", registroDJ1932.getDividendosPercibidas()).replace(".", ""))).replace(" ","0");
        
        // Contratos por diferencias percibidas
        //ret += String.format("%15d", registroDJ1932.getContratosPorDiferenciaPercibidas());
        //ret += String.format("%15d", Double.valueOf(registroDJ1932.getContratosPorDiferenciaPercibidas()).intValue());
        ret += String.format( "%15s" , (String.format("%015.0f", registroDJ1932.getContratosPorDiferenciaPercibidas()).replace(".", ""))).replace(" ","0");
        
        // Otras Rentas
        //ret += String.format("%15d", registroDJ1932.getOtrasRentasPercibidas());
        //ret += String.format("%15d", Double.valueOf(registroDJ1932.getOtrasRentasPercibidas()).intValue());
        ret += String.format( "%15s" , (String.format("%015.0f", registroDJ1932.getOtrasRentasPercibidas()).replace(".", ""))).replace(" ","0");
         
        // Intereses devengados
        //ret += String.format("%15d", registroDJ1932.getInteresesDevengadas());
        //ret += String.format("%15d", Double.valueOf(registroDJ1932.getInteresesDevengadas()).intValue());
        ret += String.format( "%15s" , (String.format("%015.0f", registroDJ1932.getInteresesDevengadas()).replace(".", ""))).replace(" ","0");
        
        // Otras rentas devengadas
        //ret += String.format("%15d", registroDJ1932.getOtrasRentasDevengadas());
        //ret += String.format("%15d", Double.valueOf(registroDJ1932.getOtrasRentasDevengadas()).intValue());
        ret += String.format( "%15s" , (String.format("%015.0f", registroDJ1932.getOtrasRentasDevengadas()).replace(".", ""))).replace(" ","0");
        
        //Fuente (Pais) de la Renta
        //ret+= "CL";
        ret += String.format("%-3s", registroDJ1932.getCodPaisRenta());
        
        // Relleno.
        ret += Serializador.espacios(31);
        
       /*if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 2");
        }
        */
        return ret;
    }
    
    public String generarRegistroTipo3() throws SerializadorException {

        // Tipo de registro y numero de formulario.
        String ret = "311932";

        // Codigo de presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

         // RUT declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());
          
        
        // Total de casos informados.
        ret += String.format("%010d", getCantidadCasosInformados());
        
        // Total depositos inversiones o prestamos
        //ret += String.format("%18d", registroResumen.getSumaDepositosInversionesPrestamos());
        //ret += String.format("%18d", Double.valueOf(registroResumen.getSumaDepositosInversionesPrestamos()).intValue());
        ret += String.format( "%18s" , (String.format("%018.0f", registroResumen.getSumaDepositosInversionesPrestamos()).replace(".", ""))).replace(" ","0");
        
         // Total rescates o devoluciones
        //ret += String.format("%18d", registroResumen.getSumaRescatesDevoluciones());
        //ret += String.format("%18d", Double.valueOf(registroResumen.getSumaRescatesDevoluciones()).intValue());
        ret += String.format( "%18s" , (String.format("%018.0f", registroResumen.getSumaRescatesDevoluciones()).replace(".", ""))).replace(" ","0");
        
        // Total intereses percibidos
        //ret += String.format("%18d", registroResumen.getSumaInteresesPercibidas());
        //ret += String.format("%18d", Double.valueOf(registroResumen.getSumaInteresesPercibidas()).intValue());
        ret += String.format( "%18s" , (String.format("%018.0f", registroResumen.getSumaInteresesPercibidas()).replace(".", ""))).replace(" ","0");
        
        // Total dividendos percibidos
        //ret += String.format("%18d", registroResumen.getSumaDividendosPercibidas());
        //ret += String.format("%18d", Double.valueOf(registroResumen.getSumaDividendosPercibidas()).intValue());
        ret += String.format( "%18s" , (String.format("%018.0f", registroResumen.getSumaDividendosPercibidas()).replace(".", ""))).replace(" ","0");
        
        // Total contratos por diferencia percibidos
        //ret += String.format("%18d", registroResumen.getSumaContratosPorDiferenciaPercibidas());
        //ret += String.format("%18d", Double.valueOf(registroResumen.getSumaContratosPorDiferenciaPercibidas()).intValue());
        ret += String.format( "%18s" , (String.format("%018.0f", registroResumen.getSumaContratosPorDiferenciaPercibidas()).replace(".", ""))).replace(" ","0");
        
        // Total otras rentas percibidas
        //ret += String.format("%18d", registroResumen.getSumaOtrasRentasPercibidas());
        //ret += String.format("%18d", Double.valueOf(registroResumen.getSumaOtrasRentasPercibidas()).intValue());
        ret += String.format( "%18s" , (String.format("%018.0f", registroResumen.getSumaOtrasRentasPercibidas()).replace(".", ""))).replace(" ","0");
        
        // Total intereses devengados
        //ret += String.format("%18d", registroResumen.getSumaInteresesDevengadas());
        //ret += String.format("%18d", Double.valueOf(registroResumen.getSumaInteresesDevengadas()).intValue());
        ret += String.format( "%18s" , (String.format("%018.0f", registroResumen.getSumaInteresesDevengadas()).replace(".", ""))).replace(" ","0");
        
        // Total otras rentas devengadas
        //ret += String.format("%18d", registroResumen.getSumaOtrasRentasDevengadas());
        //ret += String.format("%18d", Double.valueOf(registroResumen.getSumaOtrasRentasDevengadas()).intValue());
        ret += String.format( "%18s" , (String.format("%018.0f", registroResumen.getSumaOtrasRentasDevengadas()).replace(".", ""))).replace(" ","0");
        
        // Fecha de presentacion.
        ret += getContexto().getFechaPresentacion();

        // RUT del representante legal.
        ret += Serializador.serializarRut(getContexto().getRutRepresentante());

        // Relleno.
        ret += Serializador.espacios(10);

        /*if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 3");
        }
		*/
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
