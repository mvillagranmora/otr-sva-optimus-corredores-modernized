
SELECT	"IdTipoEtapa","IdEmpresa","IdTipoCanalIngreso",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'CLIENTEBLOQUEADO' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_ClienteBloqueado",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'OPERACIONSINFLUJOCAJA' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_OperacionSinFlujoCaja"
	FROM (
	
		SELECT "IdCondicionOperativa","IdTipoEtapa","IdEmpresa","IdTipoCanalIngreso","IdTipoComportamiento","IdEstado"
		FROM "BSCL.Plataforma.BD.Vistas::REQ.ConsultarRequisitoOperativo"
		WHERE "IdTipoEtapa" = 'SUSCRIPCIONEMISIONPAGO' and "IdEmpresa" = :IdEmpresa
	) 
	GROUP BY "IdTipoEtapa","IdEmpresa","IdTipoCanalIngreso";