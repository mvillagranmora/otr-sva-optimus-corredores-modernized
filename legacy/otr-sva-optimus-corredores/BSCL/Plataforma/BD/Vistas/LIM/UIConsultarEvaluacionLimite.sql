SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::LIM.UIConsultarEvaluacionLimite";
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::LIM.UIConsultarEvaluacionLimite"
AS
SELECT
	EVA."IdEvaluacionLimite",
	EVA."IdSecuenciaEvaluacion",
	EVA."IdLimite",
	EVA."IdTipoOperacion",
	IFNULL(TIPOP."Nombre",' -') AS "TipoOperacion",
	EVA."IdOperacion",
	EVA."IdUsuarioAutoriza",
	EVA."IdMoneda",
	IFNULL(MDA."NombreLargo",' -') AS "Moneda",
	EVA."ConceptoExceso",
	EVA."MontoExcedido",
	EVA."MontoUtilizado",
	EVA."FechaIngreso",
	EVA."FechaCreacion",
	EVA."IdEstado",
	IFNULL(ESTDET."Nombre",' -') as "EstadoEvaluacion",
	EVA."FechaAutorizacion",
	EVA."IdEstadoAutorizacion",
	IFNULL(ESTAUT."Nombre",' -') as "EstadoAutorizacion",
	EVA."IdUsuarioCreador",
	EVA."IdTipoEvento",
	EVA."Prioridad",
	EVA."ConceptoEvaluacion",
	EVA."IdTipoEntidad",
	EVA."IdTipoInstrumento",
	IFNULL(TIPINS."Nombre",' -') AS "TipoInstrumento",
	EVA."IdClasificacionTipoInstrumento",
	IFNULL(CLATI."Nombre",' -') AS "ClasificacionTipoInstrumento",
	EVA."IdTipoLimite",
	IFNULL(TIPLIM."Nombre",' -') AS "TipoLimite",
	EVA."MontoLimite",
	EVA."MontoCalculado",
	EVA."IdMonedaCalculo",
	IFNULL(MDACAL."NombreLargo",' -') AS "MonedaCalculo",
	EVA."IdCliente",
	CLI."DocumentoIdentidad",
	CLI."TipoDocumentoIdentidad",
	IFNULL(CLI."NombreCompleto",' -') AS "NombreCliente",
	CLI."IdEmpresa",
	EVA."IdPortafolio",
	IFNULL(PORT."CodigoPortafolio",' -') AS "CodigoPortafolio",
	EVA."IdCanalIngreso",
	CASE EVA."IdCanalIngreso" WHEN '0' THEN 'TODOS' ELSE TIPCAN."Nombre" END AS "CanalIngreso",
	EVA."MontoRemanente"
FROM "BSCL.Plataforma.BD.Vistas::LIM.ConsultarEvaluacionLimite"	AS EVA
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion"				AS TIPOP	ON (TIPOP."IdTipoOperacion" = EVA."IdTipoOperacion")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarMoneda"		AS MDA		ON (MDA."IdMoneda" = EVA."IdMoneda")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado"						AS ESTDET	ON (ESTDET."IdEstado" = EVA."IdEstado")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado"						AS ESTAUT	ON (ESTAUT."IdEstado" = EVA."IdEstadoAutorizacion")
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento"				AS TIPINS	ON (TIPINS."IdTipoInstrumento" = EVA."IdTipoInstrumento")
LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS CLATI	ON (CLATI."IdClasificacionTipoInstrumento" = EVA."IdClasificacionTipoInstrumento")
LEFT JOIN "BSCL.Plataforma.BD::LIM.TipoLimite"					AS TIPLIM	ON (TIPLIM."IdTipoLimite" = EVA."IdTipoLimite")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarMoneda"		AS MDACAL	ON (MDACAL."IdMoneda" = EVA."IdMonedaCalculo")
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"	AS CLI		ON (CLI."IdCliente" = EVA."IdCliente")
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio"					AS PORT		ON (PORT."IdPortafolio" = EVA."IdPortafolio")
LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoCanalIngreso"			AS TIPCAN	ON (TIPCAN."IdTipoCanalIngreso" = EVA."IdCanalIngreso")
ORDER BY EVA."IdSecuenciaEvaluacion" DESC;

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::LIM.UIConsultarEvaluacionLimite" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
