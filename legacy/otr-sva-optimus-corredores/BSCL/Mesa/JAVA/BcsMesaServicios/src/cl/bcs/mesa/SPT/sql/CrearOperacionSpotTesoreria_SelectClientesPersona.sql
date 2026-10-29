
SELECT cli."IdCliente", per."IdPersona", per."Nombres", cli."IdPersonaEjecutivo" AS "IdEjecutivo", cli."IdSucursalPreferencia" AS "IdSucursal"
FROM "BSCL.SocioNegocio.BD::CLI.Cliente" AS cli 
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS per ON cli."IdPersona" = per."IdPersona"
WHERE cli."IdCliente" in (:IdClientes);