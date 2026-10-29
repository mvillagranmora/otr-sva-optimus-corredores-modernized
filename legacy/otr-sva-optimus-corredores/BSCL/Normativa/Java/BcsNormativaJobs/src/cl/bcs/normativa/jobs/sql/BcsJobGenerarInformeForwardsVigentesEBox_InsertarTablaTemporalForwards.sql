INSERT INTO "BSCL.Normativa.BD::GBL.ForwardsVigentesEBox_TablaPaso"
("SecuenciaSolicitud", "FechaOperacion", "NumeroContrato", "NumeroContratoCalce", "RutCliente", 
"NombreCliente", "ClienteRelacionado", "TipoOperacion", "FechaVencimiento", "ModalidadLiquidacion", 
"MonedaTasa", "ValorNominal", "TipoCambioPactado", "MontoPactado", "TipoCambioMercado", "ValorizacionMercado", "ResultadoOperacion")
SELECT 
:SecuenciaSolicitud
,FWD."FechaOperacion"
,FWD."NumeroContrato"
,FWD."NumeroContratoCalce"
,FWD."RutCliente"
,SUBSTRING(FWD."NombreCliente", 1, 50)
,FWD."ClienteRelacionado"
,FWD."TipoOperacion"
,FWD."FechaVencimiento"
,FWD."ModalidadLiquidacion"
,FWD."MonedaTasa"
,FWD."ValorNominal"
,FWD."TipoCambioPactado"
,FWD."MontoPactado"
,COALESCE(PARUSD."TipoCambioMercado", 0)
,COALESCE(FWD."ValorNominal", 0) * COALESCE(PARUSD."TipoCambioMercado", 0) AS "ValorizacionMercado"
,FWD."ResultadoOperacion"
FROM "BSCL.Mesa.BD.Vistas::FWD.ConsultarOperacionesForwardsVigentesEBox" FWD
LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PARUSD
	ON PARUSD."IdMoneda" = 'DO'
	AND PARUSD."Fecha" = :FechaHasta
WHERE FWD."FechaOperacion" <= :FechaHasta
AND FWD."IdEmpresa" = :IdEmpresa;