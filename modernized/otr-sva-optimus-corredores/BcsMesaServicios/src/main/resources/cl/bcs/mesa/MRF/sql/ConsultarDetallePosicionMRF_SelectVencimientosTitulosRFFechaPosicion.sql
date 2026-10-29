SELECT CAR."FolioComprobante" AS "FolioComprobante"
		,CTOP."NombreEvento" AS "TipoOperacion"
		,CMEP."Agrupacion" AS "Agrupacion"
		,CEP."IndAccion" AS "SumaResta"
		,TO_VARCHAR(CAR."FechaComprobante", 'YYYY-MM-DD') AS "FechaComprobante"
		,TO_VARCHAR(CAR."FechaLiquidacion", 'YYYY-MM-DD') AS "FechaLiquidacion"
		,CLI."DocumentoIdentidad" AS "DocumentoIdentidad"
		,CAR."INGValorRescate" AS "MontoLiquidacionFinal"
FROM	"BSCL.Custodia.BD::CUS.Cartera" CAR INNER JOIN  "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON  CLI."IdCliente" = CAR."IdCliente" 
											INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarTipoOperacionPosicion" CTOP ON CAR."IdTipoOperacion" = CTOP."IdTipoOperacion"
   										    INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarMetodoEventoPosicion" CMEP ON ctop."NombreEvento" = CMEP."NombreEvento"
   										    INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarEventoPosicion" CEP ON ctop."NombreEvento" = CEP."NombreOperacion"
WHERE	TO_VARCHAR("FechaVencimientoInstrumento", 'YYYY-MM-DD') = TO_VARCHAR(:p_FechaPosicion, 'YYYY-MM-DD')
		AND CAR."IdTipoReajuste" = :p_IdTipoReajuste							 
		AND CAR."IdClasificacionTipoInstrumento" IN ('RF','IF')
		AND CAR."IdEstado" = 'VCD'
		AND CAR."IdPortafolio" = :p_IdPortafolio
