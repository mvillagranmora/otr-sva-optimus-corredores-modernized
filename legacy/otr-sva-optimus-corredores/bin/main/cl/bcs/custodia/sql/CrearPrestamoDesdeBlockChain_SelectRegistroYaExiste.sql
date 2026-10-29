SELECT "IdPrestamosPendientes"
	, "FolioSistemaExterno"
FROM "BSCL.Custodia.BD::CUS.PrestamosPendientes"
WHERE "FolioSistemaExterno" IN ( :p_FolioSistemaExterno )
AND "TipoRegistro" = :p_TipoRegistro;
