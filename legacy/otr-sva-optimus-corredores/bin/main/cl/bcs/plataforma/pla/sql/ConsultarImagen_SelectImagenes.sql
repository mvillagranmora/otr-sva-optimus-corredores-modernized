SELECT "IdEmpresa"
	, "IdImagen"
	, "NombreImagen"
	, "Tipo" 
	, "Extension" 
	, "Version" 
	, "RutaPublica" 
FROM "BSCL.Plataforma.BD::PLA.Imagenes"
WHERE "NombreImagen" IN (:p_NombreImagen) AND "IdEmpresa" = :p_IdEmpresa;