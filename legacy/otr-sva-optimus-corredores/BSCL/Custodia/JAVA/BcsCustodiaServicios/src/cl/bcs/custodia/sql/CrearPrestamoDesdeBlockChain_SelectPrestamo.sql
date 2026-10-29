SELECT
		"FolioPrestamo"
		,"FolioSistemaExterno"
FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoVigente"
WHERE 	"IdEmpresa" = :p_IdEmpresa
	AND "FolioSistemaExterno" IN ( :p_FolioSistemaExterno )