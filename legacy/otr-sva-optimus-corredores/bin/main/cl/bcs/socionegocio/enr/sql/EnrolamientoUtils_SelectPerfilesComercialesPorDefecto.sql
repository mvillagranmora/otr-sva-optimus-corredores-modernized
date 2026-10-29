SELECT "IdPerfilComercialBase"
,"IdCategoriaInversionista"
,"TipoEnrolamiento"
FROM "BSCL.Plataforma.BD::COM.PerfilComercialBase"
WHERE
	"IdEmpresa" = :IdEmpresa
  AND "PorDefecto" = 'S';