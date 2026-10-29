SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.CerrarEventoCapitalCorteCupon";
GO
CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.CerrarEventoCapitalCorteCupon" (
--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          				BIGINT 			default 0,
    IN p_IdUsuario          				BIGINT 			default 0,
    IN p_IdModulo           				NVARCHAR(3) 	default '',  
	IN p_IdEventoCapital					BIGINT     		default 0,	
	IN p_IdTipoEventoCapital				NVARCHAR(2) 	default '',
	IN p_FechaPago							NVARCHAR(10) 	default '0001-01-01',
	IN p_IdEmisor							NVARCHAR(40)	default '',     

-- PARAMETROS DE SALIDA
-- Salidas para mensajes de advertencias y errores
	OUT p_out_InformacionContador  				INT,
	OUT p_out_AdvertenciasContador  			INT,
	OUT p_out_ErroresContador       			INT
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
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador
		from "BSCL.Custodia.BD::GBL.CerrarEventoCapitalCorteCupon_Salida_ErroresDetalle";
		select count(1) into p_out_AdvertenciasContador
		from "BSCL.Custodia.BD::GBL.CerrarEventoCapitalCorteCupon_Salida_AdvertenciasDetalle";
	END;

	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt nvarchar(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP


			CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			INSERT INTO "BSCL.Custodia.BD::GBL.CerrarEventoCapitalCorteCupon_Salida_ErroresDetalle"
				SELECT v_errorExt,::SQL_ERROR_MESSAGE FROM DUMMY;				
				SELECT COUNT(1) INTO p_out_ErroresContador 
				FROM "BSCL.Custodia.BD::GBL.CerrarEventoCapitalCanje_Salida_ErroresDetalle";
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
		-----------------------------------------------------
		--Declaraciones Variables
		-----------------------------------------------------
		DECLARE v_out_AdvertenciasContadorIngreso  		INT;
		DECLARE v_out_ErroresContadorIngreso       		INT;
		DECLARE v_out_AdvertenciasContadorCargoAbono	INT;
		DECLARE v_out_ErroresContadorCargoAbono			INT;
		DECLARE v_out_InformacionContadorCargoAbono		INT;
		DECLARE v_out_InformacionDetalleCargoAbono		TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000));		

		--variables de proceso
		DECLARE v_FechaSistema 			NVARCHAR(10);
		
		DECLARE v_InformacionContador		 INT;
		DECLARE v_Sistema				NVARCHAR(2);		
		
		v_Sistema:= '';
		
		SELECT "Valor" INTO v_Sistema
		FROM "BSCL.Plataforma.BD::PLA.Configuracion"
		WHERE "IdEmpresa" = p_IdEmpresa 
		  AND "CodigoParametro" = 'PLA.LocalizacionSistema';
		
		p_out_TblId = SELECT 0 as "Ids" from dummy where 1=2;

		-- Campos segun linea de Negocio (no se obtienen desde la tabla movimiento en transito)
		p_TblErrores = SELECT '' as "Codigo", '' as "Descripcion" FROM DUMMY WHERE 1=2;
		v_TablaErrores = SELECT '' as "Codigo", '' as "Descripcion" FROM DUMMY WHERE 1=2;
		   
	    CALL  "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (p_IdEmpresa,v_FechaSistema);
	             
		-----------------------------------------------------
		--Validaciones basicas (Codigos,Existencias,etc)
		-----------------------------------------------------

		BEGIN
			-- Se valida que el evento capital debe encontrarse "VIG" para poder realizar el cierre.
/*
			SELECT count(1) INTO p_out_ErroresContador
		    FROM "BSCL.Custodia.BD::CUS.EventoCapital"
			WHERE "IdEventoCapital" = p_IdEventoCapital
			  and "IdEstado" != 'VIG';
*/		  
			SELECT count(1) INTO p_out_ErroresContador
			FROM "BSCL.Custodia.BD::CUS.EventoCapital"
			WHERE "IdTipoEventoCapital" = 'CC'
			  AND "FechaPago" = p_FechaPago
			  AND "IdEstado" != 'VIG';
			
			
			IF (p_out_ErroresContador > 0) THEN
				p_out_ErroresDetalle = SELECT 'EVENTO_CAPITAL' as "Codigo", 'ERROR' as "Descripcion" FROM DUMMY;
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
				SELECT 	 row_number() OVER (order by DT."IdBoveda",	BO."IdPersona", DT."IdMoneda", DT."FechaCierre", Emisor."IdPersona") AS "NumeroIngresoEgreso"
						,'MOVING' 												AS "IdTipoMovimiento"
						,'CNT' 													AS "IdTipoPersona"
						,CASE IFNULL(BO."IdPersona",0)
							WHEN 0 THEN Emisor."IdPersona"
							ELSE BO."IdPersona"
						 END 													AS "IdPersona"
						,DT."IdPortafolio" 										AS "IdPortafolio" -- CGO 2017
						,DT."IdMoneda" 											AS "IdMoneda"
						,CASE v_Sistema
							WHEN 'PE' THEN
								CASE DT."IdBoveda"
									WHEN 'CAV' THEN
										CAST(Sum(DT."MontoBeneficioArchivo") - sum(DT."MontoRetencionArchivo") AS DECIMAL(26,8))
									ELSE
										CAST(Sum(DT."Total") AS DECIMAL(26,8))
								END
							ELSE
								CAST(Sum(DT."Total") AS DECIMAL(26,8))					
						END 													AS "Monto"	
						,DT."FechaCierre"										AS "FechaLiquidacion"
						,'CIECCC'												AS "IdConcepto"
						,'CNT' 													AS "IdTipoPersonaTitularPago"
						,CASE IFNULL(BO."IdPersona",0)
							WHEN 0 THEN Emisor."IdPersona"
							ELSE BO."IdPersona"
						 END													AS "IdPersonaTitularPago"				
						,CAST(0 AS INTEGER)										AS "IdPortafolioTitularPago"			
						,0 														AS "IdCaja"
						,'' 													AS "SecuenciaAgrupacion"
						,0														AS "SecuenciaRelacionada"						
						,'' 													AS "Glosa"
						,0														AS "IdEmpleadoDigitador"							
						,0														AS "IdSucursalEmpleadoDigitador"							
						,''														AS "IdTipoProducto"						
						,'CORCUP'												AS "IdTipoOperacion"					
						,''														AS "IdTipoComprobante"		
						,0														AS "IdComprobante"			
						,DT."FechaCierre"										AS "FechaComprobante"
						,DT."FechaCierre"										AS "FechaLiquidacionComprobante"
						,0 														AS "IdOperacion" 
						,'0001-01-01' 											AS "FechaOperacion"
						,'' 													AS "IdCondicionLiquidacion"
						,'' 													AS "IdBolsa"
						,'' 													AS "IdMonedaOperacion"
						,0 														AS "MontoOperacion"
						,'' 													AS "IdTipoReajuste"
						,'0001-01-01' 											AS "FechaParidad"
						,0 														AS "ValorParidad"
						,0 														AS "FolioCartera"
						,DT."IdInstrumento" 									AS "IdInstrumento"
						,DT."IdTipoInstrumento"									AS "IdTipoInstrumento"
						,DT."IdClasificacionTipoInstrumento"					AS "IdClasificacionTipoInstrumento"				
						,'N' 													AS "Bilateral"
						,'N' 													AS "GeneraComprobante"
						,'N' 													AS "AgruparEnComprobante"
						,'N'													AS "MueveCuentaInversion"
						,'S' 													AS "TitularPago"
					FROM 
						"BSCL.Custodia.BD::CUS.EventoCapital"	 					   			AS MA
						LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapitalCliente"	 				AS DT
							ON (MA."IdEventoCapital" = DT."IdEventoCapital" 
							and MA."IdTipoEventoCapital" = DT."IdTipoEventoCapital" )
						LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarBoveda"             AS BO
							ON (DT."IdBoveda" = BO."IdBoveda")
						LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona"							AS PE
							ON (BO."IdPersona" = PE."IdPersona")
						LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio"   AS SN
							ON (PE."IdPersona" = SN."IdPersona")
						LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" 		as Instru
							ON DT."IdInstrumento" = Instru."IdInstrumento"
						LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" 	as Emisor
							ON  Instru."IdEmisor" = Emisor."IdEmisor"
--							AND DT."IdTipoInstrumento" = Emisor."IdTipoInstrumento"
--						INNER JOIN :v_Secuencias												AS SEC
--							ON SEC."IdBoveda" = DT."IdBoveda"
				 	WHERE  	MA."IdTipoEventoCapital" 	= p_IdTipoEventoCapital		AND
							MA."FechaPago"				= p_FechaPago				AND
							MA."IdEmisor"				= p_IdEmisor
						AND DT."Total" > 0
					GROUP BY 	DT."IdBoveda",
--								SEC."NumeroIngresoEgreso",
								BO."IdPersona",
								DT."IdPortafolio",-- CGO 2017
								DT."IdMoneda",
								DT."FechaCierre",

								DT."IdInstrumento",
								DT."IdTipoInstrumento",
								DT."IdClasificacionTipoInstrumento",
								
								Emisor."IdPersona";
								
				-- PARA ESTE CASO NO SE REQUIERE FORMAS DE PAGO.

--				v_FormasDePago = select * from "BSCL.Tesoreria.BD.Vistas::IngresoEgresoFormaDePago" where 1=2;
				

				--************************************************
				--* PREPARAR DATA PARA EL ENVIO A TESORERIA      *
				--* envío de egreso a tesorería por cada cliente *
				--************************************************
				INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Entrada_IngresoEgresoGenerar"
				SELECT 	DT."IdEventoCapitalCliente"							AS "NumeroReferencia"
--						row_number() OVER (order by FT."IdPortafolio") 	AS "NumeroIngresoEgreso"
						,'MOVEGR' 											AS "IdTipoMovimiento"
						,'CLI' 												AS "IdTipoPersona"
						,SN."IdPersona"
						,0													AS "IdPortafolio" -- CGO 2017
						,DT."IdMoneda" 										AS "IdMoneda"
						,CASE v_Sistema
							WHEN 'PE' THEN
								CASE DT."IdBoveda"
									WHEN 'CAV' THEN
										CAST(Sum(DT."MontoBeneficioArchivo") - sum(DT."MontoRetencionArchivo") AS DECIMAL(26,8))
									ELSE
										CAST(Sum(DT."Total") AS DECIMAL(26,8))
								END
							ELSE
								CAST(Sum(DT."Total") AS DECIMAL(26,8))					
						END 												AS "Monto"	
						,DT."FechaCierre"									AS "FechaLiquidacion"
						,'CIECCC'											AS "IdConcepto"
						,'CLI' 												AS "IdTipoPersonaTitularPago"
						,SN."IdPersona"										AS "IdPersonaTitularPago"				
						,CAST(DT."IdPortafolio" AS INTEGER)									AS "IdPortafolioTitularPago"			
						,0 													AS "IdCaja"
						,'' 												AS "SecuenciaAgrupacion"
						,0													AS "SecuenciaRelacionada"						
						,'' 												AS "Glosa"
						,0													AS "IdEmpleadoDigitador"							
						,0													AS "IdSucursalEmpleadoDigitador"							
						,''													AS "IdTipoProducto"						
						,'CORCUP'											AS "IdTipoOperacion"					
						,''													AS "IdTipoComprobante"		
						,0													AS "IdComprobante"			
						,DT."FechaCierre"									AS "FechaComprobante"
						,DT."FechaCierre"									AS "FechaLiquidacionComprobante"	
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
						,DT."IdInstrumento"									AS "IdInstrumento"
						,DT."IdTipoInstrumento"								AS "IdTipoInstrumento"
						,DT."IdClasificacionTipoInstrumento"				AS "IdClasificacionTipoInstrumento"				
						,'N' 												AS "Bilateral"
						,'N' 												AS "GeneraComprobante"
						,'N' 												AS "AgruparEnComprobante"
						,'N'												AS "MueveCuentaInversion"
						,'S' 												AS "TitularPago"
				FROM "BSCL.Custodia.BD::CUS.EventoCapital"	 					   			AS MA
					LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapitalCliente"	 				AS DT
					ON (MA."IdEventoCapital" = DT."IdEventoCapital" and MA."IdTipoEventoCapital" = DT."IdTipoEventoCapital" )

					INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio"	AS SN
						ON (DT."IdSocioNegocio" = SN."IdSocioDeNegocio")
					LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapitalClienteFormaDePago"			AS ECFP
						ON DT."IdEventoCapitalCliente" = ECFP."IdEventoCapitalCliente"
					LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaPagoPortafolio" 	AS Efp 
						ON ((DT."IdPortafolio" = Efp."IdPortafolio")
						AND Efp."IdEstado" = 'ACT')
			 		WHERE  	MA."IdTipoEventoCapital" 	= p_IdTipoEventoCapital		AND
							MA."FechaPago"				= p_FechaPago				AND
							MA."IdEmisor"				= p_IdEmisor
						AND DT."Total" > 0
					and CASE ECFP."IdMedioDePago"
							WHEN '' THEN CASE Efp."IdMedioDePago"
											WHEN '' 
												THEN 'CIN'
											ELSE 
												IFNULL(Efp."IdMedioDePago",'CIN')
										END
							WHEN NULL THEN 'CIN'
							ELSE CASE Efp."IdMedioDePago"
									WHEN '' 
										THEN 'CIN'
									ELSE 
										IFNULL(Efp."IdMedioDePago",'CIN')
								 END
						END <> 'CIN'
				group by  DT."IdEventoCapitalCliente", DT."IdPortafolio", SN."IdPersona", DT."IdMoneda", DT."FechaCierre",
				DT."IdInstrumento" ,DT."IdTipoInstrumento" ,DT."IdClasificacionTipoInstrumento",DT."IdBoveda";


				-- FORMAS DE PAGO.
				INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Entrada_FormaDePago"
		    	SELECT	row_number() OVER (order by ECC."IdPortafolio")	AS "NumeroReferencia"--"NumeroIngresoEgreso"
						,CASE ECFP."IdMedioDePago"
							WHEN '' THEN CASE Efp."IdMedioDePago"
											WHEN '' 
												THEN 'CIN'
											ELSE 
												IFNULL(Efp."IdMedioDePago",'CIN')
										END
							ELSE CASE Efp."IdMedioDePago"
									WHEN '' 
										THEN 'CIN'
									ELSE 
										IFNULL(Efp."IdMedioDePago",'CIN')
								 END
						END												as "IdMedioDePago"
						,CASE ECFP."IdModalidadPago"
							WHEN '' 
								THEN IFNULL(MdP."IdModalidadDePago",'')
							ELSE 
								IFNULL(ECFP."IdModalidadPago",MdP."IdModalidadDePago")
						 END											As "IdModalidadDePago"
						,ECC."IdMoneda"									as "IdMoneda"
				        ,CAST(sum(ECC."Total")	AS DECIMAL(26,8))		AS "Monto"
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
				FROM "BSCL.Custodia.BD::CUS.EventoCapital"	 					   				AS MAS
				LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapitalCliente"	 						AS ECC
				ON (MAs."IdEventoCapital" = ECC."IdEventoCapital" and MAS."IdTipoEventoCapital" = ECC."IdTipoEventoCapital" )
	
					LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapitalClienteFormaDePago"			AS ECFP
						ON ECC."IdEventoCapitalCliente" = ECFP."IdEventoCapitalCliente"
					LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaPagoPortafolio" 	AS Efp 
						ON ((ECC."IdPortafolio" = Efp."IdPortafolio")
						AND Efp."IdEstado" = 'ACT')
					LEFT JOIN "BSCL.Plataforma.BD.Vistas::PAG.ConsultarModalidadDePago" MdP
						ON Efp."IdMedioDePago" = MdP."IdMedioDePago"
				WHERE  	MAS."IdTipoEventoCapital" 	= p_IdTipoEventoCapital		AND
						MAS."FechaPago"				= p_FechaPago				AND
						MAS."IdEmisor"				= p_IdEmisor
					AND ECC."Total" > 0	
		
					and CASE ECFP."IdMedioDePago"
							WHEN '' THEN CASE Efp."IdMedioDePago"
											WHEN '' 
												THEN 'CIN'
											ELSE 
												IFNULL(Efp."IdMedioDePago",'CIN')
										END
							WHEN NULL THEN 'CIN'
							ELSE CASE Efp."IdMedioDePago"
									WHEN '' 
										THEN 'CIN'
									ELSE 
										IFNULL(Efp."IdMedioDePago",'CIN')
								 END
						END <> 'CIN'
				GROUP BY 	ECC."IdEventoCapitalCliente",
							ECC."IdPortafolio", 
							ECFP."IdMedioDePago", 
							Efp."IdMedioDePago",
							MdP."IdModalidadDePago",
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
					p_EsIngresoAutomatico		=>	'S',
			 		p_out_InformacionContador   => :v_InformacionContador,
					p_out_AdvertenciasContador	=> 	v_out_AdvertenciasContadorIngreso,
					p_out_ErroresContador		=>	v_out_ErroresContadorIngreso
				);
				
				p_out_ErroresContador := p_out_ErroresContador + v_out_ErroresContadorIngreso;
			   	p_out_AdvertenciasContador := p_out_AdvertenciasContador + v_out_AdvertenciasContadorIngreso;
			   
				INSERT INTO "BSCL.Custodia.BD::GBL.CerrarEventoCapitalCorteCupon_Salida_InformacionDetalle"("Codigo","Descripcion")
				SELECT "Codigo", "Descripcion"
				FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_InformacionDetalle";
				
				INSERT INTO "BSCL.Custodia.BD::GBL.CerrarEventoCapitalCorteCupon_Salida_ErroresDetalle"("Codigo","Descripcion")
				SELECT "Codigo", "Descripcion" 
				FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_ErroresDetalle";

				INSERT INTO "BSCL.Custodia.BD::GBL.CerrarEventoCapitalCorteCupon_Salida_AdvertenciasDetalle"("Codigo","Descripcion")
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




			--*****************************************************
			--* PREPARAR DATA PARA EL ENVIO A CUENTA DE INVERSIÓN *
			--*****************************************************

-- nuevo llamado
--				v_CargoAbonoAGenerar = 
				INSERT INTO "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_IN_CargoAbonoAGenerar"
				Select 	EvCapCli."IdEventoCapitalCliente"								AS "NumeroCargoAbono",
						EvCapCli."IdPortafolio"											AS "IdPortafolio",				
						EvCapCli."IdMoneda"												AS "IdMoneda",					
						'MOVABN'														AS "IdTipoMovimiento",			
						'CIECCC'														AS "IdConcepto",				
						Concat('Abono por Corte de Cupón, Instrumento : ' ,  EvCapCli."Nemo")	AS "Descripcion",				
						'T0'															AS "IdCondicionDeLiquidacion",	
						EvCapCli."FechaPago"											AS "FechaCondicionDeLiquidacion",
						CASE v_Sistema
							WHEN 'PE' THEN
								CASE EvCapCli."IdBoveda"
									WHEN 'CAV' THEN
										EvCapCli."MontoBeneficioArchivo" - EvCapCli."MontoRetencionArchivo"
									ELSE
										EvCapCli."Total"
								END
							ELSE
								EvCapCli."Total"
						END 															AS "Monto",		
						0																AS "IdEjecutivo",							
						0																AS "IdSucursal",
												
						''																AS "IdTipoProducto",			
						''																AS "IdTipoOperacion",			
						0																AS "IdOperacion",				
						'0001-01-01'													AS "FechaOperacion",			
						'0001-01-01'													AS "FechaLiquidacionOperacion",	
						'0001-01-01'													AS "FechaVencimientoOperacion",	
						''																AS "IdMonedaOperacion",			
						0																AS "MontoOperacion",			
						0																AS "TipoCambioOperacion",		
						0																AS "IdContraparte",				
						''																AS "IdTipoComprobante",			
						0																AS "IdComprobante",
						0																AS "FolioComprobante",
						0																AS "IdComprobanteDetalle",
						0																AS "IdComprobanteFormaDePago",
						Coalesce(TabCompTeso."IdTipoMovimiento",'') 					As "IdTipoMovimientoAsociado",
						Coalesce(TabCompTeso."IdIngresoEgreso",0) 						As "IdMovimientoAsociado",	
										
						'0001-01-01'													AS "FechaComprobante",			
						0																AS "IdPersonaPagoCobro",		
						EvCapCli."IdInstrumento"										AS "IdInstrumento",				
						EvCapCli."IdTipoInstrumento"									AS "IdTipoInstrumento",			
						0																AS "Cantidad",--cantidad
						EvCapCli."Nemo"													AS "Nemo",						
						''																AS "EsIndicadorBilateral",		
						'N'																AS "EsCargoAbonoPorTraspaso",	
						0																AS "IdPortafolioTraspaso",	
--							''																AS "GeneraContraparteEnTesoreria",
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
						END																AS "GeneroContraparteEnTesoreria",
						'S'																AS "CertificadoAutomatico"
				FROM "BSCL.Custodia.BD::CUS.EventoCapital"	 					   			AS EvCapMa
				LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapitalCliente"	 					AS EvCapCli
					ON (EvCapMa."IdEventoCapital" = EvCapCli."IdEventoCapital" and EvCapMa."IdTipoEventoCapital" = EvCapCli."IdTipoEventoCapital" )

					INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" 				AS ConCliente
						ON EvCapCli."IdPortafolio" = ConCliente."IdPortafolio"
					LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapitalClienteFormaDePago"			AS ECFP
						ON EvCapCli."IdEventoCapitalCliente" = ECFP."IdEventoCapitalCliente"
					LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaPagoPortafolio" 	AS Efp 
						ON ((EvCapCli."IdPortafolio" 	= Efp."IdPortafolio")
						AND Efp."IdEstado" 				= 'ACT'
						AND Efp."IdTipoOperacion" 		= 'CORCUP'--'DIVDIN'
						AND Efp."PorDefecto" 			= 'S')
					LEFT JOIN :v_TabCmbteTesIng													AS TabCompTeso
						ON TabCompTeso."IdComprobante" = EvCapCli."IdEventoCapitalCliente"
					WHERE  	EvCapMa."IdTipoEventoCapital" 	= p_IdTipoEventoCapital		AND
						EvCapMa."FechaPago"				= p_FechaPago				AND
						EvCapMa."IdEmisor"				= p_IdEmisor
					AND EvCapCli."Total" > 0;

				-- CARGO A GENERAR EN EL CASO DE LOS CLIENTES "CUENTA MANDANTE" QUE TENGAN CONFIGURADO QUE EL PAGO
				-- DE LOS DIVIDENDOS SE REALIZARÁ DIRECTAMENTE AL CLIENTE Y NO MEDIANTE LA CORREDORA. JIRA "SAP-5857"
				INSERT INTO "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_IN_CargoAbonoAGenerar"
				Select 	EvCapCli."IdEventoCapitalCliente"								AS "NumeroCargoAbono",
						EvCapCli."IdPortafolio"											AS "IdPortafolio",				
						EvCapCli."IdMoneda"												AS "IdMoneda",					
						'MOVCAR'														AS "IdTipoMovimiento",			
						'CIECCC'														AS "IdConcepto",				
						Concat('Cargo - Pago por Emisor - Corte de Cupón, Instrumento : ' ,  EvCapCli."Nemo")	AS "Descripcion",				
						'T0'															AS "IdCondicionDeLiquidacion",	
						EvCapCli."FechaPago"											AS "FechaCondicionDeLiquidacion",
						CASE v_Sistema
							WHEN 'PE' THEN
								CASE EvCapCli."IdBoveda"
									WHEN 'CAV' THEN
										EvCapCli."MontoBeneficioArchivo" - EvCapCli."MontoRetencionArchivo"
									ELSE
										EvCapCli."Total"
								END
							ELSE
								EvCapCli."Total"
						END 															AS "Monto",	
						0																AS "IdEjecutivo",							
						0																AS "IdSucursal",
						''																AS "IdTipoProducto",			
						''																AS "IdTipoOperacion",			
						0																AS "IdOperacion",				
						'0001-01-01'													AS "FechaOperacion",			
						'0001-01-01'													AS "FechaLiquidacionOperacion",	
						'0001-01-01'													AS "FechaVencimientoOperacion",	
						''																AS "IdMonedaOperacion",			
						0																AS "MontoOperacion",			
						0																AS "TipoCambioOperacion",		
						0																AS "IdContraparte",				
						''																AS "IdTipoComprobante",			
						0																AS "IdComprobante",
						0																AS "FolioComprobante",
						0																AS "IdComprobanteDetalle",
						0																AS "IdComprobanteFormaDePago",
						Coalesce(TabCompTeso."IdTipoMovimiento",'') 					As "IdTipoMovimientoAsociado",
						Coalesce(TabCompTeso."IdIngresoEgreso",0) 						As "IdMovimientoAsociado",	
										
						'0001-01-01'													AS "FechaComprobante",			
						0																AS "IdPersonaPagoCobro",		
						EvCapCli."IdInstrumento"										AS "IdInstrumento",				
						EvCapCli."IdTipoInstrumento"									AS "IdTipoInstrumento",			
						0																AS "Cantidad",--cantidad
						EvCapCli."Nemo"													AS "Nemo",						
						''																AS "EsIndicadorBilateral",		
						'N'																AS "EsCargoAbonoPorTraspaso",	
						0																AS "IdPortafolioTraspaso",	
--							''																AS "GeneraContraparteEnTesoreria",
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
						END																AS "GeneroContraparteEnTesoreria",
						'S'																AS "CertificadoAutomatico"
				FROM "BSCL.Custodia.BD::CUS.EventoCapital"	 					   			AS EvCapMa
				LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapitalCliente"	 					AS EvCapCli
					ON (EvCapMa."IdEventoCapital" = EvCapCli."IdEventoCapital" and EvCapMa."IdTipoEventoCapital" = EvCapCli."IdTipoEventoCapital" )

					INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" 				AS ConCliente
						ON EvCapCli."IdPortafolio" = ConCliente."IdPortafolio"
					LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapitalClienteFormaDePago"			AS ECFP
						ON EvCapCli."IdEventoCapitalCliente" = ECFP."IdEventoCapitalCliente"
					LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaPagoPortafolio" 	AS Efp 
						ON ((EvCapCli."IdPortafolio" 	= Efp."IdPortafolio")
						AND Efp."IdEstado" 				= 'ACT'
						AND Efp."IdTipoOperacion" 		= 'CORCUP'--'DIVDIN'
						AND Efp."PorDefecto" 			= 'S')
					LEFT JOIN :v_TabCmbteTesIng													AS TabCompTeso
						ON TabCompTeso."IdComprobante" = EvCapCli."IdEventoCapitalCliente"
					WHERE EvCapMa."IdTipoEventoCapital" 	= p_IdTipoEventoCapital
					  AND EvCapMa."FechaPago"				= p_FechaPago
					  AND EvCapMa."IdEmisor"				= p_IdEmisor
					  AND EvCapCli."Total" 					> 0
					  AND EvCapCli."PagaClienteCorredor"	= 'CL';


	          Call "BSCL.CuentaInversion.BD.Servicios::CrearCargoAbono"
	            (
		            p_IdEmpresa => 					:p_IdEmpresa,
		            p_IdUsuario => 					:p_IdUsuario,
		            p_IdModulo => 					'CUS',
		            p_simularOperacion => 			'N',
			        p_UsaReglasNegocio => 			'S',		
			        p_FechaSistema => 				:v_FechaSistema,
					p_EsIngresoManual => 			'N',
					
		            p_out_InformacionContador =>	v_out_InformacionContadorCargoAbono,
		            p_out_AdvertenciasContador =>	v_out_AdvertenciasContadorCargoAbono,
		            p_out_ErroresContador => 		v_out_ErroresContadorCargoAbono
	            );

			INSERT INTO "BSCL.Custodia.BD::GBL.CerrarEventoCapitalCorteCupon_Salida_InformacionDetalle"("Codigo","Descripcion")
			SELECT "Codigo", "Descripcion"
			FROM "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_OUT_InformacionDetalle";
				
			INSERT INTO "BSCL.Custodia.BD::GBL.CerrarEventoCapitalCorteCupon_Salida_ErroresDetalle"("Codigo","Descripcion")
			SELECT "Codigo", "Descripcion" 
			FROM "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_OUT_ErroresDetalle";

			INSERT INTO "BSCL.Custodia.BD::GBL.CerrarEventoCapitalCorteCupon_Salida_AdvertenciasDetalle"("Codigo","Descripcion")
			SELECT "Codigo", "Descripcion" 
			FROM "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_OUT_AdvertenciasDetalle";
			
--------------------------------------------------------------------------------------------------------
				
-- ACTUALIZAMOS EC Y ECC

			UPDATE  "BSCL.Custodia.BD::CUS.EventoCapital" 
			SET "IdEstado"	= 'CER'
			WHERE  	"IdTipoEventoCapital" 	= p_IdTipoEventoCapital		AND
					"FechaPago"				= p_FechaPago				AND
					"IdEmisor"				= p_IdEmisor;
		
		
			UPDATE  DT
			SET "IdEstado"	= 'CER'
			FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente"	 			AS DT
				INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapital"	 					   	AS MA
					ON (MA."IdEventoCapital" = DT."IdEventoCapital" 
					and MA."IdTipoEventoCapital" = DT."IdTipoEventoCapital" )
			WHERE 		MA."IdTipoEventoCapital" 	= p_IdTipoEventoCapital		AND
						MA."FechaPago"				= p_FechaPago				AND
						MA."IdEmisor"				= p_IdEmisor				;
	
	
			SELECT COUNT(*) INTO p_out_ErroresContador		FROM "BSCL.Custodia.BD::GBL.CerrarEventoCapitalCorteCupon_Salida_ErroresDetalle";
			SELECT COUNT(*) INTO p_out_AdvertenciasContador	FROM "BSCL.Custodia.BD::GBL.CerrarEventoCapitalCorteCupon_Salida_AdvertenciasDetalle";

	--------------------------------------------------------------------------------------------------------
			IF (p_out_ErroresContador > 0) THEN 
				SIGNAL SALIDA_CONTROLADA;
			ELSE
				v_TablaCodigosMensajes =	Select 	1								As "IdInterno", 
													'UI_OPERACION_CERRAR_EXITO'		As "Codigo",
													'' 								As "CodigoDato1", 
													'' 								as "ValorDato1",
													'' 								As "CodigoDato2", 
													'' 								As "ValorDato2" 
												From dummy;
					                        
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,:v_out_hash);
				
				p_out_InformacionContador = 1;
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
