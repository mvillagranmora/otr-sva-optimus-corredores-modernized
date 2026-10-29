SELECT 
"IdOperacion"
,"FolioOperacion"
,"RazonSocial"
,"MontoOperacion"
,"MontoOperacionMoneda"
,"IdMoneda"
,"IdMonedaLiquidacion"
,"IdMesaOrigen"
,"IdEstado"
,"TipoInformeGenerado"
FROM "BSCL.Mesa.BD.Vistas::ConsultarOperaciones"
WHERE "IdTipoProducto" = 'MERF' AND TO_VARCHAR("FechaOperacion",'YYYY-MM-DD') = :Fecha AND "IdEmpresa" = :IdEmpresa
AND "IdEstado"='VIG' AND "FolioOperacion" >0;