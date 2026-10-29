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
,SOC."IdSocioDeNegocio"
,"IDENT"."DocumentoIdentidad"
,"PER"."RazonSocial"
,"PORT"."CodigoPortafolio"
,"SerieFondo"."IdInstrumento"
,Fondo."IdEmisor"
,ProductoAgrupador."NombreProductoAgrupador" AS "Producto"
,"Solicitudes"."IdRegimenTributario" AS "IdRegimenFondoMutuo"
,COALESCE(CASE WHEN COALESCE("PORT"."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE("PORT"."IdMatrizAgrupadorAPV",0) ELSE COALESCE(MAT_NOAPV."IdMatrizAgrupadorNOAPV",0) END,0) AS "IdMatrizAgrupador"
,COALESCE(CASE WHEN COALESCE("PORT"."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE("MAT"."EsADC",'N') ELSE COALESCE(MAT_NOAPV."EsADC",'N') END,'N') AS "EsADC"
,"Solicitudes"."CalculoPorMontoCuota"
,Fondo."IdMoneda" AS "IdMonedaFondo"
,COALESCE (REG."Descripcion",'') AS "RegimenTributario"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS "Solicitudes"
LEFT OUTER JOIN "BSCL.Mesa.BD::SPT.OperacionSpot" Spot ON Spot."IdOperacionSpotOrigen"= "Solicitudes"."IdOperacionSpot" AND Spot."Estado" != 'MOD'
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS "PORT" ON ( "PORT"."IdPortafolio" = "Solicitudes"."IdPortafolio")
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS "CLI" ON "CLI"."IdCliente" = "PORT"."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS "PER" ON "PER"."IdPersona" = "CLI"."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as "IDENT" ON "PER"."IdDocumentoIdentidad" = "IDENT"."IdDocumentoIdentidad"  AND "IDENT"."Predeterminado" ='S'
LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SOC on SOC."IdCliente" = "PORT"."IdCliente"
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" MAT ON MAT."IdMatrizAgrupadorAPV" = "PORT"."IdMatrizAgrupadorAPV"
LEFT JOIN "BSCL.APV.BD::REC.RegimenTributario" RT ON RT."IdRegimenTributario" = "PORT"."IdMatrizAgrupadorAPV"
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = PORT."CodigoPortafolio" AND  "PORT"."IdMatrizAgrupadorAPV" = 0
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieFondo" ON ("SerieFondo"."IdSerieFondo" = "Solicitudes"."IdSerieFondo" AND "SerieFondo"."IdEmpresa" = :IdEmpresa)
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = "PORT"."IdMatrizAgrupadorAPV"
LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS ProductoAgrupador ON ProductoAgrupador."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS Fondo ON Fondo."IdFondo" = "SerieFondo"."IdFondo" AND Fondo."IdEmpresa" = "SerieFondo"."IdEmpresa"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS REG ON REG."IdRegimenFondoMutuo" = "Solicitudes"."IdRegimenTributario"
WHERE
	"Solicitudes"."Estado" IN ('ING', 'PEN')
AND "Solicitudes"."IdEmpresa" = :IdEmpresa
AND "Solicitudes"."TipoSolicitud" IN ('APO','RES')
AND ("Solicitudes"."IdCliente" = :IdCliente)
AND (:FiltraIngresoAutomatico = 'N' OR "IngresoAutomatico" = :IngresoAutomatico)
AND "Solicitudes"."CalculoPorMontoCuota" != 'T'
GROUP BY
"Solicitudes"."IdSerieFondo"
,"Solicitudes"."IdCliente"
,"Solicitudes"."IdPortafolio"
,SOC."IdSocioDeNegocio"
,"IDENT"."DocumentoIdentidad"
,"PER"."RazonSocial"
,"PORT"."CodigoPortafolio"
,"SerieFondo"."IdInstrumento" 
,Fondo."IdEmisor"
,ProductoAgrupador."NombreProductoAgrupador"
,"Solicitudes"."IdRegimenTributario"
,"PORT"."IdMatrizAgrupadorAPV"
,MAT_NOAPV."IdMatrizAgrupadorNOAPV"
,MAT_NOAPV."EsADC"
,MAT."EsADC"
,"Solicitudes"."CalculoPorMontoCuota"
,Fondo."IdMoneda"
,Fondo."IdMoneda"
,REG."Descripcion"



