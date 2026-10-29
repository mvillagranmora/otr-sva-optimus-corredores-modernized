SELECT 
"Solicitudes"."Monto"  AS "CantidadMontoAporte"
,CASE WHEN COALESCE("Spot"."IdOperacionSpotOrigen",0) > 0 THEN "Spot"."MontoMonedaSecundaria" ELSE "Solicitudes"."MontoMonedaSistema" END AS "MontoMonedaSistemaAporte"
,"Solicitudes"."IdCliente"
,"Solicitudes"."IdPortafolio"
,"Fondo"."IdMoneda"  AS "IdMonedaFondo"
,TO_VARCHAR( "Solicitudes"."FechaEmision",'YYYY-MM-DD')  as "FechaEmision"
FROM "BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso" AS "CINV" 
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS "Solicitudes" ON "Solicitudes"."IdPortafolio" = "CINV"."IdPortafolio" 
LEFT OUTER JOIN "BSCL.Mesa.BD::SPT.OperacionSpot" "Spot" ON "Spot"."IdOperacionSpotOrigen"= "Solicitudes"."IdOperacionSpot" AND "Spot"."Estado" != 'MOD'
LEFT OUTER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS "ConsultarPortafolio" ON  ( "ConsultarPortafolio"."IdPortafolio" = "Solicitudes"."IdPortafolio")
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieFondo" ON ("SerieFondo"."IdSerieFondo" = "Solicitudes"."IdSerieFondo" AND "SerieFondo"."IdEmpresa" = "Solicitudes"."IdEmpresa")
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS "MatrizAgrupador" ON "MatrizAgrupador"."IdMatrizAgrupadorAPV" = "ConsultarPortafolio"."IdMatrizAgrupadorAPV"
LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS "ProductoAgrupador" ON "ProductoAgrupador"."IdProductoAgrupador" = "MatrizAgrupador"."IdProductoAgrupador"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondo" ON "Fondo"."IdFondo" = "SerieFondo"."IdFondo" AND "Fondo"."IdEmpresa" = "SerieFondo"."IdEmpresa"
WHERE
	"Solicitudes"."Estado" IN ('ING', 'PEN')
AND "Solicitudes"."TipoSolicitud" = 'APO'
AND (:IdProductoAgrupador IS NULL OR MAP("MatrizAgrupador"."IdProductoAgrupador",NULL,0,"MatrizAgrupador"."IdProductoAgrupador") = :IdProductoAgrupador)
AND "Solicitudes"."CalculoPorMontoCuota" != 'T'
AND (:FiltraIngresoAutomatico = 'N' OR "IngresoAutomatico" = :IngresoAutomatico)
AND "CINV"."IdMoneda"   = 'CLP'
AND  "CINV"."IdTrabajo" = :IdTrabajo;
