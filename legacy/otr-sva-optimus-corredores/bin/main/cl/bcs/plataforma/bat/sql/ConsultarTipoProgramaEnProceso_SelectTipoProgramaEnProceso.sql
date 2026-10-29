SELECT 
	PROG."IdPrograma"
	, PROG."IdTipoPrograma"
	, PROG."IdUsuario"
	, COALESCE(PROG."CodigoUsuario", '*Sin Usuario Asignado*') AS "CodigoUsuario"
	, PROG."IdEstado" 
FROM "BSCL.Plataforma.BD.Vistas::BAT.ConsultarPrograma" AS PROG
WHERE PROG."IdTipoPrograma" IN (:idTipoProgramas) AND PROG."IdEstado" IN ('PEN','ASI');