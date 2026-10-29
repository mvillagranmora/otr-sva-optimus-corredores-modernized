WITH 
 CARTERA AS (
		SELECT
			c."IdCartera",
			c."FolioCartera",
			c."IdCliente",
			c."IdPortafolio",
			c."IdInstrumento",
			c."IdMonedaInstrumento",
			c."IdEmisor",
			c."PrecioCompra",
			c."Cantidad",
			c."ValorRescate",
			c."MontoCompraMonedaInstrumento",
			c."IdMonedaEmision",
			c."FechaVencimientoInstrumento",
			c."IdTipoInstrumento",
			c."EsFungible"
		FROM "BSCL.Custodia.BD::CUS.Cartera" c
		LEFT JOIN "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenEmprestitoVigente" emp
		ON emp."FolioPrestamo" = c."FolioCarteraRelacionada" AND emp."IdCliente" = c."IdCliente" AND emp."IdPortafolio" = c."IdPortafolio" 
		WHERE emp."FolioEmprestito" = :FolioEmprestito 
		  AND c."IdTipoInstrumento" = :IdTipoInstrumento
		  AND c."IdEmpresa" =  :p_IdEmpresa 
		  AND c."IdEstado" = 'VIG'
		UNION ALL
		SELECT 	
			c."IdCartera",
			c."FolioCartera",
			c."IdCliente",
			c."IdPortafolio",
			c."IdInstrumento",
			c."IdMonedaInstrumento",
			c."IdEmisor",
			c."PrecioCompra",
			c."Cantidad",
			c."ValorRescate",
			c."MontoCompraMonedaInstrumento",
			c."IdMonedaEmision",
			c."FechaVencimientoInstrumento",
			c."IdTipoInstrumento",
			c."EsFungible"
		FROM "BSCL.Custodia.BD::CUS.Cartera" AS c
		WHERE c."IdOperacion" = :FolioEmprestito
		  AND c."IdTipoInstrumento" = :IdTipoInstrumento
		  AND c."IdEmpresa" = :p_IdEmpresa 
		  AND c."IdEstado" = 'VIG'
) 
SELECT DISTINCT 
	"IdCartera",
	"FolioCartera",
	"IdCliente",
	"IdPortafolio",
	"IdInstrumento",
	"IdMonedaInstrumento",
	"IdEmisor",
	"PrecioCompra",
	"Cantidad",
	"ValorRescate",
	"MontoCompraMonedaInstrumento",
	"IdMonedaEmision",
	"FechaVencimientoInstrumento",
	"IdTipoInstrumento",
	"EsFungible"
FROM CARTERA