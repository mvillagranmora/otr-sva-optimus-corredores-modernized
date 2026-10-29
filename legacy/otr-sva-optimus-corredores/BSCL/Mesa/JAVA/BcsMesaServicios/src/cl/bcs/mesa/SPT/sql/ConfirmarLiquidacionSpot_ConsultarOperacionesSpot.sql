SELECT
	 SPT."IdOperacionSpot"
	,SPT."IdOperacionSpotOrigen"
	,SPT."IdTipoOperacion"
	,SPT."FechaOperacion"
	,SPT."FechaLiquidacion"
	,SPT."IdCliente"
	,SPT."IdPortafolio"
	,SPT."IdInstrumento"
	,INS."Nemotecnico"
	,SPT."IdMonedaPrincipal"
	,SPT."IdMonedaSecundaria"
	,SPT."MontoMonedaPrincipal"
	,SPT."MontoMonedaSecundaria"
	,SPT."TipoCambioCierre"
	,SPT."ParidadCierre"
	,SPT."TipoComprobanteFacturacion"
	,COALESCE(TIPMON."MultiplicaODivide",'M')	AS "MultiplicaODivide"
	,COALESCE(PARUSD."TipoCambioMercado",0)		AS "TipoCambioUSD"
	,COALESCE(PARMONSEC."TipoCambioMercado",0)	AS "TipoCambioMonedaSecundaria"
	,SPT."IdMesa"
	,SPT."Estado"
	,SPT."IdSolicitudAporteRescate"
	,SPT."IdTipoCanalIngreso"
	,SPT."EstadoConfirmacion"
	,CLI."DocumentoIdentidad"
	,CLI."NombreCompleto"
	,COALESCE(SOLAPORES."IdEmisorOrigen", '') AS "IdEmisorOrigen" 
	,COALESCE(SOLAPORES."MontoComision", 0)  AS "MontoComision"
	,COALESCE(SOLAPORES."IdTipoMovimientoFondoMutuo", '')  AS "IdTipoMovimientoFondoMutuo"
	,COALESCE(CAST (SOLAPORES."FechaCertificacion" AS NVARCHAR),'0001-01-01') AS "FechaCertificacion"
	,COALESCE(SERIEFONDO."IdInstrumento", 0) AS "IdInstrumentoFFMM"
	,COALESCE(SERIEFONDO."IdSerieFondo", '') AS "NemotecnicoFFMM"
	,COALESCE(FONDO."IdEmisor", '') AS "IdEmisorFFMM"
	,SPT."Observacion"
	,SPT."OperacionFicticia" AS "OperacionFicticia"
	,TIOP."IdTipoProducto"  AS "IdTipoProducto"
	,SPT."FacturacionAutomatica"
	,SPT."EstadoCaja"
FROM "BSCL.Mesa.BD::SPT.OperacionSpot" SPT
INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" TIOP ON TIOP."IdTipoOperacion" = SPT."IdTipoOperacion" 
INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS
	ON (SPT."IdInstrumento" = INS."IdInstrumento")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MON
	ON (SPT."IdMonedaPrincipal" = MON."IdMoneda")
LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoMoneda" AS TIPMON
	ON (MON."IdTipoMoneda" = TIPMON."IdTipoMoneda")
LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PARUSD
	ON (PARUSD."IdMoneda" = 'DO' AND
		PARUSD."Fecha" = TO_VARCHAR(SPT."FechaOperacion",'YYYY-MM-DD'))
LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PARMONSEC
	ON (PARMONSEC."IdMoneda" = SPT."IdMonedaSecundaria" AND
		PARMONSEC."Fecha" = TO_VARCHAR(SPT."FechaOperacion",'YYYY-MM-DD') AND
		PARMONSEC."IdEstado" = 'ACT')
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
	 ON SPT."IdCliente" = CLI."IdCliente"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" SOLAPORES
     ON SOLAPORES."IdOperacionSpot" = SPT."IdOperacionSpotOrigen"
    AND SOLAPORES."Estado" = 'CRT' 
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SERIEFONDO
     ON SERIEFONDO."IdSerieFondo" = SOLAPORES."IdSerieFondo"
    AND SERIEFONDO."IdEstado" = 'ACT'
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" FONDO 
     ON FONDO."IdFondo" = SERIEFONDO."IdFondo"    
WHERE SPT."IdOperacionSpot" IN (:NumeroOperacion) AND SPT."IdEmpresa" = :IdEmpresa;