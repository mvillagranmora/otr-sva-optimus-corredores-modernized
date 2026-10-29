SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCTTRAS"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCTTRAS" AS

		SELECT
			LEFT(COALESCE(HPR."ValorOrigen", TRA."IdTipoInstrumento"),2)  AS MERCADO, -- TIPO :char
			LEFT(COALESCE (CAST(TRA."Nemotecnico" AS VARCHAR(100)),''   ),16)  AS NEMO, -- TIPO :char
			COALESCE (TRA."IdMovimientoCarteraEnTransito" , 0    )  AS SEC_TRASPASO, -- TIPO :decimal
			COALESCE (TRA."IdMovimientoCarteraEnTransito"  , 0    )  AS SEC_CARTA_TRAM, -- TIPO :decimal
			COALESCE (CAST(CASE WHEN TRA."IdTipoOperacion" = 'INGTIT' THEN 'IT'
								WHEN TRA."IdTipoOperacion" = 'RETTIT' THEN 'RT'
								WHEN LEFT(TRA."IdTipoOperacion",1) = 'I' THEN 'IT'
								WHEN LEFT(TRA."IdTipoOperacion",1) = 'R' THEN 'RT'								
								ELSE '' END AS VARCHAR(100)),''   )  AS TIPO_TRASPASO, -- TIPO :char
								
			COALESCE (CAST(TRA."FechaOperacion" AS VARCHAR(100)),''   )  AS FEC_TRASPASO, -- TIPO :char
			COALESCE (TRA."Cantidad" , 0    )  AS CANTIDAD, -- TIPO :decimal 
			COALESCE (CAST(TRA."IdBoveda" AS VARCHAR(100)),''   )  AS COD_BOVEDA, -- TIPO :char
			COALESCE (0 , 0    )  AS COD_CORR_CONTRA, -- TIPO :decimal   > JK ver de donde 
			COALESCE (CAST('' AS VARCHAR(100)),''   )  AS COD_TESTIGO1, -- TIPO :char
			COALESCE (CAST('' AS VARCHAR(100)),''   )  AS COD_TESTIGO2, -- TIPO :char
			COALESCE (CAST(CASE WHEN TRA."IdEstado" = 'ANU' THEN 'R'
								WHEN TRA."IdEstado" = 'CRT' THEN 'E'
								WHEN TRA."IdEstado" = 'PEN' THEN 'I'
								ELSE '' END AS VARCHAR(100)),''   )  AS EST_TRASPASO, -- TIPO :char
			TO_VARCHAR (TRA."FechaLiquidacion",'YYYY-MM-DD')   AS FEC_LIQUID, -- TIPO :char 
			TO_VARCHAR (TRA."FechaLiquidacion",'YYYY-MM-DD')   AS FEC_RET_TIT, -- TIPO :char
			COALESCE (CAST(CASE WHEN TRA."IdTipoOperacion" = 'INGTIT' THEN 'B'
								WHEN TRA."IdTipoOperacion" = 'RETTIT' THEN 'D'
								ELSE '' END AS VARCHAR(100)),''   )  AS FORMA_TRASPASO, -- TIPO :char
			SUBSTRING(COALESCE (CAST(TRA."Glosa" AS VARCHAR(100)),''   ),0,40)  AS GLOSA_TITULO, -- TIPO :char
			COALESCE (TRA."MontoCompra" , 0    )  AS MONTO, -- TIPO :decimal
			COALESCE (TRA."PrecioCompra" , 0    )  AS PRECIO, -- TIPO :decimal
			COALESCE (CAST("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(CAST(IDENT."DocumentoIdentidad" AS VARCHAR(11))) AS VARCHAR(100)),''   )  AS RUT_CLI, -- TIPO :char
			COALESCE (CAST('' AS VARCHAR(100)),''   )  AS RUT_CORR_CONTRA, -- TIPO :char
			COALESCE (CAST(POR."CodigoPortafolio" AS VARCHAR(100)), '')  AS SEC_RUT_CLI, -- TIPO :decimal
			COALESCE (0 , 0    )  AS SEC_TRASPASO_ANT, -- TIPO :decimal
			COALESCE (0 , 0    )  AS SEC_TRASPASO_SIG, -- TIPO :decimal
			COALESCE (CAST('' AS VARCHAR(100)),''   )  AS SERIE -- TIPO :char

		--SELECT TRA.*
		FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" TRA
		INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON TRA."IdCliente" = CLI."IdCliente"
		INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON CLI."IdPersona" = PER."IdPersona"
		INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad"
		INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdCliente" = CLI."IdCliente" AND tra."IdPortafolio" = por."IdPortafolio"
		LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" HPR ON (HPR."TipoValor" = 'TipoInstrumentoSegunMercado' 
		                                                                   AND HPR."Entidad" = 'TipoInstrumentoSegunMercado' 
		                                                                   AND HPR."ValorDestino" = TRA."IdTipoInstrumento" )
		                                                                   
		WHERE "IdTipoOperacionOrigen" IN ('INGTIT', 'RETTIT') AND "IdConcepto" IN (SELECT "IdConcepto" FROM "BSCL.Custodia.BD::CUS.ConceptoCustodia" WHERE  "GeneraTraspaso" = 'S')


WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.TBCTTRAS" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
