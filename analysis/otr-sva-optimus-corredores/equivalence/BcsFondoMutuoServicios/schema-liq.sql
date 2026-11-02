-- F3 W3 T6-B: H2 STAND-INS of what LiquidacionSolicitudAporteRescate reads and writes and the F1 schema lacks. H2 ONLY, never HANA. Loaded through "esquemasExtra" after schema-types / schema-fmm.
-- Shapes (legacy repository, read only): the SQL files cl/bcs/fondomutuo/sql/LiquidacionSolicitudAporteRescate_*.sql, ComunesSolicitudes_SelectModalidadPago.sql and cl/bcs/APV/sql/ConsultarRetencion.sql.
-- REAL = the column and type are in the catalog extracts of the project (fmm_columnas_vistas.csv); ASSUMED = in no catalog (the type is the natural one for the way the class reads it).
-- The view of H2 is a TABLE that each case writes (as in T1..T6-A).
-- view columns the two SELECTs read that the F1 stand-in lacks (ASSUMED): who is the AGF of the movement, its portfolio, the medium of payment of each leg, the omnibus and the APV tax matrix
ALTER TABLE "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescate" ADD COLUMN IF NOT EXISTS "IdPersonaAGF" BIGINT;
ALTER TABLE "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescate" ADD COLUMN IF NOT EXISTS "IdPortafolioAGF" BIGINT;
ALTER TABLE "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescate" ADD COLUMN IF NOT EXISTS "IdMedioDePagoAbonoAGF" NVARCHAR(3);
ALTER TABLE "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescate" ADD COLUMN IF NOT EXISTS "IdMedioDePagoCargoAGF" NVARCHAR(3);
ALTER TABLE "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescate" ADD COLUMN IF NOT EXISTS "IdMatrizAgrupadorAPV" BIGINT;
ALTER TABLE "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescate" ADD COLUMN IF NOT EXISTS "IdOmnibus" BIGINT;
-- Monto: REAL type is HANA DECIMAL(34) without scale (floating); the F1 stand-in says DECIMAL(34,0), which would round every amount to an integer
ALTER TABLE "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescate" ALTER COLUMN "Monto" DECFLOAT(34);
-- the sequences of the carta and of the comprobante of liquidation (H2 sequences of the same name replace SERIES_GENERATE_BIGINT)
CREATE SEQUENCE IF NOT EXISTS "BSCL.FondoMutuo.BD.Secuencias::FolioCartaLiquidacionAporteRescate" START WITH 1 MINVALUE 1 INCREMENT BY 1 NO CYCLE;
CREATE SEQUENCE IF NOT EXISTS "BSCL.FondoMutuo.BD.Secuencias::SecuenciaComprobanteLiquidacion" START WITH 1 MINVALUE 1 INCREMENT BY 1 NO CYCLE;
-- media of payment and their modality (ComunesSolicitudes_SelectModalidadPago.sql); the legacy builds a toMap over IdMedioDePago: a repeated medium raises
CREATE TABLE IF NOT EXISTS "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" ("IdModalidadDePago" VARCHAR(500), "IdMedioDePago" VARCHAR(500), "IdEstado" VARCHAR(500));
-- APV withholding configuration (cl/bcs/APV/sql/ConsultarRetencion.sql, ASSUMED shape: percentage in 0..100 as the SQL divides it by 100)
CREATE TABLE IF NOT EXISTS "BSCL.APV.BD::TRA.ConfiguracionRetencionTraspaso" ("PorcentajeRetencion" DECIMAL(26,8), "MontoTopeRetencion" DECIMAL(26,8), "CodigoRetiro" NVARCHAR(10), "IdPersonaRetencion" BIGINT,
  "IdRegimenTributario" BIGINT, "IdEstado" NVARCHAR(3));
