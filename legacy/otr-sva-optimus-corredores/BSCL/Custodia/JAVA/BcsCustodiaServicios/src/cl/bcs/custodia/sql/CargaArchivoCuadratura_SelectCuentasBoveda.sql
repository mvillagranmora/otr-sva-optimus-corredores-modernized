SELECT CUENTA."IdCuentaCustodioPortafolio",CUENTA."IdPortafolio",CUENTA."IdBoveda",CUENTA."Codigo",CLIENTE."NombreCompleto" AS "NombreBeneficiario",CLIENTE."DocumentoIdentidad" AS "RutBeneficiario"
FROM "BSCL.Portafolio.BD.Vistas::ConsultarCuentaCustodioPortafolio" CUENTA
INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" PORTAFOLIO ON (CUENTA."IdPortafolio" = PORTAFOLIO."IdPortafolio")
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLIENTE ON (CLIENTE."IdCliente" = PORTAFOLIO."IdCliente")
WHERE CUENTA."IdBoveda" IN (:IdBoveda) AND CUENTA."Codigo" IN (:Codigos);