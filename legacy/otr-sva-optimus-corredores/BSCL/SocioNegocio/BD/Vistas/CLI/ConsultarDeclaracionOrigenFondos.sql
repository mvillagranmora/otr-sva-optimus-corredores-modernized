SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarDeclaracionOrigenFondos"; 
 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarDeclaracionOrigenFondos"  AS 

SELECT 
	DOF.*,
	ESTCALC."Nombre" as "NombreEstadoCalculado"
FROM
	(SELECT 
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
			ESTDOF."Nombre" as "NombreEstado",
			DOF."IdEstadoAutorizacion",
			ESTAUTDOF."Nombre" as "NombreEstadoAutorizacion",
			CASE 
				WHEN DOF."IdEstado" = 'ACT' AND DOF."IdEstadoAutorizacion" = 'AUT'  THEN 'ACT'
				WHEN DOF."IdEstado" = 'ACT' AND DOF."IdEstadoAutorizacion" = 'ING'  THEN 'ACT'
				WHEN DOF."IdEstado" = 'ACT' AND DOF."IdEstadoAutorizacion" = '' 	THEN 'PEN'
				WHEN DOF."IdEstado" = 'ACT' THEN IFNULL(DOF."IdEstadoAutorizacion",'PEN')
				ELSE DOF."IdEstado"
				END as "IdEstadoCalculado",
			DOF."OperacionInscripcion"
		FROM "BSCL.SocioNegocio.BD::CLI.DeclaracionOrigenFondos" as DOF
		LEFT JOIN 
			"BSCL.Plataforma.BD::PLA.Estado" ESTDOF
				ON ESTDOF."IdEstado" = DOF."IdEstado"
		LEFT JOIN 
			"BSCL.Plataforma.BD::PLA.Estado" ESTAUTDOF
				ON ESTAUTDOF."IdEstado" = DOF."IdEstadoAutorizacion"
		LEFT JOIN 
			"BSCL.Plataforma.BD::INS.TipoOperacion"	as TIPOP	
				ON TIPOP."IdTipoOperacion" = DOF."IdTipoOperacionOrigenFondos"
		) as DOF
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" ESTCALC
				ON ESTCALC."IdEstado" = DOF."IdEstadoCalculado"
;
			
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarDeclaracionOrigenFondos" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 




