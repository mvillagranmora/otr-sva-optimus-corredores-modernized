SELECT
     SUB."IdSubyacente"
    ,SUB."FolioSubyacente"
    ,SUB."FolioCarteraRelacionada" 
    ,SUB."IdEstado"
    ,SUB."INGCantidad" as "Cantidad"
    ,SUB."ValorRescate" 
    ,SUB."IdInstrumento"    
    ,SUB."PrecioCompraOperacionRelacionada"    
    ,SUB."TasaCompraOperacionRelacionada"  
    ,SUB."IdTipoCanalIngreso"
    ,SUB."IdEmisor"
    
    ,CAR."IdCartera" AS "IdCarteraOrigen" 
    ,CAR."IdTipoOperacion" AS "IdTipoOperacionOrigen"
    
    
    ,SUB."IdCarteraRetirada"
    
    
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaSubyacente" AS SUB
INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR ON (CAR."FolioCartera" = SUB."FolioCarteraRelacionada" AND CAR."IdEstado" = 'VIG')
WHERE SUB."IdEstado" = 'VIG' 
AND SUB."FolioCarteraRelacionada" = :FolioCarteraRelacionada
AND SUB."IdInstrumento" = :IdInstrumento;