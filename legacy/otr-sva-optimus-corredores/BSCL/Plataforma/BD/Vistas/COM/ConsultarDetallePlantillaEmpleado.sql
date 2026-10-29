SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePlantillaEmpleado"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePlantillaEmpleado"  
AS

SELECT 
	PEMP."IdPlantillaEmpleado",
	PEMP."Descripcion",
	PEMP."FechaCreacion",
	PEMP."FechaModificacion",
	PEMP."IdEstado",
	ESTPEMP."Nombre" as "Estado",
	PEMP."IdTipoCargoEmpleado",
	--PA."PorDefecto",
	PEMP."IdEmpresa",
	PEMP."IdUsuarioCreador",
	DPEMP."IdDetallePlantillaEmpleado",
	DPEMP."IdTipoProducto",
	CASE DPEMP."IdTipoProducto" WHEN '0' THEN 'TODOS' ELSE TPROD."Nombre" END as "TipoProducto",
		--TPROD."Nombre" as "TipoProducto",
	DPEMP."IdTipoOperacion",
	CASE DPEMP."IdTipoOperacion" WHEN '0' THEN 'TODOS' ELSE TOPER."Nombre" END as "TipoOperacion",
	DPEMP."PorcentajeDescuentoComision",
	DPEMP."PorcentajeMargenEmpleado",
	CASE DPEMP."PermiteRuteo" WHEN 'S' THEN 'SI' WHEN 'N' THEN 'NO' ELSE '' END as "NombrePermiteRuteo",
	DPEMP."PermiteRuteo",
	DPEMP."MontoMaximoRuteo",
	DPEMP."MontoMaximoOperacion",
	DPEMP."OrdenDirecta",
	DPEMP."FechaCreacion" as "FechaCreacionDetalle",
	DPEMP."IdEstado" as "IdEstadoDetalle",
	ESTDPEMP."Nombre" as "EstadoDetalle"	
FROM
	"BSCL.Plataforma.BD::COM.PlantillaEmpleado" as PEMP
INNER JOIN 
	"BSCL.Plataforma.BD::COM.DetallePlantillaEmpleado" as DPEMP
	on (PEMP."IdPlantillaEmpleado" = DPEMP."IdPlantillaEmpleado")
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TOPER
	on (TOPER."IdTipoOperacion" = DPEMP."IdTipoOperacion")
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoProducto" as TPROD
	on (TPROD."IdTipoProducto" = DPEMP."IdTipoProducto")
LEFT OUTER JOIN
	"BSCL.Plataforma.BD::PLA.Estado" as ESTPEMP
	on (PEMP."IdEstado" = ESTPEMP."IdEstado")
LEFT OUTER JOIN
	"BSCL.Plataforma.BD::PLA.Estado" as ESTDPEMP
	on (DPEMP."IdEstado" 	= ESTDPEMP."IdEstado")
;
	
	
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePlantillaEmpleado" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
