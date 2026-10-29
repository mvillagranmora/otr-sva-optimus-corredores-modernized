SELECT 
TO_CHAR(CO."FechaOperacion" , 'YYYY-MM-DD')	AS "FechaOperacion"
,CO."DocumentoIdentidad"  					    AS "Rut"
,CO."IdCliente"           						AS "Cliente"
,CO."NombreCompleto"                           AS "NombreCliente"       
,PO."CodigoPortafolio" 	  						AS "Portafolio"
,CO."IdClasificacionTipoInstrumento" 			AS "Mercado"
,CO."IdTipoInstrumento"   						AS "TipoInstrumento"
,CO."INGMontoMonedaPrincipal" 					AS "Monto"
,CO."INGCantidad"		    					AS "Cantidad"
,CO."IdMonedaPrincipal" 						AS "IdMoneda"
,CO."Precio"		  		
,CO."TasaCompra"								AS "Tasa"
,CO."ParidadCierre"        					AS	"Paridad"		
,0.0                    					AS	"PrecioPromedio"
,0.0                    					AS	"ParidadPromedio"
,0.0                   						AS	"TasaPromedio"
,TO_DATE(CO."FechaVencimiento")				AS "FechaVencimiento"
,TO_DATE(CO."FechaLiquidacion")				AS "FechaLiquidacion"
FROM "BSCL.Mesa.BD.Vistas::FWD.ConsultarOperaciones" CO
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" PO ON (PO."IdPortafolio" = CO."IdPortafolio")
WHERE CO."IdCliente" = :p_IdCliente
AND TO_CHAR(CO."FechaOperacion" , 'YYYY-MM-DD') = :p_FechaOperacion
AND CO."IdTipoProducto" = 'MEFW';
