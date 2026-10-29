
SELECT "BSCL.Orden.BD.Secuencias::MarketData_IdInterno".NEXTVAL as "IdInterno",cm."TipoMensaje" AS "_TipoMensaje",
cm."IdMensaje1",cm."IdMensaje2",tran."IdTransaccion",tran."Folio" as "FolioTransaccion",tran."Cantidad",tran."ValorRescate",
tran."IdClasificacionTipoInstrumento",tran."IdTipoInstrumento",tran."CodigoOperadorCorredor",tran."EsVentaCorta",tran."IdMoneda" AS "IdMonedaTransaccion",
tran."EsAPlazo", tran."IdTipoOperacion", tran."MarcaNegociacion", sub."IdTipoInstrumento" AS "IdTipoInstrumentoSubyacente"

FROM "BSCL.Orden.BD::ORD.ControlMercado" cm 
LEFT JOIN "BSCL.Orden.BD::ORD.Transaccion" tran on cm."IdMensaje1" = tran."FechaSistemaCreacion" and cm."IdMensaje2" = tran."IdTransaccion"
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" sub on tran."IdInstrumentoSubyacente" = sub."IdInstrumento"
WHERE cm."Procesado" = 'N' and cm."TipoMensaje" in ('TRANSACCION_OPERADOR', 'ASIGNACION_DIRECTA', 'SHORTNAME', 'TRANSACCION_MARCA_NEGOCIACION')
ORDER BY cm."FechaIngresoRegistro" asc;



