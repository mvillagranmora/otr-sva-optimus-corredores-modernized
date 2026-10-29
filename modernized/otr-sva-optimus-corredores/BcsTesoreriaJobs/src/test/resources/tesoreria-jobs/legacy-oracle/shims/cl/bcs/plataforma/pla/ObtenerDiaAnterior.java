package cl.bcs.plataforma.pla;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonElement;

import cl.bcs.oracle.Hooks;
import cl.bcs.utils.Utils;

/**
 * ORACLE FAKE of legacy cl.bcs.plataforma.pla.ObtenerDiaAnterior (Plataforma/.../pla/ObtenerDiaAnterior.java:39-76, POJOs as legacy).
 * logica() records its input and answers "colaboradores.ObtenerDiaAnterior": {"salida": {"fechas":[{"fechaCalculada": "yyyy-MM-dd"}]}}
 * (absent: one entry with fechaCalculada "2025-03-31"); "lanza" scripts a failure.
 */
public class ObtenerDiaAnterior {

	public static class InObtenerDiaAnterior {
		public Long idPais;
		public String diaHabil;
		public String soloCaeDiaHabil;
		public boolean excluirConsultarDiaFeriado = false;
		public List<FechasIn> fechas;

		public static class FechasIn {
			public FechasIn() {
			}

			public FechasIn(String fechaInicial, Long numeroDias) {
				this.fechaInicial = fechaInicial;
				this.numeroDias = numeroDias;
			}

			public String fechaInicial;
			public Long numeroDias;
		}

		public InObtenerDiaAnterior() {
			this.fechas = new ArrayList<FechasIn>();
		}
	}

	public static class OutObtenerDiaAnterior {
		public Long erroresContador;
		public List<FechasOut> fechas;

		public static class FechasOut {
			public String fechaInicial;
			public Long numeroDiasInicial;
			public Long numeroDias;
			public String fechaCalculada;
		}

		public OutObtenerDiaAnterior() {
			this.erroresContador = 0L;
			this.fechas = new ArrayList<FechasOut>();
		}
	}

	public OutObtenerDiaAnterior logica(Connection con, InObtenerDiaAnterior entrada) throws Exception {
		Hooks.registrar("ObtenerDiaAnterior", entrada);
		Hooks.lanzaSiCorresponde("ObtenerDiaAnterior.lanza");
		JsonElement s = Hooks.objeto("ObtenerDiaAnterior").get("salida");
		if (s == null) {
			OutObtenerDiaAnterior o = new OutObtenerDiaAnterior();
			OutObtenerDiaAnterior.FechasOut f = new OutObtenerDiaAnterior.FechasOut();
			f.fechaCalculada = "2025-03-31";
			o.fechas.add(f);
			return o;
		}
		return Utils.getGsonBuilder().fromJson(s, OutObtenerDiaAnterior.class);
	}
}
