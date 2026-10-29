SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarTipoFondo";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarTipoFondo"  
AS
SELECT 
 "TipoFondo"."IdTipoFondo"
, "TipoFondo"."IdTipoFondo" || '-' || "TipoFondo"."NombreCorto" AS "NombreCorto"
, "TipoFondo"."Descripcion"
, "TipoFondo"."IndicadorMoneyMarket"
FROM "BSCL.FondoMutuo.BD::FMM.TipoFondo" AS "TipoFondo";

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarTipoFondo" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
