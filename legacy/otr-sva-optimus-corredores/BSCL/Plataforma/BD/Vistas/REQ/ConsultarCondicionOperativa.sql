SET SCHEMA "BSCL";
DROP VIEW "BSCL.Plataforma.BD.Vistas::REQ.ConsultarCondicionOperativa";
CREATE VIEW "BSCL.Plataforma.BD.Vistas::REQ.ConsultarCondicionOperativa" AS
	
	SELECT
		CO."IdCondicionOperativa",
		CO."Nombre",
		CO."RequiereAtributo1",
		CO."RequiereAtributo2",
		CO."IdTipoComportamientoDefecto",
		TC."Nombre" as "NombreTipoComportamientoDefecto",
		CO."IdUsuarioCreador",
		CO."FechaCreacion",
		CO."FechaModificacion",  
		CO."IdEmpresa",
		CO."IdEstado",
		ESTCO."Nombre" as "NombreEstado" 	
	FROM 
		"BSCL.Plataforma.BD::REQ.CondicionOperativa" as CO
		LEFT JOIN "BSCL.Plataforma.BD::REQ.TipoComportamiento" as TC
			ON (CO."IdTipoComportamientoDefecto" = TC."IdTipoComportamiento")
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" as ESTCO
			ON(ESTCO."IdEstado" = CO."IdEstado");

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::REQ.ConsultarCondicionOperativa"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;

