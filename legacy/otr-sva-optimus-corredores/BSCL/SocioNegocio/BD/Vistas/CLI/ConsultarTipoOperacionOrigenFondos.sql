SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarTipoOperacionOrigenFondos"; 
GO
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarTipoOperacionOrigenFondos"  AS 

	SELECT 
		TOOF."IdTipoOperacionOrigenFondos",
		TOOF."Descripcion",
		TOPE."Nombre",
		TOOF."IdEmpresa",
		TOOF."IdEstado",
		ESTTOOF."Nombre" as "NombreEstado"
	FROM "BSCL.SocioNegocio.BD::CLI.TipoOperacionOrigenFondos" TOOF
	INNER JOIN
		"BSCL.Plataforma.BD::INS.TipoOperacion" TOPE
			ON TOPE."IdTipoOperacion" = TOOF."IdTipoOperacionOrigenFondos"
	INNER JOIN 
		"BSCL.Plataforma.BD::PLA.Estado" ESTTOOF
			ON ESTTOOF."IdEstado" = TOOF."IdEstado";
GO			
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarTipoOperacionOrigenFondos" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 




