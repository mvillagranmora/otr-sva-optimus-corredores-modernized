SET SCHEMA "BSCL";  

DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios.CIE::ConsultarResumenCierreDiario";

CREATE PROCEDURE 
-- 
--  Proposito: Consulta y calcula el resumen de los procesos del cierre diario para el cierre de módulo
--             Si el porceso fue cerrado, se muestras valores de la revisión, en caso contrario se calculan
--  Autor: JVILLAGRAN - BCS
--  Fecha Creacion: 14/02/2017
--   
"BSCL.Tesoreria.BD.Servicios.CIE::ConsultarResumenCierreDiario" (
    -- Parametros de Validacion
    IN p_IdEmpresa 						BIGINT,  
    IN p_IdUsuario 						BIGINT, 
    IN p_IdModulo 						NVARCHAR(3),    
   	IN p_simularOperacion 				NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
    IN p_UsaReglasNegocio 				NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.   
	IN p_FechaCierre					NVARCHAR(10),

 	OUT p_out_ResumenCierreDiario  		TABLE (
										 	"IdModuloFuncionProceso"			BIGINT,
											"NombreFuncionProceso"         		NVARCHAR(100),
											"DescripcionFuncionProceso"	   		NVARCHAR(100),
											"PermiteEjecucion"             		NVARCHAR(1),												
											"IdRevisionCierreDiario"       		BIGINT,
											"IdUsuario"                    		BIGINT,												
											"CodigoUsuario"				   		NVARCHAR(40),															
											"FechaCierre" 			   	   		NVARCHAR(10),
											"IdEstadoRevisionCierreDiario" 		NVARCHAR(3),
											"NombreEstadoRevisionCierreDiario" 	NVARCHAR(100),
											"TotalOperaciones"			   		INTEGER,
											"TotalConProblemas"			   		INTEGER,
											"Resultado"					   		NVARCHAR(40)												
										) DEFAULT EMPTY,
		

    --Salidas para mensajes de advertencias y errores
    OUT p_out_AdvertenciasContador 		INT,	  
    OUT p_out_AdvertenciasDetalle  		TABLE ("Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(5000)) DEFAULT EMPTY,
  	OUT p_out_ErroresContador      		INT,
    OUT p_out_ErroresDetalle       		TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)) DEFAULT EMPTY
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
	READS SQL DATA AS   -- Indica que servicio es solo de lectura

  BEGIN
  	--------------------
	--DECLARE Manejo de Errores
	--------------------	  
	DECLARE SALIDA_CONTROLADA 				CONDITION; 
	DECLARE SALIDA_CONTROLADA_NO_DATA 		CONDITION;
	DECLARE v_TablaCodigosMensajes			TABLE("IdInterno" BIGINT,"Codigo" NVARCHAR(100),"CodigoDato1" NVARCHAR(100),"ValorDato1" NVARCHAR(520),"CodigoDato2" NVARCHAR(100),"ValorDato2" NVARCHAR(520))
											DEFAULT SELECT 0  as "IdInterno",'' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" FROM DUMMY WHERE 1=2;
	
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle;
		SELECT count(1) into p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
	END;
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA     
	BEGIN		
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,:v_erroroutmsg);
		p_out_ErroresDetalle =  SELECT "Codigo", "Descripcion" FROM :v_erroroutmsg;
		SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
	END;
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		DECLARE v_errorExt 	NVARCHAR(40);	
		IF ::SQL_ERROR_CODE > 10000 THEN 
			--error pseudo-controlado pero desde un Sub-SP
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL  
								   SELECT v_errorExt,::SQL_ERROR_MESSAGE FROM dummy;				
			SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL;
		END IF;
	END;

	p_out_AdvertenciasContador		:= 0;
	p_out_ErroresContador			:= 0;
	

	--------------------
	-- VALIDACIONES
	--------------------	   
	IF p_UsaReglasNegocio = 'S' THEN
	BEGIN
		DECLARE v_resultadoRegla	NVARCHAR(40);
    
		---Validaciones datos de entrada
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,v_Errores,:v_resultadoRegla);
		IF :v_resultadoRegla = 'RECH' THEN
			 SIGNAL SALIDA_CONTROLADA_NO_DATA; 	
		END IF;
	END; 
	END IF; 

	--------------------
	--SIMULACION
	--------------------
	IF (:p_simularOperacion = 'S') THEN		
		SIGNAL SALIDA_CONTROLADA;
	END IF;

	--------------------
	--LOGICA
	--------------------
	BEGIN
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			v_TablaCodigosMensajes = 
			SELECT 0 as "IdInterno",'DATA_NO_EXISTE' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END;


		-------------------------------------------------------
		-- OBTIENE LOS PROCESOS A REVISAR
		-------------------------------------------------------
		v_ResumenCierreDiario =
		SELECT
			 MFP."IdModuloFuncionProceso"
			,MFP."NombreFuncionProceso" 
			,MFP."Descripcion" AS "DescripcionFuncionProceso"
			,MFP."PermiteEjecucion"
			,RCD."IdRevisionCierreDiario"
			,COALESCE(RCD."IdUsuario",0) AS "IdUsuario"
			,COALESCE(USU."CodigoUsuario",'') AS "CodigoUsuario"			
			,RCD."FechaCierre"
			,COALESCE(RCD."IdEstado",'ABT') AS "IdEstadoRevisionCierreDiario"        -- Al no existir registro de cierre, está Abierto
			,COALESCE(EST."Nombre",'ABIERTO') AS "NombreEstadoRevisionCierreDiario"  -- Al no existir registro de cierre, está Abierto
			,(CASE WHEN COALESCE(RCD."IdRevisionCierreDiario",0) = 0 THEN 'N' ELSE 'S' END) "ExisteRevision"    -- Especifica si existe o no revisión de cierre para la fecha indicada
			,COALESCE(RCD."TotalOperaciones",0)  AS "TotalOperaciones"      -- Si existe revisión se muestra el dato guardado, en caso contrario se calcula mas abajo
			,COALESCE(RCD."TotalConProblemas",0)  AS "TotalConProblemas"    -- Si existe revisión se muestra el dato guardado, en caso contrario se calcula mas abajo
			,COALESCE(RCD."Resultado",'') AS "Resultado"                    -- Si existe revisión se muestra el dato guardado, en caso contrario se calcula mas abajo
		FROM "BSCL.Plataforma.BD::CRE.ModuloEmpresa" AS ME
		INNER JOIN "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" AS MFP ON (MFP."IdCREModuloEmpresa" = ME."IdCREModuloEmpresa")
		LEFT JOIN "BSCL.Tesoreria.BD::CIE.RevisionCierreDiario" AS RCD ON (RCD."NombreFuncionProceso" = MFP."NombreFuncionProceso" AND RCD."FechaCierre" = :p_FechaCierre)		
		LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS USU ON (USU."IdUsuario" = RCD."IdUsuario")		
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EST ON (EST."IdEstado" = RCD."IdEstado")
		WHERE ME."IdModulo"  = 'TES'
		AND   MFP."IdEstado" = 'VIG'
		AND   MFP."PermiteEjecucion" = 'A'  -- Condicion Ejecución: Se pueden ejecutar cuando el módulo está abierto
		ORDER BY MFP."OrdenEjecucion" ASC;
		--SELECT 'v_ResumenCierreDiario', * FROM :v_ResumenCierreDiario;   -- REVISAR


		----------------------------------------------------------
		-- (INF_CMBT_SLIQ): INFORME DE COMPROBANTES SIN LIQUIDAR
		----------------------------------------------------------
		v_ComprobantesSinLiquidar =
		SELECT
			'INF_CMBT_SLIQ' AS "NombreFuncionProceso",
			COALESCE(SUM("TotalPorEstado"),0) AS "TotalOperaciones",
			COALESCE(SUM("SinLiquidar"),0)    AS "TotalConProblemas",
			CASE WHEN COALESCE(SUM("SinLiquidar"),0) = 0 THEN 'CUADRATURA OK' ELSE 'EXISTEN COMPROBANTES POR LIQUIDAR' END AS "Resultado"
		FROM
		(
			SELECT 
				"IdEstado",
				Count(1) AS "TotalPorEstado",
				CASE WHEN "IdEstado" = 'LIQ' THEN 0 ELSE Count(1) END AS "SinLiquidar"
			FROM "BSCL.Tesoreria.BD.Vistas::ConsultarComprobante"   -- Id Servicio 1020
			WHERE "IdEmpresa" 			= :p_IdEmpresa
			AND   "FechaLiquidacion" 	= :p_FechaCierre
			AND   "IdEstado" NOT IN ('ANU','BLO','MOD') -- No considerar: Anulado, Bloqueado, Modificado. Los liquidados los deja para obtener el total
			GROUP BY "IdEstado"
		);
		--SELECT 'ComprobantesSinLiquidar', * FROM :v_ComprobantesSinLiquidar;  -- REVISAR



		----------------------------------------------------------------------
		-- (INF_DOCT_SCER): INFORME DE DOCUMENTOS SIN CERTIFICAR (Cheque, Vale Vista, Boleta de Depósito, Efectivo, Extra Banco)
		---------------------------------------------------------------------
		v_DocumentosSinCertificar =		
		SELECT
			'INF_DOCT_SCER' AS "NombreFuncionProceso",
			COALESCE(SUM("TotalPorEstado"),0) AS "TotalOperaciones",
			COALESCE(SUM("SinCertificar"),0)  AS "TotalConProblemas",
			CASE WHEN COALESCE(SUM("SinCertificar"),0) = 0 THEN 'CUADRATURA OK' ELSE 'EXISTEN DOCUMENTOS POR CERTIFICAR' END AS "Resultado"
		FROM
		(
			SELECT
				"IdEstado",
				Count(1) AS "TotalPorEstado",			
				CASE WHEN "IdEstado" IN('CER','LIQ','EBV','NDP') THEN 0 ELSE Count(1) END AS "SinCertificar"   -- Solo los ING se consideran como "Sin Certificar"									
			FROM "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePago"
			WHERE "IdEmpresa"       = :p_IdEmpresa
			AND "FechaLiquidacion"  = :p_FechaCierre
			AND "IdTipoMovimiento"  = 'MOVING'  -- Solo ingresos
			AND "IdEstado" NOT IN ('ANU','GIR', 'EMI')   -- No considerar: Anulado / Girado(sólo egresos) / Emitido (se generó el cheque o vale vista, solo egresos)
														 -- Se consideran: Certificado / Liquidado / Ingresado / Enviado a boveda/ Enviado a Depósito (De este grupo se calculan los certificados y nos no certificados)
			AND "IdMedioDePago" IN (
				'CHE',   -- Cheque
				'VVI',   -- Vale Vista
				'BDE',   -- Boleta de Depósito
				'EFE',   -- Efectivo
				'EBA'    -- Extra Banco
			)
			GROUP BY "IdEstado"		
		);		
		--SELECT 'DocumentosSinCertificar', * FROM :v_DocumentosSinCertificar;  -- REVISAR
		


		
		--------------------------------------
		-- PROCESAR LA SALIDA
		--------------------------------------
		p_out_ResumenCierreDiario =
		SELECT
		     RCD."IdModuloFuncionProceso"
			,RCD."NombreFuncionProceso" 
			,RCD."DescripcionFuncionProceso"
			,RCD."PermiteEjecucion"
			,RCD."IdRevisionCierreDiario"
			,RCD."IdUsuario"
			,RCD."CodigoUsuario"			
			,RCD."FechaCierre"
			,RCD."IdEstadoRevisionCierreDiario"
			,RCD."NombreEstadoRevisionCierreDiario"
	
			,CASE   -- Si existe revisión se carga el dato guadado, en caso contrario se muestra el calculado
				WHEN RCD."ExisteRevision" = 'S' THEN RCD."TotalOperaciones" 
				ELSE
					(CASE 
						WHEN COALESCE(CSL."NombreFuncionProceso",'') <> '' THEN CSL."TotalOperaciones"  -- Comprobantes sin liquidar
						WHEN COALESCE(DSC."NombreFuncionProceso",'') <> '' THEN DSC."TotalOperaciones"  -- Documentos sin certificar 
						ELSE RCD."TotalOperaciones"
					END)
			END	AS "TotalOperaciones"

			,CASE   -- Si existe revisión se carga el dato guadado, en caso contrario se muestra el calculado
				WHEN RCD."ExisteRevision" = 'S' THEN RCD."TotalConProblemas"
				ELSE
					(CASE
						WHEN COALESCE(CSL."NombreFuncionProceso",'') <> '' THEN CSL."TotalConProblemas"  -- Comprobantes sin liquidar
						WHEN COALESCE(DSC."NombreFuncionProceso",'') <> '' THEN DSC."TotalConProblemas"  -- Documentos sin certificar 
						ELSE RCD."TotalConProblemas"
					END)
			END	AS "TotalConProblemas"

			,CASE   -- Si existe revisión se carga el dato guadado, en caso contrario se muestra el calculado
				WHEN RCD."ExisteRevision" = 'S' THEN RCD."Resultado"
				ELSE
					(CASE
						WHEN COALESCE(CSL."NombreFuncionProceso",'') <> '' THEN CSL."Resultado"          -- Comprobantes sin liquidar
						WHEN COALESCE(DSC."NombreFuncionProceso",'') <> '' THEN DSC."Resultado"          -- Documentos sin certificar 
						ELSE RCD."Resultado"
					END)
			END	AS "Resultado"

		FROM :v_ResumenCierreDiario AS RCD
		LEFT JOIN :v_ComprobantesSinLiquidar AS CSL ON (CSL."NombreFuncionProceso" = RCD."NombreFuncionProceso")		
		LEFT JOIN :v_DocumentosSinCertificar AS DSC ON (DSC."NombreFuncionProceso" = RCD."NombreFuncionProceso");


  	END;    
END;

  