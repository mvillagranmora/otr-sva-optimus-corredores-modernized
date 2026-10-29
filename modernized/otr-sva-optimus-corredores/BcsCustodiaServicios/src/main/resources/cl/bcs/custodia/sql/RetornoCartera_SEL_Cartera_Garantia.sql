WITH
	Cartera AS (
		SELECT
		DENSE_RANK() over (PARTITION BY "FolioCartera" ORDER BY "FolioCartera", "IdCartera" DESC) AS "CarteraPrincipal"
		,Car."IdCartera"
		, Car."FolioCartera"
		, Car."IdTipoOperacion"
		, Car."IdEstado"
		, Car."IdInstrumento"
		, Car."IdCliente"
		, Car."IdPortafolio"		
		, Car."INGCantidad"
		, Car."INGValorRescate"
		, Car."INGPrecioCompra"
		, Car."INGTasaCompra"
		, Car."INGMontoATasaCompraActual"
		, Car."INGMontoCompraActualMonedaInstrumento"
		, Car."EGRCantidad"
		, Car."EGRValorRescate"
		, Car."NemoSVS"
		, Car."IdClasificacionTipoInstrumento"
		, Car."FechaVencimientoInstrumento"
		, Car."IdCartera" AS "IdCustodiaDestino"
		, Car."IdRegimenFondoMutuo"
		FROM "BSCL.Custodia.BD::CUS.Cartera" AS Car
		WHERE Car."IdEmpresa" = :IdEmpresa
		AND Car."FolioCartera" IN (:FolioCartera)
	)
SELECT * FROM Cartera WHERE "CarteraPrincipal" = 1;	
		  


