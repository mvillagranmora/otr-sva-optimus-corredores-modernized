SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::Interfaces.ConsultarArchivoInterfaces";

CREATE VIEW "BSCL.Normativa.BD.Vistas::Interfaces.ConsultarArchivoInterfaces" (
	"IdPrograma"
	,"IdInterfaz"
	,"Nombre"
	,"Descripcion"
	,"fechaEjecucion"
	,"RutaSalida"
	,"RutaLog"
)
AS 
SELECT 
	PROGRAMA."IdPrograma"
	,INTERFAZ."IdInterfaz"
	,INTERFAZ."Nombre"
	,INTERFAZ."Descripcion"
	,PROGRAMA."fechaEjecucion"
	,PROGRAMA."RutaSalida"
	,PROGRAMA."RutaLog"
FROM "BSCL.Plataforma.BD::BAT.Programa" PROGRAMA
INNER JOIN "BSCL.Normativa.BD::Interfaz" INTERFAZ ON (PROGRAMA."IdTipoPrograma" = INTERFAZ."IdTipoPrograma")
WHERE INTERFAZ."TipoInterfaz" IN ('MENSUAL','DIARIO') AND PROGRAMA."idEstado" = 'TER';

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::Interfaces.ConsultarArchivoInterfaces" TO BCS_DESARROLLADOR WITH GRANT OPTION;
