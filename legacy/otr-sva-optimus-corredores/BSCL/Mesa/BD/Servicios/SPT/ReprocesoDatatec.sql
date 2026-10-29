SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Mesa.BD.Servicios::SPT.ReprocesoDatatec";
-- 
--  Proposito: 
--  Fecha Creacion: 20/09/2016
-- 
CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::SPT.ReprocesoDatatec"
    ( 
	-- Entrada
	 IN p_IdEmpresa         BIGINT,
	 IN p_IdUsuario         BIGINT,
	 IN p_IdModulo          NVARCHAR(3),
	 IN p_UsaReglasNegocio	NVARCHAR(1),   
	 IN p_Fecha				DATE,
	--Salida   	1--------------------------------------------------
   	OUT p_out_AdvertenciasContador	INT,
   	OUT p_out_AdvertenciasDetalle	TABLE ("IdInterno"	NVARCHAR(40), "IdDatatec" NVARCHAR(40), "Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)),
	OUT p_out_ErroresContador 		INT,
	OUT p_out_ErroresDetalle 		TABLE ("IdInterno"	NVARCHAR(40), "IdDatatec" NVARCHAR(40), "Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(520)),
   	OUT p_out_reprocesados			TABLE ("IdInterno" BIGINT,"LOGS" NVARCHAR(1000),"CODIGO" NVARCHAR(2))
    ) 

	LANGUAGE SQLSCRIPT 
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN		
  	DECLARE v_count_operaciones			BIGINT;
  	DECLARE v_count_errores				BIGINT;	
  	--DECLARE v_count_test				BIGINT;
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_out_AdvertenciasDetalle  	TABLE ("IdInterno" NVARCHAR(40)
												, "IdDatatec" NVARCHAR(40)
												, "Codigo" nvarchar(100)
												,"RequiereAutorizacion" NVARCHAR(1)
												,"ValorParametro" nvarchar(100)
												,"NombreParametro" NVARCHAR(40))
										DEFAULT SELECT 0  AS "IdInterno"
													, ''  AS "IdDatatec"
				                                    , ''  AS "Codigo"
				                                    , ''  AS "RequiereAutorizacion"
				                                    , ''  AS "ValorParametro"
				                                    , ''  AS "NombreParametro" 
				                               FROM dummy 
                               					WHERE 1=2;
	
	
	DECLARE v_out_ErroresDetalle  		TABLE ("IdInterno" NVARCHAR(40)
												, "IdDatatec" NVARCHAR(40)
												, "Codigo" nvarchar(100)
												,"ValorParametro" nvarchar(100)
												,"NombreParametro" NVARCHAR(40))
												 DEFAULT SELECT    0  AS "IdInterno" 
												 				, ''  AS "IdDatatec"
						                                    	, ''  AS "Codigo"
						                                    	, ''  AS "ValorParametro"
						                                    	, ''  AS "NombreParametro" 
				                               				FROM dummy 
                               									WHERE 1=2;
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
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
		
		p_out_ErroresDetalle = 	SELECT errores."IdInterno",
							 		   errores."IdDatatec",
									errores."Codigo"||' - '||"ValorParametro" as "Codigo",
									msj."Descripcion"
								FROM :v_out_ErroresDetalle errores
								INNER JOIN :v_out_ErroresDetalleMensajes msj on errores."Codigo" = msj."Codigo";
		
		p_out_AdvertenciasDetalle = SELECT adv."IdInterno",
							 		adv."IdDatatec",
									adv."Codigo"||' - '||"ValorParametro" as "Codigo",
									adv."RequiereAutorizacion",
									msj."Descripcion"
								FROM :v_out_AdvertenciasDetalle adv
								INNER JOIN :v_out_ErroresDetalleMensajes msj on adv."Codigo" = msj."Codigo";
				 						 	
	END;
	
   	v_tblOperacionSpot =
   		SELECT
   			  "IdOperacionSpot" AS "NumeroOperacion"
   			, "IdOperacionSpot"                
			, "IdEmpresa"                     
			, "IdCliente"                     
			, "IdPortafolio"                  
			, "IdInstrumento"             
			, "IdTipoOperacion"                  
			, "FechaOperacion"                
			, "FechaLiquidacion"              
			, "IdMonedaPrincipal"             
			, "IdMonedaSecundaria" 
			, "IdMonedaLocal"      --**  
			, "IdMonedaEquivalente"    --**
			, "MontoMonedaPrincipal" 
			, "MontoMonedaPrincipalEquivalente"   --**      
			, "MontoMonedaSecundaria"         
			, "TipoCambioCierre"   
			, "TipoCambioCierreForward"             
			, "ParidadCierre"
			, "ParidadCierreForward"
			, "TipoCambioCosto"               
			, "ParidadCosto"                  
			, "MontoEquivalente"              
			, "MontoRentabilidad"             
			, "MontoRentabilidadCanal"  
			, "ResultadoMonedaLocalPuntas" --**     
			, "ResultadoMonedaLocalBidAsk"    --**  
			, "ResultadoMonedaLocalDO" --**  
			, "ResultadoMonedaLocalTCSpot"   --**  
			, "ResultadoMonedaLocalTCCierreMesa"
			, "ResultadoMonedaLocalConsolidado"
			, "Margen"     
			, "MargenBidAsk"              --**          
			, "Utilidad"                      
			, "PuntaCompra"                   
			, "PuntaVenta"          
			, "PuntaBid"        --**    
			, "PuntaAsk"  --**      
			, "ParidadBid"  --**    
			, "ParidadAsk"  --** 
			, "ValorDolarObservado"--** 
			, "ValorTCSpot"   --** 
			, "SolicitudTipoCambio"           
			, "IdEvento"                      
			, "IdMesa"                     
			, "IdModulo"                    
			, "IdTipoDolar"                  
			, "TipoComprobanteFacturacion"    
			, "IdComprobanteFacturacion"      
			, "IdComprobanteTesoreriaIngreso" 
			, "IdComprobanteTesoreriaEgreso"  
			, "IdAgente"                        
			, "BenOur"                        
			, "EquivalenteConGasto"           
			, "TipoCambioCierreConGasto"      
			, "Observacion"                   
			, "CodigoComercio"                
			, "FolioPlanilla"                 
			, "FolioInterfaz"                 
			, "OperacionFicticia"             
			, "IdTipoCanalIngreso"      
			, "IdDatatec"                   
			, "Estado"                        
			, "IdUsuario"                     
			, "IdUsuarioModifica"             
			, "FechaModifica"        
			, "FechaReproceso" 
		FROM "BSCL.Mesa.BD::SPT.OperacionSpot" 
		WHERE "Estado" = 'PEN'
		  AND "FechaOperacion" <= p_Fecha
		  AND "IdEmpresa" = :p_IdEmpresa
		  AND "IdTipoCanalIngreso" = 'DAT'
		  ;
									
    
    BEGIN  
		--PARAMETROS REGLAS NEGOCIO
		DECLARE v_resultadoRegla 			NVARCHAR(40);
		--*******************************
		--*VALIDACION REGLAS BASICAS 	*
		--*******************************
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
					   
		--insert into "BSCL.Mesa.BD::SPT.TEST" ("cant","desc") values (2,:v_resultadoRegla);	
		IF :v_resultadoRegla = 'RECH' THEN
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", '' as "IdDatatec", "Codigo", '' as "ValorParametro",'' as "NombreParametro" 
								   FROM :v_out_ErroresDetalle_Basicos;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
			
		--preparar parametros para SP ServicioCumpleRequisitosCrearOperacionSpot
		v_portafoliosPrevalidacion =
			SELECT
				ope."NumeroOperacion" , 
				ope."IdPortafolio", 
				port."IdPortafolio" 		as "IdPortafolioReal", 
				port."IdEstado" 			as "IdEstadoPortafolio",
				port."IdCliente", 
				cli."IdCliente" 			as "IdClienteReal",
				cli."IdEstado" 				as "IdEstadoCliente", 
				cli."DocumentoIdentidad",
				port."IdPerfilCobros",
				cli."IdEmpleadoEjecutivo" 	as "IdEmpleadoPortafolio"
			FROM :v_tblOperacionSpot ope
			LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" port on ope."IdPortafolio" = port."IdPortafolio"
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli on port."IdCliente" = cli."IdCliente"
			LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" indCS 
				ON (indCS."IdCliente" = cli."IdCliente" and indCS."IdTipoEntidad" = 'CLIENTE' and indCS."IdUso" = 'REQ' and indCS."CodigoTipoIndicador" = 'CS');
		
		
		--*******************************
		--*REGLAS DE NEGOCIO            *
		--*******************************
	    CALL "BSCL.Mesa.BD.Reglas::ServicioCumpleRequisitosCrearOperacionSpot"(p_IdEmpresa,'S',:v_tblOperacionSpot,:v_portafoliosPrevalidacion,:v_out_SalidaDetalle,:v_out_Salida);

		v_out_AdvertenciasDetalle = SELECT "NumeroOperacion" as "IdInterno", "IdDatatec", "Codigo",'N' as "RequiereAutorizacion","ValorParametro","NombreParametro"
										FROM :v_out_SalidaDetalle
										WHERE "Resultado" = 'ADV'
										UNION ALL
										SELECT "NumeroOperacion" as "IdInterno","IdDatatec","Codigo",'S' as "RequiereAutorizacion","ValorParametro","NombreParametro"
										FROM :v_out_SalidaDetalle
										WHERE "Resultado" = 'AUT';
		v_out_ErroresDetalle 	= 	SELECT "NumeroOperacion" as "IdInterno" ,"IdDatatec", "Codigo","ValorParametro","NombreParametro"
										FROM :v_out_SalidaDetalle
										WHERE "Resultado" = 'ERR';
		v_operaciones 			= 	SELECT spot.* 
										FROM :v_tblOperacionSpot spot
										INNER JOIN :v_out_Salida SD on spot."NumeroOperacion" = SD."NumeroOperacion" 
										AND SD."Resultado" IN ('ADV','AUT','OK');
		--Cuenta las operaciones que pueden continuar con el proceso
		SELECT COUNT(1) as "count" into v_count_operaciones
		FROM :v_tblOperacionSpot spot
		INNER JOIN :v_out_Salida SD on spot."NumeroOperacion" = SD."NumeroOperacion" 
		where (SD."Resultado" IN ('ADV','AUT','OK'))
		or (spot."IdDatatec" != '0');
		
		IF (v_count_operaciones = 0) THEN
			SIGNAL SALIDA_CONTROLADA;
			--insert into "BSCL.Mesa.BD::SPT.TEST" ("cant","desc") values (3,'if v_count_operaciones es 0');	
		END IF;
			
		v_TablaCodigosMensajes =
			SELECT  "Codigo" AS "CodigoMensaje",
					"NombreParametro" as "CodigoDato",
					"ValorParametro" as "ValorDato" 
			FROM :v_out_ErroresDetalle
			UNION ALL
			SELECT 	"Codigo" AS "CodigoMensaje",
					"NombreParametro" as "CodigoDato",
					"ValorParametro" as "ValorDato" 
			FROM :v_out_AdvertenciasDetalle;
			
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes,v_out_ErroresDetalleMensajes);								
		
		SELECT COUNT(1) as "count" into v_count_operaciones FROM :v_operaciones;
		
		--Errores		
		SELECT COUNT(1) as "count" into v_count_errores FROM :v_out_ErroresDetalle;
			
		IF (v_count_operaciones != 0) THEN
			UPDATE op 
			SET  op."Estado" 			= 'VIG'
				,op."FechaReproceso" 	= :p_Fecha
			FROM "BSCL.Mesa.BD::SPT.OperacionSpot" op
			INNER JOIN :v_tblOperacionSpot v	ON v."IdOperacionSpot" = op."IdOperacionSpot"
			INNER JOIN :v_out_Salida s 			ON s."NumeroOperacion" =  v."NumeroOperacion";
			
			--agregar datos de salida para el bloter
			p_out_reprocesados = 
				select
					op."IdOperacionSpot" AS "IdInterno" 
					,CONCAT('REGISTRO REPROCESADO CORRECTAMENTE N° ID DATATEC : ',op."IdDatatec") AS "LOGS"
					,'OK' AS "CODIGO"
				FROM "BSCL.Mesa.BD::SPT.OperacionSpot" op
				INNER JOIN :v_out_Salida s 			
					ON op."IdOperacionSpot" = s."NumeroOperacion";
		END IF;
			
		--CON ERRORES
		IF (v_count_errores != 0) THEN
			--agregar datos de salida para el bloter
			p_out_error =
				SELECT
					errores."IdInterno"AS "IdInterno"
					,cast(CONCAT('REGISTRO CON ERROR: ',STRING_AGG(msj."Descripcion", ' , ')) as nvarchar(1000))  AS "LOGS"
					,'ER' AS "CODIGO"
				FROM :v_out_ErroresDetalle errores
				INNER JOIN :v_out_ErroresDetalleMensajes msj on errores."Codigo" = msj."Codigo"
				GROUP BY errores."IdInterno";
		END IF;
		
		p_out_AdvertenciasDetalle =
			SELECT
				adv."IdInterno",
				adv."IdDatatec",
				adv."Codigo"||' - '||"ValorParametro" as "Codigo",
				adv."RequiereAutorizacion",
				msj."Descripcion"
			FROM :v_out_AdvertenciasDetalle adv
			INNER JOIN :v_out_ErroresDetalleMensajes msj on adv."Codigo" = msj."Codigo";
			
	END;
		
END;