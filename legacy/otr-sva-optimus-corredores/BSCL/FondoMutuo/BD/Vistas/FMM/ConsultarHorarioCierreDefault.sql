SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarHorarioCierreDefault";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarHorarioCierreDefault"
AS
SELECT
 "AporteComercial"."Value" AS "HoraCierreAporteComercial"
, "AporteBackOffice"."Value" AS "HoraCierreAporteBackOffice"
, "RescateComercial"."Value" AS "HoraCierreRescateComercial"
, "RescateBackOffice"."Value" AS "HoraCierreRescateBackOffice"
, "ReinversionComercial"."Value" AS "HoraCierreReinversionComercial"
, "ReinversionBackOffice"."Value" AS "HoraCierreReinversionBackOffice"
, "LiquidezComercial"."Value" AS "HoraCierreLiquidezComercial"
, "LiquidezBackOffice"."Value" AS "HoraCierreLiquidezBackOffice"
, "AporteComercial"."IdEmpresa"
FROM "BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo" AS "AporteComercial"
INNER JOIN "BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo" AS "AporteBackOffice" ON ("AporteComercial"."IdEmpresa" = "AporteBackOffice"."IdEmpresa")
INNER JOIN "BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo" AS "RescateComercial" ON ("RescateComercial"."IdEmpresa" = "AporteBackOffice"."IdEmpresa")
INNER JOIN "BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo" AS "RescateBackOffice" ON ("RescateComercial"."IdEmpresa" = "RescateBackOffice"."IdEmpresa")
INNER JOIN "BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo" AS "ReinversionComercial" ON ("ReinversionComercial"."IdEmpresa" = "RescateBackOffice"."IdEmpresa")
INNER JOIN "BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo" AS "ReinversionBackOffice" ON ("ReinversionComercial"."IdEmpresa" = "ReinversionBackOffice"."IdEmpresa")
INNER JOIN "BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo" AS "LiquidezComercial" ON ("LiquidezComercial"."IdEmpresa" = "ReinversionBackOffice"."IdEmpresa")
INNER JOIN "BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo" AS "LiquidezBackOffice" ON ("LiquidezComercial"."IdEmpresa" = "LiquidezBackOffice"."IdEmpresa")
WHERE "AporteComercial"."Key" = 'HORA_CIERRE_APORTE_COMERCIAL'
AND "AporteBackOffice"."Key" = 'HORA_CIERRE_APORTE_BACKOFFICE'
AND "RescateComercial"."Key" = 'HORA_CIERRE_RESCATE_COMERCIAL'
AND "RescateBackOffice"."Key" = 'HORA_CIERRE_RESCATE_BACKOFFICE'
AND "ReinversionComercial"."Key" = 'HORA_CIERRE_REINVERSION_COMERCIAL'
AND "ReinversionBackOffice"."Key" = 'HORA_CIERRE_REINVERSION_BACKOFFICE'
AND "LiquidezComercial"."Key" = 'HORA_CIERRE_LIQUIDEZ_COMERCIAL'
AND "LiquidezBackOffice"."Key" = 'HORA_CIERRE_LIQUIDEZ_BACKOFFICE';

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarHorarioCierreDefault" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
