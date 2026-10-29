WITH SaldoInicialGarantia AS (
SELECT GAR."FolioCarteraOrigen", GAR."IdCarteraOrigen", COALESCE(SBY."IdTipoInstrumento", GAR."IdTipoInstrumento") AS "IdTipoInstrumento", MIN(GAR."IdGarantia") AS "IdGarantia",
   	CASE
   		WHEN GAR."IdClasificacionTipoInstrumento" = 'IF' AND GAR."IdTipoInstrumento" <> 'PAC' THEN GAR."NemoSVS"
   		ELSE GAR."Nemotecnico"
	END AS "Nemotecnico"
		FROM "BSCL.Custodia.BD::GTA.Garantia" GAR
		LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoSubyacente" as ULTSUB on (GAR."FolioCarteraOrigen" = ULTSUB."FolioCarteraRelacionada")
		LEFT JOIN "BSCL.Custodia.BD::CUS.Subyacente" as SBY on (SBY."IdSubyacente" = ULTSUB."IdSubyacente")
		WHERE GAR."IdEstado" <> 'ANU'
		AND TO_VARCHAR(GAR."INGFechaOperacion", 'YYYY-MM-DD')  < :FechaInicio
		AND GAR."IdCliente" = case when :IdCliente > 0 then :IdCliente else GAR."IdCliente" END
		AND COALESCE(SBY."IdInstrumento",GAR."IdInstrumento") = case when :IdInstrumento > 0 then :IdInstrumento else COALESCE(SBY."IdInstrumento",GAR."IdInstrumento") END
		AND COALESCE(SBY."Nemotecnico", GAR."Nemotecnico") = case when :Nemotecnico <> '' then :Nemotecnico else COALESCE(SBY."Nemotecnico", GAR."Nemotecnico") END
		AND GAR."IdClasificacionTipoInstrumento" = case when :IdClasificacionTipoInstrumento <> '' then :IdClasificacionTipoInstrumento else GAR."IdClasificacionTipoInstrumento" END
		AND GAR."IdTipoInstrumento" NOT IN ('PAC','SIM','PRT','EMP')
		AND GAR."IdClasificacionTipoInstrumento" != 'DV'
		AND ( (TO_VARCHAR(GAR."EGRFechaOperacion", 'YYYY-MM-DD') = '0001-01-01' AND GAR."IdEstado" = 'VIG') 
		OR (TO_VARCHAR(GAR."EGRFechaOperacion", 'YYYY-MM-DD') >=:FechaInicio))
		GROUP BY GAR."FolioCarteraOrigen", GAR."IdCarteraOrigen", SBY."IdTipoInstrumento", GAR."IdTipoInstrumento", GAR."IdGarantia", GAR."IdClasificacionTipoInstrumento",GAR."IdTipoInstrumento", GAR."NemoSVS", GAR."Nemotecnico"
)

SELECT SAL."Nemotecnico",GAR."IdEmisor",GAR."ISIN",DOC."DocumentoIdentidad", GAR."IdCliente",GAR."IdPortafolio",POR."CodigoPortafolio",GAR."IdMonedaInstrumento", CAR."IdBoveda", 
	SUM(case when GAR."IdClasificacionTipoInstrumento" = 'IF' then GAR."INGValorRescate" else GAR."INGCantidad" end) AS "Saldo", 'Saldo Inicial' AS "Moneda"
		FROM SaldoInicialGarantia AS SAL
		INNER JOIN "BSCL.Custodia.BD::GTA.Garantia" GAR ON (SAL."IdGarantia" = GAR."IdGarantia")
		INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR ON (GAR."IdCarteraOrigen" = CAR."IdCartera")
		INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON (GAR."IdPortafolio" = POR."IdPortafolio")
		INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdCliente" = GAR."IdCliente")
		INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON (DOC."IdPersona" = CLI."IdPersona") AND DOC."Predeterminado" = 'S'
		GROUP BY SAL."Nemotecnico",GAR."IdEmisor", GAR."ISIN", DOC."DocumentoIdentidad",GAR."IdCliente",POR."CodigoPortafolio",GAR."IdPortafolio",GAR."IdMonedaInstrumento",CAR."IdBoveda"
		ORDER BY GAR."IdCliente", POR."CodigoPortafolio",SAL."Nemotecnico",CAR."IdBoveda";