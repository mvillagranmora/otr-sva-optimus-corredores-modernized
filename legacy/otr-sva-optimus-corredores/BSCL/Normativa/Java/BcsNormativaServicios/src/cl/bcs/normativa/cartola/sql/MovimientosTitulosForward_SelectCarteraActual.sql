SELECT
FW."IdCliente" 				as "IdCliente",
FW."IdPortafolio" 				as "IdPortafolio",
TO_TIMESTAMP(FW."FechaOperacion") AS "FEC_MVTO"  ,
TO_DECIMAL(FW."IdOperacion",26,0) AS "SEC_MOVTO"  ,
CASE FW."Accion" WHEN 'COMPRA' THEN 'VENTA FORWARD' ELSE 'COMPRA FORWARD' END AS "operacion"  ,
FW."Nemotecnico" || ' ' || FW."IdMonedaPrincipal" || '/' || FW."IdMonedaSecundaria" AS "NEMO"  ,
'' 								AS "PB"  ,
CAR."INGCantidad"				AS "CANTIDAD"  ,
FW."TipoCambioCierre" 			AS "precioTir"  ,
FW."MontoMonedaSecundaria"		AS "montoNeto"  ,
TO_DECIMAL(0,28,6)				AS "montoTotal"  ,
'' AS "MERCADO"  ,
TO_VARCHAR(FW."FechaVencimiento",'DD-MM-YYYY') AS "fecha_vencimiento",
'' AS "NemoMP"  ,
'' AS "NemoMS"  ,
TO_DECIMAL(FW."FolioOperacion",26,0) AS "folioOper"  
FROM "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWD" AS FW
INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR ON (FW."IdCartera" = CAR."IdCartera")
WHERE 
FW."IdPortafolio" IN (:IdPortafolio)
AND FW."IdEstado" = 'VIG'
AND FW."FechaOperacion" BETWEEN :FechaDesde AND :FechaHasta;