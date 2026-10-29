SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCTDTCF"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCTDTCF" AS

		SELECT
			 DCOM."IdComprobante" as "IdComprobanteDetalle"
			 ,CASE  
			 	WHEN DCOM."NemotecnicoInstrumento" = 'INTERB' AND DCOM."IdMonedaSpot" = 'USD' THEN 'DOLAR'
				WHEN DCOM."NemotecnicoInstrumento" = 'INTERB' AND DCOM."IdMonedaSpot" = 'EUR' THEN 'EURO'
				WHEN DCOM."NemotecnicoInstrumento" = 'INTERB' THEN 'DOLAR'
			 	WHEN DCOM."NemotecnicoInstrumento" = 'DISTRI' AND DCOM."IdMonedaSpot" = 'USD' THEN 'DOLAR'
				WHEN DCOM."NemotecnicoInstrumento" = 'DISTRI' AND DCOM."IdMonedaSpot" = 'EUR' THEN 'EURO'
				WHEN DCOM."NemotecnicoInstrumento" = 'DISTRI' THEN 'DOLAR'
				WHEN DCOM."NemotecnicoInstrumento" = 'SIMULTÁNEA VENTA' THEN DCOM."NemotecnicoSubyacente"
				WHEN DCOM."NemotecnicoInstrumento" = 'SIMULTÁNEA COMPRA' THEN DCOM."NemotecnicoSubyacente"
				ELSE LEFT(DCOM."NemotecnicoInstrumento",16) END 
				AS NEMO
			 , "BSCL.Replica.BD.Servicios::MapeoTIPO_COMP_FAC"(COM."IdTipoComprobante") AS TIPO_COMP_FAC	--SE DEBE HOMOLOGAR COM."IdTipoComprobante"	--char	2
			 ,COM."FolioComprobante" AS FOLIO_COMP_FAC						--decimal	0
			 , RANK() OVER (PARTITION BY COM."FolioComprobante" ORDER BY "FolioComprobante" , "IdComprobanteDetalle" ASC)    AS  LIN_COMP_FAC
			 , 0 SEC_CAR_INVER
			 ,CAST(DCOM."Cantidad" AS DECIMAL(14,2)) AS CANTIDAD	
			 ,"BSCL.Replica.BD.Servicios::MapeoCOD_MOVTO"(COM."IdTipoOperacion",COM."IdTipoProducto",COM."EsAPlazo",COM."IdFormaCustodia") AS COD_MOVTO    --CHAR(4)
			 ,COM."IdTipoOperacion"
			 --,LEFT(coalesce (COM."IdTipoOperacion", ''),4) as COD_MOVTO					
			 ,left(coalesce(TRAN."IdEmisor", ''), 10)  AS EMISOR						     --char	10
		     ,left(coalesce (COM."FechaComprobante", ''), 10) AS FEC_EMI						 --char	10
			 ,left(coalesce (TRAN."FechaVencimiento", DCOM."FechaLiquidacion"), 10)  AS FEC_VCTO					 --char	10
		     ,CASE WHEN DCOM."IdFormaCustodia" = 'CUS' THEN 'S' ELSE 'N' END  AS IND_TIT_CUS	 --char	1	
			 ,LEFT(COALESCE( hpr."ValorOrigen", 
				case when COM."IdTipoProducto"  = 'INSI' then 'AC'
						when COM."IdTipoProducto"  = 'INFU' then 'FU'
						when COM."IdTipoProducto"  = 'INEN' then 'EN'
						when COM."IdTipoProducto"  = 'INEX' then 'ET'
						when COM."IdTipoProducto"  = 'INRF' then 'RF'
						when COM."IdTipoProducto"  = 'MEME' then 'MO'
					 else  COM."IdTipoProducto"  END)
				
				, 2) AS "MERCADO" -- TIPO :char
			 ,CAST(DCOM."MontoComprobanteDetalle" AS DECIMAL(17,2)) AS MONTO			--decimal	0 			
		     ,DCOM."Precio" AS PRECIO								--decimal	0			
			 ,DCOM."Tasa" AS TASA									--decimal	0							                 --char	2
		     ,CASE WHEN TOPE."Accion" = 'COMPRA' THEN 'C'
				  WHEN TOPE."Accion" = 'VENTA'  THEN 'V'
				  ELSE ''
			  END AS TIPO_OPERAC	
			 ,COALESCE(CASE WHEN COM."IdTipoOperacion" IN ('COMSPT','VENSPT') THEN '' WHEN TRAN."IdTipoReajuste" = 'CLP' THEN 'PB' ELSE left(TRAN."IdTipoReajuste",2)  END, 'PB') AS TIPO_REAJ	 
		 	 ,CASE WHEN DCOM."ValorRescate" > 99999999999 THEN 99999999999 ELSE COALESCE (DCOM."ValorRescate", 0) END AS VAL_RESC	
		 	 ,lpad(left(DCOM."FolioTransaccion", 8),8)  AS FOLIO_TRANS
		 	 ,CASE WHEN coalesce (left(TRAN."IdTransaccionOd", 2), 0) = 0 then '' ELSE 'OD' END  AS IND_OD													--char	2  											
			 --,coalesce (LEFT(CAST(DCOM."IdBolsa" as VARCHAR), 3), '') AS IND_BOLSA			  --char	3  
			, '' AS IND_BOLSA
		     , DCOM."Tasa" AS TASA_EMISION				--REVISAR ESTE CAMPO
			, 0 AS SPREAD_TASA_EMISION										--decimal	0
			 ,'' AS MODALIDAD_PAGO											--char	1  forma de pago y homologada
			,'' AS PERIODICIDAD_PAGO										--char	1
		
			, '' AS  IND_DEST
			--,DCOM."FechaComprobanteDetalle" AS FECHA_EMISION											--char	10    fecha de la factura
			,'' AS FECHA_EMISION											--char	10    fecha de la factura
			, CASE WHEN COM."IdTipoOperacion" IN ('COMSPT','VENSPT') THEN 0 WHEN DCOM."IdMoneda" = 'CLP' THEN 1  ELSE DCOM."ValorParidad" END AS PARIDAD_MORIGEN											--decimal	0  validar
			, CASE WHEN COM."IdTipoOperacion" IN ('COMSPT','VENSPT') THEN 0 ELSE DCOM."Monto" END AS CAPTACION_MORIGEN										--decimal	0  monto nemoneda origen  (monto ern dolar)
			, CASE WHEN DCOM."CodigoIFRS" = 'CUS' THEN 1			
				  ELSE 0
			  END AS CODCATEGORIAIFRS											--char	16
			,CASE WHEN COM."IdTipoOperacion" IN ('COMSPT','VENSPT') THEN '' ELSE coalesce (TRAN."IdMoneda", 'CLP') END AS MON_MORIGEN	--char	3
		    ,''  AS RUEDA				--char	2  si fue operacion en rueda		
			,0 AS PRECIO_PROM_COMP_ANT										--decimal	0						
			,'N' AS MONTO_MODIF												--CHAR(1)
			,dcom."MayorMenorValor" AS MONTO_MMVALOR
			,dcom."MontoRetencionGananciaCap" AS MONTO_RETENCIONGC
			,COALESCE(CAST(INS."Nemotecnico" AS CHAR(16)), '') AS "NEMO_NEGOCIACION"
			,COALESCE(CAST(DCOM."Monto" AS DECIMAL(18,2)), 0.0) AS "CAPTACION_MX"
			,COALESCE(CAST(DCOM."Precio" AS DECIMAL(17,6)), 0.0)  AS "PRECIO_MX"
			--,CAST(COALESCE(ROUND(NULLIF(TRA."Monto", 0), 1)/ COALESCE(NULLIF(TRA."MontoRaiz", 0), 6), 1) AS DECIMAL(15,6)) AS "PARIDAD_MX"
			, 0 AS paridad_mX
			
		FROM "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" AS DCOM
		LEFT JOIN "BSCL.Orden.BD::ORD.Transaccion" AS TRAN ON  DCOM."IdTransaccion"  = TRAN."IdTransaccion" AND DCOM."IdTipoProducto" != 'MEME'
		INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TOPE ON DCOM."IdTipoOperacion" = TOPE."IdTipoOperacion"
		LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" as COM on DCOM."IdComprobante" = COM."IdComprobante"
		LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS hpr ON (hpr."TipoValor" = 'TipoInstrumentoSegunMercado' 
                                                              AND hpr."Entidad" = 'TipoInstrumentoSegunMercado' 
                                                              AND hpr."ValorDestino" = COM."IdTipoInstrumento" AND    hpr."ValorOrigen" NOT IN ('CM' , 'EN' , 'ET')  )
		LEFT JOIN "BSCL.Orden.BD::ORD.Transaccion" AS TRA ON (DCOM."IdTransaccion" = TRA."IdTransaccion") AND TRA."IdClasificacionTipoInstrumento" IN ('RV', 'RF', 'IF')
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON (TRA."IdInstrumentoNegociacion" = INS."IdInstrumento")
		

		WHERE COALESCE( COM."FolioComprobante" ,0) != 0 AND DCOM."NemotecnicoInstrumento" IS NOT NUll

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.TBCTDTCF" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
