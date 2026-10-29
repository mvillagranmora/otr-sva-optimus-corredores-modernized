SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.CerrarEventoCapitalDV";
GO
CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.CerrarEventoCapitalDV" (

--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          				BIGINT 			default 0,
    IN p_IdUsuario          				BIGINT 			default 0,
    IN p_IdModulo           				NVARCHAR(3) 	default '',  
	
	IN p_IdEventoCapital					BIGINT     		default 0,
	IN p_IdTipoEventoCapital				NVARCHAR(2) 	default '',
	
-- PARAMETROS DE SALIDA
	OUT p_out_TicketAtencion 				BIGINT,
	OUT p_out_AdvertenciasContador 			INT,
	OUT p_out_ErroresContador 				INT
	 )   
	 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	AS
	
BEGIN 
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE abort int default 0;	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorcuenta INT DEFAULT 0;  
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		select count(1) into p_out_ErroresContador 
		from "BSCL.Custodia.BD::GBL.CerrarEventoCapitalDV_Salida_ErroresDetalle";
		select count(1) into p_out_AdvertenciasContador 
		from "BSCL.Custodia.BD::GBL.CerrarEventoCapitalDV_Salida_AdvertenciasDetalle";
	END;

	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	
	BEGIN
		declare v_errorExt nvarchar(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP

			CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			INSERT INTO "BSCL.Custodia.BD::GBL.CerrarEventoCapitalDV_Salida_ErroresDetalle"
				SELECT v_errorExt,::SQL_ERROR_MESSAGE FROM DUMMY;				
				SELECT COUNT(1) INTO p_out_ErroresContador 
				FROM "BSCL.Custodia.BD::GBL.CerrarEventoCapitalDV_Salida_ErroresDetalle";
		ELSE
			--Error interno no manejado  (El log sera captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;

	p_out_AdvertenciasContador			:= 0;
	p_out_ErroresContador				:= 0;
	p_out_AdvertenciasDetalle			= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle				= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	BEGIN
	
		-----------------------------------------------------
		--Declaraciones Variables
		-----------------------------------------------------
		DECLARE v_out_TicketAtencion 				BIGINT;
		
		DECLARE v_out_AdvertenciasContadorIngreso  		INT;
		DECLARE v_out_ErroresContadorIngreso       		INT;
		DECLARE v_out_AdvertenciasContadorGtia  		INT;
		DECLARE v_out_ErroresContadorGtia       		INT;
		DECLARE v_out_InformacionContador				INT;
		DECLARE v_InformacionContador		 			INT;
		DECLARE v_AdvertenciasContador		 			INT;
		DECLARE v_FechaSistema 							NVARCHAR(10);
		DECLARE v_Secuencia								BIGINT := 0;

		
		p_out_TblId = SELECT 0 as "Ids" from dummy where 1=2;

		-- Campos segun linea de Negocio (no se obtienen desde la tabla movimiento en transito)
		p_TblErrores = SELECT '' as "Codigo", '' as "Descripcion" FROM DUMMY WHERE 1=2;
		v_TablaErrores = SELECT '' as "Codigo", '' as "Descripcion" FROM DUMMY WHERE 1=2;

		v_TabCmbteTesIng = 	SELECT 	 0 	as "IdComprobante"
									,0 	as "NumeroReferencia" 
									,'' as "IdIngresoEgreso"
									,'' as "IdTipoMovimiento"
							FROM DUMMY WHERE 1=2;
									
	    CALL  "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (p_IdEmpresa,v_FechaSistema);
	             
		-----------------------------------------------------
		--Validaciones basicas (Codigos,Existencias,etc)
		-----------------------------------------------------
		BEGIN
			
			-- Se valida que el evento capital debe encontrarse "ACT" para poder realizar el cierre.
			SELECT count(1) INTO p_out_ErroresContador
		    FROM "BSCL.Custodia.BD::CUS.EventoCapital"
			WHERE "IdEventoCapital" = p_IdEventoCapital
			  and "IdEstado" != 'PRO';

			IF (p_out_ErroresContador > 0) THEN
	           	v_TablaCodigosMensajes =	Select 	p_IdEventoCapital 		As "IdInterno", 
												'UI_EVENTO_VIGENTE_CERRAR'	As "Codigo",
												'' 							As "CodigoDato1", 
												'' 							as "ValorDato1",
												''			 				As "CodigoDato2", 
												''							As "ValorDato2" 
										From dummy;
			                        
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,:v_out_hash);
				
				p_out_ErroresDetalle = 	Select "Codigo", "Descripcion" 
										From :v_out_hash;
		 	END IF;
			--Si existen errores en las validaciones debe salir
			select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle;
			
			IF :p_out_ErroresContador > 0 THEN
				SIGNAL SALIDA_CONTROLADA;
			END IF;				
		END;
		-----------------------------------------------------
		--fin Validaciones basicas (Codigos,Existencias,etc)
		-----------------------------------------------------

		-----------------------------------------------------
		--proceso de Negocio
		---------------------------------------------------
		BEGIN
				--*********************************************
				--* PREPARAR DATA PARA EL ENVIO A TESORERIA   *
				--* envío de ingreso a tesorería por el total *
				--*********************************************	

				INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Entrada_IngresoEgresoGenerar"
				SELECT 	  row_number() OVER (order by FT."IdBoveda",	BO."IdPersona", FT."IdMoneda", FT."FechaCierre", Emisor."IdPersona") AS "NumeroReferencia"
						,'MOVING' 							AS "IdTipoMovimiento"
						,'CNT' 								AS "IdTipoPersona"
						,CASE IFNULL(BO."IdPersona",0)
							WHEN 0 THEN Emisor."IdPersona"
							ELSE BO."IdPersona"
						 END 								AS "IdPersona"
						 ,0									AS "IdPortafolio" -- CGO 2017
						,FT."IdMoneda" 						AS "IdMoneda"
						,Sum(FT."Total")					AS "Monto"	
						,FT."FechaCierre"					AS "FechaLiquidacion"
						,'CIECDV'							AS "IdConcepto"
						,'CNT' 								AS "IdTipoPersonaTitularPago"
						,CASE IFNULL(BO."IdPersona",0)
							WHEN 0 THEN Emisor."IdPersona"
							ELSE BO."IdPersona"
						 END								AS "IdPersonaTitularPago"				
						,0									AS "IdPortafolioTitularPago"			
						,0 									AS "IdCaja"
						,'' 								AS "SecuenciaAgrupacion"
						,0									AS "SecuenciaRelacionada"
						,'' 								AS "Glosa"
						,0									AS "IdEmpleadoDigitador"							
						,0									AS "IdSucursalEmpleadoDigitador"							
						,''									AS "IdTipoProducto"						
						,'DIVDIN'							AS "IdTipoOperacion"					
						,''									AS "IdTipoComprobante"		
						,0									AS "IdComprobante"			
						,FT."FechaCierre"					AS "FechaComprobante"
						,FT."FechaCierre"					AS "FechaLiquidacionComprobante"	
						,0 									AS "IdOperacion" 
						,'0001-01-01' 						AS "FechaOperacion"
						,'' 								AS "IdCondicionLiquidacion"
						,'' 								AS "IdBolsa"
						,'' 								AS "IdMonedaOperacion"
						,0 									AS "MontoOperacion"
						,'' 								AS "IdTipoReajuste"
						,'0001-01-01' 						AS "FechaParidad"
						,0 									AS "ValorParidad"
						,0 									AS "FolioCartera"
						,0 									AS "IdInstrumento"
						,'' 								AS "IdTipoInstrumento"
						,'' 								AS "IdClasificacionTipoInstrumento"				
						,'N' 								AS "Bilateral"
						,'N' 								AS "GeneraComprobante"
						,'N' 								AS "AgruparEnComprobante"
						,'N' 								AS "MueveCuentaInversion"
						,'S' 								AS "TitularPago"
					FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente"	 					   AS FT												
						LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarBoveda"             AS BO
							ON (FT."IdBoveda" = BO."IdBoveda")
						LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona"							AS PE
							ON (BO."IdPersona" = PE."IdPersona")
						LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio"   AS SN
							ON (PE."IdPersona" = SN."IdPersona")
						LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" 		as Instru
							on FT."IdInstrumento" = Instru."IdInstrumento"
						LEFT JOIN "BSCL.Mercado.BD.Vistas.UI::MER.ConsultarEmisorInstrumento" 	as Emisor
							on  Instru."IdEmisor" = Emisor."IdEmisor"
							and FT."IdTipoInstrumento" = Emisor."IdTipoInstrumento"
				 	WHERE  	FT."IdEventoCapital" = p_IdEventoCapital
						and FT."Total" > 0
					group by 	FT."IdBoveda",
								BO."IdPersona",
								FT."IdMoneda",
								FT."FechaCierre",
								Emisor."IdPersona";

				-- PARA ESTE CASO NO SE REQUIERE FORMAS DE PAGO.
--				v_FormasDePago = select * from "BSCL.Tesoreria.BD.Vistas::IngresoEgresoFormaDePago" where 1=2;


				--************************************************
				--* PREPARAR DATA PARA EL ENVIO A TESORERIA      *
				--* envío de egreso a tesorería por cada cliente *
				--* que NO opere con cuenta de inversión         *
				--************************************************
		    	 
				INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Entrada_IngresoEgresoGenerar"
					SELECT 	FT."IdEventoCapitalCliente"							AS "NumeroReferencia"
							,'MOVEGR' 											AS "IdTipoMovimiento"
							,'CLI' 												AS "IdTipoPersona"
							,SN."IdPersona"
							,FT."IdPortafolio"									AS  "IdPortafolio" -- CGO 2017
							,FT."IdMoneda" 										AS "IdMoneda"
							,Sum(FT."Total")									AS "Monto"	
							,FT."FechaCierre"									AS "FechaLiquidacion"
							,'CIECDV'											AS "IdConcepto"
							,'CLI' 												AS "IdTipoPersonaTitularPago"
							,SN."IdPersona"										AS "IdPersonaTitularPago"				
							,FT."IdPortafolio"									AS "IdPortafolioTitularPago"			
							,0 													AS "IdCaja"
							,'' 												AS "SecuenciaAgrupacion"
							,0													AS "SecuenciaRelacionada"
							,'' 												AS "Glosa"
							,0													AS "IdEmpleadoDigitador"							
							,0													AS "IdSucursalEmpleadoDigitador"							
							,''													AS "IdTipoProducto"						
							,'DIVDIN'											AS "IdTipoOperacion"					
							,''													AS "IdTipoComprobante"		
							,0													AS "IdComprobante"			
							,FT."FechaCierre"									AS "FechaComprobante"
							,FT."FechaCierre"									AS "FechaLiquidacionComprobante"	
							,0 													AS "IdOperacion" 
							,'0001-01-01' 										AS "FechaOperacion"
							,'' 												AS "IdCondicionLiquidacion"
							,'' 												AS "IdBolsa"
							,'' 												AS "IdMonedaOperacion"
							,0 													AS "MontoOperacion"
							,'' 												AS "IdTipoReajuste"
							,'0001-01-01' 										AS "FechaParidad"
							,0 													AS "ValorParidad"
							,0 													AS "FolioCartera"
							,0 													AS "IdInstrumento"
							,'' 												AS "IdTipoInstrumento"
							,'' 												AS "IdClasificacionTipoInstrumento"				
							,'N' 												AS "Bilateral"
							,'N' 												AS "GeneraComprobante"
							,'N' 												AS "AgruparEnComprobante"
							,'N' 												AS "MueveCuentaInversion"
							,'S' 												AS "TitularPago"
					FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente"							AS FT												
						INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio"	AS SN
							ON (FT."IdSocioNegocio" = SN."IdSocioDeNegocio")
						LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapitalClienteFormaDePago"			AS ECFP
							ON FT."IdEventoCapitalCliente" = ECFP."IdEventoCapitalCliente"
				 	WHERE  	FT."IdEventoCapital" = p_IdEventoCapital
						and FT."Total" > 0
						and ECFP."IdMedioDePago" <> 'CIN'
					group by  FT."IdEventoCapitalCliente", FT."IdPortafolio", SN."IdPersona", FT."IdMoneda", FT."FechaCierre";

				-- FORMAS DE PAGO.
				INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Entrada_FormaDePago"
					SELECT	ECC."IdEventoCapitalCliente"					AS "NumeroReferencia"
							,ECFP."IdMedioDePago"							as "IdMedioDePago"
							,ECFP."IdModalidadPago"							As "IdModalidadDePago"
							,ECC."IdMoneda"									as "IdMoneda"
					        ,sum(ECC."Total")								AS "Monto"
					        ,0 												as "IdCuentaBancariaCorredora"
							,0 												as "IdTarjetaBancariaCorredora"
							,0 												as "IdChequera"
							,0 												as "NumeroDocumentoCorredora"
							,'0001-01-01' 									as "FechaDocumentoPagoCorredora"
							,ECFP."IdCuentaBancaria" 						as "IdCuentaBancariaSocio"
							,ECFP."IdBanco" 								as "IdBancoSocio"
							,ECFP."NombreCuentaBancaria" 					as "NombreCuentaBancariaSocio"
							,ECFP."NumeroCuentaBancaria" 					as "NumeroCuentaBancariaSocio"
							,ECFP."IdTipoCuentaBancaria" 					as "IdTipoCuentaBancariaSocio"
							,ECFP."NombreTipoCuentaBancaria" 				as "NombreTipoCuentaBancariaSocio"
							,0 												as "IdTarjetaBancariaSocio"
							,ECFP."IdTipoTarjetaBancaria" 					as "IdTipoTarjetaBancariaSocio"
							,'' 											as "NombreTipoTarjetaBancariaSocio"
							,ECFP."NumeroTarjetaBancaria" 					as "NumeroTarjetaBancariaSocio"
							,''												as "NumeroSerieSocio"
							,0												as "NumeroDocumentoSocio"
							,'0001-01-01'									as "FechaDocumentoPagoSocio"
					FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente"								AS ECC
						LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapitalClienteFormaDePago"			AS ECFP
							ON ECC."IdEventoCapitalCliente" = ECFP."IdEventoCapitalCliente"
					WHERE  	ECC."IdEventoCapital" = p_IdEventoCapital
						and ECC."Total" > 0
						and ECFP."IdMedioDePago" <> 'CIN'
					group by 	ECC."IdEventoCapitalCliente",
								ECC."IdPortafolio", 
								ECFP."IdMedioDePago", 
								ECFP."IdModalidadPago",
								ECC."IdMoneda",
								ECFP."IdCuentaBancaria",
								ECFP."IdBanco",
								ECFP."NombreCuentaBancaria", 
								ECFP."NumeroCuentaBancaria",
								ECFP."IdTipoCuentaBancaria",
								ECFP."NombreTipoCuentaBancaria",
								ECFP."IdTipoTarjetaBancaria",
								ECFP."NumeroTarjetaBancaria";
											
				--**********************
				--*  ENVIO A TESORERIA *
				--********************** 		
				CALL "BSCL.Tesoreria.BD.Servicios::CrearIngresoEgreso"(
					p_IdEmpresa 				=> 	p_IdEmpresa,
					p_IdUsuario					=> 	p_IdUsuario,
					p_IdModulo					=>	'CUS',
					p_simularOperacion			=> 	'N',
					p_UsaReglasNegocio			=>	'S',
--					p_IngresoEgresoGenerar		=>	:v_Movimientos,
--					p_TableFormaPago			=>	:v_FormasDePago,
					p_EsIngresoAutomatico		=>	'S',
--					p_out_IngresoEgresoGenerado	=>	p_out_IngEgrGeneradoIngreso,
--					p_out_ResultadoProceso		=>	v_out_ResultadoProcesoModuloIngreso,
			 		p_out_InformacionContador   => :v_InformacionContador,
--				   	p_out_InformacionDetalle	=> :v_InformacionDetalle,
					p_out_AdvertenciasContador	=> 	v_out_AdvertenciasContadorIngreso,
--					p_out_AdvertenciasDetalle	=>	v_out_AdvertenciasDetalleIngreso,
					p_out_ErroresContador		=>	v_out_ErroresContadorIngreso
--					p_out_ErroresDetalle		=>	v_out_ErroresDetalleIngreso
				);

			   	p_out_ErroresContador := p_out_ErroresContador + v_out_ErroresContadorIngreso;
			   	p_out_AdvertenciasContador := p_out_AdvertenciasContador + v_out_AdvertenciasContadorIngreso;
			   
				INSERT INTO "BSCL.Custodia.BD::GBL.CerrarEventoCapitalDV_Salida_ErroresDetalle"("Codigo","Descripcion")
				SELECT "Codigo", "Descripcion" 
				FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_ErroresDetalle";

				INSERT INTO "BSCL.Custodia.BD::GBL.CerrarEventoCapitalDV_Salida_AdvertenciasDetalle"("Codigo","Descripcion")
				SELECT "Codigo", "Descripcion" 
				FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_AdvertenciasDetalle";
	
		       --Comprobantes ingresados en tesoreria y almacenados para enviar el idingresoegreso a cuentainversion                
		       v_TabCmbteTesIng = Select 	MovtosTeso."NumeroReferencia"	As "IdComprobante", 
		       								IngEgrGen."NumeroReferencia", 
		                                 	IngEgrGen."IdIngresoEgreso", 
		                                 	IngEgrGen."IdTipoMovimiento"
		                          From "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Entrada_IngresoEgresoGenerar" As MovtosTeso --CmbteTes
		                            Inner Join "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_IngresoEgresoGenerado" As IngEgrGen
		                               On IngEgrGen."NumeroReferencia" = MovtosTeso."NumeroReferencia"
		                            Inner Join "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_ResultadoProceso" As ResProcTes
		                               On ResProcTes."Id" = IngEgrGen."IdIngresoEgreso"
		                          Where ResProcTes."Resultado" = 'ING';	 
--				END IF;

				--*****************************************************
				--* PREPARAR DATA PARA EL ENVIO A CUENTA DE INVERSIÓN *
				--* Todos los clientes mueven cuenta de inversión     *
				--*****************************************************

					INSERT INTO "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_IN_CargoAbonoAGenerar" (
						"NumeroCargoAbono","IdPortafolio","IdMoneda","IdTipoMovimiento","IdConcepto","Descripcion",
						"IdCondicionDeLiquidacion","FechaCondicionDeLiquidacion","Monto","IdEjecutivo","IdSucursal",
						"IdTipoProducto","IdTipoOperacion","IdOperacion","FechaOperacion","FechaLiquidacionOperacion",
						"FechaVencimientoOperacion","IdMonedaOperacion","MontoOperacion","TipoCambioOperacion","IdContraparte",
						"IdTipoComprobante","IdComprobante","FolioComprobante","IdComprobanteDetalle","IdComprobanteFormaDePago",
						"IdTipoMovimientoAsociado","IdMovimientoAsociado","FechaComprobante","IdPersonaPagoCobro","IdInstrumento",
						"IdTipoInstrumento","Nemo","EsIndicadorBilateral","EsCargoAbonoPorTraspaso","IdPortafolioTraspaso",
						"GeneroContraparteEnTesoreria","CertificadoAutomatico")
					Select 	EvCapCli."IdEventoCapitalCliente"											AS "NumeroCargoAbono",		
							EvCapCli."IdPortafolio"														AS "IdPortafolio",		
							EvCapCli."IdMoneda"															AS "IdMoneda",			
							'MOVABN'																	AS "IdTipoMovimiento",
							'CIECDV'																	AS "IdConcepto",
							Concat('Abono por Dividendo en Dinero, Instrumento : ' , EvCapCli."Nemo")	AS "Descripcion",
							'T0'																		AS "IdCondicionDeLiquidacion",	
							EvCapCli."FechaPago"														AS "FechaCondicionDeLiquidacion",
							EvCapCli."Total"															AS "Monto",						
										
							0																			AS "IdEjecutivo",
							0																			AS "IdSucursal",
							''																			AS "IdTipoProducto",			
							''																			AS "IdTipoOperacion",			
							0																			AS "IdOperacion",				
							'0001-01-01'																AS "FechaOperacion",			
							'0001-01-01'																AS "FechaLiquidacionOperacion",	
							'0001-01-01'																AS "FechaVencimientoOperacion",	
							''																			AS "IdMonedaOperacion",			
							0																			AS "MontoOperacion",			
							0																			AS "TipoCambioOperacion",		
							0																			AS "IdContraparte",				
							''																			AS "IdTipoComprobante",			
							0																			AS "IdComprobante",				
							''																			AS "FolioComprobante",
							0																			AS "IdComprobanteDetalle",		
							0																			AS "IdComprobanteFormaDePago",
							Coalesce(TabCompTeso."IdTipoMovimiento",'') 								As "IdTipoMovimientoAsociado",
							Coalesce(TabCompTeso."IdIngresoEgreso",0) 									As "IdMovimientoAsociado",
							'0001-01-01'																AS "FechaComprobante",			
							0																			AS "IdPersonaPagoCobro",		
							0																			AS "IdInstrumento",				
							''																			AS "IdTipoInstrumento",			
							''																			AS "Nemo",						
							''																			AS "EsIndicadorBilateral",		
							'N'																			AS "EsCargoAbonoPorTraspaso",	
							0																			AS "IdPortafolioTraspaso",		
							CASE ECFP."IdMedioDePago"
								WHEN '' THEN CASE Efp."IdMedioDePago"
												WHEN '' 
													THEN 'N'
												ELSE 
													'S'
											END
								ELSE CASE Efp."IdMedioDePago"
										WHEN '' 
											THEN 'N'
										ELSE 
											'S'
									 END
							END																			AS "GeneroContraparteEnTesoreria",
							'S'																			AS "CertificadoAutomatico"
					FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS EvCapCli
						INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS ConCliente
							ON EvCapCli."IdPortafolio" = ConCliente."IdPortafolio"
						LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapitalClienteFormaDePago"			AS ECFP
							ON EvCapCli."IdEventoCapitalCliente" = ECFP."IdEventoCapitalCliente"
						LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaPagoPortafolio" 	AS Efp 
							ON ((EvCapCli."IdPortafolio" 	= Efp."IdPortafolio")
							AND Efp."IdEstado" 				= 'ACT'
							AND Efp."IdTipoOperacion" 		= 'DIVDIN'
							AND Efp."PorDefecto" 			= 'S')
						LEFT JOIN :v_TabCmbteTesIng													AS TabCompTeso
							ON TabCompTeso."IdComprobante" = EvCapCli."IdEventoCapitalCliente"
					WHERE EvCapCli."IdEventoCapital" = p_IdEventoCapital
					  AND EvCapCli."IdTipoEventoCapital" = 'DV'
					  AND EvCapCli."Total" > 0;
					  
					-- CARGO A GENERAR EN EL CASO DE LOS CLIENTES "CUENTA MANDANTE" QUE TENGAN CONFIGURADO QUE EL PAGO
					-- DE LOS DIVIDENDOS SE REALIZARÁ DIRECTAMENTE AL CLIENTE Y NO MEDIANTE LA CORREDORA. JIRA "SAP-5857"
					INSERT INTO "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_IN_CargoAbonoAGenerar" (
						"NumeroCargoAbono","IdPortafolio","IdMoneda","IdTipoMovimiento","IdConcepto","Descripcion",
						"IdCondicionDeLiquidacion","FechaCondicionDeLiquidacion","Monto","IdEjecutivo","IdSucursal",
						"IdTipoProducto","IdTipoOperacion","IdOperacion","FechaOperacion","FechaLiquidacionOperacion",
						"FechaVencimientoOperacion","IdMonedaOperacion","MontoOperacion","TipoCambioOperacion","IdContraparte",
						"IdTipoComprobante","IdComprobante","FolioComprobante","IdComprobanteDetalle","IdComprobanteFormaDePago",
						"IdTipoMovimientoAsociado","IdMovimientoAsociado","FechaComprobante","IdPersonaPagoCobro","IdInstrumento",
						"IdTipoInstrumento","Nemo","EsIndicadorBilateral","EsCargoAbonoPorTraspaso","IdPortafolioTraspaso",
						"GeneroContraparteEnTesoreria","CertificadoAutomatico")
					Select 	EvCapCli."IdEventoCapitalCliente"											AS "NumeroCargoAbono",		
							EvCapCli."IdPortafolio"														AS "IdPortafolio",		
							EvCapCli."IdMoneda"															AS "IdMoneda",			
							'MOVCAR'																	AS "IdTipoMovimiento",
							'CIECDV'																	AS "IdConcepto",
							Concat('Cargo - Pago por Emisor - Dividendo en Dinero, Instrumento : ' , EvCapCli."Nemo")	AS "Descripcion",
							'T0'																		AS "IdCondicionDeLiquidacion",	
							EvCapCli."FechaPago"														AS "FechaCondicionDeLiquidacion",
							EvCapCli."Total"															AS "Monto",						
										
							0																			AS "IdEjecutivo",
							0																			AS "IdSucursal",
							''																			AS "IdTipoProducto",			
							''																			AS "IdTipoOperacion",			
							0																			AS "IdOperacion",				
							'0001-01-01'																AS "FechaOperacion",			
							'0001-01-01'																AS "FechaLiquidacionOperacion",	
							'0001-01-01'																AS "FechaVencimientoOperacion",	
							''																			AS "IdMonedaOperacion",			
							0																			AS "MontoOperacion",			
							0																			AS "TipoCambioOperacion",		
							0																			AS "IdContraparte",				
							''																			AS "IdTipoComprobante",			
							0																			AS "IdComprobante",				
							''																			AS "FolioComprobante",
							0																			AS "IdComprobanteDetalle",		
							0																			AS "IdComprobanteFormaDePago",
							Coalesce(TabCompTeso."IdTipoMovimiento",'') 								As "IdTipoMovimientoAsociado",
							Coalesce(TabCompTeso."IdIngresoEgreso",0) 									As "IdMovimientoAsociado",
							'0001-01-01'																AS "FechaComprobante",			
							0																			AS "IdPersonaPagoCobro",		
							0																			AS "IdInstrumento",				
							''																			AS "IdTipoInstrumento",			
							''																			AS "Nemo",						
							''																			AS "EsIndicadorBilateral",		
							'N'																			AS "EsCargoAbonoPorTraspaso",	
							0																			AS "IdPortafolioTraspaso",		
							CASE ECFP."IdMedioDePago"
								WHEN '' THEN CASE Efp."IdMedioDePago"
												WHEN '' 
													THEN 'N'
												ELSE 
													'S'
											END
								ELSE CASE Efp."IdMedioDePago"
										WHEN '' 
											THEN 'N'
										ELSE 
											'S'
									 END
							END																			AS "GeneroContraparteEnTesoreria",
							'S'																			AS "CertificadoAutomatico"
					FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS EvCapCli
						INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS ConCliente
							ON EvCapCli."IdPortafolio" = ConCliente."IdPortafolio"
						LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapitalClienteFormaDePago"			AS ECFP
							ON EvCapCli."IdEventoCapitalCliente" = ECFP."IdEventoCapitalCliente"
						LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaPagoPortafolio" 	AS Efp 
							ON ((EvCapCli."IdPortafolio" 	= Efp."IdPortafolio")
							AND Efp."IdEstado" 				= 'ACT'
							AND Efp."IdTipoOperacion" 		= 'DIVDIN'
							AND Efp."PorDefecto" 			= 'S')
						LEFT JOIN :v_TabCmbteTesIng													AS TabCompTeso
							ON TabCompTeso."IdComprobante" = EvCapCli."IdEventoCapitalCliente"
					WHERE EvCapCli."IdEventoCapital" = p_IdEventoCapital
					  AND EvCapCli."IdTipoEventoCapital" = 'DV'
					  AND EvCapCli."PagaClienteCorredor" = 'CL'
					  AND EvCapCli."Total" > 0;					  
					  
					CALL "BSCL.CuentaInversion.BD.Servicios::CrearCargoAbono" ( 
		                p_IdEmpresa				 				=> :p_IdEmpresa,					
					    p_IdUsuario	 							=> :p_IdUsuario,					
					    p_IdModulo	 							=> :P_IdModulo,					
					    p_simularOperacion 						=> 'N',
					    p_UsaReglasNegocio 						=> 'S',				
						p_FechaSistema 							=> :v_FechaSistema,
						p_EsIngresoManual 						=> 'N',
						p_out_InformacionContador 				=> :v_out_InformacionContador,
						p_out_AdvertenciasContador 				=> :p_out_AdvertenciasContador,
						p_out_ErroresContador 					=> :p_out_ErroresContador
					);
					
					IF :p_out_ErroresContador > 0 or :p_out_AdvertenciasContador > 0 THEN
						INSERT INTO "BSCL.Custodia.BD::GBL.CerrarEventoCapitalDV_Salida_AdvertenciasDetalle" ("Codigo","RequiereAutorizacion","Descripcion")
						SELECT "Codigo","RequiereAutorizacion","Descripcion" FROM "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_OUT_AdvertenciasDetalle";
						
						INSERT INTO "BSCL.Custodia.BD::GBL.CerrarEventoCapitalDV_Salida_ErroresDetalle" ("Codigo","Descripcion")
						SELECT "Codigo","Descripcion" FROM "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_OUT_ErroresDetalle";
						
						
						SIGNAL SALIDA_CONTROLADA;
					END IF;


	--------------------------------------------------------------------------------------------------------
				--******************************************
				--* PREPARAR DATA PARA EL ENVIO A GARANTIA *
				--******************************************

					INSERT INTO "BSCL.Custodia.BD::GBL.IngresarGarantiasUI_Entrada_GarantiaCustodia"(
						"IdMoneda","MontoMonedaInstrumento", "MontoMonedaSistema","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumentoGarantizado"
						,"IdConcepto","IdContraparteGarantia","IdTipoOperacion","IdOperacion")
					SELECT 
						EvCapCli."IdMoneda" 				as "IdMoneda",
						EvCapCli."Total"					AS "MontoMonedaInstrumento",
						EvCapCli."Total"					AS "MontoMonedaSistema",
						ConPortafolio."IdCliente" 			as "IdCliente", 
						EvCapCli."IdPortafolio" 			as "IdPortafolio",
						EvCapCli."IdTipoInstrumento" 		as "IdTipoInstrumento",
						EvCapCli."IdInstrumento"			as "IdInstrumentoGarantizado",	
--						'DIEVAC' 							as "IdConcepto",
						'CIECDV' 							as "IdConcepto",
						Gtia."IdContraparteGarantia"		as "IdContraparteGarantia",
						'INGGTA'							as "IdTipoOperacion",
						0									as "IdOperacion"
					FROM "BSCL.Custodia.BD::CUS.EventoCapital"											AS EvCap
							INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapitalCliente" 					AS EvCapCli
								ON EvCap."IdEventoCapital" = EvCapCli."IdEventoCapital" 
							INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" 				AS ConPortafolio
								ON EvCapCli."IdPortafolio" = ConPortafolio."IdPortafolio"
							left join "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantias"					as Gtia
								ON EvCapCli."IdCartera" = Gtia."IdGarantia"
							   AND EvCapCli."TipoCartera" = 'GAR'
					WHERE  	EvCap."IdEventoCapital" = p_IdEventoCapital
						and EvCapCli."Total" > 0
						and EvCapCli."TipoCartera" = 'GAR';
						


				Call "BSCL.Custodia.BD.Servicios::GTA.IngresarGarantiasUI" (
			            p_IdEmpresa 				=> :p_IdEmpresa,
			            p_IdUsuario 				=> :p_IdUsuario,
			            p_IdModulo 					=> 'CUS',
			            p_UsaReglasNegocio			=> 'S',
			            p_Certifica					=> 'S',
			            p_FechaMovimiento			=> :v_FechaSistema,

			 			p_out_InformacionContador   => :v_InformacionContador,			            -- CGO 2017
						p_out_TicketAtencion		=> v_out_TicketAtencion,
						p_out_AdvertenciasContador	=> v_out_AdvertenciasContadorGtia,
						p_out_ErroresContador		=> v_out_ErroresContadorGtia
				);

				-- AGRUPACIÓN DE MENSAJES
			   	p_out_ErroresContador := p_out_ErroresContador + v_out_ErroresContadorGtia;
			   	p_out_AdvertenciasContador := p_out_AdvertenciasContador + v_out_AdvertenciasContadorGtia;
			   
				INSERT INTO "BSCL.Custodia.BD::GBL.CerrarEventoCapitalDV_Salida_ErroresDetalle" ("Codigo","Descripcion")
				SELECT "Codigo", "Descripcion" 
				FROM "BSCL.Custodia.BD::GBL.IngresarGarantiasUI_Salida_ErroresDetalle";
	
				INSERT INTO "BSCL.Custodia.BD::GBL.CerrarEventoCapitalDV_Salida_AdvertenciasDetalle" ("Codigo","Descripcion")
				SELECT "Codigo", "Descripcion" 
				FROM "BSCL.Custodia.BD::GBL.IngresarGarantiasUI_Salida_AdvertenciasDetalle";

	--------------------------------------------------------------------------------------------------------
				UPDATE "BSCL.Custodia.BD::CUS.EventoCapital"
				SET "IdEstado" = 'CER'
				WHERE "IdEventoCapital" = p_IdEventoCapital;
							  
				UPDATE "BSCL.Custodia.BD::CUS.EventoCapitalCliente"
				SET "IdEstado" = 'CER'
				WHERE "IdEventoCapital" = p_IdEventoCapital;

	--------------------------------------------------------------------------------------------------------

			IF (p_out_ErroresContador > 0) THEN
				SIGNAL SALIDA_CONTROLADA;
			ELSE
				v_TablaCodigosMensajes =	Select 	p_IdEventoCapital				As "IdInterno", 
													'UI_OPERACION_CERRAR_EXITO'		As "Codigo",
													'' 								As "CodigoDato1", 
													'' 								as "ValorDato1",
													'' 								As "CodigoDato2", 
													'' 								As "ValorDato2" 
												From dummy;
					                        
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,:v_out_hash);
				
				p_out_InformacionDetalle = 	Select "Codigo", "Descripcion" 
											From :v_out_hash;				
			END IF;
		END;
		-----------------------------------------------------
		--fin proceso de Negocio
		---------------------------------------------------
	END;
END;
GO