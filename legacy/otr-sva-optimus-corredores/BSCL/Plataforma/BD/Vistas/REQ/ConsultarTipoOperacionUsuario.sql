SET SCHEMA "BSCL";
DROP VIEW "BSCL.Plataforma.BD.Vistas::REQ.ConsultarTipoOperacionUsuario";
CREATE VIEW "BSCL.Plataforma.BD.Vistas::REQ.ConsultarTipoOperacionUsuario" AS
	
	SELECT
		TIPOP."IdTipoOperacionUsuario",
		TIPOP."Nombre",
		TIPOP."IdUsuarioCreador",
		TIPOP."FechaCreacion",
		TIPOP."FechaModificacion",
		TIPOP."IdEmpresa",
		TIPOP."IdEstado",
		ESTTIPOP."Nombre" as "NombreEstado" 	
	FROM 
		"BSCL.Plataforma.BD::REQ.TipoOperacionUsuario" as TIPOP
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" as ESTTIPOP
			ON(ESTTIPOP."IdEstado" = TIPOP."IdEstado");

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::REQ.ConsultarTipoOperacionUsuario"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;

