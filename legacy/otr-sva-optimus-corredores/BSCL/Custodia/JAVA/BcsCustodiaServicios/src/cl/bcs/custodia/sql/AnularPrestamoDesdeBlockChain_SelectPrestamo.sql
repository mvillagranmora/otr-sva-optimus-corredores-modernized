SELECT
		"FolioPrestamo"
		,"FolioSistemaExterno"
		,CAST("FechaVencimientoPrestamo" AS VARCHAR(10)) AS "FechaVencimientoPrestamo"
		, "FechaPrestamo"
		, "IdEstado"
FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoVigente"
WHERE 	"IdEmpresa" = :p_IdEmpresa
		AND "FolioSistemaExterno" IN (:p_FolioSistemaExterno);