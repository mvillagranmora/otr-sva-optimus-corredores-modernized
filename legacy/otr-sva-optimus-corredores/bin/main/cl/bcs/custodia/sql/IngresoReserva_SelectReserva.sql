SELECT RES."IdReserva","IdOperacion","FolioCarteraOrigen","FolioCarteraCustodia","IdCarteraOrigen","IdCarteraCustodia","FolioOperacion"
    ,"IdEstado","IdUsuarioCreacion","IdTipoProducto","IdConcepto","Tasa","Precio","MontoValorizado"
    ,"IdClasificacionTipoInstrumento","IdInstrumento","IdTipoInstrumento","IdTipoReajuste","IdMonedaEmisionInstrumento","IdMonedaLiquidacionInstrumento","IdEmisor"
    ,CAST ("FechaVencimiento" AS  DATE) AS "FechaVencimiento","EsFungible","IdBolsa","IdBoveda","CodigoIFRS","CorteMinimo","CantidadInicial"
    ,"ValorRescateInicial","MontoValorizadoReserva",CAST ("FechaVencimientoReserva" AS DATE) AS "FechaVencimientoReserva",CAST ("FechaVencimientoProducto" AS DATE) AS "FechaVencimientoProducto"
    ,CAST ("FechaCreacion" AS DATE) AS "FechaCreacion","IngIdUsuario","IngCantidad","IngValorRescate","IngMontoValorizado",CAST ("IngFecha" AS DATE) AS "IngFecha"
    ,"EgrIdUsuario","EgrCantidad","EgrValorRescate","EgrMontoValorizado",CAST ("EgrFecha" AS DATE) AS "EgrFecha","SecuenciaCompromisoMigracion"
    ,"IdCarga","PrecioMercado","TasaMercado" 
FROM "BSCL.Custodia.BD::CUS.Reserva" AS RES
INNER JOIN "BSCL.Custodia.BD::GBL.IngresoReservaFoliosConsultaPaso" AS TMP ON ((:TipoConsulta = 'ReservaId'    AND (TMP."IdReserva" = RES."IdReserva")) OR
                                                                 (:TipoConsulta = 'ReservaFolio' AND (TMP."FolioCartera" = RES."FolioCarteraOrigen")))
AND "IdEstado" = 'VIG'
AND "IdTrabajo" = :IdTrabajo
ORDER BY RES."IdReserva" ASC;
