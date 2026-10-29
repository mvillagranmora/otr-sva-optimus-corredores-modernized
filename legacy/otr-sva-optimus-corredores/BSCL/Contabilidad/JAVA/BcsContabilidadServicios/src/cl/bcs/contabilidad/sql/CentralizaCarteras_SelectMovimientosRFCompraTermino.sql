SELECT 
TO_BIGINT(:IdTipoCentralizacion) 										AS "IdTipoCentralizacion",	
CAR."IdCartera"															AS "IdMovimiento",
'CARTCOMTER'															AS "IdTipoMovimiento",
OP."IdEmpresa"         													AS "IdEmpresa",		
CAR."FolioCartera"														AS "Numero",
TO_BIGINT(0) 															AS "Linea",
YEAR(:FechaContable) ||'-'|| RIGHT('0' || MONTH(:FechaContable),2) ||'-'|| RIGHT('0' || DAYOFMONTH(:FechaContable),2) AS "Fecha",	
CAR."DocumentoIdentidad"												AS "DocumentoIdentidad",  
CAR."IdPortafolio"														AS "IdPortafolio",
TO_BIGINT(YEAR(CAR."INGFechaOperacion"))								AS "Ejercicio",
RIGHT('0' || MONTH(CAR."INGFechaOperacion"),2) ||'-'|| YEAR(CAR."INGFechaOperacion") 	AS "Periodo",				
OP."MontoOperacion" 													AS "Monto",
CAR."IdMonedaSistema"													AS "IdMoneda",
OP."IdEstado"															AS "Estado"
FROM "BSCL.Mesa.BD::MES.Operacion" OP 
LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" 						RES 	ON 	OP."IdOperacion" 			= RES."IdOperacion"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" 					PORT 	ON 	OP."IdPortafolio"   		= PORT."IdPortafolio"
LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" 		CAR		ON  RES."IdCarteraCustodia" 	= CAR."IdCartera" 
WHERE 	OP."IdEstado"  		   	   != 	'ANU'
AND 	OP."IdTipoProducto"  		=	'MERF'
AND 	OP."IdTipoOperacion"  		= 	'COMTER'
AND 	OP ."FechaOperacion"  		= 	:FechaContable 
AND 	OP."IdEmpresa" 				= 	:IdEmpresa
AND 	CAR."DocumentoIdentidad" 	= 	:DocumentoIdentidad
