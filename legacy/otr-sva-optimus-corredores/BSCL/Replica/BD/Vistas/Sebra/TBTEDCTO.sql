SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.TBTEDCTO"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.TBTEDCTO" AS

		
		SELECT 
			CASE IEFP."IdMoneda"
				WHEN 'CLP' THEN 'CP' 
				WHEN 'USD' THEN 'CD'
				ELSE 'CE'
			END AS TIPO_CAJA
			, RPAD(CE."DocumentoIdentidad",11) AS RUT_EMPRESA
			, RPAD(CASE IEFP."IdMoneda"
				WHEN 'CLP' THEN '$ '
				WHEN 'USD' THEN 'US'
				WHEN 'EUR' THEN 'EU'
				ELSE ''
			END,2) AS TIPO_MON
			, RPAD('',2) AS TIPO_DOCUM
			, IEFP."IdComprobanteFormaDePago" AS SEC_DOCUM
			, CASE IEFP."IdEstado" 
				WHEN 'ING' THEN 'IN'
				WHEN 'ANU' THEN 'AN'
				ELSE 'DP'
			 END AS EST_DOCUM
			, CASE C."IdEstado" 
				WHEN 'ANU' THEN 'A'
				WHEN 'LIQ' THEN 'C'
				ELSE 'I'
			END AS  EST_CMBTE
			, CASE WHEN IEFP."IdTipoMovimiento" = 'MOVING' THEN 'I' ELSE 'E' END AS IND_ING_EGR
			, RPAD('',4) AS COD_CAJA
			, COALESCE(bancocorredora."CodigoBanco", '') AS COD_BCO_DOCUM
			,TO_NVARCHAR( C."FechaLiquidacion", 'YYYY-MM-DD')  AS FEC_LIQUID
			,CASE 
				WHEN C."IdTipoMovimiento" = 'MOVING' AND C."IdTipoPersona" = 'CLI' THEN
					CASE C."IdMoneda"
						WHEN 'CLP' THEN 'OI'
						WHEN 'USD' THEN 'ID'
						ELSE ''
					END
				WHEN C."IdTipoMovimiento" = 'MOVEGR' AND C."IdTipoPersona" = 'CLI' THEN
					CASE C."IdMoneda"
						WHEN 'CLP' THEN 'OP'
						WHEN 'USD' THEN 'ED'
						ELSE ''
					END
				WHEN C."IdTipoMovimiento" = 'MOVING' AND C."IdTipoPersona" IN ('CNT','PER','PRO','EMP')  AND C."IdPersona" != CE."IdPersona" THEN
					CASE C."IdMoneda"
						WHEN 'CLP' THEN 'OI'
						WHEN 'USD' THEN 'ID'
						ELSE ''
					END
				WHEN C."IdTipoMovimiento" = 'MOVING' AND C."IdTipoPersona" IN ('CNT','PER','PRO','EMP') AND C."IdPersona" = CE."IdPersona" THEN 'OI'
				WHEN C."IdTipoMovimiento" = 'MOVEGR' AND C."IdTipoPersona" IN ('CNT','PER','PRO','EMP') AND C."IdPersona" != CE."IdPersona" THEN
					CASE C."IdMoneda"
						WHEN 'CLP' THEN 'OP'
						WHEN 'USD' THEN 'ED'
						ELSE ''
					END
				WHEN C."IdTipoMovimiento" = 'MOVEGR' AND C."IdTipoPersona" IN ('CNT','PER','PRO','EMP') AND C."IdPersona" = CE."IdPersona" THEN 'OP'
			END AS TIPO_CMBTE
			
			, COALESCE(C."IdComprobante", 0) AS FOLIO_CMBTE
			, 'N' AS IND_MARCA_ESP
			, LEFT(COALESCE(IEFP."NombreCuentaBancariaCorredora",''),10) AS ALIAS_BANCO
			, 0 AS SEC_ENVIO
			, 0 AS SEC_DEVOL
			, COALESCE(IEFP."NumeroCuentaBancariaCorredora",'') AS CTA_BCO_DOCUM
			, IEFP."Monto" AS MONTO_DOCUM
			, CAST(C."NombrePersonaTitularPago" AS VARCHAR(40)) AS EMITIDO_A
			, COALESCE(TO_NVARCHAR(C."FechaMovimiento" , 'YYYY-MM-DD') , '1900-01-01') AS FEC_CMBTE
			, C."FechaMovimiento" AS FEC_DOCUM
			, 'M' AS GEN_DOCUM --???
			, 'N' AS IND_CHEQUE
			, IEFP."NumeroDocumentoSocio" AS NUM_DEPOSITO
			, IEFP."NumeroDocumentoSocio" AS NUM_DOCUM
			, 0 AS PLAZO
			, IEFP."NumeroSerieSocio" AS SERIE_DOCUM
			, '' AS TIPO_DEPOSITO
			,IEFP."IdMedioDePago"
			, CAST(CASE TRIM(IEFP."IdMedioDePago") 
					WHEN 'VVI' THEN 'VV'
					WHEN 'VVE' THEN 'VE'
					WHEN 'EFE' THEN 'EF'
					WHEN 'EBA' THEN 'BA'
					WHEN 'BDE' THEN 'BD'
					WHEN 'CHE' THEN 'CH'
					WHEN 'CIN' THEN 'AC'
					WHEN 'TRA' THEN 'TB'
					WHEN 'TLR' THEN 'TH'
					WHEN 'TSW' THEN 'TH'
					WHEN 'TDP' THEN 'TH'
					ELSE '  '
				END AS VARCHAR(2)) AS FPA_ID
			, TO_NVARCHAR(IEFP."FechaDocumentoPagoCorredora", 'YYYY-MM-DD') AS FECHA_PAGO
			, 0 AS NUM_VALUTA
			, '' AS COD_SUC
			, CASE WHEN IEFP."IdTipoMovimiento" = 'MOVEGR' THEN LEFT(COALESCE(B."CodigoBanco", ''),4) ELSE '' END AS COD_BCO_PAGO
			, CASE WHEN IEFP."IdTipoMovimiento" = 'MOVEGR' THEN IEFP."NumeroCuentaBancariaSocio" ELSE '' END AS CTA_BCO_PAGO
			, '' AS IND_TRASPASO
			
		FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" IEFP
		LEFT JOIN "BSCL.Tesoreria.BD::TES.Comprobante" C ON C."IdComprobante" = IEFP."IdComprobante"
		INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" CE ON CE."IdEmpresa"  = C."IdEmpresa"
		LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" B ON B."IdBanco" = IEFP."IdBancoSocio"
		LEFT JOIN "BSCL.Tesoreria.BD::TES.CuentaBanco" cuentacorredora on IEFP."IdCuentaBancariaCorredora" = cuentacorredora."IdCuentaBanco"
		LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" bancocorredora ON bancocorredora ."IdBanco" = cuentacorredora."IdBanco"
		WHERE IEFP."IdEstado" != 'MOD'

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.TBTEDCTO" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

