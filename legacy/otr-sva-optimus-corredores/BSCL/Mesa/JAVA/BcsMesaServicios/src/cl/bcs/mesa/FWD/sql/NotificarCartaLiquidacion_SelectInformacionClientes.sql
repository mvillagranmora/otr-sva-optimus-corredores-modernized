SELECT
CAR."IdCartera"
,CC."IdPersona" AS "IdClientePersona"
,CAR."IdPortafolio" AS "IdPortafolioCliente"
,CC."NombreCompleto" AS "Cliente"
,CEMP."RazonSocial" AS "Corredora"
,CEMP."Telefono" AS "AtencionCliente"
,CPDE."IdDireccionElectronica" AS "IdCorreoCliente"
FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CC ON CC."IdCliente" = CAR."IdCliente"
INNER JOIN "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" CEMP ON CEMP."IdEmpresa" = :IdEmpresa
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" CPDE ON CPDE."IdDireccionElectronica" = CC."IdDireccionElectronicaPrincipal"
WHERE CAR."IdEmpresa" = :IdEmpresa AND CAR."IdCartera" IN (:IdCartera)

