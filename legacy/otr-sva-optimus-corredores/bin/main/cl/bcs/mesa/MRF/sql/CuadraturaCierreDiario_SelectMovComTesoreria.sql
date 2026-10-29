SELECT   OP."IdOperacion"
		,OP."FolioOperacion"
		,OP."RazonSocial"
		,OP."MontoOperacion"
		,OP."MontoOperacionMoneda"
		,OP."IdMoneda"
		,OP."IdMonedaLiquidacion"
		,OP."IdMesaOrigen"
		,OP."IdEstado"
		---------------
		,CD."IdEstado" AS "IdEstadoTes"
		,CASE WHEN CD."IdEstado" = 'LIQ' THEN 'OK' ELSE 'NO_OK' END AS "Validacion" 
  FROM "BSCL.Mesa.BD.Vistas::ConsultarOperaciones" OP
 INNER JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" as IE on IE."IdOperacion"= OP."IdOperacion"  AND  IE."IdTipoProducto"= OP."IdTipoProducto"
 INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalleOrigen" as CDO ON IE."IdIngresoEgreso" = CDO."IdIngresoEgreso"
 INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" as CD on CD."IdComprobanteDetalle" = CDO."IdComprobanteDetalle"
 INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" as C on C."IdComprobante" = CD."IdComprobante"
  WHERE IE."IdTipoProducto"='MERF'AND TO_VARCHAR(C."FechaLiquidacion",'YYYY-MM-DD') = :Fecha AND OP."IdEmpresa" = :IdEmpresa 
  AND OP."IdEstado"<>'PEN' AND CD."IdEstado" <> 'ANU'; 