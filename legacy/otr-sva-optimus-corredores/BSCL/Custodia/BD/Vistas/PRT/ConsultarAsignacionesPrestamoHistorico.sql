SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Custodia.BD.Vistas::PRT.ConsultarAsignacionesPrestamoHistorico";

CREATE VIEW "BSCL.Custodia.BD.Vistas::PRT.ConsultarAsignacionesPrestamoHistorico" AS
WITH 
Cartera AS (
	SELECT
		prt."FolioCartera" AS "FolioCarteraPrestamo" ,
		prt."IdEmpresa" ,
		prt."EGRCantidad" AS "CantidadPrestamo", 
		prt."EGRFechaOperacion" AS "FechaTerminoPrestamo",
		prt."IdEstado", 		
		emp."FolioCartera" AS "FolioCarteraPrestamista" ,
		emp."EGRCantidad" AS "CantidadEmprestito",
		emp."EGRFechaOperacion" AS "FechaTerminoEmprestito",
		emp."IdEstado",
		CASE WHEN prt."EGRFechaOperacion" = emp."EGRFechaOperacion" THEN emp."EGRCantidad" 
			 WHEN prt."EGRFechaOperacion" > emp."EGRFechaOperacion" THEN 0 END AS "CantidadLiquidada"
	FROM "BSCL.Custodia.BD::CUS.Cartera" AS prt
	LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" AS emp ON
		prt."IdEmpresa" = emp."IdEmpresa"
		AND prt."FolioCartera" = emp."FolioCarteraRelacionada"
		AND prt."EGRFechaOperacion" = emp."EGRFechaOperacion"
		AND emp."IdTipoInstrumento" IN ('EMP','ERF')
		AND emp."IdEstado" <> 'VIG'
		AND emp."FolioCarteraRelacionada" > 0
	WHERE 
		prt."IdTipoInstrumento" IN ('PRT','PRF')
		AND prt."IdEstado" <> 'VIG'
	ORDER BY prt."FolioCartera"
)
SELECT 
	"FolioCarteraPrestamo", 
	"IdEmpresa",
	SUM("CantidadPrestamo") AS "Cantidad",
	COALESCE(SUM("CantidadLiquidada"),0) AS "CantidadAsignada" ,
	SUM("CantidadPrestamo") - COALESCE(SUM("CantidadLiquidada"),0) AS "CantidadPorAsignar"
FROM Cartera
GROUP BY "IdEmpresa", "FolioCarteraPrestamo"
WITH READ ONLY;

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::PRT.ConsultarAsignacionesPrestamoHistorico" TO BCS_DESARROLLADOR WITH GRANT OPTION; 