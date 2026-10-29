SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarRequisitosOperativosCuentaInversion";
 
CREATE VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarRequisitosOperativosCuentaInversion" AS
SELECT	"IdTipoEtapa","IdEmpresa","IdTipoCanalIngreso",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'CONTMERCANTIL' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "RQ_ContratoMercantil"
FROM (

	SELECT "IdCondicionOperativa","IdTipoEtapa","IdEmpresa","IdTipoCanalIngreso","IdTipoComportamiento","IdEstado"
	FROM "BSCL.Plataforma.BD.Vistas::REQ.ConsultarRequisitoOperativo"
	WHERE "IdTipoEtapa" = 'INGRESOCARABO'	
)
GROUP BY "IdTipoEtapa","IdEmpresa","IdTipoCanalIngreso";

GRANT SELECT ON "BSCL.CuentaInversion.BD.Vistas::ConsultarRequisitosOperativosCuentaInversion" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
