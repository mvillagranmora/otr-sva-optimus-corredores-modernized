SELECT 	
   	TO_BIGINT(:IdTipoCentralizacion)	AS "IdTipoCentralizacion"
	,"IdCartera" 						AS "IdMovimiento"
	,'INTERESES' 						AS "IdEvento"	
	,'Intereses' 						AS "Nombre"	
	,"INGMontoInteresCompraAnterior"	AS "MontoEvento"
	,"IdMonedaSistema"					AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
WHERE ConsultarCartera."IdEmpresa" = :IdEmpresa 
	AND (
    	("INGFechaOperacion" <= :FechaContable AND "EGRCantidad" = 0) OR("INGFechaOperacion" <= :FechaContable AND "EGRCantidad" <> 0 AND "INGFechaOperacion" > :FechaContable)
    	)
	AND ConsultarCartera."IdTipoOperacion"				IN ('TDEPLA','TAUTDI')
   	AND ConsultarCartera."IdClasificacionTipoInstrumento" 	= 'IF' 	        
--    AND ConsultarCartera."DocumentoIdentidad" 				= :DocumentoIdentidad
    AND ConsultarCartera."IdEstado"							= 'VIG'
    AND ConsultarCartera."INGMontoInteresCompraAnterior" <> 0;   	
