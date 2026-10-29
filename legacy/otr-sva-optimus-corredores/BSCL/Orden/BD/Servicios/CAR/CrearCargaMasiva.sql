SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Orden.BD.Servicios::CAR.CrearCargaMasiva";
GO

CREATE PROCEDURE "BSCL.Orden.BD.Servicios::CAR.CrearCargaMasiva"(

    -- Parametros de Validacion                                                                                            
	IN p_IdEmpresa					BIGINT,
   	IN p_IdUsuario					BIGINT,
   	IN p_IdModulo					NVARCHAR(3),                                                                       
   	IN p_UsaReglasNegocio			NVARCHAR(1),	 -- S/N se usa en caso de servicios anidados.
	IN p_FechaCarga                 NVARCHAR(10),    -- Fecha de Sistema
	IN p_CargaMasiva                TABLE (
											"IdInterno"					BIGINT,           -- Solo Referencial(para relacionar maestro con detalle)
											"CodigoTipoCargaMasiva" 	NVARCHAR(40), 	  
											"NombreArchivo"         	NVARCHAR(100)     -- Nombre del archivo fisico(txt/csv)
										  ),
										  
	IN p_CargaMasivaDetalle			TABLE (  -- Parametros estandar para todo tipo de carma masiva
											"IdInterno"				BIGINT,               -- Solo Referencial(para relacionar maestro con detalle)
											"IdRegistro"			BIGINT,               -- Indicador del registro para trasponer
											"NombreCampo"			NVARCHAR(255), 		   
											"ValorCampo"			NVARCHAR(520)		  
									      ),

   	-- Parametros de Salida
   	OUT p_out_AdvertenciasContador	INT,
   	OUT p_out_AdvertenciasDetalle	TABLE ("Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),
   	OUT p_out_ErroresContador		INT,
   	OUT p_out_ErroresDetalle		TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000))
) 
   
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
  	--------------------
	--DECLARE Manejo de Errores
	--------------------	  
	DECLARE SALIDA_CONTROLADA 			CONDITION;
	DECLARE SALIDA_CONTROLADA_NO_DATA 	CONDITION;
	DECLARE v_resultadoRegla 			NVARCHAR(40);
	DECLARE v_CargaMasivaValidar TABLE ("IdCargaMasiva"     BIGINT, "CodigoTipoCargaMasiva"     NVARCHAR(40));
	
			
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle;
		SELECT count(1) into p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
	END;
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA
	BEGIN
		v_hash= SELECT 0 as "IdInterno",'DATA_NO_EXISTE' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" FROM DUMMY;	
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_hash,:v_erroroutmsg);		
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
	p_out_AdvertenciasDetalle		= SELECT '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" FROM dummy where 1=2;
	p_out_ErroresDetalle			= SELECT '' as "Codigo",'' as "Descripcion" FROM dummy where 1=2;  
	v_CargaMasivaValidar            = SELECT 0 as "IdCargaMasiva",'' as "CodigoTipoCargaMasiva" FROM dummy DUMMY;

	--------------------
	-- VALIDACIONES
	--------------------	   
	IF p_UsaReglasNegocio = 'S' THEN
	BEGIN		
		-- Validaciones datos de entrada			
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,v_Errores,:v_resultadoRegla);
		IF :v_resultadoRegla = 'RECH' THEN
			 SIGNAL SALIDA_CONTROLADA; 	
		END IF;		
	END; 
	END IF;



	--------------------
	--LOGICA
	--------------------
	BEGIN
		
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;
		

		----------------------------------------------------------------------------
		-- PROCESA DATOS PARA DETALLE CARGA MASIVA
		----------------------------------------------------------------------------
		v_CargaMasivaDetalle =								      
			SELECT
				-- DATOS GENERALES
				CM."IdInterno",
				CM."CodigoTipoCargaMasiva",
				CM."NombreArchivo",
				CMD."IdRegistro",
				
				-- DETALLE CARGA GENERAL
				MAX(CMD."DocumentoIdentidad") 		AS "DocumentoIdentidad",
				MAX(CMD."CodigoPortafolio") 		AS "CodigoPortafolio",
				MAX(CMD."TipoOperacion") 			AS "TipoOperacion",
				MAX(CMD."Cantidad") 				AS "Cantidad",
				MAX(CMD."Precio") 					AS "Precio",
				MAX(CMD."Nemotecnico") 				AS "Nemotecnico",
				MAX(CMD."IdCondicionDeLiquidacion") AS "IdCondicionDeLiquidacion",
				
				-- DETALLE CARGA INGRESO_ORDEN_MASIVO				
				MAX(CMD."IdMecanismoCaptacion") 	AS "IdMecanismoCaptacion",											
				MAX(CMD."IdTipoVigenciaOrden") 		AS "IdTipoVigenciaOrden",
				MAX(CMD."FechaVigencia") 			AS "FechaVigencia",
				MAX(CMD."IdFormaCustodia") 			AS "IdFormaCustodia",
				MAX(CMD."IdBolsa") 					AS "IdBolsa",
				MAX(CMD."IdBoveda") 				AS "IdBoveda",
				MAX(CMD."IdTipoPrecioOrden") 		AS "IdTipoPrecioOrden",
				
				--DETALLE CARGA INGRESO_INSTRUCCIONES_MASIVA
				MAX(CMD."TipoInstruccion") 			AS "TipoInstruccion",
				MAX(CMD."Emisor") 					AS "Emisor",
				MAX(CMD."FechaVencimiento")			AS "FechaVencimiento",
			    MAX(CMD."ValorRescate") 			AS "ValorRescate",
			    MAX(CMD."FechaTrade") 				AS "FechaTrade",
				MAX(CMD."Intermediario") 			AS "Intermediario",
				MAX(CMD."FolioFactura") 			AS "FolioFactura",
				MAX(CMD."MontoLiquidacion") 		AS "MontoLiquidacion",
				MAX(CMD."Tasa")						AS "Tasa",				
				MAX(CMD."IdTipoReajuste")			AS "IdTipoReajuste",
				MAX(CMD."TotalCobros")				AS "TotalCobros"
				
			FROM 
			(
				SELECT  -- Se usa para transponer datos
					"IdInterno" , 
					"IdRegistro",	
					
					-- DETALLE CARGA GENERAL
					CASE WHEN ("NombreCampo" = 'DocumentoIdentidad') 	 	THEN "ValorCampo" ELSE NULL END AS "DocumentoIdentidad",
					CASE WHEN ("NombreCampo" = 'CodigoPortafolio') 	 		THEN "ValorCampo" ELSE NULL END AS "CodigoPortafolio",
					CASE WHEN ("NombreCampo" = 'TipoOperacion') 	 		THEN "ValorCampo" ELSE NULL END AS "TipoOperacion",
					CASE WHEN ("NombreCampo" = 'Cantidad') 	 				THEN "ValorCampo" ELSE NULL END AS "Cantidad",
					CASE WHEN ("NombreCampo" = 'Precio') 	 				THEN "ValorCampo" ELSE NULL END AS "Precio",
					CASE WHEN ("NombreCampo" = 'Nemotecnico') 	 			THEN "ValorCampo" ELSE NULL END AS "Nemotecnico",
					CASE WHEN ("NombreCampo" = 'IdCondicionDeLiquidacion') 	THEN "ValorCampo" ELSE NULL END AS "IdCondicionDeLiquidacion",
					
					-- DETALLE CARGA INGRESO_ORDEN_MASIVO	
					CASE WHEN ("NombreCampo" = 'IdMecanismoCaptacion') 		THEN "ValorCampo" ELSE NULL END AS "IdMecanismoCaptacion",														
					CASE WHEN ("NombreCampo" = 'IdTipoVigenciaOrden') 	 	THEN "ValorCampo" ELSE NULL END AS "IdTipoVigenciaOrden",
					CASE WHEN ("NombreCampo" = 'FechaVigencia') 	 		THEN "ValorCampo" ELSE NULL END AS "FechaVigencia",
					CASE WHEN ("NombreCampo" = 'IdFormaCustodia') 	 		THEN "ValorCampo" ELSE NULL END AS "IdFormaCustodia",
					CASE WHEN ("NombreCampo" = 'IdBolsa') 	 				THEN "ValorCampo" ELSE NULL END AS "IdBolsa",
					CASE WHEN ("NombreCampo" = 'IdBoveda') 	 				THEN "ValorCampo" ELSE NULL END AS "IdBoveda",					
					CASE WHEN ("NombreCampo" = 'IdTipoPrecioOrden') 	 	THEN "ValorCampo" ELSE NULL END AS "IdTipoPrecioOrden",					
					
					--DETALLE CARGA INGRESO_INSTRUCCIONES_MASIVA
					CASE WHEN ("NombreCampo" = 'TipoInstruccion') 	 		THEN "ValorCampo" ELSE NULL END AS "TipoInstruccion", 
					CASE WHEN ("NombreCampo" = 'Emisor') 	 				THEN "ValorCampo" ELSE NULL END AS "Emisor",
					CASE WHEN ("NombreCampo" = 'FechaVencimiento') 	 		THEN "ValorCampo" ELSE NULL END AS "FechaVencimiento",
				    CASE WHEN ("NombreCampo" = 'ValorRescate') 	 			THEN "ValorCampo" ELSE NULL END AS "ValorRescate",
				    CASE WHEN ("NombreCampo" = 'FechaTrade') 	 			THEN "ValorCampo" ELSE NULL END AS "FechaTrade",
				    CASE WHEN ("NombreCampo" = 'Intermediario') 	 		THEN "ValorCampo" ELSE NULL END AS "Intermediario",
					CASE WHEN ("NombreCampo" = 'FolioFactura') 	 			THEN "ValorCampo" ELSE NULL END AS "FolioFactura",
					CASE WHEN ("NombreCampo" = 'MontoLiquidacion') 	 		THEN "ValorCampo" ELSE NULL END AS "MontoLiquidacion",
					CASE WHEN ("NombreCampo" = 'Tasa') 	 					THEN "ValorCampo" ELSE NULL END AS "Tasa",
					CASE WHEN ("NombreCampo" = 'IdTipoReajuste') 	 		THEN "ValorCampo" ELSE NULL END AS "IdTipoReajuste",
					CASE WHEN ("NombreCampo" = 'TotalCobros') 	 			THEN "ValorCampo" ELSE NULL END AS "TotalCobros"
					
					
				FROM :p_CargaMasivaDetalle
			) AS CMD
			INNER JOIN :p_CargaMasiva AS CM ON (CM."IdInterno" = CMD."IdInterno")
			GROUP BY CM."IdInterno",CM."CodigoTipoCargaMasiva",CM."NombreArchivo",CMD."IdRegistro";
		--SELECT 'v_CargaMasivaDetalle', * FROM :v_CargaMasivaDetalle; -- v_CargaMasivaDetalle
			
		
		-- Obtiene el protafolio al cual asignar la orden
		v_ClientePortafolio = 
			WITH PORTAFOLIOS_CLIENTE AS
			( 
				SELECT
					IDE."DocumentoIdentidad",
					PER."IdPersona",
					POR."IdPortafolio",
					POR."CodigoPortafolio",	
					RANK() OVER (PARTITION BY IDE."DocumentoIdentidad", POR."CodigoPortafolio" ORDER BY PAGR."NombreProductoAgrupador" ASC) AS "Ranking"
				
				FROM :v_CargaMasivaDetalle AS CMD 
				LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDE ON (IDE."DocumentoIdentidad" = CMD."DocumentoIdentidad") AND (IDE."Predeterminado" = 'S')
				LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (IDE."IdPersona" = PER."IdPersona")
				LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (PER."IdPersona" = CLI."IdPersona")
				LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON (CLI."IdCliente" = POR."IdCliente" AND POR."CodigoPortafolio" = CMD."CodigoPortafolio")
				LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MAT ON (MAT."IdMatrizAgrupadorAPV" = POR."IdMatrizAgrupadorAPV")
				LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS PAGR ON (PAGR."IdProductoAgrupador" = MAT."IdProductoAgrupador")
				GROUP BY IDE."DocumentoIdentidad", POR."CodigoPortafolio",PER."IdPersona", POR."IdPortafolio", PAGR."NombreProductoAgrupador"
			)
			SELECT "DocumentoIdentidad", "IdPersona", "CodigoPortafolio", "IdPortafolio"		
			FROM PORTAFOLIOS_CLIENTE
			WHERE "Ranking" = 1;

		
		
		-----------------------------------------------------------------
		-- PROCESA DATOS PARA ENCABEZADO CARGA MASIVA
		------------------------------------------------------------------	
			
		v_CargaMasiva = 
			SELECT
				"BSCL.Orden.BD.Secuencias::CAR.CargaMasiva".NEXTVAL as "IdCargaMasiva", -- Nuevas secuencias				
				"IdInterno",
				"CodigoTipoCargaMasiva",
				"NombreArchivo",
				"CantidadRegistros"
			FROM
			(
				SELECT 
					"IdInterno", 
					"CodigoTipoCargaMasiva", 
					"NombreArchivo",				
					COUNT("IdInterno") AS "CantidadRegistros"
				FROM :v_CargaMasivaDetalle
				GROUP BY "IdInterno", "CodigoTipoCargaMasiva" ,"NombreArchivo"
			) AS CMD;	
		--SELECT 'v_CargaMasiva', * FROM :v_CargaMasiva; -- REVISAR
	

		-- Obtiene Homologacion de datos  	
		v_HomologacionCargaMasiva = 
			SELECT HOM."Campo", HOM."CodigoTipoCargaMasiva", HOM."ValorExterno", HOM."ValorOptimus"
			FROM :v_CargaMasiva AS CM
			INNER JOIN "BSCL.Orden.BD::CAR.Homologacion" AS HOM  ON (HOM."CodigoTipoCargaMasiva" = CM."CodigoTipoCargaMasiva")
			WHERE HOM."NombreEntidad" = 'ORD.CargaMasiva'
			AND   HOM."IdEmpresa" 	  = :p_IdEmpresa			
			AND   HOM."IdEstado" 	  = 'ACT';
		--SELECT 'HomologacionCargaMasiva', * FROM :v_HomologacionCargaMasiva; -- REVISAR
		
		--------------------------------------------   
		-- INSERTAR ENCABEZADO CARGA MASIVA
		--------------------------------------------
		INSERT INTO "BSCL.Orden.BD::CAR.CargaMasiva"
		(
			"IdCargaMasiva",
			"CodigoTipoCargaMasiva",
			"IdEmpresa",
			"IdUsuario",
			"CantidadRegistros",
			"NombreArchivo",
		 	"FechaCarga",			
		 	"FechaProceso",
		  	"FechaIngresoRegistro",
			"IdEstado"
		)
		SELECT 
			CM."IdCargaMasiva",
			CM."CodigoTipoCargaMasiva",
			:p_IdEmpresa,
			:p_IdUsuario,
			CM."CantidadRegistros",
			CM."NombreArchivo",
			:p_FechaCarga,
			'0001-01-01',  			-- FechaProceso null ya que aun no se procesa
			CURRENT_UTCTIMESTAMP, 	-- FechaIngresoRegistro
			'ING'  				-- Estado inicial ingresado
		FROM :v_CargaMasiva AS CM;


		------------------------------------------------------------------
		-- INSERTAR DETALLE CARGA MASIVA
		------------------------------------------------------------------
		INSERT INTO "BSCL.Orden.BD::CAR.CargaMasivaDetalle"
		(
			"IdCargaMasivaDetalle",	
			"IdCargaMasiva", 	
			"ObservacionValidacion",
			"IdEstado",
			"DocumentoIdentidad",
			"CodigoPortafolio",
			"IdPersona",
			"IdPortafolio",
			"TipoOperacion",
			"Cantidad",
			"Precio",
			"Nemotecnico",
			"IdCondicionDeLiquidacion",
			
			"IdMecanismoCaptacion",							
			"IdTipoVigenciaOrden",
			"FechaVigencia",
			"IdFormaCustodia",
			"IdBolsa",
			"IdBoveda",
			"IdTipoPrecioOrden",						
			
			"TipoInstruccion",
			"Emisor",
			"FechaVencimiento",
			"ValorRescate",
			"FechaTrade",
			"Intermediario",
			"FolioFactura",
			"MontoLiquidacion",
			"Tasa",
			"IdTipoReajuste",
			"TotalCobros"
		)
		SELECT 
			 "BSCL.Orden.BD.Secuencias::CAR.CargaMasivaDetalle".NEXTVAL AS "IdCargaMasivaDetalle",
			 CM."IdCargaMasiva",
			 '',    	-- ObservacionValidacion
			 'ING',   	-- Estado inicial Ingresado
			 
			 CMD."DocumentoIdentidad",
			 CMD."CodigoPortafolio",
			 COALESCE(POR."IdPersona",0) AS "IdPersona",
			 COALESCE(POR."IdPortafolio",0) AS "IdPortafolio",
			 CMD."TipoOperacion",
			 CMD."Cantidad",
			 CMD."Precio",
			 CMD."Nemotecnico",
			 COALESCE(HCONLIQ."ValorOptimus", CMD."IdCondicionDeLiquidacion") AS "IdCondicionDeLiquidacion",
			 
			 COALESCE(CMD."IdMecanismoCaptacion",'-') AS "IdMecanismoCaptacion",			 
			 COALESCE(HTIPVIG."ValorOptimus", CMD."IdTipoVigenciaOrden",'-') AS "IdTipoVigenciaOrden",
			 COALESCE(CMD."FechaVigencia",'0001-01-01') AS "FechaVigencia",
			 COALESCE(HFORCUS."ValorOptimus", CMD."IdFormaCustodia",'-') AS "IdFormaCustodia",
			 COALESCE(CMD."IdBolsa",'-') AS "IdBolsa",
			 COALESCE(CMD."IdBoveda",'-') AS "IdBoveda",
			 COALESCE(CMD."IdTipoPrecioOrden",'-') AS "IdTipoPrecioOrden",			 
			 			 
			 CMD."TipoInstruccion",
			 CMD."Emisor",
			 CMD."FechaVencimiento",
			 CMD."ValorRescate",
			 CMD."FechaTrade",
			 CMD."Intermediario",
			 CMD."FolioFactura",
			 CMD."MontoLiquidacion",
			 COALESCE ( CMD."Tasa",0) AS "Tasa" ,
			 CMD."IdTipoReajuste",
			 CMD."TotalCobros"

		FROM :v_CargaMasivaDetalle AS CMD
		INNER JOIN :v_CargaMasiva AS CM ON (CM."IdInterno" = CMD."IdInterno")
		LEFT JOIN :v_ClientePortafolio AS POR ON (POR."DocumentoIdentidad"= CMD."DocumentoIdentidad" AND POR."CodigoPortafolio" = CMD."CodigoPortafolio")
						
		LEFT JOIN :v_HomologacionCargaMasiva AS HCONLIQ ON (HCONLIQ."CodigoTipoCargaMasiva" = CM."CodigoTipoCargaMasiva" 
														AND HCONLIQ."Campo" = 'IdCondicionDeLiquidacion' 
														AND HCONLIQ."ValorExterno" = CMD."IdCondicionDeLiquidacion")	
		
		LEFT JOIN :v_HomologacionCargaMasiva AS HTIPVIG ON (HTIPVIG."CodigoTipoCargaMasiva" = CM."CodigoTipoCargaMasiva" 
														AND HTIPVIG."Campo" = 'IdTipoVigenciaOrden' 
														AND HTIPVIG."ValorExterno" = CMD."IdTipoVigenciaOrden")
		
		LEFT JOIN :v_HomologacionCargaMasiva AS HFORCUS ON (HFORCUS."CodigoTipoCargaMasiva" = CM."CodigoTipoCargaMasiva" 
														AND HFORCUS."Campo" = 'IdFormaCustodia' 
														AND HFORCUS."ValorExterno" = CMD."IdFormaCustodia");
		

		--------------------------------------------------   
		-- VALIDA LA CM Y ACTUALIZA ESTADO DE INVALIDOS
		--------------------------------------------------

		-- Obtiene Id de las CM a validar
		v_CargaMasivaValidar = SELECT "IdCargaMasiva", "CodigoTipoCargaMasiva" FROM :v_CargaMasiva;
		
		CALL "BSCL.Orden.BD.Servicios.CAR::ValidarCargaMasiva"
		(	 
			 p_CargaMasivaValidar  		=> :v_CargaMasivaValidar
			,p_out_CargaMasivaInvalida  => :v_CargaMasivaInvalida
			,p_out_ErroresValidacion    => :v_ErroresValidacion
		);
		
		-- Actualiza estado de CM que tienen registros invalidos
		UPDATE CM
		SET CM."IdEstado" = 'INV'
		FROM "BSCL.Orden.BD::CAR.CargaMasiva" AS CM
		INNER JOIN :v_CargaMasivaInvalida AS CMI ON (CMI."IdCargaMasiva" = CM."IdCargaMasiva");
		
		-- Actualiza estado de los registros de detalle invalidos
		UPDATE CMD
		SET  CMD."IdEstado"              = 'INV',
			 CMD."ObservacionValidacion" =  EV."ObservacionValidacion"		    
		FROM "BSCL.Orden.BD::CAR.CargaMasivaDetalle" AS CMD
		INNER JOIN :v_ErroresValidacion AS EV ON (EV."IdCargaMasivaDetalle" = CMD."IdCargaMasivaDetalle");
  	END;    
END;