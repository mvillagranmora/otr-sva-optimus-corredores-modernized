SELECT
	"TRA"."IdBolsa"
	,"MOV"."IdCargoAbonoMovimiento" AS "IdMovimiento"
	,"MOV"."NombreCliente" AS "Cliente"
	,"MOV"."IdPortafolio"
	,"MOV"."FechaOperacion" AS "FechaMovimiento"
	,"MOV"."TipoMovimiento"
	,"MOV"."NombreTipoMovimiento" AS "DescripcionMovimiento"
	,"MOV"."NombreTipoMovimiento"
	,CAST(:v_CodigoGrupoCamara AS BIGINT) AS "CodigoGrupoCamara"
	,:v_NombreGrupoCamara AS "NombreGrupoCamara"
	,"TRA"."IdClasificacionTipoInstrumento"
	,"TRA"."IdCorredorContraparte"
	,"CON"."DocumentoIdentidad" AS "RutContraparte"
	,"CON"."NombreCorto" AS "RazonSocialContraparte"
	,"MOV"."IdMoneda" AS "IdMonedaTransaccion"
	,"MOV"."IdComprobanteTesoreria"
	,(CASE WHEN "MOV"."TipoMovimiento" = 'CA'  THEN "MOV"."Monto" ELSE 0 END) AS "TotalCompra"
	,(CASE WHEN "TRA"."AccionTipoOperacion" = 'COMPRA'  THEN "MOV"."Monto" ELSE 0 END) AS "Cargo"
	,(CASE WHEN "TRA"."AccionTipoOperacion" = 'COMPRA'  THEN "MOV"."Monto" ELSE 0 END) AS "SaldoDeudor"
	,(CASE WHEN "MOV"."TipoMovimiento" = 'AB'  THEN "MOV"."Monto" ELSE 0 END) AS "TotalVenta"
	,(CASE WHEN "TRA"."AccionTipoOperacion" = 'VENTA'  THEN "MOV"."Monto" ELSE 0 END) AS "Abono"
	,(CASE WHEN "TRA"."AccionTipoOperacion" = 'VENTA'  THEN "MOV"."Monto" ELSE 0 END) AS "SaldoAcreedor"
	,"MOV"."Monto"
	,"TRA"."MontoRetencion"
	,"TRA"."AccionTipoOperacion"
	,"MOV"."TipoMovimiento"
	,"MOV"."IdEstado"
	,"TRA"."MontoTransaccion"
	,"TRA"."MontoRetencion"  -- PERU: Monto retencion cavali(informado por archivo IGK)
FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarCartolaDeMovimientos" AS "MOV"
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" AS "CAR" ON "CAR"."IdCartera" = "MOV"."IdOperacion"
LEFT JOIN "BSCL.Orden.BD.Vistas::ConsultarTransaccion" AS "TRA" ON "CAR"."FolioExternoReferencia" = "TRA"."FolioTransaccion"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora" AS "CON" ON "CON"."IdCorredora" = "TRA"."IdCorredorContraparte"
WHERE 
	"MOV"."FechaOperacion" = :p_FechaLiquidacion 
	AND ("TRA"."IdBolsa" <> '' OR "TRA"."IdBolsa" <> '') 
	AND "MOV"."IdEstado" <> 'ANU' 
	AND MOV."IdConcepto" IN ('CALFVE','CALFDI','ABLFVE','ABLFDI','ABEJ','CAEJ','ABSO','CASO')
	AND "TRA"."IdClasificacionTipoInstrumento" = 'DV'
	AND "TRA"."AccionTipoOperacion" <> ''
