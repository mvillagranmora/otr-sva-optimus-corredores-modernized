SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Mesa.BD.Vistas::SPT.ConsultarRequisitosOperativosSpot";
 
CREATE VIEW "BSCL.Mesa.BD.Vistas::SPT.ConsultarRequisitosOperativosSpot" AS
SELECT	"IdTipoEtapa","IdEmpresa","IdTipoCanalIngreso",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'MESACERRADA' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_MesaActiva",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'DOLARMENORIGUALCERO' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_DolarMayorCero",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'EXCEDEHORARIO' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_HorarioValido",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'FECHAINVALIDA' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_FechaProcesoValido",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'TIPOCOMPROBANTEINVALIDO' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_TipoComprobanteValido",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'VALORDOLARACTUALCERO' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_ValorDolarFechaProceso",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'PARIDADPRINCIPALCERO'  AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_ParidadPrincipalFechaProceso",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'PARIDADSECUNDARIACERO' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_ParidadSecundariaFechaProceso",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'CLIENTEBLOQUEADO' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_ClienteValido",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'OPERADORINVALIDO' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_OperadorValido",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'MONEDASDISTINTAS' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_MonedasDistintas",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'AGENTEINVALIDO' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_AgenteValido"

FROM (

	SELECT "IdCondicionOperativa","IdTipoEtapa","IdEmpresa","IdTipoCanalIngreso","IdTipoComportamiento","IdEstado"
	FROM "BSCL.Plataforma.BD.Vistas::REQ.ConsultarRequisitoOperativo"	
) 

GROUP BY "IdTipoEtapa","IdEmpresa","IdTipoCanalIngreso";

GRANT SELECT ON "BSCL.Mesa.BD.Vistas::SPT.ConsultarRequisitosOperativosSpot" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
