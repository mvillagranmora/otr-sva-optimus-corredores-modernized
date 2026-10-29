SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarTipoOrigenFondo"; 
 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarTipoOrigenFondo"  AS 

	SELECT 
		TORF."IdTipoOrigenFondo",
		TORF."Nombre",
		TORF."ValorDefecto",
		TORF."IdUsuarioCreador",
		TORF."FechaIngreso",
		TORF."FechaCreacion",
		TORF."FechaModificacion",
		TORF."IdEmpresa",
		TORF."IdEstado",
		ESTTORF."Nombre" as "NombreEstado"
	FROM "BSCL.SocioNegocio.BD::CLI.TipoOrigenFondo" TORF
	LEFT JOIN
		"BSCL.Plataforma.BD::PLA.Estado" ESTTORF
			ON ESTTORF."IdEstado" = TORF."IdEstado";
			
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarTipoOrigenFondo" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 




