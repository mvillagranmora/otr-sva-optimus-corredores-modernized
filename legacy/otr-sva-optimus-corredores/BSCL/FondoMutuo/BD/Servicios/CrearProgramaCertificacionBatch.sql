SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::CrearProgramaCertificacionBatch";

CREATE PROCEDURE
--  Proposito: Crear Programa para Certificacion Batch de operaciones
--  Autor: - Gat-Blac 
--  Fecha Creacion: 25/10/2016
"BSCL.FondoMutuo.BD.Servicios::CrearProgramaCertificacionBatch" (
	 --VARIABLES OBLIGATORIAS
    IN p_IdEmpresa 						BIGINT,
	IN p_IdUsuario 						BIGINT,
    IN p_IdModulo 						NVARCHAR(3),
    IN p_SimularOperacion 				NVARCHAR(1),
    IN p_UsaReglasNegocio 				NVARCHAR(1),    
    IN p_FechaCertificacion				NVARCHAR(10),
    IN p_Masivo				            NVARCHAR(1),
    IN p_Solicitudes                    TABLE (
    										"IdSolicitudAporteRescate"   BIGINT
    									),
    IN p_Filtros						TABLE (
											"IdInterno"					 BIGINT,
											"IdEmisor" 	 				 NVARCHAR(40),
											"IdMoneda"					 NVARCHAR(3),
											"IdSerieFondo"				 NVARCHAR(40),
											"Nacional"			 		 NVARCHAR(1),
											"TipoSolicitud"		 		 NVARCHAR(1),
											"IdTipoMovimientoFondoMutuo" NVARCHAR(10),
											"MoneyMarket"				 NVARCHAR(1),
											"IdCarga"					 BIGINT	
										),
  	OUT p_out_Resultado			TABLE	(
  											"IdInterno"				BIGINT,
  											"IdEstado"		    BIGINT
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
			DECLARE v_parametroCargas              	NVARCHAR(100);
			DECLARE v_arr_id_carga                  BIGINT ARRAY;
			DECLARE v_arr_index               	    BIGINT;
			
			DECLARE v_parametro_idinterno_filtro           NVARCHAR(100);
			DECLARE v_parametro_idemisor               	   NVARCHAR(100);
			DECLARE v_parametro_idmoneda                   NVARCHAR(100);
			DECLARE v_parametro_idseriefondo               NVARCHAR(100);
			DECLARE v_parametro_nacional                   NVARCHAR(100);
			DECLARE v_parametro_tiposolicitud              NVARCHAR(100);
			DECLARE v_parametro_idtipomovimientofondomutuo NVARCHAR(100);
			DECLARE v_parametro_moneymarket                NVARCHAR(100);
			DECLARE v_parametro_cargas                     NVARCHAR(100);
			DECLARE v_parametro_idsolicitud                NVARCHAR(100);

			DECLARE v_arr_idinternofiltro           	BIGINT ARRAY;			
			DECLARE v_arr_idemisor               	    NVARCHAR(40) ARRAY;
			DECLARE v_arr_idmoneda               		NVARCHAR(3) ARRAY;
			DECLARE v_arr_idseriefondo               	NVARCHAR(40) ARRAY;
			DECLARE v_arr_nacional         				NVARCHAR(1) ARRAY;
			DECLARE v_arr_tiposolicitud                 NVARCHAR(3) ARRAY;
			DECLARE v_arr_idtipomovimientofondomutuo    NVARCHAR(10) ARRAY;
			DECLARE v_arr_moneymarket        			NVARCHAR(1) ARRAY;
			DECLARE v_arr_cargas               	    	BIGINT ARRAY;
			DECLARE v_arr_idsolicitud               	BIGINT ARRAY;	
			
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'AGENDAR CERTIFICACION MASIVA FONDO MUTUO','INICIO','INICIO');
				    				
			CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
			
			if :v_resultadoRegla = 'RECH' then
				v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
				SIGNAL SALIDA_CONTROLADA; 	
			end if;
			
			v_idinternofiltro = SELECT "IdInterno" AS "IdInterno" FROM :p_Filtros;
			v_idemisor = SELECT "IdEmisor" AS "IdEmisor" FROM :p_Filtros;
			v_idmoneda = SELECT "IdMoneda" AS "IdMoneda" FROM :p_Filtros;
			v_idseriefondo = SELECT "IdSerieFondo" AS "IdSerieFondo" FROM :p_Filtros;
			v_nacional = SELECT "Nacional" AS "Nacional" FROM :p_Filtros;
			v_tiposolicitud = SELECT "TipoSolicitud" AS "TipoSolicitud" FROM :p_Filtros;
			v_idtipomovimientofondomutuo = SELECT "IdTipoMovimientoFondoMutuo" AS "IdTipoMovimientoFondoMutuo" FROM :p_Filtros;
			v_moneymarket = SELECT "MoneyMarket" AS "MoneyMarket" FROM :p_Filtros;
			v_cargas = SELECT "IdCarga" AS "IdCarga" FROM :p_Filtros;
			v_solicitudes = SELECT "IdSolicitudAporteRescate" AS "IdSolicitudAporteRescate" FROM :p_Solicitudes;
			
			v_arr_idinternofiltro = ARRAY_AGG(:v_idinternofiltro."IdInterno");
			v_arr_idemisor = ARRAY_AGG(:v_idemisor."IdEmisor");
			v_arr_idmoneda = ARRAY_AGG(:v_idmoneda."IdMoneda");
			v_arr_idseriefondo = ARRAY_AGG(:v_idseriefondo."IdSerieFondo");
			v_arr_nacional = ARRAY_AGG(:v_nacional."Nacional");
			v_arr_tiposolicitud = ARRAY_AGG(:v_tiposolicitud."TipoSolicitud");
			v_arr_idtipomovimientofondomutuo = ARRAY_AGG(:v_idtipomovimientofondomutuo."IdTipoMovimientoFondoMutuo");
			v_arr_moneymarket = ARRAY_AGG(:v_moneymarket."MoneyMarket");
			v_arr_cargas = ARRAY_AGG(:v_cargas."IdCarga");
			v_arr_idsolicitud =  ARRAY_AGG(:v_solicitudes."IdSolicitudAporteRescate");
			
			v_parametro_idinterno_filtro := '';
			v_parametro_idemisor := '';
			v_parametro_idmoneda := '';
			v_parametro_idseriefondo := '';
			v_parametro_nacional := '';
			v_parametro_tiposolicitud := '';
			v_parametro_idtipomovimientofondomutuo := '';
			v_parametro_moneymarket := '';
			v_parametro_cargas := '';
			v_parametro_idsolicitud := '';
			
			
			FOR v_arr_index IN 1..CARDINALITY(:v_arr_idsolicitud) DO
				IF (CARDINALITY(:v_arr_idsolicitud) = :v_arr_index) THEN
					v_parametro_idsolicitud := :v_parametro_idsolicitud || :v_arr_idsolicitud[:v_arr_index];
				ELSE
					v_parametro_idsolicitud := :v_parametro_idsolicitud || :v_arr_idsolicitud[:v_arr_index] || ';';
				END IF;
			END FOR;
			
			FOR v_arr_index IN 1..CARDINALITY(:v_arr_idinternofiltro) DO
				IF (CARDINALITY(:v_arr_idinternofiltro) = :v_arr_index) THEN
					v_parametro_idinterno_filtro := :v_parametro_idinterno_filtro || :v_arr_idinternofiltro[:v_arr_index];
					v_parametro_idemisor := :v_parametro_idemisor || :v_arr_idemisor[:v_arr_index];
					v_parametro_idmoneda := :v_parametro_idmoneda || :v_arr_idmoneda[:v_arr_index];
					v_parametro_idseriefondo := :v_parametro_idseriefondo || :v_arr_idseriefondo[:v_arr_index];
					v_parametro_nacional := :v_parametro_nacional || :v_arr_nacional[:v_arr_index];
					v_parametro_tiposolicitud := :v_parametro_tiposolicitud || :v_arr_tiposolicitud[:v_arr_index];
					v_parametro_idtipomovimientofondomutuo := :v_parametro_idtipomovimientofondomutuo || :v_arr_idtipomovimientofondomutuo[:v_arr_index];
					v_parametro_moneymarket := :v_parametro_moneymarket || :v_arr_moneymarket[:v_arr_index];
					v_parametro_cargas := :v_parametro_cargas || :v_arr_cargas[:v_arr_index];
				ELSE
					v_parametro_idinterno_filtro := :v_parametro_idinterno_filtro || :v_arr_idinternofiltro[:v_arr_index] || ';';
					v_parametro_idemisor := :v_parametro_idemisor || :v_arr_idemisor[:v_arr_index] || ';';
					v_parametro_idmoneda := :v_parametro_idmoneda || :v_arr_idmoneda[:v_arr_index] || ';';
					v_parametro_idseriefondo := :v_parametro_idseriefondo || :v_arr_idseriefondo[:v_arr_index] || ';';
					v_parametro_nacional := :v_parametro_nacional || :v_arr_nacional[:v_arr_index] || ';';
					v_parametro_tiposolicitud := :v_parametro_tiposolicitud || :v_arr_tiposolicitud[:v_arr_index] || ';';
					v_parametro_idtipomovimientofondomutuo := :v_parametro_idtipomovimientofondomutuo || :v_arr_idtipomovimientofondomutuo[:v_arr_index] || ';';
					v_parametro_moneymarket := :v_parametro_moneymarket || :v_arr_moneymarket[:v_arr_index] || ';';
					v_parametro_cargas := :v_parametro_cargas || :v_arr_cargas[:v_arr_index] || ';';
				END IF;
			END FOR;
			
			v_parametros = SELECT 'IdInternoFiltro' AS "NombreParametro",
								  cast(:v_parametro_idinterno_filtro as NVARCHAR(100)) AS "Valor" 
						   FROM DUMMY
						   UNION ALL
						   SELECT 'IdEmisor' AS "NombreParametro",
								  cast(:v_parametro_idemisor as NVARCHAR(100)) AS "Valor" 
						   FROM DUMMY
						   UNION ALL
						   SELECT 'IdMoneda' AS "NombreParametro",
								  cast(:v_parametro_idmoneda as NVARCHAR(100)) AS "Valor" 
						   FROM DUMMY
						   UNION ALL
						   SELECT 'IdSerieFondo' AS "NombreParametro",
								  cast(:v_parametro_idseriefondo as NVARCHAR(100)) AS "Valor" 
						   FROM DUMMY
						   UNION ALL
						   SELECT 'Nacional' AS "NombreParametro",
								  cast(:v_parametro_nacional as NVARCHAR(100)) AS "Valor" 
						   FROM DUMMY
						   UNION ALL
						   SELECT 'TipoSolicitud' AS "NombreParametro",
								  cast(:v_parametro_tiposolicitud as NVARCHAR(100)) AS "Valor" 
						   FROM DUMMY
						   UNION ALL
						   SELECT 'IdTipoMovimientoFondoMutuo' AS "NombreParametro",
								  cast(:v_parametro_idtipomovimientofondomutuo as NVARCHAR(100)) AS "Valor" 
						   FROM DUMMY
						   UNION ALL
						   SELECT 'MoneyMarket' AS "NombreParametro",
								  cast(:v_parametro_moneymarket as NVARCHAR(100)) AS "Valor" 
						   FROM DUMMY
						   UNION ALL
						   SELECT 'IdCarga' AS "NombreParametro",
								  cast(:v_parametro_cargas as NVARCHAR(100)) AS "Valor" 
						   FROM DUMMY
						   UNION ALL
						   SELECT 'IdEmpresa' AS "NombreParametro",
								  cast(:p_IdEmpresa as NVARCHAR(100)) AS "Valor" 
						   FROM DUMMY
						   UNION ALL
						   SELECT 'IdUsuario' AS "NombreParametro",
								  cast(:p_IdUsuario as NVARCHAR(100)) AS "Valor" 
						   FROM DUMMY
						   UNION ALL
						   SELECT 'SimularOperacion' AS "NombreParametro",
								  cast(:p_SimularOperacion as NVARCHAR(100)) AS "Valor" 
						   FROM DUMMY
						   UNION ALL
						   SELECT 'UsaReglasNegocio' AS "NombreParametro",
								  cast(:p_SimularOperacion as NVARCHAR(100)) AS "Valor" 
						   FROM DUMMY
						   UNION ALL
						   SELECT 'FechaCertificacion' AS "NombreParametro",
								  cast(:p_FechaCertificacion as NVARCHAR(100)) AS "Valor" 
						   FROM DUMMY
						   UNION ALL
						   SELECT 'IdSolicitud' AS "NombreParametro",
								  cast(:v_parametro_idsolicitud as NVARCHAR(100)) AS "Valor" 
						   FROM DUMMY
						   UNION ALL
						   SELECT 'EsMasivo' AS "NombreParametro",
								  cast(:p_Masivo as NVARCHAR(100)) AS "Valor" 
						   FROM DUMMY;
						  
			CALL "BSCL.Plataforma.BD.Servicios::BAT.AgregarPrograma"(:p_IdEmpresa,
																	:p_IdUsuario,
																	:p_IdModulo,
																	0,
																	'CERTIFICACIONMASIVASOLICITUDESFFMM',
																	1,
																	:v_parametros,
																	'S',
																	v_p_out_IdPrograma,
																	v_p_out_AdvertenciasContador,
																	v_p_out_AdvertenciasDetalle,
																	v_p_out_ErroresContador,
																	v_p_out_ErroresDetalle);
						
			p_out_Resultado = SELECT  0 as "IdInterno", "IdPrograma" AS "IdEstado"
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
					,'CERT_MASIVA_BATCH_FFMM' AS "IdTipoRegistro"
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
