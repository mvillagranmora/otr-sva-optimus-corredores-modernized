WITH SaldoInicialCartera AS (
SELECT CAR."FolioCartera", MIN(CAR."IdCartera") AS "IdCartera", COALESCE(SBY."IdTipoInstrumento", CAR."IdTipoInstrumento") AS "IdTipoInstrumento",
   	CASE
   		WHEN car."IdClasificacionTipoInstrumento" = 'IF' AND car."IdTipoInstrumento" <> 'PAC' THEN car."NemoSVS"
   		ELSE car."Nemotecnico"
	END AS "Nemotecnico"
		FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
		LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoSubyacente" as ULTSUB on (CAR."FolioCartera" = ULTSUB."FolioCarteraRelacionada")
		LEFT JOIN "BSCL.Custodia.BD::CUS.Subyacente" as SBY on (SBY."IdSubyacente" = ULTSUB."IdSubyacente")
		WHERE CAR."IdEstado" <> 'ANU'
		AND TO_VARCHAR(CAR."INGFechaOperacion", 'YYYY-MM-DD')  < :FechaInicio
		AND CAR."IdCliente" = case when :IdCliente > 0 then :IdCliente else CAR."IdCliente" END
		AND COALESCE(SBY."IdInstrumento",CAR."IdInstrumento") = case when :IdInstrumento > 0 then :IdInstrumento else COALESCE(SBY."IdInstrumento",CAR."IdInstrumento") END
		AND COALESCE(SBY."Nemotecnico", CAR."Nemotecnico") = case when :Nemotecnico <> '' then :Nemotecnico else COALESCE(SBY."Nemotecnico", CAR."Nemotecnico") END
		AND CAR."IdClasificacionTipoInstrumento" = case when :IdClasificacionTipoInstrumento <> '' then :IdClasificacionTipoInstrumento else CAR."IdClasificacionTipoInstrumento" END
		AND CAR."IdTipoInstrumento" NOT IN ('PAC','SIM','PRT','EMP')
		AND CAR."IdClasificacionTipoInstrumento" != 'DV'
		AND ( (TO_VARCHAR(CAR."EGRFechaOperacion", 'YYYY-MM-DD') = '0001-01-01' AND CAR."IdEstado" = 'VIG') 
		OR (TO_VARCHAR(CAR."EGRFechaOperacion", 'YYYY-MM-DD') >=:FechaInicio))
		GROUP BY CAR."FolioCartera", CAR."NemoSVS", CAR."Nemotecnico", SBY."IdTipoInstrumento", CAR."IdTipoInstrumento", CAR."IdInstrumento", CAR."IdClasificacionTipoInstrumento"
)

SELECT SAL."Nemotecnico",CAR."IdEmisor",CAR."ISIN",DOC."DocumentoIdentidad", CAR."IdCliente",CAR."IdPortafolio",POR."CodigoPortafolio",CAR."IdMonedaInstrumento", CAR."IdBoveda", 
	SUM(case when car."IdClasificacionTipoInstrumento" = 'IF' then car."INGValorRescate" else car."INGCantidad" end) AS "Saldo", 'Saldo Inicial' AS "Moneda"
		FROM SaldoInicialCartera AS SAL
		INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR ON (SAL."IdCartera" = CAR."IdCartera")
		INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON (CAR."IdPortafolio" = POR."IdPortafolio")
		INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdCliente" = CAR."IdCliente")
		INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON (DOC."IdPersona" = CLI."IdPersona")
		WHERE DOC."Predeterminado" = 'S'
		GROUP BY SAL."Nemotecnico",CAR."IdEmisor", CAR."ISIN", DOC."DocumentoIdentidad",CAR."IdCliente",POR."CodigoPortafolio",CAR."IdPortafolio",CAR."IdMonedaInstrumento",CAR."IdBoveda"
		ORDER BY CAR."IdCliente", POR."CodigoPortafolio",SAL."Nemotecnico",CAR."IdBoveda";