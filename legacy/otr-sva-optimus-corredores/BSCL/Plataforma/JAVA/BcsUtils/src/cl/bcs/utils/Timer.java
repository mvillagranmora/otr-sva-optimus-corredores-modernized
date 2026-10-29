package cl.bcs.utils;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Logger;
/**
 * Clase que permite medir el tiempo que tarda un bloque de codigo:
 * <pre>
 * Timer tm = Timer.createTimer();
 * tm.addMetric("INICIO PROCESO");
 * >> bloque que se desea medir.
 * tm.endMetric();
 * </pre>
 * @author chernandez
 *
 */
public class Timer {

	private LinkedList<Timer.Metric> metrics;
	private TimeUnit timeUnit;
	private Logger logger;
	
	public static Timer createTimer() {
		return new Timer();
	}

	public Timer setTimeUnit(final TimeUnit timeUnit) {
		this.timeUnit = timeUnit;
		return this;
	}

	public Timer setLogger(final Logger logger) {
		this.logger = logger;
		return this;
	}

	private Timer() {
		this.metrics = new LinkedList<Timer.Metric>();
		this.timeUnit = TimeUnit.SECONDS;
	}

	public Timer addMetric(final String flag) {
		this.lastMetricFix();
		this.metrics.add(new Timer.Metric(this, System.currentTimeMillis(), flag, this.timeUnit.name()));
		return this;
	}

	public Timer endMetric() {
		this.metrics.getLast().setEnd(System.currentTimeMillis());
		String msj = String.format("%s, tardo: %d(%s)", this.metrics.getLast().getFlag(), this.calculateMetric(this.metrics.getLast().getInit(), this.metrics.getLast().getEnd()),this.metrics.getLast().getUnit());
		if (this.logger != null) {
			this.logger.trace(msj);
		} else {
			System.out.println(msj);
		}
		return this;
	}
	
	public Timer clearMetrics() {
		this.metrics.clear();
		return this;
	}

	private void lastMetricFix() {
		if (!this.metrics.isEmpty() && this.metrics.getLast().end == null) {
			this.endMetric();
		}
	}

	private Long calculateMetric(final Long init, final Long end) {
		Long elapsedTime = end - init;
		if (this.timeUnit.equals(TimeUnit.SECONDS)) {
			elapsedTime /= 1000L;
		}
		return elapsedTime;
	}

	public void printMetrics() {
		this.lastMetricFix();
		System.out.println("");
		this.metrics.forEach(metric -> {
			String msj = String.format("%s, tardo: %d(%s)", metric.getFlag(), this.calculateMetric(metric.getInit(), metric.getEnd()), metric.getUnit());
			if (this.logger != null)
				this.logger.debug(msj);
			else
				System.out.println(msj);
		});
		clearMetrics();
	}
	
	

	public class Metric {

		protected Long init;
		protected Long end;
		protected String flag;
		protected String unit;

		public Metric(Timer paramTimer, Long init, String flag, String unit) {
			this.init = init;
			this.flag = flag;
			this.unit = unit;
		}

		public Long getInit() {
			return this.init;
		}

		public Long getEnd() {
			return this.end;
		}

		public void setEnd(Long end) {
			this.end = end;
		}

		public String getFlag() {
			return this.flag;
		}

		public String getUnit() {
			return this.unit;
		}
	}
}