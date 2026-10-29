 SELECT
CC."IdCartera"
,CC."IdMonedaPrincipal"
,ABS(CC."INGMontoValorRazonableActivo") AS "RazonableActivo"
,ABS(CC."INGMontoValorRazonablePasivo") AS "RazonablePasivo"
,CC."INGMontoValorRazonableNeto" AS "RazonableNeto"
,COALESCE(ABS(CC_HIS."INGMontoValorRazonableActivo"),0) AS "RazonableActivoAnterior"
,COALESCE(ABS(CC_HIS."INGMontoValorRazonablePasivo"),0) AS "RazonablePasivoAnterior"
,COALESCE(CC_HIS."INGMontoValorRazonableNeto",0) AS "RazonableNetoAnterior"
FROM "BSCL.Custodia.BD::HIS.Cartera" CC
LEFT JOIN "BSCL.Custodia.BD::HIS.Cartera" CC_HIS ON (CC."IdCartera" = CC_HIS."IdCartera")
  AND CC_HIS."FechaRespaldo" = :FechaAnteriorSistema
WHERE CC."IdEmpresa"			= :IdEmpresa
  AND CC."FechaRespaldo" = :FechaContable
  AND CC."IdTipoProducto"		= 'MEFW'
  AND CC."INGFechaOperacion"	<= :FechaContable 
  AND CC."FechaVencimientoOperacion" > :FechaContable
  AND CC."IdEstado"			= 'VIG' 