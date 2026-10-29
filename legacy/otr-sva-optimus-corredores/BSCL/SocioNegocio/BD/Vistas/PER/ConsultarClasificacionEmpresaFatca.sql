SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarClasificacionEmpresaFatca"; 

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarClasificacionEmpresaFatca" AS
	SELECT 
	"IdClasificacionEmpresaFatca",
	"Nombre",
	"PorDefecto",
 	"UsaNumeroGIIN"
 	FROM "BSCL.SocioNegocio.BD::PER.ClasificacionEmpresaFatca"
WITH READ ONLY; 

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarClasificacionEmpresaFatca" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
