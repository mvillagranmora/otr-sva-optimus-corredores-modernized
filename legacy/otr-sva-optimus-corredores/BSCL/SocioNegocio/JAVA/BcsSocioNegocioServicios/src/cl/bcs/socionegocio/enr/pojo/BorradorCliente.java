package cl.bcs.socionegocio.enr.pojo;

import java.util.ArrayList;
import java.util.List;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;

public class BorradorCliente extends BcsPojo {
	public Long IdInterno;
	public Enrolamiento Enrolamiento;
	public List<DocumentoIdentidad> DocumentosIdentidades;
	public List<CuentaBancaria> CuentasBancaria;
	public List<TarjetaBancaria> TarjetasBancaria;
	public List<DireccionFisica> DireccionesFisica;
	public List<DireccionElectronicaEnrolamiento> DireccionesElectronica;
	public List<Telefono> Telefonos;
	public List<GrupoDireccionEnrolamiento> GruposDireccion;
	public List<GrupoDireccionDetalleEnrolamiento> GruposDireccionDetalle;
	public List<Indicador> Indicadores;
	public List<RelacionPersona> Relaciones;
	public Fatca Fatca;
	public List<FatcaPaisResidencia> FatcaPaisesResidencia;
	public List<CuentaCustodioPortafolio> CuentasCustodio;
	public List<PreferenciaPagoPortafolio> PreferenciasPago;
	public List<MensajeSalida> MensajesSalida;
	public String EsPersonaNueva;
	public Long IdPersona;
	public Long IdSocioNegocio;
	public Cliente Cliente;
	public Long IdPortafolio;
	public BorradorCliente() {
		this.EsPersonaNueva = "N";
		this.IdPersona = 0L;
		this.DocumentosIdentidades = new ArrayList<>();
		this.CuentasBancaria = new ArrayList<>();
		this.TarjetasBancaria = new ArrayList<>();
		this.DireccionesFisica = new ArrayList<>();
		this.DireccionesElectronica = new ArrayList<>();
		this.Telefonos = new ArrayList<>();
		this.GruposDireccion = new ArrayList<>();
		this.GruposDireccionDetalle = new ArrayList<>();
		this.Indicadores = new ArrayList<>();
		this.Relaciones = new ArrayList<>();
		this.FatcaPaisesResidencia = new ArrayList<>();
		this.CuentasCustodio = new ArrayList<>();
		this.PreferenciasPago = new ArrayList<>();
		this.MensajesSalida = new ArrayList<>();
		this.Cliente = new Cliente();
		this.IdPortafolio = 0L;
	}
}
