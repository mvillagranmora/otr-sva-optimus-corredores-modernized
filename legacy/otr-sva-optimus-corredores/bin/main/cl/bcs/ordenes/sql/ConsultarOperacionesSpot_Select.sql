SELECT
TO_CHAR(CO."FechaMovimiento" , 'YYYY-MM-DD')	AS "FechaOperacion"
,CO."DocumentoIdentidad"  						AS "Rut"
,CO."IdCliente"           						AS "Cliente"
,CO."NombreCompleto" 							AS "NombreCliente"
,PO."CodigoPortafolio" 	  						AS "Portafolio"
,CO."IdClasificacionTipoInstrumento" 			AS "Mercado"
,CO."IdTipoInstrumento"   						AS "TipoInstrumento"
,CO."MontoMonedaSecundaria"					AS "Monto"
,CO."Cantidad"		    						
,CO."IdMoneda"
,CO."Precio"		  		
,CO."TasaCompra"								AS "Tasa"
,CO."ParidadCierre"        					AS	"Paridad"		
,0.0                    					AS	"PrecioPromedio"
,0.0                    					AS	"ParidadPromedio"
,0.0                    					AS	"TasaPromedio"
,TO_DATE('0001-01-01','YYYY-MM-DD')  		AS 	"FechaVencimiento"
,TO_DATE(CO."FechaLiquidacion")				AS "FechaLiquidacion"
FROM "BSCL.Mesa.BD.Vistas::SPT.ConsultarOperaciones" CO
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" PO ON (PO."IdPortafolio" = CO."IdPortafolio")
WHERE CO."IdCliente" = :p_IdCliente
AND TO_CHAR(CO."FechaMovimiento" , 'YYYY-MM-DD') = :p_FechaOperacion;


