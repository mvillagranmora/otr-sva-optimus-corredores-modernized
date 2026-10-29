SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilProductos"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilProductos"  
AS

SELECT 
	PP."IdPerfilProductos",
	PP."DescripcionPerfilProductos",
	PP."IdCategoriaInversionista",
	PP."FechaCreacion",
	PP."FechaModificacion",
	PP."IdEstado" as "EstadoPerfilProductos",
	PP."PorDefecto",
	PP."IdEmpresa",
	PP."EsPerfilPersonalizado",
	DPP."PermiteRuteo",
	DPP."IdDetallePerfilProductos",
	DPP."IdTipoProducto",
	POR."IdCategoriaInversionista" as "IdCategoriaInversionistaPortafolio",
	TIPPROD."Nombre" as "TipoProducto",
	DPP."IdTipoOperacion",
	CASE DPP."IdTipoOperacion" WHEN '0' THEN 'TODOS' ELSE TIPOP."Nombre" END as "TipoOperacion",
	DPP."IdCanalIngreso",
	CASE DPP."IdCanalIngreso" WHEN '0' THEN 'TODOS' ELSE TCI."Nombre" END as "CanalIngreso",
	DPP."IdMoneda",
	CASE DPP."IdGrupoActivos" WHEN '0' THEN '' ELSE  DPP."IdGrupoActivos" END as "IdGrupoActivos",
	DPP."IdEstado" as "EstadoProducto",
	ESTDET."Nombre" as "NombreEstadoProducto"
FROM
	"BSCL.Plataforma.BD::COM.PerfilProductos" as PP
	INNER JOIN "BSCL.Plataforma.BD::COM.DetallePerfilProductos" as DPP 
		on (PP."IdPerfilProductos" = DPP."IdPerfilProductos")
	LEFT JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarPortafolio" POR
		ON (PP."IdPerfilProductos" = POR."IdPerfilProductosExtra")
	LEFT JOIN  "BSCL.Plataforma.BD::INS.TipoOperacion"  as TIPOP 
		on (TIPOP."IdTipoOperacion" = DPP."IdTipoOperacion")
	LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" as TCI
		on (TCI."IdTipoCanalIngreso" = DPP."IdCanalIngreso")
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoProducto"  as TIPPROD
		on (TIPPROD."IdTipoProducto" = DPP."IdTipoProducto")
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" as ESTDET
		ON (ESTDET."IdEstado" = DPP."IdEstado") ;

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilProductos" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
