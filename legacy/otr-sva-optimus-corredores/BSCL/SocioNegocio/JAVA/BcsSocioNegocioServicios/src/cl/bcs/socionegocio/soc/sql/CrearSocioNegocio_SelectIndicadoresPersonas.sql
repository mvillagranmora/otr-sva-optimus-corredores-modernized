SELECT
"IdIndicador"
,"IdTipoIndicador"
,"ValorIndicador"
,"CodigoTipoIndicador"
FROM
"BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPersona"
WHERE
"IdPersona" = :IdPersona
AND "CodigoTipoIndicador" IN (:CodigoTipoIndicador)