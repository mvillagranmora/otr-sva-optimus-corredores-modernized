SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::CrearFamiliaFondoMutuo";

CREATE PROCEDURE
--  Proposito: Crear Familia de Fondo
--  Autor: - Gat-Blac 
--  Fecha Creacion: 13/09/2016
"BSCL.FondoMutuo.BD.Servicios::CrearFamiliaFondoMutuo" (
	 --VARIABLES OBLIGATORIAS
    IN p_IdEmpresa 						BIGINT,
	IN p_IdUsuario 						BIGINT,
    IN p_IdModulo 						NVARCHAR(3),
    IN p_UsaReglasNegocio 				NVARCHAR(1),
    
	IN p_FamiliaFondo 			TABLE	(
			    					  		"IdInterno" 					BIGINT,
			    					  		"IdFamiliaFondo"				NVARCHAR(40),
			    					  		"NombreFamilia"					NVARCHAR(100),
			    					  		"IdEstado"						NVARCHAR(3)
		    					  		),
  	
  	OUT p_out_Resultado			TABLE	(
  											"IdInterno"						BIGINT,
  											"IdFamiliaFondo"				NVARCHAR(40),
  											"NombreFamilia"					NVARCHAR(100),
  											"IdEstado"						NVARCHAR(3),
  											"IdEmpresa"                     BIGINT
  										),
	OUT p_out_AdvertenciasContador 		INT,
	OUT p_out_AdvertenciasDetalle 		TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)),
	OUT p_out_ErroresContador 			INT,
	OUT p_out_ErroresDetalle 			TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(520))
	)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	DECLARE v_out_AdvertenciasDetalle 		  	TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
	DECLARE v_out_ErroresDetalle  			  	TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
	BEGIN
		v_out_AdvertenciasDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'N' as "RequiereAutorizacion",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
		v_out_ErroresDetalle 	  = SELECT 0 as "IdInterno" ,'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
	END;
	BEGIN
		DECLARE SALIDA_CONTROLADA CONDITION;
		DECLARE v_FechaSistema NVARCHAR(10); 
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
			DECLARE v_count_familias		  		BIGINT;
			DECLARE v_resultadoRegla 		  		NVARCHAR(40);
			DECLARE v_count					  		BIGINT;
			DECLARE v_count_familias_validacion 	BIGINT;
		
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'CREAR FAMILIA FONDO MUTUO MM','INICIO','INICIO');
				    				
			CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
			
			if :v_resultadoRegla = 'RECH' then
				v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
				SIGNAL SALIDA_CONTROLADA; 	
			end if;
		
			SELECT COUNT(1) AS "Count" into v_count_familias
			FROM :p_FamiliaFondo;
			
			if (v_count_familias = 0) THEN
				v_out_ErroresDetalle = SELECT 0 as "IdInterno", 'NO_SE_INGRESARON_FAMILIAS' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
		
			SELECT TOP 1 COUNT("IdInterno") AS "Count" into v_count
			FROM :p_FamiliaFondo
			GROUP BY "IdInterno"
			ORDER BY "Count" DESC;
			
			if (v_count > 1) THEN
				v_out_ErroresDetalle = SELECT 0 AS "IdInterno", 'CORRELATIVO_INCORRECTO' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
		
			v_familias_validacion = SELECT
									"p_FamiliaDeFondo"."IdInterno"
									,"p_FamiliaDeFondo"."IdFamiliaFondo"		
							  		,"p_FamiliaDeFondo"."NombreFamilia"	
							  		,"p_FamiliaDeFondo"."IdEstado"
							  		,:p_IdEmpresa AS "IdEmpresa"
							  		,CASE WHEN COALESCE("FamiliaFondo"."IdFamiliaFondo",'-1') = '-1' THEN 'OK' ELSE 'ERR' END AS "Resultado"
							  	FROM :p_FamiliaFondo AS "p_FamiliaDeFondo"
							  	LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.FamiliaFondo" AS "FamiliaFondo" ON ("FamiliaFondo"."IdFamiliaFondo" = "p_FamiliaDeFondo"."IdFamiliaFondo" AND :p_IdEmpresa = "FamiliaFondo"."IdEmpresa");
		
			BEGIN
				v_out_ErroresDetalle = SELECT 0 AS "IdInterno", 'FAMILIA_EXISTE' as "Codigo", "Familias"."IdFamiliaFondo" as "ValorParametro", 'IdFamiliaFondo' AS "NombreParametro"
									   FROM :v_familias_validacion AS "Familias" WHERE "Resultado" = 'ERR';
				SELECT COUNT(1) as "Count" INTO v_count_familias_validacion
				FROM :v_familias_validacion WHERE "Resultado" = 'OK';
				IF (v_count_familias_validacion = 0) THEN
					SIGNAL SALIDA_CONTROLADA;
				END IF;
			END;
		
			v_familias_maestro = SELECT
									"p_FamiliaDeFondo"."IdInterno"
									,"p_FamiliaDeFondo"."IdFamiliaFondo"		
							  		,"p_FamiliaDeFondo"."NombreFamilia"	
							  		,"p_FamiliaDeFondo"."IdEstado"
							  		,:p_IdEmpresa AS "IdEmpresa"
							  	FROM :p_FamiliaFondo AS "p_FamiliaDeFondo";
								  	
			INSERT INTO "BSCL.FondoMutuo.BD::FMM.FamiliaFondo"(
				"IdFamiliaFondo", "NombreFamilia", "IdEstado", "IdEmpresa")
			SELECT "IdFamiliaFondo", "NombreFamilia", "IdEstado", "IdEmpresa"
			FROM :v_familias_maestro;
			
			p_out_Resultado = SELECT 	"IdInterno",
										"IdFamiliaFondo",
										"NombreFamilia", 
										"IdEstado", 
										"IdEmpresa"
								FROM :v_familias_maestro;
								
			v_log = SELECT 
					"IdInterno" AS "IdCabeceraLog"
					,'INGRESO' AS "IdTipoLog"
					,'FMM' AS "IdModulo"
					,:p_IdUsuario AS "IdUsuario"
					,:p_IdEmpresa AS "IdEmpresa"
					,:v_FechaSistema AS "FechaSistemaLog"
					,:p_IdUsuario AS "IdUsuarioAtendido"
					,0  AS "IdPersona"
					,0  AS "IdCliente"
					,'' AS "EstadoCliente"
					,'' AS "NombreCliente"
					,'' AS "IdTipoDocumentoIdentidad"
					,'' AS "IdDocumentoIdentidad"
					,0 AS "IdPortafolio"
					,'' AS "EstadoPortafolio"
					,'FAMILIA_FONDO_MUTUO' AS "IdTipoRegistro"
					,"IdInterno" AS "IdRegistro"
					,"IdEstado" AS "EstadoRegistro"
					,'' AS "IdTipoOperacion"
					,0 AS "Folio"
					,0 AS "Cantidad"
					,0 AS "PrecioTasa"
					,0 AS "Monto"
					,'' AS "IdMonedaOrigen"
					,'' AS "IdMonedaLiquidacion"
					,'' AS "TipoCambio"
					,"IdFamiliaFondo" AS "Nemo"
					,0 AS "IdProceso"
					,'' AS "EstadoProceso"
					,'' AS "NombreProceso"
				FROM :v_familias_maestro;
			
			v_DETALLELOG = SELECT 0 AS "IdCabeceraLog",'' as "SubRegistro", '' as "CampoRegistro", '' as "ValorAntiguo", '' as "ValorNuevo" from dummy where 1=2;
		
			CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle => :v_DETALLELOG);
			
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'CREAR INSTRUMENTO FONDO MUTUO MM','FIN','FIN');
								
			SIGNAL SALIDA_CONTROLADA;
		END;
	END;
	
END;
						
