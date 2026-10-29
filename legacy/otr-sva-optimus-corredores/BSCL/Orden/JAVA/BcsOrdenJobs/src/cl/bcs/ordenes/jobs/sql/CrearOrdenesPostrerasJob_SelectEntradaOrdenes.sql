SELECT 
ORD."IdInterno",
ORD."FechaRecepcion",
ORD."Glosa",
ORD."IdBoveda",
ORD."IdFormaCustodia",
ORD."EsFacturaInmediata",
ORD."IdTransaccion",
ORD."IdPortafolio",
ORD."Cantidad",
ORD."ValorRescate",
ORD."IdGrupo",
ORD."FolioCartera",
ORD."TasaVentaCorta",
ORD."PlazoVentaCorta",
ORD."TipoCambio",
ORD."IdMonedaLiquidacionOrden",
ORD."PagoCobroCuentaInversion",
pref."IdPreferenciaPagoPortafolio",
ORD."IdCuentaCustodioPortafolio",
ORD."CodigoIFRS",
ORD."PorcentajeAllIn",
ORD."FlujoOrigen",
ORD."FechaTrade",
ORD."IngresoDesde",
TRAN."Folio" AS "FolioTransaccion",
POR."CodigoPortafolio",
COALESCE(CC."DocumentoIdentidad", 'No Encontrado') AS "DocumentoIdentidad",
COALESCE(CC."NombreCompleto", 'No Encontrado') AS "NombreCompleto",
TRAN."Nemotecnico",
CAST(ORD."Cantidad" * TRAN."Precio" AS DECIMAL(26,8)) AS "Monto",
TRAN."IdTipoOperacion"
FROM "BSCL.Orden.BD::ORD.ProcesarOrdenesPostreras_Entrada_Ordenes" ORD 
INNER JOIN BSCL."BSCL.Orden.BD::ORD.Transaccion" TRAN  
	ON ORD."IdTransaccion" = TRAN."IdTransaccion"
LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaPagoPortafolio" pref 
	ON TRAN."IdTipoOperacion" = pref."IdTipoOperacion"
	AND ORD."IdMonedaLiquidacionOrden" = pref."IdMoneda"
	AND ORD."IdPortafolio" = pref."IdPortafolio"
	AND pref."PorDefecto" = 'S'
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdPortafolio" = ORD."IdPortafolio" 
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CC ON CC."IdCliente" = POR."IdCliente"
WHERE "IdTrabajo" = :IdTrabajo;
