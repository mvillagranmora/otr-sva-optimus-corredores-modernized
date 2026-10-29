SELECT	"IdCondicionOperativa","IdTipoEtapa","IdTipoCanalIngreso","IdTipoComportamiento","IdEstado",
    COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'LIQFECHADIS' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_FechaDif",
    COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'LIQSALDONEG' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_FlujoCajaNeg"
FROM (

    SELECT "IdCondicionOperativa","IdTipoEtapa","IdTipoCanalIngreso","IdTipoComportamiento","IdEstado"
    FROM "BSCL.Plataforma.BD.Vistas::REQ.ConsultarRequisitoOperativo"
    WHERE "IdTipoEtapa" = 'LIQUIDACOMPTES' and "IdEmpresa" = :IdEmpresa
) 
GROUP BY "IdCondicionOperativa","IdTipoEtapa","IdTipoCanalIngreso","IdTipoComportamiento","IdEstado";