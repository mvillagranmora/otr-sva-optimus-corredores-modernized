
SELECT 
     SUB."IdSubyacente"
    ,SUB."FolioSubyacente"
    ,SUB."FolioCarteraRelacionada" 
    ,SUB."IdEstado"
    ,SUB."INGCantidad" AS "Cantidad"
    ,SUB."ValorRescate" 
    ,SUB."IdInstrumento"    
    ,SUB."PrecioCompraOperacionRelacionada"
    ,SUB."TasaCompraOperacionRelacionada"
    ,CAR."IdCartera" AS "IdCarteraOrigen" 
    ,CAR."IdTipoOperacion" AS "IdTipoOperacionOrigen"
    ,CAR."IdCliente"
	,CAR."IdPortafolio"
	,CAR."IdBoveda"
    ,SUB."IdInstrumento"
    ,SUB."IdMonedaInstrumento"
    ,SUB."IdEmisor"
    ,SUB."IdTipoCanalIngreso"
    
    ,SUB."IdCarteraRetirada"
    ,CAR."IdBolsa"
    ,CAR."IdCuentaCustodioPortafolio"  
    ,CAR."MontoCompra"

        
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaSubyacente" AS SUB
INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR ON (CAR."FolioCartera" = SUB."FolioCarteraRelacionada" AND CAR."IdEstado" = 'VIG')
WHERE  CAR."IdCartera" IN (:IdCarteras)
AND SUB."IdEstado" = 'VIG';
 