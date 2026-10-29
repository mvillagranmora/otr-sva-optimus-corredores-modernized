SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::CrearProgramarGeneraArchivoOperacionesAGF";

CREATE PROCEDURE
--  Proposito: Crear Programa para Certificacion Batch de operaciones
--  Autor: - Gat-Blac 
--  Fecha Creacion: 25/10/2016
"BSCL.FondoMutuo.BD.Servicios::CrearProgramarGeneraArchivoOperacionesAGF" (
	 --VARIABLES OBLIGATORIAS
    IN p_IdEmpresa 						BIGINT,
	IN p_IdUsuario 						BIGINT,
    IN p_IdModulo 						NVARCHAR(3),
    IN p_IdPortafolioAgf 				BIGINT,    
    IN p_FechaDesde						NVARCHAR(10),
    IN p_FechaHasta						NVARCHAR(10),
  	OUT p_out_Resultado					TABLE	(
  											"IdInterno"				BIGINT,
  											"IdPrograma"		    BIGINT
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
			DECLARE v_resultadoRegla 		  		NVARCHAR(40);
			DECLARE v_p_out_AdvertenciasContador    BIGINT;
			DECLARE v_p_out_ErroresContador         BIGINT;
			
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'AGENDAR CERTIFICACION MASIVA FONDO MUTUO','INICIO','INICIO');
				    				
			CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
			
			if :v_resultadoRegla = 'RECH' then
				v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
				SIGNAL SALIDA_CONTROLADA; 	
			end if;
			
			v_parametros = SELECT 'IdUsuario' AS "NombreParametro",
								  cast(:p_IdUsuario as NVARCHAR(100)) AS "Valor" 
						   FROM DUMMY
						   UNION ALL
						   SELECT 'IdEmpresa' AS "NombreParametro",
								  cast(:p_IdEmpresa as NVARCHAR(100)) AS "Valor" 
						   FROM DUMMY
						   UNION ALL
						   SELECT 'FechaDesde' AS "NombreParametro",
								  cast(:p_FechaDesde as NVARCHAR(100)) AS "Valor" 
						   FROM DUMMY
						   UNION ALL
						   SELECT 'FechaHasta' AS "NombreParametro",
								  cast(:p_FechaHasta as NVARCHAR(100)) AS "Valor" 
						   FROM DUMMY
						   UNION ALL
						   SELECT 'IdPortafolioAgf' AS "NombreParametro",
								  cast(:p_IdPortafolioAgf as NVARCHAR(100)) AS "Valor" 
						   FROM DUMMY;
						  
			CALL "BSCL.Plataforma.BD.Servicios::BAT.AgregarPrograma"(:p_IdEmpresa,
																	:p_IdUsuario,
																	:p_IdModulo,
																	0,
																	'GENERARARCHIVOOPERACIONESAGF',
																	1,
																	:v_parametros,
																	'S',
																	v_p_out_IdPrograma,
																	v_p_out_AdvertenciasContador,
																	v_p_out_AdvertenciasDetalle,
																	v_p_out_ErroresContador,
																	v_p_out_ErroresDetalle);
		
			p_out_Resultado = SELECT  0 as "IdInterno", "IdPrograma" AS "IdPrograma"
								FROM :v_p_out_IdPrograma;
						
			v_log = SELECT 
					"IdPrograma" AS "IdCabeceraLog"
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
					,'GEN_ARCH_AGF_FFMM' AS "IdTipoRegistro"
					,"IdPrograma" AS "IdRegistro"
					,'' AS "EstadoRegistro"
					,'' AS "IdTipoOperacion"
					,0 AS "Folio"
					,0 AS "Cantidad"
					,0 AS "PrecioTasa"
					,0 AS "Monto"
					,'' AS "IdMonedaOrigen"
					,'' AS "IdMonedaLiquidacion"
					,'' AS "TipoCambio"
					,'' AS "Nemo"
					,0 AS "IdProceso"
					,'' AS "EstadoProceso"
					,'' AS "NombreProceso"
				FROM :v_p_out_IdPrograma;
			
			v_DETALLELOG = SELECT 0 AS "IdCabeceraLog",'' as "SubRegistro", '' as "CampoRegistro", '' as "ValorAntiguo", '' as "ValorNuevo" from dummy where 1=2;
		
			CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle => :v_DETALLELOG);
			
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'AGENDAR CERTIFICACION MASIVA FONDO MUTUO','FIN','FIN');
						
			SIGNAL SALIDA_CONTROLADA;
		END;
	END;
	
END;
