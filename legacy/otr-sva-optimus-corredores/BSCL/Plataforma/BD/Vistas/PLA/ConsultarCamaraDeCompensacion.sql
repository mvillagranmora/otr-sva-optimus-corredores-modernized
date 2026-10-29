SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarCamaraDeCompensacion"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarCamaraDeCompensacion"
AS

	SELECT
		   CAM."IdCamaraDeCompensacion"
		  ,CAM."Codigo"
		  ,CAM."Nombre"
		  ,CAM."IdPersona"
		  ,CAM."IdPais"   AS "IdPaisCamara"
		  ,CAM."IdEstado" AS "IdEstadoCamara"
		  ,PER."DocumentoIdentidad"
		  ,PER."RazonSocial"	  
	FROM "BSCL.Plataforma.BD::PLA.CamaraDeCompensacion" AS CAM
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER ON (PER."IdPersona" = CAM."IdPersona");


GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarCamaraDeCompensacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 