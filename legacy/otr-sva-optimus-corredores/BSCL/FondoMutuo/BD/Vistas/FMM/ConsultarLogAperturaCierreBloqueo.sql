SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarLogAperturaCierreBloqueo";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarLogAperturaCierreBloqueo"
AS
SELECT 
 "IdLogAperturaCierreBloqueo"
, "IdUsuario"
, "IdEmpresa"
, "IdFondo"
, "FechaSistema"
, "FechaIngresoRegistro"
, "EstadoAporteComercialAntiguo"
, "EstadoRescateComercialAntiguo"
, "EstadoReinversionComercialAntiguo"
, "EstadoLiquidezComercialAntiguo"
, "EstadoAporteBackofficeAntiguo"
, "EstadoRescateBackofficeAntiguo"
, "EstadoReinversionBackofficeAntiguo"
, "EstadoLiquidezBackofficeAntiguo"
, "BloqueoAporteAntiguo"
, "BloqueoRescateAntiguo"
, "BloqueoIndefinidoAntiguo"
, "FechaBloqueoDesdeAntiguo"
, "FechaBloqueoHastaAntiguo"
, "EstadoAporteComercialNuevo"
, "EstadoRescateComercialNuevo"
, "EstadoReinversionComercialNuevo"
, "EstadoLiquidezComercialNuevo"
, "EstadoAporteBackofficeNuevo"
, "EstadoRescateBackofficeNuevo"
, "EstadoReinversionBackofficeNuevo"
, "EstadoLiquidezBackofficeNuevo"
, "BloqueoAporteNuevo"
, "BloqueoRescateNuevo"
, "BloqueoIndefinidoNuevo"
, "FechaBloqueoDesdeNuevo"
, "FechaBloqueoHastaNuevo"
FROM "BSCL.FondoMutuo.BD::FMM.LogAperturaCierreBloqueo";

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarLogAperturaCierreBloqueo" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
