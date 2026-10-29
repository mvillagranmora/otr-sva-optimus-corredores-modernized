SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarPerfilProductos"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarPerfilProductos"  
AS

SELECT 
	PP."IdPerfilProductos",
	PP."DescripcionPerfilProductos",
	PP."IdCategoriaInversionista",
	PP."FechaCreacion",
	PP."FechaModificacion",
	PP."IdEstado" as "EstadoPerfilProductos",
	ESTDET."Nombre" as "NombreEstadoPerfilProductos",
	PP."PorDefecto",
	PP."IdEmpresa",
	PP."EsPerfilPersonalizado"
FROM
	"BSCL.Plataforma.BD::COM.PerfilProductos" as PP
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" as ESTDET
		ON (ESTDET."IdEstado" = PP."IdEstado") ;

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::COM.ConsultarPerfilProductos" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
