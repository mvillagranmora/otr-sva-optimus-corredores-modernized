SELECT
    "PORT"."IdPortafolio",
    "PORT"."IdDireccionEnvioCorrespondencia",
    "PORT"."IdTelefonoContacto",
    "PORT"."IdCliente",
    "PORT"."IdPerfilCobros" AS "IdPlantillaCobros",
    "PORT"."IdPerfilProductos" AS "IdPlantillaProductos",
    "PORT"."Proposito" AS "PropositoPortafolio",
    "PORT"."IdEmpleadoEjecutivo" AS "IdEjecutivoPortafolio",
    "PORT"."IdEmpleadoEjecutivoComercial" AS "IdEjecutivoComercial",
    COALESCE("ARE"."Codigo", '') AS "IdAreaCliente"
FROM "BSCL.Portafolio.BD::POR.Portafolio" AS "PORT"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Area" AS "ARE"
    ON "PORT"."AreaCliente" = "ARE"."Codigo"
WHERE "PORT"."CodigoPortafolio" = :CodigoPortafolio
AND "PORT"."IdCliente" = :IdCliente
AND "PORT"."Borrado" = 'N'
