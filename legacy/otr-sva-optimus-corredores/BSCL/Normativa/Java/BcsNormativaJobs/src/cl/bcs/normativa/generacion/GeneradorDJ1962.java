package cl.bcs.normativa.generacion;

import java.io.IOException;
import java.sql.SQLException;

import cl.bcs.normativa.data.Registro;
import cl.bcs.normativa.registros.RegistroDJ1962;
import java.math.RoundingMode;


public final class GeneradorDJ1962 extends Generador{
    private static final int LARGO_REGISTRO = 331;
	private RegistroDJ1962 registroResumen;
    private long totalRegistros;

    public GeneradorDJ1962(final ContextoGeneracion contexto)
            throws SQLException, IOException, SerializadorException {
        super(contexto);
        this.totalRegistros = 0;
        this.registroResumen = new RegistroDJ1962();

    }
    
    private void actualizarAcumuladores(final RegistroDJ1962 registroDJ1962) {
        this.contarCasosInformados();
        this.totalRegistros += 1;
    }
    
    @Override
    public void generarDetalle(final Registro registro) throws SerializadorException {
        try{          

                println(generarRegistroTipo2(registro));
            
        }catch(Exception ex){
             throw new SerializadorException(" Generacion Seccion2: "+ex.getMessage());
        }
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
        ret += "1962";

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
        ret += Serializador.espacios(242);
        

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
        ret += "1962";

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
        ret += Serializador.espacios(127);
        

        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 1");
        }
		
        return ret;
    }

    

    //Generar Registro tipo 2, para esta DJ, en sistema Optimus solo se debe generar tipo contribuyente 2
    public String generarRegistroTipo2(final Registro registro) throws SerializadorException {
        final RegistroDJ1962 registroDJ1962 = new RegistroDJ1962(registro);
        String ret = "2";
        ret += "1962";
        ret += getContexto().getModoPresentacion().getCodigo();
        ret += String.format("%07d", getContexto().getFolioDeclaracion());
        ret += getContexto().getTipoDeclaracion().getCodigo();
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());

        // Tipo Seccion = 2
        ret += "2";

        // Tipo Contribuyente C27 "2" Para corredor de bolsa
       
         ret += "2";

        // El resto del layout tipo 2 DEBE IR EN BLANCO O CEROS

        //Seccion B
        // RutAdministradora C1
        ret += String.format("%09d", 0);
        // ret += String.format("%-9s",0);

        // RutFondo C2
        ret += String.format("%09d", 0);

        // RunFondo C3
        ret += String.format("%09d", 0);

        // Tipo De Fondo C4
        ret += String.format("%-1s","0");
        
        // Tribusa S.A C5
        ret +="0";

        // Beneficio positivo C6
        ret += String.format("%020d", 0); 

        // Beneficio negativo C7
        ret += String.format("%020d", 0); 

        // Utilidades C8
        ret += String.format("%020d", 0); 

        // Intereses C9
        ret += String.format("%020d", 0); 

        // Dividendos C10
        ret += String.format("%020d", 0); 

        // Ganancias C11
        ret += String.format("%020d", 0); 

        // Perdidas C12
        ret += String.format("%020d", 0); 

        // Seccion C

        //Rut del Fondo C14
        ret += String.format("%-9s", Serializador.serializarRut(registroDJ1962.getRutFondoC()));

        //Rut del Participe C15
        ret += String.format("%-9s", Serializador.serializarRut(registroDJ1962.getRutCliente()));

        // Nombre o Razon Social C16
        String NombreParticipe=registroDJ1962.getNombreParticipe();
        NombreParticipe=Serializador.removerCaracteresEspeciales(NombreParticipe);
        ret += String.format("%-30s",  (NombreParticipe).length() > 30 ?  (NombreParticipe).substring(0,30) :  (NombreParticipe));

        // TAX-ID C17 
        ret += String.format("%-15s", registroDJ1962.getTaxId() == null ? "" : (registroDJ1962.getTaxId().length() > 15 ? registroDJ1962.getTaxId().substring(0,15) : registroDJ1962.getTaxId()));

        // Pais de Residencia del Participe o Aportante C18 
        ret += String.format("%-2s", registroDJ1962.getCodigoPais() == null ? "" : (registroDJ1962.getCodigoPais().length() > 2 ? registroDJ1962.getCodigoPais().substring(0,2) : registroDJ1962.getCodigoPais()));

        // Moneda o Unidad de reajuste C19 
        ret += String.format("%01d", registroDJ1962.getMonedaReajuste() == null || registroDJ1962.getMonedaReajuste().trim().isEmpty() ? 0 : Integer.parseInt(registroDJ1962.getMonedaReajuste().trim()));

        // Monto Nominal de Aportes al Fondo en $ C20
        ret += String.format("%015d", registroDJ1962.getMontoNominalAportesActualizado() == null ? 0 : registroDJ1962.getMontoNominalAportesActualizado().longValue());

        // Numero de Cuotas de la Serie de Propiedad del Participe o Aportante C21
        ret += String.format("%024d", registroDJ1962.getCantidadCuotasParticipe() == null ? 0 : registroDJ1962.getCantidadCuotasParticipe().setScale(6, RoundingMode.DOWN).movePointRight(6).longValue());

        // Participe o Aportante cumple requisitos C22
        ret += String.format("%01d", registroDJ1962.getParticipeCumpleRequisitos() == null ? 0 : registroDJ1962.getParticipeCumpleRequisitos());

        //Cuotas de Fondos que cumplen requisitos C23
        ret += String.format("%01d",registroDJ1962.getCuotasCumplenRequisitos() == null ? 0 : registroDJ1962.getCuotasCumplenRequisitos());

        // Valor de la cuota precio Mercado C24
        ret += String.format("%021d",registroDJ1962.getValorCuotaPesos().setScale(6, RoundingMode.HALF_UP).movePointRight(6).longValue());

        // % de Participacion en el Patrimonio del fondo al cierre del ejercicio C25
        ret += String.format("%09d", 0);

        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 1 seccion 2");
        }
        actualizarAcumuladores(registroDJ1962);
        return ret;
    }


    public String generarRegistroTipo3() throws SerializadorException {

        // Tipo de registro y numero de formulario.
        String ret = "311962";

        // Codigo de presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // RUT Declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());
        
        // Total de casos informados.
        ret += String.format("%010d", getCantidadCasosInformados());
        
        // Fecha de presentacion.
        ret += getContexto().getFechaPresentacion();

        // RUT del representante legal.
        ret += Serializador.serializarRut(getContexto().getRutRepresentante());
        ret += Serializador.espacios(280);
        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 3 :" + ret.length());
        }
        return ret;
    }

    @Override
    public void generarResumen() throws SerializadorException {
        print(generarRegistroTipo3());
        println("");
    }
    
    private long getCantidadTotalRegistros() {
        return getCantidadCasosInformados() + 3; // 3, ya que se suman el registro tipo 0,1 y el 3
 }
}