package cl.bcs.normativa.generacion;

import java.io.IOException;
import java.sql.SQLException;

import cl.bcs.normativa.data.Registro;
import cl.bcs.normativa.registros.RegistroDJ1949;

public final class GeneradorDJ1949 extends Generador {
	
	private static final int LARGO_REGISTRO = 113;
	private final RegistroDJ1949 registroResumen;
    private long totalRegistros;
    
    public GeneradorDJ1949(final ContextoGeneracion contexto)
            throws SQLException, IOException, SerializadorException {
        super(contexto);
        this.totalRegistros = 0;
        this.registroResumen = new RegistroDJ1949();
    }
    
    private void actualizarAcumuladores(final RegistroDJ1949 registroDJ1949) {
        this.contarCasosInformados();
        this.totalRegistros += 1;
        registroResumen.acumular(registroDJ1949);
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
        ret += "1949";

        // RUT declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());
        
        // Total de registros a transmitir.
        ret += String.format("%010d", getCantidadTotalRegistros());

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
        ret += Serializador.espacios(546);

        //if (ret.length() != LARGO_REGISTRO) {
        //    throw new SerializadorException("Largo invalido registro tipo 0");
        //}
	
        return ret;
    }
    
    public String generarRegistroTipo1() throws SerializadorException {

        // Tipo de registro y precedencia.
        String ret = "11";
          
        // Ano tributario.
        //ret += String.format("%04d", getContexto().getAnoTributario());
        ret += String.format("%04d", getContexto().getAnoTributario()+1L)  ;
        
        // Numero de formulario.
        ret += "001949";

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

            // Codigo certificacion
    		// Codigo de empresa.
    		ret += String.format("%02d", getContexto().getCodigoEmpresa());

    		// Numero de cliente.
    		ret += String.format("%04d", getContexto().getNumeroCliente());

		//localizacion del docto	
        // Unidad, caja y paquete.
        ret += Serializador.ceros(13);
        
        // Relleno.
        ret += Serializador.espacios(431);

       // if (ret.length() != LARGO_REGISTRO) {
        //    throw new SerializadorException("Largo invalido registro tipo 1");
        //}
		 
        return ret;
    }
    
    
   
    
      public String generarRegistroTipo2(final Registro registro)
            throws SerializadorException {
    	
    	final RegistroDJ1949 registroDJ1949 = new RegistroDJ1949(registro);
        actualizarAcumuladores(registroDJ1949);

       // Tipo de Registro y Numero de Formulario.
        String ret = "21949";
        
        // Codigo de Presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de Declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // Tipo de Declaracion.
        ret += getContexto().getTipoDeclaracion().getCodigo();

        // RUT Declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());
        
        // Fecha del dividendo distribuido
        ret += Serializador.serializar(registroDJ1949.getFecMovto());
        
        // RUT  de la sociedad pagadora del dividendo
        ret += Serializador.serializarRut(registroDJ1949.getRutSociedad());
        
        // Tipo S.A.
        ret += String.format("%1s", registroDJ1949.getTipoSociedad());
        
        // RUT del beneficiario del los dividendos distribuidos
        ret += Serializador.serializarRut(registroDJ1949.getRutCliente());
        
        //Cantidad de acciones al 31/12
        ret += String.format( "%15s" , (String.format("%015.0f", registroDJ1949.getCantCuotas3112()).replace(".", ""))).replace(" ","0");
        
        //Con Credito por IDPC  Generados a contar del 01.01.2017
        ret += String.format( "%18s" , (String.format("%018.0f", registroDJ1949.getMtoDivReaAfecGCCCIDPC_C6()).replace(".", ""))).replace(" ","0");
        
        //Con Credito por IDPC  Acumulados hasta el 31.12.2016
        ret += String.format( "%18s" , (String.format("%018.0f", registroDJ1949.getMtoDivReaAfecGCCCIDPCAL311216_C7()).replace(".", ""))).replace(" ","0");
        
        //Con Derecho a Credito por IDPC Voluntario
        ret += String.format( "%18s" , (String.format("%018.0f", registroDJ1949.getMtoDivReaAfecGCCDIDPCVol_C8()).replace(".", ""))).replace(" ","0");
       
        //Sin Derecho a Credito
        ret += String.format( "%18s" , (String.format("%018.0f", registroDJ1949.getMtoDivReaAfecGCSinDer_C9()).replace(".", ""))).replace(" ","0");
        
        //Rentas provenientes del registro RAP y diferecnia inicial de sociedad acogida al ex Art.14 TER A) LIR
        ret += String.format( "%18s" , (String.format("%018.0f", registroDJ1949.getMtoDivReaREXTribCumpRentasRap_C10()).replace(".", ""))).replace(" ","0");
        
        //Otras rentas percibidas Sin prioridad en su orden de imputacion
        ret += String.format( "%18s" , (String.format("%018.0f", registroDJ1949.getMtoDivReaREXTribCumpOtrasRentas_C11()).replace(".", ""))).replace(" ","0");
        
        //Exceso distribuciones desproporcionadas
        ret += String.format( "%18s" , (String.format("%018.0f", registroDJ1949.getMtoDivReaREXTribCumpExcDist_C12()).replace(".", ""))).replace(" ","0");
        
        //Utilidades afectadas con ISFUT
        ret += String.format( "%18s" , (String.format("%018.0f", registroDJ1949.getMtoDivReaREXTribCumpUtiISFUT_C13()).replace(".", ""))).replace(" ","0");
        
        //Rentas generadas hasta el 31.12.1983 y7o utilidades afectadas on impuesto sustitutivo al FUT (ISFUT) Ley 21.210
        ret += String.format( "%18s" , (String.format("%018.0f", registroDJ1949.getMtoDivReaREXTribCumpRentasISFUT_C14()).replace(".", ""))).replace(" ","0");
        
        //Rentas Exentas de Impuesto Global Complementario (IGC) (Articulo 11, Ley 18.401), Afectas a Impuesto Adicional
        ret += String.format( "%18s" , (String.format("%018.0f", registroDJ1949.getMtoDivReaREXRentExIGCAfecIAart11_C15()).replace(".", ""))).replace(" ","0");
        
        //Rentas Exentas de Impuesto Global Complementario (IGC) y/o Impuesto Adicional (IA)
        ret += String.format( "%18s" , (String.format("%018.0f", registroDJ1949.getMtoDivReaREXRentExIGCIA_C16()).replace(".", ""))).replace(" ","0");
        
        //Ingresos No Constitutivos de  Renta
        ret += String.format( "%18s" , (String.format("%018.0f", registroDJ1949.getMtoDivReaREXNoConstRenta_C17()).replace(".", ""))).replace(" ","0");
        
        //No Sujetos a Restitucion generados Hasta el 31.12.2019 Sin derecho a devolucion
        ret += String.format( "%18s" , (String.format("%018.0f", registroDJ1949.getCIGCDesde01012017RentAfecNoRestSDD_C18()).replace(".", ""))).replace(" ","0");
        
        //No Sujetos a Restitucion generados Hasta el 31.12.2019 Con derecho a devolucion
        ret += String.format( "%18s" , (String.format("%018.0f", registroDJ1949.getCIGCDesde01012017RentAfecNoRestCDD_C19()).replace(".", ""))).replace(" ","0");
        
        //No Sujetos a Restitucion generados a contar del 01.01.2020 Sin derecho a devolucion
        ret += String.format( "%18s" , (String.format("%018.0f", registroDJ1949.getCIGCDesde01012017RentAfecNoRestSDD_C20()).replace(".", ""))).replace(" ","0");
        
        //No Sujetos a Restitucion generados a contar del 01.01.2020 Con derecho a devolucion
        ret += String.format( "%18s" , (String.format("%018.0f", registroDJ1949.getCIGCDesde01012017RentAfecNoRestCDD_C21()).replace(".", ""))).replace(" ","0");
        
        //Sujetos a Restitucion Sin derecho a devolucion
        ret += String.format( "%18s" , (String.format("%018.0f", registroDJ1949.getCIGCDesde01012017RentAfecRestSDD_C22()).replace(".", ""))).replace(" ","0");
        
        //Sujetos a Restitucion Con derecho a devolucion
        ret += String.format( "%18s" , (String.format("%018.0f", registroDJ1949.getCIGCDesde01012017RentAfecRestCDD_C23()).replace(".", ""))).replace(" ","0");
        
        //Asociados a Rentas Exentas Sin derecho a devolucion
        ret += String.format( "%18s" , (String.format("%018.0f", registroDJ1949.getCIGCDesde01012017RentExenRestSDD_C24()).replace(".", ""))).replace(" ","0");
        
        //Asociados a Rentas Exentas Con derecho a devolucion
        ret += String.format( "%18s" , (String.format("%018.0f", registroDJ1949.getCIGCDesde01012017RentExenRestCDD_C25()).replace(".", ""))).replace(" ","0");
        
        //Asociados a rentas afectas Credito por IPE
        ret += String.format( "%18s" , (String.format("%018.0f", registroDJ1949.getCIGCDesde01012017CredIPE_C26()).replace(".", ""))).replace(" ","0");
        
        //Asociados a rentas afectas Sin derecho a devolucion
        ret += String.format( "%18s" , (String.format("%018.0f", registroDJ1949.getCIGCHasta31122016RentAfecSDD_C27()).replace(".", ""))).replace(" ","0");
        
        //Asociados a rentas afectas Con derecho a devolucion
        ret += String.format( "%18s" , (String.format("%018.0f", registroDJ1949.getCIGCHasta31122016RentAfecCDD_C28()).replace(".", ""))).replace(" ","0");
        
        //Asociados a rentas exentas Sin derecho a devolucion
        ret += String.format( "%18s" , (String.format("%018.0f", registroDJ1949.getCIGCHasta31122016RentExenSDD_C29()).replace(".", ""))).replace(" ","0");
        
        //Asociados a rentas exentas Con derecho a devolucion
        ret += String.format( "%18s" , (String.format("%018.0f", registroDJ1949.getCIGCHasta31122016RentExenCDD_C30()).replace(".", ""))).replace(" ","0");
        
        //Asociados a rentas exentas Credito por IPE
        ret += String.format( "%18s" , (String.format("%018.0f", registroDJ1949.getCIGCHasta31122016CredIPE_C31()).replace(".", ""))).replace(" ","0");
        
        //Credito por Impuesto Tasa Adicional ex. Art. 21 LIR. 
        ret += String.format( "%18s" , (String.format("%018.0f", registroDJ1949.getCIGCImptoTasaAdicional_C32()).replace(".", ""))).replace(" ","0");
        
        //Devolucion de Capital art.17 N 7 LIR 
        ret += String.format( "%18s" , (String.format("%018.0f", registroDJ1949.getDevCapArt17N7LIR_C33()).replace(".", ""))).replace(" ","0");
        
        //Numero de Certificado
        ret += String.format("%07d", registroDJ1949.getNumCertificado_C34());
        
        ret += Serializador.espacios(59);
 
        /*  if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 2, Rut: " + registroDJ1889.getRutCliente() + " ");
            
        }
        
			*/
        return ret;  
    }
    
    public String generarRegistroTipo3() throws SerializadorException {

       // Tipo de registro y numero de formulario.
        String ret = "311949";

            // Codigo de presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // RUT declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());
        
      //Saldo de Ahorro Neto del ejercicio Negativo
        ret += String.format( "%15s" , (String.format("%015.0f", registroResumen.getsumaCantCuotas3112()).replace(".", ""))).replace(" ","0");
         
        
        //Saldo de Ahorro Neto del ejercicio Negativo
        ret += String.format( "%20s" , (String.format("%020.0f", registroResumen.getsumaMtoDivReaAfecGCCCIDPC_C6()).replace(".", ""))).replace(" ","0");
         
        //Con Credito por IDPC  Acumulados hasta el 31.12.2016
        ret += String.format( "%20s" , (String.format("%020.0f", registroResumen.getsumaMtoDivReaAfecGCCCIDPCAL311216_C7()).replace(".", ""))).replace(" ","0");
        
        //Con Derecho a Credito por IDPC Voluntario
        ret += String.format( "%20s" , (String.format("%020.0f", registroResumen.getsumaMtoDivReaAfecGCCDIDPCVol_C8()).replace(".", ""))).replace(" ","0");
       
        //Sin Derecho a Credito
        ret += String.format( "%20s" , (String.format("%020.0f", registroResumen.getsumaMtoDivReaAfecGCSinDer_C9()).replace(".", ""))).replace(" ","0");
        
        //Rentas provenientes del registro RAP y diferecnia inicial de sociedad acogida al ex Art.14 TER A) LIR
        ret += String.format( "%20s" , (String.format("%020.0f", registroResumen.getsumaMtoDivReaREXTribCumpRentasRap_C10()).replace(".", ""))).replace(" ","0");
        
        //Otras rentas percibidas Sin prioridad en su orden de imputacion
        ret += String.format( "%20s" , (String.format("%020.0f", registroResumen.getsumaMtoDivReaREXTribCumpOtrasRentas_C11()).replace(".", ""))).replace(" ","0");
        
        //Exceso distribuciones desproporcionadas
        ret += String.format( "%20s" , (String.format("%020.0f", registroResumen.getsumaMtoDivReaREXTribCumpExcDist_C12()).replace(".", ""))).replace(" ","0");
        
        //Utilidades afectadas con ISFUT
        ret += String.format( "%20s" , (String.format("%020.0f", registroResumen.getsumaMtoDivReaREXTribCumpUtiISFUT_C13()).replace(".", ""))).replace(" ","0");
        
        //Rentas generadas hasta el 31.12.1983 y7o utilidades afectadas on impuesto sustitutivo al FUT (ISFUT) Ley 21.210
        ret += String.format( "%20s" , (String.format("%020.0f", registroResumen.getsumaMtoDivReaREXTribCumpRentasISFUT_C14()).replace(".", ""))).replace(" ","0");
        
        //Rentas Exentas de Impuesto Global Complementario (IGC) (Articulo 11, Ley 18.401), Afectas a Impuesto Adicional
        ret += String.format( "%20s" , (String.format("%020.0f", registroResumen.getsumaMtoDivReaREXRentExIGCAfecIAart11_C15()).replace(".", ""))).replace(" ","0");
        
        //Rentas Exentas de Impuesto Global Complementario (IGC) y/o Impuesto Adicional (IA)
        ret += String.format( "%20s" , (String.format("%020.0f", registroResumen.getsumaMtoDivReaREXRentExIGCIA_C16()).replace(".", ""))).replace(" ","0");
        
        //Ingresos No Constitutivos de  Renta
        ret += String.format( "%20s" , (String.format("%020.0f", registroResumen.getsumaMtoDivReaREXNoConstRenta_C17()).replace(".", ""))).replace(" ","0");
        
        //No Sujetos a Restitucion generados Hasta el 31.12.2019 Sin derecho a devolucion
        ret += String.format( "%20s" , (String.format("%020.0f", registroResumen.getsumaCIGCDesde01012017RentAfecNoRestSDD_C18()).replace(".", ""))).replace(" ","0");
        
        //No Sujetos a Restitucion generados Hasta el 31.12.2019 Con derecho a devolucion
        ret += String.format( "%20s" , (String.format("%020.0f", registroResumen.getsumaCIGCDesde01012017RentAfecNoRestCDD_C19()).replace(".", ""))).replace(" ","0");
        
        //No Sujetos a Restitucion generados a contar del 01.01.2020 Sin derecho a devolucion
        ret += String.format( "%20s" , (String.format("%020.0f", registroResumen.getsumaCIGCDesde01012017RentAfecNoRestSDD_C20()).replace(".", ""))).replace(" ","0");
        
        //No Sujetos a Restitucion generados a contar del 01.01.2020 Con derecho a devolucion
        ret += String.format( "%20s" , (String.format("%020.0f", registroResumen.getsumaCIGCDesde01012017RentAfecNoRestCDD_C21()).replace(".", ""))).replace(" ","0");
        
        //Sujetos a Restitucion Sin derecho a devolucion
        ret += String.format( "%20s" , (String.format("%020.0f", registroResumen.getsumaCIGCDesde01012017RentAfecRestSDD_C22()).replace(".", ""))).replace(" ","0");
        
        //Sujetos a Restitucion Con derecho a devolucion
        ret += String.format( "%20s" , (String.format("%020.0f", registroResumen.getsumaCIGCDesde01012017RentAfecRestCDD_C23()).replace(".", ""))).replace(" ","0");
        
        //Asociados a Rentas Exentas Sin derecho a devolucion
        ret += String.format( "%20s" , (String.format("%020.0f", registroResumen.getsumaCIGCDesde01012017RentExenRestSDD_C24()).replace(".", ""))).replace(" ","0");
        
        //Asociados a Rentas Exentas Con derecho a devolucion
        ret += String.format( "%20s" , (String.format("%020.0f", registroResumen.getsumaCIGCDesde01012017RentExenRestCDD_C25()).replace(".", ""))).replace(" ","0");
        
        //Asociados a rentas afectas Credito por IPE
        ret += String.format( "%20s" , (String.format("%020.0f", registroResumen.getsumaCIGCDesde01012017CredIPE_C26()).replace(".", ""))).replace(" ","0");
        
        //Asociados a rentas afectas Sin derecho a devolucion
        ret += String.format( "%20s" , (String.format("%020.0f", registroResumen.getsumaCIGCHasta31122016RentAfecSDD_C27()).replace(".", ""))).replace(" ","0");
        
        //Asociados a rentas afectas Con derecho a devolucion
        ret += String.format( "%20s" , (String.format("%020.0f", registroResumen.getsumaCIGCHasta31122016RentAfecCDD_C28()).replace(".", ""))).replace(" ","0");
        
        //Asociados a rentas exentas Sin derecho a devolucion
        ret += String.format( "%20s" , (String.format("%020.0f", registroResumen.getsumaCIGCHasta31122016RentExenSDD_C29()).replace(".", ""))).replace(" ","0");
        
        //Asociados a rentas exentas Con derecho a devolucion
        ret += String.format( "%20s" , (String.format("%020.0f", registroResumen.getsumaCIGCHasta31122016RentExenCDD_C30()).replace(".", ""))).replace(" ","0");
        
        //Asociados a rentas exentas Credito por IPE
        ret += String.format( "%20s" , (String.format("%020.0f", registroResumen.getsumaCIGCHasta31122016CredIPE_C31()).replace(".", ""))).replace(" ","0");
        
        //Credito por Impuesto Tasa Adicional ex. Art. 21 LIR. 
        ret += String.format( "%20s" , (String.format("%020.0f", registroResumen.getsumaCIGCImptoTasaAdicional_C32()).replace(".", ""))).replace(" ","0");
        
        //Devolucion de Capital art.17 N 7 LIR 
        ret += String.format( "%20s" , (String.format("%020.0f", registroResumen.getsumaDevCapArt17N7LIR_C33()).replace(".", ""))).replace(" ","0");
        
        
        // Total de casos informados.
        ret += String.format("%010d", getCantidadCasosInformados());
        
         // Fecha de presentacion.
        ret += getContexto().getFechaPresentacion();

        // RUT del representante legal.
        ret += Serializador.serializarRut(getContexto().getRutRepresentante());
        
         // Relleno.
        ret += Serializador.ceros(9);

        /* if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 3");
        }
     		*/
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
