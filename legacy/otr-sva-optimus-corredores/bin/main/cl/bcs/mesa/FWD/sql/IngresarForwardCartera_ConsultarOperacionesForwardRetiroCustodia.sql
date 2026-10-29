SELECT
	 FWD."FolioInterno"
	,FWD."IdOperacion"
	,FWD."FolioCartera"
	,FWD."IdEstado"
	,FWD."IdTipoOperacionNuevo"						AS "IdTipoOperacion"
	,FWD."IdTipoInstrumento"
	,FWD."IdClasificacionTipoInstrumento"
	,FWD."IdTipoProducto"
	,FWD."IdInstrumento"
	,FWD."Nemotecnico"
	,FWD."IdClienteOriginal"						AS "IdCliente"				
	,FWD."IdPortafolioOriginal"						AS "IdPortafolio"
	,FWD."IdPersonaEjecutivoOriginal"				AS "IdPersonaEjecutivo"
	,FWD."IdTipoCanalIngreso"						AS "IdTipoCanalIngreso"
	,FWD."IdMonedaPrincipal"
	,FWD."IdMonedaSecundaria"
	,FWD."PrecioVencimiento"
	,FWD."MontoOperacionOriginal"					AS "MontoOperacion"
	,FWD."MontoInicialOriginal"						AS "MontoInicial"
	,FWD."MontoFinalOriginal"						AS "MontoFinal"
	,FWD."FechaOperacion"
	,FWD."TCCierreOriginal"							AS "TCCierre"
	,FWD."ParidadCierreOriginal"					AS "ParidadCierre"
	,FWD."PlazoOriginal"							AS "Plazo"
	,FWD."FechaVencimientoOriginal"					AS "FechaVencimiento"
	,FWD."FechaPagoOriginal"						AS "FechaPago"
	,FWD."IdModalidadLiquidacionOriginal"			AS "IdModalidadLiquidacion"
	,FWD."IdReferenciaMercado"
	,FWD."IdPlaza"
	,FWD."TCCostoOriginal"							AS "TCCosto"
	,FWD."PuntosTasasForwardOriginal"				AS "PuntosTasasForward"
	,FWD."PuntosForwardOriginal"					AS "PuntosForward"
	,FWD."TCForwardOriginal"						AS "TCForward"
	,FWD."TasasMonedaPrincipalOriginal"				AS "TasasMonedaPrincipal"
	,FWD."TasasMonedaSecundariaOriginal"			AS "TasasMonedaSecundaria"
	,FWD."MontoDiferidoOriginal"					AS "MontoDiferido"
	,FWD."MontoMargenOriginal"						AS "MontoMargen"
	,FWD."IdFixingOriginal"							AS "IdFixing"
	,FWD."FechaFixingOriginal"						AS "FechaFixing"
	,FWD."HoraFixingOriginal"						AS "HoraFixing"
	,FWD."IdMesa"
	,FWD."IdSpotAsociado"
	,FWD."Observaciones"
	,FWD."TasaDescuento"
	,FWD."TipoCambioAnticipo"
	,FWD."MontoARecibirAnticipo"
	,FWD."TipoCambioObservado"
	,COALESCE(PMP."TipoCambioMercado",0)			AS "TipoCambioMonedaPrincipal"
	,COALESCE(TIPMON."MultiplicaODivide",'M')		AS "TipoMonedaAdicionalSistema"
	,COALESCE(PMAS."ParidadMercado",0)				AS "ParidadMonedaAdicionalSistema"
	,COALESCE(TIPMONPRI."MultiplicaODivide",'M')	AS "TipoMonedaPrincipal"
	,:IdEmpresa										AS "IdEmpresa"
    ,COALESCE(CAR."IdCartera",0)					AS "IdCartera"
FROM "BSCL.Mesa.BD::FWD.IngresarForwardCartera_Proceso" FWD
--LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" AS CAR
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR
	ON (CAR."FolioCartera" = FWD."FolioCartera" AND 
		CAR."IdTipoProducto" = 'MEFW' AND
		(CAR."IdEstado" = CASE WHEN FWD."IdTipoOperacionNuevo" IN ('MODFWD','ANUFWD','ANTFWD') THEN 'VIG'
							  WHEN FWD."IdTipoOperacionNuevo" = 'VCDFWD' THEN FWD."IdEstado"
							  WHEN FWD."IdTipoOperacionNuevo" = 'REVFWD' THEN 'VIG' END))
LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PMP
	ON (PMP."IdMoneda" = FWD."IdMonedaPrincipal" AND
		PMP."Fecha" = FWD."FechaOperacion")
LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PMAS
	ON (PMAS."IdMoneda" = :MonedaAdicionalSistema AND
		PMAS."Fecha" = FWD."FechaOperacion")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MON
	ON (MON."IdMoneda" = :MonedaAdicionalSistema)
LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoMoneda" AS TIPMON
	ON (MON."IdTipoMoneda" = TIPMON."IdTipoMoneda")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MONPRI
	ON (MONPRI."IdMoneda" = FWD."IdMonedaPrincipal")
LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoMoneda" AS TIPMONPRI
	ON (MONPRI."IdTipoMoneda" = TIPMONPRI."IdTipoMoneda")
WHERE FWD."FolioInterno" IN (:FolioInterno)
  AND ((CAR."IdCartera" IN (:FolioInterno)) OR
  	   CAR."FolioCartera" IN (:FolioInterno))
;
