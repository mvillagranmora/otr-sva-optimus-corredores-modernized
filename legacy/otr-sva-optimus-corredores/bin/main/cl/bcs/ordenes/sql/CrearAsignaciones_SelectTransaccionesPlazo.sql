/********************************************************** 
Proposito	: consulta de transacciones para asignar
autor		: RU
fecha		: 01/02/2017
***********************************************************/
SELECT tran."IdTransaccion",tran."IdTipoOperacion",tran."IdInstrumento",tran."IdLibroOrdenes",
tranDisp."CantidadDisponible", tranDisp."MontoDisponible", tranDisp."ValorRescateDisponible",tran."IdCondicionDeLiquidacion",
tran."IdBolsa",tran."FechaOperacion",tran."Precio",tran."Tasa",tran."EsVentaCorta",tran."IdEstado",tran."EsFungible",
itr."CorteMinimo",
tran."Cantidad",tran."Monto",tran."ValorRescate", tran."IdInstrumentoSubyacente",tran."IdClasificacionTipoInstrumento",
inst."Tamano",tran."IdTransaccionPlazo",tran."EsAPlazo",tran."IdMoneda",tipoope."AccionOrden",
tran."IdTipoInstrumento","IdCorredorCorresponsal" as "IdCorredoraCorresponsal",tran."Bloqueado",tran."Reservado",tran."IdUsuarioReserva",tran."IdTransaccionOd", tran."Intereses",
tran."FechaVencimiento" AS "FechaVencimiento", tran."Folio" AS "Folio", tran."NemotecnicoSVS" AS "NemoSVS"

FROM "BSCL.Orden.BD::ORD.Transaccion" tran
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" inst on tran."IdInstrumento" = inst."IdInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" tipoope on tran."IdTipoOperacion" = tipoope."IdTipoOperacion"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoTipoReajuste" itr on itr."IdInstrumento" = tran."IdInstrumento" and itr."IdTipoInstrumento" = tran."IdTipoInstrumento" and itr."IdTipoReajuste" = tran."IdTipoReajuste"		
LEFT JOIN "BSCL.Orden.BD.Vistas::ConsultarDisponibleTransaccion" tranDisp on tran."IdTransaccion" = tranDisp."IdTransaccion"
WHERE tran."IdTransaccion" in (:idsTransaccion);