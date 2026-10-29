SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarEventosDisponibles";
CREATE PROCEDURE 
-- 
--  Proposito: Consultar Registros
--			   
--  Empresa: Ingeniería ProNet IT SpA.
--  Autor: Hans Muller :: HMR
--  Fecha Creacion: 04/11/2016
--  Id Servicio = 12054
--  
"BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarEventosDisponibles" (
	--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          		BIGINT,
    IN p_IdUsuario          		BIGINT,
    IN p_IdModulo           		NVARCHAR(3),
    IN p_UsaReglasNegocio			NVARCHAR(1),
    --------------------------------------------------------------------------------------------                             
	IN p_Estado 					NVARCHAR(1), 
	IN p_FechaContable 				NVARCHAR(10),
	IN p_IdCentralizacion 			DECIMAL(18,2), 
	IN p_IdPreComprobante 			DECIMAL(18,2), 
	IN p_IdPreComprobanteAsiento 	DECIMAL(18,2), 
	IN p_NumeroMovimiento 			DECIMAL(18,2),	
	IN p_RutMovimiento 				NVARCHAR(11) 	default '0-0', 
	--------------------------------------- Parametros de Salida -------------------------------------------------------------------------
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

	DECLARE v_errorhashmsg 				TABLE ("Codigo" NVARCHAR(100), "Valor" NVARCHAR(520));
	DECLARE v_erroroutmsg  				NVARCHAR(520);	        	
	DECLARE v_count_operaciones			BIGINT;
	
	DECLARE v_out_AdvertenciasDetalle	TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" NVARCHAR(100),"NombreParametro" NVARCHAR(40)) DEFAULT 
												SELECT 0 AS "IdInterno", '' AS "Codigo", 'N' AS "RequiereAutorizacion", '' AS "ValorParametro",'' AS "NombreParametro" FROM DUMMY WHERE 1=2;										
	DECLARE v_out_ErroresDetalle		TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100),"ValorParametro" NVARCHAR(100),"NombreParametro" NVARCHAR(40)) DEFAULT 
												SELECT 0 AS "IdInterno", '' AS "Codigo", '' AS "ValorParametro", '' AS "NombreParametro" FROM DUMMY WHERE 1=2;

	DECLARE SALIDA_CONTROLADA CONDITION;
	 
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

	--------------------
	--Negocio Aqui
	--------------------
	BEGIN -- proceso de negocio      
    	--PARAMETROS REGLAS NEGOCIO
		DECLARE v_resultadoRegla 			NVARCHAR(40);
		DECLARE v_RutMovimiento 			NVARCHAR(40);		

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


						SELECT LPAD(LTRIM(:p_RutMovimiento),  11 , ' ')
				  INTO v_RutMovimiento
				  FROM DUMMY;

		
		  SELECT	0 																		AS "chkEvento",
	   				CTB_itzEvento."IdMovimiento" 											AS "IdMovimiento",
	   				CTB_itzEvento."IdEvento" 												AS "IdEvento",
	   				CTB_itzEventoTipo."Nombre" 												AS "NombreEvento", 
	   				ABS(CAST(CTB_itzEvento."Monto" AS decimal)) 							AS "Monto", 
	   				CTB_itzEvento."CodigoMoneda" 											AS "CodigoMoneda", 
	   				CAST(CAST(CTB_itzEvento."FechaProceso" AS timestamp) AS char(10)) 		AS "FechaProceso", 
	   				CTB_itzEvento."HoraProceso" 											AS "HoraProceso", 
	   				CTB_itzEvento."Estado" 													AS "EstadoEvento",
	   				'' 																		AS "Contabilizado", 
	   				CTB_itzMovimiento."Numero" 												AS "NumeroMovimiento", 
	   				CAST(CAST(CTB_itzMovimiento."Fecha" AS timestamp) AS char(10)) 			AS "FechaMovimiento", 
	   				CTB_itzMovimiento."DocumentoIdentidad" 									AS "RutMovimiento", 
	   				CTB_itzEvento."CodigoUsuario" 											AS "CodigoUsuario", 
	   				:p_IdPreComprobante 													AS "IdPreComprobante", 
	   				:p_IdPreComprobanteAsiento 												AS "IdPreComprobanteAsiento", 
	   				CTB_itzEvento."IdTipoEvento" 											AS "IdTipoEvento", 
	   				CTB_itzMovimientoTipo."IdTipoMovimiento" 								AS "IdTipoMovimiento", 
	   				CTB_itzMovimientoTipo."Nombre" 											AS "NombreMovimiento", 
	   				CTB_itzMovimiento."Cuenta" 												AS "CuentaMovimiento", 
	   				CTB_itzMovimiento."Estado" 												AS "EstadoMovimiento", 
	   				'' 																		AS "Operador", 
	   				'' 																		AS "EstadoDetallePreComprobante", 
	   				'' 																		AS "EstadoPreComprobante", 
	   				CTB_itzCentralizacionTipo."Nombre" 										AS "NombreCentralizacion" 
	   		FROM	"BSCL.Contabilidad.BD::ITZ.Evento"										AS CTB_itzEvento
	  INNER JOIN 	"BSCL.Contabilidad.BD::ITZ.EventoTipo"									AS CTB_itzEventoTipo
	  		  	ON 	CTB_itzEventoTipo."IdTipoEvento" 					= 		CTB_itzEvento."IdTipoEvento" 
	  INNER JOIN 	"BSCL.Contabilidad.BD::ITZ.Movimiento"									AS CTB_itzMovimiento
	  		  	ON 	CTB_itzMovimiento."IdMovimiento" 					= 		CTB_itzEvento."IdMovimiento" 
	  INNER JOIN 	"BSCL.Contabilidad.BD::ITZ.MovimientoTipo"								AS CTB_itzMovimientoTipo
	  		  	ON 	CTB_itzMovimientoTipo."IdTipoMovimiento" 			= 		CTB_itzMovimiento."IdTipoMovimiento" 
	  INNER JOIN 	"BSCL.Contabilidad.BD::ITZ.Centralizacion"								AS CTB_itzCentralizacion
	  		  	ON 	CTB_itzCentralizacion."IdCentralizacion" 			= 		CTB_itzEvento."IdCentralizacion" 
	  INNER JOIN 	"BSCL.Contabilidad.BD::ITZ.CentralizacionTipo"							AS CTB_itzCentralizacionTipo
	  		 	ON 	CTB_itzCentralizacionTipo."IdTipoCentralizacion" 	= 		CTB_itzCentralizacion."IdTipoCentralizacion" 
	  	   WHERE 	CTB_itzEvento."IdEmpresa" 							= 		:p_IdEmpresa 
		  	   AND 	(CTB_itzMovimiento."DocumentoIdentidad" 			= 		:v_RutMovimiento 		OR :v_RutMovimiento 	= '        0-0') 
	  	   	   AND 	(CTB_itzEvento."IdMovimiento" 						= 		:p_NumeroMovimiento 	OR :p_NumeroMovimiento 	= 0) 
	  	   	   AND 	(CTB_itzEvento."FechaContable" 						= 		:p_FechaContable) 
	  	   	   AND 	(CTB_itzEvento."IdCentralizacion" 					= 		:p_IdCentralizacion 	OR :p_IdCentralizacion 	= 0) 
	  	   	   AND 	(CTB_itzEvento."Estado" 							= 		:p_Estado 				OR :p_Estado 			= '') 
	  	   	   AND 	(
		  	   	 		CTB_itzEvento."IdEvento" 
			  	   	 	NOT IN 
				  	   	 	(
				  	   	 		SELECT	CTB_itzEventoPreComprobanteAsiento."IdEvento" 
				  	   	 		  FROM	"BSCL.Contabilidad.BD::ITZ.EventoPreComprobanteAsiento" 	AS CTB_itzEventoPreComprobanteAsiento
				  	   	 		 WHERE 	CTB_itzEventoPreComprobanteAsiento."IdPreComprobanteAsiento" = :p_IdPreComprobanteAsiento 
				  	   	 		   	AND CTB_itzEventoPreComprobanteAsiento."Estado" <> 'A'
				  	   	 	)
			  	   	 );		 
		    

	END;
END;
