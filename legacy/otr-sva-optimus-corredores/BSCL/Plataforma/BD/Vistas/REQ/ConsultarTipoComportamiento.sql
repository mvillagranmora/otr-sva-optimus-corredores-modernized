SET SCHEMA "BSCL";
DROP VIEW "BSCL.Plataforma.BD.Vistas::REQ.ConsultarTipoComportamiento";
CREATE VIEW "BSCL.Plataforma.BD.Vistas::REQ.ConsultarTipoComportamiento" AS
	
	SELECT
		TCO."IdTipoComportamiento",
		TCO."Nombre",
		TCO."IdUsuarioCreador",
		TCO."FechaCreacion",
		TCO."FechaModificacion",
		TCO."IdEmpresa",
		TCO."IdEstado", 	
		ESTCO."Nombre" as "NombreEstado" 	
	FROM 
		"BSCL.Plataforma.BD::REQ.TipoComportamiento" as TCO
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" as ESTCO
			ON(ESTCO."IdEstado" = TCO."IdEstado");

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::REQ.ConsultarTipoComportamiento"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;

