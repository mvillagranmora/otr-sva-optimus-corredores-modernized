
SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Normativa.BD.Vistas::CIR.ConsultarParametro"; 
 		
CREATE VIEW "BSCL.Normativa.BD.Vistas::CIR.ConsultarParametro" AS

SELECT
   NOR."IdParametro",
  -- NOR."Esquema",
   --NOR."Version",
   NOR."Periodo",
   NOR."Codigo",
   NOR."Descripcion",
   NOR."Valor" ,
   NOR."FechaIngreso",
   NOR."FechaCreacion",
   NOR."FechaModificacion",
--   NOR."IdEstado",
   "BSCL.Normativa.BD.Utilitarios::NOR.ObtenerEstadoAutorizacion"('CIRCULARIZACION_PARAMETRO',NOR."IdEstado",NOR."IdEstadoAutorizacion") AS "IdEstado",
   NOR."IdEstadoAutorizacion",
   NOR."IdUsuarioCreador" 	
FROM "BSCL.Normativa.BD::CIR.Parametro"  AS NOR;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::CIR.ConsultarParametro" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
 


