SELECT 
	CASE COMP."IdCondicionLiquidacion" WHEN 'T0' THEN CILI."ConceptoPH" ELSE CILI."Concepto" END AS "Concepto",
	CILI."Movimiento" AS "TipoMovimiento",
	LPAD("IdComprobante", 12, '0') AS "FolioOperacion",
	LPAD(comp."DocumentoIdentidad", 12, '0') AS "RutCliente",
	RPAD("NombreLargoCliente", 50, ' ') AS "NombreCliente",
	"MontoComprobanteFinal" AS "MontoDecimal",
	'CLP' AS "Moneda",
	TO_NVARCHAR("FechaLiquidacion", 'YYYYMMDD') AS "FechaLiquidacion",
	CASE CLI."IdTipoCliente" 
	WHEN 'INTERPOSITA' THEN '00'
	WHEN 'INVERSIONISTA' THEN '01'
	WHEN 'INSTITUCIONAL' THEN '02'
	ELSE 'XX' END AS "TipoCliente",
	CASE comp."IdEstadoComprobante" WHEN 'ANU' THEN '01' ELSE '00' END  AS "EstadoOperacion"
FROM "BSCL.Facturacion.BD::FAC.Comprobante" COMP
INNER JOIN "BSCL.Plataforma.BD::PLA.CodigoInterfazLiquidezIntradia" CILI 
ON CILI."CodigoOperacion" = COMP."IdConcepto" 
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TIOP ON (COMP."IdTipoOperacion" = TIOP."IdTipoOperacion")
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON COMP."IdCliente" = cli."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaCliente" AS  PER ON CLI."IdPersona" = PER."IdPersona" 
WHERE COMP."IdClasificacionTipoInstrumento" IN ('RV', 'RF', 'IF') AND
comp."IdTipoOperacion" NOT IN ('ANSIMV', 'ANSIMC', 'VENSPT', 'ANVRF', 'COMSPT', 'ANCRF', 'VENOTC', 'COMOTC', 'ANCOTC', 'SUSOSA')
AND TO_NVARCHAR("FechaLiquidacion", 'YYYY-MM-DD') = :FechaProceso
AND comp."IdEmpresa" = :IdEmpresa;
