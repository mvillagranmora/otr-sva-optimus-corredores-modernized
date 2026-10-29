SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion",CC."IdCartera" AS "IdMovimiento",'VALOR_MONEDA_PRINCIPAL' AS "IdEvento",'Valor Moneda Principal' AS "Nombre", ABS(CC."INGMontoMonedaPrincipal") AS "MontoEvento", CC."IdMonedaPrincipal" AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" cc
WHERE "IdEmpresa" = :IdEmpresa AND "IdTipoProducto" = 'MEFW' AND "INGFechaOperacion" = :FechaContable AND "IdEstado" IN ('VIG','LIQ')
UNION

SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion",CC."IdCartera" AS "IdMovimiento",'MONTO_SECUNDARIA' AS "IdEvento",'Monto Moneda Secundaria' AS "Nombre", ABS(CC."INGMontoMonedaSecundaria") AS "MontoEvento", CC."IdMonedaSecundaria" AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" CC
INNER JOIN "BSCL.Plataforma.BD::PLA.Configuracion"	 MO
ON (CC."IdEmpresa" = MO."IdEmpresa")
WHERE 
	CC."IdEmpresa" 				= :IdEmpresa 	 	AND 
	CC."IdTipoProducto" 		= 'MEFW' 		 	AND 
	CC."INGFechaOperacion" 		= :FechaContable 	AND 
	CC."IdEstado" 				IN  ('VIG','LIQ')	AND  
	MO."CodigoParametro"		='PLA.MonedaSistema'	AND 
	CC."IdMonedaSecundaria" 	= coalesce(MO."Valor",MO."ValorDefecto") 
	

UNION
SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion",CC."IdCartera" AS "IdMovimiento",'VALOR_MONEDA_SECUNDARIA' AS "IdEvento",'Valor Moneda Secundaria' AS "Nombre", ABS(CC."INGMontoMonedaSecundaria") AS "MontoEvento", CC."IdMonedaSecundaria" AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" CC
INNER JOIN "BSCL.Plataforma.BD::PLA.Configuracion"	 MO
ON (CC."IdEmpresa" = MO."IdEmpresa")
WHERE 
	CC."IdEmpresa" 				= :IdEmpresa 	 	AND 
	CC."IdTipoProducto" 		= 'MEFW' 		 	AND 
	CC."INGFechaOperacion" 		= :FechaContable 	AND 
	CC."IdEstado" 				IN  ('VIG','LIQ')	AND  
	MO."CodigoParametro"		='PLA.MonedaSistema'	AND 
	CC."IdMonedaSecundaria" 	<> coalesce(MO."Valor",MO."ValorDefecto") 
UNION
 
SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion",CC."IdCartera" AS "IdMovimiento",'MONTO_MONEDA_CLP' AS "IdEvento",'Monto en CLP' AS "Nombre"
	, CASE WHEN "IdMonedaSecundaria" = 'CLP' THEN ABS(CC."INGMontoMonedaSecundaria") ELSE ABS("INGMontoATasaCompraActual") END AS "MontoEvento"
	, 'CLP' AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" CC
WHERE 
	CC."IdEmpresa" 				= :IdEmpresa 	 	AND 
	CC."IdTipoProducto" 		= 'MEFW' 		 	AND 
	CC."INGFechaOperacion" 		= :FechaContable 	AND 
	CC."IdEstado" 				= 'VIG'  

UNION
 
SELECT 
	TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion"
	,CC."IdCartera" AS "IdMovimiento"
	,'MARGEN_UTILIDAD' AS "IdEvento"
	,'Margen de utilidades' AS "Nombre"
	,CASE WHEN CC."INGMontoMargen" > 0 THEN CC."INGMontoMargen"  ELSE 0 END AS "MontoEvento" 
	,CC."IdMonedaSistema" AS "CodigoMoneda"
	
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" CC
WHERE 
	CC."IdEmpresa" 				= :IdEmpresa 	 	AND 
	CC."IdTipoProducto" 		= 'MEFW' 		 	AND 
	CC."INGFechaOperacion" 		= :FechaContable 	AND 
	CC."IdEstado" 				= 'VIG'  
	
UNION
 
SELECT
	TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion"
	,CC."IdCartera" AS "IdMovimiento"
	,'MARGEN_PERDIDA' AS "IdEvento"
	,'Margen de perdidas' AS "Nombre"
	,CASE WHEN CC."INGMontoMargen" < 0 THEN CC."INGMontoMargen"  ELSE  0 END AS "MontoEvento" 
	,CC."IdMonedaSistema" AS "CodigoMoneda"
	
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" CC
WHERE 
	CC."IdEmpresa" 				= :IdEmpresa 	 	AND 
	CC."IdTipoProducto" 		= 'MEFW' 		 	AND 
	CC."INGFechaOperacion" 		= :FechaContable 	AND 
	CC."IdEstado" 				= 'VIG'  