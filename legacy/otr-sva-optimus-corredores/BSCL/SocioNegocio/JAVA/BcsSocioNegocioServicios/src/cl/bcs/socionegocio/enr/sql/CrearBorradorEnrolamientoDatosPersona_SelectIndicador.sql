SELECT 
"IdIndicador"
,"IdEntidad" AS "IdPersona"
,"IdTipoEntidad"
,"IdTipoIndicador"
,"ModeloCliente"
,"IdEmpresa"
,"ValorIndicador"
FROM "BSCL.Plataforma.BD::IND.Indicador"
WHERE "IdEntidad" IN (:IdPersona) AND "IdTipoEntidad" = 'PERSONA';