-- Obtiene datos de cartera relacionada a la simultanea compra, es decir la que le prestaron al cliente y que no quedó en garantia
SELECT
	 CARREL."IdCartera"
	,CARREL."FolioCartera"
	,CARREL."IdCliente"
	,CARREL."IdPortafolio"
    ,CARREL."IdInstrumento"   
    ,CARREL."IdEmisor"
    ,CARREL."IdConcepto"
    ,CARREL."IdTipoOperacion"
    ,CARREL."IdCondicionLiquidacion"
    ,CARREL."INGCantidad"
    ,CARREL."IdBoveda"
    ,CARREL."IdMonedaInstrumento"
    ,SUB."IdSubyacente"
    
FROM "BSCL.Custodia.BD::CUS.Subyacente" AS SUB
INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR ON (CAR."FolioCartera" = SUB."FolioCarteraRelacionada" AND CAR."IdEstado" = 'VIG') -- Cartera de la simultanea
INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CARREL ON (CARREL."IdCartera" = SUB."IdCarteraRetirada" AND CARREL."IdEstado" = 'VIG') -- Cartera relacionada (prestada y que no esta en garantia)
WHERE  CAR."IdCartera" IN (:IdCarteras)
AND SUB."IdEstado" = 'VIG';	