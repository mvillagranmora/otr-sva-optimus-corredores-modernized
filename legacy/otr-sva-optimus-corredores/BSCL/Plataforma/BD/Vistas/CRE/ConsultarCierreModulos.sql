SET SCHEMA "BSCL";

DROP VIEW "BSCL.Plataforma.BD.Vistas::CRE.ConsultarCierreModulos";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::CRE.ConsultarCierreModulos" AS
SELECT
	"CREModuloEmpresa"."IdCREModuloEmpresa",
	"CREModuloEmpresa"."IdEmpresa", 	
	"Empresa"."Nombre" AS "NombreEmpresa",
	"CREModuloEmpresa"."IdModulo",  	
	"Modulos"."Nombre",
	"CREModuloEmpresa"."NombreModuloEmpresa",
	"CREModuloEmpresa"."TipoApertura",
	"CREModuloEmpresa"."HoraApertura",
	"CREModuloEmpresa"."TipoCierre", 
	"CREModuloEmpresa"."HoraCierre",
	"CREModuloEmpresa"."IdEstado", 	
	"CREModuloEmpresa"."TipoEstado",
	CASE "CREModuloEmpresa"."TipoEstado"
			WHEN 'A' THEN 'ABIERTO'
			WHEN 'C' THEN 'CERRADO'
			ELSE '' END AS "NombreTipoEstado",
	CASE "CREModuloEmpresa"."TipoEstado" 
			WHEN 'A' THEN "CREModuloEmpresa"."HoraApertura" 
			WHEN 'C' THEN "CREModuloEmpresa"."HoraCierre"
			ELSE '' END AS "Hora"
FROM "BSCL.Plataforma.BD::CRE.ModuloEmpresa" AS "CREModuloEmpresa"
INNER JOIN "BSCL.Plataforma.BD::PLA.Empresa" AS "Empresa" ON "CREModuloEmpresa"."IdEmpresa" = "Empresa"."IdEmpresa"
INNER JOIN "BSCL.Plataforma.BD::PLA.Modulo" AS "Modulos" ON "CREModuloEmpresa"."IdModulo" = "Modulos"."IdModulo";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::CRE.ConsultarCierreModulos" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
