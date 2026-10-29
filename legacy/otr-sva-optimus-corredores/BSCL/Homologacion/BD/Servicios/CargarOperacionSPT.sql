
SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Homologacion.BD.Servicios::CargarOperacionSPT"; 

CREATE PROCEDURE "BSCL.Homologacion.BD.Servicios::CargarOperacionSPT"(
	-- Parametros entrada comunes en todos los servicios publicos
	 IN  p_IdEmpresa		   BIGINT,			-- ID Empresa destino
     IN  p_IdUsuario		   BIGINT,			-- ID del usuario que solicita el servicio
	 IN  p_IdCarga 			   BIGINT,          -- ID de la carga 
	 IN  p_Modulo              NVARCHAR(3),     -- modulo 
     IN  P_Modalidad           NVARCHAR(1),     -- Ingreso I incremental, 
     IN  p_RutCliente          NVARCHAR(11) default '0-0',    -- rut del cliente sebra
	 IN  p_FechaInicial        NVARCHAR(10),   --YYYY-MM-DD
	 IN  p_FechaFinal		   NVARCHAR(10),   --YYYY-MM-DD
	-- Salidas para mensajes de advertencias y errores
	OUT p_ResumenDatosProcesados Table ("CodigoProceso" nvarchar(40),"Modulo" nvarchar(3),"TotalRegistrosSebra" DECIMAL(26,8),"TotalRegistrosSap" DECIMAL(26,8)),
	OUT p_out_ErroresContador int,
	OUT p_out_ErroresDetalle table ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520))
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorcuenta INT DEFAULT 0;  
	DECLARE v_errorhashmsg table ("Codigo" NVARCHAR(40),"Valor" NVARCHAR(520));
	DECLARE v_erroroutmsg  NVARCHAR(520);	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;		 					 	
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt NVARCHAR(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL  
								   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;				
			select count(1) into p_out_ErroresContador 
			from :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log serÃ¡ captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;	
	p_out_ErroresContador		:= 0;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	BEGIN	
		-----------------------------------------------------
		--Declaraciones Variables
		----------------------------------------------------- 
	    -- declaracion variables 
	    DECLARE v_Total_Operaciones_Spot BIGINT:=0;
	    DECLARE v_Total_Spot_Cargados BIGINT :=0;

		IF p_Modulo = 'SPT'	  THEN  
		-- COMENZAMOS A SELECCIONAR LOS DATOS DE LA ASIGNACION
			INSERT INTO "BSCL.Homologacion.BD::HOM.OperacionSpot"
			("IdOperacionSpot","IdEmpresa","IdCliente","FechaOperacion","IdMonedaPrincipal","IdMonedaSecundaria","MontoMonedaPrincipal","MontoMonedaSecundaria"
			,"TipoCambioCierre","ParidadCierre","TipoCambioCosto","ParidadCosto","MontoEquivalente","MontoRentabilidad","MontoRentabilidadCanal","Margen"
			,"PuntaCompra","PuntaVenta","SolicitudTipoCambio","IdEvento","IdMesa","IdTipoDolar","TipoComprobanteFacturacion","IdComprobanteFacturacion"
			,"IdComprobanteTesoreriaIngreso","IdComprobanteTesoreriaEgreso","BenOur","EquivalenteConGasto","TipoCambioCierreConGasto","Observacion"
			,"CodigoComercio","FolioPlanilla","FolioInterfaz","Estado","IdUsuario","FechaModifica","OperacionFicticia","IdUsuarioModifica","IdPortafolio"
			,"Utilidad","MontoMonedaPrincipalEquivalente","ValorTCSpot","ResultadoMonedaLocalPuntas","ParidadAsk","ValorDolarObservado","PuntaBid"
			,"ResultadoMonedaLocalTCSpot","IdMonedaLocal","ResultadoMonedaLocalBidAsk","ParidadBid","PuntaAsk","MargenBidAsk","IdMonedaEquivalente"
			,"ResultadoMonedaLocalDO","IdDatatec","FechaLiquidacion","FechaReproceso","ResultadoMonedaLocalConsolidado","ResultadoMonedaLocalTCCierreMesa"
			,"IdInstrumento","IdTipoOperacion","TipoCambioCierreForward","IdModulo","IdTipoCanalIngreso","ParidadCierreForward","IdAgente","IdCarga"
			,"FechaIngresoRegistro","EstadoConfirmacion","FolioTransaccion","FolioOperacion","RutCliente","CuentaCliente","CodProducto","CodSubproducto"
			,"CodTipoMovimiento","CodMonPrincipal","CodMonSecu","UsuarioCreador")
			select 
					"BSCL.Mesa.BD.Secuencias::SPT.IdOperacionSpot".NEXTVAL as "IdOperacionSpot"
					, p_IdEmpresa 							as "IdEmpresa"
					, CLIENTE."IdCliente" 					as "IdCliente"
					, FMMOVTOSSPOT."FechaOperacion" 		as "FechaOperacion"
					, MONEDA_PRIMARIA."ValorDestino" 		as "IdMonedaPrincipal"
					, MONEDA_SECUNDARIA."ValorDestino" 		as "IdMonedaSecundaria"
					, FMMOVTOSSPOT."MtoMonPrinc" 			as "MontoMonedaPrincipal"
					, FMMOVTOSSPOT."MtoMonSecu" 			as "MontoMonedaSecundaria"
					, FMMOVTOSSPOT."TCCierre" 				as "TipoCambioCierre"
					, FMMOVTOSSPOT."ParidadCierreMonPrinc" 	as "ParidadCierre"-- ver sies arbitraje
					, FMMOVTOSSPOT."TCHoy"  				as "TipoCambioCosto"-- NO ESTA 
					, FMMOVTOSSPOT."ParidadHoyMonPrinc"		as "ParidadCosto"
					, FMMOVTOSSPOT."MtoMonSecu"  			as "MontoEquivalente"
					, 0 									as "MontoRentabilidad"
					, case when FMMOVTOSSPOT."Origen"  = 'DATATEC'  THEN 0
					       when FMMOVTOSSPOT."Origen"  = 'CRM' THEN  FMMOVTOSSPOT."MtoRentabilidadCRM"
					       when FMMOVTOSSPOT."Origen"  = 'Forward' THEN 0 
					       else 0
					  end 									as "MontoRentabilidadCanal"
					, FMMOVTOSSPOT."Spread" 				as "Margen" 
					, FMMOVTOSSPOT."TCRefCompra" 			as "PuntaCompra"
					, FMMOVTOSSPOT."TCRefVenta" 			as "PuntaVenta"
					, 0 									as "SolicitudTipoCambio"
					, 0 									as "IdEvento"
					, COALESCE(MesaSpotDefecto."IdMesa",0) 	as "IdMesa" 
					, 0 									as "IdTipoDolar"
					, COALESCE(TIPO_COMPROBANTE."ValorDestino",'') as "TipoComprobanteFacturacion"
					, 0 									as "IdComprobanteFacturacion"
					, 0 									as "IdComprobanteTesoreriaIngreso"
					, 0 									as "IdComprobanteTesoreriaEgreso"
					, '' 									as "BenOur"
					, 0 									as "EquivalenteConGasto"
					, 0 									as "TipoCambioCierreConGasto"
					, FMMOVTOSSPOT."Observaciones" 			as "Observacion"
					, FMMOVTOSSPOT."CodComercio" 			as "CodigoComercio"
					, 0 									as "FolioPlanilla"
					, FMMOVTOSSPOT."FolioInterfaz" 			as "FolioInterfaz"
					, ESTADO_SPOT."ValorDestino"  			as "Estado"
					, coalesce(USUARIO."IdUsuario",0) 		as "IdUsuario"
					, '0001-01-01' 							as "FechaModifica"
					, 'N' 									as "OperacionFicticia"
					, 0 									as "IdUsuarioModifica"
					, PORTAFOLIO."IdPortafolio" 			as "IdPortafolio"
					, FMMOVTOSSPOT."Spread" 				as "Utilidad"
					, 0 									as "MontoMonedaPrincipalEquivalente"
					, FMMOVTOSSPOT."TCHoy" 					as "ValorTCSpot"
					, 0 									as "ResultadoMonedaLocalPuntas" 
					, 0 									as "ParidadAsk"
					, FMMOVTOSSPOT."TCReferencia" 			as "ValorDolarObservado" -- recuperar desde bolsa
					, 0 									as "PuntaBid"
					, 0 									as "ResultadoMonedaLocalTCSpot"
					, 'CLP' 								as "IdMonedaLocal"-- moneda sistema --
					, 0 									as "ResultadoMonedaLocalBidAsk" 
					, 0 									as "ParidadBid"
					, 0 									as "PuntaAsk"
					, 0 									as "MargenBidAsk"
					, '' 									as "IdMonedaEquivalente"-- no se lo que es 
					, 0 									as "ResultadoMonedaLocalDO" 
					, 0  									as "IdDatatec"
					, '0001-01-01' 							as "FechaLiquidacion"
					, '0001-01-01' 							as "FechaReproceso"
					, 0 									as "ResultadoMonedaLocalConsolidado"
					, 0 									as "ResultadoMonedaLocalTCCierreMesa"
					, INSTRUMENTO."IdInstrumento" 			as "IdInstrumento"
					, TIPO_OPERACION_SPOT."ValorDestino" 	as "IdTipoOperacion"
					, 0 									as "TipoCambioCierreForward" -- cuando es origen fwd  
					, 'MES' 								as "IdModulo"
					, case when FMMOVTOSSPOT."Origen"  = 'DATATEC'  THEN 'DAT'
					       when FMMOVTOSSPOT."Origen"  = 'CRM' 		THEN 'AGE'
					       else 'MES'
					  end 									as "IdTipoCanalIngreso"					
					, 0 									as "ParidadCierreForward" -- arbitraje origen forward
					, COALESCE(EMPLEADO."IdEmpleado",0) 	as "IdAgente"
					, p_IdCarga 							AS "IdCarga"
					, FMMOVTOSSPOT."FechaOperacion" 		as "FechaIngresoRegistro"
					, '' 									as "EstadoConfirmacion"
					,FMMOVTOSSPOT."FolioTransaccion" 		as "FolioTransaccion"
					,FMMOVTOSSPOT."FolioOperacion" 			as "FolioOperacion"
					,FMMOVTOSSPOT."RutCliente" 				as "RutCliente"
					,FMMOVTOSSPOT."CuentaCliente" 			as "CuentaCliente"
					,FMMOVTOSSPOT."CodProducto" 			as "CodProducto"
					,FMMOVTOSSPOT."CodSubProducto" 			as "CodSubproducto"
					,FMMOVTOSSPOT."CodTipoMovto" 			as "CodTipoMovimiento"
					,FMMOVTOSSPOT."CodMonPrinc" 			as "CodMonPrincipal"
					,FMMOVTOSSPOT."CodMonSecu" 				as "CodMonSecu"
					,FMMOVTOSSPOT."UsuarioCreador" 			as "UsuarioCreador"
					
			FROM "BSCL_VIRTUAL"."GES_FMMOVTOSSPOT"			AS FMMOVTOSSPOT
				INNER JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE
				         	ON upper(trim(CLIENTE."DocumentoIdentidad")) = CASE WHEN p_RutCliente = '        0-0' THEN upper(trim(FMMOVTOSSPOT."RutCliente")) ELSE upper(TRIM(p_RutCliente)) end
    			LEFT  JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio 
				            ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
				            AND PORTAFOLIO."IdCuentaOrigen" =  TRIM(TO_VARCHAR(FMMOVTOSSPOT."CuentaCliente")) 
				LEFT  JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS "NEMO_INSTRUMENTO_SPOT" 		
							ON  "NEMO_INSTRUMENTO_SPOT"."TipoValor" = 'TipoInstrumentoSpot'
						    AND "NEMO_INSTRUMENTO_SPOT"."ValorOrigen" = FMMOVTOSSPOT."CodProducto"
						    AND "NEMO_INSTRUMENTO_SPOT"."ValorOrigen2" = FMMOVTOSSPOT."CodSubProducto"
				LEFT  JOIN "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" AS INSTRUMENTO
							ON INSTRUMENTO."Nemotecnico" = "NEMO_INSTRUMENTO_SPOT"."ValorDestino"
				LEFT  JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS MONEDA_PRIMARIA
							ON MONEDA_PRIMARIA."TipoValor" = 'MonedaDivisas'
							AND MONEDA_PRIMARIA."ValorOrigen" = FMMOVTOSSPOT."CodMonPrinc"
	            LEFT  JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS MONEDA_SECUNDARIA
							ON MONEDA_SECUNDARIA."TipoValor" = 'MonedaDivisas'
							AND MONEDA_SECUNDARIA."ValorOrigen" = FMMOVTOSSPOT."CodMonSecu"
				LEFT  JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS TIPO_OPERACION_SPOT
							ON TIPO_OPERACION_SPOT."TipoValor" =  'TipoOperacionSpot'
							AND TIPO_OPERACION_SPOT."ValorOrigen" = FMMOVTOSSPOT."CodTipoMovto"
				LEFT  JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS ESTADO_SPOT
							ON ESTADO_SPOT."TipoValor" = 'EstadoSpot'
							AND ESTADO_SPOT."ValorOrigen" = FMMOVTOSSPOT."Estado" 
				LEFT JOIN   "BSCL"."BSCL.Mesa.BD::MES.Mesa" as MesaSpotDefecto 
				            ON MesaSpotDefecto."IdTipoProducto" = 'MEME'
				            AND MesaSpotDefecto."PorDefecto" = 'S' 
			 	LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS TIPO_COMPROBANTE
							ON  TIPO_COMPROBANTE."TipoValor" ='TipoComprobante'
							AND TIPO_COMPROBANTE."ValorOrigen" = FMMOVTOSSPOT."TipoCompPago"
				LEFT JOIN "BSCL"."BSCL.SocioNegocio.BD::EMP.Empleado" AS EMPLEADO
				           ON trim(EMPLEADO."CodigoAgenteMigracion") =  trim(FMMOVTOSSPOT."CodAgente")
				LEFT JOIN BSCL."BSCL.Plataforma.BD::ACC.Usuario" AS USUARIO
				           ON trim(USUARIO."CodigoUsuario") =  trim(FMMOVTOSSPOT."UsuarioCreador")
				LEFT JOIN "BSCL.Homologacion.BD::HOM.OperacionSpot" AS HOM_OPERACION_SPOT
							ON HOM_OPERACION_SPOT."FolioOperacion"    = FMMOVTOSSPOT."FolioOperacion"  
							AND HOM_OPERACION_SPOT."FolioTransaccion" = FMMOVTOSSPOT."FolioTransaccion" 		

				WHERE FMMOVTOSSPOT."FechaOperacion" >= p_FechaInicial
				AND  FMMOVTOSSPOT."Estado" IN ('L','V')
			    AND  FMMOVTOSSPOT."FechaOperacion" <= p_FechaFinal	           
				AND  HOM_OPERACION_SPOT."FolioOperacion" IS NULL;
				

           		--------------------------------------------------------------------------------
           		----- MONEDA PRINCIPAL														---
           		--------------------------------------------------------------------------------	   
		   		insert into BSCL."BSCL.Homologacion.BD::HOM.ComprobantePagoOperacion"
				   ("IdComprobantePagoOperacion","IdEmpresa","IdOperacion","IdIngresoEgreso"
					,"IdAbono","IdCargo","IdTipoProducto","IdFormaDePago","RecibePaga"
					,"IdModalidadPago","IdMoneda","Monto"
					,"FechaIngresoComprobantePagoOperacion"
					,"FechaLiquidacionComprobantePagoOperacion"
					,"IdEstado"
					,"AfectarTesoreria"
					,"IdIngresoEgresoAnticipo"
					,"IdAbonoAnticipo"
					,"IdCargoAnticipo"
					,"IdCarga" 
					,"FolioOperacion"
					,"FolioTransaccion"
					)
					    	
					
			SELECT
			 BSCL."BSCL.Mesa.BD.Secuencias::IdComprobantePagoOperacion".NEXTVAL	AS 	"IdComprobantePagoOperacion"
			,p_IdEmpresa											AS 	"IdEmpresa"
			,COALESCE(Spt."IdOperacionSpot",0)						AS	"IdOperacion"
			,0														AS 	"IdIngresoEgreso"
			,0														AS 	"IdAbono"
			,0														AS 	"IdCargo"
			,'MEME'													AS	"IdTipoProducto"
			,CASE WHEN TBpago."TIPO_OPER" = 'CO' then 	--VENSPT_TRA_USD_1		
			      CASE 	WHEN TBpago."FORMA_RECIBO" = 'TH' THEN CONCAT('COMSPT_TRA_',TRIM( COALESCE(Spt."IdMonedaPrincipal" || '_0','CLP_0' )))
			      		WHEN TBpago."FORMA_RECIBO" = 'CC' THEN CONCAT('COMSPT_CIN_',TRIM( COALESCE(Spt."IdMonedaPrincipal",'CLP_0' )))
			      		WHEN TBpago."FORMA_RECIBO" = 'TF' THEN CONCAT('COMSPT_TRA_',TRIM( COALESCE(Spt."IdMonedaPrincipal",'CLP_0' )))
			      		ELSE  							       CONCAT('COMSPT_TRA_',TRIM( COALESCE(Spt."IdMonedaPrincipal" || '_1','CLP_1' )))
			      END
			  ELSE
			      CASE 	WHEN TBpago."FORMA_PAGO" = 'TH' THEN CONCAT('VENSPT_TRA_',TRIM( COALESCE(Spt."IdMonedaPrincipal",'CLP_0' )))
			      		WHEN TBpago."FORMA_PAGO" = 'AC' THEN CONCAT('VENSPT_CIN_',TRIM( COALESCE(Spt."IdMonedaPrincipal",'CLP_0' )))
			      		WHEN TBpago."FORMA_PAGO" = 'TF' THEN CONCAT('VENSPT_TRA_',TRIM( COALESCE(Spt."IdMonedaPrincipal",'CLP_0' )))
			      		ELSE  							     CONCAT('VENSPT_TRA_',TRIM( COALESCE(Spt."IdMonedaPrincipal" || '_1','CLP_1' )))
			      END
			  END													AS "IdFormaDePago"
			,CASE WHEN TBpago."TIPO_OPER" = 'VE' then 'P' 
				  ELSE 'R' 
			 END													AS 	"RecibePaga"
			,'MODCAJ'												AS	"IdModalidadPago"
			,COALESCE(Spt."IdMonedaPrincipal",'CLP')				AS 	"IdMoneda"	
		    ,Spt."MontoMonedaPrincipal"								AS	"Monto"
			,TBpago."FEC_MVTO" 										AS	"FechaIngresoComprobantePagoOperacion"
			,TBpago."FEC_MVTO" 										AS 	"FechaLiquidacionComprobantePagoOperacion"
			,COALESCE(Estados."ValorDestino",'PEN')					AS	"IdEstado"
			,CASE WHEN TBpago."TIPO_OPER" = 'CO' then 			
			      CASE 	WHEN TBpago."FORMA_RECIBO" = 'CC' THEN 'N' ELSE 'S' END
		  	 ELSE
			      CASE 	WHEN TBpago."FORMA_PAGO" = 'AC' THEN 'N'   ELSE 'S' END
		    END													AS 	"AfectarTesoreria"
			,0														AS 	"IdIngresoEgresoAnticipo"
			,0														AS 	"IdCargoAnticipo"
			,0														AS 	"IdAbonoAnticipo"
			,p_IdCarga												AS  "IdCarga" 
			,Spt."FolioOperacion"									AS  "FolioOperacion" 
			,Spt."FolioTransaccion"									AS  "FolioTransaccion" 
			

			FROM  "BSCL_VIRTUAL"."GES_PASO_TBMOMVTO" AS TBpago
			
					INNER JOIN "BSCL.Homologacion.BD::HOM.OperacionSpot"  AS Spt
	  					   ON Spt."FolioOperacion"    = TBpago."SEC_MOVTO"

					LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" Estados
						   ON trim(Estados."TipoValor") = 'EstadoSpot'
						  AND Estados."ValorOrigen"	=	TBpago."ESTADO"

					  
             WHERE TRIM(TBpago."FEC_MVTO") BETWEEN TRIM(p_FechaInicial) AND TRIM(p_FechaFinal);
             
             
          	--------------------------------------------------------------------------------
           	----- MONEDA SECUNDARIA														---
           	--------------------------------------------------------------------------------	   
		   	insert into BSCL."BSCL.Homologacion.BD::HOM.ComprobantePagoOperacion"
				   ("IdComprobantePagoOperacion","IdEmpresa","IdOperacion","IdIngresoEgreso"
					,"IdAbono","IdCargo","IdTipoProducto","IdFormaDePago","RecibePaga"
					,"IdModalidadPago","IdMoneda"
					,"Monto"
					,"FechaIngresoComprobantePagoOperacion"
					,"FechaLiquidacionComprobantePagoOperacion"
					,"IdEstado"
					,"AfectarTesoreria"
					,"IdIngresoEgresoAnticipo"
					,"IdAbonoAnticipo"
					,"IdCargoAnticipo"
					,"IdCarga" 
					,"FolioOperacion"
					,"FolioTransaccion"
					)
					
			SELECT
			 BSCL."BSCL.Mesa.BD.Secuencias::IdComprobantePagoOperacion".NEXTVAL	AS 	"IdComprobantePagoOperacion"
			,p_IdEmpresa											AS 	"IdEmpresa"
			,COALESCE(Spt."IdOperacionSpot",0)						AS	"IdOperacion"
			,0														AS 	"IdIngresoEgreso"
			,0														AS 	"IdAbono"
			,0														AS 	"IdCargo"
			,'MEME'													AS	"IdTipoProducto"
			 ,CASE WHEN TBpago."TIPO_OPER" = 'VE' then 
			      CASE 	WHEN TBpago."FORMA_PAGO" = 'TH' THEN CONCAT('COMSPT_TRA_',TRIM( COALESCE(Spt."IdMonedaSecundaria",'CLP_0' )))
			      		WHEN TBpago."FORMA_PAGO" = 'CC' THEN CONCAT('COMSPT_CIN_',TRIM( COALESCE(Spt."IdMonedaSecundaria",'CLP_0' )))
			      		WHEN TBpago."FORMA_PAGO" = 'TF' THEN CONCAT('COMSPT_TRA_',TRIM( COALESCE(Spt."IdMonedaSecundaria",'CLP_0' )))
			      		ELSE  							     CONCAT('COMSPT_TRA_',TRIM( COALESCE(Spt."IdMonedaSecundaria" || '_1','CLP_1' )))
			      END
			  ELSE
			      CASE 	WHEN TBpago."FORMA_RECIBO" = 'TH' THEN CONCAT('VENSPT_TRA_',TRIM( COALESCE(Spt."IdMonedaSecundaria" || '_0','CLP_0' )))
			      		WHEN TBpago."FORMA_RECIBO" = 'AC' THEN CONCAT('VENSPT_CIN_',TRIM( COALESCE(Spt."IdMonedaSecundaria",'CLP_0' )))
			      		WHEN TBpago."FORMA_RECIBO" = 'TF' THEN CONCAT('VENSPT_TRA_',TRIM( COALESCE(Spt."IdMonedaSecundaria",'CLP_0' )))
			      		ELSE  							       CONCAT('VENSPT_TRA_',TRIM( COALESCE(Spt."IdMonedaSecundaria" || '_1','CLP_1' )))
			      END
			  END													AS "IdFormaDePago"
			 
			,CASE WHEN TBpago."TIPO_OPER" = 'VE' then 'R' 
				  ELSE 'P' 
			 END													AS 	"RecibePaga"
			,'MODCAJ'												AS	"IdModalidadPago"
			,COALESCE(Spt."IdMonedaSecundaria",'CLP')				AS 	"IdMoneda"	
		    ,Spt."MontoMonedaSecundaria"							AS	"Monto"
			,TBpago."FEC_MVTO" 										AS	"FechaIngresoComprobantePagoOperacion"
			,TBpago."FEC_MVTO" 										AS 	"FechaLiquidacionComprobantePagoOperacion"
			,COALESCE(Estados."ValorDestino",'PEN')					AS	"IdEstado"
			,CASE WHEN TBpago."TIPO_OPER" = 'CO' then 			
			      CASE 	WHEN TBpago."FORMA_RECIBO" = 'CC' THEN 'N' ELSE 'S' END
		  	 ELSE
			      CASE 	WHEN TBpago."FORMA_PAGO" = 'AC' THEN 'N'   ELSE 'S' END
		    END													AS 	"AfectarTesoreria"
			,0														AS 	"IdIngresoEgresoAnticipo"
			,0														AS 	"IdCargoAnticipo"
			,0														AS 	"IdAbonoAnticipo"
			,p_IdCarga												AS  "IdCarga" 
			,Spt."FolioOperacion"									AS  "FolioOperacion" 
			,Spt."FolioTransaccion"									AS  "FolioTransaccion" 
			
			FROM  "BSCL_VIRTUAL"."GES_PASO_TBMOMVTO" AS TBpago

					INNER JOIN "BSCL.Homologacion.BD::HOM.OperacionSpot"  AS Spt
	  					   ON Spt."FolioOperacion"    = TBpago."SEC_MOVTO"

					LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" Estados
						   ON trim(Estados."TipoValor") = 'EstadoSpot'
						  AND Estados."ValorOrigen"	=	TBpago."ESTADO"

		
              WHERE TRIM(TBpago."FEC_MVTO") BETWEEN TRIM(p_FechaInicial) AND TRIM(p_FechaFinal);				
			--------------------------------------------------------------------------------------------              						               
			--------------------------------------------------------------------------------------------              						               
			--------------------------------------------------------------------------------------------
			
              
             --REVISAMOS CUANTAS OPERACIONES SERAN CARGADAS
                  SELECT COUNT(*) INTO v_Total_Operaciones_Spot
                  FROM "BSCL_VIRTUAL"."GES_FMMOVTOSSPOT" as FMMOVTOSSPOT	
                  WHERE FMMOVTOSSPOT."FechaOperacion" >= p_FechaInicial
		        	AND FMMOVTOSSPOT."FechaOperacion" <= p_FechaFinal;

			-- REVISAMOS CUANTOS REGISTROS FUERON CARGADOS
                  SELECT COUNT(*) INTO v_Total_Spot_Cargados FROM 
                  "BSCL.Homologacion.BD::HOM.OperacionSpot" 
                  WHERE "IdCarga" = p_IdCarga;
 
                  
             -- SACAMOS LOS REGISTROS PROCESADOS E INGRESADOS EN LA ASIGNACION
	   			  p_ResumenDatosProcesados =  select 'CARGAOPERACIONSPOT' as "CodigoProceso" 
	   			  									,'ORD' as "Modulo" 
	   			 									,v_Total_Operaciones_Spot as "TotalRegistrosSebra"
	   			  									,v_Total_Spot_Cargados as "TotalRegistrosSap"  FROM DUMMY
       							    		   union all
       							    		   select * from :p_ResumenDatosProcesados;

                  IF v_Total_Operaciones_Spot <> v_Total_Spot_Cargados THEN 
				     p_out_ErroresDetalle =  select 'ORD_CARGA_ERR_SPT' as "Codigo",'falla en la carga spot' as "Descripcion" from dummy UNION ALL
					    	       			 select * from :p_out_ErroresDetalle;	
	   			  END IF;
				
ELSE
            -- insertar logs
			 -- el cliente debe existir
			 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'SPOT' as "Entidad"
                    ,'No existe el cliente EN OPTIMUS' as "Mensaje"
                    , 'RutCliente' as "NombreCampoGenerico1"
                    , to_varchar(FMMOVTOSSPOT."RutCliente") as "ValorCampoGenerico1"
                    , '' as "NombreCampoGenerico2"
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'' as "NombreCampoGenerico6"
                    , 0 as "ValorCampoGenerico6"
                     ,'FolioOperacion' as "NombreCampoGenerico7"
                    , FMMOVTOSSPOT."FolioOperacion" as "ValorCampoGenerico7"
                    ,'FolioTransaccion' as "NombreCampoGenerico8"
                    , FMMOVTOSSPOT."FolioTransaccion" as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              FROM "BSCL_VIRTUAL"."GES_FMMOVTOSSPOT"	AS FMMOVTOSSPOT
				LEFT JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE
			         	ON upper(trim(CLIENTE."DocumentoIdentidad")) = CASE WHEN p_RutCliente = '        0-0' THEN upper(trim(FMMOVTOSSPOT."RutCliente")) ELSE upper(TRIM(p_RutCliente)) end
			    LEFT JOIN "BSCL.Homologacion.BD::HOM.OperacionSpot" AS HOM_OPERACION_SPOT
						ON HOM_OPERACION_SPOT."FolioOperacion"    = FMMOVTOSSPOT."FolioOperacion"  
						AND HOM_OPERACION_SPOT."FolioTransaccion" = FMMOVTOSSPOT."FolioTransaccion" 
			  WHERE FMMOVTOSSPOT."FechaOperacion" >= p_FechaInicial
		        AND FMMOVTOSSPOT."FechaOperacion" <= p_FechaFinal
		        AND CLIENTE."DocumentoIdentidad" IS NULL
		        AND HOM_OPERACION_SPOT."FolioOperacion" IS NULL;
		        
			 -- el portafolio debe existir
			 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'SPOT' as "Entidad"
                    ,'No existe Portafolio cliente EN OPTIMUS' as "Mensaje"
                    , 'RutCliente' as "NombreCampoGenerico1"
                    , to_varchar(FMMOVTOSSPOT."RutCliente") as "ValorCampoGenerico1"
                    , 'CuentaCliente' as "NombreCampoGenerico2"
                    , TO_VARCHAR(FMMOVTOSSPOT."CuentaCliente") as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    , '' as "NombreCampoGenerico6"
                    , 0  as "ValorCampoGenerico6"
                     ,'FolioOperacion' as "NombreCampoGenerico7"
                    , FMMOVTOSSPOT."FolioOperacion" as "ValorCampoGenerico7"
                    ,'FolioTransaccion' as "NombreCampoGenerico8"
                    , FMMOVTOSSPOT."FolioTransaccion" as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              FROM "BSCL_VIRTUAL"."GES_FMMOVTOSSPOT"	AS FMMOVTOSSPOT
				INNER JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE
			         	ON upper(trim(CLIENTE."DocumentoIdentidad")) = CASE WHEN p_RutCliente = '        0-0' THEN upper(trim(FMMOVTOSSPOT."RutCliente")) ELSE upper(TRIM(p_RutCliente)) end
	         	LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio"  as PORTAFOLIO
			            ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
			            AND PORTAFOLIO."IdCuentaOrigen" =  TRIM(TO_VARCHAR(FMMOVTOSSPOT."CuentaCliente"))
			    LEFT JOIN "BSCL.Homologacion.BD::HOM.OperacionSpot" AS HOM_OPERACION_SPOT
						ON HOM_OPERACION_SPOT."FolioOperacion"    = FMMOVTOSSPOT."FolioOperacion"  
						AND HOM_OPERACION_SPOT."FolioTransaccion" = FMMOVTOSSPOT."FolioTransaccion" 
			  WHERE FMMOVTOSSPOT."FechaOperacion" >= p_FechaInicial
		        AND FMMOVTOSSPOT."FechaOperacion" <= p_FechaFinal
		        AND PORTAFOLIO."IdCliente" IS NULL
		        AND HOM_OPERACION_SPOT."FolioOperacion" IS NULL;
		      		        
		     -- el instrumento spot debe estar homologado
			 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'SPOT' as "Entidad"
                    ,'El Instrumento spot no se encuentra HOMOLOGADO' as "Mensaje"
                    , 'TipoValor' as "NombreCampoGenerico1"
                    , 'TipoInstrumentoSpot' as "ValorCampoGenerico1"
                    , '' as "NombreCampoGenerico2"
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , 'CodProducto' as "NombreCampoGenerico4"
                    , TO_VARCHAR(FMMOVTOSSPOT."CodProducto") as "ValorCampoGenerico4"
                    ,'CodSubProducto' as "NombreCampoGenerico5"
                    , TO_VARCHAR(FMMOVTOSSPOT."CodSubProducto") as "ValorCampoGenerico5"
                    , '' as "NombreCampoGenerico6"
                    , 0 as "ValorCampoGenerico6"
                     ,'FolioOperacion' as "NombreCampoGenerico7"
                    , FMMOVTOSSPOT."FolioOperacion" as "ValorCampoGenerico7"
                    ,'FolioTransaccion' as "NombreCampoGenerico8"
                    , FMMOVTOSSPOT."FolioTransaccion" as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              FROM "BSCL_VIRTUAL"."GES_FMMOVTOSSPOT"	AS FMMOVTOSSPOT
				INNER JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE
			         	ON upper(trim(CLIENTE."DocumentoIdentidad")) = CASE WHEN p_RutCliente = '        0-0' THEN upper(trim(FMMOVTOSSPOT."RutCliente")) ELSE upper(TRIM(p_RutCliente)) end
			    INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" as PORTAFOLIO
			            ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
			            AND PORTAFOLIO."IdCuentaOrigen" =  TRIM(TO_VARCHAR(FMMOVTOSSPOT."CuentaCliente"))
			    LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS "NEMO_INSTRUMENTO_SPOT" 		
						ON  "NEMO_INSTRUMENTO_SPOT"."TipoValor" = 'TipoInstrumentoSpot'
					    AND "NEMO_INSTRUMENTO_SPOT"."ValorOrigen" = FMMOVTOSSPOT."CodProducto"
					    AND "NEMO_INSTRUMENTO_SPOT"."ValorOrigen2" = FMMOVTOSSPOT."CodSubProducto"
				LEFT JOIN "BSCL.Homologacion.BD::HOM.OperacionSpot" AS HOM_OPERACION_SPOT
						ON HOM_OPERACION_SPOT."FolioOperacion"    = FMMOVTOSSPOT."FolioOperacion"  
						AND HOM_OPERACION_SPOT."FolioTransaccion" = FMMOVTOSSPOT."FolioTransaccion" 
			  WHERE FMMOVTOSSPOT."FechaOperacion" >= p_FechaInicial
		        AND FMMOVTOSSPOT."FechaOperacion" <= p_FechaFinal
		        AND "NEMO_INSTRUMENTO_SPOT"."TipoValor" IS NULL
		        AND HOM_OPERACION_SPOT."FolioOperacion" IS NULL;  
		        	
			-- el instrumento debe existir en optimus
			insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
            select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'SPOT' as "Entidad"
                    ,'El Instrumento spot no se encuentra en tabla instrumento optimus' as "Mensaje"
                    , 'Nemotecnico' as "NombreCampoGenerico1"
                    , "NEMO_INSTRUMENTO_SPOT"."ValorDestino" as "ValorCampoGenerico1"
                    , '' as "NombreCampoGenerico2"
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    , '' as "NombreCampoGenerico6"
                    , 0 as "ValorCampoGenerico6"
                     ,'FolioOperacion' as "NombreCampoGenerico7"
                    , FMMOVTOSSPOT."FolioOperacion" as "ValorCampoGenerico7"
                    ,'FolioTransaccion' as "NombreCampoGenerico8"
                    , FMMOVTOSSPOT."FolioTransaccion" as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              FROM "BSCL_VIRTUAL"."GES_FMMOVTOSSPOT"	AS FMMOVTOSSPOT
				INNER JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE
			         	ON upper(trim(CLIENTE."DocumentoIdentidad")) = CASE WHEN p_RutCliente = '        0-0' THEN upper(trim(FMMOVTOSSPOT."RutCliente")) ELSE upper(TRIM(p_RutCliente)) end
			    INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" as PORTAFOLIO
			            ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
			            AND PORTAFOLIO."IdCuentaOrigen" =  TRIM(TO_VARCHAR(FMMOVTOSSPOT."CuentaCliente"))
			    INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS "NEMO_INSTRUMENTO_SPOT" 		
						ON  "NEMO_INSTRUMENTO_SPOT"."TipoValor" = 'TipoInstrumentoSpot'
					    AND "NEMO_INSTRUMENTO_SPOT"."ValorOrigen" = FMMOVTOSSPOT."CodProducto"
					    AND "NEMO_INSTRUMENTO_SPOT"."ValorOrigen2" = FMMOVTOSSPOT."CodSubProducto"
				LEFT JOIN "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" AS INSTRUMENTO
						ON INSTRUMENTO."Nemotecnico" = "NEMO_INSTRUMENTO_SPOT"."ValorDestino"
				LEFT JOIN "BSCL.Homologacion.BD::HOM.OperacionSpot" AS HOM_OPERACION_SPOT
						ON HOM_OPERACION_SPOT."FolioOperacion"    = FMMOVTOSSPOT."FolioOperacion"  
						AND HOM_OPERACION_SPOT."FolioTransaccion" = FMMOVTOSSPOT."FolioTransaccion" 
			  WHERE FMMOVTOSSPOT."FechaOperacion" >= p_FechaInicial
		        AND FMMOVTOSSPOT."FechaOperacion" <= p_FechaFinal
		        AND INSTRUMENTO."Nemotecnico" IS NULL
		        AND HOM_OPERACION_SPOT."FolioOperacion" IS NULL;  
			
			-- la moneda primaria no esta en la tabla de moneda
		    insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
            select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'SPOT' as "Entidad"
                    ,'la moneda principal no se encuentra Homologada en OPTIMUS' as "Mensaje"
                    , 'CodMonPrinc' as "NombreCampoGenerico1"
                    , TO_VARCHAR(FMMOVTOSSPOT."CodMonPrinc") as "ValorCampoGenerico1"
                    , 'TipoValor' as "NombreCampoGenerico2"
                    , 'MonedaDivisas' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    , '' as "NombreCampoGenerico6"
                    , 0 as "ValorCampoGenerico6"
                    ,'FolioOperacion' as "NombreCampoGenerico7"
                    , FMMOVTOSSPOT."FolioOperacion" as "ValorCampoGenerico7"
                    ,'FolioTransaccion' as "NombreCampoGenerico8"
                    , FMMOVTOSSPOT."FolioTransaccion" as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              FROM "BSCL_VIRTUAL"."GES_FMMOVTOSSPOT"	AS FMMOVTOSSPOT
				INNER JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE
			         	ON upper(trim(CLIENTE."DocumentoIdentidad")) = CASE WHEN p_RutCliente = '        0-0' THEN upper(trim(FMMOVTOSSPOT."RutCliente")) ELSE upper(TRIM(p_RutCliente)) end
			    INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" as PORTAFOLIO
			            ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
			            AND PORTAFOLIO."IdCuentaOrigen" =  TRIM(TO_VARCHAR(FMMOVTOSSPOT."CuentaCliente"))
			    INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS "NEMO_INSTRUMENTO_SPOT" 		
						ON  "NEMO_INSTRUMENTO_SPOT"."TipoValor" = 'TipoInstrumentoSpot'
					    AND "NEMO_INSTRUMENTO_SPOT"."ValorOrigen" = FMMOVTOSSPOT."CodProducto"
					    AND "NEMO_INSTRUMENTO_SPOT"."ValorOrigen2" = FMMOVTOSSPOT."CodSubProducto"
				INNER JOIN "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" AS INSTRUMENTO
						ON INSTRUMENTO."Nemotecnico" = "NEMO_INSTRUMENTO_SPOT"."ValorDestino"
				LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS MONEDA_PRIMARIA
						ON MONEDA_PRIMARIA."TipoValor" = 'MonedaDivisas'
						AND MONEDA_PRIMARIA."ValorOrigen" = FMMOVTOSSPOT."CodMonPrinc"
				LEFT JOIN "BSCL.Homologacion.BD::HOM.OperacionSpot" AS HOM_OPERACION_SPOT
						ON HOM_OPERACION_SPOT."FolioOperacion"    = FMMOVTOSSPOT."FolioOperacion"  
						AND HOM_OPERACION_SPOT."FolioTransaccion" = FMMOVTOSSPOT."FolioTransaccion" 
			  WHERE FMMOVTOSSPOT."FechaOperacion" >= p_FechaInicial
		        AND FMMOVTOSSPOT."FechaOperacion" <= p_FechaFinal
		        AND MONEDA_PRIMARIA."IdHomologacion" IS NULL
		        AND HOM_OPERACION_SPOT."FolioOperacion" IS NULL;  

		   -- la moneda Secundaria no esta homologada
			insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
            select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'SPOT' as "Entidad"
                    ,'la moneda Secundaria no se encuentra Homologada en OPTIMUS' as "Mensaje"
                    , 'CodMonSecu' as "NombreCampoGenerico1"
                    , TO_VARCHAR(FMMOVTOSSPOT."CodMonSecu") as "ValorCampoGenerico1"
                    , 'TipoValor' as "NombreCampoGenerico2"
                    , 'MonedaDivisas' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    , '' as "NombreCampoGenerico6"
                    , 0 as "ValorCampoGenerico6"
                     ,'FolioOperacion' as "NombreCampoGenerico7"
                    , FMMOVTOSSPOT."FolioOperacion" as "ValorCampoGenerico7"
                    ,'FolioTransaccion' as "NombreCampoGenerico8"
                    , FMMOVTOSSPOT."FolioTransaccion" as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              FROM "BSCL_VIRTUAL"."GES_FMMOVTOSSPOT"	AS FMMOVTOSSPOT
				INNER JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE
			         	ON upper(trim(CLIENTE."DocumentoIdentidad")) = CASE WHEN p_RutCliente = '        0-0' THEN upper(trim(FMMOVTOSSPOT."RutCliente")) ELSE upper(TRIM(p_RutCliente)) end
			    INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" as PORTAFOLIO
			            ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
			            AND PORTAFOLIO."IdCuentaOrigen" =  TRIM(TO_VARCHAR(FMMOVTOSSPOT."CuentaCliente"))
			    INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS "NEMO_INSTRUMENTO_SPOT" 		
						ON  "NEMO_INSTRUMENTO_SPOT"."TipoValor" = 'TipoInstrumentoSpot'
					    AND "NEMO_INSTRUMENTO_SPOT"."ValorOrigen" = FMMOVTOSSPOT."CodProducto"
					    AND "NEMO_INSTRUMENTO_SPOT"."ValorOrigen2" = FMMOVTOSSPOT."CodSubProducto"
				INNER JOIN "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" AS INSTRUMENTO
						ON INSTRUMENTO."Nemotecnico" = "NEMO_INSTRUMENTO_SPOT"."ValorDestino"
				INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS MONEDA_PRIMARIA
						ON MONEDA_PRIMARIA."TipoValor" = 'MonedaDivisas'
						AND MONEDA_PRIMARIA."ValorOrigen" = FMMOVTOSSPOT."CodMonPrinc"
                LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS MONEDA_SECUNDARIA
						ON MONEDA_SECUNDARIA."TipoValor" = 'MonedaDivisas'
						AND MONEDA_SECUNDARIA."ValorOrigen" = FMMOVTOSSPOT."CodMonSecu"
				LEFT JOIN "BSCL.Homologacion.BD::HOM.OperacionSpot" AS HOM_OPERACION_SPOT
						ON HOM_OPERACION_SPOT."FolioOperacion"    = FMMOVTOSSPOT."FolioOperacion"  
						AND HOM_OPERACION_SPOT."FolioTransaccion" = FMMOVTOSSPOT."FolioTransaccion" 
			  	WHERE FMMOVTOSSPOT."FechaOperacion" >= p_FechaInicial
		        	AND FMMOVTOSSPOT."FechaOperacion" <= p_FechaFinal
		        	AND MONEDA_SECUNDARIA."IdHomologacion" IS NULL
		        	AND HOM_OPERACION_SPOT."FolioOperacion" IS NULL;

		   -- la operacion optimus no se encuentra homologada en optimus
			insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
            select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'SPOT' as "Entidad"
                    ,'El tipo operacion no se encuentra homologada en OPTIMUS' as "Mensaje"
                    , 'CodTipoMovto' as "NombreCampoGenerico1"
                    , TO_VARCHAR(FMMOVTOSSPOT."CodTipoMovto") as "ValorCampoGenerico1"
                    , 'TipoValor' as "NombreCampoGenerico2"
                    , 'TipoOperacionSpot' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    , '' as "NombreCampoGenerico6"
                    , 0 as "ValorCampoGenerico6"
                     ,'FolioOperacion' as "NombreCampoGenerico7"
                    , FMMOVTOSSPOT."FolioOperacion" as "ValorCampoGenerico7"
                    ,'FolioTransaccion' as "NombreCampoGenerico8"
                    , FMMOVTOSSPOT."FolioTransaccion" as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              FROM "BSCL_VIRTUAL"."GES_FMMOVTOSSPOT"	AS FMMOVTOSSPOT
				INNER JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE
			         	ON upper(trim(CLIENTE."DocumentoIdentidad")) = CASE WHEN p_RutCliente = '        0-0' THEN upper(trim(FMMOVTOSSPOT."RutCliente")) ELSE upper(TRIM(p_RutCliente)) end
			    INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" as PORTAFOLIO
			            ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
			            AND PORTAFOLIO."IdCuentaOrigen" =  TRIM(TO_VARCHAR(FMMOVTOSSPOT."CuentaCliente"))
			    INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS "NEMO_INSTRUMENTO_SPOT" 		
						ON  "NEMO_INSTRUMENTO_SPOT"."TipoValor" = 'TipoInstrumentoSpot'
					    AND "NEMO_INSTRUMENTO_SPOT"."ValorOrigen" = FMMOVTOSSPOT."CodProducto"
					    AND "NEMO_INSTRUMENTO_SPOT"."ValorOrigen2" = FMMOVTOSSPOT."CodSubProducto"
				INNER JOIN "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" AS INSTRUMENTO
						ON INSTRUMENTO."Nemotecnico" = "NEMO_INSTRUMENTO_SPOT"."ValorDestino"
				INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS MONEDA_PRIMARIA
						ON MONEDA_PRIMARIA."TipoValor" = 'MonedaDivisas'
						AND MONEDA_PRIMARIA."ValorOrigen" = FMMOVTOSSPOT."CodMonPrinc"
                INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS MONEDA_SECUNDARIA
						ON MONEDA_SECUNDARIA."TipoValor" = 'MonedaDivisas'
						AND MONEDA_SECUNDARIA."ValorOrigen" = FMMOVTOSSPOT."CodMonSecu"
				LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS TIPO_OPERACION_SPOT
						ON TIPO_OPERACION_SPOT."TipoValor" =  'TipoOperacionSpot'
						AND TIPO_OPERACION_SPOT."ValorOrigen" = FMMOVTOSSPOT."CodTipoMovto"
				LEFT JOIN "BSCL.Homologacion.BD::HOM.OperacionSpot" AS HOM_OPERACION_SPOT
						ON HOM_OPERACION_SPOT."FolioOperacion"    = FMMOVTOSSPOT."FolioOperacion"  
						AND HOM_OPERACION_SPOT."FolioTransaccion" = FMMOVTOSSPOT."FolioTransaccion"
			  	WHERE FMMOVTOSSPOT."FechaOperacion" >= p_FechaInicial
		        	AND FMMOVTOSSPOT."FechaOperacion" <= p_FechaFinal
		        	AND TIPO_OPERACION_SPOT."ValorOrigen" IS NULL
		        	AND HOM_OPERACION_SPOT."FolioOperacion" IS NULL;
		        	
            -- El estado de la operacion no se encuentra homologada en optimus
			insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
            select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'SPOT' as "Entidad"
                    ,'El tipo operacion no se encuentra homologada en OPTIMUS' as "Mensaje"
                    , 'Estado' as "NombreCampoGenerico1"
                    , TO_VARCHAR(FMMOVTOSSPOT."Estado") as "ValorCampoGenerico1"
                    , 'TipoValor' as "NombreCampoGenerico2"
                    , 'EstadoSpot' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    , '' as "NombreCampoGenerico6"
                    , 0 as "ValorCampoGenerico6"
                     ,'FolioOperacion' as "NombreCampoGenerico7"
                    , FMMOVTOSSPOT."FolioOperacion" as "ValorCampoGenerico7"
                    ,'FolioTransaccion' as "NombreCampoGenerico8"
                    , FMMOVTOSSPOT."FolioTransaccion" as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              FROM "BSCL_VIRTUAL"."GES_FMMOVTOSSPOT"	AS FMMOVTOSSPOT
				INNER JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE
			         	ON upper(trim(CLIENTE."DocumentoIdentidad")) = CASE WHEN p_RutCliente = '        0-0' THEN upper(trim(FMMOVTOSSPOT."RutCliente")) ELSE upper(TRIM(p_RutCliente)) end
			    INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" as PORTAFOLIO
			            ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
			            AND PORTAFOLIO."IdCuentaOrigen" =  TRIM(TO_VARCHAR(FMMOVTOSSPOT."CuentaCliente"))
			    INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS "NEMO_INSTRUMENTO_SPOT" 		
						ON  "NEMO_INSTRUMENTO_SPOT"."TipoValor" = 'TipoInstrumentoSpot'
					    AND "NEMO_INSTRUMENTO_SPOT"."ValorOrigen" = FMMOVTOSSPOT."CodProducto"
					    AND "NEMO_INSTRUMENTO_SPOT"."ValorOrigen2" = FMMOVTOSSPOT."CodSubProducto"
				INNER JOIN "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" AS INSTRUMENTO
						ON INSTRUMENTO."Nemotecnico" = "NEMO_INSTRUMENTO_SPOT"."ValorDestino"
				INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS MONEDA_PRIMARIA
						ON MONEDA_PRIMARIA."TipoValor" = 'MonedaDivisas'
						AND MONEDA_PRIMARIA."ValorOrigen" = FMMOVTOSSPOT."CodMonPrinc"
            	INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS MONEDA_SECUNDARIA
						ON MONEDA_SECUNDARIA."TipoValor" = 'MonedaDivisas'
						AND MONEDA_SECUNDARIA."ValorOrigen" = FMMOVTOSSPOT."CodMonSecu"
				INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS TIPO_OPERACION_SPOT
						ON TIPO_OPERACION_SPOT."TipoValor" =  'TipoOperacionSpot'
						AND TIPO_OPERACION_SPOT."ValorOrigen" = FMMOVTOSSPOT."CodTipoMovto"
				LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS ESTADO_SPOT
						ON ESTADO_SPOT."TipoValor" = 'EstadoSpot'
						AND ESTADO_SPOT."ValorOrigen" = FMMOVTOSSPOT."Estado"
				LEFT JOIN "BSCL.Homologacion.BD::HOM.OperacionSpot" AS HOM_OPERACION_SPOT
						ON HOM_OPERACION_SPOT."FolioOperacion"    = FMMOVTOSSPOT."FolioOperacion"  
						AND HOM_OPERACION_SPOT."FolioTransaccion" = FMMOVTOSSPOT."FolioTransaccion"
			  	WHERE FMMOVTOSSPOT."FechaOperacion" >= p_FechaInicial
		        	AND FMMOVTOSSPOT."FechaOperacion" <= p_FechaFinal
		        	AND TIPO_OPERACION_SPOT."ValorOrigen" IS NULL
		        	AND HOM_OPERACION_SPOT."FolioOperacion" IS NULL;

             --REVISAMOS CUANTAS OPERACIONES SERAN CARGADAS
                  SELECT COUNT(*) INTO v_Total_Operaciones_Spot
                  FROM "BSCL_VIRTUAL"."GES_FMMOVTOSSPOT" as FMMOVTOSSPOT	
                  WHERE FMMOVTOSSPOT."FechaOperacion" >= p_FechaInicial
		        	AND FMMOVTOSSPOT."FechaOperacion" <= p_FechaFinal;

			-- REVISAMOS CUANTOS REGISTROS FUERON CARGADOS
                  SELECT COUNT(*) INTO v_Total_Spot_Cargados FROM 
                   "BSCL"."BSCL.Homologacion.BD::HOM.Error" 
                  WHERE "IdCarga" = p_IdCarga;
 
                  
             -- SACAMOS LOS REGISTROS PROCESADOS E INGRESADOS EN LA ASIGNACION
	   			  p_ResumenDatosProcesados =  select 'CargaOperacion Spot' as "CodigoProceso" 
	   			  									,'VAL' as "Modulo" 
	   			 									,v_Total_Operaciones_Spot as "TotalRegistrosSebra"
	   			 									,v_Total_Spot_Cargados    as "TotalRegistrosSap"
	   			 							   from dummy 	
       							    		   union all
       							    		   select * from :p_ResumenDatosProcesados;

  			     p_out_ErroresDetalle =  select 'CargaOperacion Spot' as "Codigo",'falla en la carga spot' as "Descripcion" from dummy 
  			     						 UNION ALL
					    	       		 select * from :p_out_ErroresDetalle;	
END IF;		        	
		--------------------
		--FIN SP 
		--------------------
	END;	
END;

 
