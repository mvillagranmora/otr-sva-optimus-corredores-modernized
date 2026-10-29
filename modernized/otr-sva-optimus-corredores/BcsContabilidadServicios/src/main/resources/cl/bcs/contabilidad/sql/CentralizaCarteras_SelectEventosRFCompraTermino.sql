SELECT 
	TO_BIGINT(:IdTipoCentralizacion) 					AS "IdTipoCentralizacion",
	CAR."IdCartera"											AS "IdMovimiento"
	,'MONTO_ACTUAL_COMPRA' 								AS "IdEvento"
	,'Monto a Tasa Compra Actual' 						AS "Nombre"
	,OP."MontoOperacion" 								AS "MontoEvento"
	,CAR."IdMonedaSistema"									AS "CodigoMoneda"
FROM "BSCL.Mesa.BD::MES.Operacion" OP 
LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" 						RES 	ON 	OP."IdOperacion" 			= RES."IdOperacion"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" 					PORT 	ON 	OP."IdPortafolio"   		= PORT."IdPortafolio"
LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" 		CAR		ON  RES."IdCarteraCustodia" 	= CAR."IdCartera" 
INNER JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipo" ET ON ET."IdTipoEvento" = 'MONTO_ACTUAL_COMPRA' AND ET."Estado" ='V'
WHERE 	OP."IdEstado"  		   	   != 	'ANU'
AND 	OP."IdTipoProducto"  		=	'MERF'
AND 	OP."IdTipoOperacion"  		= 	'COMTER'
AND 	OP ."FechaOperacion"  		= 	:FechaContable 
AND 	OP."IdEmpresa" 				= 	:IdEmpresa
AND 	CAR."DocumentoIdentidad" 	= 	:DocumentoIdentidad
UNION ALL
SELECT 
	TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion",
	CAR."IdCartera" AS "IdMovimiento"
	,'MONTO_ACTUAL_COMPRA_DETALLE' AS "IdEvento"
	,'Monto a Tasa Compra Actual Detalle' AS "Nombre"
	,RES."MontoValorizado" AS "MontoEvento"
	,CAR."IdMonedaSistema" AS "CodigoMoneda"
FROM "BSCL.Mesa.BD::MES.Operacion" OP
LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" RES ON OP."IdOperacion" = RES."IdOperacion"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" PORT ON OP."IdPortafolio" = PORT."IdPortafolio"
LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" CAR ON RES."IdCarteraCustodia" = CAR."IdCartera"
INNER JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipo" ET ON ET."IdTipoEvento" = 'MONTO_ACTUAL_COMPRA_DETALLE' AND ET."Estado" ='V'
WHERE OP."IdEstado" != 'ANU'
AND OP."IdTipoProducto" = 'MERF'
AND OP."IdTipoOperacion" = 'COMTER'
AND OP ."FechaOperacion" = :FechaContable
AND OP."IdEmpresa" = :IdEmpresa
AND CAR."DocumentoIdentidad" = :DocumentoIdentidad;