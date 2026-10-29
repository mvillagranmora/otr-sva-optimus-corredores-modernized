SET SCHEMA "BSCL"; 
GO 
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarDetalleDeclaracionOrigenFondos"; 
GO
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarDetalleDeclaracionOrigenFondos"  AS 

SELECT 
	DOF."IdDeclaracionOrigenFondos",
 	DOF."IdCliente",
	DOF."IdTipoOperacionOrigenFondos",
	DOF."NombreTipoOperacionOrigenFondos",
 	DOF."FechaDeclaracion",
 	DOF."Comentario",
 	DOF."IdUsuarioCreador",
 	DOF."FechaIngreso",
 	DOF."FechaCreacion",
 	DOF."FechaModificacion",
 	DOF."IdEmpresa",
 	DOF."IdEstado",
	DOF."NombreEstado",
	DOF."OperacionInscripcion",
	--detalle
	CASE WHEN IFNULL(DDOF."IdDeclaracionOrigenFondos",0) = 0 THEN 'N' ELSE 'S' END as "DetalleExiste",
	IFNULL(DDOF."IdTipoOrigenFondo",DOF."IdTipoOrigenFondo") as "IdTipoOrigenFondo",
	IFNULL(TOF."Nombre",DOF."NombreTipoOrigenFondo") as "NombreTipoOrigenFondo",
	IFNULL(DDOF."Valor",0) as "Valor",
	DDOF."IdUsuarioCreador" as "IdUsuarioCreadorDetalle",
	DDOF."FechaIngreso" as "FechaIngresoDetalle",
	DDOF."FechaCreacion" as "FechaCreacionDetalle",
	DDOF."FechaModificacion" as "FechaModificacionDetalle", 
	IFNULL(DDOF."IdEstado",'') as "IdEstadoDetalle",
	IFNULL(DDOF."Otros",'') as "Otros",
	ESTDDOF."Nombre" as "NombreEstadoDetalle"
	
FROM (
		SELECT 
			DOF."IdDeclaracionOrigenFondos",
		 	DOF."IdCliente",
			DOF."IdTipoOperacionOrigenFondos",
			TIPOP."Nombre" as "NombreTipoOperacionOrigenFondos",
		 	DOF."FechaDeclaracion",
		 	DOF."Comentario",
		 	DOF."IdUsuarioCreador",
		 	DOF."FechaIngreso",
		 	DOF."FechaCreacion",
		 	DOF."FechaModificacion",
		 	DOF."IdEmpresa",
		 	DOF."IdEstado",
		 	DOF."OperacionInscripcion",
			ESTDOF."Nombre" as "NombreEstado",
			ALLTOF."IdTipoOrigenFondo",
			ALLTOF."Nombre" as "NombreTipoOrigenFondo"
			--,ALLTOF."ValorDefecto"
		FROM "BSCL.SocioNegocio.BD::CLI.DeclaracionOrigenFondos" as DOF
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" ESTDOF
			ON ESTDOF."IdEstado" = DOF."IdEstado"	
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion"	as TIPOP	
			ON TIPOP."IdTipoOperacion" = DOF."IdTipoOperacionOrigenFondos"
		INNER JOIN "BSCL.SocioNegocio.BD::CLI.TipoOrigenFondo" ALLTOF
			ON ALLTOF."IdEstado" = 'ACT' AND ALLTOF."IdEmpresa" = DOF."IdEmpresa"
	) as DOF
	INNER JOIN "BSCL.SocioNegocio.BD::CLI.DetalleDeclaracionOrigenFondos" as DDOF
		ON DDOF."IdDeclaracionOrigenFondos" = DOF."IdDeclaracionOrigenFondos" AND DDOF."IdTipoOrigenFondo" = DOF."IdTipoOrigenFondo"
		
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.TipoOrigenFondo" as TOF
		ON TOF."IdTipoOrigenFondo" = DDOF."IdTipoOrigenFondo" AND TOF."IdEmpresa" = DOF."IdEmpresa"
		
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" ESTDDOF
		ON ESTDDOF."IdEstado" = DDOF."IdEstado"	
	;
GO			
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarDetalleDeclaracionOrigenFondos" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
