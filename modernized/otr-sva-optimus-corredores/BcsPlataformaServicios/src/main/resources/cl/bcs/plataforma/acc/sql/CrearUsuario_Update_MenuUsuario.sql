UPDATE U 
	SET U."CodigoMenu" = U1."CodigoMenu"
FROM "BSCL.Plataforma.BD::ACC.Usuario" U, "BSCL.Plataforma.BD::ACC.Usuario" U1
WHERE U."IdUsuario" = :IdUsuario AND U1."IdUsuario" = :IdUsuarioHeredar;