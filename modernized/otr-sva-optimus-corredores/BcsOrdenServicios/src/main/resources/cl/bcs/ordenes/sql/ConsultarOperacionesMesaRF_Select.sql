SELECT  
 TO_CHAR(CO."FechaMovimiento" , 'YYYY-MM-DD') 	AS "FechaOperacion"
,CO."DocumentoIdentidad"  						AS "Rut"
,CO."IdCliente"           						AS "Cliente"
,CO."NombreCompleto"           				AS "NombreCliente"
,PO."CodigoPortafolio" 	   						AS "Portafolio"
,CO."IdClasificacionTipoInstrumento" 			AS "Mercado"
,CO."IdTipoInstrumento"  						AS "TipoInstrumento"
,CO."MontoValorizado"     						AS "Monto"
,CO."Cantidad"									AS "Cantidad"
,CO."IdMoneda" 
,CO."Precio"		   
,CO."Tasa"
,0.0                    					AS"Paridad"		
,0.0                    					AS"PrecioPromedio"
,0.0                    					AS"ParidadPromedio"
,0.0                    					AS"TasaPromedio"
,TO_DATE(CO."FechaVencimiento")				AS "FechaVencimiento"
,TO_DATE(CO."FechaLiquidacion")				AS "FechaLiquidacion"
FROM "BSCL.Mesa.BD.Vistas::MRF.ConsultarOperaciones" CO
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" PO ON (PO."IdPortafolio" = CO."IdPortafolio")
WHERE CO."IdCliente" = :p_IdCliente
AND TO_CHAR(CO."FechaMovimiento" , 'YYYY-MM-DD') = :p_FechaOperacion; 
