SELECT  
		CAR."FolioCartera" AS "FolioPrestamo",  -- Es el mismo FolioCartera
		SUB."IdInstrumento",
		SUB."IdClasificacionTipoInstrumento",
		SUB."IdTipoInstrumento",
		SUB."IdInstrumento",
		SUB."Nemotecnico",
		SUB."IdMonedaInstrumento",
		
		SUB."IdSubyacente",
		SUB."FolioSubyacente",    
		SUB."FolioCarteraRelacionada",
		SUB."Cantidad",
		SUB."ValorRescate", 
		SUB."IdInstrumento",
		SUB."PrecioCompraOperacionRelacionada",    
		SUB."TasaCompraOperacionRelacionada",
		CAR."IdCartera" AS "IdCarteraOrigen",
		CAR."IdTipoOperacion" AS "IdTipoOperacionOrigen"
    
FROM       "BSCL.Custodia.BD::CUS.Cartera" AS CAR 
INNER JOIN "BSCL.Custodia.BD::CUS.Subyacente" AS SUB ON (SUB."FolioCarteraRelacionada" = CAR."FolioCartera" AND SUB."IdEstado" = 'VIG')
WHERE CAR."FolioCartera" = :FolioPrestamo 
AND   CAR."IdEstado" = 'VIG';