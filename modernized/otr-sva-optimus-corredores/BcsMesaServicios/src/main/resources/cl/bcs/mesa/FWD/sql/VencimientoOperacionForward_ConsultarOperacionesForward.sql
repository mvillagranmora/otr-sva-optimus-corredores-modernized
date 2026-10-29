SELECT
	 CAR."IdCartera"
	,CAR."IdOperacion"
	,CAR."FolioCartera"
	,CAR."IdTipoOperacion"
	,CAR."IdTipoInstrumento"
	,CAR."IdClasificacionTipoInstrumento"
	,CAR."IdTipoProducto"
	,CAR."IdInstrumento"
	,CAR."Nemotecnico"
	,CAR."IdCliente"
	,CAR."IdPortafolio"
	,CAR."IdAgente"
	,CAR."IdTipoCanalIngreso"
	,CAR."IdMonedaPrincipal"
	,CAR."IdMonedaSecundaria"
	,CAR."EGRIdTipoOperacion"
	,CAR."INGMontoMonedaPrincipal"
	,CAR."INGMontoMonedaSecundaria"
	,CAR."EGRMontoMonedaPrincipal"
	,CAR."EGRMontoMonedaSecundaria"
	,CAR."FechaOperacion"
	,CAR."ValorTipoCambioCierre"
	,CAR."ValorParidadCierre"
	,CASE WHEN CAR."PlazoEnDiasOperacion" > 0 THEN CAR."PlazoEnDiasOperacion"
		  WHEN CAR."FechaVencimientoOperacion" > TO_DATE('0001-01-01','YYYY-MM-DD') THEN DAYS_BETWEEN(CAR."FechaOperacion", CAR."FechaVencimientoOperacion")
		  ELSE 0 END AS "Plazo"
	,CAR."FechaVencimientoOperacion"
	,TO_DATE(TO_VARCHAR(OPE."FechaLiquidacion",'YYYY-MM-DD'),'YYYY-MM-DD') AS "FechaLiquidacion"
	,CAR."IdModalidadCumplimiento"
	,PAR."ValorParametro" AS "ModalidadCumplimientoValor"
	,CAR."IdPlaza"
	,CAR."ValorTipoCambioCosto"
	,CAR."INGEsPuntos"
	,CAR."INGTasaPuntosMonedaPrincipal"
	,CAR."TasaCompra"
	,CAR."IdFixing"
	,CAR."FechaFixing"
	,CAR."HoraFixing"
	,CAR."IdMesa"
	,CAR."EGRIdSpotAsociado"
	,CAR."Glosa"
	,CAR."IdEstado"
	,CAR."EGRMontoARecibirAnticipo"
	,COALESCE(PARDO."TipoCambioMercado",0) AS "TipoCambioMercadoDO"
	,COALESCE(PARUF."TipoCambioMercado",0) AS "TipoCambioMercadoUF"
	,TIPMON."MultiplicaODivide"
FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
INNER JOIN "BSCL.Mesa.BD::MES.Operacion" AS OPE
	ON (OPE."IdOperacion" = CAR."IdOperacion" AND
		OPE."IdTipoProducto" = CAR."IdTipoProducto")
LEFT JOIN "BSCL.Mesa.BD::MES.Parametro" AS PAR
	ON (PAR."IdParametro" = CAR."IdModalidadCumplimiento" AND
		PAR."NombreTipoParametro" = 'ModalidadLiquidacion' AND
		PAR."IdEmpresa" = CAR."IdEmpresa")
LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" AS PARDO
	ON (PARDO."IdMoneda" = 'DO' AND 
		TO_CHAR(PARDO."Fecha",'YYYY-MM-DD') = TO_CHAR(CAR."FechaVencimientoOperacion",'YYYY-MM-DD') AND 
		PARDO."IdEstado" = 'ACT')
LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" AS PARUF
	ON (PARUF."IdMoneda" = 'UF' AND 
		TO_CHAR(PARUF."Fecha",'YYYY-MM-DD') = TO_CHAR(CAR."FechaVencimientoOperacion",'YYYY-MM-DD') AND 
		PARUF."IdEstado" = 'ACT')
INNER JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MON
	ON (MON."IdMoneda" = CAR."IdMonedaPrincipal")
LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoMoneda" AS TIPMON
	ON (MON."IdTipoMoneda" = TIPMON."IdTipoMoneda")
WHERE CAR."IdCartera" IN (:IdCartera)
  AND CAR."IdTipoProducto" = 'MEFW'
  AND CAR."IdEstado" IN ('VIG','VCD')
  AND OPE."IdEmpresa" = :IdEmpresa;
