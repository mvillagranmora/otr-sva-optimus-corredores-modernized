SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCTTRPT"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCTTRPT" AS
	
		WITH BASE AS (
			SELECT LEFT(CONF."Valor",7) AS "Periodo",
			SUB."Nemotecnico",
			"BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(IDENT."DocumentoIdentidad").result AS "DocumentoIdentidadPrestamista",
			POR."CodigoPortafolio" AS "CodigoPortafolioPrestamista",
			TRA."SecuenciaMovimiento",
			CAR."Cantidad",
			CASE WHEN CAR."EGRIdTipoOperacion" = 'ANTEMP' THEN TO_VARCHAR(CAR."EGRFechaOperacion", 'YYYY-MM-DD') ELSE '' END AS "FechaLiquidacionAnticipo",
			TO_VARCHAR(CAR."FechaVencimientoOperacion", 'YYYY-MM-DD') AS "FechaVencimientoOperacion" ,
			TO_VARCHAR(CAR."FechaOperacion", 'YYYY-MM-DD') AS "FechaOperacion",
			CAR."MontoCompra" AS "Monto",
			CASE CAR."IdEstado" WHEN 'VIG' THEN 'V' WHEN 'ANU' THEN 'A' WHEN 'LIQ' THEN 'L' WHEN 'ANT' THEN 'L' ELSE 'A' END AS "EstadoPrestamista",
			CAR."IdEstado" AS "IdEstadoCartera",
			"BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(IDENT_REL."DocumentoIdentidad").result AS "DocumentoIdentidadPrestatario",
			CAR."IdBolsa", 
			CASE CAR."EsCustodiaExterna" WHEN 'S' THEN 'CE' ELSE 'CL' END AS "TipoCustodia",
			CAR."FolioCartera",
			POR_REL."CodigoPortafolio" AS "CodigoPortafolioPrestatario",
			CAR."TasaCompra" AS "Tasa",
			CAR."PrecioCompra" AS "Precio",
			CAR."EGRIdTipoOperacion",
			CAR."EGRFechaOperacion",
			COALESCE(PREMIO."MontoMovimientoComprobante",0) AS "MontoPremio",
			COALESCE(PREMIO."IdMovimientoComprobante",0) AS "IdMovimientoComprobantePremio",
			CAR."EsPrimeraCartera"
			FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
			LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" CONF ON "CodigoParametro" = 'PLA.FechaSistema'
			LEFT JOIN "BSCL.Custodia.BD::CUS.Subyacente" SUB ON CAR."FolioCartera" = SUB."FolioCarteraRelacionada" AND SUB."EsPrimeraCartera" = 'S'
			LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CAR."IdCliente" = CLI."IdCliente"
			LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON CLI."IdPersona" = PER."IdPersona"
			LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad"
			LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdCliente" = CLI."IdCliente" AND por."IdPortafolio"  = car."IdPortafolio"
			LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" TRA ON CAR."IdMovimientoCarteraEnTransito" = TRA."IdMovimientoCarteraEnTransito"
			LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR_REL ON CAR_REL."FolioCartera" = CAR."FolioCarteraRelacionada" AND CAR_REL."EsPrimeraCartera" = 'S'
			LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI_REL ON CAR_REL."IdCliente" = CLI_REL."IdCliente"
			LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER_REL ON CLI_REL."IdPersona" = PER_REL."IdPersona"
			LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" IDENT_REL ON PER_REL."IdDocumentoIdentidad" = IDENT_REL."IdDocumentoIdentidad"
			LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR_REL ON POR_REL."IdCliente" = CLI_REL."IdCliente" AND POR_REL."IdPortafolio"  = car_rel."IdPortafolio"
			LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" PREMIO ON PREMIO."FolioCartera" = CAR."FolioCartera" 
				AND PREMIO."IdTipoComprobante" = 'FEE' AND PREMIO."IdEstadoFacturacion" != 'ANU'
			WHERE CAR."IdTipoInstrumento" = 'EMP'
		)
		SELECT 
		"Periodo" AS COD_PER, 
		"FechaVencimientoOperacion" AS FEC_VCTO_TP, 
		"Nemotecnico" AS NEMO, 
		"DocumentoIdentidadPrestamista" AS RUT_PTMTA, 
		"CodigoPortafolioPrestamista" AS SEC_RUT_PTMTA, 
		"SecuenciaMovimiento" SEC_MOVTO, 
		"Cantidad" AS CANTIDAD, 
		LPAD('',10) AS FEC_LIQ_ANTICIP,  --cuando se anticipa aca queda la fecha --char(10)
		LPAD('',10) AS FEC_LIQUID, --en sebra esta en blanco siempre, char(10)
		"FechaOperacion" AS FEC_TRANS, 
		0 AS FOLIO_COMP_ADJ,  -- siempre en 0 en sebra
		'' AS FOLIO_TRANS,  -- siempre en blanco en sebra
		0 AS LIN_COMP_ADJ,  -- siempre en 0 en sebra
		"Monto" AS MONTO,  --monto en clp
		0 AS MONTO_ACUM_VC,  --siempre en 0 en sebra
		"MontoPremio" AS PREMIO,  --TODO donde esta el premio en optimus?
		'' AS TIPO_COMP_ADJ,  --siempre blanco
		'' AS EST_ORDEN, --siempre blanco
		"EstadoPrestamista" AS EST_PTMTA, 
		"FechaOperacion" AS FEC_PTMTA_ING, 
		'' AS FEC_PTMTA_FIN, 
		'' AS IND_FAC_PRIPRE, 
		"DocumentoIdentidadPrestatario" AS RUT_CLI, 
		"CodigoPortafolioPrestatario" AS SEC_RUT_CLI, 
		"IdBolsa" AS IND_BOLSA, 
		"TipoCustodia" AS TIPO_PRESTAMISTA, 
		0 AS MONTO_PAGADO, 
		"FolioCartera" AS FOLIO_PRESTAMO, 
		"Tasa" AS TASA, 
		0 AS MTO_VAL_HOY, 
		'' AS FECHA_HOY, 
		0 AS MTO_VAL_MAN, 
		'' AS FECHA_MAN, 
		0 AS MTO_VAL_MDO_HOY, 
		0 AS MTO_VAL_MDO_MAN, 
		"IdMovimientoComprobantePremio" AS SEC_MVTO_FAC, --TODO movimiento a facturar de premio, falta averiguar como se relaciona con la cartera
		'N' AS IMPRESO, 
		"Precio" AS PRECIO, 
		'General' AS CLASEEMPRESTITO, 
		0 AS SEC_FOLIO, 
		'IN' AS TIPO_REL, 
		'N' AS ACT_CARTERA
		FROM BASE AS INGRESOS
		WHERE "EsPrimeraCartera" = 'S'
			AND ( LEFT(INGRESOS."FechaOperacion",7) = "Periodo" OR INGRESOS."IdEstadoCartera" = 'VIG' )
		--operaciones ingresadas o vigentes del periodo
		UNION
		SELECT 
		"Periodo" AS COD_PER, 
		"FechaVencimientoOperacion" AS FEC_VCTO_TP, 
		"Nemotecnico" AS NEMO, 
		"DocumentoIdentidadPrestamista" AS RUT_PTMTA, 
		"CodigoPortafolioPrestamista" AS SEC_RUT_PTMTA, 
		"SecuenciaMovimiento" SEC_MOVTO, 
		"Cantidad" AS CANTIDAD, 
		COALESCE("FechaLiquidacionAnticipo",'') AS FEC_LIQ_ANTICIP,  --cuando se anticipa aca queda la fecha --char(10)
		'' AS FEC_LIQUID, --en sebra esta en blanco siempre, char(10)
		"FechaOperacion" AS FEC_TRANS, 
		0 AS FOLIO_COMP_ADJ,  -- siempre en 0 en sebra
		'' AS FOLIO_TRANS,  -- siempre en blanco en sebra
		0 AS LIN_COMP_ADJ,  -- siempre en 0 en sebra
		"Monto" AS MONTO,  --monto en clp
		0 AS MONTO_ACUM_VC,  --siempre en 0 en sebra
		0 AS PREMIO,  --TODO donde esta el premio en optimus?
		'' AS TIPO_COMP_ADJ,  --siempre blanco
		'' AS EST_ORDEN, --siempre blanco
		"EstadoPrestamista" AS EST_PTMTA, 
		"FechaOperacion" AS FEC_PTMTA_ING, 
		'' AS FEC_PTMTA_FIN, 
		'' AS IND_FAC_PRIPRE, 
		"DocumentoIdentidadPrestatario" AS RUT_CLI, 
		"CodigoPortafolioPrestatario" AS SEC_RUT_CLI, 
		"IdBolsa" AS IND_BOLSA, 
		"TipoCustodia" AS TIPO_PRESTAMISTA, 
		0 AS MONTO_PAGADO, 
		"FolioCartera" AS FOLIO_PRESTAMO, 
		"Tasa" AS TASA, 
		0 AS MTO_VAL_HOY, 
		'' AS FECHA_HOY, 
		0 AS MTO_VAL_MAN, 
		'' AS FECHA_MAN, 
		0 AS MTO_VAL_MDO_HOY, 
		0 AS MTO_VAL_MDO_MAN, 
		"IdMovimientoComprobantePremio" AS SEC_MVTO_FAC, --TODO movimiento a facturar de premio, falta averiguar como se relaciona con la cartera
		'N' AS IMPRESO, 
		"Precio" AS PRECIO, 
		'General' AS CLASEEMPRESTITO, 
		1 AS SEC_FOLIO, 
		'AN' AS TIPO_REL, 
		'N' AS ACT_CARTERA
		FROM BASE AS ANTICIPOS
		WHERE ANTICIPOS."IdEstadoCartera" = 'ANT' AND ANTICIPOS."EGRIdTipoOperacion" = 'ANTEMP'
			AND LEFT(ANTICIPOS."EGRFechaOperacion",7) = "Periodo"
		--anticipos del periodo

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.TBCTTRPT" TO  BCS_DESARROLLADOR WITH GRANT OPTION;