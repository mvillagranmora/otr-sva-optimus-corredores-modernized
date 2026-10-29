package cl.bcs.normativa.generacion;

import java.io.IOException;
import java.sql.SQLException;

import cl.bcs.normativa.data.Registro;
import cl.bcs.normativa.registros.RegistroDJ1958;
import java.math.RoundingMode;

public final class GeneradorDJ1958 extends Generador{
    private static final int LARGO_REGISTRO = 965;
	private RegistroDJ1958 registroResumen;
    private long totalRegistros;

    public GeneradorDJ1958(final ContextoGeneracion contexto)
        throws SQLException, IOException, SerializadorException {
        super(contexto);
        this.totalRegistros = 0;
        this.registroResumen = new RegistroDJ1958();
    }
    
    private void actualizarAcumuladores(final RegistroDJ1958 registroDJ1958) {
        this.contarCasosInformados();
        this.totalRegistros += 1;
    }
    
    @Override
    public void generarDetalle(final Registro registro) throws SerializadorException {
        try{
            println(generarRegistroTipo2(registro));
        }catch(Exception ex){
             throw new SerializadorException("Error: "+ex.getMessage());
        }
            
    }  
    
    @Override
    public void generarEncabezado() throws SerializadorException {
    	
    	if (getContexto().getModoPresentacion().getCodigo().equalsIgnoreCase("I"))
    	{
    		println(generarRegistroTipo0());
    	};
        
        println(generarRegistroTipo1());
        println(generarRegistroTipo2Seccion1());
    }

    @Override
    public String generarRegistroTipo0() throws SerializadorException {

        // Tipo de registro.
        String ret = "0";
        
        // Ano tributario.
        ret += String.format("%04d", getContexto().getAnoTributario()+1L);

        // Numero de formulario.
        ret += "1958";

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
        ret += Serializador.espacios(876);

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
        ret += "1958";

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
        ret += String.format("%30s",  (EmailDeclaracion).length() > 30 ?  (EmailDeclaracion).substring(0,30) :  (EmailDeclaracion));
        
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
        ret += Serializador.espacios(761);
        
        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 1");
        }
		
        return ret;
    }

    public String generarRegistroTipo2Seccion1() throws SerializadorException {
        //Tipo Registro
        String ret = "2";

        // Numero Registro
        ret += "1958";

        // Codigo de Presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de Declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // Tipo de Declaracion.
        ret += getContexto().getTipoDeclaracion().getCodigo();

        // RUT Declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());

        // Tipo Seccion
        ret += "1";

        //c1
        ret += "CL";

        //c2
        ret += "1";

        //c3
        ret += "1";
            
        //c4
        String periodo = "00000000";
        ret += periodo;

        //c5
        ret += "  ";
        
        //c6
        ret += "0";

        // Relleno.
        ret += Serializador.espacios(568);
        
        // Relleno 2
        
        ret += "0000000000000000000000000000000000000000";
        
        // Relleno.3
        ret += Serializador.espacios(3);
        
        // Relleno 4
        
        ret +="000000000000000000000";
        
        // Relleno.5
        ret += Serializador.espacios(256);
        
        // Relleno 6
        
        ret += "00000000";
        
        // Relleno 7
        ret += Serializador.espacios(9);
        
        // Relleno 8
        
        ret += "000000000000000000000";
        
        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 2.1");
        }

        return ret;

    }

    public String generarRegistroTipo2(final Registro registro) throws SerializadorException {
        final RegistroDJ1958 registroDJ1958 = new RegistroDJ1958(registro);

        //Tipo Registro
        String ret = "2";

        // Numero Registro
        ret += "1958";

        // Codigo de Presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de Declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // Tipo de Declaracion.
        ret += getContexto().getTipoDeclaracion().getCodigo();

        // RUT Declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());

        // Tipo Seccion
        ret += "2";

        //c1
        ret += String.format("%-2s", registroDJ1958.getPaisResidencia() != null ? (registroDJ1958.getPaisResidencia().length() > 2 ? registroDJ1958.getPaisResidencia().substring(0, 2) : registroDJ1958.getPaisResidencia()) : "");

        //c2
        ret += String.format("%-1s", registroDJ1958.getTipoInstitucion() != null ? (registroDJ1958.getTipoInstitucion().length() > 1 ? registroDJ1958.getTipoInstitucion().substring(0, 1) : registroDJ1958.getTipoInstitucion()) : "0");

        //c3
        ret += String.format("%-1s", registroDJ1958.getPoseeCuentasFinanciera() != null ? (registroDJ1958.getPoseeCuentasFinanciera().length() > 1 ? registroDJ1958.getPoseeCuentasFinanciera().substring(0, 1) : registroDJ1958.getPoseeCuentasFinanciera()) : "0");
        
        // c4
        String periodo = Serializador.serializar(registroDJ1958.getPeriodoReporte());
        if (periodo != null && periodo.length() == 8 && !periodo.equals("01010001")) {
            // ddMMyyyy → yyyyMMdd
            periodo = periodo.substring(4, 8)   // yyyy
                    + periodo.substring(2, 4)   // MM
                    + periodo.substring(0, 2);  // dd
        } else {
            periodo = "00000000";
        }
        ret += periodo;
        
        //c5
        ret += String.format("%-2s", registroDJ1958.getPaisResidencia() != null ? (registroDJ1958.getPaisResidencia().length() > 2 ? registroDJ1958.getPaisResidencia().substring(0, 2) : registroDJ1958.getPaisResidencia()) : "0");
        
        //c6
        ret += String.format("%-1s", registroDJ1958.getTipoPersona() != null ? (registroDJ1958.getTipoPersona().length() > 1 ? registroDJ1958.getTipoPersona().substring(0, 1) : registroDJ1958.getTipoPersona()) :"0");

        //c7
        ret += String.format("%-6s", registroDJ1958.getTipoJuridico() != null ? (registroDJ1958.getTipoJuridico().length() > 6 ? registroDJ1958.getTipoJuridico().substring(0, 6) : registroDJ1958.getTipoJuridico()) : "");
        
        //c8
        String Nombres=registroDJ1958.getNombres();
        Nombres=Serializador.removerCaracteresEspeciales(Nombres);
        ret += String.format("%-100s", Nombres != null ? (Nombres.length() > 100 ? Nombres.substring(0, 100): Nombres) : "");
        
        //c9
        String Apellidos=registroDJ1958.getApellidos();
        Apellidos=Serializador.removerCaracteresEspeciales(Apellidos);
        ret += String.format("%-100s", Apellidos != null ? (Apellidos.length() > 100 ? Apellidos.substring(0, 100): Apellidos) : "");
        
        //c10
        String RazonSocial=registroDJ1958.getRazonSocial();
        RazonSocial=Serializador.removerCaracteresEspeciales(RazonSocial);
        ret += String.format("%-150s", RazonSocial != null ? (RazonSocial.length() > 150 ? RazonSocial.substring(0, 150) : RazonSocial) : "");
        
        //c11
        ret += String.format("%-2s", registroDJ1958.getPaisTaxID() != null ? (registroDJ1958.getPaisTaxID().length() > 2 ? registroDJ1958.getPaisTaxID().substring(0, 2) : registroDJ1958.getPaisTaxID()) : "");
        
        //c12
        ret += String.format("%-21s", registroDJ1958.getTaxID() != null ? (registroDJ1958.getTaxID().length() > 21 ? registroDJ1958.getTaxID().substring(0, 21) : registroDJ1958.getTaxID()) : "");
        
        //c13
        ret += String.format("%-2s", registroDJ1958.getPaisDomicilio() != null ? (registroDJ1958.getPaisDomicilio().length() > 2 ? registroDJ1958.getPaisDomicilio().substring(0, 2) : registroDJ1958.getPaisDomicilio()) : "");
        
        //c14
        ret += String.format("%-7s", registroDJ1958.getTipoDomicilio() != null ? (registroDJ1958.getTipoDomicilio().length() > 7 ? registroDJ1958.getTipoDomicilio().substring(0,7) : registroDJ1958.getTipoDomicilio()) : "");

        //c15
        String Domicilio=registroDJ1958.getDomicilio();
        Domicilio=Serializador.removerCaracteresEspeciales(Domicilio);
        ret += String.format("%-180s", Domicilio != null ? (Domicilio.length() > 180 ? Domicilio.substring(0, 180) : Domicilio): "");

        //c16
        // ret += Serializador.serializar(registroDJ1958.getFechaNacimiento());
        String fechaNacimiento = Serializador.serializar(registroDJ1958.getFechaNacimiento());
        if (fechaNacimiento != null && fechaNacimiento.length() == 8 && !fechaNacimiento.equals("01010001")) {
            // ddMMyyyy → yyyyMMdd
            fechaNacimiento = fechaNacimiento.substring(4, 8)       // yyyy
                            + fechaNacimiento.substring(2, 4)       // MM
                            + fechaNacimiento.substring(0, 2);      // dd
        } else {
            fechaNacimiento = "00000000";
        }
        ret += fechaNacimiento;
        
        //c17
        ret += String.format("%021d", registroDJ1958.getNumeroCuenta());

        // RutFondo y DV C37 y C38
        String RutFondo = "000000000";
        if (registroDJ1958.getRutFondo() != null  && !registroDJ1958.getRutFondo().trim().isEmpty()) {
            RutFondo = Serializador.serializarRut(registroDJ1958.getRutFondo());
        }
        ret += String.format("%-9s",  (RutFondo).length() > 9 ?  (RutFondo).substring(0,9) :  (RutFondo));
        
        //c18
        ret += String.format("%01d", registroDJ1958.getCuentaNoDocumentada());
        
        //c19
        ret += String.format("%01d", registroDJ1958.getCuentaCerrada());
        
        //c20
        ret += String.format("%-3s", registroDJ1958.getDivisaSaldo() != null ? (registroDJ1958.getDivisaSaldo().length() > 3 ? registroDJ1958.getDivisaSaldo().substring(0, 3) : registroDJ1958.getDivisaSaldo()) : 0);

        //c21
        ret += String.format("%021d", registroDJ1958.getSaldoActualizado().setScale(2, RoundingMode.HALF_UP).movePointRight(2)
        .longValue());
        
        //c22
        ret += String.format("%-2s", registroDJ1958.getPaisResidenciaControlador() != null ? (registroDJ1958.getPaisResidenciaControlador().length() > 2 ? registroDJ1958.getPaisResidenciaControlador().substring(0, 2) : registroDJ1958.getPaisResidenciaControlador()) : "0");
        
        //c23
        ret += String.format("%-21s", registroDJ1958.getNombreControlador() != null ? (registroDJ1958.getNombreControlador().length() > 21 ? registroDJ1958.getNombreControlador().substring(0, 21) : registroDJ1958.getNombreControlador()) : "");
        
        //c24
        ret += String.format("%-21s", registroDJ1958.getApellidosControlador() != null ? (registroDJ1958.getApellidosControlador().length() > 21 ? registroDJ1958.getApellidosControlador().substring(0, 21) : registroDJ1958.getApellidosControlador()) : "");
        
        //c25
        ret += String.format("%-2s", registroDJ1958.getPaisTaxIDControlador() != null ? (registroDJ1958.getPaisTaxIDControlador().length() > 2 ? registroDJ1958.getPaisTaxIDControlador().substring(0, 2) : registroDJ1958.getPaisTaxIDControlador()) : "");
        
        //c26
        ret += String.format("%-21s", registroDJ1958.getTaxIDControlador() != null ? (registroDJ1958.getTaxIDControlador().length() > 21 ? registroDJ1958.getTaxIDControlador().substring(0,21) : registroDJ1958.getTaxIDControlador()) : "");
        
        //c27
        ret += String.format("%-2s", registroDJ1958.getPaisDomicilioControlador() != null ? (registroDJ1958.getPaisDomicilioControlador().length() > 2 ? registroDJ1958.getTaxIDControlador().substring(0, 2) : registroDJ1958.getTaxIDControlador()) : "");
        
        //c28
        ret += String.format("%-7s", registroDJ1958.getTipoDomicilioControlador() != null ? (registroDJ1958.getTipoDomicilioControlador().length() > 7 ? registroDJ1958.getTipoDomicilioControlador().substring(0, 7) : registroDJ1958.getTipoDomicilioControlador()) : "");

        //c29
        ret += String.format("%-180s", registroDJ1958.getDomicilioControlador() != null ? (registroDJ1958.getDomicilioControlador().length() > 180 ? registroDJ1958.getTipoDomicilioControlador().substring(0, 180) : registroDJ1958.getTipoDomicilioControlador()) : "");
        
        //c30
        String fechaNacimientoControlador = Serializador.serializar(registroDJ1958.getFechaNacimientoControlador());
        if (fechaNacimientoControlador != null && fechaNacimientoControlador.length() == 8 && !fechaNacimientoControlador.equals("01010001")) {
            // ddMMyyyy → yyyyMMdd
            fechaNacimientoControlador = fechaNacimientoControlador.substring(4, 8)     // yyyy
                                    + fechaNacimientoControlador.substring(2, 4)        // MM
                                    + fechaNacimientoControlador.substring(0, 2);       // dd
        } else {
            fechaNacimientoControlador = "00000000";
        }
        ret += fechaNacimientoControlador;

        //31
        ret += String.format("%-6s", registroDJ1958.getTipoPagoCuentaFinanciera() != null ? (registroDJ1958.getTipoPagoCuentaFinanciera().length() > 6 ? registroDJ1958.getTipoPagoCuentaFinanciera().substring(0, 6) : registroDJ1958.getTipoPagoCuentaFinanciera()) : "");
        
        //c32
        ret += String.format("%-3s", registroDJ1958.getDivisaPagoCuentaFinanciera() != null ? (registroDJ1958.getDivisaPagoCuentaFinanciera().length() > 3 ? registroDJ1958.getDivisaPagoCuentaFinanciera().substring(0, 3) : registroDJ1958.getDivisaPagoCuentaFinanciera()) : "");

        //c33
        ret += String.format("%021d", registroDJ1958.getMontoPagoActualizado().setScale(2, RoundingMode.HALF_UP).movePointRight(2)
        .longValue());
        actualizarAcumuladores(registroDJ1958);
        return ret; 
        
    }

    public String generarRegistroTipo3() throws SerializadorException {

        // Tipo de registro y numero de formulario.
        String ret = "311958";

        // Codigo de presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // RUT Declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());
        
        // Total de casos informados.
        ret += String.format("%010d", getCantidadCasosInformados()+1);
        
        // Fecha de presentacion.
        ret += getContexto().getFechaPresentacion();

        // RUT del representante legal.
        ret += Serializador.serializarRut(getContexto().getRutRepresentante());
        ret += Serializador.espacios(914);
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
        return getCantidadCasosInformados() + 4;
    }
}