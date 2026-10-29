SELECT 
"IdOperacion"
,"FolioOperacion"
,"NombreCliente" AS "RazonSocial"
,"MontoMonedaPrincipal" AS "MontoOperacion"
,"MontoMonedaSecundaria" AS "MontoOperacionMoneda"
,"IdMonedaPrincipal" AS "IdMoneda"
,"IdMonedaSecundaria" AS "IdMonedaLiquidacion"
,"IdMesa" AS "IdMesaOrigen"
,"Estado" AS "IdEstado"
FROM "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot"
WHERE "OperacionNoFacturable" = 'N' AND TO_VARCHAR("FechaOperacion",'YYYY-MM-DD') = :Fecha AND "IdEmpresa" = :IdEmpresa