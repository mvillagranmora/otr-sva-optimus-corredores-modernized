SELECT IFNULL(indicador."ValorIndicador", 'N') AS "TieneContrato"
	, indicador."IdCliente"
FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" indicador
WHERE indicador."IdCliente" IN (:p_IdCliente) AND indicador."CodigoTipoIndicador" = 'CC';