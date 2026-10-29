SELECT tran."IdTransaccion",
CASE tran."EsFungible" WHEN 'S' THEN "ValorRescate" ELSE "Cantidad" END AS "CantidadTransaccion",
tran."Monto" AS "MontoTransaccion",
tran."FechaSistemaCreacion",
tran."HoraSistemaCreacion",
tran."FechaOperacion" AS "FechaOperacionTransaccion",
tran."Folio" AS "FolioTransaccion",
tran."Precio" AS "PrecioTransaccion",
CASE tran."IdTipoInstrumento" WHEN 'SIM' THEN ins2."Nemotecnico" ELSE ins1."Nemotecnico" END AS "InstrumentoUI",
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
"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(tran."EsFungible") as "EsFungibleUI",
tran."EsVentaCorta",
"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(tran."EsVentaCorta") as "EsVentaCortaUI",
tipoins."Nombre" AS "NombreTipoInstrumento",
cla."Nombre" AS "NombreClasificacionTipoInstrumento",
cor."NombreCorto" as "NombreCorredorContraparte",
tran."IdClasificacionTipoInstrumento"



,tran."Reservado"
,tran."IdUsuarioReserva"

,CASE
	WHEN tran."IdUsuarioReserva" = 0 THEN ''
	ELSE USU."CodigoUsuario"
END AS "CodigoUsuarioReserva"

,tran."IdGrupoReserva"


FROM "BSCL.Orden.BD::ORD.Transaccion" tran
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" ins1 ON tran."IdInstrumento" = ins1."IdInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" ins2 ON tran."IdInstrumentoSubyacente" = ins2."IdInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" tipoope ON tipoope."IdTipoOperacion" = tran."IdTipoOperacion"
LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" condliq ON condliq."IdCondicionDeLiquidacion" = tran."IdCondicionDeLiquidacion"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" tipoins ON tipoins."IdTipoInstrumento" = tran."IdTipoInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" cla ON cla."IdClasificacionTipoInstrumento" = tipoins."IdClasificacionTipoInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.Corredora" AS cor on cor."IdCorredora" = tran."IdCorredorContraparte"


LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS USU ON (USU."IdUsuario" = tran."IdUsuarioReserva")


WHERE tran."IdTransaccion" IN (:idsTransaccion)
ORDER BY tran."IdTransaccion";