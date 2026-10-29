SELECT 	PP."IdPreferenciaPago"
FROM "BSCL.Plataforma.BD::PAG.PreferenciaPago" PP
WHERE PP."IdTipoOperacion" = :p_IdTipoOperacion 
AND PP."IdMedioDePago" = :p_IdMedioDePago 
AND PP."IdMoneda" = :p_IdMoneda
AND PP."IdEmpresa" = :p_IdEmpresa
AND PP."IdEstado" = 'ACT'