package cl.bcs.normativa.registros;

import cl.bcs.normativa.data.Registro;
import java.util.Date;
import java.math.BigDecimal;


public class RegistroDJ1958 {

	//* Declaración de Variables *//
	private Long ID;
    private Long IdDocumento;
    private String VboUsuario;
    private String Observaciones;
    private String FuenteDatos;
    private String PaisResidenciaDeclarante;
    private String TipoInstitucion;
    private String PoseeCuentasFinanciera;
    private Date PeriodoReporte;
    private String PaisResidencia;
    private String TipoPersona;
    private String TipoJuridico;
    private String Nombres;
    private String Apellidos;
    private String RazonSocial;
    private String PaisTaxID;
    private String TaxID;
    private String PaisDomicilio;
    private String TipoDomicilio;
    private String Domicilio;
    private Date FechaNacimiento;
    private Long NumeroCuenta;
    private Long CuentaNoDocumentada;
    private Long CuentaCerrada;
    private String DivisaSaldo;
    private BigDecimal SaldoActualizado;
    private String PaisResidenciaControlador;
    private String NombreControlador;
    private String ApellidosControlador;
    private String PaisTaxIDControlador;
    private String TaxIDControlador;
    private String PaisDomicilioControlador;
    private String TipoDomicilioControlador;
    private String DomicilioControlador;
    private Date FechaNacimientoControlador;
    private String TipoPagoCuentaFinanciera;
    private String DivisaPagoCuentaFinanciera;
    private BigDecimal MontoPagoActualizado;
    private Long IdCliente;
    private String Nemo;
    private String TipoRegistro;
    private String RutFondo;

	//* Sumatorias *// TODO AJARA, se deja vació por el momento


	//* Obtenemos los datos del registro *//
	public RegistroDJ1958(final Registro registro) {
		this.ID = (Long) registro.get("ID");
		this.IdDocumento = (Long) registro.get("IdDocumento");
		this.VboUsuario = (String) registro.get("VboUsuario");
		this.Observaciones = (String) registro.get("Observaciones");
		this.FuenteDatos = (String) registro.get("FuenteDatos");
		this.PaisResidenciaDeclarante = (String) registro.get("PaisResidenciaDeclarante");
		this.TipoInstitucion = (String) registro.get("TipoInstitucion");
		this.PoseeCuentasFinanciera = (String) registro.get("PoseeCuentasFinanciera");
		this.PeriodoReporte = (Date) registro.get("PeriodoReporte");
		this.PaisResidencia = (String) registro.get("PaisResidencia");
		this.TipoPersona = (String) registro.get("TipoPersona");
    	this.TipoJuridico = (String) registro.get("TipoJuridico");
    	this.Nombres = (String) registro.get("Nombres");
    	this.Apellidos = (String) registro.get("Apellidos");
    	this.RazonSocial = (String) registro.get("RazonSocial");
    	this.PaisTaxID = (String) registro.get("PaisTaxID");
    	this.TaxID = (String) registro.get("TaxID");
    	this.PaisDomicilio = (String) registro.get("PaisDomicilio");
    	this.TipoDomicilio = (String) registro.get("TipoDomicilio");
    	this.Domicilio = (String) registro.get("Domicilio");
    	this.FechaNacimiento = (Date) registro.get("FechaNacimiento");
    	this.NumeroCuenta = (Long) registro.get("NumeroCuenta");
    	this.CuentaNoDocumentada = (Long) registro.get("CuentaNoDocumentada");
    	this.CuentaCerrada = (Long) registro.get("CuentaCerrada");
    	this.DivisaSaldo = (String) registro.get("DivisaSaldo");
    	this.SaldoActualizado = (BigDecimal) registro.get("SaldoActualizado");
    	this.PaisResidenciaControlador = (String) registro.get("PaisResidenciaControlador");
    	this.NombreControlador = (String) registro.get("NombreControlador");
    	this.ApellidosControlador = (String) registro.get("ApellidosControlador");
    	this.PaisTaxIDControlador = (String) registro.get("PaisTaxIDControlador");
    	this.TaxIDControlador = (String) registro.get("TaxIDControlador");
    	this.PaisDomicilioControlador = (String) registro.get("PaisDomicilioControlador");
    	this.TipoDomicilioControlador = (String) registro.get("TipoDomicilioControlador");
    	this.DomicilioControlador = (String) registro.get("DomicilioControlador");
    	this.FechaNacimientoControlador = (Date) registro.get("FechaNacimientoControlador");
    	this.TipoPagoCuentaFinanciera = (String) registro.get("TipoPagoCuentaFinanciera");
    	this.DivisaPagoCuentaFinanciera = (String) registro.get("DivisaPagoCuentaFinanciera");
    	this.MontoPagoActualizado = (BigDecimal) registro.get("MontoPagoActualizado");
    	this.IdCliente = (Long) registro.get("IdCliente");
    	this.Nemo = (String) registro.get("Nemo");
    	this.TipoRegistro = (String) registro.get("TipoRegistro");
        this.RutFondo = (String) registro.get("RutFondo");
    
    }
	
	public Long getID(){
		return this.ID;
	}

	public void setID(Long ID){
		this.ID = ID;
	}

    public Long getIdDocumento() {
    	return this.IdDocumento;
    }
    public void setIdDocumento(Long IdDocumento) {
    	this.IdDocumento = IdDocumento;
    }


    public String getVboUsuario() {
    	return this.VboUsuario;
    }
    public void setVboUsuario(String VboUsuario) {
    	this.VboUsuario = VboUsuario;
    }


    public String getObservaciones() {
    	return this.Observaciones;
    }
    public void setObservaciones(String Observaciones) {
    	this.Observaciones = Observaciones;
    }


    public String getFuenteDatos() {
    	return this.FuenteDatos;
    }
    public void setFuenteDatos(String FuenteDatos) {
    	this.FuenteDatos = FuenteDatos;
    }


    public String getPaisResidenciaDeclarante() {
    	return this.PaisResidenciaDeclarante;
    }
    public void setPaisResidenciaDeclarante(String PaisResidenciaDeclarante) {
    	this.PaisResidenciaDeclarante = PaisResidenciaDeclarante;
    }

    public String getTipoInstitucion() {
    	return this.TipoInstitucion;
    }
    public void setTipoInstitucion(String TipoInstitucion) {
    	this.TipoInstitucion = TipoInstitucion;
    }

    public String getPoseeCuentasFinanciera() {
    	return this.PoseeCuentasFinanciera;
    }
    public void setPoseeCuentasFinanciera(String PoseeCuentasFinanciera) {
    	this.PoseeCuentasFinanciera = PoseeCuentasFinanciera;
    }

    public Date getPeriodoReporte() {
    	return this.PeriodoReporte;
    }
    public void setPeriodoReporte(Date PeriodoReporte) {
    	this.PeriodoReporte = PeriodoReporte;
    }


    public String getPaisResidencia() {
    	return this.PaisResidencia;
    }
    public void setPaisResidencia(String PaisResidencia) {
    	this.PaisResidencia = PaisResidencia;
    }


    public String getTipoPersona() {
    	return this.TipoPersona;
    }
    public void setTipoPersona(String TipoPersona) {
    	this.TipoPersona = TipoPersona;
    }

    public String getTipoJuridico() {
    	return this.TipoJuridico;
    }
    public void setTipoJuridico(String TipoJuridico) {
    	this.TipoJuridico = TipoJuridico;
    }

    public String getNombres() {
    	return this.Nombres;
    }
    public void setNombres(String Nombres) {
    	this.Nombres = Nombres;
    }


    public String getApellidos() {
    	return this.Apellidos;
    }
    public void setApellidos(String Apellidos) {
    	this.Apellidos = Apellidos;
    }


    public String getRazonSocial() {
    	return this.RazonSocial;
    }
    public void setRazonSocial(String RazonSocial) {
    	this.RazonSocial = RazonSocial;
    }


    public String getPaisTaxID() {
    	return this.PaisTaxID;
    }
    public void setPaisTaxID(String PaisTaxID) {
    	this.PaisTaxID = PaisTaxID;
    }

    public String getTaxID() {
    	return this.TaxID;
    }
    public void setTaxID(String TaxID) {
    	this.TaxID = TaxID;
    }


    public String getPaisDomicilio() {
    	return this.PaisDomicilio;
    }
    public void setPaisDomicilio(String PaisDomicilio) {
    	this.PaisDomicilio = PaisDomicilio;
    }

    public String getTipoDomicilio() {
    	return this.TipoDomicilio;
    }
    public void setTipoDomicilio(String TipoDomicilio) {
    	this.TipoDomicilio = TipoDomicilio;
    }

    public String getDomicilio() {
    	return this.Domicilio;
    }
    public void setDomicilio(String Domicilio) {
    	this.Domicilio = Domicilio;
    }


    public Date getFechaNacimiento() {
    	return this.FechaNacimiento;
    }
    public void setFechaNacimiento(Date FechaNacimiento) {
    	this.FechaNacimiento = FechaNacimiento;
    }


     public Long getNumeroCuenta() {
     	return this.NumeroCuenta;
     }
     public void setNumeroCuenta(Long NumeroCuenta) {
     	this.NumeroCuenta = NumeroCuenta;
     }


    public Long getCuentaNoDocumentada() {
    	return this.CuentaNoDocumentada;
    }
    public void setCuentaNoDocumentada(Long CuentaNoDocumentada) {
    	this.CuentaNoDocumentada = CuentaNoDocumentada;
	}

    public Long getCuentaCerrada() {
    	return this.CuentaCerrada;
    }
    public void setCuentaCerrada(Long CuentaCerrada) {
    	this.CuentaCerrada = CuentaCerrada;
    }

    public String getDivisaSaldo() {
    	return this.DivisaSaldo;
     }
     public void setDivisaSaldo(String DivisaSaldo) {
     	this.DivisaSaldo = DivisaSaldo;
     }

    public BigDecimal getSaldoActualizado() {
    	return this.SaldoActualizado;
    }
    public void setSaldoActualizado(BigDecimal SaldoActualizado) {
    	this.SaldoActualizado = SaldoActualizado;
    }


    public String getPaisResidenciaControlador() {
    	return this.PaisResidenciaControlador;
    }
	public void setPaisResidenciaControlador(String PaisResidenciaControlador) {
    	this.PaisResidenciaControlador = PaisResidenciaControlador;
    }

    public String getNombreControlador() {
     	return this.NombreControlador;
    }
    public void setNombreControlador(String NombreControlador) {
     	this.NombreControlador = NombreControlador;
    }


    public String getApellidosControlador() {
    	return this.ApellidosControlador;
    }
    public void setApellidosControlador(String ApellidosControlador) {
     	this.ApellidosControlador = ApellidosControlador;
    }


    public String getPaisTaxIDControlador() {
     	return this.PaisTaxIDControlador;
    }
    public void setPaisTaxIDControlador(String PaisTaxIDControlador) {
    	this.PaisTaxIDControlador = PaisTaxIDControlador;
    }

    public String getTaxIDControlador() {
    	return this.TaxIDControlador;
    }
    public void setTaxIDControlador(String TaxIDControlador) {
    	this.TaxIDControlador = TaxIDControlador;
	}

    public String getPaisDomicilioControlador() {
    	return this.PaisDomicilioControlador;
    }
    public void setPaisDomicilioControlador(String PaisDomicilioControlador) {
    	this.PaisDomicilioControlador = PaisDomicilioControlador;
    }

    public String getTipoDomicilioControlador() {
     	return this.TipoDomicilioControlador;
    }
     public void setTipoDomicilioControlador(String TipoDomicilioControlador) {
     	this.TipoDomicilioControlador = TipoDomicilioControlador;
    }

    public String getDomicilioControlador() {
     	return this.DomicilioControlador;
    }
    public void setDomicilioControlador(String DomicilioControlador) {
     	this.DomicilioControlador = DomicilioControlador;
    }

    public Date getFechaNacimientoControlador() {
     	return this.FechaNacimientoControlador;
	}
    public void setFechaNacimientoControlador(Date FechaNacimientoControlador) {
     	this.FechaNacimientoControlador = FechaNacimientoControlador;
    }

    public String getTipoPagoCuentaFinanciera() {
     	return this.TipoPagoCuentaFinanciera;
    }
    public void setTipoPagoCuentaFinanciera(String TipoPagoCuentaFinanciera) {
     	this.TipoPagoCuentaFinanciera = TipoPagoCuentaFinanciera;
    }

    public String getDivisaPagoCuentaFinanciera() {
	 	return this.DivisaPagoCuentaFinanciera;
    }
    public void setDivisaPagoCuentaFinanciera(String DivisaPagoCuentaFinanciera) {
     	this.DivisaPagoCuentaFinanciera = DivisaPagoCuentaFinanciera;
    }


    public BigDecimal getMontoPagoActualizado() {
     	return this.MontoPagoActualizado;
    }
    public void setMontoPagoActualizado(BigDecimal MontoPagoActualizado) {
     	this.MontoPagoActualizado = MontoPagoActualizado;
    }

    public Long getIdCliente() {
    	return this.IdCliente;
    }
    public void setIdCliente(Long IdCliente) {
     	this.IdCliente = IdCliente;
    }

    public String getNemo() {
     	return this.Nemo;
    }
    public void setNemo(String Nemo) {
     	this.Nemo = Nemo;
    }

    public String getTipoRegistro() {
    	return this.TipoRegistro;
    }
    public void setTipoRegistro(String TipoRegistro) {
    	this.TipoRegistro = TipoRegistro;
    }

     public String getRutFondo(){
        return this.RutFondo;
    }

    public void setRutFondo(String RutFondo){
        this.RutFondo = RutFondo;
    }

	public RegistroDJ1958(){};

    
}