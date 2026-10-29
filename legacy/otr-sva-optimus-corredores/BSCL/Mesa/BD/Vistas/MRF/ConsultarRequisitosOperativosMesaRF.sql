SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Mesa.BD.Vistas::MRF.ConsultarRequisitosOperativosMesaRF";
 
CREATE VIEW "BSCL.Mesa.BD.Vistas::MRF.ConsultarRequisitosOperativosMesaRF" AS
	SELECT	"IdTipoEtapa"
			,"IdEmpresa"
			,"IdTipoCanalIngreso"
			,"IdCondicionOperativa"
			,CASE 
				WHEN "IdCondicionOperativa" = 'CONTCUSTODIA' THEN 'CC' 
				WHEN "IdCondicionOperativa" = 'CONTCONDGRAPACTO' THEN 'CCGP'
				ELSE ''
			 END "CodigoTipoIndicador"-- solo para el enganche con los indicadores del cliente segun corresponda
			,"IdTipoComportamiento"
			,"IdEstado" 
			
	FROM "BSCL.Plataforma.BD.Vistas::REQ.ConsultarRequisitoOperativo"
	WHERE "IdTipoEtapa" in ('INGRESOOPMESARF') and "IdEstado" = 'ACT'
	GROUP BY "IdTipoEtapa","IdEmpresa","IdTipoCanalIngreso","IdCondicionOperativa","IdTipoComportamiento","IdEstado";
/*
select "IdTipoEtapa","IdEmpresa","IdTipoCanalIngreso"
	,COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'CONTCUSTODIA' THEN "IdTipoComportamiento" END),'RECHAZO') AS "AccionContratoCustodia"
	,COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'CONTCONDGRAPACTO' THEN "IdTipoComportamiento" END),'RECHAZO') AS "AccionContratoPacto"
from (
	SELECT	"IdTipoEtapa","IdEmpresa","IdTipoCanalIngreso","IdCondicionOperativa"
			,"IdTipoComportamiento","IdEstado" 
			
	FROM "BSCL.Plataforma.BD.Vistas::REQ.ConsultarRequisitoOperativo"
	WHERE "IdTipoEtapa" = 'INGRESOOPMESARF' and "IdEstado" = 'ACT'
	GROUP BY "IdTipoEtapa","IdEmpresa","IdTipoCanalIngreso","IdCondicionOperativa","IdTipoComportamiento","IdEstado"
)
GROUP BY "IdTipoEtapa","IdEmpresa","IdTipoCanalIngreso";*/
GRANT SELECT ON "BSCL.Mesa.BD.Vistas::MRF.ConsultarRequisitosOperativosMesaRF" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
