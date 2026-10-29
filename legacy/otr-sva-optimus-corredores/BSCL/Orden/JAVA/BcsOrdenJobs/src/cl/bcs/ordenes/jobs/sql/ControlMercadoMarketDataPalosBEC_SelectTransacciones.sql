SELECT
    "BSCL.Orden.BD.Secuencias::MarketData_IdInterno".NEXTVAL AS "IdInterno",
    cm."TipoMensaje" as "_TipoMensaje",
    cm."IdMensaje1" as "_IdMensaje1",
    cm."IdMensaje2" as "_IdMensaje2",
    cm."FechaIngresoRegistro",
    cm."FechaProcesamiento",
    cm."Procesado",
    cm."Log",
    tx."ID",
    tx."FechaRegistro",
    tx."Fecha",
    tx."Folio",
    tx."FolioXStream",
    tx."Hora",
    tx."Accion",
    tx."Instrumento",
    tx."Moneda",
    tx."Mercado",
    tx."Grupo",
    tx."LugarPago",
    tx."Precio",
    tx."Cantidad",
    tx."Monto",
    tx."ValorPar",
    tx."Plazo",
    tx."FechaLiquidacion",
    tx."Board",
    tx."CompradorIdCorredor",
    tx."VendedorIdCorredor",
    tx."CompradorXStreamIdCorredor",
    tx."VendedorXStreamIdCorredor",
    tx."CompradorBCSTraderID",
    tx."VendedorBCSTraderID",
    IFNULL(tx."CompradorTraderID", '') AS "CompradorTraderID",
    IFNULL(tx."VendedorTraderID", '') AS "VendedorTraderID",
    IFNULL(tx."CompradorIdOpdir", '') AS "CompradorIdOpdir",
    IFNULL(tx."CompradorTraderIdOpdir", '') AS "CompradorTraderIdOpdir",
    IFNULL(tx."VendedorIdOpdir", '') AS "VendedorIdOpdir",
    IFNULL(tx."VendedorTraderIdOpdir", '') AS "VendedorTraderIdOpdir",
    IFNULL(tx."CompradorXStreamIdOpdir", '') AS "CompradorXStreamIdOpdir",
    IFNULL(tx."VendedorXStreamIdOpdir", '') AS "VendedorXStreamIdOpdir",
    IFNULL(tx."fondoCompra", '') AS "fondoCompra",
    IFNULL(tx."fondoVenta", '') AS "fondoVenta",
    IFNULL(tx."ClientIDCompra", '') AS "ClientIDCompra",
    IFNULL(tx."ClientIDVenta", '') AS "ClientIDVenta",
    tx."Variacion",
    tx."FolioRelacionado",
    tx."Anticipable",
    tx."madurez",
    tx."ventaCorta",
    tx."PlazoAAMM",
    tx."PlazoDias",
    tx."Generico",
    tx."TipoInstrumentoRF",
    tx."Reajustabilidad",
    IFNULL(tx."BCSCodeComprador", '') AS "BCSCodeComprador",
    IFNULL(tx."BCSCodeVendedor", '') AS "BCSCodeVendedor",
    IFNULL(tx."BCSCodeCompradorOpdir", '') AS "BCSCodeCompradorOpdir",
    IFNULL(tx."BCSCodeVendedorOpdir", '') AS "BCSCodeVendedorOpdir",
    tx."puntaCompraBolsa",
    tx."puntaCompraPrecio",
    tx."puntaCompraCantidad",
    tx."puntaVentaBolsa",
    tx."puntaVentaPrecio",
    tx."puntaVentaCantidad",
    tx."offshore",
    tx."EmisorID",
    tx."EmisorNombre",
    tx."odAutomatica"
--    buyer."CodigoCorredoraFix",
--    seller."CodigoCorredoraFix"
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
  	AND (buyer."CodigoCorredora" IS NOT NULL OR seller."CodigoCorredora" IS NOT NULL)
  	
ORDER BY
    tx."FechaRegistro",
    tx."Accion";
