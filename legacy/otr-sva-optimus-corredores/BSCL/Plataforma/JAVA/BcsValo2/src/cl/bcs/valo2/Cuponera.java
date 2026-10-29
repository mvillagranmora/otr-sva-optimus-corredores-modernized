package cl.bcs.valo2;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cl.bcs.valo2.cupones.ProveedorCupones;
import cl.bcs.valo2.cupones.ProveedorCuponesBullet;
import cl.bcs.valo2.cupones.ProveedorCuponesCall;
import cl.bcs.valo2.cupones.ProveedorCuponesCero;
import cl.bcs.valo2.cupones.ProveedorCuponesVariable;
import cl.bcs.valo2.entidades.Bono;
import cl.bcs.valo2.entidades.Cupon;
import cl.bcs.valo2.fechas.Fecha;
import cl.bcs.valo2.fechas.contadores.ContadorDias;
import cl.bcs.valo2.fechas.contadores.ContadorDiasParams;
import cl.bcs.valo2.repositorios.RepositorioBonos;

public class Cuponera {
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	private static ContadorDiasParams newContadorDiasParams(final Bono bono) {
		final ContadorDiasParams params = new ContadorDiasParams();
		params.setAjusteEOM(bono.isAjusteFinDeMes());
		params.setCalendario(bono.getCalendario());
		params.setConvencionDias(bono.getConvencionDias());
		params.setFechaVencimiento(Fecha.getInstance(bono.getFechaVencimiento()));
		return params;
	}

	public static void print(final PrintStream out, final List<Cupon> cupones) {
		final DecimalFormat df = new DecimalFormat();
		df.setGroupingUsed(true);
		df.setMaximumFractionDigits(2);
		df.setMinimumFractionDigits(2);

		out.println(String.format("N.\tFecha Ant.\tFecha Pago\tPeriodo\tVencim.\t%15s\t%15s\t%15s", "Saldo",
				"Amortizacion", "Intereses"));
		for (final Cupon cupon : cupones) {
			final String str = String.format("%02d\t%s\t%s\t%s\t%s\t%s\t%s\t%s", cupon.getNumero(),
					DATE_FORMAT.format(cupon.getFechaAnterior()), DATE_FORMAT.format(cupon.getFecha()),
					cupon.getPeriodoEnDias(), cupon.getVencimientoEnDias(),
					String.format("%15s", df.format(cupon.getSaldoPorAmortizar())),
					String.format("%15s", df.format(cupon.getAmortizacion())),
					String.format("%15s", df.format(cupon.getIntereses())));
			out.println(str);
		}
	}

	private final ContadorDias contadorDias;
	private final ValorizadorContexto contexto;
	private final ProveedorCuponesBullet proveedorCuponesBullet;
	private final ProveedorCuponesCall proveedorCuponesCall;
	private final ProveedorCuponesVariable proveedorCuponesVariable;
	private final RepositorioBonos repositorioBonos;
	private final ProveedorCupones proveedorCuponesCero;

	public Cuponera(final ValorizadorContexto contexto) throws SQLException {
		this.contexto = contexto;
		this.contadorDias = new ContadorDias(contexto);
		this.proveedorCuponesCall = new ProveedorCuponesCall(contexto);
		this.proveedorCuponesVariable = new ProveedorCuponesVariable(contexto);
		this.proveedorCuponesBullet = new ProveedorCuponesBullet(contexto);
		this.proveedorCuponesCero = new ProveedorCuponesCero();
		this.repositorioBonos = new RepositorioBonos(contexto);
	}

	public BigDecimal calcularMonto(final BigDecimal valor, final double factor) {
		return new BigDecimal(valor.doubleValue() * factor).setScale(contexto.getConfig().getEscalaMontos(),
				RoundingMode.HALF_UP);
	}

	private double calcularPeriodoEnAnos(final Cupon cupon, final ContadorDiasParams params) {
		return contadorDias.calcularFraccionAno(Fecha.getInstance(cupon.getFechaAnterior()),
				Fecha.getInstance(cupon.getFecha()), params);
	}

	private int calcularPeriodoEnDias(final Cupon cupon, final ContadorDiasParams params) {
		return contadorDias.calcularDiasCorridos(Fecha.getInstance(cupon.getFechaAnterior()),
				Fecha.getInstance(cupon.getFecha()), params);
	}

	public void calcularTiempo(final List<Cupon> cupones, final Date fecha, final ContadorDiasParams params) {
		for (final Cupon cupon : cupones) {
			cupon.setPeriodoEnAnos(calcularPeriodoEnAnos(cupon, params));
			cupon.setPeriodoEnDias(calcularPeriodoEnDias(cupon, params));
			cupon.setVencimientoEnAnos(calcularVencimientoEnAnos(cupon, params, fecha));
			cupon.setVencimientoEnDias(calcularVencimientoEnDias(cupon, params, fecha));
		}
	}

	private double calcularVencimientoEnAnos(final Cupon cupon, final ContadorDiasParams params, final Date fecha) {
		return contadorDias.calcularFraccionAno(Fecha.getInstance(fecha), Fecha.getInstance(cupon.getFecha()), params);
	}

	private int calcularVencimientoEnDias(final Cupon cupon, final ContadorDiasParams params, final Date fecha) {
		return contadorDias.calcularDiasCorridos(Fecha.getInstance(fecha), Fecha.getInstance(cupon.getFecha()), params);
	}

	private void escalar(final List<Cupon> cupones, final BigDecimal valorNominal) {
		final double factor = valorNominal.doubleValue() / 100;

		for (final Cupon cupon : cupones) {
			cupon.setAmortizacion(calcularMonto(cupon.getAmortizacion(), factor));
			cupon.setIntereses(calcularMonto(cupon.getIntereses(), factor));
			cupon.setSaldoPorAmortizar(calcularMonto(cupon.getSaldoPorAmortizar(), factor));
		}
	}

	public ProveedorCupones getProveedorCupones(final Bono bono) {
		if (bono.isCuponeraVariable()) {
			if (bono.isCallConfirmado()) {
				return proveedorCuponesCall;
			} else {
				return proveedorCuponesVariable;
			}
		} else {
			/* Se comenta ya que desarrollo no se finaliza por priorización de Optimus Chile
			if (bono.getCantidadCupones() == 0) {
				return proveedorCuponesCero;
			} else {
				return proveedorCuponesBullet;
			}*/
			
			return proveedorCuponesBullet;
		}
	}

	public List<Cupon> obtenerCupones(final Bono bono) {
		return obtenerCupones(bono, bono.getFechaDevengo());
	}

	public List<Cupon> obtenerCupones(final Bono bono, final Date fecha) {
		return obtenerCupones(bono, fecha, null);
	}

	public List<Cupon> obtenerCupones(final Bono bono, final Date fecha, final BigDecimal valorNominal) {
		final ContadorDiasParams params = newContadorDiasParams(bono);
		final ProveedorCupones proveedorCupones = getProveedorCupones(bono);
		final List<Cupon> cupones = proveedorCupones.obtener(bono, fecha);
		calcularTiempo(cupones, fecha, params);
		if (valorNominal != null) {
			escalar(cupones, valorNominal);
		}

		return cupones;
	}

	public List<Cupon> obtenerCupones(final String nemo, final Date fecha) {
		return obtenerCupones(nemo, fecha, null);
	}

	public List<Cupon> obtenerCupones(final String nemo, final Date fecha, final BigDecimal valorNominal) {
		final Bono bono = repositorioBonos.buscar(nemo);
		return obtenerCupones(bono, fecha, valorNominal);
	}

}
