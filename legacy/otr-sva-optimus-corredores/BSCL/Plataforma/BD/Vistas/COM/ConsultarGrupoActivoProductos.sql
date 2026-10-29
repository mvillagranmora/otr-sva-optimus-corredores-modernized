SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarGrupoActivoProductos"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarGrupoActivoProductos" AS
SELECT * FROM (
	SELECT
		"GA"."IdInstrumento",
		"GA"."Nemotecnico",
		"INST"."ISIN",
		"INST"."IdMoneda",
		"GA"."IdGrupoActivos",
		"INST"."RazonSocial",
		"GA"."DescripcionGrupoActivos",
		"GA"."IdTipoGrupoActivos",
		"GA"."FechaCreacion",
		"GA"."FechaModificacion",
		"GA"."IdEstado",
		"GA"."PorDefecto",
		"GA"."IdEmpresa",
		"GA"."IdDetalleGrupoActivos",
		"GA"."IdTipoInstrumento",
		"GA"."CodigoTipoInstrumento",
		"GA"."NombreTipoInstrumento",
		"GA"."FechaCreacionDetalle",
		"GA"."IdEstadoDetalle",
		"GA"."EstadoDetalle",
		"CDPP"."IdPerfilProductos",
        "CDPP"."DescripcionPerfilProductos",
        "CDPP"."EstadoPerfilProductos",
        "CDPP"."EsPerfilPersonalizado",
        "CDPP"."IdCategoriaInversionistaPortafolio",
        "CDPP"."EstadoProducto"
	FROM "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilProductos"  AS "CDPP"
	INNER JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarGrupoActivos"	AS "GA"
		ON "GA"."IdGrupoActivos"="CDPP"."IdGrupoActivos"
		AND "GA"."IdTipoGrupoActivos" = 'INCLUYENTE'
		AND "CDPP"."EstadoProducto" = 'ACT'
	INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS "INST"
		ON "INST"."IdInstrumento" = "GA"."IdInstrumento"
	--WHERE "IdPerfilProductos"  = 'ARRIESGADO FULL' and 	"GA"."IdTipoInstrumento" = 'ACN'
	GROUP BY "GA"."IdInstrumento",
			"GA"."Nemotecnico",
			"INST"."ISIN",
			"INST"."IdMoneda",
			"GA"."IdGrupoActivos",
			"INST"."RazonSocial",
			"GA"."DescripcionGrupoActivos",
			"GA"."IdTipoGrupoActivos",
			"GA"."FechaCreacion",
			"GA"."FechaModificacion",
			"GA"."IdEstado",
			"GA"."PorDefecto",
			"GA"."IdEmpresa",
			"GA"."IdDetalleGrupoActivos",
			"GA"."IdTipoInstrumento",
			"GA"."CodigoTipoInstrumento",
			"GA"."NombreTipoInstrumento",
			"GA"."FechaCreacionDetalle",
			"GA"."IdEstadoDetalle",
			"GA"."EstadoDetalle",
			"CDPP"."IdPerfilProductos",
	        "CDPP"."DescripcionPerfilProductos",
	        "CDPP"."EstadoPerfilProductos",
	        "CDPP"."EsPerfilPersonalizado",
	        "CDPP"."IdCategoriaInversionistaPortafolio",
	        "CDPP"."EstadoProducto"
	       -- ORDER BY 1
	UNION ALL
	SELECT 
		"INST"."IdInstrumento",
		"INST"."Nemotecnico",
		"INST"."ISIN",
		"INST"."IdMoneda",
		'' as "IdGrupoActivos",
		"INST"."RazonSocial",
		
		'' as "DescripcionGrupoActivos",
		'' as "IdTipoGrupoActivos",
		'2018-04-10' as  "FechaCreacion",
		'2018-04-10' as"FechaModificacion",
		'ACT' as "IdEstado",
		'test' as "PorDefecto",
		0 AS "IdEmpresa",
		0 as "IdDetalleGrupoActivos",
		"TIPO"."IdTipoInstrumento",
		"TIPO"."CodigoTipoInstrumento",
		"TIPO"."NombreTipoInstrumento",
		'2018-04-10' as "FechaCreacionDetalle",
		'' as "IdEstadoDetalle",
		'activo' as "EstadoDetalle",

		"CDPP"."IdPerfilProductos",
        "CDPP"."DescripcionPerfilProductos",
        "CDPP"."EstadoPerfilProductos",
        "CDPP"."EsPerfilPersonalizado",
        "CDPP"."IdCategoriaInversionistaPortafolio",
        "CDPP"."EstadoProducto"
	FROM "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilProductos"  AS "CDPP"
	inner join "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoInstrumento" as "TIPO"
		ON "CDPP"."IdTipoProducto" = "TIPO"."IdTipoProducto"
		AND "CDPP"."EstadoProducto" = 'ACT'
	INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS "INST"
		ON "INST"."IdTipoInstrumento" = "TIPO"."IdTipoInstrumento"
		--	WHERE  "IdPerfilProductos"  = 'ARRIESGADO FULL'
	where not exists (
		select 1				
		FROM "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilProductos"  AS "P"
		INNER JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarGrupoActivos"	AS "G"
			ON	"G"."IdGrupoActivos"="P"."IdGrupoActivos"
		INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS "I"
			ON  "I"."IdInstrumento" = "INST"."IdInstrumento"
		WHERE "P"."IdPerfilProductos" = "CDPP"."IdPerfilProductos"
		and "G"."IdTipoInstrumento" = "INST"."IdTipoInstrumento"
		and "P"."EstadoProducto" = 'ACT'
	) 
	GROUP BY "INST"."IdInstrumento",
	"INST"."Nemotecnico",
	"INST"."ISIN",
	"INST"."IdMoneda",
	"INST"."RazonSocial",
	"TIPO"."IdTipoInstrumento",
	"TIPO"."CodigoTipoInstrumento",
	"TIPO"."NombreTipoInstrumento",
	"CDPP"."IdPerfilProductos",
    "CDPP"."DescripcionPerfilProductos",
    "CDPP"."EstadoPerfilProductos",
    "CDPP"."EsPerfilPersonalizado",
    "CDPP"."IdCategoriaInversionistaPortafolio",
    "CDPP"."EstadoProducto"
    --ORDER BY 1
);-- WHERE  "IdPerfilProductos"  = 'ARRIESGADO FULL' and 	"IdTipoInstrumento" = 'FFM';

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::COM.ConsultarGrupoActivoProductos" TO BCS_DESARROLLADOR WITH GRANT OPTION; 