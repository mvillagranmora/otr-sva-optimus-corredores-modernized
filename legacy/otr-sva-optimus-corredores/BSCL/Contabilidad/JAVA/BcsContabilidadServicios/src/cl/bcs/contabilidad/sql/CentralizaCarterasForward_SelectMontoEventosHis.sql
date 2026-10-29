SELECT DISTINCT
CA."IdCartera"
,CA."IdMonedaPrincipal"
,ABS(CC."INGMontoValorRazonableActivo") AS "RazonableActivo"
,ABS(CC."INGMontoValorRazonablePasivo") AS "RazonablePasivo"
,CC."INGMontoValorRazonableNeto" AS "RazonableNeto"
FROM "BSCL.Custodia.BD::HIS.Cartera" CC 
INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" CA ON (CC."IdCartera" = CA."IdCartera")
WHERE CA."IdEmpresa" = :IdEmpresa AND CA."IdTipoProducto" = 'MEFW' AND CA."INGFechaOperacion" <= :FechaContable AND CA."IdEstado" = 'VIG' AND CC."FechaRespaldo" = :FechaAnteriorSistema
UNION
SELECT DISTINCT
CA."IdCartera"
,CA."IdMonedaPrincipal"
,ABS(CC."INGMontoValorRazonableActivo") AS "RazonableActivo"
,ABS(CC."INGMontoValorRazonablePasivo") AS "RazonablePasivo"
,CC."INGMontoValorRazonableNeto" AS "RazonableNeto"
FROM "BSCL.Custodia.BD::HIS.Cartera" CC 
INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" CA ON (CC."IdCartera" = CA."IdCartera")
WHERE CA."IdEmpresa" = :IdEmpresa AND CA."IdTipoProducto" = 'MEFW' AND CA."EGRFechaOperacion" = :FechaContable AND CA."IdEstado" IN ('VCD','ANT') AND CC."FechaRespaldo" = :FechaAnteriorSistema