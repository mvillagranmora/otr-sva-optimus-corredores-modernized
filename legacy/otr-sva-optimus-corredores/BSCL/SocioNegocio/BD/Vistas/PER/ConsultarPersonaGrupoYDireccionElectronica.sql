SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaGrupoYDireccionElectronica";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaGrupoYDireccionElectronica" AS 

SELECT 
	"IdGrupoDireccion" as "IdDireccion",
	"Grupo"				AS "Nombre",
	"IdEstado",
	"IdPersona",
	'G' as "Tipo" -- Grupo
	FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaGrupoDireccionElectronica" 
	UNION
	SELECT
	"IdDireccionElectronica" AS "IdDireccion",
	"Direccion" 		AS "Nombre",
	"IdEstado",
	"IdPersona",
	'I' as "Tipo" -- Individual
	FROM
	"BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica"
	order by "IdDireccion";

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaGrupoYDireccionElectronica" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
