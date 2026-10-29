SELECT
per."NombreCompleto",
per."DocumentoIdentidad",
per."DocumentoIdentidadAdicional",
cli."IdCliente",
cli."IdEmpresa"
FROM "BSCL.Custodia.BD::CUS.ConsultarCustodiaPortafolio_Clientes" cc
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" cli on cc."IdCliente" = cli."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" per on cli."IdPersona" = per."IdPersona"
WHERE "IdEmpresa"=:IdEmpresa AND "IdTrabajo" = :IdTrabajo and cli."IdCliente" is not null

