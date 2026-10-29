SELECT
CAR."IdCliente"
,CAR."IdPortafolio"
,CASE 	WHEN TOPER."Nombre" = 'VENTA CON PACTO'  THEN 'COMPRA CON PACTO'
		WHEN TOPER."Nombre" = 'COMPRA CON PACTO' THEN 'VENTA CON PACTO'
	END AS "operacion"
,CAR."IdTipoReajuste" AS "moneda"
,TO_DECIMAL(CAR."IdOperacion",26,0) AS "SEC_COMPROM"
,TO_DECIMAL(CAR."PlazoEnDiasOperacion",26,0) AS "plazo"
,CAR."TasaCompra" AS "TASA_COMPROM"
,CASE 	WHEN TOPER."Accion" = 'COMPRA'  THEN 'Venta Contado'
		WHEN TOPER."Accion" = 'VENTA' THEN 'Compra Contado'
	END AS "operacionContado"
,TO_TIMESTAMP(CAR."INGFechaOperacion") AS "fechaContado"
,CAR."MontoCompraMonedaInstrumento" AS "montoContratadoContado"
,CAR."MontoCompra" AS "equivClpContado"
,CASE 	WHEN (INS."NombreTipoInstrumento" = 'PACTOS' and TOPER."Accion" = 'COMPRA') THEN 'Compra Plazo'
		WHEN (INS."NombreTipoInstrumento" = 'PACTOS' and TOPER."Accion" = 'VENTA') THEN 'Venta Plazo'
	END AS "operacionPlazo"
,TO_TIMESTAMP(CAR."FechaVencimientoOperacion")AS "fechaPlazo"
,CAR."MontoAPlazoMonedaInstrumento" AS "montoContratadoPlazo"
,CAR."MontoAPlazo" AS "equivClpPlazo"
,CAR."INGMontoMercadoActualMonedaInstrumento" AS "VALOR_PTE"
,CAR."INGMontoATasaMercadoActual" AS "equivClpValPresen"
FROM "BSCL.Custodia.BD::HIS.Cartera" CAR
INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON CAR."IdPortafolio" = P."IdPortafolio"
JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" INS 
ON INS."IdTipoInstrumento" = CAR."IdTipoInstrumento" AND CAR."IdTipoInstrumento" = 'PAC'
AND INS."Nemotecnico" = CAR."Nemotecnico"
INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" TOPER ON TOPER."IdTipoOperacion" = CAR."IdTipoOperacion"
WHERE CAR."FechaRespaldo" = :FechaHasta
AND CAR."IdEstado" = 'VIG';