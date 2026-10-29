SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Contabilidad.BD.Servicios::ITZ.ModificarEstadoTipoContabilidad";

CREATE PROCEDURE 
-- 
--  Proposito: Modifica
--			   
--  Empresa: Ingeniería ProNet IT SpA.
--  Autor: Hans Muller :: HMR
--  Fecha Creacion: 13/10/2016
--  Id Servicio = 12022
--  
"BSCL.Contabilidad.BD.Servicios::ITZ.ModificarEstadoTipoContabilidad" (
	--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          		BIGINT,
    IN p_IdUsuario          		BIGINT,
    IN p_IdModulo           		NVARCHAR(3),
	-- Variables de Entrada
	IN p_IdTipoContabilidad			BIGINT,
	IN p_Estado						NVARCHAR(1),
	-- Parametros de Salida
	OUT p_out_AdvertenciasContador 	INT,
	OUT p_out_AdvertenciasDetalle 	TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100), "RequiereAutorizacion" NVARCHAR(40), "Descripcion" NVARCHAR(5000)) DEFAULT EMPTY,
	OUT p_out_ErroresContador 		INT,
	OUT p_out_ErroresDetalle 		TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)) DEFAULT EMPTY
 )
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	AS
	   
BEGIN
	--DECLARE v_IdGrupoLog BIGINT;
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	
	DECLARE v_out_AdvertenciasDetalle	TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" NVARCHAR(100),"NombreParametro" NVARCHAR(40))
										DEFAULT SELECT 0 as "IdInterno",'' as "Codigo",'' as "RequiereAutorizacion",'' as "ValorParametro",'' as  "NombreParametro" FROM DUMMY WHERE 1=2;
	DECLARE v_out_ErroresDetalle		TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100),"ValorParametro" NVARCHAR(100),"NombreParametro" NVARCHAR(40))
										DEFAULT SELECT 0 as "IdInterno",'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" FROM DUMMY WHERE 1=2;
	
	DECLARE v_count_operaciones			BIGINT;
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT COUNT(1) INTO p_out_ErroresContador 
		  FROM :v_out_ErroresDetalle;
		
		SELECT COUNT(1) INTO p_out_AdvertenciasContador 
		  FROM :v_out_AdvertenciasDetalle; 	
		
		v_TablaCodigosMensajes = SELECT "Codigo" 			AS "CodigoMensaje",
								        "NombreParametro" 	AS "CodigoDato",
  								        "ValorParametro" 	AS "ValorDato" 
								   FROM :v_out_ErroresDetalle
								 UNION ALL
								 SELECT "Codigo" 			AS "CodigoMensaje",
								        "NombreParametro" 	AS "CodigoDato",
								        "ValorParametro" 	AS "ValorDato" 
								   FROM :v_out_AdvertenciasDetalle;

		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes, v_out_ErroresDetalleMensajes);
		
		p_out_ErroresDetalle = 	SELECT errores."IdInterno",
									   errores."Codigo",
									   msj."Descripcion"
 								  FROM :v_out_ErroresDetalle errores INNER JOIN :v_out_ErroresDetalleMensajes msj on 
 								  														errores."Codigo" = msj."Codigo";

		p_out_AdvertenciasDetalle = SELECT adv."IdInterno",
									       adv."Codigo",
 									       adv."RequiereAutorizacion",
 									       msj."Descripcion"
 								    FROM :v_out_AdvertenciasDetalle adv INNER JOIN :v_out_ErroresDetalleMensajes msj on 
 								    													adv."Codigo" = msj."Codigo";
	END;

	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;

	--------------------
	--Negocio Aqui
	--------------------
	BEGIN -- proceso de negocio      
    	--PARAMETROS REGLAS NEGOCIO
		DECLARE v_resultadoRegla 			NVARCHAR(40);
		DECLARE v_Operaciones_Modificar		BIGINT;
		DECLARE v_Operaciones_Existentes	BIGINT;

		--*******************************
		--*VALIDACION REGLAS BASICAS *
		--*******************************
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa
		                                                                 ,:p_IdUsuario
		                                                                 ,:p_IdModulo
		                                                                 ,:v_out_ErroresDetalle_Basicos
		                                                                 ,:v_resultadoRegla);
		IF :v_resultadoRegla = 'RECH' THEN
			v_out_ErroresDetalle = SELECT 0 	AS "IdInterno",
			                              '' 	AS "Codigo",
			                              '' 	AS "ValorParametro",
			                              '' 	AS "NombreParametro"
			                         FROM :v_out_ErroresDetalle_Basicos;
			SIGNAL SALIDA_CONTROLADA;
		END IF;

		UPDATE "BSCL.Contabilidad.BD::ITZ.ContabilidadTipo"
		   SET "Estado" = :p_Estado 
		 WHERE "IdTipoContabilidad" = :p_IdTipoContabilidad;
		
		UPDATE "BSCL.Contabilidad.BD::ITZ.ContabilidadDefinicionAsiento"
		   SET "Estado" = :p_Estado 
		 WHERE "IdTipoContabilidad" = :p_IdTipoContabilidad 
		   AND "Estado" NOT IN ('A','E');

		UPDATE "BSCL.Contabilidad.BD::ITZ.ContabilidadTipoCentralizacionTipo"
		   SET "Estado" = :p_Estado 
		 WHERE "IdTipoContabilidad" = :p_IdTipoContabilidad 
		   AND "Estado" NOT IN ('A','E');

		UPDATE "BSCL.Contabilidad.BD::ITZ.ContabilidadTipoAsientoCondicion"
		   SET "Estado" = :p_Estado 
		 WHERE "IdTipoContabilidad" = :p_IdTipoContabilidad 
		   AND "Estado" NOT IN ('A','E');
		
		UPDATE "BSCL.Contabilidad.BD::ITZ.ContabilidadTipoGlosa"
		   SET "Estado" = :p_Estado 
		 WHERE "IdTipoContabilidad" = :p_IdTipoContabilidad 
		   AND "Estado" NOT IN ('A','E');

		UPDATE "BSCL.Contabilidad.BD::ITZ.ContabilidadDefinicionTipoEvento" AS ContabilidadDefinicionTipoEvento
		   SET ContabilidadDefinicionTipoEvento."Estado" = :p_Estado 
		 WHERE ContabilidadDefinicionTipoEvento."IdDefinicionAsiento" IN (SELECT CTB_itzContabilidadDefinicionAsiento."IdDefinicionAsiento" 
											 								FROM "BSCL.Contabilidad.BD::ITZ.ContabilidadDefinicionAsiento" AS CTB_itzContabilidadDefinicionAsiento
											 							   WHERE CTB_itzContabilidadDefinicionAsiento."IdTipoContabilidad" = :p_IdTipoContabilidad)
		   AND ContabilidadDefinicionTipoEvento."Estado" NOT IN ('A','E');

		UPDATE "BSCL.Contabilidad.BD::ITZ.ContabilidadTipoAsientoCondicion"
		   SET "Estado" = :p_Estado
		 WHERE "IdDefinicionAsiento" IN (SELECT CTB_itzContabilidadDefinicionAsiento."IdDefinicionAsiento" 
		 								   FROM "BSCL.Contabilidad.BD::ITZ.ContabilidadDefinicionAsiento" AS CTB_itzContabilidadDefinicionAsiento
		 								  WHERE CTB_itzContabilidadDefinicionAsiento."IdTipoContabilidad" = :p_IdTipoContabilidad) 
		   AND "Estado" NOT IN ('A','E');
		   
		--LOG DE AUDITORIA (Inicio)
		BEGIN
			DECLARE p_IdLog BIGINT;
			DECLARE v_fechaHoraSistema NVARCHAR(40);

			SELECT "BSCL.Plataforma.BD.Secuencias::Log".NEXTVAL INTO p_IdLog FROM dummy;

			call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
				p_IdEmpresa => p_IdEmpresa,
				p_out_FechaHoraSistema => v_fechaHoraSistema
			);

			v_log = SELECT :p_IdLog AS "IdCabeceraLog"
							,CASE WHEN :p_Estado = 'E' THEN 'ELIMINACION'
								WHEN :p_Estado IN ('B','V') THEN 'MODIFICACION'
								ELSE ''
							END AS "IdTipoLog"
							,:p_IdModulo AS "IdModulo"
							,:p_IdUsuario AS "IdUsuario"
							,:p_IdEmpresa AS "IdEmpresa"
							,TO_VARCHAR (COALESCE(:v_fechaHoraSistema, '0001-01-01'), 'YYYY-MM-DD') as "FechaSistemaLog"
							,:p_IdUsuario AS "IdUsuarioAtendido"
							,0  AS "IdPersona"
							,0  AS "IdCliente"
							,'' AS "EstadoCliente"
							,'' AS "NombreCliente"
							,'' AS "IdTipoDocumentoIdentidad"
							,'' AS "IdDocumentoIdentidad"
							,0  AS "IdPortafolio"
							,'' AS "EstadoPortafolio"
							,'MODIFICAR_ESTADO_TIPO_CONTABILIDAD' AS "IdTipoRegistro"
							,'' AS "IdRegistro"
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
							,0  AS "IdProceso" 
							,'' AS "EstadoProceso"
							,'ModificarEstadoTipoContabilidad' AS "NombreProceso"
					FROM DUMMY;

			v_LogDetalle =
		 			SELECT
		 				:p_IdLog AS "IdCabeceraLog"
		 				,CASE WHEN :p_Estado = 'E' THEN 'ELIMINACION'
		 					WHEN :p_Estado IN ('B','V') THEN 'MODIFICACION'
		 					ELSE ''
		 				END AS "SubRegistro"
		 				,'Estado' AS "CampoRegistro"
		 				,'' AS "ValorAntiguo"
		 				,CASE WHEN :p_Estado = 'E' THEN 'Eliminado'
		 					WHEN :p_Estado = 'B' THEN 'Bloqueado'
		 					WHEN :p_Estado = 'V' THEN 'Vigente'
		 					ELSE ''
		 				END AS "ValorNuevo"
		   			FROM DUMMY;

			CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle => :v_LogDetalle);
		END;
		--LOG DE AUDITORIA (Fin)
	END;
END;