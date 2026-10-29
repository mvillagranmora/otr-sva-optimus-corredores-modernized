
SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Normativa.BD.Vistas::UAF.ConsultarEvaluacionOperacionSOI"; 
 		
CREATE VIEW "BSCL.Normativa.BD.Vistas::UAF.ConsultarEvaluacionOperacionSOI" AS

	SELECT
		   SOI."IdEvaluacion",
		   SOI."IdEmpresa",
		   SOI."IdModulo",
		   SOI."IdCliente",
		   SOI."IdPortafolio",
		   SOI."IdTipoProducto",
		   SOI."IdTipoOperacion",
		   SOI."IdOperacion",
		   SOI."MontoUtilizado",
		   SOI."IdMoneda",
		   SOI."IdTipoInstrumento",
		   SOI."IdClasificacionTipoInstrumento",
		   SOI."FechaOperacion",--'YYYY-MM-DD'
		    ---Evaluacion
		   SOI."TipoEvaluacion",
			---Auditoria
		   SOI."FechaIngreso",
		   SOI."FechaCreacion",
		   SOI."IdUsuarioCreador"			
		FROM "BSCL.Normativa.BD::UAF.EvaluacionOperacionSOI" SOI;	

	
GRANT SELECT ON "BSCL.Normativa.BD.Vistas::UAF.ConsultarEvaluacionOperacionSOI" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
 


