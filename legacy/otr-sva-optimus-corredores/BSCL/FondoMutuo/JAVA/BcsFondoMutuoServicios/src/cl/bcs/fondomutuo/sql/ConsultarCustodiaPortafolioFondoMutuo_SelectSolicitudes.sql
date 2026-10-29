WITH ConsultaPortafolio AS (
	SELECT 
		"PORT"."IdPortafolio", 
		"PORT"."CodigoPortafolio",
		"CLI"."DocumentoIdentidad",
		"CLI"."RazonSocial",
		SOC."IdSocioDeNegocio",
		"PORT"."IdMatrizAgrupadorAPV",
		CASE WHEN COALESCE("PORT"."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE("PORT"."IdMatrizAgrupadorAPV",0) ELSE COALESCE(MAT_NOAPV."IdMatrizAgrupadorNOAPV",0) END AS "IdMatrizAgrupador",
		CASE WHEN COALESCE("PORT"."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE("MAT"."EsADC",'N') ELSE COALESCE(MAT_NOAPV."EsADC",'N') END AS "EsADC"
	FROM "BSCL.Portafolio.BD::POR.Portafolio" AS "PORT"
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "CLI" ON "CLI"."IdCliente" = "PORT"."IdCliente"
	LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SOC on SOC."IdCliente" = "PORT"."IdCliente"
	LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" MAT ON MAT."IdMatrizAgrupadorAPV" = "PORT"."IdMatrizAgrupadorAPV"
	LEFT JOIN "BSCL.APV.BD::REC.RegimenTributario" RT ON RT."IdRegimenTributario" = "PORT"."IdMatrizAgrupadorAPV"
	LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = PORT."CodigoPortafolio" AND  "PORT"."IdMatrizAgrupadorAPV" = 0
	WHERE (:IdPortafolio = 0 OR "PORT"."IdPortafolio" = :IdPortafolio)
)

SELECT
"Solicitudes"."IdSerieFondo"
,TO_DECIMAL(SUM(CASE "Solicitudes"."TipoSolicitud" WHEN 'APO' THEN "Solicitudes"."NumeroCuotas" ELSE 0 END),26,8) AS "CantidadAporte"
,TO_DECIMAL(SUM(CASE 
					WHEN "Solicitudes"."TipoSolicitud" = 'RES' THEN "Solicitudes"."NumeroCuotas"
                ELSE 0 
                END),26,8) AS "CantidadRescate"
,TO_DECIMAL(SUM(CASE "Solicitudes"."TipoSolicitud" WHEN 'APO' THEN "Solicitudes"."Monto" ELSE 0 END),26,8) AS "CantidadMontoAporte"
,TO_DECIMAL(SUM(CASE "Solicitudes"."TipoSolicitud" WHEN 'RES' THEN "Solicitudes"."Monto" ELSE 0 END),26,8) AS "CantidadMontoRescate"
,TO_DECIMAL(SUM(CASE "Solicitudes"."TipoSolicitud" WHEN 'APO' THEN CASE WHEN COALESCE(Spot."IdOperacionSpotOrigen",0) > 0 THEN Spot."MontoMonedaSecundaria" ELSE "Solicitudes"."MontoMonedaSistema" END ELSE 0 END),26,8) AS "MontoMonedaSistemaAporte"
,"Solicitudes"."IdCliente"
,"Solicitudes"."IdPortafolio"
,"ConsultarPortafolio"."IdSocioDeNegocio"
,"ConsultarPortafolio"."DocumentoIdentidad"
,"ConsultarPortafolio"."RazonSocial"
,"ConsultarPortafolio"."CodigoPortafolio"
,"SerieFondo"."IdInstrumento"
,Fondo."IdEmisor"
,ProductoAgrupador."NombreProductoAgrupador" AS "Producto"
,"Solicitudes"."IdRegimenTributario" AS "IdRegimenFondoMutuo"
,COALESCE("ConsultarPortafolio"."IdMatrizAgrupador",0) AS "IdMatrizAgrupador"
,COALESCE("ConsultarPortafolio"."EsADC",'N') AS "EsADC"
,"Solicitudes"."CalculoPorMontoCuota"
,Fondo."IdMoneda" AS "IdMonedaFondo"
,COALESCE (REG."Descripcion",'') AS "RegimenTributario"
,COALESCE("Solicitudes"."IdTipoMovimientoFondoMutuo",'') AS "IdTipoMovimientoFondoMutuo"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS "Solicitudes"
LEFT OUTER JOIN "BSCL.Mesa.BD::SPT.OperacionSpot" Spot ON Spot."IdOperacionSpotOrigen"= "Solicitudes"."IdOperacionSpot" AND Spot."Estado" != 'MOD'
LEFT OUTER JOIN ConsultaPortafolio AS "ConsultarPortafolio" ON  ( "ConsultarPortafolio"."IdPortafolio" = "Solicitudes"."IdPortafolio")
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieFondo" ON ("SerieFondo"."IdSerieFondo" = "Solicitudes"."IdSerieFondo" AND "SerieFondo"."IdEmpresa" = :IdEmpresa)
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = "ConsultarPortafolio"."IdMatrizAgrupadorAPV"
LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS ProductoAgrupador ON ProductoAgrupador."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS Fondo ON Fondo."IdFondo" = "SerieFondo"."IdFondo" AND Fondo."IdEmpresa" = "SerieFondo"."IdEmpresa"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS REG ON REG."IdRegimenFondoMutuo" = "Solicitudes"."IdRegimenTributario"
WHERE
	"Solicitudes"."Estado" IN ('ING', 'PEN')
AND "Solicitudes"."IdEmpresa" = :IdEmpresa
AND "Solicitudes"."TipoSolicitud" IN ('APO','RES')
AND (:IdCliente = 0 OR "Solicitudes"."IdCliente" = :IdCliente)
AND (:IdPortafolio = 0 OR "ConsultarPortafolio"."IdPortafolio" = :IdPortafolio)
AND (:IdInstrumento = 0 OR "SerieFondo"."IdInstrumento" = :IdInstrumento)
AND (:IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :IdProductoAgrupador)
AND (:IdEmisor IS NULL OR Fondo."IdEmisor" = :IdEmisor)
AND (:IdFamiliaFondo ='' OR "SerieFondo"."IdFamiliaFondo" = :IdFamiliaFondo)
AND (:FiltraIngresoAutomatico = 'N' OR "IngresoAutomatico" = :IngresoAutomatico OR "Solicitudes"."IdRegimenTributario"='BF')
AND "Solicitudes"."CalculoPorMontoCuota" != 'T'
GROUP BY
"Solicitudes"."IdSerieFondo"
,"Solicitudes"."IdCliente"
,"Solicitudes"."IdPortafolio"
,"ConsultarPortafolio"."IdSocioDeNegocio"
,"ConsultarPortafolio"."DocumentoIdentidad"
,"ConsultarPortafolio"."RazonSocial"
,"ConsultarPortafolio"."CodigoPortafolio"
,"SerieFondo"."IdInstrumento" 
,Fondo."IdEmisor"
,ProductoAgrupador."NombreProductoAgrupador"
,"Solicitudes"."IdRegimenTributario"
,"ConsultarPortafolio"."IdMatrizAgrupador"
,"ConsultarPortafolio"."EsADC"
,"Solicitudes"."CalculoPorMontoCuota"
,Fondo."IdMoneda"
,REG."Descripcion"
,"Solicitudes"."IdTipoMovimientoFondoMutuo"