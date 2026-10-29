SELECT 
FWD_COMP."IdOperacion"
,FWD_COMP."FolioCartera"
,CLI."NombreCompleto" AS "RazonSocial"
,FWD_COMP."MontoMonedaPrincipal"
,FWD_COMP."MontoMonedaSecundaria"
,FWD_COMP."IdMonedaPrincipal"
,FWD_COMP."IdMonedaSecundaria"
,FWD_COMP."IdTipoProducto" AS "IdMesa"
,FWD_COMP."IdEstado"
,FWD_COMP."INGMontoValorRazonableActivo" AS "RazonableActivo"
,FWD_COMP."INGMontoValorRazonablePasivo" AS "RazonablePasivo"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraLite"  AS FWD_COMP
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI ON  FWD_COMP."IdCliente" = CLI."IdCliente"
WHERE "IdTipoProducto" = 'MEFW' 
AND TO_VARCHAR("FechaOperacion",'YYYY-MM-DD') = :Fecha 
AND FWD_COMP."IdEmpresa" = :IdEmpresa;