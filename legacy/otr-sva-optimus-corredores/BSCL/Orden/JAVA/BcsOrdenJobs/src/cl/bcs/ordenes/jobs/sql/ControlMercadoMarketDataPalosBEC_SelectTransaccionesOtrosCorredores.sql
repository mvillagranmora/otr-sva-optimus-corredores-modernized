SELECT
    cm."TipoMensaje" as "_TipoMensaje",
    cm."IdMensaje1" as "_IdMensaje1",
    cm."IdMensaje2" as "_IdMensaje2",
	tx."BCSCodeComprador",
	tx."BCSCodeVendedor",
	tx."Folio",
	tx."FechaRegistro",
	buyer."CodigoCorredora",
	seller."CodigoCorredora"
FROM
    "BSCL.Orden.BD::ORD.ControlMercado" cm
LEFT JOIN
    "BSCL.Orden.BD::BEC.Palos" tx ON cm."IdMensaje1" = tx."FechaRegistro" AND cm."IdMensaje2" = tx."Folio"
LEFT JOIN
    "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraEmpresa" buyer ON buyer."CodigoCorredora" = tx."BCSCodeComprador" AND buyer."IdBolsa" = 'BCS'
LEFT JOIN
    "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraEmpresa" seller ON seller."CodigoCorredora" = tx."BCSCodeVendedor" AND seller."IdBolsa" = 'BCS'
WHERE
    cm."Procesado" = 'N'
    AND cm."TipoMensaje" = 'MARKET_DATA_PALO_BEC'
  	AND (buyer."CodigoCorredora" IS NULL AND seller."CodigoCorredora" IS NULL)
