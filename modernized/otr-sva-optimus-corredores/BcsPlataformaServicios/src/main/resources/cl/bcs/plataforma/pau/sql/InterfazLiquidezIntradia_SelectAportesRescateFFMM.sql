SELECT 
	CASE AR."FechaLiquidacion" WHEN AR."FechaEmision" THEN CILI."ConceptoPH" ELSE CILI."Concepto" END AS "Concepto",
	CILI."Movimiento" AS "TipoMovimiento",
	LPAD(ar."FolioContrato", 12, '0') AS "FolioOperacion",
	LPAD("DocumentoIdentidad", 12, '0') AS "RutCliente",
	RPAD("NombreCompleto", 50, ' ') AS "NombreCliente",
	"MontoMonedaSistema" AS "MontoDecimal", 
	"IdMonedaSistema" AS "IdMoneda",
	"IdMonedaPago", 
	TO_NVARCHAR("FechaLiquidacion" , 'YYYYMMDD') AS "FechaLiquidacion",
	CASE CLI."IdTipoCliente"
	WHEN 'INTERPOSITA' THEN '00'
	WHEN 'INVERSIONISTA' THEN '01'
	WHEN 'INSTITUCIONAL' THEN '02'
	ELSE 'XX' END AS "TipoCliente",
	'00' AS "EstadoOperacion"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AR
LEFT JOIN "BSCL.Plataforma.BD::PLA.CodigoInterfazLiquidezIntradia" CILI 
ON CILI."CodigoOperacion" = AR."TipoSolicitud"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON AR."IdCliente" = cli."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaCliente" AS  PER ON CLI."IdPersona" = PER."IdPersona"
WHERE AR."IdEmpresa" = :IdEmpresa AND AR."FechaLiquidacion" = :FechaProceso;
