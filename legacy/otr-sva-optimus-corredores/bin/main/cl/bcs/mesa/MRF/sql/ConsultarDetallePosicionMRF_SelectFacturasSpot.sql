SELECT Cmbte."FolioComprobante"  AS "FolioComprobante"
		,CTOP."NombreEvento" AS "TipoOperacion"
		,CMEP."Agrupacion" AS "Agrupacion"
		,CEP."IndAccion" AS "SumaResta"
		,TO_VARCHAR("FechaEmision"  , 'YYYY-MM-DD') AS "FechaComprobante"
		,TO_VARCHAR("FechaLiquidacion", 'YYYY-MM-DD') AS "FechaLiquidacion"
		,CLI."DocumentoIdentidad" AS "DocumentoIdentidad"
		,"MontoLiquidacion"  AS "MontoLiquidacionFinal"
FROM "BSCL.Facturacion.BD::FAC.Comprobante" Cmbte INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON CLI."IdCliente" = Cmbte."IdCliente"
												  INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarTipoOperacionPosicion" CTOP ON Cmbte."IdTipoOperacion" = CTOP."IdTipoOperacion"
           										  INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarMetodoEventoPosicion" CMEP ON ctop."NombreEvento" = CMEP."NombreEvento"
           										  INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarEventoPosicion" CEP ON ctop."NombreEvento" = CEP."NombreOperacion"
WHERE Cmbte."IdTipoComprobante" = 'FEE'
    AND Cmbte."IdComprobanteReferencia" = 0
    AND Cmbte."IdMoneda" IN ('CLP', 'USD')
    AND Cmbte."FechaLiquidacion" = TO_VARCHAR(:p_FechaPosicion, 'YYYY-MM-DD')  
	AND Cmbte."IdTipoOperacion" = :p_IdTipoOperacion	
    AND Cmbte."IdCondicionLiquidacion" <> 'T0'
    AND CEP."IdTipoEvento" = 'A'    
    AND CMEP."IdMetodo" = :p_IdMetodo
    AND Cmbte."IdEstadoComprobante" NOT IN ('ANU', 'ERR', 'PEN');