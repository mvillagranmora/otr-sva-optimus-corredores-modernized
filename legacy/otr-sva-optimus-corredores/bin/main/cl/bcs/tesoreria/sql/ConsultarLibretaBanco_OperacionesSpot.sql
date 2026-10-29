WITH  MOVIMIENTOSSPOT AS (
	SELECT 
		CD."IdComprobante" 
		,OPE."IdOperacionUI" as "IdOperacionSpot"
		,OPE."IdMonedaPrincipal" 
		,OPE."IdMonedaSecundaria"
		,OPE."MontoMonedaPrincipal" 
		,OPE."MontoMonedaSecundaria" 
		,IFNULL(OPE."TipoCambioCierre" ,0) as "TipoCambioCierre"
		,IFNULL(OPE."ParidadCierre" ,0) as "ParidadCierre"
		,OPE."Nemotecnico"  
		,IFNULL(PARAM."Valor",0) AS "DolarObservado"
	FROM
		"BSCL.Tesoreria.BD::TES.ComprobanteDetalleOrigen" AS CDO
	INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" AS CD ON
		CD."IdComprobanteDetalle" = CDO."IdComprobanteDetalle"
	INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" AS C ON
		C."IdComprobante" = CD."IdComprobante"
	LEFT JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS IE ON
		IE."IdIngresoEgreso" = CDO."IdIngresoEgreso"
	LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" FAC ON (IE."IdOperacion"=FAC."IdComprobante")
	INNER JOIN "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot" OPE  ON (OPE."IdOperacion"=FAC."IdTransaccion")
	LEFT JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" PARAM ON (TO_VARCHAR(PARAM."Fecha",'YYYY-MM-DD')=TO_VARCHAR(OPE."FechaOperacion",'YYYY-MM-DD') AND PARAM."IdParametroFinanciero" ='DO' )
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MON
	ON (OPE."IdMonedaPrincipal" = MON."IdMoneda")
	WHERE C."IdComprobante"IN (:idsComprobante) AND OPE."OperacionFicticia"='N'
),
MOVIMIENTOSPOTNOFACTURABLE AS (
	SELECT 
	CD."IdComprobante" 
		,OPE."IdOperacionUI"  as "IdOperacionSpot"
		,OPE."IdMonedaPrincipal" 
		,OPE."IdMonedaSecundaria"
		,OPE."MontoMonedaPrincipal" 
		,OPE."MontoMonedaSecundaria" 
		,IFNULL(OPE."TipoCambioCierre" ,0)as "TipoCambioCierre"
		,IFNULL(OPE."ParidadCierre" ,0)  as "ParidadCierre"
		,OPE."Nemotecnico" 
		,IFNULL(PARAM."Valor",0) AS "DolarObservado"
	FROM
		"BSCL.Tesoreria.BD::TES.ComprobanteDetalleOrigen" AS CDO
	INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" AS CD ON
		CD."IdComprobanteDetalle" = CDO."IdComprobanteDetalle"
	INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" AS C ON
		C."IdComprobante" = CD."IdComprobante"
	LEFT JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS IE ON
		IE."IdIngresoEgreso" = CDO."IdIngresoEgreso"	
	INNER JOIN "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot" OPE  ON (OPE."IdOperacion"=IE."IdOperacion")
	LEFT JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" PARAM ON (TO_VARCHAR(PARAM."Fecha",'YYYY-MM-DD')=TO_VARCHAR(OPE."FechaOperacion",'YYYY-MM-DD') AND PARAM."IdParametroFinanciero" ='DO' )
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MON
	ON (OPE."IdMonedaPrincipal" = MON."IdMoneda")
	WHERE C."IdComprobante"IN (:idsComprobante) AND OPE."OperacionFicticia"='S'
)
SELECT 	"IdComprobante" 
		,"IdOperacionSpot"
		,"IdMonedaPrincipal" 
		,"IdMonedaSecundaria"
		,"MontoMonedaPrincipal" 
		,"MontoMonedaSecundaria" 
		,"TipoCambioCierre"
		,"ParidadCierre"
		,"Nemotecnico" 
		,"DolarObservado" 
		FROM MOVIMIENTOSSPOT

UNION ALL
SELECT "IdComprobante" 
		,"IdOperacionSpot"
		,"IdMonedaPrincipal" 
		,"IdMonedaSecundaria"
		,"MontoMonedaPrincipal" 
		,"MontoMonedaSecundaria" 
		,"TipoCambioCierre"
		,"ParidadCierre"
		,"Nemotecnico" 
		,"DolarObservado" 
		FROM MOVIMIENTOSPOTNOFACTURABLE;