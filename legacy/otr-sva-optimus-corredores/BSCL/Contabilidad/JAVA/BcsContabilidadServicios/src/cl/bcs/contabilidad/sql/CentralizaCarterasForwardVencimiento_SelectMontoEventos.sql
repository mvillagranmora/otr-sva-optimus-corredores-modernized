SELECT
"IdCartera"
,"IdMonedaPrincipal"
,ABS(CC."INGMontoValorRazonableActivo") AS "RazonableActivo"
,ABS(CC."INGMontoValorRazonablePasivo") AS "RazonablePasivo"
,CC."INGMontoValorRazonableNeto" AS "RazonableNeto"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" CC
WHERE "IdEmpresa"			= :IdEmpresa
  AND "IdTipoProducto"		= 'MEFW'
  AND CASE WHEN CC."IdEstado"='ANT' OR (CC."IdEstado" IN ('VCD','LIQ') AND CC."EGRIdTipoOperacion" = 'ANTFWD') 
	THEN CC."EGRFechaOperacion" 
	ELSE CC."FechaVencimientoOperacion" 
		END	= :FechaContable
  AND ("IdEstado" IN ('ANT','LIQ') OR (CC."EGRIdTipoOperacion" = 'ANTFWD' AND CC."IdEstado" = 'VCD'))