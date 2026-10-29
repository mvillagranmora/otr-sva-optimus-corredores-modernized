SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Mesa.BD.Servicios::FWD.CrearMovimientoForwardTesoreria";
GO
CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::FWD.CrearMovimientoForwardTesoreria"(
	IN p_IdEmpresa				BIGINT,
    IN p_IdUsuario				BIGINT,
    IN p_IdModulo				NVARCHAR(3),
    IN p_IdPais					BIGINT,
    IN p_IngresoEgresoGenerar	TABLE(	 "IdCartera"			BIGINT
    									,"FolioCartera"			BIGINT
    									,"IdOperacion"			BIGINT
    									,"IdEstado"				NVARCHAR(3)
    									,"MontoOperacion"		DECIMAL(26,8)
    									,"IdMonedaLiquidacion"	NVARCHAR(3)),
    
    -- Salida Id Generado
	OUT p_out_IngresoEgresoGenerado	TABLE(	 "IdCartera"			BIGINT
											,"FolioCartera"			BIGINT
											,"IdOperacion"			BIGINT
    									 	,"NumeroIngresoEgreso"	BIGINT
    										,"IdIngresoEgreso" 		BIGINT
    										,"IdTipoMovimiento" 	NVARCHAR(6)),
	
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador	INT,
	OUT p_out_AdvertenciasDetalle	TABLE (	 "FolioCartera"			BIGINT
											,"Codigo" 				NVARCHAR(100)
											,"RequiereAutorizacion"	NVARCHAR(40)
											,"Descripcion" 			NVARCHAR(520))DEFAULT EMPTY,
	OUT p_out_ErroresContador		INT,
	OUT p_out_ErroresDetalle		TABLE (	 "FolioCartera"	BIGINT, "Codigo" NVARCHAR(100), "Descripcion" NVARCHAR(520))DEFAULT EMPTY
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER  
DEFAULT SCHEMA "BSCL"
--READS SQL DATA   
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorhashmsg TABLE ("Codigo" NVARCHAR(100), "Valor" NVARCHAR(520));
	DECLARE v_erroroutmsg  NVARCHAR(520);	        	

	DECLARE v_secuenciaRelacionada	BIGINT:=0;
	DECLARE v_countDetalles			INT:=0;
	DECLARE v_countCambioEstado		INT:=0;
	
	DECLARE v_fechaHoraSistema		NVARCHAR(40);
	DECLARE v_ValorParametro		NVARCHAR(3);
	DECLARE v_NombreParametro		NVARCHAR(100);
	
	DECLARE v_max_id				BIGINT;
	
	DECLARE v_out_IngresoEgresoGenerado	TABLE (	 "IdCartera"			BIGINT
												,"FolioCartera"			BIGINT
												,"IdOperacion"			BIGINT
	    									 	,"NumeroIngresoEgreso"	BIGINT
	    									 	,"IdIngresoEgreso" 		BIGINT
	    								)DEFAULT SELECT 0 AS "IdCartera",0 AS "FolioCartera", 0 AS "IdOperacion", 0 AS "NumeroIngresoEgreso", 0 as "IdIngresoEgreso"
						                         FROM dummy WHERE 1=2;
    										  
	DECLARE v_out_AdvertenciasDetalle  	TABLE (	"FolioCartera"			BIGINT
												,"Codigo"				NVARCHAR(100)
												,"RequiereAutorizacion"	NVARCHAR(40)
												,"Descripcion"			NVARCHAR(5000)
												--,"ValorParametro"		NVARCHAR(100)
												--,"NombreParametro"		NVARCHAR(40)
										)DEFAULT SELECT 0 AS "FolioCartera", '' AS "Codigo", 
														'N' AS "RequiereAutorizacion", '' AS "Descripcion"
												 FROM dummy WHERE 1=2;
	
	DECLARE v_out_ErroresDetalle  		TABLE (	 "FolioCartera"		BIGINT
												,"Codigo"			NVARCHAR(100)
												,"Descripcion"		NVARCHAR(5000)
												--,"ValorParametro"	NVARCHAR(100)
												--,"NombreParametro"	NVARCHAR(40)
										)DEFAULT SELECT 0 AS "FolioCartera", '' AS "Codigo", '' AS "Descripcion"
						                         FROM dummy WHERE 1=2;
	
	DECLARE v_DetalleLog 			    TABLE (	 "SubRegistro"			NVARCHAR(40)
                                            	,"CampoRegistro" 		NVARCHAR(40)
                                            	,"ValorAntiguo" 		NVARCHAR(250)
                                            	,"ValorNuevo" 			NVARCHAR(250));
	
	DECLARE v_TableFormaPago			TABLE (	 "NumeroReferencia"					BIGINT
												,"IdMedioDePago"					NVARCHAR(3) -- CHE, EFE, TRA, VVI, CIN
												,"IdModalidadDePago"				NVARCHAR(6) -- MODCAJ: Por Caja   MODDEP: Deposito
												,"IdMoneda"							NVARCHAR(3)
												,"Monto"							DECIMAL(26,8)
												,"IdCuentaBancariaCorredora"		BIGINT
												,"IdTarjetaBancariaCorredora"		BIGINT
												,"IdChequera"						BIGINT
												,"NumeroDocumentoCorredora"			BIGINT
												,"FechaDocumentoPagoCorredora"		NVARCHAR(40)
												,"IdCuentaBancariaSocio" 			BIGINT
												,"IdBancoSocio"						BIGINT
												,"NombreCuentaBancariaSocio"		NVARCHAR(100)
												,"NumeroCuentaBancariaSocio"		NVARCHAR(100)
												,"IdTipoCuentaBancariaSocio"		NVARCHAR(4)
												,"NombreTipoCuentaBancariaSocio"	NVARCHAR(100)
												,"IdTarjetaBancariaSocio"			BIGINT
												,"IdTipoTarjetaBancariaSocio"		NVARCHAR(40)
												,"NombreTipoTarjetaBancariaSocio"	NVARCHAR(100)
												,"NumeroTarjetaBancariaSocio"		NVARCHAR(100)
												,"NumeroSerieSocio"					NVARCHAR(40)
												,"NumeroDocumentoSocio"				BIGINT
												,"FechaDocumentoPagoSocio"			NVARCHAR(40));
	
	DECLARE p_out_InformacionContador  	INT;
 	DECLARE p_out_InformacionDetalle   	TABLE ("NumeroReferencia" 	BIGINT
 											  ,"Id" 				BIGINT
 									          ,"Codigo" 			NVARCHAR(100)
 											  ,"Descripcion" 		NVARCHAR(5000));
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador from :v_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador from :v_out_AdvertenciasDetalle; 	
		
		v_TablaCodigosMensajes = SELECT "Codigo" AS "CodigoMensaje", '' AS "CodigoDato", '' AS "ValorDato"
								 FROM :v_out_ErroresDetalle
								 UNION ALL
								 SELECT "Codigo" AS "CodigoMensaje", '' AS "CodigoDato", '' AS "ValorDato"
								 FROM :v_out_AdvertenciasDetalle;
		
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes,v_out_ErroresDetalleMensajes);
		
		p_out_ErroresDetalle = 	SELECT errores."FolioCartera", errores."Codigo", msj."Descripcion"
								FROM :v_out_ErroresDetalle errores
								INNER JOIN :v_out_ErroresDetalleMensajes msj ON errores."Codigo" = msj."Codigo";
		
		p_out_AdvertenciasDetalle = SELECT adv."FolioCartera", adv."Codigo", adv."RequiereAutorizacion", msj."Descripcion"
								    FROM :v_out_AdvertenciasDetalle adv
								    INNER JOIN :v_out_ErroresDetalleMensajes msj ON adv."Codigo" = msj."Codigo";
				 						 	
	END;
	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	v_countDetalles				:=0;
	v_countCambioEstado			:=0;
	
	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(:p_IdEmpresa, :v_fechaHoraSistema);
	
	
    BEGIN       
		--*******************************
		--*CUENTA INVERSION             *
		--*******************************
		
		p_CargoAbonoAGenerar =
	   		SELECT
	   			 INGEGR."FolioCartera"								as "NumeroCargoAbono"
	   			,FWD."IdPortafolio"									as "IdPortafolio"
	   			/*,CASE WHEN FWD."IdTipoOperacion" = 'COMFWD' THEN FWD."IdMonedaSecundaria"
			   		  WHEN FWD."IdTipoOperacion" = 'VENFWD' THEN FWD."IdMonedaPrincipal" END	as "IdMoneda"*/
			   	,INGEGR."IdMonedaLiquidacion"						as "IdMoneda"
			   	,CASE WHEN FWD."IngresoEgreso"='ING' THEN 'MOVABN'
			   		  WHEN FWD."IngresoEgreso"='EGR' THEN 'MOVCAR' END	as "IdTipoMovimiento"
				--,'VCDFWD'											as "IdConcepto"
				,CASE WHEN FWD."TipoVencimiento" = 'ANTICIPADA' THEN 'ANTFWD' ELSE 'VCDFWD' END "IdConcepto"
			    ,''													as "Descripcion"
			    ,'T0'												as "IdCondicionDeLiquidacion"
			    ,TO_VARCHAR(:v_fechaHoraSistema, 'YYYY-MM-DD')		as "FechaCondicionDeLiquidacion"
			    --,CASE WHEN FWD."IngresoEgreso"='ING' THEN FWD."MontoRecibimos"
			   		  --WHEN FWD."IngresoEgreso"='EGR' THEN FWD."MontoPagamos" END	as "Monto"
			   	,INGEGR."MontoOperacion"						as "Monto"
			    ,FWD."IdAgente"									as "IdEjecutivo"
			    ,FWD."IdSucursal"								as "IdSucursal"
			    ,'MEFW'											as "IdTipoProducto"
			    ,FWD."IdTipoOperacion"							as "IdTipoOperacion"
			    ,INGEGR."FolioCartera"							as "IdOperacion"
			    ,FWD."FechaOperacion"							as "FechaOperacion"
			    ,TO_VARCHAR(:v_fechaHoraSistema, 'YYYY-MM-DD')	as "FechaLiquidacionOperacion"
			    ,FWD."FechaVencimiento"							as "FechaVencimientoOperacion"
			    ,INGEGR."IdMonedaLiquidacion"					as "IdMonedaOperacion"
			    ,INGEGR."MontoOperacion"						as "MontoOperacion"
			    ,CASE WHEN FWD."Nemotecnico" = 'ARBITR'	THEN FWD."ParidadCierre"
			    	  ELSE FWD."TipoCambioCierre" END			as "TipoCambioOperacion"
			    ,FWD."IdCliente"								as "IdContraparte"
			    ,'MFW' 											as "IdTipoComprobante"
			   	,INGEGR."FolioCartera"							as "IdComprobante"
				,''												as "FolioComprobante"
				,0												as "IdComprobanteDetalle"
				,0												as "IdComprobanteFormaDePago"
				,''												as "IdTipoMovimientoAsociado"
				,0												as "IdMovimientoAsociado"
				,TO_VARCHAR(:v_fechaHoraSistema, 'YYYY-MM-DD')	as "FechaComprobante"
				,0												as "IdPersonaPagoCobro"
				,FWD."IdInstrumento"							as "IdInstrumento"
				,FWD."IdTipoInstrumento"						as "IdTipoInstrumento"
				,FWD."Nemotecnico"								as "Nemo"
				,'N'											as "EsIndicadorBilateral"
				,'N'											as "EsCargoAbonoPorTraspaso"
				,0												as "IdPortafolioTraspaso"
				,'N'											as "GeneroContraparteEnTesoreria"--S:genera mov cta y tes  N: genero mov cta
				,'S'											as "CertificadoAutomatico"	
		   	FROM "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWDVencimiento" AS FWD
			INNER JOIN :p_IngresoEgresoGenerar AS INGEGR
				ON (INGEGR."IdCartera"		= FWD."IdCartera" AND 
					INGEGR."FolioCartera"	= FWD."IdOperacion" AND 
					INGEGR."IdEstado"		= FWD."IdEstado")
			LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" POR
				ON (FWD."IdPortafolio" = POR."IdPortafolio")
			LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" indPF 
				ON (POR."IdPortafolio"			= indPF."IdPortafolio" AND
					indPF."IdTipoEntidad"		= 'PORTAFOLIO' AND 
					indPF."CodigoTipoIndicador"	= 'GEN_OCCINV')
			WHERE FWD."ModalidadLiquidacionValor" = 'COMPENSADA'
		   	;
		   	
		SELECT 'p_CargoAbonoAGenerar', * FROM :p_CargoAbonoAGenerar;
		
		INSERT INTO "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_IN_CargoAbonoAGenerar"(
			"NumeroCargoAbono",
			"IdPortafolio",
			"IdMoneda",
			"IdTipoMovimiento",
			"IdConcepto",
			"Descripcion",
			"IdCondicionDeLiquidacion",
			"FechaCondicionDeLiquidacion",
			"Monto",
			"IdEjecutivo",
			"IdSucursal",
			"IdTipoProducto",
			"IdTipoOperacion",
			"IdOperacion",
			"FechaOperacion",
			"FechaLiquidacionOperacion",
			"FechaVencimientoOperacion",
			"IdMonedaOperacion",
			"MontoOperacion",
			"TipoCambioOperacion",
			"IdContraparte",
			"IdTipoComprobante",
			"IdComprobante",
			"FolioComprobante",
			"IdComprobanteDetalle",
			"IdComprobanteFormaDePago",
			"IdTipoMovimientoAsociado",
			"IdMovimientoAsociado",
			"FechaComprobante",
			"IdPersonaPagoCobro",
			"IdInstrumento",
			"IdTipoInstrumento",
			"Nemo",
			"EsIndicadorBilateral",
			"EsCargoAbonoPorTraspaso",
			"IdPortafolioTraspaso",
			"GeneroContraparteEnTesoreria",
			"CertificadoAutomatico"
		)
	   	SELECT
	   		"NumeroCargoAbono",
			"IdPortafolio",
			"IdMoneda",
			"IdTipoMovimiento",
			"IdConcepto",
			"Descripcion",
			"IdCondicionDeLiquidacion",
			"FechaCondicionDeLiquidacion",
			"Monto",
			"IdEjecutivo",
			"IdSucursal",
			"IdTipoProducto",
			"IdTipoOperacion",
			"IdOperacion",
			"FechaOperacion",
			"FechaLiquidacionOperacion",
			"FechaVencimientoOperacion",
			"IdMonedaOperacion",
			"MontoOperacion",
			"TipoCambioOperacion",
			"IdContraparte",
			"IdTipoComprobante",
			"IdComprobante",
			"FolioComprobante",
			"IdComprobanteDetalle",
			"IdComprobanteFormaDePago",
			"IdTipoMovimientoAsociado",
			"IdMovimientoAsociado",
			"FechaComprobante",
			"IdPersonaPagoCobro",
			"IdInstrumento",
			"IdTipoInstrumento",
			"Nemo",
			"EsIndicadorBilateral",
			"EsCargoAbonoPorTraspaso",
			"IdPortafolioTraspaso",
			"GeneroContraparteEnTesoreria",
			"CertificadoAutomatico"
	   	FROM :p_CargoAbonoAGenerar;
	   	
	   	CALL "BSCL.CuentaInversion.BD.Servicios::CrearCargoAbono" (
			p_IdEmpresa                 => :p_IdEmpresa, 
		   	p_IdUsuario                 => :p_IdUsuario, 
		    p_IdModulo                  => :p_IdModulo,
			p_simularOperacion			=> 'N',
			p_UsaReglasNegocio			=> 'S',
			p_FechaSistema  			=> TO_VARCHAR(v_fechaHoraSistema, 'YYYY-MM-DD'),
			p_EsIngresoManual			=> 'N',
			p_out_InformacionContador	=> p_out_InformacionContador,
			p_out_AdvertenciasContador	=> p_out_AdvertenciasContador,
			p_out_ErroresContador		=> p_out_ErroresContador
		);
		
		
		--verifica errores
		v_out_AdvertenciasDetalle =
			SELECT * FROM :v_out_AdvertenciasDetalle
			UNION ALL
			SELECT "NumeroCargoAbono" as "FolioCartera", "Codigo", "RequiereAutorizacion", "Descripcion"
			FROM "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_OUT_AdvertenciasDetalle";
		
		v_out_ErroresDetalle =
			SELECT * FROM :v_out_ErroresDetalle
			UNION ALL
			SELECT "NumeroCargoAbono" as "FolioCartera", "Codigo", "Descripcion"
			FROM "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_OUT_ErroresDetalle";
	   
		SELECT COUNT(*) INTO p_out_ErroresContador FROM "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_OUT_ErroresDetalle";
		IF (:p_out_ErroresContador > 0) THEN
			SIGNAL SALIDA_CONTROLADA;
		ELSE
			p_out_IngresoEgresoGenerado =
				SELECT
					 ING."IdCartera"
					,ING."FolioCartera"
					,ING."IdOperacion"
					,OUTING."NumeroCargoAbono" AS "NumeroIngresoEgreso"
					,OUTING."IdCargoAbonoMovimiento" AS "IdIngresoEgreso"
					,OUTING."IdTipoMovimiento"
					
				FROM "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_OUT_CargoAbonoGenerado" AS OUTING
				INNER JOIN :p_IngresoEgresoGenerar AS ING
					ON (OUTING."NumeroCargoAbono" = ING."FolioCartera");
			
			v_out_IngresoEgresoGenerado =
				SELECT
					 DISTINCT(ING."IdCartera") AS "IdCartera"
					,ING."FolioCartera"
					,ING."IdOperacion"
					,ING."NumeroIngresoEgreso"
					,0 as "IdIngresoEgreso"
				FROM :p_out_IngresoEgresoGenerado ING;
			
			--select 'p_out_IngresoEgresoGenerado1', * from :p_out_IngresoEgresoGenerado;
			--select 'v_out_IngresoEgresoGenerado1', * from :v_out_IngresoEgresoGenerado;
		END IF;
		
		select 'OUT_CargoAbonoGenerado', * FROM "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_OUT_CargoAbonoGenerado";
		select 'OUT_InformacionDetalle', * FROM "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_OUT_InformacionDetalle";
		
		
		--*******************************
		--*TESORERIA                    *
		--*******************************
		
		--Se guarda la secuencia relacionada
		SELECT "BSCL.Tesoreria.BD.Secuencias::SecuenciaRelacionada".NEXTVAL INTO v_secuenciaRelacionada FROM DUMMY;
		
		--Se guarda el ultimo ID
		SELECT count(1) into v_max_id FROM :p_IngresoEgresoGenerar;
		
		--IMPCREDOPT-2723
		--De las operaciones Forward, obtiene las formas de pago que no son Cuenta Inversion
		v_FormasDePagoNoCuentaInversion =
			SELECT
				 CPO."IdOperacion"
				,INGEGR."IdCartera"
				,INGEGR."FolioCartera"
				,INGEGR."IdEstado"
				,CPO."IdModalidadPago"
				,CPO."IdFormaDePago"
				,CPO."IdTipoProducto"
				,CPO."Monto" AS "MontoFormaDePagoForward"
				,CPO."IdMoneda" AS "MonedaFormaDePagoForward"
				,FWD."MontoMonedaPrincipal"
				,CPO."Monto" / FWD."MontoMonedaPrincipal" AS "ProporcionMontoFormaDePago"
				,INGEGR."MontoOperacion" AS "MontoLiquidacion"
				,INGEGR."IdMonedaLiquidacion"
				,INGEGR."MontoOperacion" * (CPO."Monto" / FWD."MontoMonedaPrincipal") AS "MontoTesoreria"
				,FWD."IdPortafolio"
			FROM "BSCL.Mesa.BD::MES.ComprobantePagoOperacion" CPO
			INNER JOIN :p_IngresoEgresoGenerar AS INGEGR
				ON (CPO."IdOperacion" = INGEGR."IdCartera" AND
					CPO."IdEmpresa" = :p_IdEmpresa)
			INNER JOIN "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWDVencimiento" AS FWD
				ON (INGEGR."IdCartera"		= FWD."IdCartera" AND 
					INGEGR."FolioCartera"	= FWD."IdOperacion" AND 
					INGEGR."IdEstado"		= FWD."IdEstado")
			WHERE CPO."AfectarTesoreria" = 'S'
			  AND FWD."ModalidadLiquidacionValor" = 'COMPENSADA';
		
		select 'v_FormasDePagoNoCuentaInversion', * FROM :v_FormasDePagoNoCuentaInversion;
		
		--Realiza la suma de la proporcion de los montos de cada forma de pago para obtener el monto del movimiento Tesoreria
		v_SumaFormasDePago =
			SELECT
				 FPA."IdCartera"
				,FPA."FolioCartera"
				,FPA."IdMonedaLiquidacion"
				,FPA."IdEstado"
				,SUM(FPA."MontoTesoreria") AS "MontoTesoreria"
			FROM :v_FormasDePagoNoCuentaInversion AS FPA
			GROUP BY FPA."IdCartera", FPA."FolioCartera", FPA."IdMonedaLiquidacion", FPA."IdEstado";
		
		select 'v_SumaFormasDePago', * FROM :v_SumaFormasDePago;
		
		--Se crea tabla para tesoreria:
		v_IngresoEgresoGenerar =
			SELECT
				 FPA."FolioCartera"									as "NumeroReferencia"
				,CASE WHEN FWD."IngresoEgreso"='ING' THEN 'MOVEGR' WHEN FWD."IngresoEgreso"='EGR' THEN 'MOVING' END	as "IdTipoMovimiento"
			   	,'CLI' 												as "IdTipoPersona"
			   	,CLI."IdPersona"  									as "IdPersona"
			   	,FWD."IdPortafolio"									as "IdPortafolio"
			   	,FPA."IdMonedaLiquidacion"							as "IdMoneda"
			   	,FPA."MontoTesoreria"								as "Monto"
				,TO_VARCHAR(:v_fechaHoraSistema, 'YYYY-MM-DD')		as "FechaLiquidacion"
			   	,'VCDFWD'											as "IdConcepto"
			   	,'CLI' 												as "IdTipoPersonaTitularPago"
			   	,CLI."IdPersona" 									as "IdPersonaTitularPago"
			   	,FWD."IdPortafolio"  								as "IdPortafolioTitularPago"
			   	,0 													as "IdCaja"
			   	,'' 												as "SecuenciaAgrupacion"
			   	,v_secuenciaRelacionada 							as "SecuenciaRelacionada"
			   	,'' 												as "Glosa"
			   	-- Parametros Opcionales(Referenciales)
			   	,FWD."IdAgente"										as "IdEmpleadoDigitador"
			   	,0 													as "IdSucursalEmpleadoDigitador"
			   	,FWD."IdTipoProducto"								as "IdTipoProducto"
			    ,FWD."IdTipoOperacion"								as "IdTipoOperacion"
			    ,'MFW' 												as "IdTipoComprobante"
			   	,FPA."FolioCartera"									as "IdComprobante"
			   	,TO_VARCHAR(:v_fechaHoraSistema, 'YYYY-MM-DD')		as "FechaComprobante"
			    ,TO_VARCHAR(:v_fechaHoraSistema, 'YYYY-MM-DD')		as "FechaLiquidacionComprobante"
			    ,FWD."FolioOperacion"		 						as "IdOperacion"
			    ,(TO_CHAR(FWD."FechaOperacion", 'yyyy-MM-dd'))		as "FechaOperacion"
			    ,'T0' 												as "IdCondicionLiquidacion"
			    ,'BCS' 												as "IdBolsa"
			    ,FPA."IdMonedaLiquidacion" 							as "IdMonedaOperacion"
			    ,FPA."MontoTesoreria"								as "MontoOperacion"
			    ,'' 												as "IdTipoReajuste"
			    ,'' 												as "FechaParidad"
			    ,0 													as "ValorParidad"
			    ,0 													as "FolioCartera"
			    ,FWD."IdInstrumento"	  							as "IdInstrumento"
			    ,FWD."IdTipoInstrumento"							as "IdTipoInstrumento"
			    ,TIPINS."IdClasificacionTipoInstrumento"			as "IdClasificacionTipoInstrumento"
			    -- Indicadores
			    ,'N' 												as "Bilateral"
			    ,'S' 												as "GeneraComprobante"
			    ,'N' 												as "AgruparEnComprobante"
				,'N' 												as "MueveCuentaInversion"
			    ,'S' 												as "TitularPago"
			--FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" AS FWD
			FROM "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWDVencimiento" AS FWD
			INNER JOIN :v_SumaFormasDePago AS FPA
				ON (FWD."IdCartera"		= FPA."IdCartera" AND 
					FWD."IdOperacion"	= FPA."FolioCartera" AND 
					FWD."IdEstado"		= FPA."IdEstado")
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
				ON (CLI."IdCliente" = FWD."IdCliente")
			INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarClasificacionTipoInstrumento" AS TIPINS
				ON (TIPINS."IdTipoInstrumento" = FWD."IdTipoInstrumento")
			/*INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" POR
				ON (FWD."IdPortafolio" = POR."IdPortafolio")
			INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" indPF 
				ON (POR."IdPortafolio"			= indPF."IdPortafolio" AND
					indPF."IdTipoEntidad"		= 'PORTAFOLIO' AND 
					indPF."CodigoTipoIndicador"	= 'GEN_OCCINV'
					AND COALESCE(indPF."ValorIndicador",'N') = 'N'
					)*/
			WHERE --(FWD."IngresoEgreso" = 'ING') AND
				FWD."ModalidadLiquidacionValor" = 'COMPENSADA'
			;
		
		--select 'v_IngresoEgresoGenerar', * FROM :v_IngresoEgresoGenerar;
		
		v_TableFormaPago =
			SELECT
				 FPA."FolioCartera"					AS "NumeroReferencia"
				,fPag."IdMedioDePago" 				AS "IdMedioDePago"					--	NVARCHAR(3), -- CHE, EFE, TRA, VVI, CIN
				,FPA."IdModalidadPago"				AS "IdModalidadDePago"
			   	,FPA."IdMonedaLiquidacion"			AS "IdMoneda"
			   	,FPA."MontoTesoreria"				AS "Monto"
				,0 									AS "IdCuentaBancariaCorredora"		--	BIGINT,
				,0 									AS "IdTarjetaBancariaCorredora"	--BIGINT,
				,0 									AS "IdChequera"						--BIGINT,
				,0 									AS "NumeroDocumentoCorredora"			--BIGINT,
				,'0001-01-01'						AS "FechaDocumentoPagoCorredora"		--NVARCHAR(40),
				,PPP."IdCuentaBancaria"				AS "IdCuentaBancariaSocio" 			--BIGINT,
				,PPP."IdBancoCuentaBancaria"		AS "IdBancoSocio"						--BIGINT,
				,PPP."NombreBancoCuentaBancaria"	AS "NombreCuentaBancariaSocio"		--	NVARCHAR(100),
				,PPP."NumeroCuentaBancaria"			AS "NumeroCuentaBancariaSocio"		--	NVARCHAR(100),
				,PPP."IdTipoCuentaBancaria"			AS "IdTipoCuentaBancariaSocio"		--	NVARCHAR(4),
				,PPP."NombreTipoCuentaBancaria"		AS "NombreTipoCuentaBancariaSocio"	--	NVARCHAR(100),
				,PPP."IdBancoTarjetaBancaria"		AS "IdTarjetaBancariaSocio"			--BIGINT,
				,PPP."IdTipoTarjetaBancaria"		AS "IdTipoTarjetaBancariaSocio"		--NVARCHAR(40),
				,PPP."NombreTipoTarjetaBancaria"	AS "NombreTipoTarjetaBancariaSocio"	--NVARCHAR(100),
				,PPP."NumeroTarjetaBancaria"		AS "NumeroTarjetaBancariaSocio"		--NVARCHAR(100),
				,''									AS "NumeroSerieSocio"				--NVARCHAR(40),
				,0 									AS "NumeroDocumentoSocio"				--BIGINT,
				,'0001-01-01'						AS "FechaDocumentoPagoSocio"			--NVARCHAR(40)
			FROM :v_FormasDePagoNoCuentaInversion FPA
			LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarFormasDePagoMesa" fPag
			    ON (FPA."IdFormaDePago" = fPag."IdFormaDePago" AND
			        FPA."IdTipoProducto" = fPag."IdTipoProducto" AND
			        --cPag."IdMoneda"= fPag."IdMoneda" AND
			        fPag."IdEstado"='ACT')
			LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaPagoPortafolio" PPP
				ON (FPA."IdPortafolio" = PPP."IdPortafolio" AND
					--fPag."IdPreferenciaPago" = PPP."IdPreferenciaPago" AND
					fPag."IdTipoOperacion" = PPP."IdTipoOperacion" AND
					fPag."IdMedioDePago" = PPP."IdMedioDePago" AND
					FPA."IdMonedaLiquidacion" = PPP."IdMoneda" AND
					fPag."IdEmpresa" = PPP."IdEmpresa")
			--WHERE FormaPago."IdTipoMovimiento" = 'MOVEGR'
			
			/*
			UNION ALL
			
			SELECT
				 --FormaPago."IdComprobanteFormaDePago"	AS "NumeroReferencia"
				 v_max_id + INGEGR."FolioCartera"		as "NumeroReferencia" 
				,FormaPago."IdMedioDePago" 				AS "IdMedioDePago"					--	NVARCHAR(3), -- CHE, EFE, TRA, VVI, CIN
				,MODI."IdModalidadDePago"				AS "IdModalidadDePago"		--	NVARCHAR(6), -- MODCAJ: Por Caja   MODDEP: Deposito
				,FormaPago."IdMoneda" 					AS "IdMoneda"						--NVARCHAR(3),
				,FormaPago."Monto" 						AS "Monto"							--	DECIMAL(26,8),
				,0 										AS "IdCuentaBancariaCorredora"		--	BIGINT,
				,0 										AS "IdTarjetaBancariaCorredora"		--BIGINT,
				,0 										AS "IdChequera"						--BIGINT,
				,0 										AS "NumeroDocumentoCorredora"		--BIGINT,
				,'' 									AS "FechaDocumentoPagoCorredora"	--NVARCHAR(40),
				,0 										AS "IdCuentaBancariaSocio" 			--BIGINT,
				,0 										AS "IdBancoSocio"					--BIGINT,
				,'' 									AS "NombreCuentaBancariaSocio"		--	NVARCHAR(100),
				,'' 									AS "NumeroCuentaBancariaSocio"		--	NVARCHAR(100),
				,'' 									AS "IdTipoCuentaBancariaSocio"		--	NVARCHAR(4),
				,'' 									AS "NombreTipoCuentaBancariaSocio"	--	NVARCHAR(100),
				,0 										AS "IdTarjetaBancariaSocio"			--BIGINT,
				,'' 									AS "IdTipoTarjetaBancariaSocio"		--NVARCHAR(40),
				,'' 									AS "NombreTipoTarjetaBancariaSocio"	--NVARCHAR(100),
				,'' 									AS "NumeroTarjetaBancariaSocio"		--NVARCHAR(100),
				,''										AS "NumeroSerieSocio"				--NVARCHAR(40),
				,0 										AS "NumeroDocumentoSocio"			--BIGINT,
				,'' 									AS "FechaDocumentoPagoSocio"			
			FROM "BSCL.Mesa.BD::ComprobanteFormaDePago" AS FormaPago
			INNER JOIN :p_IngresoEgresoGenerar AS INGEGR
				ON (INGEGR."IdOperacion" = FormaPago."FolioOperacion")
			INNER JOIN "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWDVencimiento" AS FWD
				ON (INGEGR."IdCartera"		= FWD."IdCartera" AND 
					INGEGR."FolioCartera"	= FWD."IdOperacion" AND 
					INGEGR."IdEstado"		= FWD."IdEstado" AND
					FWD."ModalidadLiquidacionValor" = 'COMPENSADA')
			INNER JOIN "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" AS MODI
				ON (MODI."IdMedioDePago" = FormaPago."IdMedioDePago")
			WHERE FormaPago."IdTipoMovimiento" = 'MOVING'
			*/
			;
		
		/*IF (IS_EMPTY(:v_TableFormaPago)) THEN
			v_TableFormaPago =
				SELECT
					 INGEGR."FolioCartera"					AS "NumeroReferencia"
					,'TRA'					 				AS "IdMedioDePago"
					,'MODDEP'								AS "IdModalidadDePago"
				    ,INGEGR."IdMonedaLiquidacion"			AS "IdMoneda"
				   	,INGEGR."MontoOperacion"				AS "Monto"
					,0 										AS "IdCuentaBancariaCorredora"
					,0 										AS "IdTarjetaBancariaCorredora"
					,0 										AS "IdChequera"
					,0 										AS "NumeroDocumentoCorredora"
					,'' 									AS "FechaDocumentoPagoCorredora"
					,0 										AS "IdCuentaBancariaSocio"
					,0 										AS "IdBancoSocio"
					,'' 									AS "NombreCuentaBancariaSocio"
					,'' 									AS "NumeroCuentaBancariaSocio"
					,'' 									AS "IdTipoCuentaBancariaSocio"
					,'' 									AS "NombreTipoCuentaBancariaSocio"
					,0 										AS "IdTarjetaBancariaSocio"
					,'' 									AS "IdTipoTarjetaBancariaSocio"
					,'' 									AS "NombreTipoTarjetaBancariaSocio"
					,'' 									AS "NumeroTarjetaBancariaSocio"
					,''										AS "NumeroSerieSocio"
					,0 										AS "NumeroDocumentoSocio"
					,'' 									AS "FechaDocumentoPagoSocio"
				FROM :p_IngresoEgresoGenerar AS INGEGR
				INNER JOIN "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWDVencimiento" AS FWD
					ON (INGEGR."IdCartera"		= FWD."IdCartera" AND 
						INGEGR."FolioCartera"	= FWD."IdOperacion" AND 
						INGEGR."IdEstado"		= FWD."IdEstado" AND
						FWD."ModalidadLiquidacionValor" = 'COMPENSADA')
				INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" POR
					ON (FWD."IdPortafolio" = POR."IdPortafolio")
				INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" indPF 
					ON (POR."IdPortafolio"			= indPF."IdPortafolio" AND
						indPF."IdTipoEntidad"		= 'PORTAFOLIO' AND 
						indPF."CodigoTipoIndicador"	= 'GEN_OCCINV' AND
						COALESCE(indPF."ValorIndicador",'N') = 'N')
			;
		END IF;*/
		
		--Insert a tablas globales de SP de Tesoreria
		INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Entrada_IngresoEgresoGenerar"
		SELECT * FROM :v_IngresoEgresoGenerar;
		INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Entrada_FormaDePago"
		SELECT * FROM :v_TableFormaPago;
		
		--Ejecucion de servicio
		IF (NOT IS_EMPTY(:v_IngresoEgresoGenerar)) THEN
			select 'Entrada_IngresoEgresoGenerar',	* from "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Entrada_IngresoEgresoGenerar";
			select 'Entrada_FormaDePago',			* from "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Entrada_FormaDePago";

			CALL "BSCL.Tesoreria.BD.Servicios::CrearIngresoEgreso" (
				p_IdEmpresa                 => :p_IdEmpresa, 
			   	p_IdUsuario                 => :p_IdUsuario, 
			    p_IdModulo                  => :p_IdModulo, 
			   	p_simularOperacion          => 'N',
			   	p_UsaReglasNegocio          => 'S',
				p_EsIngresoAutomatico       => 'S',
				p_out_InformacionContador  	=> p_out_InformacionContador,
				p_out_AdvertenciasContador  => p_out_AdvertenciasContador,
				p_out_ErroresContador       => p_out_ErroresContador
			);
		END IF;
		 
		 
select 'Salida_IngresoEgresoGenerado',	* from "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_IngresoEgresoGenerado";
select 'Salida_ResultadoProceso',		* from "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_ResultadoProceso";
select 'Salida_InformacionDetalle',		* from "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_InformacionDetalle";
select 'Salida_AdvertenciasDetalle',	* from "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_AdvertenciasDetalle";
select 'Salida_ErroresDetalle',			* from "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_ErroresDetalle";
		 
		
		--verifica errores	
		v_out_AdvertenciasDetalle =
			SELECT * FROM :v_out_AdvertenciasDetalle
			UNION ALL
			SELECT "NumeroReferencia" AS "FolioCartera", "Codigo", "RequiereAutorizacion", "Descripcion"
			FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_AdvertenciasDetalle";

		v_out_ErroresDetalle =
			SELECT * FROM :v_out_ErroresDetalle
			UNION ALL
			SELECT "NumeroReferencia" AS "FolioCartera", "Codigo", "Descripcion"
			FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_ErroresDetalle";
		
		SELECT COUNT(*) INTO v_countDetalles FROM /*:v_out_AdvertenciasDetalle,*/ :v_out_ErroresDetalle;
		IF (:v_countDetalles > 0) THEN
			SIGNAL SALIDA_CONTROLADA;
		ELSE
			p_out_IngresoEgresoGenerado =
				SELECT * FROM :p_out_IngresoEgresoGenerado
				UNION ALL
				SELECT
					 ING."IdCartera"
					,ING."FolioCartera"
					,ING."IdOperacion"
					,OUTING."NumeroReferencia" AS "NumeroIngresoEgreso"
					,OUTING."IdIngresoEgreso"
					,OUTING."IdTipoMovimiento"
				FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_IngresoEgresoGenerado" AS OUTING
				INNER JOIN :p_IngresoEgresoGenerar AS ING
					ON (OUTING."NumeroReferencia" = ING."FolioCartera");
			
			v_out_IngresoEgresoGenerado =
				SELECT
					 DISTINCT(ING."IdCartera") AS "IdCartera"
					,ING."FolioCartera"
					,ING."IdOperacion"
					,ING."NumeroIngresoEgreso"
					,ING."IdIngresoEgreso"
				FROM :p_out_IngresoEgresoGenerado ING;
			
			--select 'p_out_IngresoEgresoGenerado2', * from :p_out_IngresoEgresoGenerado;
			select 'v_out_IngresoEgresoGenerado2', * from :v_out_IngresoEgresoGenerado;
			
			p_out_AdvertenciasDetalle =
				SELECT "FolioCartera","Codigo","RequiereAutorizacion","Descripcion"
				FROM :v_out_AdvertenciasDetalle;
			
			p_out_ErroresDetalle =
				SELECT "FolioCartera","Codigo","Descripcion"
				FROM :v_out_ErroresDetalle;
			
		END IF;
        
		/****************************************************************************************************************/
		/********************************* ACTUALIZA ESTADO DE LA OPERACION A LIQUIDADA *********************************/
		/****************************************************************************************************************/
		INSERT INTO "BSCL.Mesa.BD::PAR.CambiarEstadoOperacion_IN"
		SELECT
			 ING."IdOperacion"			AS "IdOperacion"
			,'MEFW'						AS "IdTipoProducto"
		    ,'LIQ' 						AS "IdNuevoEstado"
		    ,0							AS "IdReserva"
		    ,FWD."FolioCartera"			AS "FolioCarteraCustodia"
		    --,FWD."FolioCartera"			AS "FolioOperacion"
		    ,0							AS "MontoOperacionAnticipo"
			,0							AS "MontoOperacionInicialAnticipo"
			,0							AS "MontoOperacionFinalAnticipo"
			,0							AS "TasaAnticipo"
			,0							AS "MontoOperacionMonedaAnticipo"
			,0							AS "MontoOperacionInicialMonedaAnticipo"
			,0							AS "MontoOperacionFinalMonedaAnticipo"
			,''							AS "IdMonedaLiquidacionAnticipo"
			,''							AS "IdCondicionLiquidacionAnticipo"
			,'0001-01-01'				AS "FechaCondicionLiquidacionAnticipo"
		--FROM :p_out_IngresoEgresoGenerado AS FWD
		FROM :p_IngresoEgresoGenerar AS ING
		INNER JOIN :v_out_IngresoEgresoGenerado AS FWD
			ON (ING."IdCartera" = FWD."IdCartera" AND 
				ING."FolioCartera" = FWD."FolioCartera" AND 
				ING."IdOperacion" = FWD."IdOperacion");
		
		select 'CambiarEstadoOperacion_IN', * from "BSCL.Mesa.BD::PAR.CambiarEstadoOperacion_IN";

		CALL "BSCL.Mesa.BD.Servicios::CambiarEstadoOperacion" (
		     p_IdEmpresa 		=> :p_IdEmpresa
		    ,p_IdUsuario 		=> :p_IdUsuario
		    ,p_IdModulo 		=> :p_IdModulo
		    ,p_simularOperacion => 'N'  				
		    ,p_UsaReglasNegocio => 'N'  				
		    ,p_IdPais 			=> :p_IdPais
		);
		
		SELECT COUNT(1) INTO v_countCambioEstado 
		FROM "BSCL.Mesa.BD::PAR.CambiarEstadoOperacion_OUT"
		WHERE "CodResultado" = 'ERR';
		
		--DATOS DETALLE LOG
		v_DetalleLog =
			SELECT
				'ESTADO'	    		AS "SubRegistro",
				'' 						AS "CampoRegistro",
				"FolioCarteraCustodia"  AS "ValorAntiguo",
				"IdNuevoEstado"			AS "ValorNuevo"
			FROM "BSCL.Mesa.BD::PAR.CambiarEstadoOperacion_IN";--:v_TblNuevoEstado;
		
		--GRABAR LOG
		CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
			 p_IdTipoLog		 	=> 'CAMBIOEST' 
			,p_IdModulo 			=> :p_IdModulo 
			,p_IdUsuario 			=> :p_IdUsuario 
			,p_IdEmpresa 			=> :p_IdEmpresa 
			,p_FechaSistemaLog 		=> TO_VARCHAR (:v_fechaHoraSistema, 'YYYY-MM-DD')
			,p_IdUsuarioAtendido 	=> :p_IdUsuario
			,p_IdTipoRegistro 		=> 'FORWARD' --v_TipoEntidad--'CLIENTE'
			,p_DetalleLog 			=> :v_DetalleLog	-- hacia arriba obligatorios
			,p_NombreProceso 		=> 'Cambio de Estado Forward tesoreria'-- hacia abajo opcionales
			,p_EstadoProceso 		=> 'VIG'
		);
		--LOG DE AUDITORIA (Fin)
		
		
		IF(:v_countCambioEstado > 0) THEN
			v_out_ErroresDetalle =
				SELECT
					 ING."FolioCartera"
	  			    ,RES."CodResultado" AS "Codigo"  	
                    ,RES."MsgResultado" AS "Descripcion" 		
			   FROM "BSCL.Mesa.BD::PAR.CambiarEstadoOperacion_OUT" AS RES--:p_out_TblResultado AS RES
			   INNER JOIN :p_IngresoEgresoGenerar AS ING
					   ON ING."IdOperacion" = RES."IdOperacion";
		
			SIGNAL SALIDA_CONTROLADA;
		ELSE
			/***********************************************************************************************/
			/*************************** ACTUALIZA CAMPOS EN LA CARTERA ************************************/
			/***********************************************************************************************/
			
			UPDATE CAR
			SET CAR."IdEstado"				= 'LIQ',
				--CAR."EGRFolioComprobante" = TES."IdIngresoEgreso",
				CAR."EGRFolioComprobante" = coalesce(TESS."IdIngresoEgreso",CTA."IdIngresoEgreso"),
				CAR."EGRIdTipoComprobante" = (CASE TESS."IdTipoMovimiento" WHEN  'MOVING' THEN  'TES'
																		   WHEN  'MOVEGR' THEN  'TES'
				                                            ELSE 'CTA' END), -- Modificacion para referenciar mov tes o cta
				CAR."EGRIdMonedaLiquidacion"= ING."IdMonedaLiquidacion",
				CAR."EGRMontoLiquidacion"	= (CASE WHEN CAR."EGRMontoLiquidacion" < 0 THEN (ING."MontoOperacion"*-1) 
												ELSE ING."MontoOperacion" END)
			--select 'datosActualizar', *
			FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
			--INNER JOIN :p_out_IngresoEgresoGenerado AS TES
			INNER JOIN :v_out_IngresoEgresoGenerado AS TES
				ON (CAR."IdCartera" = TES."IdCartera" AND 
					CAR."FolioCartera" = TES."FolioCartera" AND 
					CAR."IdOperacion" = TES."IdOperacion")
			INNER JOIN :p_IngresoEgresoGenerar AS ING
				ON (TES."IdCartera" = ING."IdCartera" AND 
					TES."FolioCartera" = ING."FolioCartera" AND 
					TES."IdOperacion" = ING."IdOperacion")
			LEFT JOIN :p_out_IngresoEgresoGenerado AS TESS
				ON (TESS."IdTipoMovimiento" in ('MOVING','MOVEGR'))
			LEFT JOIN :p_out_IngresoEgresoGenerado AS CTA
				ON (CTA."IdTipoMovimiento" in ('MOVCAR','MOVABN'))
			WHERE CAR."IdTipoProducto" = 'MEFW';
		
		END IF;
		
    END;
END;
