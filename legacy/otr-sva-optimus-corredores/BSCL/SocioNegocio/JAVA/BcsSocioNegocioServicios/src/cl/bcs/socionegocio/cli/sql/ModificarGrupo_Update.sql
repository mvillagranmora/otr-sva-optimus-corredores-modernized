UPDATE "BSCL.SocioNegocio.BD::CLI.Grupo"
	SET "IdEmpresa"= :p_IdEmpresa, 
		"Nombre"   = :p_Nombre, 
		"IdEstado" = :p_IdEstado, 
		"Codigo"   = :p_Codigo
WHERE "IdGrupo"    = :p_IdGrupo;