package cl.bcs.normativa.generacion;

import java.io.IOException;
import java.sql.SQLException;

import cl.bcs.normativa.data.Registro;
import cl.bcs.normativa.registros.RegistroDJ1820;

/**
 * Implementaci&oacute;n del generador de la DJ1820.
 *
 *
 */
public final class GeneradorDJ1820 extends Generador {

    private int LARGO_REGISTRO = 227;

    /**
     * Registro para agrupar dividendos de un mismo cliente.
     */
    private RegistroDJ1820 registroAgrupador;


    /**
     * Registro resumen de la DJ.
     */
    private RegistroDJ1820 registroResumen;
    private long totalRegistros;
    private long versionDJ;
    /**
     * Constructor.
     *
     * @param contexto
     *            El contexto de generaci&oacute;n.
     * @throws SQLException
     *             En caso de error SQL.
     * @throws IOException
     *             En caso de error de I/O.
     * @throws SerializadorException
     *             En caso de error al serializar.
     */
    public GeneradorDJ1820(final ContextoGeneracion contexto) throws SQLException, IOException, SerializadorException {
        super(contexto);
        this.totalRegistros = 0;
        this.registroResumen = new RegistroDJ1820();
        this.versionDJ=getContexto().getAnoTributario();
        if (versionDJ >= 2025){
            LARGO_REGISTRO -= 10;
        }
    }



    /**
     * Actualiza los acumuladores.
     *
     * @param registroDJ1820
     *            Registro DJ1820.
     */
    private void actualizarAcumuladores(final RegistroDJ1820 registroDJ1820) {
        this.contarCasosInformados();
        this.totalRegistros += 1;
        registroResumen.acumular(registroDJ1820);
    }

    private void agrupar(final RegistroDJ1820 registroDJ1820) {
        registroAgrupador.acumular(registroDJ1820);
    }

    private boolean corte(final RegistroDJ1820 registroDJ1820) {
        if (registroAgrupador.getRutCliente().compareTo(registroDJ1820.getRutCliente()) != 0) {
            return true;
        }
        return false;
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

    /**
     * Genera registro tipo 0.
     *
     * @return Registro tipo 0.
     * @throws SerializadorException
     *             En caso de error al serializar.
     */
    @Override
    public String generarRegistroTipo0() throws SerializadorException {

        // Tipo de registro.
        String ret = "0";
        
        // Ano tributario.
        ret += String.format("%04d", getContexto().getAnoTributario());

        // Numero de formulario.
        ret += "1820";

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
        ret += String.format("%04d", getContexto().getAnoTributario());
        
        // Mes tributario.
        ret += String.format("%02d", getContexto().getMesTributario());
        
        // Relleno.
        ret += Serializador.espacios(132);

        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 0");
        }
	
        return ret;
    }

    /**
     * Genera registro tipo 1.
     *
     * @return Registro tipo 1.
     * @throws SerializadorException
     *             En caso de error al serializar.
     */
    public String generarRegistroTipo1() throws SerializadorException {

        // Tipo de registro y precedencia.
        String ret = "11";
       
        // Ano tributario.
        ret += String.format("%04d", getContexto().getAnoTributario());
        
        // Mes tributario.
        ret += String.format("%02d", getContexto().getMesTributario());
            

        // Numero de formulario.
        ret += "1820";

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

        // Relleno.
        // ret += Serializador.espacios(1);

        // Codigo de empresa.
        ret += String.format("%02d", getContexto().getCodigoEmpresa());

        // Numero de cliente.
        ret += String.format("%04d", getContexto().getNumeroCliente());

        // Unidad, caja y paquete.
        ret += Serializador.ceros(13);

        // Relleno.
        ret += Serializador.espacios(23);

        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 1");
        }
		
        return ret;
    }

    /**
     * Genera registro tipo 2.
     *
     * @param registro
     *            El registro de datos.
     * @return Registro tipo 2.
     * @throws SerializadorException
     *             En caso de error al serializar.
     */
    public String generarRegistroTipo2(final Registro registro)
            throws SerializadorException {
    	
    	final RegistroDJ1820 registroDJ1820 = new RegistroDJ1820(registro);
        actualizarAcumuladores(registroDJ1820);

        // Tipo de Registro y Numero de Formulario.
        String ret = "21820";
       
        // Codigo de Presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de Declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // Tipo de Declaracion.
        ret += getContexto().getTipoDeclaracion().getCodigo();

        // RUT Declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());

        // RUT Contraparte.
        ret += Serializador.serializarRut(registro.getRutCliente());
        
        // TaxID.
        ret += String.format("%15s", registroDJ1820.getTaxID());
     
        // CodigoPaisResidencia.
        ret += String.format("%2s", registroDJ1820.getCodigoPaisResidencia());    
        
        // CodigoRelacion.
        ret += (String.format("%2s", registroDJ1820.getCodigoRelacion())).replace(" ","0");    
                
        //CodigoModoContrato        
        ret += (String.format("%1s", registroDJ1820.getCodigoModoContrato())).replace(" ","0");
        
        //TipoAcuerdoMarco        
        ret += (String.format("%1s", registroDJ1820.getTipoAcuerdoMarco())).replace(" ","0");
        
        //NumeroAcuerdoMarco        
        ret += String.format("%010d", registroDJ1820.getNumeroAcuerdoMarco());
          
        //FechaSuscripcionAcuerdo
        ret += Serializador.serializar(registroDJ1820.getFechaSuscripcionAcuerdo());
        
        //NumeroContrato        
        ret += String.format("%010d", registroDJ1820.getNumeroContrato());
        
        //FechaSuscripcionContrato
        ret += Serializador.serializar(registroDJ1820.getFechaSuscripcionContrato());
        
        //TipoEvento        
        ret += (String.format("%1s", registroDJ1820.getTipoEvento())).replace(" ","0");
        
        //TipoContrato        
        ret += (String.format("%2s", registroDJ1820.getTipoContrato())).replace(" ","0");
        
        //Instrumento        
        ret += String.format("%20s", registroDJ1820.getInstrumento());   
        
        //ModalidadCumplimiento        
        ret += (String.format("%1s", registroDJ1820.getModalidadCumplimiento())).replace(" ","0");
        
        //codigoPosicionDeclarante
        ret += (String.format("%1s", registroDJ1820.getCodigoPosicionDeclarante())).replace(" ","0");
 
// Activo Subyacente 

        //TipoActivoSubyacente
        ret += (String.format("%1s", registroDJ1820.getTipoActivoSubyacente())).replace(" ","0");
        
        //codigoActivoSubyacente
        ret += String.format("%3s", registroDJ1820.getCodigoActivoSubyacente());
        
        //otroActivoSubyacente
        ret += String.format("%15s", registroDJ1820.getOtroActivoSubyacente());   
        
        //TasaActivoSubyacente
        ret +=  String.format( "%7s" , (String.format("%02.04f", registroDJ1820.getTasaActivoSubyacente()).replace(".", "") )).replace(" ","0");
          
        
// Segundo Activo Subyacente 
        //TipoSegundoActivoSubyacente
       
        ret += (String.format("%1s", registroDJ1820.getTipoSegundoActivoSubyacente())).replace(" ","0");
        
        //codigoActivoSubyacente
        ret += String.format("%3s", registroDJ1820.getCodigoSegundoActivoSubyacente());
        //otroSegundoActivoSubyacente
        ret += String.format("%15s", registroDJ1820.getOtroSegundoActivoSubyacente()); 
        
        //TasaSegundoActivoSubyacente
        ret +=  String.format( "%7s" , (String.format("%02.04f", registroDJ1820.getTasaSegundoActivoSubyacente()).replace(".", "") )).replace(" ","0");
       
        //codigoPrecioFuturo
        ret += (String.format("%1s", registroDJ1820.getCodigoPrecioFuturo())).replace(" ","0");
        
        //precioFuturo
        ret += String.format( "%15s" , (String.format("%013.02f", registroDJ1820.getPrecioFuturo()).replace(".", ""))).replace(" ","0");
        
        //codigoMonedaFuturo
        ret += String.format("%3s", registroDJ1820.getCodigoMonedaFuturo());
        
        //codigoUnidad
        ret += (String.format("%2s", registroDJ1820.getCodigoUnidad())).replace(" ","0");
        
        //montoNocional
        ret += String.format( "%15s" , (String.format("%013.02f", registroDJ1820.getMontoNocional()).replace(".", ""))).replace(" ","0");
        
        //codigoSegundaUnidad
        ret += (String.format("%2s", registroDJ1820.getCodigoSegundaUnidad())).replace(" ","0");
        
        //segundoMontoNocional
        //ret += String.format( "%15s" , (String.format("%013.02f", registroDJ1820.getSegundoMontoNocional()).replace(".", ""))).replace(" ","0");CGO
        ret += (String.format("%15s", registroDJ1820.getSegundoMontoNocional())).replace(" ","0");

                
        //fechaVencimiento
        ret += Serializador.serializar(registroDJ1820.getFechaVencimiento());
        

        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 2");
        }

        return ret;
    }

    /**
     * Genera registro tipo 3.
     *
     * @return Registro tipo 3.
     * @throws SerializadorException
     *             En caso de error al serializar.
     */
    public String generarRegistroTipo3() throws SerializadorException {

        // Tipo de registro y numero de formulario.
        String ret = "311820";

        // Codigo de presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // RUT declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());

        // Total de casos informados.
        ret += String.format("%010d", totalRegistros);

        // Fecha de presentacion.
        ret += getContexto().getFechaPresentacion();

        // RUT del representante legal.
        ret += Serializador.serializarRut(getContexto().getRutRepresentante());

        // Relleno.
        ret += Serializador.espacios(176);

        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 3");
        }

        return ret;
    }




    public String generarRegistroTipo0_2025() throws SerializadorException {

        // Tipo de registro.
        String ret = "0";
        
        // Ano tributario.
        ret += String.format("%04d", getContexto().getAnoTributario());

        // Numero de formulario.
        ret += "1820";

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
        ret += String.format("%04d", getContexto().getAnoTributario());
        
        // Mes tributario.
        ret += String.format("%02d", getContexto().getMesTributario());
        
        // Relleno.
        ret += Serializador.espacios(122);

        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 0");
        }
	
        return ret;
    }

    /**
     * Genera registro tipo 1.
     *
     * @return Registro tipo 1.
     * @throws SerializadorException
     *             En caso de error al serializar.
     */
    public String generarRegistroTipo1_2025() throws SerializadorException {

        // Tipo de registro y precedencia.
        String ret = "11";
       
        // Ano tributario.
        ret += String.format("%04d", getContexto().getAnoTributario());
        
        // Mes tributario.
        ret += String.format("%02d", getContexto().getMesTributario());
            

        // Numero de formulario.
        ret += "1820";

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

        // Relleno.
        // ret += Serializador.espacios(1);

        // Codigo de empresa.
        ret += String.format("%02d", getContexto().getCodigoEmpresa());

        // Numero de cliente.
        ret += String.format("%04d", getContexto().getNumeroCliente());

        // Unidad, caja y paquete.
        ret += Serializador.ceros(13);

        // Relleno.
        ret += Serializador.espacios(13);

        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 1");
        }
		
        return ret;
    }

    /**
     * Genera registro tipo 2.
     *
     * @param registro
     *            El registro de datos.
     * @return Registro tipo 2.
     * @throws SerializadorException
     *             En caso de error al serializar.
     */
    public String generarRegistroTipo2_2025(final Registro registro)
            throws SerializadorException {
    	
    	final RegistroDJ1820 registroDJ1820 = new RegistroDJ1820(registro);
        actualizarAcumuladores(registroDJ1820);

        // Tipo de Registro y Numero de Formulario.
        String ret = "21820";
       
        // Codigo de Presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de Declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // Tipo de Declaracion.
        ret += getContexto().getTipoDeclaracion().getCodigo();

        // RUT Declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());

        // RUT Contraparte.
        ret += Serializador.serializarRut(registro.getRutCliente());
        
        // TaxID.
        ret += String.format("%15s", registroDJ1820.getTaxID());
     
        // CodigoPaisResidencia.
        ret += String.format("%2s", registroDJ1820.getCodigoPaisResidencia());    
        
        // CodigoRelacion.
        ret += (String.format("%2s", registroDJ1820.getCodigoRelacion())).replace(" ","0");    
                
        //CodigoModoContrato        
        ret += (String.format("%1s", registroDJ1820.getCodigoModoContrato())).replace(" ","0");
        
        //TipoAcuerdoMarco        
        ret += (String.format("%1s", registroDJ1820.getTipoAcuerdoMarco())).replace(" ","0");
        
        //NumeroAcuerdoMarco        
        ret += String.format("%010d", registroDJ1820.getNumeroAcuerdoMarco());
          
        //FechaSuscripcionAcuerdo
        ret += Serializador.serializar(registroDJ1820.getFechaSuscripcionAcuerdo());
        
        //NumeroContrato        
        ret += String.format("%010d", registroDJ1820.getNumeroContrato());
        
        //FechaSuscripcionContrato
        ret += Serializador.serializar(registroDJ1820.getFechaSuscripcionContrato());
        
        //TipoEvento        
        ret += (String.format("%1s", registroDJ1820.getTipoEvento())).replace(" ","0");
        
        //TipoContrato        
        ret += (String.format("%2s", registroDJ1820.getTipoContrato())).replace(" ","0");
        
        //Instrumento        
        ret += String.format("%20s", registroDJ1820.getInstrumento());   
        
        //ModalidadCumplimiento        
        ret += (String.format("%1s", registroDJ1820.getModalidadCumplimiento())).replace(" ","0");
        
        //codigoPosicionDeclarante
        ret += (String.format("%1s", registroDJ1820.getCodigoPosicionDeclarante())).replace(" ","0");
 
// Activo Subyacente 

        //TipoActivoSubyacente
        ret += (String.format("%1s", registroDJ1820.getTipoActivoSubyacente())).replace(" ","0");
        
        //codigoActivoSubyacente
        ret += String.format("%3s", registroDJ1820.getCodigoActivoSubyacente());
        
        //otroActivoSubyacente
        ret += String.format("%10s", registroDJ1820.getOtroActivoSubyacente());   
        
        //TasaActivoSubyacente
        ret +=  String.format( "%7s" , (String.format("%02.04f", registroDJ1820.getTasaActivoSubyacente()).replace(".", "") )).replace(" ","0");
          
        
// Segundo Activo Subyacente 
        //TipoSegundoActivoSubyacente
       
        ret += (String.format("%1s", registroDJ1820.getTipoSegundoActivoSubyacente())).replace(" ","0");
        
        //codigoActivoSubyacente
        ret += String.format("%3s", registroDJ1820.getCodigoSegundoActivoSubyacente());
        //otroSegundoActivoSubyacente
        ret += String.format("%10s", registroDJ1820.getOtroSegundoActivoSubyacente()); 
        
        //TasaSegundoActivoSubyacente
        ret +=  String.format( "%7s" , (String.format("%02.04f", registroDJ1820.getTasaSegundoActivoSubyacente()).replace(".", "") )).replace(" ","0");
       
        //codigoPrecioFuturo
        ret += (String.format("%1s", registroDJ1820.getCodigoPrecioFuturo())).replace(" ","0");
        
        //precioFuturo
        ret += String.format( "%15s" , (String.format("%013.02f", registroDJ1820.getPrecioFuturo()).replace(".", ""))).replace(" ","0");
        
        //codigoMonedaFuturo
        ret += String.format("%3s", registroDJ1820.getCodigoMonedaFuturo());
        
        //codigoUnidad
        ret += (String.format("%2s", registroDJ1820.getCodigoUnidad())).replace(" ","0");
        
        //montoNocional
        ret += String.format( "%15s" , (String.format("%013.02f", registroDJ1820.getMontoNocional()).replace(".", ""))).replace(" ","0");
        
        //codigoSegundaUnidad
        ret += (String.format("%2s", registroDJ1820.getCodigoSegundaUnidad())).replace(" ","0");
        
        //segundoMontoNocional
        //ret += String.format( "%15s" , (String.format("%013.02f", registroDJ1820.getSegundoMontoNocional()).replace(".", ""))).replace(" ","0");CGO
        ret += (String.format("%15s", registroDJ1820.getSegundoMontoNocional())).replace(" ","0");

                
        //fechaVencimiento
        ret += Serializador.serializar(registroDJ1820.getFechaVencimiento());
        

        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 2");
        }

        return ret;
    }

    /**
     * Genera registro tipo 3.
     *
     * @return Registro tipo 3.
     * @throws SerializadorException
     *             En caso de error al serializar.
     */
    public String generarRegistroTipo3_2025() throws SerializadorException {

        // Tipo de registro y numero de formulario.
        String ret = "311820";

        // Codigo de presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // RUT declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());

        // Total de casos informados.
        ret += String.format("%010d", totalRegistros);

        // Fecha de presentacion.
        ret += getContexto().getFechaPresentacion();

        // RUT del representante legal.
        ret += Serializador.serializarRut(getContexto().getRutRepresentante());

        // Relleno.
        ret += Serializador.espacios(166);

        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 3");
        }

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

    /**
     * Obtiene la cantidad total de registros que contiene el archivo de
     * resultado.
     *
     * @return La cantidad total de registros que contiene el archivo de
     *         resultado.
     */
    private long getCantidadTotalRegistros() {
        return getCantidadCasosInformados() + 3;
    }

}
