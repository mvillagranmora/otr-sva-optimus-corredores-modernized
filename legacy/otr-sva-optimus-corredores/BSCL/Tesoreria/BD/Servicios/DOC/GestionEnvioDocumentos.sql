SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios.DOC::GestionEnvioDocumentos";

CREATE PROCEDURE
-- 
--  Proposito: - Crear un nuevo envio de documentos a tesoreria
--             - Cambiar el estado de un envio de documento. Dependiendo el estado del detalle actualiza estado del encabezado
--  Autor: JVILLAGRAN - BCS
--  Fecha Creacion: 08/02/2017
--   
"BSCL.Tesoreria.BD.Servicios.DOC::GestionEnvioDocumentos" (
    -- Parametros de Validacion
    IN p_IdEmpresa 						BIGINT,  
    IN p_IdUsuario 						BIGINT, 
    IN p_IdModulo 						NVARCHAR(3),
    IN p_simularOperacion 				NVARCHAR(1),	-- S/N se usa en caso de servicios anidados
    IN p_UsaReglasNegocio 				NVARCHAR(1),	-- S/N se usa en caso de servicios anidados

    -- Parametros de Entrada   
    IN p_TipoEjecucion					NVARCHAR(3),	-- EST: Cambio de estado(aceptado, devuelto)
														-- ING: Crear un nuevo envio de documentos	
		
	IN p_EnvioDocumento					TABLE (
												"IdEnvioDocumento"				BIGINT,    -- Para ING(crear ED) este número es sólo referencial
												"IdCaja"						BIGINT,	
												"IdMedioDePago" 				NVARCHAR(3),
												"IdMoneda"  					NVARCHAR(3),
												"IdEstado"  					NVARCHAR(3)
										      ),
															

	IN p_EnvioDocumentoDetalle			TABLE (
												"IdEnvioDocumento"				BIGINT,
												"IdEnvioDocumentoDetalle" 		BIGINT,
												"IdComprobanteFormaDePago"		BIGINT,
												"IdEstado"                      NVARCHAR(3)
										      ),  
             	
    --Salidas para mensajes de advertencias y errores
	OUT p_out_InformacionContador  		INT,
 	OUT p_out_InformacionDetalle   		TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)) DEFAULT EMPTY,   
    OUT p_out_AdvertenciasContador 		INT,	  
    OUT p_out_AdvertenciasDetalle  		TABLE ("Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(5000)) DEFAULT EMPTY,
  	OUT p_out_ErroresContador      		INT,
    OUT p_out_ErroresDetalle       		TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)) DEFAULT EMPTY
)  	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
AS
BEGIN

	--------------------
	--DECLARE Manejo de Errores
	--------------------	  
	DECLARE SALIDA_EXITOSA					CONDITION;
	DECLARE SALIDA_CONTROLADA 				CONDITION; 
	DECLARE SALIDA_CONTROLADA_NO_DATA 		CONDITION;	
	DECLARE v_CountEnvioDocumentoDetalle    INT := 0;	
	DECLARE v_InformacionDetalle   			TABLE("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000))
											DEFAULT SELECT '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	DECLARE v_AdvertenciasDetalle  			TABLE("Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(5000))
											DEFAULT SELECT '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" FROM dummy where 1=2;
	DECLARE v_ErroresDetalle 				TABLE("Codigo" NVARCHAR(100), "Descripcion" NVARCHAR(5000), "CodigoDato1" NVARCHAR(100), "ValorDato1" NVARCHAR(520), "CodigoDato2" NVARCHAR(100), "ValorDato2" NVARCHAR(520))
											DEFAULT SELECT '' as "Codigo", '' as "Descripcion", '' as "CodigoDato1", '' as "ValorDato1", '' as "CodigoDato2", '' as "ValorDato2"  FROM dummy where 1=2;											
	DECLARE v_TablaCodigosMensajes			TABLE("IdInterno" BIGINT,"Codigo" NVARCHAR(100),"CodigoDato1" NVARCHAR(100),"ValorDato1" NVARCHAR(520),"CodigoDato2" NVARCHAR(100),"ValorDato2" NVARCHAR(520))
											DEFAULT SELECT 0  as "IdInterno",'' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" FROM DUMMY WHERE 1=2;
				        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle;
		SELECT count(1) into p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
	END;
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA     
	BEGIN
		v_TablaCodigosMensajes= SELECT 0 as "IdInterno",'DATA_NO_EXISTE' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" FROM DUMMY;	
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,:v_erroroutmsg);		
		p_out_ErroresDetalle =  SELECT "Codigo", "Descripcion" FROM :v_erroroutmsg;
		SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
	END;
	DECLARE EXIT HANDLER FOR SALIDA_EXITOSA     
	BEGIN
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,:v_erroroutmsg);
		p_out_InformacionDetalle = SELECT "Codigo", "Descripcion" FROM :v_erroroutmsg;
		SELECT COUNT("Codigo") INTO p_out_InformacionContador FROM :p_out_InformacionDetalle;		
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
	
	p_out_InformacionContador		:= 0;
	p_out_AdvertenciasContador		:= 0;
	p_out_ErroresContador			:= 0;


	---------------------------------------
	-- VALIDACIONES
	---------------------------------------
	IF p_UsaReglasNegocio = 'S' THEN
	BEGIN
		DECLARE v_resultadoRegla	NVARCHAR(40);
		
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,p_IdModulo,v_ErroresDetalle,:v_resultadoRegla);
		
		p_out_ErroresDetalle = SELECT "Codigo", "Descripcion" FROM :v_ErroresDetalle UNION ALL
						       SELECT "Codigo", "Descripcion" FROM :p_out_ErroresDetalle;
		
		IF :v_resultadoRegla = 'RECH' THEN
			 SIGNAL SALIDA_CONTROLADA;
		END IF;

		CALL "BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosGestionEnvioDocumentos"
		(
			 p_IdEmpresa  			 => :p_IdEmpresa
			,p_TipoEjecucion 		 => :p_TipoEjecucion		
			,p_EnvioDocumento 		 => :p_EnvioDocumento
			,p_EnvioDocumentoDetalle => :p_EnvioDocumentoDetalle
			,p_out_Resultado		 => v_ResultadoValidacion
		);
		
		-- Se eliminan los registros rechazados
		v_EnvioDocumentoDetalle	=	
		 SELECT
			 EDD."IdEnvioDocumento",
			 EDD."IdEnvioDocumentoDetalle",
			 EDD."IdComprobanteFormaDePago",
			 EDD."IdEstado"														 		
		  FROM :p_EnvioDocumentoDetalle AS EDD		   
		  INNER JOIN (SELECT DISTINCT "IdEnvioDocumentoDetalle" FROM :v_ResultadoValidacion WHERE "Resultado" = 'OK'
					  EXCEPT 
					  SELECT DISTINCT "IdEnvioDocumentoDetalle" FROM :v_ResultadoValidacion WHERE "Resultado" = 'RECH') AS VAL					  
				  ON EDD."IdEnvioDocumentoDetalle" = VAL."IdEnvioDocumentoDetalle";

		--SELECT 'EnvioDocumentoDetalle', * FROM :v_EnvioDocumentoDetalle; -- REVISAR


		--Se Consulta diccionario
		v_TablaCodigosMensajes =
		SELECT	DISTINCT 
			RV."IdEnvioDocumentoDetalle" as "IdInterno", 
			RV."Codigo", 
			CASE WHEN COALESCE(CFP."IdComprobante",0) <> 0 THEN 'IdComprobante' ELSE '' END as "CodigoDato1", 		
			CASE WHEN COALESCE(CFP."IdComprobante",0) <> 0 THEN CAST(CFP."IdComprobante" AS NVARCHAR(50)) ELSE ''	END as "ValorDato1",		
			'' as "CodigoDato2",
			'' as "ValorDato2"
		FROM :v_ResultadoValidacion AS RV
		LEFT JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS CFP ON (CFP."IdComprobanteFormaDePago" = RV."IdComprobanteFormaDePago")
		WHERE RV."Resultado" <> 'OK';

		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes, :v_erroroutmsg);
		
		p_out_ErroresDetalle = 	SELECT E."Codigo",E."Descripcion" FROM :v_erroroutmsg as E INNER JOIN :v_ResultadoValidacion as R ON R."IdEnvioDocumentoDetalle" = E."IdInterno" AND R."Codigo" = E."Codigo" AND R."Resultado" = 'RECH';		
		SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
		
		p_out_AdvertenciasDetalle =  SELECT E."Codigo",'' as "RequiereAutorizacion",E."Descripcion" FROM :v_erroroutmsg as E INNER JOIN :v_ResultadoValidacion as R ON R."IdEnvioDocumentoDetalle" = E."IdInterno" AND R."Codigo" = E."Codigo" AND R."Resultado" = 'ADVE';
		SELECT COUNT(1) INTO p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;

	END; 
	ELSE
	BEGIN
		v_EnvioDocumentoDetalle	= SELECT * FROM :p_EnvioDocumentoDetalle;
	END;
	END IF;  -- IF p_UsaReglasNegocio = 'S'


	-- Obtiene cantidad de registros
	SELECT Count(1)
	INTO v_CountEnvioDocumentoDetalle
	FROM :v_EnvioDocumentoDetalle;	
		
		    
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
		DECLARE v_FechaFechaHoraSistema 		SECONDDATE;		
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;
	 


		--------------------------------------------------------------
		-- CAMBIO DE ESTADO DE ENVIO DOCUEMNTO(aceptado / devuelto)
		--------------------------------------------------------------
	 	IF (:p_TipoEjecucion = 'EST') THEN
		
			 -- Actualiza estado del envio documentos detalle
		 	 UPDATE EDD
			 SET
				 EDD."IdEstado"				= PEDD."IdEstado"   -- ACP:aceptado    DEV:devuelto    ING:Ingresado
				,EDD."FechaHoraRevision" 	= CURRENT_UTCTIMESTAMP
				,EDD."IdUsuarioRevision"	= p_IdUsuario
			 FROM "BSCL.Tesoreria.BD::TES.EnvioDocumentoDetalle" AS EDD
			 INNER JOIN :v_EnvioDocumentoDetalle AS PEDD ON 
			 (
				    EDD."IdEnvioDocumento"        = PEDD."IdEnvioDocumento"
				AND EDD."IdEnvioDocumentoDetalle" = PEDD."IdEnvioDocumentoDetalle"			 
			 );


			-- Se reestablece el estado original de los documentos devueltos
			UPDATE CFP
			SET CFP."IdEstado" = 'CRT'   -- Certificado
			FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS CFP
			INNER JOIN :v_EnvioDocumentoDetalle AS EDD ON (EDD."IdComprobanteFormaDePago" = CFP."IdComprobanteFormaDePago")
			WHERE EDD."IdEstado" = 'DEV';  -- solo los devuelto



			-- Obtiene cantidad de documentos por estados para cada envío.
			v_CantidadDocumentosPorEstado =
			SELECT
				 "IdEnvioDocumento"
				,MAX("TotalDocumentos")     AS "TotalDocumentos"
				,MAX("DocumentosDevueltos") AS "DocumentosDevueltos"
				,MAX("DocumentosAceptados") AS "DocumentosAceptados"
				,MAX("DocumentosIngresados") AS "DocumentosIngresados"
			FROM
			(
					SELECT 
						EDD."IdEnvioDocumento" 
						,COUNT(1) over (Partition by EDD."IdEnvioDocumento") AS "TotalDocumentos"	
						,CASE 
							WHEN EDD."IdEstado" = 'DEV' THEN Count(1) over (Partition by EDD."IdEnvioDocumento", EDD."IdEstado")
							ELSE 0
						END AS "DocumentosDevueltos" -- Cantidad de documentos devueltos por envío
						,CASE 
							WHEN EDD."IdEstado" = 'ACP' THEN Count(1) over (Partition by EDD."IdEnvioDocumento", EDD."IdEstado")
							ELSE 0
						END AS "DocumentosAceptados" -- Cantidad de documentos aceptados por envío
						,CASE 
							WHEN EDD."IdEstado" = 'ING' THEN Count(1) over (Partition by EDD."IdEnvioDocumento", EDD."IdEstado")
							ELSE 0
						END AS "DocumentosIngresados"  -- Cantidad de documentos ingresados por envío
					FROM :v_EnvioDocumentoDetalle AS PEDD
					INNER JOIN  "BSCL.Tesoreria.BD::TES.EnvioDocumentoDetalle" AS EDD ON (EDD."IdEnvioDocumento" = PEDD."IdEnvioDocumento")
			)
			GROUP BY "IdEnvioDocumento";
				
			--SELECT 'CantidadDocumentosPorEstado', * FROM :v_CantidadDocumentosPorEstado;   -- REVISION

			 -- Actualiza el estado del encabezado de envío documentos, según el detalle
		 	 UPDATE ED
			 SET
				 ED."IdEstado"	= 				 
						 (CASE 
						 	WHEN (CDE."TotalDocumentos" = CDE."DocumentosDevueltos")  THEN 'DEV' -- Devueltos  (si todo el detalle fue devuelto)
						 	WHEN (CDE."TotalDocumentos" = CDE."DocumentosAceptados")  THEN 'ACP' -- Aceptados  (si todo el detalle fue aceptado)
						 	WHEN (CDE."TotalDocumentos" = CDE."DocumentosIngresados") THEN 'ING' -- Ingresados (si todo el detalle sigue ingresado)
							WHEN (CDE."DocumentosIngresados" = 0 AND CDE."DocumentosAceptados" > 0 AND CDE."DocumentosDevueltos" > 0) THEN 'DPA'  -- Parcialmente devueltos (si hay aceptados y devueltos y no quedan por revisar revisar)
							WHEN (CDE."DocumentosIngresados" > 0 AND (CDE."DocumentosAceptados" > 0 OR CDE."DocumentosDevueltos" > 0)) THEN 'REV' -- En Revision (si hay aceptados o devueltos y aun quedan por revisar)
							ELSE ED."IdEstado"
						END)				
			 FROM "BSCL.Tesoreria.BD::TES.EnvioDocumento" AS ED
			 INNER JOIN :v_CantidadDocumentosPorEstado AS CDE ON (CDE."IdEnvioDocumento" = ED."IdEnvioDocumento");
			 
-- 		 	OUT p_out_InformacionDetalle   		TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)) DEFAULT EMPTY,   
	 	END IF;  -- IF (:p_TipoEjecucion = 'EST')
	 	



		---------------------------------------------------
		-- CREAR UN NUEVO ENVIO DOCUMENTOS
		---------------------------------------------------
		IF (:p_TipoEjecucion = 'ING' AND v_CountEnvioDocumentoDetalle > 0) THEN		
				-- FechaHora de sistema
				CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema" (:p_IdEmpresa, v_FechaFechaHoraSistema);
				SELECT 'FechaFechaHoraSistema', v_FechaFechaHoraSistema FROM DUMMY;   -- REVISAR
		
				-- Genera secuencia para los nuevos envíos
				v_SecuenciaEnvioDocumento = 
				SELECT 
					ED."IdEnvioDocumento",    -- Para ING se usa sólo cono número referencial ya que recién se está creando el IdEnvioDocumento
					"BSCL.Tesoreria.BD.Secuencias::TES.EnvioDocumento".NEXTVAL as "NewIdEnvioDocumento"
				FROM :p_EnvioDocumento AS ED;			
				SELECT 'SecuenciaEnvioDocumento', * FROM :v_SecuenciaEnvioDocumento;   -- REVISAR			
				
				-- Inserta encabezado de los nuevos envios
				INSERT INTO "BSCL.Tesoreria.BD::TES.EnvioDocumento" ("IdEnvioDocumento","IdCaja","FechaHoraEnvio","IdEmpresa","IdMedioDePago","IdMoneda","IdUsuario","IdEstado")
				SELECT
					"NewIdEnvioDocumento",
					ED."IdCaja",
					:v_FechaFechaHoraSistema,
					:p_IdEmpresa,
					ED."IdMedioDePago",
					ED."IdMoneda",
					:p_IdUsuario,
					ED."IdEstado"     -- ING(Ingresado)
				FROM :p_EnvioDocumento AS ED
				INNER JOIN :v_SecuenciaEnvioDocumento AS SED ON (SED."IdEnvioDocumento" = ED."IdEnvioDocumento");
		
		
				-- Inserta detalle de los nuevos envios
				INSERT INTO "BSCL.Tesoreria.BD::TES.EnvioDocumentoDetalle"("IdEnvioDocumentoDetalle", "IdEnvioDocumento", "IdComprobanteFormaDePago", "FechaHoraRevision", "IdUsuarioRevision", "IdEstado")
				SELECT
					"BSCL.Tesoreria.BD.Secuencias::TES.EnvioDocumentoDetalle".NEXTVAL,
					SED."NewIdEnvioDocumento",
					EDD."IdComprobanteFormaDePago",
					'0001-01-01',
					null,				
					EDD."IdEstado"     -- ING(Ingresado)
				FROM :v_EnvioDocumentoDetalle AS EDD
				INNER JOIN :v_SecuenciaEnvioDocumento AS SED ON (SED."IdEnvioDocumento" = EDD."IdEnvioDocumento");
		
				
				-- Genera mensaje de exito
				v_TablaCodigosMensajes =
				SELECT	DISTINCT 
					SED."NewIdEnvioDocumento" as "IdInterno", 			
					'DOCUMENTOS_ENVIADOS_EXITOSAMENTE' as "Codigo", 			
					'IdEnvioDocumento' as "CodigoDato1",					
					CAST(SED."NewIdEnvioDocumento" AS NVARCHAR(50)) as "ValorDato1",		
					'' as "CodigoDato2",
					'' as "ValorDato2"					
				FROM :p_EnvioDocumento AS ED
				INNER JOIN :v_SecuenciaEnvioDocumento AS SED ON (SED."IdEnvioDocumento" = ED."IdEnvioDocumento");				
		
				SIGNAL SALIDA_EXITOSA;

		END IF;  -- IF (:p_TipoEjecucion = 'ING')


	END;
END;