
SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::MRF.CoberturaPatrimonialDetalle";

--
--  Proposito: servicio para consultar Emision Movimientos Diario FWD Monedas
--  Autor: EORTIZ - PRONET-IT
--  Fecha Creacion: 03/04/2017
--
--* Fecha ......: 30 Mayo 2018
--* Autor ......: MSH, BCS
--* Descripción : - Corrige Tipo Cartera : Libre, CP, VP
--*				  - Incluye Todas las carteras (antes solo op. mesa)
--* 			  - Incluye logica de Pactos Calzados.
--*				  - Incluye logica de Art. 514 : Plazo papel < 90 dias => Val. a Compra, Plazo papel > 90 dias Val. a Mercado 
--* 			  - Corrige Porcentaje de Cobertura.

-- Descripcion : Se agregó tabla de salida (out) ya que el resultado del servicio se utilizará para genrar excel y pdf desde la UI

CREATE PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::MRF.CoberturaPatrimonialDetalle"
(
-- Parametros entrada 
	IN p_IdEmpresa				BIGINT,
	IN p_FechaOperacion			DATE,
	IN p_plazoDias				BIGINT,
	IN p_opcion					BIGINT,
	IN p_TipoValor				BIGINT,
	IN p_IdCliente				BIGINT,
	IN p_IdPortafolio			BIGINT,
	IN p_NombreMesa				NVARCHAR(30),
	
	OUT p_out_CoberturaPatrimonialDetalle    TABLE 
	(
		 "CantidadDecimales"		BIGINT,
		 "TasaDecimales"			BIGINT,
		 "IdCoberturaPatrimonial"	BIGINT,
		 "FolioComprobante"			BIGINT,
		 "IdTipoOperacion"			NVARCHAR(6),		 
		 "IdInstrumento" 			BIGINT,
		 "IdTipoInstrumento"		NVARCHAR(40),
		 "TipoInstrumento"			NVARCHAR(100),
		 "ValorMercado" 			DECIMAL(18,4),
		 "TIR" 						DECIMAL(18,6),
		 "Cartera" 					NVARCHAR(40),
		 "CC" 						NVARCHAR(40),
		 "CC1" 						NVARCHAR(40),
		 "FechaVcto" 				NVARCHAR(40),
		 "Cantidad" 				BIGINT,
		 "Emisor"					NVARCHAR(40),
		 "Nemotecnico"				NVARCHAR(40),
		 "Corredor"					NVARCHAR(100),
		 "Variable_Cobertura"		NVARCHAR(40),
		 "IdMonedaInstrumento"		NVARCHAR(3),
		 "CLA_RIE"					NVARCHAR(40),
		 "FechaVencimientoInstrumento" NVARCHAR(40),
		 "IdIndicador"				NVARCHAR(40),
		 "NombreIndicador"			NVARCHAR(40),
		 "Plazo"					BIGINT,
		 "PlazoPacto"				BIGINT,
		 "TramoCobertura"			DECIMAL(18,4),
		 "PorcentajeCobertura"		DECIMAL(18,4),
		 "MontoCobertura"			DECIMAL(18,4),
		 "Clasificacion"			NVARCHAR(100),
		 "CatRie"					NVARCHAR(40),
		 "CatRiePactos"				NVARCHAR(40),
		 "Tipo_Amo"					BIGINT,
		 "TieneCupon"				NVARCHAR(40),
		 "Fecha"					NVARCHAR(10),
		 "IdOpcionSeleccionada"		BIGINT,
		 "OpcionSeleccionada"  		NVARCHAR(40),
		 "IdTipoValorizacion" 		BIGINT,
		 "TipoValorizacion"  		NVARCHAR(40),
		 "IdOpcPlazoDias" 			BIGINT,
		 "OpcPlazoDias"  			NVARCHAR(30),
		 "Observaciones"  			NVARCHAR(100),
		 "IdEmpresa" 				BIGINT,
		 "TipoEmisor"  				NVARCHAR(10),
		 "TipoCartera"  			NVARCHAR(10),
		 "NombreMesa"				NVARCHAR(30)
	),
	
	OUT p_out_ErroresContador 				INT

	
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER  
	DEFAULT SCHEMA "BSCL"
	
AS
 	BEGIN
		BEGIN
		--------------------
		--Variables
		--------------------
		DECLARE varFECHA DATE;
		DECLARE porCobertura	varchar(4);
		DECLARE varExisteOperacion		INT;
		DECLARE varExistePorCobertura	INT;
		DECLARE v_IdCliente             BIGINT;
		

		
		-----------------------------------------------
		-- TABLA TEMPORAL 1
		-----------------------------------------------
		CREATE LOCAL TEMPORARY TABLE #Tmp_Cobertura 
		(	"FolioComprobante" 					BIGINT,
			"FolioCartera" 						BIGINT,		
			"IdTipoOperacion"					NVARCHAR(40),
			"IdCartera"							BIGINT,
			"IdInstrumento"						BIGINT,
			"IdTipoInstrumento"					NVARCHAR(40),
			"TipoInstrumento"					NVARCHAR(100),
			"ValorMercado"						DECIMAL(18,4),
			"TIR"								DECIMAL(18,6),
			"MtoValorizadoMercado"				DECIMAL(18,4),
			"TIRMercado"						DECIMAL(18,4),						
			"Cartera"							NVARCHAR(40),
			"CC"								NVARCHAR(40),
			"CC1"								NVARCHAR(40),
			"FechaVcto"							NVARCHAR(40),
			"Cantidad"							BIGINT,
			"ValorRescate"						DECIMAL(18,4),
			"EsFungible"						NVARCHAR(10),
			"IdClasificacionTipoInstrumento"	NVARCHAR(3),
			"Emisor"							NVARCHAR(40),
			"Nemotecnico"						NVARCHAR(40),
			"TipoReajuste"						NVARCHAR(05),
			"Corredor"							NVARCHAR(40),
			"Variable_Cobertura"				NVARCHAR(40),
			"IdMonedaInstrumento"				NVARCHAR(40),
			"CLA_RIE"							NVARCHAR(40),
			"FechaVencimientoInstrumento"		NVARCHAR(40),
			"IdIndicador"						NVARCHAR(40),
			"NombreIndicador"					NVARCHAR(40),
			"Plazo"								BIGINT,
			"PlazoPacto"						BIGINT,
			"Clasificacion"						NVARCHAR(40),
			"CatRie" 							NVARCHAR(40),
			"Tipo_Amo"							BIGINT,
			"TieneCupon"						NVARCHAR(40),
			"Fecha"								NVARCHAR(10),
			"IdOpcionSeleccionada"				BIGINT,			
			"OpcionSeleccionada"				NVARCHAR(40),
			"TipoValorizacion"					NVARCHAR(40),
			"OpcPlazoDias"						NVARCHAR(30),
			"Observaciones"						NVARCHAR(100),
			"CatRiePactos"						NVARCHAR(40),
			"IdTipoValorizacion"				BIGINT,
			"IdOpcPlazoDias"					BIGINT,
			"TipoEmisor"						NVARCHAR(10),
			"TipoCartera"						NVARCHAR(10),
			"TramoCobertura"					DECIMAL(18,4),
			"PorcentajeCobertura"				DECIMAL(18,4)
			);

			

		SELECT 	"Valor" 
		INTO	varFECHA
		FROM 	"BSCL.Plataforma.BD::PLA.Configuracion"
		WHERE 	"IdEmpresa" = :p_IdEmpresa AND "CodigoParametro" = 'PLA.FechaSistema';
		


		p_IdCliente = COALESCE( p_IdCliente, 0 );
		p_IdPortafolio = COALESCE( p_IdPortafolio, 0 );
		

		IF (:p_IdCliente = 0) THEN
			SELECT Top 1 "IdCliente" INTO v_IdCliente FROM "BSCL.Mesa.BD::MES.Mesa" WHERE "IdTipoProducto" = 'MERF';
			p_IdCliente = COALESCE(v_IdCliente, 0 );

		END IF;
		
				
		IF :p_Opcion = 1 THEN
			--* INFORME POR CARTERA
			INSERT INTO #Tmp_Cobertura (
				"FolioComprobante", "FolioCartera", "IdCartera", 
				"Cantidad", "ValorRescate", "ValorMercado", "MtoValorizadoMercado", "TIR", "TIRMercado",
				"Cartera", "CC", "CC1",
				"FechaVcto", "Plazo", "PlazoPacto","TipoEmisor","TipoCartera" )
			SELECT	COALESCE( OP."FolioOperacion", CC."FolioComprobante" ) AS "FolioComprobante"
					,CC."FolioCartera" 	AS "FolioCartera"
					,CC."IdCartera"		AS "IdCartera"
					,CASE 	WHEN CC."IdClasificacionTipoInstrumento" = 'IF' THEN CC."INGValorRescate" 
							ELSE CC."INGCantidad" 	END AS "Cantidad"
					,CC."INGValorRescate"			AS "ValorRescate"
					,CC."INGMontoMercadoActualMonedaInstrumento" AS "ValorMercado"
					,CC."INGMontoATasaMercadoActual"AS "MtoValorizadoMercado"
					,CC."INGTasaCompra"				AS "TIR"
					,CC."INGTasaMercadoActual" 		AS "TIRMercado"
					,'Cartera Libre'	AS "Cartera"						
					,''	AS "CC"
					,''	AS "CC1"
					,CC."FechaVencimientoOperacion" AS "FechaVcto"
					,DAYS_BETWEEN (	TO_SECONDDATE( varFECHA, 'YYYY-MM-DD'), TO_SECONDDATE( CC."FechaVencimientoInstrumento", 'YYYY-MM-DD') ) AS "Plazo" 
					,DAYS_BETWEEN (	TO_SECONDDATE( varFECHA, 'YYYY-MM-DD'), TO_SECONDDATE(CC."FechaVencimientoOperacion", 'YYYY-MM-DD') ) AS "PlazoPacto"
					,(CASE 
						WHEN (CC."IdClasificacionTipoInstrumento" = 'IF' AND CC."IdTipoInstrumento" = 'PAG' AND COALESCE(T_EMI."CodigoTipoEmisor",'') = '') THEN 'OE'
						ELSE COALESCE(T_EMI."CodigoTipoEmisor",'')
					 END) AS "TipoEmisor"
					,CC."TipoCarteraCorredor" AS "TipoCartera"
					
										
			FROM 	"BSCL.Custodia.BD::CUS.Cartera" CC
			LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" OP ON 	(CC."FolioComprobante" = OP."IdOperacion" AND OP."IdEstado" = 'VIG' AND OP."IdTipoProducto" = 'MERF')
								
			LEFT JOIN "BSCL.Plataforma.BD::MER.TipoInstrumentoTipoEmisor" AS T_EMI ON 
			(
					T_EMI."IdClasificacionTipoInstrumento" = CC."IdClasificacionTipoInstrumento" AND T_EMI."IdTipoInstrumento" = CC."IdTipoInstrumento"
					
					AND  ((T_EMI."Nemotecnico" = CASE CC."IdClasificacionTipoInstrumento" WHEN 'IF' THEN LEFT(CC."Nemotecnico",4) ELSE '' END) OR
					      (T_EMI."Nemotecnico" = CASE CC."IdClasificacionTipoInstrumento" WHEN 'IF' THEN LEFT(CC."Nemotecnico",3) ELSE '' END) OR
					      (T_EMI."Nemotecnico" = CASE CC."IdClasificacionTipoInstrumento" WHEN 'IF' THEN LEFT(CC."Nemotecnico",6) ELSE ''END)
						)
						
						
					AND T_EMI."IdEmisor" = (CASE 
											    WHEN CC."IdClasificacionTipoInstrumento" = 'IF' AND T_EMI."Nemotecnico" = LEFT(CC."Nemotecnico",6)   THEN CC."IdEmisor"
											    WHEN CC."IdClasificacionTipoInstrumento" = 'IF' AND T_EMI."Nemotecnico" = ''   THEN ''
											   ELSE ''
											END)
			)	
			
			WHERE 	(CC."IdClasificacionTipoInstrumento" = 'RF' OR CC."IdClasificacionTipoInstrumento" = 'IF')
			
					AND (	(:p_plazoDias = 1  AND (DAYS_BETWEEN(TO_SECONDDATE( varFECHA, 'YYYY-MM-DD' ), TO_SECONDDATE( CC."FechaVencimientoOperacion", 'YYYY-MM-DD'))) <= 90 ) 
						OR 	(:p_plazoDias = 2  AND (DAYS_BETWEEN(TO_SECONDDATE( varFECHA, 'YYYY-MM-DD' ), TO_SECONDDATE( CC."FechaVencimientoOperacion", 'YYYY-MM-DD'))) > 90)
						OR	(:p_plazoDias = 3 )
						)
					AND ( :p_IdCliente = 0 OR :p_IdCliente = CASE WHEN OP."IdTipoOperacion" IN ( 'COMPAC', 'COMTER' ) THEN CC."IdCliente" WHEN OP."IdTipoOperacion" IS NULL THEN CC."IdCliente" ELSE CC."IdClienteRelacionado" END )
					AND ( :p_IdPortafolio = 0 OR :p_IdPortafolio = CASE WHEN OP."IdTipoOperacion" IN ( 'COMPAC', 'COMTER' ) THEN CC."IdPortafolio"  ELSE CC."IdPortafolioRelacionado" END )
					
					AND	CC."IdTipoInstrumento" <> 'PAC'
					AND CC."IdTipoOperacion" <> 'VENTER'
					AND CC."IdEstado" = 'VIG'
					AND CC."IdEmpresa" = :p_IdEmpresa;

			
					
					
			/** Garantias **/
			INSERT INTO #Tmp_Cobertura (
				"FolioComprobante", "FolioCartera", "IdCartera", 
				"Cantidad", "ValorRescate", "ValorMercado", "MtoValorizadoMercado", "TIR", "TIRMercado",
				"Cartera", "CC", "CC1",
				"FechaVcto", "Plazo", "PlazoPacto","TipoEmisor","TipoCartera" )
				
			SELECT 	COALESCE( OP."FolioOperacion", CC."FolioComprobante" ) AS "FolioComprobante"
					,CC."FolioCartera" 	AS "FolioCartera"
					,CC."IdCartera"	AS "IdCartera"
					,CASE 	WHEN CC."IdClasificacionTipoInstrumento" = 'IF' THEN Gar."INGValorRescate" 
							ELSE Gar."INGCantidad" 	END AS "Cantidad"
					,Gar."INGValorRescate"			AS "ValorRescate"
					,CASE WHEN CC."IdEstado" = 'RPA' THEN CC."EGRMontoMercadoMonedaInstrumento"
						  ELSE Gar."MontoGarantia" END AS "ValorMercado"
					,CASE WHEN CC."IdEstado" = 'RPA' THEN CC."EGRMontoATasaMercado"
						  ELSE Gar."MontoGarantia" END AS "MtoValorizadoMercado"
					,CC."INGTasaCompra"				AS "TIR"
					,CC."INGTasaMercadoActual" 		AS "TIRMercado"
					,'Cartera Garantia'	AS "Cartera"
					,''	AS "CC"
					,''	AS "CC1"	
					,CC."FechaVencimientoOperacion" AS "FechaVcto"
					,DAYS_BETWEEN (	TO_SECONDDATE( varFECHA, 'YYYY-MM-DD'), TO_SECONDDATE( CC."FechaVencimientoInstrumento", 'YYYY-MM-DD') ) AS "Plazo" 
					,DAYS_BETWEEN (	TO_SECONDDATE( varFECHA, 'YYYY-MM-DD'), TO_SECONDDATE(CC."FechaVencimientoOperacion", 'YYYY-MM-DD') ) AS "PlazoPacto"
					
					,(CASE 
						WHEN (CC."IdClasificacionTipoInstrumento" = 'IF' AND CC."IdTipoInstrumento" = 'PAG' AND COALESCE(T_EMI."CodigoTipoEmisor",'') = '') THEN 'OE'
						ELSE COALESCE(T_EMI."CodigoTipoEmisor",'')
					END) AS "TipoEmisor"
					,'' AS "TipoCarteraCorredor"
					
			FROM 	"BSCL.Custodia.BD::CUS.Cartera" CC
					
			LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" OP  ON 	CC."FolioComprobante" = OP."IdOperacion" AND OP."IdEstado" = 'VIG' AND OP."IdTipoProducto" = 'MERF'
					
			LEFT JOIN "BSCL.Plataforma.BD::MER.TipoInstrumentoTipoEmisor" AS T_EMI ON 
			(
					T_EMI."IdClasificacionTipoInstrumento" = CC."IdClasificacionTipoInstrumento" AND T_EMI."IdTipoInstrumento" = CC."IdTipoInstrumento"
					
					AND  ((T_EMI."Nemotecnico" = CASE CC."IdClasificacionTipoInstrumento" WHEN 'IF' THEN LEFT(CC."Nemotecnico",4) ELSE '' END) OR
					      (T_EMI."Nemotecnico" = CASE CC."IdClasificacionTipoInstrumento" WHEN 'IF' THEN LEFT(CC."Nemotecnico",3) ELSE '' END) OR
					      (T_EMI."Nemotecnico" = CASE CC."IdClasificacionTipoInstrumento" WHEN 'IF' THEN LEFT(CC."Nemotecnico",6) ELSE ''END)
						)
						
						
					AND T_EMI."IdEmisor" = (CASE 
											    WHEN CC."IdClasificacionTipoInstrumento" = 'IF' AND T_EMI."Nemotecnico" = LEFT(CC."Nemotecnico",6)   THEN CC."IdEmisor"
											    WHEN CC."IdClasificacionTipoInstrumento" = 'IF' AND T_EMI."Nemotecnico" = ''   THEN ''
											   ELSE ''
											END)
			)	
			
			INNER JOIN "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantiasSaldos" Gar ON (Gar."FolioCarteraOrigen" = CC."FolioCartera"
						AND CC."IdCliente" = Gar."IdCliente" AND Gar."FolioComprobanteOperacionOrigen" = CC."FolioComprobante" AND Gar."IdCarteraOrigen" = CC."IdCartera")	
										
			WHERE 	CC."IdClasificacionTipoInstrumento" IN ('RF','IF')
									
					AND (	(:p_plazoDias = 1  AND (DAYS_BETWEEN(TO_SECONDDATE( varFECHA, 'YYYY-MM-DD' ),
																 TO_SECONDDATE( CC."FechaVencimientoOperacion", 'YYYY-MM-DD'))) <= 90 ) 
						OR 	(:p_plazoDias = 2  AND (DAYS_BETWEEN(TO_SECONDDATE( varFECHA, 'YYYY-MM-DD' ),
																 TO_SECONDDATE( CC."FechaVencimientoOperacion", 'YYYY-MM-DD'))) > 90)
						OR	(:p_plazoDias = 3 )
						)
					AND ( :p_IdCliente = 0 OR :p_IdCliente = CC."IdCliente" )
					AND ( :p_IdPortafolio= 0 OR :p_IdPortafolio = CC."IdPortafolio" )

					AND	CC."IdTipoInstrumento" <> 'PAC'
					AND (CC."IdEstado" = 'RPA' OR CC."IdEstado" = 'LIQ')
					AND CC."IdEmpresa" = :p_IdEmpresa
					
					GROUP BY OP."FolioOperacion"
					,CC."FolioComprobante" 
					,CC."FolioCartera"
					,CC."IdCartera"	
					,CC."IdClasificacionTipoInstrumento"
					,Gar."INGValorRescate" 
					,Gar."INGCantidad"
					,Gar."INGValorRescate"
					,CC."INGMontoATasaCompraActual"
					,CC."INGMontoATasaMercadoActual"
					,CC."INGTasaCompra"
					,CC."INGTasaMercadoActual"
					,CC."FechaVencimientoOperacion"
					,CC."FechaVencimientoInstrumento"
					,CC."FechaVencimientoOperacion"
					,CC."IdEstado"
					,CC."EGRMontoATasaMercado"
					,CC."IdTipoInstrumento"
					,CC."Nemotecnico"
					,CC."IdEmisor"
					,Gar."MontoGarantia"
					,CC."EGRMontoMercadoMonedaInstrumento"
					,CC."IdTipoOperacion"
					,T_EMI."CodigoTipoEmisor";	

		ELSE --* IF :p_Opcion = 1 THEN


		
			 --* INFORME POR PACTOS CALZADOS
			INSERT INTO #Tmp_Cobertura (
				"FolioComprobante", "FolioCartera", "IdCartera", 
				"Cantidad", "ValorRescate", "ValorMercado", "MtoValorizadoMercado", "TIR", "TIRMercado",
				"Cartera", "CC", "CC1",
				"FechaVcto", "Plazo", "PlazoPacto" )
			SELECT	OP."FolioOperacion"	AS "FolioComprobante"
					,CAR."FolioCartera" AS "FolioCartera" 
					,CAR."IdCartera"	AS "IdCartera"
					
					,CASE 	WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' THEN CAR."INGValorRescate" 
							ELSE CAR."INGCantidad" 	END AS "Cantidad"
					,CAR."INGValorRescate"			AS "ValorRescate"
					,CAR."INGMontoATasaCompraActual" AS "ValorMercado"
					,CAR."INGMontoATasaMercadoActual"AS "MtoValorizadoMercado"
					,CAR."INGTasaCompra"				AS "TIR"
					,CAR."INGTasaMercadoActual" 		AS "TIRMercado"
									
					,CASE 	WHEN OP."IdTipoOperacion" = 'VENPAC' THEN 'Venta con Pacto' 
							WHEN OP."IdTipoOperacion" = 'COMPAC' THEN 'Compra con Pacto'
							END						AS "Cartera"	
					,CASE 	WHEN OP."IdTipoOperacion" = 'COMPAC' THEN 'CP'
							WHEN OP."IdTipoOperacion" = 'VENPAC' THEN 'VP'
							ELSE ''	END				AS "CC"
					,CASE 	WHEN OP."IdTipoOperacion" = 'COMPAC' THEN 'CC1'
							ELSE ''	END				AS "CC1"	
					
					,OP."FechaVencimiento" AS "FechaVcto"
					,DAYS_BETWEEN (	TO_SECONDDATE( varFECHA, 'YYYY-MM-DD'), TO_SECONDDATE( CAR."FechaVencimientoInstrumento", 'YYYY-MM-DD') ) AS "Plazo"
					,DAYS_BETWEEN (	TO_SECONDDATE( varFECHA, 'YYYY-MM-DD'), TO_VARCHAR( OP."FechaVencimiento", 'YYYY-MM-DD') ) AS "PlazoPacto"
					
			FROM 	"BSCL.Mesa.BD::MES.Operacion" OP
			INNER JOIN "BSCL.Custodia.BD::CUS.Reserva" RSV ON 	OP."IdOperacion" = RSV."IdOperacion" AND RSV."IdCarteraCustodia" > 0
			INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR ON 	RSV."IdCarteraCustodia" = CAR."IdCartera"
															
						
			WHERE 	(OP."IdTipoOperacion" = 'COMPAC' OR OP."IdTipoOperacion" = 'VENPAC') 
					AND OP."IdEstado" = 'VIG' 
					AND OP."IdTipoProducto" = 'MERF'
					AND ( p_IdCliente = 0 OR p_IdCliente = OP."IdClienteCorredor" )
					AND ( p_IdPortafolio = 0 OR p_IdPortafolio = OP."IdPortafolioCorredor" )
					----------------------------------------------------------------------------------------------						
					AND (	(:p_plazoDias = 1  AND (DAYS_BETWEEN(TO_SECONDDATE( varFECHA, 'YYYY-MM-DD' ), TO_VARCHAR( OP."FechaVencimiento", 'YYYY-MM-DD'))) <= 90 ) 
						OR 	(:p_plazoDias = 2  AND (DAYS_BETWEEN(TO_SECONDDATE( varFECHA, 'YYYY-MM-DD' ), TO_VARCHAR( OP."FechaVencimiento", 'YYYY-MM-DD'))) > 90)
						OR	(:p_plazoDias = 3 ) )
					----------------------------------------------------------------------------------------------				
					AND OP."IdEmpresa" = :p_IdEmpresa;
					

		END IF; --* IF :p_Opcion = 1 THEN

		--* VALIDAR SI EXISTEN OPERACIONES
		SELECT 	COUNT (1) 
		INTO 	varExisteOperacion
		FROM 	#Tmp_Cobertura;
		
	
		
		--* ---------------------------------------------------------------
		IF varExisteOperacion != 0  THEN
			UPDATE	TMP
			SET		TMP."IdTipoOperacion" = CASE WHEN "CC" = 'CP' THEN 'COMPAC'
												 WHEN "CC" = 'VP' THEN 'VENPAC'
												 ELSE CC."IdTipoOperacion" END,
					TMP."IdInstrumento" = CC."IdInstrumento", 
					TMP."IdTipoInstrumento" = CASE WHEN "CC" > '' THEN 'MERF' ELSE CC."IdTipoProducto" END,
					TMP."TipoInstrumento" = TINS."Descripcion",
					TMP."EsFungible" = CC."EsFungible",
					TMP."IdClasificacionTipoInstrumento" = CC."IdClasificacionTipoInstrumento",
					TMP."Emisor" = CC."IdEmisor",
					TMP."Nemotecnico" = CASE WHEN CC."IdClasificacionTipoInstrumento"='IF' THEN CC."NemoSVS" ELSE CC."Nemotecnico" END,
					TMP."TipoReajuste" = CC."IdTipoReajuste",
					TMP."Corredor" = EMP."Nombre",
					TMP."IdMonedaInstrumento" = CC."IdTipoReajuste", --CC."IdMonedaInstrumento",
					TMP."CLA_RIE" = COALESCE(ICR."ValorClasificacionRiesgo",''),
					TMP."IdIndicador" = CIP."IdTipoEmisor",
					
					TMP."NombreIndicador" = CIP."Codigo",
					TMP."Clasificacion" = SEG."NombreSegmentoCliente",
					TMP."CatRie" = COALESCE(IR."ValorClasificacionRiesgo",''),
					TMP."Tipo_Amo" = CASE WHEN INS."TieneCupon"='S' THEN 1 ELSE 2 END,
					TMP."TieneCupon" = INS."TieneCupon",
					TMP."FechaVencimientoInstrumento" = CC."FechaVencimientoInstrumento",
					TMP."Fecha" = :p_FechaOperacion,
					TMP."Observaciones" = '',
					TMP."TramoCobertura" = 0,
					
					
					TMP."CatRiePactos" = '',
					TMP."IdOpcionSeleccionada" = :p_opcion,
					TMP."IdTipoValorizacion" = :p_TipoValor,
					TMP."Cartera" = CASE WHEN CC."IdTipoOperacion" = 'COMPAC' THEN
						CASE WHEN TMP."Cartera" = 'Cartera Garantia' THEN 'Cartera Garantia' ELSE 'Cartera Libre' END
						ELSE CASE WHEN CC."IdTipoOperacion" = 'VENPAC' THEN 'Cartera Comprometida' 
						ELSE CASE WHEN TMP."Cartera" = 'Cartera Garantia' THEN 'Cartera Garantia' ELSE 'Cartera Libre' END END
						END
					
			FROM	#Tmp_Cobertura TMP			
			INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" CC ON (TMP."IdCartera" = CC."IdCartera")				
			INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdCliente" = CC."IdCliente")			
			LEFT JOIN  "BSCL.SocioNegocio.BD::CLI.TipoSegmento" AS  SEG ON (SEG."IdSegmentoCliente" = CLI."IdSegmentoCliente")
			LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" IR ON IR."IdInstrumento" = CC."IdInstrumento" AND IR."IdEntidadClasificadoraRiesgo" ='BCS'	
			LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON INS."IdInstrumento" = CC."IdInstrumento"
			INNER JOIN "BSCL.Plataforma.BD::MER.Emisor" Ex ON CC."IdEmisor" = Ex."IdEmisor"
			INNER JOIN "BSCL.Plataforma.BD::MER.TipoEmisor" CIP ON CIP."IdTipoEmisor" = Ex."IdTipoEmisor"
			LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TINS ON (TINS."IdTipoInstrumento" = CC."IdTipoInstrumento")	
			LEFT JOIN "BSCL.Plataforma.BD::PLA.Empresa" AS EMP  ON (CC."IdEmpresa" = EMP."IdEmpresa")						
			LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" AS ICR ON ICR."IdInstrumento" = Ins."IdInstrumento";						

			
			--select 'Tabla Temporal Antes de Borrado',* FROM #Tmp_Cobertura;
			--* ----------------------------------------------------------------------------
			--* OJO!!
			--* Limpia la data de los que no tienen Instrumento asociado o Tipo de operacion.
			--* ----------------------------------------------------------------------------
			DELETE 	FROM #Tmp_Cobertura
			WHERE	COALESCE("IdTipoOperacion", '?') = '?' OR RTRIM(COALESCE("IdMonedaInstrumento", '')) = '';
			

			SELECT 	"ValorParametro" INTO porCobertura
			FROM 	"BSCL.Mesa.BD::MES.Parametro" 
			WHERE 	"NombreTipoParametro" = '%_COBERTURA_PATRIMONIAL';
		
			DELETE FROM "BSCL.Mesa.BD::MRF.CoberturaPatrimonial"
			WHERE 	"Fecha" = :p_FechaOperacion
					AND "IdOpcionSeleccionada" = :p_opcion
					AND	"IdTipoValorizacion" = :p_TipoValor;

							
			UPDATE	#Tmp_Cobertura
			SET		"TramoCobertura" = (
						CASE  WHEN "Cartera" = 'Cartera Libre' OR "Cartera" = 'Cartera Garantia' THEN
							CASE WHEN "TipoEmisor" = 'ES' THEN
									CASE	WHEN "Plazo" <= 0 THEN 0
			  								WHEN "Plazo" <= 90 THEN 2
			        						WHEN "Plazo" > 90   AND "Plazo" <= 360  THEN 5
			         						WHEN "Plazo" > 360  AND "Plazo" <= 1800 THEN 8
			            					WHEN "Plazo" > 1800 AND "Plazo" <= 4320 AND "TieneCupon" = 'S' THEN 9
			               					WHEN "Plazo" > 1800 AND "Plazo" <= 4320 AND "TieneCupon" = 'N' THEN 14
			               					WHEN "Plazo" > 4320 AND "TieneCupon" = 'S' THEN 25
						                    ELSE 14
									END
							ELSE
									CASE	WHEN "Plazo" <= 0 THEN 0
											WHEN "Plazo" <= 90 AND "TipoInstrumento" = 'PAGARE' THEN 2
			  								WHEN "Plazo" <= 90 THEN 2.6
			        						WHEN "Plazo" > 90   AND "Plazo" <= 360  THEN 6.5
			         						WHEN "Plazo" > 360  AND "Plazo" <= 1800 THEN 10.4
			            					WHEN "Plazo" > 1800 AND "Plazo" <= 4320 AND "TieneCupon" = 'S' THEN 11.7
			               					WHEN "Plazo" > 1800 AND "Plazo" <= 4320 AND "TieneCupon" = 'N' THEN 18.20
			               					WHEN "Plazo" > 4320 AND "TieneCupon" = 'S' THEN 32.5
						                    ELSE 18.20
									END
							END
						ELSE
							CASE WHEN  "TipoEmisor" = 'ES' THEN
									CASE	WHEN "PlazoPacto" <= 0 THEN 0
			  								WHEN "PlazoPacto" <= 90 THEN 2
			        						WHEN "PlazoPacto" > 90   AND "PlazoPacto" <= 360  THEN 5
			         						WHEN "PlazoPacto" > 360  AND "PlazoPacto" <= 1800 THEN 8
			            					WHEN "PlazoPacto" > 1800 AND "PlazoPacto" <= 4320 AND "TieneCupon" = 'S' THEN 9
			               					WHEN "PlazoPacto" > 1800 AND "PlazoPacto" <= 4320 AND "TieneCupon" = 'N' THEN 14
			               					WHEN "PlazoPacto" > 4320 AND "TieneCupon" = 'S' THEN 25
						                    ELSE 14
									END
								 WHEN	"TipoCartera" = 'CC' THEN 0
							ELSE
									CASE	WHEN "PlazoPacto" <= 0 THEN 0
										WHEN "Plazo" <= 90 AND "TipoInstrumento" = 'PAGARE' THEN 2
			  								WHEN "PlazoPacto" <= 90 THEN 2.6
			        						WHEN "PlazoPacto" > 90   AND "PlazoPacto" <= 360  THEN 6.5
			         						WHEN "PlazoPacto" > 360  AND "PlazoPacto" <= 1800 THEN 10.4
			            					WHEN "PlazoPacto" > 1800 AND "PlazoPacto" <= 4320 AND "TieneCupon" = 'S' THEN 11.7
			               					WHEN "PlazoPacto" > 1800 AND "PlazoPacto" <= 4320 AND "TieneCupon" = 'N' THEN 18.20
			               					WHEN "PlazoPacto" > 4320 AND "TieneCupon" = 'S' THEN 32.5
						                    ELSE 18.20
									END
							END			
						END ),
						"CatRie" = (
							CASE WHEN  "Cartera" = 'Cartera Libre' OR "Cartera" = 'Cartera Garantia' THEN
								CASE 	WHEN "Plazo" < 0 THEN ''  
										WHEN "Plazo" <= 90 THEN 'A'
										WHEN "Plazo" > 90  AND "Plazo" <= 360 THEN 'B'
										WHEN "Plazo" > 360 AND "Plazo" <= 1800 THEN 'C'
										WHEN "Plazo" > 1800 AND "Plazo" <= 4320 AND "TieneCupon" = 'S' THEN 'D TIPO 1'
										WHEN "Plazo" > 1800 AND "Plazo" <= 4320 AND "TieneCupon" = 'N' THEN 'D TIPO 2'
										WHEN "Plazo" > 4320 AND "TieneCupon" = 'S' THEN 'E TIPO 1'
										ELSE 'E TIPO 2'
								END
							ELSE
								CASE 	WHEN "PlazoPacto" < 0 THEN ''  
										WHEN "PlazoPacto" <= 90 THEN 'A'
										WHEN "PlazoPacto" > 90  AND "PlazoPacto" <= 360 THEN 'B'
										WHEN "PlazoPacto" > 360 AND "PlazoPacto" <= 1800 THEN 'C'
										WHEN "PlazoPacto" > 1800 AND "PlazoPacto" <= 4320 AND "TieneCupon" = 'S' THEN 'D TIPO 1'
										WHEN "PlazoPacto" > 1800 AND "PlazoPacto" <= 4320 AND "TieneCupon" = 'N' THEN 'D TIPO 2'
										WHEN "PlazoPacto" > 4320 AND "TieneCupon" = 'S' THEN 'E TIPO 1'
										ELSE 'E TIPO 2'
								END
							END),
					
					/*"CatRiePactos" = 
						CASE 	WHEN "PlazoPacto" <  0 THEN ''  
								WHEN "PlazoPacto" <= 90 THEN 'A'
								WHEN "PlazoPacto" > 90  AND "PlazoPacto" <= 360 THEN 'B'
								WHEN "PlazoPacto" > 360 AND "PlazoPacto" <= 1800 THEN 'C'
								WHEN "PlazoPacto" > 1800 AND "PlazoPacto" <= 4320 AND "TieneCupon" = 'S' THEN 'D TIPO 1'
								WHEN "PlazoPacto" > 1800 AND "PlazoPacto" <= 4320 AND "TieneCupon" = 'N' THEN 'D TIPO 2'
								WHEN "PlazoPacto" > 4320 AND "TieneCupon" = 'S' THEN 'E TIPO 1'
								ELSE 'E TIPO 2'
						END,*/
					--* ART 514
					"ValorMercado" =
						CASE	WHEN :p_TipoValor = 2 AND "Plazo" > 90 THEN "MtoValorizadoMercado" 
								ELSE "ValorMercado"
						END,
					"TIR" =
						CASE	WHEN :p_TipoValor = 2 AND "Plazo" > 90 AND "TIRMercado" > 0 THEN "TIRMercado" 
								WHEN :p_TipoValor = 2 AND "Plazo" > 90 AND "TIRMercado" = 0 THEN "TIR"
								ELSE "TIR"
						END,
					"OpcionSeleccionada" = 
						CASE	WHEN :p_opcion = 1 THEN 'POR CARTERAS'
								ELSE'POR PACTOS CALZADOS'
						END,
					"TipoValorizacion" = 
						CASE	WHEN :p_TipoValor = 1 THEN 'ART.514'
								ELSE 'A MERCADO'
						END,
					"OpcPlazoDias" =
						CASE 	WHEN :p_plazoDias = 1 THEN 'Menor a 90'					
								WHEN :p_plazoDias = 2 THEN 'Mayor a 90'
								ELSE 'TODOS'
						END,
					"Variable_Cobertura" = :porCobertura;



			UPDATE	#Tmp_Cobertura					
			SET		"PorcentajeCobertura" =
						CASE 	WHEN "NombreIndicador" = 'BANCO' 
								THEN ("TramoCobertura" * "TramoCobertura")
								ELSE "TramoCobertura"
						END;

			--* ----------------------------------------------------------
			--* PACTOS CALZADOS			
			--* ----------------------------------------------------------
			IF p_opcion = 2 THEN

				--* AGRUPO POR PACTOS CP/VP
				v_TblPactos = 
					SELECT	"Emisor", "Nemotecnico", "TipoReajuste", "FechaVencimientoInstrumento", "IdClasificacionTipoInstrumento", "CC", 
							SUM(	CASE WHEN "IdClasificacionTipoInstrumento" = 'IF' AND "EsFungible" = 'S' THEN "Cantidad" * "ValorRescate" 
										 ELSE "Cantidad"
									END 
								) AS "NOMINAL" 
					FROM	#Tmp_Cobertura
					WHERE	"CC" IN ('VP', 'CP')
					GROUP BY
							"Emisor", "Nemotecnico", "TipoReajuste", "FechaVencimientoInstrumento", "IdClasificacionTipoInstrumento", "CC";

				--* EN ESTA TABLA QUEDAN TODOS LOS PACTOS QUE ESTAN CALZADOS
				v_TblCalse =
					SELECT	CP.*
					FROM	:v_TblPactos CP
					WHERE	CP."CC" = 'CP'
							AND ( CP."Emisor", CP."Nemotecnico", CP."TipoReajuste", CP."FechaVencimientoInstrumento", CP."IdClasificacionTipoInstrumento", TRIM(CP."NOMINAL") ) 
							IN ( SELECT VP."Emisor", VP."Nemotecnico", VP."TipoReajuste", VP."FechaVencimientoInstrumento", VP."IdClasificacionTipoInstrumento", TRIM(VP."NOMINAL")
								 FROM :v_TblPactos VP WHERE VP."CC" = 'VP' );

				--* SACO TODOS LOS REGISTROS QUE NO ESTAN OK
				DELETE FROM #Tmp_Cobertura TMP
				WHERE	( TMP."Emisor", TMP."Nemotecnico", TMP."TipoReajuste", TMP."FechaVencimientoInstrumento", TMP."IdClasificacionTipoInstrumento" ) NOT IN 
						( 	SELECT 	PAC."Emisor", PAC."Nemotecnico", PAC."TipoReajuste", PAC."FechaVencimientoInstrumento", PAC."IdClasificacionTipoInstrumento" 
							FROM	:v_TblCalse PAC );


			END IF;	


		END IF; --IF varExisteOperacion != 0  THEN


		--* VALIDAR SI EXISTEN OPERACIONES (SI ES PACTO Y NO CALZO NADA)
		SELECT 	COUNT (1) 
		INTO 	varExisteOperacion
		FROM 	#Tmp_Cobertura;

		--* ---------------------------------------------------------------
		IF varExisteOperacion != 0  THEN
			INSERT INTO "BSCL.Mesa.BD::MRF.CoberturaPatrimonial"  
				("IdCoberturaPatrimonial",
				"FolioComprobante",
				"IdTipoOperacion",
				"IdInstrumento",
				"IdTipoInstrumento",
				"TipoInstrumento",
				"ValorMercado",
				"TIR",
				"Cartera",
				"CC",
				"CC1",
				"FechaVcto",
				"Cantidad",
				"Emisor",
				"Nemotecnico",
				"Corredor",
				"Variable_%Cobertura",
				"IdMonedaInstrumento",
				"CLA_RIE",
				"FechaVencimientoInstrumento",
				"IdIndicador",
				"NombreIndicador",
				"Plazo",
				"PlazoPacto",
				"TramoCobertura",
				"PorcentajeCobertura",
				"MontoCobertura",
				"Clasificacion",
				"CatRie",
				"CatRiePactos",
				"Tipo Amo",
				"TieneCupon",
				"Fecha",
				"IdOpcionSeleccionada",
				"OpcionSeleccionada",
				"IdTipoValorizacion",
				"TipoValorizacion",
				"IdOpcPlazoDias",
				"OpcPlazoDias",
				"Observaciones"
				,"IdEmpresa"
				,"TipoEmisor"
				,"TipoCartera" )  
			SELECT	"BSCL.Mesa.BD.Secuencias::MRF.IdCoberturaPatrimonial".NEXTVAL AS "IdCoberturaPatrimonial",
					COALESCE("FolioCartera",0),
					COALESCE("IdTipoOperacion",''),
					COALESCE("IdInstrumento",0),
					COALESCE("IdTipoInstrumento",''),
					LEFT(COALESCE("TipoInstrumento",''), 40),
					COALESCE("ValorMercado",0),
					COALESCE("TIR",0),
					COALESCE("Cartera",''),
					COALESCE("CC",''),
					COALESCE("CC1",''),
					
					CAST(COALESCE("FechaVcto",'1900-01-01') AS NVARCHAR(10)),
					
					COALESCE("Cantidad",0),
					COALESCE("Emisor",''),
					COALESCE("Nemotecnico",''),
					COALESCE("Corredor",''),
					COALESCE("Variable_Cobertura",''),
					COALESCE("IdMonedaInstrumento",''),
					COALESCE("CLA_RIE",''),
					COALESCE("FechaVencimientoInstrumento",'1900-01-01'),
					COALESCE("IdIndicador",''),
					COALESCE("NombreIndicador",''),
					COALESCE("Plazo",0),
					COALESCE("PlazoPacto",0),
					COALESCE("TramoCobertura",0),
					COALESCE("PorcentajeCobertura",0),
					CEIL(COALESCE(("ValorMercado" * "PorcentajeCobertura"/100),0)) AS "MontoCobertura",
					COALESCE("Clasificacion",''),
					COALESCE("CatRie",''),
					COALESCE("CatRiePactos",''),
					COALESCE("Tipo_Amo",0),
					COALESCE("TieneCupon",''),
					COALESCE("Fecha",'1900-01-01'),
					COALESCE("IdOpcionSeleccionada",0),
					COALESCE("OpcionSeleccionada",''),
					COALESCE("IdTipoValorizacion",0),
					COALESCE("TipoValorizacion",''),
					COALESCE("IdOpcPlazoDias",0),
					COALESCE("OpcPlazoDias",''),
					COALESCE("Observaciones",''),
					:p_IdEmpresa,
					"TipoEmisor",
					"TipoCartera"
			FROM 	#Tmp_Cobertura;


			v_CoberturtaDetalle = 
				SELECT 	
				COALESCE(Mon."CantidadDecimales", 0) AS "CantidadDecimales",COALESCE(Mon."TasaDecimales",0) AS "TasaDecimales",Cober."IdCoberturaPatrimonial",Cober."FolioComprobante",Cober."IdTipoOperacion",Cober."IdInstrumento",Cober."IdTipoInstrumento",Cober."TipoInstrumento",Cober."ValorMercado"
				,Cober."TIR",Cober."Cartera",Cober."CC",Cober."CC1",Cober."FechaVcto",Cober."Cantidad",Cober."Emisor",Cober."Nemotecnico",Cober."Corredor",Cober."Variable_%Cobertura" AS "Variable_Cobertura"
				,Cober."IdMonedaInstrumento",Cober."CLA_RIE",Cober."FechaVencimientoInstrumento",Cober."IdIndicador",Cober."NombreIndicador",Cober."Plazo",Cober."PlazoPacto",
				Cober."TramoCobertura",Cober."PorcentajeCobertura",Cober."MontoCobertura",Cober."Clasificacion",Cober."CatRie",Cober."CatRiePactos",Cober."Tipo Amo" AS "Tipo_Amo",Cober."TieneCupon",Cober."Fecha"
				,Cober."IdOpcionSeleccionada",Cober."OpcionSeleccionada",Cober."IdTipoValorizacion",Cober."TipoValorizacion",Cober."IdOpcPlazoDias",Cober."OpcPlazoDias",Cober."Observaciones",Cober."IdEmpresa",Cober."TipoEmisor",
				Cober."TipoCartera", :p_NombreMesa AS "NombreMesa"
				FROM 	"BSCL.Mesa.BD::MRF.CoberturaPatrimonial" AS Cober
				LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS
					ON INS."IdInstrumento" = Cober."IdInstrumento"
				LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" AS Mon ON (Mon."IdUsoMonedaFormato"='GEN' 
				AND Mon."IdMoneda" = Cober."IdMonedaInstrumento" AND Mon."IdTipoOperacion" = '' AND Mon."IdTipoInstrumento" = INS."IdTipoInstrumento")
				WHERE 	"Fecha" = :p_FechaOperacion
						AND Cober."IdOpcionSeleccionada" = :p_opcion
						AND	Cober."IdTipoValorizacion" = :p_TipoValor;
								 
		ELSE --* IF (varExisteOperacion != 0 AND varExistePorCobertura != 0) THEN

			v_CoberturtaDetalle = 
				SELECT 	0 AS "CantidadDecimales" --Formato Decimales Cantidad
						,0 AS "TasaDecimales"
						,0 AS "IdCoberturaPatrimonial"
						,0   AS "FolioComprobante"
						,' ' AS "IdTipoOperacion"
						,0   AS "IdInstrumento"--"SubProducto"
						,' ' AS "IdTipoInstrumento"
						,' ' AS "TipoInstrumento"--"SubProducto"
						,0   AS "ValorMercado"
						,0   AS "TIR"	
						,' ' AS "Cartera"		
						,' ' AS "CC"
						,' ' AS "CC1"	
						,' ' AS "FechaVcto"
						,0	 AS "Cantidad"
						,' ' AS "Emisor"
						,' ' AS "Nemotecnico"
						,' ' AS "Corredor"
						,' ' AS "Variable_Cobertura"
						,' ' AS "IdMonedaInstrumento"
						,' ' AS "CLA_RIE"
						,' ' AS "FechaVencimientoInstrumento"
						,' ' AS "IdIndicador"
						,' ' AS "NombreIndicador"
						,0 	 AS "Plazo" 
						,0	 AS "PlazoPacto"
						,0   AS "TramoCobertura"
						,0   AS "PorcentajeCobertura"
						,0	 AS "MontoCobertura"
						,' ' AS "Clasificacion"
						,' ' AS "CatRie"
						,''  AS "CatRiePactos"
						,0 AS "Tipo_Amo"
						,' ' AS "TieneCupon"
						,:p_FechaOperacion				AS "Fecha"
						,:p_opcion    					AS "IdOpcionSeleccionada"
						,CASE	
							WHEN :p_opcion = 1 THEN 'POR CARTERAS'
							ELSE'POR PACTOS CALZADOS'
						END AS "OpcionSeleccionada"
						,:p_TipoValor					AS "IdTipoValorizacion"
						,CASE	
							WHEN :p_TipoValor = 1 THEN 'ART.514'
							ELSE 'A MERCADO'
						END AS "TipoValorizacion"
						,:p_plazoDias					AS "IdOpcPlazoDias"
						,CASE 
							WHEN :p_plazoDias = 1 THEN 'Menor a 90'					
							WHEN :p_plazoDias = 2 THEN 'Mayor a 90'
							ELSE 'TODOS'
						END AS "OpcPlazoDias"
						,CASE WHEN varExisteOperacion = 0 THEN 'NO EXISTEN OPERACIONES'
							ELSE 'NO EXISTE PARAMETRO PROCENTAJE DE COBERTURA CONFIGURADO EN PARAMETROS MESA'	
							END	AS "Observaciones"
						,:p_IdEmpresa as "IdEmpresa"
						,'' AS "TipoEmisor",
						'' AS "TipoCartera",
						'' AS "NombreMesa"
				FROM DUMMY; 
		END IF; --* IF (varExisteOperacion != 0 AND varExistePorCobertura != 0) THEN


		BEGIN
		
			p_out_CoberturaPatrimonialDetalle = 
			SELECT  "CantidadDecimales","TasaDecimales", "IdCoberturaPatrimonial", "FolioComprobante", "IdTipoOperacion", "IdInstrumento", "IdTipoInstrumento",
		 			"TipoInstrumento", "ValorMercado", "TIR", "Cartera", "CC", "CC1", "FechaVcto", "Cantidad", "Emisor", "Nemotecnico", "Corredor", "Variable_Cobertura",
		 			"IdMonedaInstrumento", "CLA_RIE", "FechaVencimientoInstrumento", "IdIndicador", "NombreIndicador", "Plazo", "PlazoPacto",
		 			"TramoCobertura", "PorcentajeCobertura", "MontoCobertura", "Clasificacion", "CatRie", "CatRiePactos", "Tipo_Amo",
		 			"TieneCupon", "Fecha", "IdOpcionSeleccionada",
		 			"OpcionSeleccionada", "IdTipoValorizacion", "TipoValorizacion", "IdOpcPlazoDias", "OpcPlazoDias", "Observaciones", "IdEmpresa", "TipoEmisor", "TipoCartera",
		 			"NombreMesa"
			FROM :v_CoberturtaDetalle; 
			
			p_out_ErroresContador := 0;
			
				
			DROP TABLE #Tmp_Cobertura;
			
			
			
		END;
	 END;		
 END;