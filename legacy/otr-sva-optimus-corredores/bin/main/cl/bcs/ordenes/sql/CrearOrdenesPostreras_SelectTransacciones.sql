/********************************************************** 
Proposito	: lectura de transacciones en orden postrera
autor		: RU
fecha		: 01/02/2017
***********************************************************/

SELECT	"IdTransaccion","Folio",tran."FechaEmision","FechaOperacion", tran."IdClasificacionTipoInstrumento",tran."Precio","EsFungible","Tamano","Monto",tran."Cantidad",
		"ValorRescate",tran."IdTipoReajuste",tran."IdEmisor",tran."IdBolsa",tran."IdMoneda",tran."IdInstrumento","IdInstrumentoSubyacente",
		tran."IdTipoOperacion","IdCondicionDeLiquidacion",tran."Tasa","Plazo",tran."EsVentaCorta","EsFungible",tran."IdTipoInstrumento",
		"IdCorredorCorresponsal","AccionOrden",tran."IdLibroOrdenes",libro."IdTipoLibroOrden",tran."BaseTasa",tran."TipoInteres",tran."NemotecnicoSVS",tran."Nemotecnico",
		itr."CorteMinimo", tran."FechaIngresoOferta", tran."FechaVencimiento", tran."IndicadorOrdenDirecta"
FROM "BSCL.Orden.BD::ORD.Transaccion" tran
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst ON  inst."IdInstrumento" = tran."IdInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS tipo on tran."IdTipoOperacion" = tipo."IdTipoOperacion"
LEFT JOIN "BSCL.Orden.BD::ORD.LibroOrdenes" libro on libro."IdLibroOrdenes" = tran."IdLibroOrdenes"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoTipoReajuste" itr on itr."IdInstrumento" = tran."IdInstrumento" and itr."IdTipoInstrumento" = tran."IdTipoInstrumento" and itr."IdTipoReajuste" = tran."IdTipoReajuste"
WHERE "IdTransaccion" in (:idsTransaccion)
;
