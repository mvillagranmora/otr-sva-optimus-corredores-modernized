SELECT EPRES."IdEmpresa"
	, CASE WHEN PVIG."TipoCustodia" = 'S' THEN 'C' ELSE 'G' END AS "TipoCustodia"
	, EPRES."ExigirGarantia"
	, EPRES."INGCantidad" AS "Cantidad"
	, EPRES."IdClientePrestamo"
	, EPRES."IdCliente" AS "IdClientePrestamista"
	, PVIG."FolioSistemaExterno"
	, EPRES."FolioEmprestito"
FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenEmprestitoPrestamista" EPRES
INNER JOIN "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoVigente" PVIG ON PVIG."FolioPrestamo" = EPRES."FolioPrestamo"
WHERE ( EPRES."FolioSistemaExterno" = '0' OR EPRES."FolioSistemaExterno" = '' OR EPRES."FolioSistemaExterno" = NULL );