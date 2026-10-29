SELECT
    "PORT"."IdPortafolio",
    "PORT"."IdDireccionEnvioCorrespondencia",
    "PORT"."IdTelefonoContacto",
    "PORT"."IdCliente",
    "PORT"."IdPerfilCobros",
    "PORT"."IdPerfilProductos",
    "PORT"."Proposito",
    "PORT"."IdEmpleadoEjecutivo",
    "PORT"."IdEmpleadoEjecutivoComercial",
    COALESCE("ARE"."Codigo", '') AS "IdAreaCliente"
FROM "BSCL.Portafolio.BD::POR.Portafolio" AS "PORT"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Area" AS "ARE" 
    ON "PORT"."AreaCliente" = "ARE"."Codigo"
WHERE "PORT"."CodigoPortafolio" = :CodigoPortafolio
AND "PORT"."IdCliente" = :IdCliente
AND "PORT"."Borrado" = 'N'