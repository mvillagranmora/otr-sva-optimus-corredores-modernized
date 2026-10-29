SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::LIM.ConsultarEvaluacionLimite"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::LIM.ConsultarEvaluacionLimite"  
AS
SELECT
	EVA."IdEvaluacionLimite",
	EVA."IdSecuenciaEvaluacion",
	EVA."IdLimite",
	EVA."IdTipoOperacion",
	EVA."IdOperacion",
	EVA."IdUsuarioAutoriza",
	EVA."IdMoneda",
	EVA."ConceptoExceso",
	EVA."MontoExcedido",
	EVA."MontoUtilizado",
	EVA."FechaIngreso",
	EVA."FechaCreacion",
	EVA."IdEstado",
	--"BSCL.Plataforma.BD.Utilitarios::ObtenerEstadoAutorizacion"('EVALUACIONLIMITE',EVA."IdEstado",EVA."IdEstadoAutorizacion") AS "IdEstado",
	EVA."FechaAutorizacion",
	EVA."IdEstadoAutorizacion",
	EVA."IdUsuarioCreador",
	EVA."IdTipoEvento",--Tipo evento de autorizacion
	EVA."Prioridad", --Prioridad de autorizacion
	EVA."ConceptoEvaluacion",
	EVA."IdTipoEntidad",
	EVA."IdTipoInstrumento",
	EVA."IdClasificacionTipoInstrumento",
	EVA."IdTipoLimite",
	EVA."MontoLimite",
	EVA."MontoCalculado",
	EVA."IdMonedaCalculo",
	EVA."IdCliente",
	EVA."IdPortafolio",
	EVA."IdCanalIngreso",
	CASE WHEN (EVA."MontoLimite" >= EVA."MontoUtilizado") THEN (EVA."MontoLimite" - EVA."MontoUtilizado") ELSE 
		0 END AS "MontoRemanente"
FROM
	"BSCL.Plataforma.BD::LIM.EvaluacionLimites" EVA;

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::LIM.ConsultarEvaluacionLimite" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
