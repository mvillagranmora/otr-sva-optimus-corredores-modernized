
SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Normativa.BD.Vistas::PRO.ConsultarEstadisticaOperacionCliente"; 
 		
CREATE VIEW "BSCL.Normativa.BD.Vistas::PRO.ConsultarEstadisticaOperacionCliente" AS

SELECT
   PRO."IdEstadistica",
   PRO."IdEmpresa",
   PRO."IdCliente",
   PRO."MontoTotalDias",
   PRO."CantidadDias",
   PRO."MontoPromedioDiario",
   PRO."MontoMaximo",
   PRO."IdMoneda",
   PRO."IdTipoInstrumento",
   PRO."IdClasificacionTipoInstrumento",
   PRO."FechaIngreso",
   PRO."FechaCreacion",
   PRO."FechaModificacion"
FROM "BSCL.Normativa.BD::PRO.EstadisticaOperacionCliente" AS PRO 
ORDER BY PRO."IdEstadistica";

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::PRO.ConsultarEstadisticaOperacionCliente" TO  BCS_DESARROLLADOR WITH GRANT OPTION;  
