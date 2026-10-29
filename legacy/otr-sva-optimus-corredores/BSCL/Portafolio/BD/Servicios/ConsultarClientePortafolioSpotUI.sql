SET SCHEMA BSCL;
GO
DROP PROCEDURE "BSCL.Portafolio.BD.Servicios::ConsultarClientePortafolioSpotUI";

GO
CREATE PROCEDURE  
"BSCL.Portafolio.BD.Servicios::ConsultarClientePortafolioSpotUI" (
	IN p_IdEmpresa          BIGINT,
	IN p_IdUsuario          BIGINT,
	IN p_IdModulo           NVARCHAR(3),
	
	IN p_IdEmpleado			BIGINT,
	IN p_IgnoraFiltro		NVARCHAR(1) DEFAULT 'N',
	
	OUT p_out_Portafolios		"BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolioSpot"
								
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER
	DEFAULT SCHEMA "BSCL"
	READS SQL DATA WITH RESULT VIEW "BSCL.Portafolio.BD.Vistas::ConsultarClientePortafolioSpotUI" AS 
BEGIN
	DECLARE v_RestringeCarteraCliente	NVARCHAR(1);
	DECLARE v_FechaActual				NVARCHAR(10) DEFAULT TO_NVARCHAR(CURRENT_UTCTIMESTAMP, 'YYYY-MM-DD');

	--Identificar si el empleado tiene restricciones sobre su cartera de clientes.
	SELECT "RestringeCarteraCliente" INTO v_RestringeCarteraCliente from "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleadoUI" where "IdEmpleado" = :p_IdEmpleado;
	
	SELECT "Valor" INTO v_FechaActual FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "CodigoParametro" = 'PLA.FechaSistema' AND "IdEmpresa" = p_IdEmpresa; 
	
	p_out_Portafolios =   SELECT 
							por."NombreCompletoPortafolio"--*
							,por."CodigoPortafolioPad"--*
							,por."IdPortafolio" --*
							,por."IdCliente" --*
							,por."IdEstado" --*
							,por."NombreCompleto"--*
							,por."IdEmpresa"--*
							,por."IdPersonaEjecutivo" 
							,por."IdEstadoCliente"
							,por."TipoPersona"	
							,por."IdContraparte"		
				FROM (
					--PORTAFOLIOS QUE SON VISIBLES AL EMPLEADO
					SELECT 
						PORT."IdPortafolio"
					FROM 
						(
							SELECT 
								POR."IdPortafolio"
								,POR."EsEjecutivoExclusivo"
								,POR."IdEmpleadoEjecutivo"
								,EJE."IdEmpleadoSupervisor"
								,EJE."IdEstado"
								FROM "BSCL.Portafolio.BD::POR.Portafolio" POR 
								LEFT JOIN "BSCL.SocioNegocio.BD::EJE.Ejecutivo" EJE ON POR."IdEmpleadoEjecutivo" = EJE."IdEmpleadoEjecutivo"
								WHERE POR."IdEmpleadoEjecutivo" = :p_IdEmpleado 
								OR (EJE."IdEmpleadoSupervisor" = :p_IdEmpleado and POR."EsEjecutivoExclusivo" = 'N') 
								OR :p_IgnoraFiltro = 'S' and POR."IdEstado" = 'ACT'
						) PORT
					UNION
					--PORTAFOLIOS QUE VE UN EMPLEADO BACKOFFICE
					SELECT"IdPortafolio" FROM (SELECT "PORT"."IdPortafolio", "PORT"."CodigoPortafolio", "PORT"."DescripcionPortafolio", "MAT"."NombreAgrupador" AS "NombreAgrupador", "PORT"."Proposito", "PORT"."IdCliente", "PORT"."IdEstado", "EST"."Nombre" as "Estado", "PORT"."FechaBloqueo", "PORT"."ComentarioBloqueo", "PORT"."IdTipoBloqueo", "BLOQ"."NombreTipoBloqueo", "IdPerfilComercialBaseOrigen", "PORT"."IdPerfilProductos", "PORT"."IdPersonaBrokerExtranjero", "PORT"."IdPerfilProductosExtra", "PORT"."IdPerfilCobros", "PORT"."IdPerfilCobrosExtra", "PORT"."IdPerfilCobrosPeru", "PORT"."IdPerfilCobrosPeruExtra", "PORT"."IdPerfilLimites", "PORT"."IdPerfilLimitesExtra", "PORT"."IdPerfilCondicionesOperativas", "PORT"."IdPerfilCondicionesOperativasExtra", "PORT"."IdEstadoAutorizacion", "PORT"."FechaIngreso", "PORT"."FechaCreacion", "PORT"."FechaModificacion", "PORT"."IdDireccionEnvioCorrespondencia", "PORT"."IdTelefonoContacto", "PORT"."IdCarga", "PORT"."IdEmpleadoEjecutivo", "PORT"."EsEjecutivoExclusivo", "PORT"."CodigoPortafolio" ||' '||"PORT"."Proposito" as "CodigoPropositoPortafolio", "PORT"."NumeroFicha", "PORT"."IdNombreFantasia", "PORT"."Alias", "PORT"."FechaUltimaCartola", "PORT"."IdUltimaCartola", case when EMP."IdEmpresa" is null then 'N' else 'S' end as "EsCuentaPropia", EMP."IdEmpresa" as "IdEmpresaCuentaPropia", "PORT"."IdMatrizAgrupadorAPV", CASE WHEN "PORT"."IdMatrizAgrupadorAPV" = 0 THEN 'N' ELSE 'S' END AS "TienePropositoAPV", MAP(MAT."IdProductoAgrupador",NULL,9999,MAT."IdProductoAgrupador") AS "IdProductoAgrupador", RT."IdRegimenTributario", (CASE WHEN COALESCE(SOC."IdContraparte",0) = 0 THEN 'N' ELSE 'S' END) AS "EsContraparte", CASE WHEN COALESCE("PORT"."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE("PORT"."IdMatrizAgrupadorAPV",0) ELSE COALESCE(MAT_NOAPV."IdMatrizAgrupadorNOAPV",0) END AS "IdMatrizAgrupador", CASE WHEN COALESCE("PORT"."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE("MAT"."EsADC",'N') ELSE COALESCE(MAT_NOAPV."EsADC",'N') END AS "EsADC" FROM "BSCL.Portafolio.BD::POR.Portafolio" AS "PORT" LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "CLI" ON "CLI"."IdCliente" = "PORT"."IdCliente" LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "PER" ON "CLI"."IdPersona" = "PER"."IdPersona" LEFT JOIN "BSCL.Portafolio.BD::POR.EstadoPortafolio" AS "ESTPORT" ON "PORT"."IdEstado" = "ESTPORT"."IdEstado" AND "ESTPORT"."NombreEntidad" = 'PORTAFOLIO' LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "EST" ON "EST"."IdEstado" = (CASE WHEN "PORT"."IdEstado" IN ('ACT','VIG') THEN "PORT"."IdEstado" ELSE 'BLO' END) LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SOC on SOC."IdCliente" = "PORT"."IdCliente" LEFT JOIN "BSCL.Plataforma.BD::PLA.Empresa" AS EMP ON EMP."IdSocioDeNegocio" = SOC."IdSocioDeNegocio" LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" MAT ON MAT."IdMatrizAgrupadorAPV" = "PORT"."IdMatrizAgrupadorAPV" LEFT JOIN "BSCL.APV.BD::REC.RegimenTributario" RT ON RT."IdRegimenTributario" = "MAT"."IdRegimenTributario" LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = PORT."CodigoPortafolio" AND  "PORT"."IdMatrizAgrupadorAPV" = 0 LEFT JOIN "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio" AS "BLOQ" ON BLOQ."IdTipoBloqueo" = "PORT"."IdTipoBloqueo" AND BLOQ."IdTipoEntidad" = 'PORTAFOLIO') PORT,
					(
						--EMPLEADOS BACKOFFICE
						SELECT "IdEmpleado" FROM "BSCL.SocioNegocio.BD::EMP.Empleado"
						EXCEPT
						--SUPERVISORES
						SELECT "IdEmpleadoSupervisor" AS "IdEmpleado" 
						FROM (
							SELECT 
								SUP."IdEmpleadoSupervisor"
								FROM "BSCL.SocioNegocio.BD::EJE.Supervisor" SUP
								JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" EMP ON SUP."IdEmpleadoSupervisor" = EMP."IdEmpleado"
								JOIN "BSCL.Plataforma.BD::PLA.Estado" EST ON EST."IdEstado" = SUP."IdEstado"
								JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER ON PER."IdPersona" = EMP."IdPersona"	
							)
						EXCEPT
						SELECT "IdEmpleadoEjecutivo" AS "IdEmpleado" 
						FROM (
							SELECT 
							EJE."IdEmpleadoEjecutivo"
							
							FROM "BSCL.SocioNegocio.BD::EJE.Ejecutivo" EJE
							)
						--EMPLEADOS REEMPLAZO
						EXCEPT
						SELECT "IdEmpleadoEjecutivo" AS "IdEmpleado" 
						FROM (
							SELECT 
								EJE."IdEmpleadoEjecutivo"
								,EJE."IdEmpleadoReemplaza" as "IdEmpleadoEjecutivoReemplaza"
								,CASE WHEN CONF."Valor" BETWEEN EJE."FechaInicioReemplazo" AND EJE."FechaFinReemplazo" THEN EJE."FechaInicioReemplazo"
								     ELSE '0001-01-01'     
								END AS "FechaInicioReemplazo"
								,CASE WHEN CONF."Valor" BETWEEN EJE."FechaInicioReemplazo" AND EJE."FechaFinReemplazo" THEN EJE."FechaFinReemplazo"
								     ELSE '0001-01-01'     
								END AS "FechaFinReemplazo"								
								FROM "BSCL.SocioNegocio.BD::EJE.Ejecutivo" EJE
								JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" EMP ON EJE."IdEmpleadoEjecutivo" = EMP."IdEmpleado"
								JOIN "BSCL.Plataforma.BD::PLA.Configuracion" CONF ON CONF."CodigoParametro" = 'PLA.FechaSistema' AND EMP."IdEmpresa" = CONF."IdEmpresa"
								WHERE EJE."IdEmpleadoReemplaza" = :p_IdEmpleado AND :v_FechaActual BETWEEN EJE."FechaInicioReemplazo" AND EJE."FechaFinReemplazo"
								)					
					) EMP
					WHERE EMP."IdEmpleado" = :p_IdEmpleado
					UNION
                    --Todos los portafolios en los que el empleado esta reemplazando
                    SELECT PORT."IdPortafolio"
                    FROM (
                    	SELECT 
							POR."IdPortafolio"
							,EJE."IdEmpleadoReemplaza"
							,EJE."FechaInicioReemplazo"
							,EJE."FechaFinReemplazo"
							FROM (
							SELECT
								"PORT"."IdPortafolio"
								,"PORT"."IdEmpleadoEjecutivo"
							FROM "BSCL.Portafolio.BD::POR.Portafolio" AS "PORT") POR
							LEFT JOIN "BSCL.SocioNegocio.BD::EJE.Ejecutivo" EJE ON POR."IdEmpleadoEjecutivo" = EJE."IdEmpleadoEjecutivo"
							WHERE EJE."IdEmpleadoReemplaza" = :p_IdEmpleado AND :v_FechaActual BETWEEN EJE."FechaInicioReemplazo" AND EJE."FechaFinReemplazo"
                    	) PORT
					UNION
					SELECT PORT."IdPortafolio"
                    
					FROM (
                    	SELECT 
							POR."IdPortafolio"
							,POR."EsEjecutivoExclusivo"
							FROM (
								SELECT
									"PORT"."IdPortafolio" 
									,"PORT"."IdEmpleadoEjecutivo"
									,"PORT"."EsEjecutivoExclusivo"
								FROM "BSCL.Portafolio.BD::POR.Portafolio" AS "PORT"
								LEFT JOIN "BSCL.SocioNegocio.BD::EJE.Ejecutivo" EJE ON "PORT"."IdEmpleadoEjecutivo" = EJE."IdEmpleadoEjecutivo"
								WHERE :v_RestringeCarteraCliente='N' and "PORT"."EsEjecutivoExclusivo" = 'N'
							) POR
	                    ) PORT           
				) as PORT
				INNER JOIN  (SELECT
								cli."DocumentoIdentidad" || ' ' || cli."NombreCompleto" || CASE WHEN COALESCE(ProductoAgrupador."NombreProductoAgrupador",'') != '' THEN ' - ' || ProductoAgrupador."NombreProductoAgrupador" ELSE '' END || CASE WHEN COALESCE(port."Alias",'') != '' THEN ' - ' || port."Alias" ELSE '' END || CASE WHEN fan."DescripcionNombreFantasia" IS NOT NULL THEN ' - ' || fan."DescripcionNombreFantasia" ELSE '' END ||' (' || port."CodigoPortafolio" || ')' as "NombreCompletoPortafolio",
								LPAD(port."CodigoPortafolio",3,' ') AS "CodigoPortafolioPad",
								port."IdPortafolio", 
								port."IdCliente", 
								port."IdEstado",
								cli."NombreCompleto",
								cli."IdEmpresa",
								cli."IdPersonaEjecutivo",
								cli."IdEstado" as "IdEstadoCliente",
								cli."Tipo" as "TipoPersona",
								COALESCE("IdContraparte", 0) AS "IdContraparte"
							FROM "BSCL.Portafolio.BD::POR.Portafolio" AS port
							INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli on port."IdCliente" = cli."IdCliente"
							LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" soc ON soc."IdCliente" = cli."IdCliente"
							LEFT JOIN "BSCL.SocioNegocio.BD::PER.NombreFantasia" fan on port."IdNombreFantasia" = fan."IdNombreFantasia"
							LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" MAT ON MAT."IdMatrizAgrupadorAPV" = port."IdMatrizAgrupadorAPV"
							LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS ProductoAgrupador
									ON ProductoAgrupador."IdProductoAgrupador" = MAT."IdProductoAgrupador"
  							) por on por."IdPortafolio" = port."IdPortafolio"
				WHERE "IdEmpresa" = :p_IdEmpresa;
END;
GO