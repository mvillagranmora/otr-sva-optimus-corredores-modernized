SELECT DISTINCT
"IdCartera"
,"IdMonedaPrincipal"
,ABS(CC."INGMontoValorRazonableActivo") AS "RazonableActivo"
,ABS(CC."INGMontoValorRazonablePasivo") AS "RazonablePasivo"
,CC."INGMontoValorRazonableNeto" AS "RazonableNeto"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" CC
WHERE "IdEmpresa" = :IdEmpresa AND "IdTipoProducto" = 'MEFW' AND "INGFechaOperacion" <= :FechaContable AND "IdEstado" = 'VIG'
UNION
SELECT DISTINCT
"IdCartera"
,"IdMonedaPrincipal"
,ABS(CC."INGMontoValorRazonableActivo") AS "RazonableActivo"
,ABS(CC."INGMontoValorRazonablePasivo") AS "RazonablePasivo"
,CC."INGMontoValorRazonableNeto" AS "RazonableNeto"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" CC
WHERE "IdEmpresa" = :IdEmpresa AND "IdTipoProducto" = 'MEFW' AND "EGRFechaOperacion" = :FechaContable AND "IdEstado" IN ('VCD','ANT')