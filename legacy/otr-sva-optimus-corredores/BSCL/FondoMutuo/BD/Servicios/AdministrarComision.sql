SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::AdministrarComision";

CREATE PROCEDURE
--  Proposito: Crear o Modificar Comision para una Serie de Fondo segun corresponda.
--  Autor: Gat-Blac 
--  Fecha Creacion: 18/06/2016
"BSCL.FondoMutuo.BD.Servicios::AdministrarComision" (
	 --VARIABLES OBLIGATORIAS
    IN p_IdEmpresa 								BIGINT,
    IN p_IdUsuario 								BIGINT,
    IN p_IdModulo 								NVARCHAR(3),
    IN p_UsaReglasNegocio 						NVARCHAR(1),
    
    IN p_Comision								TABLE	(
    														"IdInterno" 			BIGINT,
    														"IdSerieFondo"			NVARCHAR(40),
    														"Descripcion"			NVARCHAR(100),
    														"TipoComision"			NVARCHAR(40),
    														"FechaVigenciaDesde"	NVARCHAR(10),
    														"FechaVigenciaHasta"	NVARCHAR(10),
    														"PorcentajeLiberado"	DECIMAL(26,8),
    														"Estado"				NVARCHAR(3)
    													),
    IN p_TramoComision							TABLE	(
    														"IdInterno"				BIGINT,
    														"IdTramo"				BIGINT,
    														"DiasDesde"				BIGINT,
    														"DiasHasta"				BIGINT,
    														"PorcentajeComision"	DECIMAL(26,8)
    													),
    OUT p_out_Resultado							TABLE	(
    														"IdInterno" 			BIGINT,
    														"IdComision"			BIGINT,
    														"IdSerieFondo"			NVARCHAR(40),
    														"Descripcion"			NVARCHAR(100),
    														"TipoComision"			NVARCHAR(40),
    														"FechaVigenciaDesde"	DATE,
    														"FechaVigenciaHasta"	DATE,
    														"PorcentajeLiberado"	DECIMAL(26,8),
    														"Estado"				NVARCHAR(3)
    													),
    OUT p_out_Tramos							TABLE	(
    														"IdInterno" 			BIGINT,
    														"IdTramo"				BIGINT,
    														"IdComision"			BIGINT,
    														"DiasDesde"				BIGINT,
    														"DiasHasta"				BIGINT,
    														"PorcentajeComision"	DECIMAL(26,8)		
    													),
    OUT p_out_AdvertenciasContador 				INT,
	OUT p_out_AdvertenciasDetalle 				TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)),
	OUT p_out_ErroresContador 					INT,
	OUT p_out_ErroresDetalle 					TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(520))
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------
	DECLARE v_out_AdvertenciasDetalle 				TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
	DECLARE v_out_ErroresDetalle  					TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
	BEGIN
		v_out_AdvertenciasDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'N' as "RequiereAutorizacion",'' as "ValorParametro",'' as "NombreParametro" from dummy where 1=2;
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
			
			p_out_ErroresDetalle = SELECT "errores"."IdInterno",
										  "errores"."Codigo",
										  "msj"."Descripcion"
								   FROM :v_out_ErroresDetalle AS "errores"
									 INNER JOIN :v_out_ErroresDetalleMensajes AS "msj" 
									    ON "errores"."Codigo" = "msj"."Codigo";
			
			p_out_AdvertenciasDetalle = SELECT 	"adv"."IdInterno",
												"adv"."Codigo",
												"adv"."RequiereAutorizacion",
												"msj"."Descripcion"
										FROM :v_out_AdvertenciasDetalle AS "adv"
										  INNER JOIN :v_out_ErroresDetalleMensajes AS "msj" 
										     ON "adv"."Codigo" = "msj"."Codigo"; 
		END;
		
		p_out_AdvertenciasContador	:= 0;
		p_out_ErroresContador		:= 0;
		
		BEGIN
		DECLARE v_FechaSistema 				NVARCHAR(10);
		DECLARE v_count						BIGINT;
		DECLARE	v_count_comisiones			BIGINT;
		DECLARE v_count_comisiones_maestro	BIGINT;
		DECLARE v_count_comisiones_final    BIGINT;
		DECLARE v_count_tramos_incorrectos  BIGINT;
		DECLARE v_resultadoRegla 			NVARCHAR(40);
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'CREAR COMISION FONDO MUTUO','INICIO','INICIO');
		
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
		
		if :v_resultadoRegla = 'RECH' then
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
			SIGNAL SALIDA_CONTROLADA; 	
		end if;
		
		SELECT COUNT(1) as "Count" 
		into v_count_comisiones
		FROM :p_Comision;
		
		if (v_count_comisiones = 0) THEN
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", 'NO_SE_INGRESARON_SOLICITUDES_APORTE_RESCATE' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		SELECT TOP 1 COUNT("IdInterno") as "Count" into v_count
		FROM :p_Comision
		GROUP BY "IdInterno"
		ORDER BY "Count" DESC;
		
		if (v_count > 1) THEN
			v_out_ErroresDetalle = SELECT 0 AS "IdInterno", 'CORRELATIVO_INCORRECTO' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
		
		v_comisiones_entrada = SELECT p_Comision.*, :p_IdEmpresa AS "IdEmpresa", :v_FechaSistema AS "FechaSistema" FROM :p_Comision AS p_Comision;
		
		v_comisiones_maestro = SELECT 	"p_Comision"."IdInterno"
										,"p_Comision"."IdSerieFondo"
										,"p_Comision"."Descripcion"
										,"p_Comision"."TipoComision"
										,"p_Comision"."FechaVigenciaDesde"
										,"p_Comision"."FechaVigenciaHasta"
										,"p_Comision"."PorcentajeLiberado"
										,"p_Comision"."Estado"
										,"p_Comision"."IdEmpresa"
										,"p_Comision"."FechaSistema"
										,CASE WHEN COALESCE("SerieDeFondo"."IdSerieFondo",'-1')!='-1' THEN 'OK' ELSE 'ERR' END AS "ResultadoSerie"
										,"Comision"."IdComision"
							   FROM :v_comisiones_entrada AS "p_Comision"
								 LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieDeFondo" 
								   ON ("SerieDeFondo"."IdSerieFondo" = "p_Comision"."IdSerieFondo" 
								  AND "SerieDeFondo"."IdEmpresa" = "p_Comision"."IdEmpresa")
								  LEFT OUTER JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarComisionMaxima" AS "ComisionMAX" 
								   ON ("ComisionMAX"."IdSerieFondo" = "p_Comision"."IdSerieFondo")
								 LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.Comision" AS "Comision" 
								   ON ("Comision"."IdComision" = "ComisionMAX"."IdComision"
								   AND "Comision"."TipoComision" = 'SALIDA');
								  
		
		v_tramos_correctos = SELECT "p_Tramo"."IdInterno"
						 			,"p_Tramo"."IdTramo"
						 			,"p_Tramo"."DiasDesde"
						 			,"p_Tramo"."DiasHasta"
						 			,"p_Tramo"."PorcentajeComision"
						 			,CASE WHEN "p_Tramo"."DiasHasta" >= "p_Tramo"."DiasDesde" THEN 'OK' ELSE 'ERR' END "ResultadoTrama"
							 FROM :p_TramoComision AS "p_Tramo";
		
		BEGIN
			v_out_ErroresDetalle = SELECT "ComisionesMaestro"."IdInterno", 'SERIE_NO_EXISTE' as "Codigo", '' as "ValorParametro", '' AS "NombreParametro"
								   FROM :v_comisiones_maestro AS "ComisionesMaestro" 
								   WHERE "ResultadoSerie" = 'ERR';
								   
			v_out_ErroresDetalle = SELECT "TRAMOS"."IdInterno", 'SECUENCIA_INCORRECTA' as "Codigo", '' as "ValorParametro", '' AS "NombreParametro"
								   FROM :v_tramos_correctos AS "TRAMOS" 
								   WHERE "ResultadoTrama" = 'ERR'
								   UNION ALL
								   SELECT * 
								   FROM :v_out_ErroresDetalle;
								   
			SELECT COUNT(1) as "Count" 
			INTO v_count_comisiones_maestro
			FROM :v_comisiones_maestro 
			WHERE "ResultadoSerie" = 'OK';
			
			SELECT COUNT(1) as "Count" 
			INTO v_count_tramos_incorrectos
			FROM :v_tramos_correctos 
			WHERE "ResultadoTrama" = 'ERR';
			
			IF (v_count_comisiones_maestro = 0 OR v_count_tramos_incorrectos > 0) THEN
				SIGNAL SALIDA_CONTROLADA;
			END IF;
		END;
		
		v_comisiones_antes_verificacion = SELECT  "ComisionMaestro"."IdInterno"
												  ,"ComisionMaestro"."IdComision"
												  ,"ComisionMaestro"."IdSerieFondo"
												  ,"ComisionMaestro"."Descripcion"
												  ,"ComisionMaestro"."TipoComision"
												  ,"ComisionMaestro"."FechaVigenciaDesde"
												  ,"ComisionMaestro"."FechaVigenciaHasta"
												  ,"ComisionMaestro"."PorcentajeLiberado"
												  ,"ComisionMaestro"."Estado"
												  ,"ComisionMaestro"."FechaSistema"
												  ,"ComisionMaestro"."IdEmpresa"
										  FROM :v_comisiones_maestro AS "ComisionMaestro"
										  WHERE "ResultadoSerie" = 'OK';
		
		v_tramos_antes_verificacion = SELECT "p_Tramo"."IdInterno"
								 			,"p_Tramo"."IdTramo"
								 			,"p_Tramo"."DiasDesde"
								 			,"p_Tramo"."DiasHasta"
								 			,"p_Tramo"."PorcentajeComision"
									  FROM :p_TramoComision AS "p_Tramo" 
									  INNER JOIN :v_comisiones_antes_verificacion AS "ComisionPre" ON ("p_Tramo"."IdInterno" = "ComisionPre"."IdInterno");
		
		v_count_tramos = SELECT "IdInterno", COUNT(1) AS "Count" FROM :v_tramos_antes_verificacion GROUP BY "IdInterno";
		
		v_validaciones = SELECT "Tramo$1"."IdInterno", 
								MAX("Tramo$1"."DiasHasta") AS "Max", 
								count(1) as "Count", 
								SUM(CASE WHEN "Tramo$1"."DiasDesde" = 0 THEN "Tramo$1"."DiasHasta" - "Tramo$1"."DiasDesde" ELSE "Tramo$1"."DiasHasta" - "Tramo$1"."DiasDesde" + 1 END) AS "Suma"
						 FROM :v_tramos_antes_verificacion AS "Tramo$1" 
						   INNER JOIN :v_tramos_antes_verificacion AS "Tramo$2" 
						      ON ("Tramo$1"."IdInterno" = "Tramo$2"."IdInterno")
						 WHERE "Tramo$1"."DiasDesde" BETWEEN "Tramo$2"."DiasDesde" AND "Tramo$2"."DiasHasta" OR "Tramo$1"."DiasHasta" BETWEEN "Tramo$2"."DiasDesde" AND "Tramo$2"."DiasHasta"
						 GROUP BY "Tramo$1"."IdInterno";
		
		v_validacion = SELECT "Validacion"."IdInterno",
							  CASE WHEN "Validacion"."Count" = "Tramos"."Count" THEN 'OK' ELSE 'ERR' END AS "ResultadoCorrelativo",
							  CASE WHEN "Validacion"."Max" = "Validacion"."Suma" THEN 'OK' ELSE 'ERR' END AS "ResultadoSuma"
						FROM :v_validaciones AS "Validacion"
						  INNER JOIN :v_count_tramos AS "Tramos" 
						     ON ("Tramos"."IdInterno" = "Validacion"."IdInterno");
		
		BEGIN
			v_out_ErroresDetalle = SELECT "Validacion"."IdInterno", 'TRAMO_COMISION_SERIE_FONDO_INCORRECTA' as "Codigo", '' as "ValorParametro", '' AS "NombreParametro"
								   FROM :v_validacion AS "Validacion" 
								   WHERE "ResultadoCorrelativo" = 'ERR' OR "ResultadoSuma" = 'ERR';
								   
			SELECT COUNT(1) as "Count" 
			INTO v_count_comisiones_final
			FROM  :v_validacion 
			WHERE "ResultadoCorrelativo" = 'ERR' OR "ResultadoSuma" = 'ERR';
			
			IF (v_count_comisiones_final > 0) THEN
				SIGNAL SALIDA_CONTROLADA;
			END IF;
		END;
		
		v_comisiones_finales = SELECT  "ComisionValidacion"."IdInterno"
									  ,"ComisionValidacion"."IdComision"
									  ,"ComisionValidacion"."IdSerieFondo"
									  ,"ComisionValidacion"."Descripcion"
									  ,"ComisionValidacion"."TipoComision"
									  ,"ComisionValidacion"."FechaVigenciaDesde"
									  ,"ComisionValidacion"."FechaVigenciaHasta"
									  ,"ComisionValidacion"."PorcentajeLiberado"
									  ,"ComisionValidacion"."Estado"
									  ,"ComisionValidacion"."FechaSistema"
									  ,"ComisionValidacion"."IdEmpresa"
									 FROM :v_comisiones_antes_verificacion AS "ComisionValidacion"
									 LEFT JOIN :v_validacion AS "Validacion" ON ("Validacion"."IdInterno" = "ComisionValidacion"."IdInterno")
									 WHERE COALESCE("Validacion"."IdInterno",-1) = -1 OR ("Validacion"."ResultadoCorrelativo" = 'OK' AND "Validacion"."ResultadoSuma" = 'OK');
		
		v_comisiones_nuevas = SELECT "ComisionFinal"."IdInterno"
									 ,"ComisionFinal"."IdSerieFondo"
									 ,"ComisionFinal"."Descripcion"
									 ,"ComisionFinal"."TipoComision"
									 ,"ComisionFinal"."FechaVigenciaDesde"
									 ,"ComisionFinal"."FechaVigenciaHasta"
									 ,"ComisionFinal"."PorcentajeLiberado"
									 ,"ComisionFinal"."Estado"
									 ,"ComisionFinal"."FechaSistema"
									 ,"ComisionFinal"."IdEmpresa"
							  FROM  :v_comisiones_finales AS "ComisionFinal"
							  WHERE COALESCE("ComisionFinal"."IdComision",'-1')='-1';
		
		v_comisiones_exitentes = SELECT "ComisionFinal"."IdInterno"
										,"ComisionFinal"."IdComision"
										,"ComisionFinal"."IdSerieFondo"
										,"ComisionFinal"."Descripcion"
										,"ComisionFinal"."TipoComision"
										,"ComisionFinal"."FechaVigenciaDesde"
										,"ComisionFinal"."FechaVigenciaHasta"
										,"ComisionFinal"."PorcentajeLiberado"
										,"ComisionFinal"."Estado"
								FROM :v_comisiones_finales AS "ComisionFinal"
								WHERE COALESCE("ComisionFinal"."IdComision",'-1')!='-1';
		
		
		v_tramos_comisiones_nuevas = SELECT "p_Tramo"."IdInterno"
											, 0 AS "IdTramo"
											,"p_Tramo"."DiasDesde"
											,"p_Tramo"."DiasHasta"
											,"p_Tramo"."PorcentajeComision"
											,:v_FechaSistema AS "FechaSistema"
									 FROM :p_TramoComision AS "p_Tramo"
									   INNER JOIN :v_comisiones_nuevas AS "Comision" 
									      ON ("p_Tramo"."IdInterno" = "Comision"."IdInterno");
		
		v_tramos_comisiones_existentes = SELECT "p_Tramo"."IdInterno"
												,"p_Tramo"."IdTramo"
												,"p_Tramo"."DiasDesde"
												,"p_Tramo"."DiasHasta"
												,"p_Tramo"."PorcentajeComision"
												,:v_FechaSistema AS "FechaSistema"
										 FROM :p_TramoComision AS "p_Tramo"
										   INNER JOIN :v_comisiones_exitentes AS "Comision" ON ("p_Tramo"."IdInterno" = "Comision"."IdInterno");
		

		CALL "BSCL.FondoMutuo.BD.Servicios::CrearComision"(:v_comisiones_nuevas, :v_tramos_comisiones_nuevas, v_comisiones_nuevas_salida, v_tramos_comisiones_nuevas_salida);
		
		CALL "BSCL.FondoMutuo.BD.Servicios::ModificarComision"(:v_comisiones_exitentes,:v_tramos_comisiones_existentes, v_comisiones_existentes_salida, v_tramos_comisiones_existentes_salida);

		p_out_Resultado = SELECT	"IdInterno"
									,"IdComision"
									,"IdSerieFondo"
									,"Descripcion"
									,"TipoComision"
									,"FechaVigenciaDesde"
									,"FechaVigenciaHasta"
									,"PorcentajeLiberado"
									,"Estado"
							FROM :v_comisiones_nuevas_salida
							UNION ALL
							SELECT	"IdInterno"
									,"IdComision"
									,"IdSerieFondo"
									,"Descripcion"
									,"TipoComision"
									,"FechaVigenciaDesde"
									,"FechaVigenciaHasta"
									,"PorcentajeLiberado"
									,"Estado"
							FROM :v_comisiones_existentes_salida;

		p_out_Tramos = SELECT 	"IdInterno"
							 	,"IdTramo"
							 	,"IdComision"
							 	,"DiasDesde"
							 	,"DiasHasta"
							 	,"PorcentajeComision"
						FROM :v_tramos_comisiones_nuevas_salida
						UNION ALL
						SELECT 	"IdInterno"
							 	,"IdTramo"
							 	,"IdComision"
							 	,"DiasDesde"
							 	,"DiasHasta"
							 	,"PorcentajeComision"
						FROM :v_tramos_comisiones_existentes_salida;
		
		v_log = SELECT 
					"IdComision" AS "IdCabeceraLog"
					,'INGRESO' AS "IdTipoLog"
					,'FMM' AS "IdModulo"
					,:p_IdUsuario AS "IdUsuario"
					,:p_IdEmpresa AS "IdEmpresa"
					,:v_FechaSistema AS "FechaSistemaLog"
					,:p_IdUsuario AS "IdUsuarioAtendido"
					,0 AS "IdPersona"
					,0 AS "IdCliente"
					,'' AS "EstadoCliente"
					,'' AS "NombreCliente"
					,'' AS "IdTipoDocumentoIdentidad"
					,'' AS "IdDocumentoIdentidad"
					,0 AS "IdPortafolio"
					,'' AS "EstadoPortafolio"
					,'INSTRUMENTO_FONDO_MUTUO' AS "IdTipoRegistro"
					,"IdComision" AS "IdRegistro"
					,"Estado" AS "EstadoRegistro"
					,'' AS "IdTipoOperacion"
					,0 AS "Folio"
					,0 AS "Cantidad"
					,0 AS "PrecioTasa"
					,0 AS "Monto"
					,'' AS "IdMonedaOrigen"
					,'' AS "IdMonedaLiquidacion"
					,'' AS "TipoCambio"
					,"IdSerieFondo" AS "Nemo"
					,0 AS "IdProceso"
					,'' AS "EstadoProceso"
					,'' AS "NombreProceso"
				FROM :v_comisiones_nuevas_salida
				UNION ALL
				SELECT 
					"IdComision" AS "IdCabeceraLog"
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
					,'INSTRUMENTO_FONDO_MUTUO' AS "IdTipoRegistro"
					,"IdComision" AS "IdRegistro"
					,"Estado" AS "EstadoRegistro"
					,'' AS "IdTipoOperacion"
					,0 AS "Folio"
					,0 AS "Cantidad"
					,0 AS "PrecioTasa"
					,0 AS "Monto"
					,'' AS "IdMonedaOrigen"
					,'' AS "IdMonedaLiquidacion"
					,'' AS "TipoCambio"
					,"IdSerieFondo" AS "Nemo"
					,0 AS "IdProceso"
					,'' AS "EstadoProceso"
					,'' AS "NombreProceso"
				FROM :v_comisiones_existentes_salida;
			
		v_DETALLELOG = SELECT 0 AS "IdCabeceraLog",'' as "SubRegistro", '' as "CampoRegistro", '' as "ValorAntiguo", '' as "ValorNuevo" from dummy where 1=2;
		
		CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle => :v_DETALLELOG);
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'CREAR COMISION FONDO MUTUO','FIN','FIN');
		
		SIGNAL SALIDA_CONTROLADA;
		
		END;
	END;
END;