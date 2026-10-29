package cl.bcs.normativa.generacion;

import java.io.IOException;
import java.sql.SQLException;

import cl.bcs.normativa.data.Registro;
import cl.bcs.normativa.registros.RegistroDJ1891;

/**
 * Implementaci&oacute;n del generador de la DJ1891.
 *
 * @author fpino@email.com
 *
 */
public final class GeneradorDJ1891 extends Generador {

    private static final int LARGO_REGISTRO = 177;

    /**
     * Suma de los montos de las compras.
     */
    private double sumaMontosCompras;


    /**
     * Suma de los montos de las ventas.
     */
    private double sumaMontosVentas;

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
    public GeneradorDJ1891(final ContextoGeneracion contexto)
            throws SQLException, IOException, SerializadorException {
        super(contexto);

        this.sumaMontosCompras = 0;
        this.sumaMontosVentas = 0;
    }

    /**
     * Actualiza los acumuladores.
     *
     * @param registroDJ1891
     *            Registro DJ1891.
     */
    private void actualizarAcumuladores(final RegistroDJ1891 registroDJ1891) {
        contarCasosInformados();

        this.sumaMontosCompras += Math.round(registroDJ1891.getMontoCompras());
        this.sumaMontosVentas += Math.round(registroDJ1891.getMontoVentas());
    }

    @Override
    public void generarDetalle(final Registro registro) throws SerializadorException {
        println(generarRegistroTipo2(registro));
    }

    @Override
    public void generarEncabezado() throws SerializadorException {
        println(generarRegistroTipo0());
        println(generarRegistroTipo11());
        println(generarRegistroTipo12());
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
        ret += String.format("%04d", getContexto().getAnoTributario()+1L);

        // Numero de formulario.
        ret += "1891";

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

        // Fecha/hora envio (ddMMyyyyhhmmsscc).
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

    /**
     * Genera registro tipo 1.1.
     *
     * @return Registro tipo 1.1.
     * @throws SerializadorException
     *             En caso de error al serializar.
     */
    public String generarRegistroTipo11() throws SerializadorException {

        // Tipo de registro y precedencia.
        String ret = "11";

        // Ano tributario.
        ret += String.format("%04d", getContexto().getAnoTributario()+1L);

        // Numero de formulario.
        ret += "1891";

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


        // Relleno.
        ret += Serializador.espacios(16);

        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 11");
        }

        return ret;
    }

    /**
     * Genera registro tipo 1.2.
     *
     * @return Registro tipo 1.2.
     * @throws SerializadorException
     *             En caso de error al serializar.
     */
    public String generarRegistroTipo12() throws SerializadorException {

        // Tipo de registro y precedencia.
        String ret = "12";

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
        ret += Serializador.espacios(133);

        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 12");
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
    public String generarRegistroTipo2(final Registro registro) throws SerializadorException {

        final RegistroDJ1891 registroDJ1891 = new RegistroDJ1891(registro);
        actualizarAcumuladores(registroDJ1891);

        // Tipo de registro y numero de formulario.
        String ret = "21891";

        // Codigo de presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // Tipo de declaracion.
        ret += getContexto().getTipoDeclaracion().getCodigo();

        // RUT declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());

        // Tipo de documento.
        ret += registroDJ1891.getTipoDocumento();

        // Numero de documento.
        ret += String.format("%010d", registroDJ1891.getNumeroDocumento());
        String numeroDoc = String.format("%010d", registroDJ1891.getNumeroDocumento());
        // Fecha de documento.
        ret += Serializador.serializar(registroDJ1891.getFechaDocumento());

        // RUT del emisor.
        if (registroDJ1891.getRutEmisor().length() > 1){
        	ret += Serializador.serializarRut(registroDJ1891.getRutEmisor());
        }else{
        	 ret += Serializador.ceros(9);
        };
        
      
        // Nemotecnico.
        
        String Nemotecnico=registroDJ1891.getNemotecnico();
        ret += String.format("%-50s",  (Nemotecnico).length() > 50 ?  (Nemotecnico).substring(0,50) :  (Nemotecnico));
          
      

        // RUT del cliente.
        ret += Serializador.serializarRut(registroDJ1891.getRutOperante());

        // Cantidad de acciones.
        ret += String.format("%015d00000", registroDJ1891.getCantidadAcciones());
        
        // Monto de compra.
        //ret += String.format("%015d", registroDJ1891.getMontoCompra());
        ret += String.format( "%15s" , (String.format("%015.0f", registroDJ1891.getMontoCompras()).replace(".", ""))).replace(" ","0");
        
        // Monto de venta.
        //ret += String.format("%015d", registroDJ1891.getMontoVenta());
        ret += String.format( "%15s" , (String.format("%015.0f", registroDJ1891.getMontoVentas()).replace(".", ""))).replace(" ","0");
        
        // Valor de Adquisicion.
        //ret += String.format("%015d", registroDJ1891.getMontoVenta());
        ret += String.format( "%15s" , (String.format("%015.0f", registroDJ1891.getValorAdquisicion()).replace(".", ""))).replace(" ","0");

        // Tipo de transaccion.
        ret += registroDJ1891.getTipoTransaccion();
        
        
        // Unidad, caja y paquete.
        //ret += Serializador.ceros(13);
        
        
        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 2: ");
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
        String ret = "31891";

        // Codigo de presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // RUT declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());

        // Total monto de compras.
        //ret += String.format("%18f", sumaMontosCompras);
        ret += String.format("%18s" , (String.format("%018.0f",sumaMontosCompras).replace(".", ""))).replace(" ","0");

        // Total monto de ventas.
       
        ret += String.format("%18s" , (String.format("%018.0f",sumaMontosVentas).replace(".", ""))).replace(" ","0");

        // Total de casos informados.
        ret += String.format("%07d", getCantidadCasosInformados());

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

    /**
     * Obtiene la cantidad total de registros que contiene el archivo de
     * resultado.
     *
     * @return La cantidad total de registros que contiene el archivo de
     *         resultado.
     */
    private long getCantidadTotalRegistros() {
        return getCantidadCasosInformados() + 4;
    }

}
