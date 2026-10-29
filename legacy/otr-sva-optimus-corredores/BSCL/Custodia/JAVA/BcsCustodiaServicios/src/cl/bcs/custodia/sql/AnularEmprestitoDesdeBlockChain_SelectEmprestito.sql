SELECT	EMP."FolioEmprestito"
	, EMP."FolioSistemaExterno"
	, EMP."DocumentoIdentidad"
FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenEmprestitoVigente" AS EMP
WHERE EMP."IdEmpresa" = :p_IdEmpresa
      AND EMP."FolioSistemaExterno" IN ( :p_FoliosSistemaExterno );