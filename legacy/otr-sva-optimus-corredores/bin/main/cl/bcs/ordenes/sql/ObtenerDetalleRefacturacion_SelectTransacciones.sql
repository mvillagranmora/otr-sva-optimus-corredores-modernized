SELECT tran."IdTransaccion",
CASE tran."EsFungible" WHEN 'S' THEN "ValorRescate" ELSE "Cantidad" END AS "CantidadTransaccion",
tran."Monto" AS "MontoTransaccion",
tran."IndicadorOrdenDirecta",
tran."FechaSistemaCreacion",
tran."HoraSistemaCreacion",
tran."FechaOperacion" AS "FechaOperacionTransaccion",
tran."Folio" AS "FolioTransaccion",
tran."Precio" AS "PrecioTransaccion",
CASE tran."IdTipoInstrumento" WHEN 'SIM' THEN ins2."Nemotecnico" ELSE ins1."Nemotecnico" END AS "InstrumentoUI",
tran."IdInstrumento",
tipoope."IdTipoProducto",
tran."IdTipoOperacion",
tipoope."Nombre" AS "NombreTipoOperacion",
tran."IdCondicionDeLiquidacion",
condliq."Nombre" AS "NombreCondicionDeLiquidacion",
tran."IdBolsa",
tran."IdMoneda" AS "IdMonedaTransaccion",
tran."FechaVencimiento",
tran."IdEmisor",
tran."Tasa",
tran."IdTipoReajuste",
tran."NemotecnicoSVS",
tran."EsFungible",
tran."IdClasificacionTipoInstrumento",
"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(tran."EsFungible") as "EsFungibleUI",
tran."EsVentaCorta",
"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(tran."EsVentaCorta") as "EsVentaCortaUI",
tipoins."Nombre" AS "NombreTipoInstrumento",
cla."Nombre" AS "NombreClasificacionTipoInstrumento",
cor."NombreCorto" as "NombreCorredorContraparte",
tran."IdTipoInstrumento",

tipoope."AccionOrden"


,tran."Reservado"
,tran."IdUsuarioReserva"

,CASE
	WHEN tran."IdUsuarioReserva" = 0 THEN ''
	ELSE USU."CodigoUsuario"
END AS "CodigoUsuarioReserva"

,tran."IdGrupoReserva"


,COMDET."IdComprobante"
,CASE
	WHEN tran."IdInstrumentoSubyacente" = 0 THEN ins1."IdMoneda"
	ELSE ins2."IdMoneda"
END AS "IdMonedaInstrumento"
,ins3."Nemotecnico" AS "InstrumentoNegociacionTran"
FROM "BSCL.Orden.BD::ORD.Transaccion" tran
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" ins1 ON tran."IdInstrumento" = ins1."IdInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" ins2 ON tran."IdInstrumentoSubyacente" = ins2."IdInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" ins3 ON tran."IdInstrumentoNegociacion" = ins3."IdInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" tipoope ON tipoope."IdTipoOperacion" = tran."IdTipoOperacion"
LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" condliq ON condliq."IdCondicionDeLiquidacion" = tran."IdCondicionDeLiquidacion"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" tipoins ON tipoins."IdTipoInstrumento" = tran."IdTipoInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" cla ON cla."IdClasificacionTipoInstrumento" = tipoins."IdClasificacionTipoInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.Corredora" AS cor on cor."IdCorredora" = tran."IdCorredorContraparte"


LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS USU ON (USU."IdUsuario" = tran."IdUsuarioReserva")


LEFT JOIN (
			SELECT MAX(COM."IdComprobante") AS "IdComprobante", DET."IdTransaccion"
			FROM "BSCL.Facturacion.BD::FAC.Comprobante" AS COM
			INNER JOIN "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" AS DET ON (DET."IdComprobante" = COM."IdComprobante")
			WHERE DET."IdEstadoComprobanteDetalle" <> 'ANU'
			AND COM."IdEstadoComprobante" <> 'ANU'
			AND COM."IdTipoComprobante" NOT IN ('NCE','NEO','NCR')
			GROUP BY DET."IdTransaccion"
		  ) AS COMDET ON (tran."IdTransaccion" = COMDET."IdTransaccion")
WHERE tran."IdTransaccion" IN (:idsTransaccion)
ORDER BY tran."IdTransaccion";