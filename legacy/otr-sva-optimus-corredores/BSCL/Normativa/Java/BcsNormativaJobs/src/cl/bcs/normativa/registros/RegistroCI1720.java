package cl.bcs.normativa.registros;

import cl.bcs.normativa.data.Registro;

public class RegistroCI1720 {

	/*
	 * Nemotecnico.
	 */
	private String nemotecnico;
	
	/*
	 * Nombre del Emisor.
	 */
	private String nombreEmisor;
	
	/*
	 * Tipo de Instrumento.
	 */
	private String tipoInstrumento;
	
	/*
	 * Cantidad de Unidades Nominales.
	 */
	private double cantidadUnidadesNominal;
	
	/*
	 * Tipo de Unidades
	 */
	private String tipoUnidades;
	
	/*
	 * Valorizacion.
	 */
	private long valorizacion;
	
	/*
	 * Cartera Propia.
	 */
	private double carteraPropia;
	
	/*
	 * Custodia de Terceros.
	 */
	private double custodiaTerceros;
	
	/*
	 * Custiodia pripia en DCV.
	 */
	private double custodiaPropiaDCV;
	
	/*
	 * Custodia de Terceros en DCV.
	 */
	private double custodiaTercerosDCV;
	
	/*
	 * Custodia en Sociedad Emisora.
	 */
	private double custodiaSociedadEmisora;
	
	/*
	 * Custodia en el Intermediario
	 */
	private double custodiaIntermediario;
	
	/*
	 * Custodia en Bolsas.
	 */
	private double custodiaBolsas;
	
	/*
	 * Custodia en Otros
	 */
	private double custodiaOtros;
	
	public RegistroCI1720() {
	}
	
	/*
	 * Constructor.
	 * 
	 * @param registro
	 * 					El regstro de datos.
	 */
	public RegistroCI1720(final Registro registro) {
		this.nemotecnico = (String) registro.get("Nemotecnico");
		this.nombreEmisor = (String) registro.get("NombreEmisor");
		this.tipoInstrumento = (String) registro.get("TipoInstrumento");
		this.cantidadUnidadesNominal = (double) ( (Number) registro.get("CantidadUnidadesNominal")).doubleValue();
		this.tipoUnidades = (String) registro.get("TipoUnidades");
		this.valorizacion = (Long) registro.get("Valorizacion");
		this.carteraPropia = (double) ((Number) registro.get("CarteraPropia")).doubleValue();
		this.custodiaTerceros = (double) ((Number) registro.get("CustodiaTerceros")).doubleValue();
		this.custodiaPropiaDCV = (double) ((Number) registro.get("CustodiaPropiaDCV")).doubleValue();
		this.custodiaTercerosDCV = (double) ((Number) registro.get("CustodiaTercerosDCV")).doubleValue();
		this.custodiaSociedadEmisora = (double) ((Number) registro.get("CustodiaSociedadEmisora")).doubleValue();
		this.custodiaIntermediario = (double) ((Number) registro.get("CustodiaIntermediario")).doubleValue();
		this.custodiaBolsas = (double) ((Number) registro.get("CustodiaBolsas")).doubleValue();
		this.custodiaOtros = (double) ((Number) registro.get("CustodiaOtros")).doubleValue();
	}
	
	
	
	/**
	 * @return the nemotecnico
	 */
	public String getNemotecnico() {
		return nemotecnico;
	}

	/**
	 * @return the nombreEmisor
	 */
	public String getNombreEmisor() {
		return nombreEmisor;
	}

	/**
	 * @return the tipoInstrumento
	 */
	public String getTipoInstrumento() {
		return tipoInstrumento;
	}

	/**
	 * @return the cantidadUnidadesNominal
	 */
	public double getCantidadUnidadesNominal() {
		return cantidadUnidadesNominal;
	}

	/**
	 * @return the tipoUnidades
	 */
	public String getTipoUnidades() {
		return tipoUnidades;
	}

	/**
	 * @return the valorizacion
	 */
	public long getValorizacion() {
		return valorizacion;
	}

	/**
	 * @return the carteraPropia
	 */
	public double getCarteraPropia() {
		return carteraPropia;
	}

	/**
	 * @return the custodiaTerceros
	 */
	public double getCustodiaTerceros() {
		return custodiaTerceros;
	}

	/**
	 * @return the custodiaPropiaDCV
	 */
	public double getCustodiaPropiaDCV() {
		return custodiaPropiaDCV;
	}

	/**
	 * @return the custodiaTercerosDCV
	 */
	public double getCustodiaTercerosDCV() {
		return custodiaTercerosDCV;
	}

	/**
	 * @return the custodiaSociedadEmisora
	 */
	public double getCustodiaSociedadEmisora() {
		return custodiaSociedadEmisora;
	}

	/**
	 * @return the custodiaIntermediario
	 */
	public double getCustodiaIntermediario() {
		return custodiaIntermediario;
	}

	/**
	 * @return the custodiaBolsas
	 */
	public double getCustodiaBolsas() {
		return custodiaBolsas;
	}

	/**
	 * @return the custodiaOtros
	 */
	public double getCustodiaOtros() {
		return custodiaOtros;
	}
}
