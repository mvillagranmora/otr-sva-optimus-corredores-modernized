SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarTipoMovimientoFondoMutuoCarga";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarTipoMovimientoFondoMutuoCarga"  
AS
SELECT
 "IdTipoMovimientoFondoMutuo" AS "TipoMovimiento"
, "Descripcion"
, "IndicadorAporteRescate"
, "IdTipoOperacion"
, "IdEstado"
FROM "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" AS "TipoMovimientoFondoMutuo"
WHERE "IdTipoMovimientoFondoMutuo" IN ('APONORM','RESNORM','APOREINV','RESREINV','APODIV','RESCOM');

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarTipoMovimientoFondoMutuoCarga" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
