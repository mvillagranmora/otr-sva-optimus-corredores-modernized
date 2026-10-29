SELECT
CLI."NombreCompleto"
,"DocumentoIdentidad"													
,"DocumentoIdentidadAdicional"
,"IdCliente"
,"IdEmpresa"
,EJE."NombreCompleto" AS "NombreEjecutivo"
FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto"	AS EJE
ON (CLI."IdPersonaEjecutivo" = EJE."IdPersona")
WHERE "IdEmpresa"=:IdEmpresa AND "IdCliente" IN (:IdCliente)