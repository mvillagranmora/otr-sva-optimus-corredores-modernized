SELECT 
	CAR."IdCartera", CAR."FolioCartera", CAR."FolioCarteraRelacionada", CAR."INGMontoATasaCompraActual",CAR."PlazoEnDiasOperacion", 
	SUB."IdSubyacente", SUB."FolioSubyacente"
	
FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR 
INNER JOIN "BSCL.Custodia.BD::CUS.Subyacente" AS SUB ON (SUB."FolioCarteraRelacionada" = CAR."FolioCartera")
WHERE CAR."FolioCarteraRelacionada" = :FolioPrestamo
AND   CAR."IdEmpresa"               = :IdEmpresa
AND   CAR."IdTipoInstrumento" 		= 'EMP'
AND   CAR."IdEstado"				= 'VIG'
AND   SUB."IdEstado"				= 'VIG';