-- F3 W3 T6-A: H2 STAND-INS of what CertificarPorFiltrosSolicitudAporteRescate_SelectSolicitudes.sql reads and the F1 / T1 stand-ins lack. H2 ONLY, never HANA. Loaded through "esquemasExtra".
-- Source of the shapes (legacy repository, read only): BD/Vistas/FMM/ConsultarSolicitudAporteRescate.sql (EsFondoNacional = CASE Fondo.FondoExtranjero WHEN 'S' THEN 'N' ELSE 'S' END; MoneyMarket = Fondo.MoneyMarket)
-- and BD/Scripts/FMM_Create_Tables.sql (FMM.DetalleCargaSolicitudesAporteRescate). The view of H2 is a TABLE that each case writes (as in T1..T5): the two columns below are written by the case
-- (UPDATE of the view row); the defaults are the values of an ordinary national fund that is not money market.
ALTER TABLE "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescate" ADD COLUMN IF NOT EXISTS "EsFondoNacional" NVARCHAR(1) DEFAULT 'S';
ALTER TABLE "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescate" ADD COLUMN IF NOT EXISTS "MoneyMarket" NVARCHAR(1) DEFAULT 'N';
CREATE TABLE IF NOT EXISTS "BSCL.FondoMutuo.BD::FMM.DetalleCargaSolicitudesAporteRescate" (
  "IdDetalleCargaSolicitudesAporteRescate" BIGINT NOT NULL,
  "IdCargaSolicitudesAporteRescate" BIGINT NOT NULL,
  "RutEmisor" NVARCHAR(40) NOT NULL,
  "CodigoPortafolioEmisor" NVARCHAR(100) NOT NULL,
  "TipoMovimiento" NVARCHAR(10) NOT NULL,
  "FechaMovimiento" DATE NOT NULL,
  "IdSolicitudAporteRescate" BIGINT NOT NULL,
  "DocumentoIdentidad" NVARCHAR(40) NOT NULL,
  "CodigoPortafolio" NVARCHAR(100) NOT NULL,
  "IdSerieFondo" NVARCHAR(40) NOT NULL,
  "CalculoPorMontoCuota" NVARCHAR(1) NOT NULL,
  "NumeroCuotas" DECIMAL(26,8) NOT NULL,
  "ValorCuota" DECIMAL(26,8) NOT NULL,
  "Monto" DECIMAL(26,8) NOT NULL,
  "MontoComision" DECIMAL(26,8) NOT NULL,
  "IdMoneda" NVARCHAR(3) NOT NULL,
  "IdSerieFondoDestino" NVARCHAR(40) NOT NULL,
  "IdCliente" BIGINT NOT NULL,
  "IdPortafolio" BIGINT NOT NULL,
  "IdEmisor" NVARCHAR(40) NOT NULL,
  "Concepto" NVARCHAR(10) DEFAULT '' NOT NULL,
  "IndicadorMLT" NVARCHAR(1) DEFAULT 'N' NOT NULL,
  "FechaCarga" DATE DEFAULT DATE '0001-01-01' NOT NULL,
  "FechaIngresoRegistro" TIMESTAMP DEFAULT TIMESTAMP '0001-01-01 00:00:00' NOT NULL,
  "Estado" NVARCHAR(3) NOT NULL,
  "DescEstado" NVARCHAR(100) NOT NULL,
  "EstadoProceso" NVARCHAR(3) NOT NULL,
  "Eliminado" NVARCHAR(1) NOT NULL,
  "IdEmpresa" BIGINT NOT NULL,
  PRIMARY KEY ("IdDetalleCargaSolicitudesAporteRescate")
);
