SELECT OPE."FolioOperacion" AS "FolioComprobante"
		,CTOP."NombreEvento" AS "TipoOperacion"	
		,CMEP."Agrupacion" AS "Agrupacion"	
		,CEP."IndAccion" AS "SumaResta"
		,TO_VARCHAR(OPE."FechaOperacion", 'YYYY-MM-DD') AS "FechaComprobante"		
	    ,TO_VARCHAR(OPE."FechaLiquidacion", 'YYYY-MM-DD') AS "FechaLiquidacion"		
		,CLI."DocumentoIdentidad" AS "DocumentoIdentidad"
		,OPE."MontoOperacionInicial" AS "MontoLiquidacionFinal"
FROM	"BSCL.Mesa.BD::MES.Operacion" OPE INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON OPE."IdCliente" = CLI."IdCliente"
										  INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarTipoOperacionPosicion" CTOP ON OPE."IdTipoOperacion" = CTOP."IdTipoOperacion"
  										  INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarMetodoEventoPosicion" CMEP ON ctop."NombreEvento" = CMEP."NombreEvento"
  										  INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarEventoPosicion" CEP ON ctop."NombreEvento" = CEP."NombreOperacion"
WHERE 	(TO_VARCHAR("FechaLiquidacion", 'YYYY-MM-DD') = TO_VARCHAR(:p_FechaPosicion, 'YYYY-MM-DD') OR TO_VARCHAR("FechaCondicionLiquidacion", 'YYYY-MM-DD') = TO_VARCHAR(:p_FechaPosicion, 'YYYY-MM-DD') )
		AND OPE."IdTipoProducto" = :p_IdTipoProducto
		AND OPE."IdTipoOperacion" = :p_IdTipoOperacion
		AND OPE."IdTipoReajuste" = :p_IdTipoReajuste							
		AND OPE."IdMesaOrigen" = :p_IdMesa	
		AND	CTOP."IdTipoEstado" = :p_IdTipoEstado
		AND	( OPE."IdEstado" = 'VIG' OR OPE."IdEstado" = 'PEN' )