SELECT
	 FWD."FolioInterno"
	,FWD."IdOperacion"
	,FWD."FolioCartera"
	,FWD."IdTipoOperacionOriginal"					AS "IdTipoOperacion"
	,FWD."IdEstado"
	,FWD."IdTipoInstrumento"
	,FWD."IdClasificacionTipoInstrumento"
	,FWD."IdTipoProducto"
	,FWD."IdInstrumento"
	,FWD."Nemotecnico"
	,FWD."IdClienteNuevo"							AS "IdCliente"				
	,FWD."IdPortafolioNuevo"						AS "IdPortafolio"
	,FWD."IdPersonaEjecutivoNuevo"					AS "IdPersonaEjecutivo"
	,FWD."IdTipoCanalIngreso"						AS "IdTipoCanalIngreso"
	,FWD."IdMonedaPrincipal"
	,FWD."IdMonedaSecundaria"
	,FWD."PrecioVencimiento"
	,FWD."MontoOperacionNuevo"						AS "MontoOperacion"
	,FWD."MontoInicialNuevo"						AS "MontoInicial"
	,FWD."MontoFinalNuevo"							AS "MontoFinal"
	,FWD."FechaOperacion"
	,FWD."TCCierreNuevo"							AS "TCCierre"
	,FWD."ParidadCierreNuevo"						AS "ParidadCierre"
	,FWD."PlazoNuevo"								AS "Plazo"
	,FWD."FechaVencimientoNuevo"					AS "FechaVencimiento"
	,FWD."FechaPagoNuevo"							AS "FechaPago"
	,FWD."IdModalidadLiquidacionNuevo"				AS "IdModalidadLiquidacion"
	,FWD."IdReferenciaMercado"
	,FWD."IdPlaza"
	,FWD."TCCostoNuevo"								AS "TCCosto"
	,FWD."PuntosTasasForwardNuevo"					AS "PuntosTasasForward"
	,FWD."PuntosForwardNuevo"						AS "PuntosForward"
	,FWD."TCForwardNuevo"							AS "TCForward"
	,FWD."TasasMonedaPrincipalNuevo"				AS "TasasMonedaPrincipal"
	,FWD."TasasMonedaSecundariaNuevo"				AS "TasasMonedaSecundaria"
	,FWD."MontoDiferidoNuevo"						AS "MontoDiferido"
	,FWD."MontoMargenNuevo"							AS "MontoMargen"
	,FWD."IdFixingNuevo"							AS "IdFixing"
	,FWD."FechaFixingNuevo"							AS "FechaFixing"
	,FWD."HoraFixingNuevo"							AS "HoraFixing"
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
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR
	ON (CAR."IdCartera" = FWD."FolioInterno")
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
WHERE FWD."FolioInterno" IN (:FolioInterno);
