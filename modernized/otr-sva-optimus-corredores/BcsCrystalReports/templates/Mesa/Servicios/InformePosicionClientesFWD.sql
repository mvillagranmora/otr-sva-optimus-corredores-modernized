SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::InformePosicionClientesFWD";

--
--  Proposito: servicio para consultar Emision Movimientos Diario FWD Monedas
--  Autor: EORTIZ - PRONET-IT
--  Fecha Creacion: /12/2016
--

CREATE PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::InformePosicionClientesFWD"
(
-- Parametros entrada 
IN p_IdEmpresa				BIGINT,
IN p_FechaOperacion			DATE,
IN p_Usuario				VARCHAR(100)

)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER  
	DEFAULT SCHEMA "BSCL"
	--READS SQL DATA   
AS
BEGIN

	--------------------
	--Variables
	--------------------
	DECLARE varDOLAR 		DECIMAL(26,8);
		
	--+ ------------------------------------------------------------
	--+ Se obtiene el valor del dolar
	v_TblUS = 
		SELECT 	PTC."TipoCambioMercado" AS "varDOLAR"
		FROM 	"BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PTC
		WHERE 	PTC."Origen" = 'OBSERVADO'
		  		AND PTC."Fecha"	= :p_FechaOperacion
		  		AND PTC."IdMoneda"= 'DO';

	SELECT 	COUNT(1) 
	INTO	varDOLAR
	FROM	:v_TblUS;
	
	IF varDOLAR > 0 THEN
		SELECT 	"varDOLAR"
		INTO	varDOLAR
		FROM	:v_TblUS;
	END IF;
	--+ ------------------------------------------------------------	

	v_Movimientos = 
			SELECT 
				 "FolioOperacion"
				,"Nemotecnico"
			    ,"NombreInstrumento"
				,"RutCliente"
				,"NombreCliente"
				,TO_VARCHAR("FechaInicio", 'DD-MM-YYYY')                        AS "FechaInicio"
				,"HoraInicio"	
				,"FechaVencimiento" 
				,"PlazoOperacion"
				,"IdMonedaPrincipal"
				,"IdMonedaSecundaria"
				,"Monto"
				,"TipoCambioFWD"
				,"PlazoRem"
				,"MontoValorRazonableActivo"
				,"MontoValorRazonablePasivo"
				,"TasaValorRazonableActivo"
				,"TasaValorRazonablePasivo"
				,"TipoOperacion"				
				,TO_VARCHAR(:p_FechaOperacion, 'DD-MM-YYYY') 						AS "FechaConsulta"
				,:varDOLAR															AS "DolarObservado"
				,:p_Usuario 				 										AS "Usuario"
				,"Estado"
				,"ModalidadPago"			
				,"TipoCambioSpot"
				, CASE
					WHEN "ValorModalidadPago" = 'ENTREGA_FISICA' AND "EGRIdTipoOperacion" = 'ANTFWD' THEN
						CASE WHEN "IdTipoOperacion" = 'COMFWD' THEN "EGRMontoMonedaSecundaria" ELSE 0 END
					WHEN "ValorModalidadPago" = 'ENTREGA_FISICA' AND "EGRIdTipoOperacion" <> 'ANTFWD' THEN
						CASE WHEN "IdTipoOperacion" = 'COMFWD' THEN "INGMontoMonedaSecundaria" ELSE 0 END
					WHEN "ValorModalidadPago" = 'COMPENSADA' AND "IdTipoOperacion" = 'COMFWD' AND "MontoLiquidacionRef" > 0
						THEN "MontoLiquidacionRef"
					WHEN "ValorModalidadPago" = 'COMPENSADA' AND "IdTipoOperacion" = 'VENFWD' AND "MontoLiquidacionRef" > 0 
						THEN "MontoLiquidacionRef"
					ELSE 0 END 														AS "MontoRecibimos"
				, CASE
					WHEN "ValorModalidadPago" = 'ENTREGA_FISICA' AND "EGRIdTipoOperacion" = 'ANTFWD' THEN
						CASE WHEN "IdTipoOperacion" = 'COMFWD' THEN 0 ELSE "EGRMontoMonedaSecundaria" END
					WHEN "ValorModalidadPago" = 'ENTREGA_FISICA' AND "EGRIdTipoOperacion" <> 'ANTFWD' THEN
						CASE WHEN "IdTipoOperacion" = 'COMFWD' THEN 0 ELSE "INGMontoMonedaSecundaria" END
					WHEN "ValorModalidadPago" = 'COMPENSADA' AND "IdTipoOperacion" = 'COMFWD' AND "MontoLiquidacionRef" < 0 THEN 
						ABS("MontoLiquidacionRef")
					WHEN "ValorModalidadPago" = 'COMPENSADA' AND "IdTipoOperacion" = 'VENFWD' AND "MontoLiquidacionRef" < 0 THEN 
						ABS("MontoLiquidacionRef")	
					ELSE 0 END														AS "MontoPagamos"
					
				FROM (
				
					SELECT  
							CAR."FolioCartera"  												AS "FolioOperacion"
							,CAR."Nemotecnico"	 												AS "Nemotecnico"
							,INS."RazonSocial"													AS "NombreInstrumento"
							,PER."DocumentoIdentidad"											AS "RutCliente"
							,PER."NombreCompleto"												AS "NombreCliente"
							,CAR."FechaOperacion"												AS "FechaInicio"
							,TO_CHAR(OPE."FechaCreacion",'HH24:MI')								AS "HoraInicio"	
							,TO_VARCHAR(CAR."FechaVencimientoOperacion", 'DD-MM-YYYY') 			AS "FechaVencimiento" 
							, CASE
								WHEN CAR."PlazoEnDiasOperacion" > 0 THEN CAR."PlazoEnDiasOperacion"
								WHEN CAR."FechaVencimientoOperacion" > TO_DATE('0001-01-01','YYYY-MM-DD') THEN
									DAYS_BETWEEN(CAR."FechaOperacion", CAR."FechaVencimientoOperacion")
								ELSE 0 END														AS "PlazoOperacion"
							,CAR."IdMonedaPrincipal"											AS "IdMonedaPrincipal"
							,CAR."IdMonedaSecundaria"											AS "IdMonedaSecundaria"
							,CAR."MontoMonedaPrincipal"											AS "Monto"
							,CASE 	WHEN CAR."Nemotecnico" = 'ARBITR' 
									THEN CAR."ValorParidadCierre"
									ELSE CAR."ValorTipoCambioCierre" 
									END															AS "TipoCambioFWD"
							,DAYS_BETWEEN (
								TO_SECONDDATE((SELECT CON."Valor" FROM "BSCL.Plataforma.BD::PLA.Configuracion" CON 
											   WHERE CON."IdEmpresa" = CAR."IdEmpresa" AND CON."CodigoParametro" = 'PLA.FechaSistema'),'YYYY-MM-DD'),
								TO_SECONDDATE((CASE WHEN CAR."IdEstado" = 'ANT' OR 
														(CAR."IdEstado" = 'VCD' AND CAR."EGRIdTipoOperacion" = 'ANTFWD') THEN CAR."EGRFechaOperacion" 
													ELSE CAR."FechaVencimientoOperacion" END), 'YYYY-MM-DD')) 				
																								AS "PlazoRem"
							,CAR."INGMontoValorRazonableActivo" 								AS "MontoValorRazonableActivo"
							,CAR."INGMontoValorRazonablePasivo" 								AS "MontoValorRazonablePasivo"
							,CAR."INGTasaValorRazonableActivo"									AS "TasaValorRazonableActivo"
							,CAR."INGTasaValorRazonablePasivo"  								AS "TasaValorRazonablePasivo"
							,CAR."IdTipoOperacion"												AS "IdTipoOperacion"
							,CASE
									WHEN CAR."IdTipoOperacion" = 'COMFWD' THEN 'COMPRA' 
									WHEN CAR."IdTipoOperacion" = 'VENFWD' THEN 'VENTA' END 		AS "TipoOperacion"
							,CASE
								WHEN CAR."IdEstado" = 'PEN' AND COALESCE(AUT."IdEstado",'') = 'VIG' THEN 'PAU'
								WHEN CAR."IdEstado" = 'ANU' AND CAR."EGRIdConcepto" IN ('ANTFWD','REVFWD','') THEN 'REV'--cartera reversada
								ELSE CAR."IdEstado" 
							 END																AS "Estado"
							,PARAM."ValorParametro"											    AS "ValorModalidadPago"
							,PARAM."NombreParametro"	  										AS "ModalidadPago"		
							,CASE 	WHEN CAR."Nemotecnico" = 'ARBITR' 
									THEN PAR."ParidadMercado"
									ELSE CAR."ValorTipoCambioCosto" 
									END                                                         AS "TipoCambioSpot"
							,CASE WHEN CAR."Nemotecnico" = 'SECANO' OR CAR."Nemotecnico" = 'SEINFL' THEN
								"MontoMonedaPrincipal" * ((COALESCE(PAR."TipoCambioMercado",0) - CAR."ValorTipoCambioCierre") * CASE WHEN CAR."IdTipoOperacion" = 'COMFWD' THEN 1 ELSE -1 END)
							    
								WHEN CAR."Nemotecnico" = 'SECARE' THEN
								"MontoMonedaPrincipal" * (((:varDOLAR/COALESCE(PAR."TipoCambioMercado",0)) - CAR."ValorTipoCambioCierre") * CASE WHEN CAR."IdTipoOperacion" = 'COMFWD' THEN 1 ELSE -1 END * :varDOLAR)
							
								WHEN CAR."Nemotecnico" = 'ARBITR' THEN
									CASE WHEN "MultiplicaODivide" = 'D' THEN
										(CEILING("MontoMonedaPrincipal"/COALESCE(PAR."ParidadMercado",0))-CEILING("MontoMonedaPrincipal"/CAR."ValorParidadCierre"))*CASE WHEN CAR."IdTipoOperacion" = 'COMFWD' THEN 1 ELSE -1 END * :varDOLAR
									ELSE
										(((COALESCE(PAR."ParidadMercado",0)-CAR."ValorParidadCierre")*CASE WHEN CAR."IdTipoOperacion" = 'COMFWD' THEN 1 ELSE -1 END ) * "MontoMonedaPrincipal") * :varDOLAR
								END
							 END																AS "MontoLiquidacionRef"
							,CAR."EGRIdTipoOperacion"											AS "EGRIdTipoOperacion"
							,CAR."INGMontoMonedaPrincipal"										AS "INGMontoMonedaPrincipal"
					        ,CAR."INGMontoMonedaSecundaria"										AS "INGMontoMonedaSecundaria"
							,CAR."EGRMontoMonedaPrincipal"										AS "EGRMontoMonedaPrincipal"
							,CAR."EGRMontoMonedaSecundaria"										AS "EGRMontoMonedaSecundaria"
							
					FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraLite" CAR
					INNER JOIN "BSCL.Mesa.BD::MES.Operacion" AS OPE
						ON (OPE."IdOperacion" = CAR."IdOperacion" AND OPE."IdTipoProducto" = CAR."IdTipoProducto")
					LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS
						ON (CAR."IdInstrumento" = INS."IdInstrumento")
					LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI
						ON (CAR."IdCliente" = CLI."IdCliente")
					LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER
						ON CLI."IdPersona" = PER."IdPersona"
					INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" AS SOC
						ON (CLI."IdPersona" = SOC."IdPersona")
					INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR
						ON (POR."IdCliente" = CAR."IdCliente" AND POR."IdPortafolio" = CAR."IdPortafolio" AND POR."IdEstado" IN ('ACT','BLO'))
					INNER JOIN "BSCL.Mesa.BD::MES.Parametro" AS PARAM
						ON (PARAM."IdParametro" = CAR."IdModalidadCumplimiento" AND
							PARAM."NombreTipoParametro" = 'ModalidadLiquidacion' AND
							PARAM."IdEmpresa" = CAR."IdEmpresa")
					INNER JOIN "BSCL.Mesa.BD::MES.Parametro" AS PARAMFIXING
						ON (PARAMFIXING."IdParametro" = CAR."IdFixing")
					LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PAR
						ON (PAR."IdMoneda" = CASE WHEN CAR."Nemotecnico" = 'SECARE' THEN CAR."IdMonedaSecundaria"
        										  WHEN CAR."IdMonedaPrincipal" = 'USD' THEN 'DO'
        										  ELSE CAR."IdMonedaPrincipal"
                        						  END
                        AND	PAR."Fecha" = :p_FechaOperacion)
					LEFT JOIN (
					        SELECT DISTINCT "IdOperacion","FechaLiquidacionComprobantePagoOperacion","IdEmpresa"
					        --FROM "BSCL.Mesa.BD.Vistas::ConsultarComprobantePagoOperacion"
					        FROM "BSCL.Mesa.BD::MES.ComprobantePagoOperacion"
					        WHERE "IdTipoProducto" = 'MEFW' AND "RecibePaga" = 'R'
					    ) AS FPR
					   ON (FPR."IdOperacion" = CAR."IdCartera" AND FPR."IdEmpresa" = CAR."IdEmpresa")
					LEFT JOIN "BSCL.Mesa.BD::MES.Parametro" AS PCC
						ON (PCC."IdParametro" = OPE."IdClasificacionCartera" AND PCC."NombreTipoParametro" = 'ClasificacionCartera' AND PCC."IdEstado" = 'VIG')
					LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TIOP
						ON (CAR."IdTipoOperacion" = TIOP."IdTipoOperacion")
					LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MON
						ON (CAR."IdMonedaPrincipal" = MON."IdMoneda")
					LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoMoneda" AS TIPMON
						ON (MON."IdTipoMoneda" = TIPMON."IdTipoMoneda")
					LEFT JOIN "BSCL.Plataforma.BD::AUT.Evento" AS AUT
						ON (TO_NVARCHAR(CAR."IdOperacion") = COALESCE(AUT."NumeroOperacionConcepto",'0') AND
							CAR."IdEmpresa" = AUT."IdEmpresa" AND
							CAR."IdTipoOperacion" = AUT."IdTipoOperacion" AND
							AUT."IdModulo" = 'MFW')
					WHERE CAR."IdTipoProducto" = 'MEFW'
					AND	  CAR."FechaOperacion" <= :p_FechaOperacion
					AND   CASE
								WHEN CAR."IdEstado" = 'PEN' AND COALESCE(AUT."IdEstado",'') = 'VIG' THEN 'PAU'
								WHEN CAR."IdEstado" = 'ANU' AND CAR."EGRIdConcepto" IN ('ANTFWD','REVFWD','') THEN 'REV'--cartera reversada
								ELSE CAR."IdEstado" 
							END	= 'VIG'
				)
				GROUP BY "FolioOperacion","Nemotecnico","NombreInstrumento","RutCliente","NombreCliente","FechaInicio","HoraInicio"
						,"FechaVencimiento","PlazoOperacion","IdMonedaPrincipal","IdMonedaSecundaria","Monto"
						,"TipoCambioFWD","PlazoRem","MontoValorRazonableActivo","MontoValorRazonablePasivo"
						,"TasaValorRazonableActivo","TasaValorRazonablePasivo","IdTipoOperacion","TipoOperacion","Estado"
						,"ModalidadPago","TipoCambioSpot","MontoLiquidacionRef","ValorModalidadPago","EGRIdTipoOperacion"
						,"EGRMontoMonedaPrincipal","EGRMontoMonedaSecundaria","INGMontoMonedaPrincipal","INGMontoMonedaSecundaria";



	SELECT 
		 *
		,(SELECT TOP 1 "Binario" FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen" 
		  WHERE "NombreImagen" = 'LOGO_CORREDOR' ORDER BY "Version" DESC) AS "LogoCorredora"
		
	FROM :v_Movimientos;
	
 END;