SELECT
CAR."IdOperacion"
,CAR."FolioCartera"
,CLI."NombreCompleto" AS "RazonSocial"
,CAR."INGMontoMonedaPrincipal" AS "MontoMonedaPrincipal"
,CAR."INGMontoMonedaSecundaria" AS "MontoMonedaSecundaria"
,CAR."INGMontoValorRazonableActivo" AS "RazonableActivo"
,CAR."INGMontoValorRazonablePasivo" AS "RazonablePasivo"
,CAR."IdMonedaPrincipal"
,CAR."IdMonedaSecundaria"
,CAR."IdEstado"
,CAR."IdTipoProducto" AS "IdMesa"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" CAR
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI ON  CAR."IdCliente" = CLI."IdCliente"
WHERE CAR."IdEmpresa" = :IdEmpresa AND CAR."IdTipoProducto" = 'MEFW' AND CAR."IdEstado" = 'VIG';