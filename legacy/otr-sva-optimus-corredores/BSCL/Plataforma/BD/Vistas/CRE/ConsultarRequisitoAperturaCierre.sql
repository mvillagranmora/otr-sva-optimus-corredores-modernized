SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::CRE.ConsultarRequisitoAperturaCierre"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::CRE.ConsultarRequisitoAperturaCierre" AS
SELECT
	"CREModuloRequisitoAperturaCierre"."IdModuloRequisitoAperturaCierre",
	"CREModuloRequisitoAperturaCierre"."IdCREModuloEmpresa",
	"CREModuloRequisitoAperturaCierre"."IdTipoRequisito",
	CASE "CREModuloRequisitoAperturaCierre"."IdTipoRequisito" 
		WHEN 'HOR' THEN
			CASE "Tipo" 
				WHEN 'A' THEN "CREModuloRequisitoAperturaCierre"."HoraMinApertura"
				WHEN 'C' THEN "CREModuloRequisitoAperturaCierre"."HoraMinCierre"
				ELSE '' END
		WHEN 'USU' THEN "Usuario"."CodigoUsuario"
		WHEN 'PRF' THEN "Perfil"."Nombre"
		WHEN 'PRO' THEN ''
		ELSE '' END AS "Valor",
	"CREModuloRequisitoAperturaCierre"."Tipo",
	"CREModuloRequisitoAperturaCierre"."Nombre",
	"CREModuloRequisitoAperturaCierre"."IdEstado" 
 FROM "BSCL.Plataforma.BD::CRE.ModuloRequisitoAperturaCierre" AS "CREModuloRequisitoAperturaCierre"
 LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS "Usuario" ON "CREModuloRequisitoAperturaCierre"."IdUsuario" = "Usuario"."IdUsuario"
 LEFT JOIN "BSCL.Plataforma.BD::ACC.Perfil" AS "Perfil" ON "CREModuloRequisitoAperturaCierre"."IdPerfil" = "Perfil"."IdPerfil";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::CRE.ConsultarRequisitoAperturaCierre" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
