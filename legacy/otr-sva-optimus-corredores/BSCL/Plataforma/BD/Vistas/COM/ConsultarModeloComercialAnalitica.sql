SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarModeloComercialAnalitica"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarModeloComercialAnalitica" AS
SELECT
	"IdPerfilComercialBase",
	"DescripcionPerfilComercialBase",
	"IdCategoriaInversionista",
	"IdPerfilProductos",
	"IdPerfilCobros",
	"IdPerfilLimites",
	"IdPerfilCondicionesOperativas",
	TO_TIMESTAMP("FechaCreacion") AS "FechaCreacion",
	TO_TIMESTAMP("FechaModificacion") AS "FechaModificacion",
	"IdEstado",
	"IdEmpresa",
	"PorDefecto",
	"IdModeloFacturacion"
FROM "BSCL.Plataforma.BD::COM.PerfilComercialBase";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::COM.ConsultarModeloComercialAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
