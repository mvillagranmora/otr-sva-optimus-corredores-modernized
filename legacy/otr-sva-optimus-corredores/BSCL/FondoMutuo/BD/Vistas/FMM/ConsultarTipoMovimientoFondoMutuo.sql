SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarTipoMovimientoFondoMutuo";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarTipoMovimientoFondoMutuo"  
AS
SELECT
 "IdTipoMovimientoFondoMutuo"
, "Descripcion"
, "IndicadorAporteRescate"
, "IdTipoOperacion"
, "TieneCarga"
, "TieneIngresoManual"
, "TieneCertificacion"
, "IdEstado"
, "GeneraCargoAbono"
FROM "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" AS "TipoMovimientoFondoMutuo";

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarTipoMovimientoFondoMutuo" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 