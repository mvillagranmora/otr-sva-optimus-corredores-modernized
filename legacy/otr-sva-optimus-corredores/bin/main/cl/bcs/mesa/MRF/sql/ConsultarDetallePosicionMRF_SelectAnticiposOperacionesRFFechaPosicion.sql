SELECT OPE."FolioOperacion" AS "FolioComprobante" 
		,CTOP."NombreEvento" AS "TipoOperacion"
		,CMEP."Agrupacion" AS "Agrupacion"
		,CEP."IndAccion" AS "SumaResta"
		,TO_VARCHAR(OPE."FechaOperacion", 'YYYY-MM-DD') AS "FechaComprobante"
		,TO_VARCHAR(OPE."FechaLiquidacion", 'YYYY-MM-DD') AS "FechaLiquidacion"
		,CLI."DocumentoIdentidad" AS "DocumentoIdentidad"
		,CASE WHEN :p_IdTipoEstado = 'ANT' THEN OPE."MontoOperacionFinalAnticipo" ELSE OPE."MontoOperacionFinal" END AS "MontoLiquidacionFinal"
FROM	"BSCL.Mesa.BD::MES.Operacion" OPE INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON CLI."IdCliente" = OPE."IdCliente"  
										  INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarTipoOperacionPosicion" CTOP ON OPE."IdTipoOperacion" = CTOP."IdTipoOperacion"
   										  INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarMetodoEventoPosicion" CMEP ON ctop."NombreEvento" = CMEP."NombreEvento"
   										  INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarEventoPosicion" CEP ON ctop."NombreEvento" = CEP."NombreOperacion"
WHERE	TO_VARCHAR("FechaLiquidacion", 'YYYY-MM-DD') = TO_VARCHAR(:p_FechaPosicion, 'YYYY-MM-DD')
		AND OPE."IdTipoProducto" = :p_IdTipoProducto
		AND OPE."IdTipoOperacion" = :p_IdTipoOperacion
		AND OPE."IdTipoReajuste" = :p_IdTipoReajuste							
		AND OPE."IdMesaOrigen" = :p_IdMesa 
		AND OPE."IdEstado" = 'ANT'
		AND	CTOP."IdTipoEstado" = :p_IdTipoEstado