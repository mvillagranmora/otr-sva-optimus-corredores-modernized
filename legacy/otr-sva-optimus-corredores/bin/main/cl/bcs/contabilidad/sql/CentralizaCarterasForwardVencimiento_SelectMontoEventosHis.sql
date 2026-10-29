SELECT
CC."IdCartera"
,CC."IdMonedaPrincipal"
,ABS(CC."INGMontoValorRazonableActivo") AS "RazonableActivo"
,ABS(CC."INGMontoValorRazonablePasivo") AS "RazonablePasivo"
,CC."INGMontoValorRazonableNeto" AS "RazonableNeto"
FROM "BSCL.Custodia.BD::HIS.Cartera" CC 
INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" CA ON (CC."IdCartera" = CA."IdCartera")
WHERE CA."IdEmpresa"			= :IdEmpresa
  AND CA."IdTipoProducto"		= 'MEFW'
  AND CASE WHEN CA."IdEstado"='ANT' OR (CA."IdEstado" IN ('VCD','LIQ') AND CA."EGRIdTipoOperacion" = 'ANTFWD') 
	THEN CA."EGRFechaOperacion" 
	ELSE CA."FechaVencimientoOperacion" 
		END	= :FechaContable
  AND (CA."IdEstado" IN ('ANT','LIQ') OR (CA."EGRIdTipoOperacion" = 'ANTFWD' AND CA."IdEstado" = 'VCD')) AND CC."FechaRespaldo" = :FechaAnteriorSistema;