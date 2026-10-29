WITH IngresosEgresos AS (
	SELECT 
	CASE WHEN IE."IdPersona" = EMP."IdPersona" AND IE."IdTipoOperacion" = 'DIVDIN' THEN 'S' ELSE 'N' END AS "EsCuentaPropia", 
	ie.*, 
	CD."IdComprobante" AS "IdComprobante"
	FROM "BSCL.Tesoreria.BD::TES.IngresoEgreso" IE
	INNER JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteDeIngresoEgreso" AS CD ON IE."IdIngresoEgreso" = CD."IdIngresoEgreso" 
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMP ON EMP."IdPersona" = IE."IdPersona"
	WHERE IE."IdEstado" = 'EMI' 
	AND IE."FechaLiquidacion" = :FechaProceso
	AND "IdConcepto" IN ('CIECDV')
)
SELECT 
	CASE IE."FechaLiquidacion" WHEN ie."FechaOperacion" THEN CILI."ConceptoPH" ELSE CILI."Concepto" END AS "Concepto",
	CILI."Movimiento" AS "TipoMovimiento",
	LPAD("IdComprobante" , 12, '0') AS "FolioOperacion",
	LPAD(IE."DocumentoIdentidad", 12, '0') AS "RutCliente",
	RPAD("NombrePersona" , 50, ' ') AS "NombreCliente",
	"Monto" AS "MontoDecimal",
	'CLP' AS "Moneda",
	TO_NVARCHAR(IE."FechaLiquidacion", 'YYYYMMDD') AS "FechaLiquidacion",
	CASE CLI."IdTipoCliente"
	WHEN 'INTERPOSITA' THEN '00'
	WHEN 'INVERSIONISTA' THEN '01'
	WHEN 'INSTITUCIONAL' THEN '02'
	ELSE CASE WHEN IE."IdConcepto" IN ('CFPHSM', 'CLFIRV') THEN '04' ELSE 'XX' END END AS "TipoCliente",
	'00' AS "EstadoOperacion"
FROM IngresosEgresos IE
INNER JOIN "BSCL.Plataforma.BD::PLA.CodigoInterfazLiquidezIntradia" CILI 
ON CILI."CodigoOperacion"  = IE."IdConcepto"  AND CILI."IdTipoMovimiento" = IE."IdTipoMovimiento" AND CILI."EsCuentaPropia" = IE."EsCuentaPropia"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdPersona" = IE."IdPersona" 
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMP ON EMP."IdPersona" = IE."IdPersona" 
WHERE IE."IdEmpresa" = :IdEmpresa AND IE."IdConcepto" = 'CIECDV';