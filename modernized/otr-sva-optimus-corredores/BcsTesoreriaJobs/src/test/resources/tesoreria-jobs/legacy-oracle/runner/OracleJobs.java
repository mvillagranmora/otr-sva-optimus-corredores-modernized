import cl.bcs.scheduler.BcsJob;

/**
 * Thin subclasses of the UNMODIFIED legacy jobs whose only change is that onError(t) keeps the exception, so the oracle can
 * record its type and message (the legacy BcsJob.run() hands the exception to onError after status(jobId, true, message), and
 * the real onError of every job only logs and prints). Nothing else is overridden.
 */
final class OracleJobs {

    interface Capturado {
        Throwable error();
    }

    static final class ProcesarDistribucion extends cl.bcs.tesoreria.BcsJobProcesarDistribucion implements Capturado {
        Throwable t;

        @Override
        public void onError(Throwable x) {
            t = x;
        }

        public Throwable error() {
            return t;
        }
    }

    static final class CargaMasiva extends cl.bcs.tesoreria.cargaMasivaIngresoEgresoDAPFFMM.BcsJobCargaMasivaIngresoEgresoDAPFFMM implements Capturado {
        Throwable t;

        @Override
        public void onError(Throwable x) {
            t = x;
        }

        public Throwable error() {
            return t;
        }
    }

    static final class Cargos extends cl.bcs.tesoreria.generarArchivoCargos.GenerarArchivoCargos implements Capturado {
        Throwable t;

        @Override
        public void onError(Throwable x) {
            t = x;
        }

        public Throwable error() {
            return t;
        }
    }

    static final class Pago extends cl.bcs.tesoreria.generarArchivoPago.GenerarArchivoPago implements Capturado {
        Throwable t;

        @Override
        public void onError(Throwable x) {
            t = x;
        }

        public Throwable error() {
            return t;
        }
    }

    static BcsJob crear(String job) {
        return switch (job) {
            case "ProcesarDistribucion" -> new ProcesarDistribucion();
            case "CargaMasiva" -> new CargaMasiva();
            case "Cargos" -> new Cargos();
            case "Pago" -> new Pago();
            default -> throw new IllegalArgumentException("unknown job " + job);
        };
    }

    private OracleJobs() {
    }
}
