SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::Interfaces.ConsultarInterfaces";

CREATE VIEW "BSCL.Normativa.BD.Vistas::Interfaces.ConsultarInterfaces" (
	"IdInterfaz"
	,"Nombre"
	,"Descripcion"
	,"JsonData"
	,"IdTipoPrograma"
	,"TipoInterfaz"
	,"IdEstado"
)
AS 
SELECT 
	"IdInterfaz"
	,"Nombre"
	,"Descripcion"
	,"JsonData"
	,"IdTipoPrograma"
	,"TipoInterfaz"
	,"IdEstado"
FROM "BSCL.Normativa.BD::Interfaz";

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::Interfaces.ConsultarInterfaces" TO BCS_DESARROLLADOR WITH GRANT OPTION;
