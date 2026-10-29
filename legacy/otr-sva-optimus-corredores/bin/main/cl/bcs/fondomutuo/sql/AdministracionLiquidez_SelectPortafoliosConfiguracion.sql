SELECT "CONFIGURACION"."IdPortafolio"
,"CONFIGURACION"."IdCliente"
,"CONFIGURACION"."IdSerieFondo"
,"CONFIGURACION"."MontoMinimo"
,"CONFIGURACION"."IdMoneda"
FROM "BSCL.FondoMutuo.BD::FMM.ConfiguracionPortafolioAdministracionLiquidez" AS "CONFIGURACION" 
WHERE "CONFIGURACION"."IdPortafolio" IN (:IdPortafolio)
AND "CONFIGURACION"."IdEstado" = 'ACT'