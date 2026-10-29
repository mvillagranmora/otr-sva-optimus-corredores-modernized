SELECT
	CDET."Cantidad",
	COMP."IdCliente",
	COMP."IdPortafolio",
	CUS."Nemotecnico",
	COMP."IdComprobante" AS "FolioComprobante",
    CUS."CantidadCustodia" AS "INGCantidad", 
    'CARTERA' as "TipoCartera",
    CDET."IdFormaCustodia" AS "IdFormaCustodiaDetalleComprobante",
    CDET."IdClasificacionTipoInstrumento",
    TRANS."IdEstado" AS "IdEstadoMovimientoTransito",
    TRANS."Cantidad" AS "CantidadMovimientoTransito"
FROM "BSCL.Facturacion.BD::FAC.Comprobante" COMP
INNER JOIN "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" CDET
	ON CDET."IdComprobante"  = COMP."IdComprobante" 
INNER JOIN (SELECT "IdCliente", "IdPortafolio","Nemotecnico", SUM("Cantidad") AS "CantidadCustodia" FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraDisponible" GROUP BY "IdCliente","IdPortafolio","Nemotecnico") CUS
	ON COMP."IdCliente" = CUS."IdCliente" 
	AND COMP."IdPortafolio" = CUS."IdPortafolio" 
	AND CDET."NemotecnicoInstrumento" = CUS."Nemotecnico" 
LEFT  JOIN  "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" TRANS
	ON COMP."IdComprobante" = TRANS."FolioComprobante"
	AND CDET."IdComprobante"  = TRANS."FolioComprobante"
	AND CUS."IdCliente" = TRANS."IdCliente"
	AND CUS."Nemotecnico" = TRANS."Nemotecnico"
INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" TIPOP ON TIPOP."IdTipoOperacion" = CDET."IdTipoOperacion" 
WHERE-- "IdModulo" = 'FAC' 
	--AND "IdEstado" != 'RPA'
	 COMP."IdComprobante" IN (:idsComprobante)
	AND TIPOP."AccionOperacion" != 'VENTA';