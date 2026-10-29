SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.FondoMutuo.BD.Servicios::ConsultarRequisitosBasicosCrearSolicitudAporteRescate";

CREATE PROCEDURE
-- 
--  Proposito: Servicio para confirmar solicitud aporte rescate.
--  Autor: Gat-Blac
--  Fecha Creacion: 03-04-2017
--  
"BSCL.FondoMutuo.BD.Servicios::ConsultarRequisitosBasicosCrearSolicitudAporteRescate" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa						BIGINT,			--ID Empresa destino
	IN  p_IdUsuario						BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo						NVARCHAR(3),  	--ID del Modulo que lo invoca
		  
	IN	p_IdEmpleado					BIGINT,
	IN  p_IdTipoProducto				NVARCHAR(6),
	IN	p_IdTipoOperacion				NVARCHAR(6),
	IN	p_IdPortafolio					BIGINT,
	
	OUT p_out_AdvertenciasContador			INT,
	OUT p_out_AdvertenciasDetalle			TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)) DEFAULT EMPTY,	
	OUT p_out_ErroresContador 				INT,
	OUT p_out_ErroresDetalle 				TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(520)) DEFAULT EMPTY
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------
	DECLARE v_out_AdvertenciasDetalle 	TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
	DECLARE v_out_ErroresDetalle  		TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
	BEGIN
		v_out_AdvertenciasDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'N' as "RequiereAutorizacion",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
		v_out_ErroresDetalle 	  = SELECT 0 as "IdInterno" ,'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
	END;
	
	BEGIN
		DECLARE SALIDA_CONTROLADA CONDITION;
		DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA  
		BEGIN
			select count(1) into p_out_ErroresContador 
			from :v_out_ErroresDetalle;
			select count(1) into p_out_AdvertenciasContador 
			from :v_out_AdvertenciasDetalle;
			
			v_TablaCodigosMensajes = SELECT "Codigo" AS "CodigoMensaje",
									"NombreParametro" as "CodigoDato",
									"ValorParametro" as "ValorDato" 
									FROM :v_out_ErroresDetalle
									UNION ALL
									SELECT "Codigo" AS "CodigoMensaje",
									"NombreParametro" as "CodigoDato",
									"ValorParametro" as "ValorDato" 
									FROM :v_out_AdvertenciasDetalle;
									
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes,v_out_ErroresDetalleMensajes);
			
			p_out_ErroresDetalle = SELECT 	"errores"."IdInterno",
											"errores"."Codigo",
											"msj"."Descripcion"
									FROM :v_out_ErroresDetalle AS "errores"
									INNER JOIN :v_out_ErroresDetalleMensajes AS "msj" ON "errores"."Codigo" = "msj"."Codigo";
			
			p_out_AdvertenciasDetalle = SELECT 	"adv"."IdInterno",
												"adv"."Codigo",
												"adv"."RequiereAutorizacion",
												"msj"."Descripcion"
										FROM :v_out_AdvertenciasDetalle AS "adv"
										INNER JOIN :v_out_ErroresDetalleMensajes AS "msj" ON "adv"."Codigo" = "msj"."Codigo"; 
		END;
		
		p_out_AdvertenciasContador	:= 0;
		p_out_ErroresContador		:= 0;
		
		BEGIN
			DECLARE v_EmpleadoTienePlantilla NVARCHAR(1);
			DECLARE v_PortafolioTieneProducto NVARCHAR(1);
					
			SELECT TOP 1 CASE WHEN COALESCE(DETALLE$1."IdTipoProducto",'') = '' THEN 
							 CASE WHEN COALESCE(DETALLE$2."IdTipoProducto",'') = '' THEN 'N'
							 	  ELSE 'S'
							 END
							 ELSE 'S'
							 END INTO v_PortafolioTieneProducto 
							FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" PORTAFOLIO 
							LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilProductos" DETALLE$1 ON (PORTAFOLIO."IdPerfilProductosExtra" = DETALLE$1."IdPerfilProductos" AND DETALLE$1."IdTipoProducto" = :p_IdTipoProducto AND DETALLE$1."IdEmpresa" = :p_IdEmpresa)
							LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilProductos" DETALLE$2 ON (PORTAFOLIO."IdPerfilProductos" = DETALLE$2."IdPerfilProductos" AND DETALLE$2."IdTipoProducto" = :p_IdTipoProducto AND DETALLE$2."IdEmpresa" = :p_IdEmpresa AND COALESCE(DETALLE$1."IdTipoProducto",'') = '')
							WHERE PORTAFOLIO."IdPortafolio" = :p_IdPortafolio;
			
			SELECT  CASE WHEN det1."IdTipoOperacion" IS NOT NULL THEN 'S' WHEN det2."IdTipoOperacion" IS NOT NULL THEN 'S' ELSE 'N' END INTO v_EmpleadoTienePlantilla
							FROM "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" emp
							LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" tipoope on tipoope."IdTipoOperacion" = :p_IdTipoOperacion
							LEFT JOIN "BSCL.Plataforma.BD::COM.PlantillaEmpleado" pnt on emp."IdPlantillaEmpleado" = pnt."IdPlantillaEmpleado" AND pnt."IdEmpresa" = :p_IdEmpresa
							LEFT JOIN "BSCL.Plataforma.BD::COM.DetallePlantillaEmpleado" det1 on det1."IdPlantillaEmpleado" = pnt."IdPlantillaEmpleado" and det1."IdEstado" = 'ACT' and det1."IdTipoProducto" = tipoope."IdTipoProducto" and det1."IdTipoOperacion" = :p_IdTipoOperacion
							LEFT JOIN "BSCL.Plataforma.BD::COM.DetallePlantillaEmpleado" det2 on det2."IdPlantillaEmpleado" = pnt."IdPlantillaEmpleado" and det2."IdEstado" = 'ACT' and det2."IdTipoProducto" = tipoope."IdTipoProducto" and det2."IdTipoOperacion" = '0'
							WHERE emp."IdEmpleado" = :p_IdEmpleado AND emp."IdEmpresa" = :p_IdEmpresa;
							
			IF (:v_EmpleadoTienePlantilla = 'N') THEN
				v_out_ErroresDetalle = SELECT 0 as "IdInterno" ,'PRODUCTO_NO_HABILITADO_EMPLEADO' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" from DUMMY; 
			END IF;
			
			IF (:v_PortafolioTieneProducto = 'N') THEN
				v_out_ErroresDetalle = SELECT 0 as "IdInterno" ,'PRODUCTO_NO_HABILITADO' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" from DUMMY
									   union all 
									   SELECT * FROM :v_out_ErroresDetalle; 
			END IF;
			SIGNAL SALIDA_CONTROLADA;
		END;
	END;		
END;