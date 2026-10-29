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
, COALESCE("MontoMonedaPrincipalEquivalente",0) as "MontoMonedaPrincipalEquivalente"
, COALESCE("MontoEquivalente",0) as "MontoEquivalente"
FROM "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot"
WHERE "OperacionNoFacturable" = 'N' AND TO_VARCHAR("FechaOperacion",'YYYY-MM-DD') = :Fecha 
AND "IdEmpresa" = :IdEmpresa
AND "Estado" = 'VIG'