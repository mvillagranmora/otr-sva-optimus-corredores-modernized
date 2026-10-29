SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCTGTIA"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCTGTIA" AS

		SELECT
			GAR."IdGarantia",
			LEFT(fec."Valor",7) AS "COD_PER", -- TIPO :char
			COALESCE (CAST("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(CAST( IDENT."DocumentoIdentidad" AS VARCHAR(11))) AS VARCHAR(100)),'0-0'   )  AS "RUT_CLI", -- TIPO :char
			CAST(COALESCE (POR."CodigoPortafolio" , '-1') AS varchar)  AS "SEC_RUT_CLI", -- TIPO :decimal
			CASE WHEN TINS."IdClasificacionTipoInstrumento" IN ('RF', 'IF', 'FM') THEN TINS."IdClasificacionTipoInstrumento"
            			WHEN INS."IdTipoInstrumento" IN ('OSA','SIM','ACN') THEN 'AC'
 	           			WHEN INS."IdTipoInstrumento" IN ('ETN') THEN 'EN'
 	           			WHEN INS."IdTipoInstrumento" IN ('ETX') THEN 'ET'
 	           			WHEN INS."IdTipoInstrumento" IN ('MON') THEN 'MO'
 	           			WHEN INS."IdTipoInstrumento" IN ('MIL') THEN 'ST'
 	           			WHEN INS."IdTipoInstrumento" IN ('ACX') THEN 'AO'
	           			WHEN INS."IdTipoInstrumento" IN ('CFX') THEN 'CO'
	           			WHEN INS."IdTipoInstrumento" IN ('CFM') THEN 'CM'
            			WHEN INS."IdTipoInstrumento" = 'CFI' THEN 'CF'
            			WHEN gar."IdOrigenGarantia" = 'CTA' AND "IdMonedaInstrumento" = 'CLP' THEN 'MN'
            			WHEN gar."IdOrigenGarantia" = 'CTA' AND "IdMonedaInstrumento" = 'USD' THEN 'MN'
            			ELSE ''END AS "MERCADO", -- CHAR(2)
			CASE 
				WHEN gar."IdOrigenGarantia" = 'CTA' AND "IdMonedaInstrumento" = 'CLP' THEN 'MN'
				WHEN gar."IdOrigenGarantia" = 'CTA' AND "IdMonedaInstrumento" = 'USD' THEN 'US'
			ELSE COALESCE (CAST(GAR."Nemotecnico" AS VARCHAR(16)),''   ) END AS "NEMO", -- TIPO :char
			COALESCE (CAST('COR' AS VARCHAR(100)),''   )  AS "COD_BOVEDA", -- TIPO :char
			COALESCE (GAR."FolioGarantia" , 0    )  AS "SEC_CAR_INVER", -- TIPO :decimal
			CASE 
				WHEN  CONCEPTO."ConceptoAbonoReplica" IS NOT NULL AND CONCEPTO."ConceptoAbonoReplica" <> ''  THEN RIGHT(CONCEPTO."ConceptoAbonoReplica",2)
				ELSE LEFT(CAST(GAR."IdConcepto" AS VARCHAR(100)),2) END AS "CONCEPTO_MVTO",
			COALESCE (GAR."INGCantidad" , 0    )  AS "CAN_GARANTIA", -- TIPO :decimal
			COALESCE (GAR."TasaGarantia" , 0    )  AS "TASA_EMISION", -- TIPO :decimal
			COALESCE (0 , 0    )  AS "SPREAD_TASA_EMISION", -- TIPO :decimal
			COALESCE (CAST('' AS VARCHAR(100)),''   )  AS "MODALIDAD_PAGO", -- TIPO :char
			COALESCE (CAST('' AS VARCHAR(100)),''   )  AS "PERIODICIDAD_PAGO", -- TIPO :char
			COALESCE (CAST(GAR."INGFechaOperacion" AS VARCHAR(100)),CAST('1900-01-01' AS VARCHAR(100)))  AS "FECHA_EMISION" -- TIPO :char
		FROM "BSCL.Custodia.BD::GTA.Garantia" GAR
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON INS."IdInstrumento" = GAR."IdInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TINS ON TINS."IdTipoInstrumento" = INS."IdTipoInstrumento"
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON GAR."IdCliente" = CLI."IdCliente"
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON CLI."IdPersona" = PER."IdPersona"
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdCliente" = CLI."IdCliente" AND gar."IdPortafolio"  = por."IdPortafolio"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS fec ON (fec."IdEmpresa" = 1 and "CodigoParametro" = 'PLA.FechaSistema')
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS CONCEPTO ON CONCEPTO."IdConcepto" = GAR."IdConcepto"
		WHERE  GAR."IdEstado" = 'VIG'
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.TBCTGTIA" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
