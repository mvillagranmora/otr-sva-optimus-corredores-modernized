SELECT CASE WHEN "IdEntidad" IS NOT NULL THEN 'S' ELSE 'N' END AS "ValidaProducto", "IdEntidad"
FROM "BSCL.Plataforma.BD::ACC.Usuario" WHERE "TipoEntidad" = 'EMP' AND "IdUsuario" = :p_IdUsuario