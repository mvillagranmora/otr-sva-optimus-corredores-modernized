UPDATE "BSCL.Plataforma.BD::COM.PerfilComercialBase"
SET "PorDefecto" = 'N'
WHERE "IdCategoriaInversionista" = :IdCategoriaInversionista
	AND "TipoEnrolamiento" = :TipoEnrolamiento
	AND "IdPerfilComercialBase" <> :IdPerfilComercialBase
	AND "IdEmpresa" = :IdEmpresa
