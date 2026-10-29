
 SELECT
	mov."IdMovimientoComprobante",
	mov."IdTransaccion",
	mov."IdCliente",
	mov."IdPortafolio",
	mov."DocumentoIdentidad",
	mov."IdMoneda",
	tipoope."AccionOrden",
	COALESCE(ind."ValorIndicador",'N') AS "UsaCuentaInversion",
	tran."Folio",
	tran."FechaSistemaCreacion",
	tran."IdClasificacionTipoInstrumento",
	tran."Nemotecnico",
	mov."FechaLiquidacion",
	mov."IdPersona",
	tran."MontoRetencion",
	tran."Monto",
	tran."Cantidad",
	mov."NombreMoneda" AS "DescripcionMoneda",
	mov."NombreLargoCliente" AS "NombreCliente",
	CLI."IdDireccionElectronicaPrincipal" AS "IdDireccionPrincipal"
FROM "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante" mov
INNER JOIN "BSCL.Orden.BD::ORD.Transaccion" tran on tran."IdTransaccion" = mov."IdTransaccion"
INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" tipoope on tipoope."IdTipoOperacion" = mov."IdTipoOperacion"
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" ind on mov."IdPortafolio" = ind."IdPortafolio" AND "CodigoTipoIndicador" = 'GEN_OCCINV'
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON CLI."IdCliente" = mov."IdCliente"
WHERE tran."Folio" in (:foliosTransaccion)

