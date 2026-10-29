SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::TRA.ConsultarHomologacionOperador"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::TRA.ConsultarHomologacionOperador"
AS

SELECT 
	HO."IdHomologacionOperador",
	HO."CodigoOperador",
	HO."IdBolsa",
	BOL."Nombre" as "Bolsa",
	HO."CodigoHomologacion",
	HO."IdEstado",
	HOEST."Nombre" as "Estado",
	HO."FechaCreacion",
	HO."FechaModificacion",
	HO."IdUsuarioCrea",
	HO."IdUsuarioModifica",
	HO."IdEmpresa"	
FROM 
	"BSCL.Plataforma.BD::TRA.HomologacionOperador" as HO
LEFT JOIN "BSCL.Plataforma.BD::INS.Bolsa" as BOL
	ON(BOL."IdBolsa" = HO."IdBolsa")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstado" as HOEST
	ON(HOEST."IdEstado" = HO."IdEstado");

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::TRA.ConsultarHomologacionOperador" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
