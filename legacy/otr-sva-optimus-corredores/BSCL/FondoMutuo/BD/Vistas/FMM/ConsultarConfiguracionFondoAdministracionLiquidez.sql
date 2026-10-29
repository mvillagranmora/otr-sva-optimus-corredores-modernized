SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarConfiguracionFondoAdministracionLiquidez";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarConfiguracionFondoAdministracionLiquidez"
AS
SELECT
  "ConfiguracionFondoAdministracionLiquidez"."IdMoneda" 	AS "IdMoneda"
, "ConfiguracionFondoAdministracionLiquidez"."IdSerieFondo" AS "IdSerieFondo"
, "ConfiguracionFondoAdministracionLiquidez"."MontoMinimo"	AS "MontoMinimo"
, "ConfiguracionFondoAdministracionLiquidez"."IdEmpresa"
, "ConfiguracionFondoAdministracionLiquidez"."HoraEjecucion" AS "HoraCierreLiquidezComercial"
FROM "BSCL.FondoMutuo.BD::FMM.ConfiguracionAdministracionLiquidez" AS "ConfiguracionFondoAdministracionLiquidez";

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarConfiguracionFondoAdministracionLiquidez" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
