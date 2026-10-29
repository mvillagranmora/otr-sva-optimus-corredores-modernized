SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarCustodiaMovimientosHistoricos";

CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarCustodiaMovimientosHistoricos" AS 
SELECT 
	   CUS.RUT_CLI								AS "DocumentoIdentidad"
      ,CLI."CodigoPortafolio"					AS "CodigoPortafolio"	   
	  ,CLI."IdCliente"							AS "IdCliente"
	  ,CLI."IdPortafolio"						AS "IdPortafolio"
	  ,CLI."NombreCompleto"						AS "NombreCliente"
      ,TO_DATE(CUS.FEC_MVTO, 'YYYY-MM-DD')  	AS "FechaOperacion"
      ,TO_DATE(CUS.FEC_LIQUID, 'YYYY-MM-DD')  	AS "FechaLiquidacion"      
	  ,CASE WHEN LEFT(PL.codigo,2) 				= 'IG' THEN 'INGRESO'
	 		WHEN LEFT(PL.codigo,2) 				= 'RG' THEN 'EGRESO'
       ELSE
       		CASE WHEN TRIM(CUS.TIPO_COMP_ADJ)	= 'IT' OR TRIM(CUS.TIPO_COMP_ADJ) = 'ET' THEN
                 	CASE WHEN PL.FPT_CAN_CUST 	= 1   THEN 'INGRESO' ELSE 'EGRESO' END
            	 WHEN TRIM(CUS.TIPO_COMP_ADJ)	= 'MA' THEN -- 
                	CASE WHEN TRIM(CUS.IND_CUST_CTDO) = 'E' THEN 'INGRESO' ELSE 'EGRESO' END
            	 WHEN TRIM(CUS.TIPO_COMP_ADJ) 	= 'ND' THEN -- Notas de crédito
                	CASE WHEN TRIM(CUS.IND_CUST_CTDO) = 'E' THEN 'INGRESO' ELSE 'EGRESO' END
            ELSE
                CASE WHEN PL.FPT_CAN_CUST = 1 THEN 'INGRESO' ELSE 'EGRESO' END
       		END
	   END 										AS "Accion"
	  ,'2'										AS "TipoRegistro"
	  ,CASE WHEN TRIM(CUS.COD_MOVTO) = 'EOSA' AND CUS.MERCADO = 'AC' THEN 'INGOSA' 		-- Emision OSA
	  		WHEN TRIM(CUS.COD_MOVTO) = 'EMPT' AND CUS.MERCADO = 'AC' THEN 'SUSOSA' 		-- Emision Pagada Titulos
	  		WHEN TRIM(CUS.COD_MOVTO) = 'CIRV' THEN										-- Compra RV
	  			CASE WHEN CUS.MERCADO = 'AC' THEN										-- (ACN)
	  					CASE WHEN INS."IdMoneda" = 'CLP' THEN 'COMACN' ELSE 'COMACX' END	
	  			     WHEN CUS.MERCADO = 'CF' THEN										-- (CFI)
	  					CASE WHEN INS."IdMoneda" = 'CLP' THEN 'COMCFN' ELSE 'COMCFX' END
	  				 WHEN CUS.MERCADO = 'EN' THEN										-- (ETN)
	  					CASE WHEN INS."IdMoneda" = 'CLP' THEN 'COMETN' ELSE 'COMETX' END
	  			END		
	  		WHEN TRIM(CUS.COD_MOVTO) = 'VIRV' THEN										-- Venta RV
	  			CASE WHEN CUS.MERCADO = 'AC' THEN										-- (ACN)
	  					CASE WHEN INS."IdMoneda" = 'CLP' THEN 'VENACN' ELSE 'VENACX' END	
	  				 WHEN CUS.MERCADO = 'CF' THEN										-- (CFI)
	  					CASE WHEN INS."IdMoneda" = 'CLP' THEN 'VENCFI' ELSE 'VENCFX' END	
	  				 WHEN CUS.MERCADO = 'EN' THEN										-- (ETN)
	  					CASE WHEN INS."IdMoneda" = 'CLP' THEN 'VENETN' ELSE 'VENETX' END	
	  			END
	  		WHEN TRIM(CUS.COD_MOVTO) = 'CIET' THEN 										-- Compra ETF
	  			CASE WHEN INS."IdMoneda" = 'CLP' THEN 'COMETN' ELSE 'COMETX' END
	  		WHEN TRIM(CUS.COD_MOVTO) = 'VIET' THEN 										-- Venta ETF
	  			CASE WHEN INS."IdMoneda" = 'CLP' THEN 'VENETN' ELSE 'VENETX' END
	  		WHEN TRIM(CUS.COD_MOVTO) = 'IIRV' AND CUS.MERCADO = 'AC' THEN 'INGTIT' 		-- Ingreso RV
	  		WHEN TRIM(CUS.COD_MOVTO) = 'RIRV' AND CUS.MERCADO = 'AC' THEN 'RETTIT' 		-- Retiro RV	  		
	  		WHEN TRIM(CUS.COD_MOVTO) = 'NCRV' AND CUS.MERCADO = 'AC' THEN 'INGTIT' 		-- N.Credito Ingreso RV
	  		WHEN TRIM(CUS.COD_MOVTO) = 'NVRV' AND CUS.MERCADO = 'AC' THEN 'RETTIT' 		-- N.Credito Retiro RV
	  		WHEN TRIM(CUS.COD_MOVTO) = 'IGRV' AND CUS.MERCADO = 'AC' THEN 'INGGTA' 		-- Ingreso Garantias RV	  		
	  		WHEN TRIM(CUS.COD_MOVTO) = 'RGRV' AND CUS.MERCADO = 'AC' THEN 'RETGTA' 		-- Retiro Garantias RV
			WHEN TRIM(CUS.COD_MOVTO) = 'VITP' AND CUS.MERCADO = 'AC' THEN 'VENSIM' 		-- Venta TP
			WHEN TRIM(CUS.COD_MOVTO) = 'CITP' AND CUS.MERCADO = 'AC' THEN 'CENSIM' 		-- Compra TP
			WHEN TRIM(CUS.COD_MOVTO) = 'TVTP' AND CUS.MERCADO = 'AC' THEN 'VESIMV' 		-- Liquidacion Venta TP	
			WHEN TRIM(CUS.COD_MOVTO) = 'NVTP' AND CUS.MERCADO = 'AC' THEN 'RETTIT' 		-- N.Credito Retiro TP
			
	  		WHEN TRIM(CUS.COD_MOVTO) = 'CIET' THEN 										-- Compra ETF
	  			CASE WHEN INS."IdMoneda" = 'CLP' THEN 'COMETN' ELSE 'COMETX' END
	  		WHEN TRIM(CUS.COD_MOVTO) = 'VIET' THEN 										-- Venta ETF
	  			CASE WHEN INS."IdMoneda" = 'CLP' THEN 'VENETN' ELSE 'VENETX' END			

	  		WHEN TRIM(CUS.COD_MOVTO) = 'CIIF' AND CUS.MERCADO = 'IF' THEN 'COMIIF' 		-- Compra IF
	  		WHEN TRIM(CUS.COD_MOVTO) = 'VIIF' AND CUS.MERCADO = 'IF' THEN 'VENIIF' 		-- Venta IF
	  		WHEN TRIM(CUS.COD_MOVTO) = 'IIIF' AND CUS.MERCADO = 'IF' THEN 'INGTIT' 		-- Ingreso IF
	  		WHEN TRIM(CUS.COD_MOVTO) = 'RIIF' AND CUS.MERCADO = 'IF' THEN 'RETTIT' 		-- Retiro IF  		
	  		WHEN TRIM(CUS.COD_MOVTO) = 'NCIF' AND CUS.MERCADO = 'IF' THEN 'INGTIT' 		-- N.Credito Ingreso IF		
	  		WHEN TRIM(CUS.COD_MOVTO) = 'NVIF' AND CUS.MERCADO = 'IF' THEN 'RETTIT' 		-- N.Credito Retiro IF
	  		WHEN TRIM(CUS.COD_MOVTO) = 'VTIF' AND CUS.MERCADO = 'IF' THEN 'RETTIT' 		-- Vencimiento IF
	  		WHEN TRIM(CUS.COD_MOVTO) = 'CRIC' AND CUS.MERCADO = 'IF' THEN 'COMPAC' 		-- Compra IF Retrov Custodia	  		
	  		WHEN TRIM(CUS.COD_MOVTO) = 'VRIC' AND CUS.MERCADO = 'IF' THEN 'VENPAC' 		-- Venta IF Retroc Custodia
	  		WHEN TRIM(CUS.COD_MOVTO) = 'CFIC' AND CUS.MERCADO = 'IF' THEN 'COMIIF' 		-- Compra IF FR Custodia (Fuera de Rueda)	  		
	  		WHEN TRIM(CUS.COD_MOVTO) = 'VFIC' AND CUS.MERCADO = 'IF' THEN 'VENIIF' 		-- Venta IF FR Custodia (Fuera de Rueda)
	  		WHEN TRIM(CUS.COD_MOVTO) = 'RCIC' AND CUS.MERCADO = 'IF' THEN 'COMPAC' 		-- Retroc IF Custodia
	  		WHEN TRIM(CUS.COD_MOVTO) = 'RVIC' AND CUS.MERCADO = 'IF' THEN 'VENPAC' 		-- Retrov IF Custodia
	  		WHEN TRIM(CUS.COD_MOVTO) = 'RGIF' AND CUS.MERCADO = 'IF' THEN 'RETGTA' 		-- Retiro Garantias IF
	  		WHEN TRIM(CUS.COD_MOVTO) = 'IGIF' AND CUS.MERCADO = 'IF' THEN 'INGGTA' 		-- Ingreso Garantias IF
	  		
	  		WHEN TRIM(CUS.COD_MOVTO) = 'CIRF' AND CUS.MERCADO = 'RF' THEN 'COMRF' 		-- Compra RF
	  		WHEN TRIM(CUS.COD_MOVTO) = 'VIRF' AND CUS.MERCADO = 'RF' THEN 'VENRF' 		-- Venta RF
	  		WHEN TRIM(CUS.COD_MOVTO) = 'IIRF' AND CUS.MERCADO = 'RF' THEN 'INGTIT' 		-- Ingreso RF
	  		WHEN TRIM(CUS.COD_MOVTO) = 'RIRF' AND CUS.MERCADO = 'RF' THEN 'RETTIT' 		-- Retiro RF  		
	  		WHEN TRIM(CUS.COD_MOVTO) = 'NCRF' AND CUS.MERCADO = 'RF' THEN 'INGTIT' 		-- N.Credito Ingreso RF		
	  		WHEN TRIM(CUS.COD_MOVTO) = 'NVRF' AND CUS.MERCADO = 'RF' THEN 'RETTIT' 		-- N.Credito Retiro RF
	  		WHEN TRIM(CUS.COD_MOVTO) = 'VRRC' AND CUS.MERCADO = 'RF' THEN 'VENPAC' 		-- Venta RF Retroc Custodia
	  		WHEN TRIM(CUS.COD_MOVTO) = 'CRRC' AND CUS.MERCADO = 'RF' THEN 'COMPAC' 		-- Compra RF Retrov Custodia	  		
	  		WHEN TRIM(CUS.COD_MOVTO) = 'CFRC' AND CUS.MERCADO = 'RF' THEN 'COMRF' 		-- Compra RF FR Custodia (Fuera de Rueda)	  		
	  		WHEN TRIM(CUS.COD_MOVTO) = 'VFRC' AND CUS.MERCADO = 'RF' THEN 'VENRF' 		-- Venta RF FR Custodia (Fuera de Rueda)	  		
	  		WHEN TRIM(CUS.COD_MOVTO) = 'RCRC' AND CUS.MERCADO = 'RF' THEN 'COMPAC' 		-- Retroc RF Custodia
	  		WHEN TRIM(CUS.COD_MOVTO) = 'RVRC' AND CUS.MERCADO = 'RF' THEN 'VENPAC' 		-- Retrov RF Custodia	  		
	  		WHEN TRIM(CUS.COD_MOVTO) = 'VTRF' AND CUS.MERCADO = 'RF' THEN 'RETTIT' 		-- Vencimiento RF
	  		WHEN TRIM(CUS.COD_MOVTO) = 'RTSL' AND CUS.MERCADO = 'RF' THEN 'RETTIT' 		-- Retiro RF Por Sorteo Letras
	  		WHEN TRIM(CUS.COD_MOVTO) = 'RGRF' AND CUS.MERCADO = 'RF' THEN 'RETGTA' 		-- Retiro Garantias IF
	  		WHEN TRIM(CUS.COD_MOVTO) = 'IGRF' AND CUS.MERCADO = 'RF' THEN 'INGGTA' 		-- Ingreso Garantias IF	  		
	  		
	  		WHEN TRIM(CUS.COD_MOVTO) = 'CIFM' AND CUS.MERCADO = 'FM' THEN 'APORFM' 		-- Aporte Fondos Mutuos
	  		WHEN TRIM(CUS.COD_MOVTO) = 'VIFM' AND CUS.MERCADO = 'FM' THEN 'RESCFM' 		-- Retiro Fondo Mutuo
	  			  		
	  		ELSE '' END							AS "IdTipoOperacion"
	  ,TRIM(CUS.HOM_CON_DESCRIPCION)			AS "NombreConcepto"
	  ,CASE WHEN "IdClasificacionTipoInstrumento" = 'IF' AND IND_FUNGIBILIDAD = 'S' THEN 
	  		CUS.VAL_RESC ELSE CUS.CANTIDAD END 	AS "Cantidad"							-- Cantidad y CantidadNeg
	  ,CASE WHEN "IdClasificacionTipoInstrumento" IN ('RF','IF') THEN 
	  		CUS.TASA ELSE CUS.PRECIO END 		AS "PrecioTir"
	  ,CUS.PRECIO								AS "Precio"
	  ,CUS.MONTO								AS "Monto"
	  ,CUS.TASA									AS "Tasa"
	  ,INS."IdInstrumento"						AS "IdInstrumento"	  		
	  ,TRIM(CUS.NEMO)							AS "Nemotecnico"
	  ,INS."ISIN"								AS "ISIN"
	  ,INS."IdEmisor"							AS "IdEmisor"
	  ,INS."IdClasificacionTipoInstrumento"		AS "IdClasificacionTipoInstrumento"
	  ,INS."IdTipoInstrumento"					AS "IdTipoInstrumento"
	  ,CUS.COD_BOVEDA							AS "IdBoveda"
	  ,''										AS "NumeroCuentaBoveda"	   
	  ,'CUS'									AS "IdTipoCustodia"
	  ,'CUSTODIA'								AS "NombreTipoCustodia"
	  ,TO_DATE(CUS.FEC_MVTO, 'YYYY-MM-DD') 		AS "FechaComprobante"
	  ,CASE TRIM(CUS.TIPO_COMP_ADJ)
	  		WHEN 'FD' THEN 'FAE'
	  		WHEN 'ND' THEN 
				CASE WHEN TRIM(CUS.COD_MOVTO) IN ('NRVC', 'NRVV') THEN 'NCE' ELSE 'NEE' END
	  		WHEN 'FX' THEN 'FEE'
	  		WHEN 'FM' THEN 'FFMM'
	  		WHEN 'FT' THEN 'FOE'
	  		WHEN 'IT' THEN 'CUS'
	  		WHEN 'ET' THEN 'CUS'	  		
	  		WHEN 'MA' THEN 'MES'
	  		ELSE ''   END						AS "IdTipoComprobante"
	  ,CAST(CUS.FOLIO_COMPROBANTE AS BIGINT)	AS "FolioComprobante"
	  ,CAST(SEC_MOVTO AS BIGINT)				AS "FolioCartera"
	  ,COALESCE(TO_TIMESTAMP(CUS.FEC_ING_MVTO, 'YYYY-MM-DD'), TO_TIMESTAMP('0001-01-01', 'YYYY-MM-DD')) AS "FechaCertificacion"
 FROM "BSCL.Sebra.BD::MIG.TBCTMVTO" CUS
 LEFT JOIN "BSCL.Sebra.BD::MIG.TBPLTMOV" AS PL 	ON PL.CODIGO = CUS.COD_MOVTO AND PL.TIPO_REG = 'CM'
INNER JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" AS CLI 
   ON (TRIM(CUS.RUT_CLI) = TRIM(CLI."DocumentoIdentidad") AND TRIM(CUS.SEC_RUT_CLI) = TRIM(CLI."CodigoPortafolio"))
INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS INS ON (INS."Nemotecnico" = CUS.NEMO)
WHERE EST_MOVTO 								!= 'I'
AND   CUS.COD_BOVEDA 							!= 'TOT'
AND   IND_ANULADO 								= 'I'
AND   CUS.IND_TIT_CCTE 							= 'T'
WITH READ ONLY;

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarCustodiaMovimientosHistoricos" TO BCS_DESARROLLADOR WITH GRANT OPTION;
