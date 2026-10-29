SELECT
"NombreCompleto",
"DocumentoIdentidad",
"DocumentoIdentidadAdicional",
"IdCliente",
"IdEmpresa",
"IdPersona"
FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"
WHERE "IdEmpresa"=:IdEmpresa AND "IdCliente" IN (:IdCliente)