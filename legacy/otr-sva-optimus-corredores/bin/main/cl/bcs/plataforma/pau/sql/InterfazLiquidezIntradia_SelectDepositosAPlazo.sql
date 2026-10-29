WITH VencimientosDepositosAPlazo AS (
	SELECT
		Venc."IdComprobante",
		Venc."DocumentoIdentidad",
		Venc."NombreCliente",
		Venc."Monto",
		Venc."IdCliente",
		Venc."IdCondicionDeLiquidacion",
		Venc."FechaEfectivaMovimiento" as "FechaLiquidacionOperacion",
		Venc."IdEmpresa",
		Venc."IdTipoOperacion",
		Venc."IdTipoMovimiento",
		Venc."AccionTipoOperacion",
		Venc."IdEstado",
		Venc."IdConcepto"
	FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarCargosAbonos" Venc
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMP ON EMP."DocumentoIdentidad" = Venc."DocumentoIdentidad"
	WHERE Venc."IdEmpresa" = :IdEmpresa AND Venc."FechaEfectivaMovimiento" = :FechaProceso 
	AND Venc."IdEstado" = 'CRT' AND Venc."IdConcepto" = 'VCDTIT' AND "IdClasificacionTipoInstrumento" = 'IF'
)
SELECT
	CASE Venc."IdCondicionDeLiquidacion" WHEN 'T0' THEN CILI."ConceptoPH" ELSE CILI."Concepto" END AS "Concepto",
	CILI."Movimiento" AS "TipoMovimiento",
	LPAD(Venc."IdComprobante" , 12, '0') AS "FolioOperacion",
	LPAD(Venc."DocumentoIdentidad", 12, '0') AS "RutCliente",
	RPAD("NombreCliente", 50, ' ') AS "NombreCliente",
	Venc."Monto" AS "MontoDecimal",
	'CLP' AS "Moneda",
	TO_NVARCHAR(Venc."FechaLiquidacionOperacion", 'YYYYMMDD') AS "FechaLiquidacion",
	CASE CLI."IdTipoCliente"
	WHEN 'INTERPOSITA' THEN '00'
	WHEN 'INVERSIONISTA' THEN '01'
	WHEN 'INSTITUCIONAL' THEN '02'
	ELSE 'XX' END AS "TipoCliente",
	'00' AS "EstadoOperacion"
FROM VencimientosDepositosAPlazo Venc
INNER JOIN "BSCL.Plataforma.BD::PLA.CodigoInterfazLiquidezIntradia" CILI 
ON CILI."CodigoOperacion" = Venc."IdConcepto"  AND CILI."IdTipoMovimiento" = Venc."IdTipoMovimiento"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON Venc."IdCliente" = cli."IdCliente";
