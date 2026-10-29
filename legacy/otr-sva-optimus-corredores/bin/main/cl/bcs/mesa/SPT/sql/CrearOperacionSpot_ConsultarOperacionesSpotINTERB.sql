SELECT
	 SPT."NumeroOperacion"
	,TO_BIGINT(0)							AS "IdOperacionSpot"
    ,TO_BIGINT(:IdEmpresa)					AS "IdEmpresa"
    ,SPT."IdCliente"
	,SPT."IdPortafolio"
	,SPT."IdInstrumento"
	,SPT."IdTipoOperacion"
	,SPT."IdTipoProducto"
	,SPT."FechaOperacion"  
	,TO_DATE('0001-01-01','YYYY-MM-DD')		AS "FechaLiquidacion"
	,SPT."IdMonedaPrincipal"    
	,SPT."IdMonedaSecundaria"      
	,:MonedaLocal							AS "IdMonedaLocal"
	,:MonedaEquivalente						AS "IdMonedaEquivalente"
	,SPT."MontoMonedaPrincipal"
	,TO_DECIMAL(0,26,8)						AS "MontoMonedaPrincipalEquivalente"
	,SPT."MontoMonedaSecundaria"
	,SPT."TipoCambioCierre"
	,SPT."TipoCambioCierreForward"
	,SPT."ParidadCierre"
	,SPT."ParidadCierreForward"
	,SPT."TipoCambioCosto"
	,SPT."ParidadCosto"
	,SPT."MontoEquivalente"
	,SPT."MontoRentabilidad"
	,SPT."MontoRentabilidadCanal"
	,TO_DECIMAL(0,26,8)						AS "ResultadoMonedaLocalPuntas"
	,TO_DECIMAL(0,26,8)						AS "ResultadoMonedaLocalBidAsk"
	,TO_DECIMAL(0,26,8)						AS "ResultadoMonedaLocalDO"
	,TO_DECIMAL(0,26,8)						AS "ResultadoMonedaLocalTCSpot"
	,TO_DECIMAL(0,26,8)						AS "ResultadoMonedaLocalTCCierreMesa"
	,TO_DECIMAL(0,26,8)						AS "ResultadoMonedaLocalConsolidado"
	,SPT."Margen"
	,TO_DECIMAL(0,26,8)						AS "MargenBidAsk"
	,SPT."Utilidad"
	,SPT."PuntaCompra"
	,SPT."PuntaVenta"
	,COALESCE(PAR."PrecioSpotCompra",0)		AS "PuntaBid"
	,COALESCE(PAR."PrecioSpotVenta",0)		AS "PuntaAsk"
	,COALESCE(PAR."ParidadBID",0)			AS "ParidadBid"    
	,COALESCE(PAR."ParidadASK",0)			AS "ParidadAsk" 
	,COALESCE(PARDO."TipoCambioMercado",0)	AS "ValorDolarObservado" 
	,COALESCE(PAR."TipoCambioMercado",0)	AS "ValorTCSpot" 
	,SPT."SolicitudTipoCambio"
	,TO_BIGINT(0)							AS "IdEvento"
	,SPT."IdMesa"
	,SPT."IdModulo"
	,SPT."IdTipoDolar"
	,SPT."TipoComprobanteFacturacion"
	,TO_BIGINT(0)							AS "IdComprobanteFacturacion"
	,TO_BIGINT(0)							AS "IdComprobanteTesoreriaIngreso"
	,TO_BIGINT(0)							AS "IdComprobanteTesoreriaEgreso"
	,SPT."IdAgente"
	,SPT."BenOur"
	,SPT."EquivalenteConGasto"
	,SPT."TipoCambioCierreConGasto"
	,SPT."Observacion"
	,SPT."CodigoComercio"
	,SPT."FolioPlanilla"
	,SPT."FolioInterfaz"
	,SPT."OperacionFicticia"
	,SPT."IdTipoCanalIngreso"
	,SPT."IdDatatec"
	,SPT."Estado"
	,TO_BIGINT(:IdUsuario)					AS "IdUsuario"
	,TO_BIGINT(0)							AS "IdUsuarioModifica"
	,TO_TIMESTAMP('0001-01-01 00:00:00')	AS "FechaModifica"
	,TO_TIMESTAMP('0001-01-01 00:00:00')	AS "FechaReproceso"
	,COALESCE(TIPMON."MultiplicaODivide",'M')	AS "MultiplicaODivide"
	,COALESCE(PAR."ParidadMercado",0)			AS "ParidadMercado"
	,COALESCE(PosMonPri."CierreIngresado",0)	AS "CierreIngresadoPosMonPri"
	,COALESCE(PosMonEqui."CierreIngresado",0)	AS "CierreIngresadoPosMonEqui"
	,INS."Nemotecnico"
	,COALESCE(PARUSD."TipoCambioMercado",0)		AS "TipoCambioUSD"
	,COALESCE(PARMONSEC."TipoCambioMercado",0)	AS "TipoCambioMonedaSecundaria"
	,TO_BIGINT(0)							AS "IdSolicitudAporteRescate"
	,SPT."AfectoADerivado"
	,SPT."DolarObservado"
	,SPT."IdEmpleadoDigitador"
	,SPT."FacturacionAutomatica"
	,SPT."EstadoCaja"
FROM "BSCL.Mesa.BD::SPT.CrearOperacionSpot_Proceso" SPT
--INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" INS
INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS
	ON (SPT."IdInstrumento" = INS."IdInstrumento" AND
		INS."Nemotecnico" = 'INTERB')
--LEFT JOIN "BSCL.Mesa.BD.Vistas::SPT.ConsultarPosicionInicial" as PosMonPri
LEFT JOIN "BSCL.Mesa.BD::SPT.PosicionInicialMesa" as PosMonPri
	ON (SPT."IdMonedaPrincipal" = PosMonPri."IdMoneda" AND
		PosMonPri."Estado" = 'ACT' AND
		TO_CHAR(PosMonPri."Fecha", 'yyyyMMdd') = TO_CHAR(:FechaHoraSistema, 'yyyyMMdd'))
--LEFT JOIN "BSCL.Mesa.BD.Vistas::SPT.ConsultarPosicionInicial" as PosMonEqui
LEFT JOIN "BSCL.Mesa.BD::SPT.PosicionInicialMesa" as PosMonEqui
	ON (PosMonEqui."IdMoneda" = :MonedaEquivalente AND
		PosMonEqui."Estado" = 'ACT' AND
		TO_CHAR(PosMonEqui."Fecha", 'yyyyMMdd') = TO_CHAR(:FechaHoraSistema, 'yyyyMMdd'))
LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PAR
	ON (PAR."IdMoneda" = SPT."IdMonedaPrincipal" AND
		TO_CHAR(PAR."Fecha", 'yyyyMMdd') = TO_CHAR(SPT."FechaOperacion", 'yyyyMMdd'))
		
LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PARDO
	ON (PARDO."IdMoneda" = 'DO' AND
		TO_CHAR(PARDO."Fecha", 'yyyyMMdd') = TO_CHAR(SPT."FechaOperacion", 'yyyyMMdd'))
		
LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PARUSD
	ON (PARUSD."IdMoneda" = 'USD' AND
		TO_CHAR(PARUSD."Fecha", 'yyyyMMdd') = TO_CHAR(SPT."FechaOperacion", 'yyyyMMdd'))
		
LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PARMONSEC
	ON (PARMONSEC."IdMoneda" = SPT."IdMonedaSecundaria" AND
		PARMONSEC."Fecha" = SPT."FechaOperacion" AND
		PARMONSEC."IdEstado" = 'ACT')
LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MON
	ON (SPT."IdMonedaPrincipal" = MON."IdMoneda")
LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoMoneda" AS TIPMON
	ON (MON."IdTipoMoneda" = TIPMON."IdTipoMoneda")
WHERE SPT."NumeroOperacion" IN (:NumeroOperacion);
