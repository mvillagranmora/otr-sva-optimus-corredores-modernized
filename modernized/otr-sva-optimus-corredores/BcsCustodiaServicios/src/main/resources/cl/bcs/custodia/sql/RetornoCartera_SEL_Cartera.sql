/********************************************************** 
Proposito	: Script para la selecci�n de la tabla Cartera
autor		: NRM
fecha		: 22/11/2017
***********************************************************/

WITH
	Cartera AS (
		SELECT
			  C."IdCartera"
			, C."FolioCartera"
			, C."IdTipoOperacion"
			, C."IdEstado"
			, C."IdInstrumento"
			, C."IdCliente"
			, C."IdPortafolio"
			, C."INGCantidad"
			, C."INGValorRescate"
			, C."INGPrecioCompra"
			, C."INGTasaCompra"
			, C."INGMontoATasaCompraActual"
			, C."INGMontoCompraActualMonedaInstrumento"
			, C."EGRCantidad"
			, C."EGRValorRescate"		
			, C."NemoSVS"
			, C."IdClasificacionTipoInstrumento"
			, C."FechaVencimientoInstrumento"
			, C."IdRegimenFondoMutuo"
			, C."IdEmisor"
			, DENSE_RANK() over (PARTITION BY C."FolioCartera" ORDER BY C."IdCartera" DESC) AS "CarteraPrincipal"
			FROM "BSCL.Custodia.BD::CUS.Cartera" AS C
			WHERE C."IdEmpresa" = :IdEmpresa
			AND C."FolioCartera" IN (:FolioCartera)
	)
SELECT 	  Car."IdCartera"
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
		, Car."IdRegimenFondoMutuo"
		, Car."IdEmisor"		
FROM Cartera AS Car WHERE "CarteraPrincipal" = 1;

