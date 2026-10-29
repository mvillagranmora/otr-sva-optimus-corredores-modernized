package cl.bcs.custodia.jobs;

/** ORACLE SHIM of legacy cl.bcs.custodia.jobs.ValorizarCustodiaUIbatchJob: only the nested POJO BcsJobProcesarDistribucion imports (ValorizarCustodiaUIbatchJob.java:31-39, verbatim). */
public class ValorizarCustodiaUIbatchJob {

	public static class ActualizarPrograma {
		public String RutaSalida;
		public Long IdPrograma;

		public ActualizarPrograma(String RutaSalida, Long IdPrograma) {
			this.RutaSalida = RutaSalida;
			this.IdPrograma = IdPrograma;
		}
	}
}
