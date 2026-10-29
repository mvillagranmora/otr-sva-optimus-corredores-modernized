SET SCHEMA "BSCL";
DROP VIEW "BSCL.Plataforma.BD.Vistas::REQ.ConsultarTipoEtapa";
CREATE VIEW "BSCL.Plataforma.BD.Vistas::REQ.ConsultarTipoEtapa" AS
	
	SELECT
		TE."IdTipoEtapa",
		TE."Nombre",
		TE."IdUsuarioCreador",
		TE."FechaCreacion",
		TE."FechaModificacion", 
		TE."IdEmpresa",
		TE."IdEstado",
		ESTE."Nombre" as "NombreEstado" 	
	FROM 
		"BSCL.Plataforma.BD::REQ.TipoEtapa" as TE
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" as ESTE
			ON(ESTE."IdEstado" = TE."IdEstado");

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::REQ.ConsultarTipoEtapa"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;

