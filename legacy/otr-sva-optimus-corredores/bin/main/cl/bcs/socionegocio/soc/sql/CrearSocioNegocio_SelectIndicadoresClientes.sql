SELECT
"IdIndicador"
,"IdTipoIndicador"
,"ValorIndicador"
,"CodigoTipoIndicador"
FROM
"BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"
WHERE
"IdCliente" = :IdCliente
AND "CodigoTipoIndicador" IN (:CodigoTipoIndicador)