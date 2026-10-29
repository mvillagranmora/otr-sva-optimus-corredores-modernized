SELECT 
 CLI."IdPersona"
,CLI."IdCliente"
,CLI."IdEstado"
,CLI."TipoEnrolamiento"
FROM "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI
WHERE "IdCliente" IN (:idCliente)
OR   "IdPersona" IN (:idPersona)  -- cambio por tablas