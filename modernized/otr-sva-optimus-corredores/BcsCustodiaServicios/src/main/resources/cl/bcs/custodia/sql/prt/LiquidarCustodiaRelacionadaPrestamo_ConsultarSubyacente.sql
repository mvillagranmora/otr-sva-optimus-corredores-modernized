WITH 
	subyacenteRelacionada AS (
		SELECT SUB."IdInstrumento"
		  FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaSubyacente" AS SUB
		 WHERE SUB."IdEstado" = 'VIG' 
  		   AND SUB."FolioCarteraRelacionada" IN (:FolioCarteraRelacionada)
	)
SELECT 	 CAR."IdEstado"
		,CAR."INGCantidad"
		,CAR."INGValorRescate"
	    ,CAR."IdCartera" 
	    ,CAR."FolioCartera"
	    ,CAR."IdCliente"
		,CAR."IdPortafolio"
		,CAR."IdBoveda"
		,CAR."IdInstrumento"
		,CAR."IdEmisor"
		,CAR."Nemotecnico"
		,CAR."IdMonedaEmision"
		,CAR."IdTipoReajuste"
		,CAR."EsFungible"
		,CAR."CorteMinimo"
		,CAR."NemoSVS"
		,CAR."FechaLiquidacion"
FROM "BSCL.Custodia.BD::CUS.Cartera" as CAR
INNER JOIN subyacenteRelacionada as SUB ON SUB."IdInstrumento" = CAR."IdInstrumento"
WHERE "IdPortafolio" in (:IdPortafolio)
  AND "IdEstado" = 'VIG'