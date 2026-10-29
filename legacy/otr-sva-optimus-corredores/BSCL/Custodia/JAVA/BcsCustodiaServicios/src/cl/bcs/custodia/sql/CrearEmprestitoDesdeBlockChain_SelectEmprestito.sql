SELECT	EMP."FolioPrestamo"
		,EMP."FolioEmprestito"
		,EMP."INGCantidad"
		,EMP."FolioSistemaExterno"
		,EMP."DocumentoIdentidad"
FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenEmprestitoVigente" AS EMP
WHERE EMP."IdEmpresa" = :p_IdEmpresa
      AND EMP."FolioSistemaExterno" = :p_FolioSistemaExterno
      AND EMP."DocumentoIdentidad" IN ( :p_DocumentoIdentidad);
      