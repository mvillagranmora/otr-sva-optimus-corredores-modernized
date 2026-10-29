SELECT	"IdTipoEtapa","IdEmpresa","IdTipoCanalIngreso",
	COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'CUADRATURAFACTURACION' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "AP_CuadraturaFacturacion"
	FROM (
		SELECT "IdCondicionOperativa","IdTipoEtapa","IdEmpresa","IdTipoCanalIngreso","IdTipoComportamiento","IdEstado"
		FROM "BSCL.Plataforma.BD.Vistas::REQ.ConsultarRequisitoOperativo"
		WHERE "IdTipoEtapa" = 'APROBARDESCUADRATURA' and "IdEmpresa" = :IdEmpresa
	) 
	GROUP BY "IdTipoEtapa","IdEmpresa","IdTipoCanalIngreso";
