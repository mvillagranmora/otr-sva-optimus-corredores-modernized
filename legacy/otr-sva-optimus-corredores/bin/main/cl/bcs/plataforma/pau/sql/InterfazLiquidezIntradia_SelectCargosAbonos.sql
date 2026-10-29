  WITH CargoAbonoSPT AS (
	SELECT 
		carab."IdConcepto", 
		CARAB."IdComprobante", 
		CARAB."DocumentoIdentidad",
		CARAB."NombreCliente",
		CARAB."MontoOperacion",
		CARAB."IdCliente",
		CARAB."IdCondicionDeLiquidacion",
		CARAB."FechaEfectivaMovimiento" AS "FechaLiquidacionOperacion",
		CARAB."IdEmpresa",
		CASE WHEN CARAB."IdTipoOperacion" = 'COMSPT' THEN 'VENSPT' ELSE 'COMSPT' END AS "IdTipoOperacion",
		CARAB."IdTipoMovimiento",
		CARAB."AccionTipoOperacion",
		CARAB."IdEstado",
		carab."FechaComprobante" AS "FechaOperacion",
		CASE CARAB."DocumentoIdentidad" WHEN EMP."DocumentoIdentidad" THEN 'S' ELSE 'N' END AS "EsCuentaPropia",
		ROW_NUMBER() OVER (PARTITION BY CARAB."IdOperacion" ORDER BY CARAB."FechaComprobante" DESC) AS RN
	FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarCargosAbonos" CARAB
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMP ON EMP."DocumentoIdentidad" = CARAB."DocumentoIdentidad"
	WHERE CARAB."IdEmpresa" = :IdEmpresa 
	AND CARAB."IdEstado" = 'CRT'
	AND "IdTipoOperacion" IN ('VENSPT', 'COMSPT')
	AND CARAB."FechaEfectivaMovimiento" = :FechaProceso 
	),
CargoAbonoTER AS (
	SELECT
		carab."IdConcepto",
		CARAB."IdComprobante",
		CARAB."DocumentoIdentidad",
		CARAB."NombreClienteRelacionado" AS "NombreCliente",
		CARAB."MontoOperacion",
		CARAB."IdCliente",
		CARAB."IdCondicionDeLiquidacion",
		CARAB."FechaLiquidacionOperacion" as "FechaLiquidacionOperacion",
		CARAB."IdEmpresa",
		CASE WHEN CARAB."IdTipoOperacion" = 'COMTER' THEN 'VENTER' ELSE 'COMTER' END AS "IdTipoOperacion",
		CARAB."IdTipoMovimiento",
		CARAB."AccionTipoOperacion",
		CARAB."IdEstado",
		carab."FechaComprobante" AS "FechaOperacion",
		CASE CARAB."DocumentoIdentidad" WHEN EMP."DocumentoIdentidad" THEN 'S' ELSE 'N' END AS "EsCuentaPropia",
		ROW_NUMBER() OVER (PARTITION BY CARAB."IdOperacion" ORDER BY CARAB."FechaComprobante" DESC) AS RN
	FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarCargosAbonos" CARAB
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMP ON EMP."DocumentoIdentidad" = CARAB."DocumentoIdentidad"
	WHERE CARAB."IdEmpresa" = :IdEmpresa 
	AND CARAB."IdEstado" = 'CRT'
	AND "IdTipoOperacion" IN ('VENTER', 'COMTER')
	AND CARAB."FechaLiquidacionOperacion" = :FechaProceso 
	),
CargoAbonoFWD AS (
	SELECT
		CARAB."IdConcepto",
		CARAB."IdComprobante",
		CARAB."DocumentoIdentidad",
		CARAB."NombreCliente",
		CARAB."MontoOperacion",
		CARAB."IdCliente",
		CASE WHEN CARAB."IdCondicionDeLiquidacion" = 'T0' THEN 'T1' ELSE 'T0' END AS "IdCondicionDeLiquidacion",
		CARAB."FechaLiquidacionOperacion" AS "FechaLiquidacionOperacion",
		CARAB."IdEmpresa",
		CASE WHEN CARAB."IdTipoOperacion" = 'COMFWD' THEN 'VENFWD' ELSE 'COMFWD' END AS "IdTipoOperacion",
		CARAB."IdTipoMovimiento",
		CARAB."AccionTipoOperacion",
		CARAB."IdEstado",
		CARAB."FechaComprobante" AS "FechaOperacion",
		CASE CARAB."DocumentoIdentidad" WHEN EMP."DocumentoIdentidad" THEN 'S' ELSE 'N' END AS "EsCuentaPropia",
		ROW_NUMBER() OVER (PARTITION BY CARAB."IdOperacion" ORDER BY CARAB."FechaComprobante" DESC) AS RN
	FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarCargosAbonos" CARAB
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMP ON EMP."DocumentoIdentidad" = CARAB."DocumentoIdentidad"
	WHERE CARAB."IdEmpresa" = :IdEmpresa 
		AND CARAB."IdEstado" = 'CRT'
		AND CARAB."IdTipoOperacion" IN ('VENFWD', 'COMFWD')
		AND CARAB."FechaLiquidacionOperacion" = :FechaProceso
	),	
CargosAbonos AS (
	SELECT * FROM CargoAbonoSPT WHERE RN = 1
	UNION 
	SELECT * FROM CargoAbonoTER WHERE RN = 1
	UNION 
	SELECT * FROM CargoAbonoFWD WHERE RN = 1
	)
	
SELECT
	CASE CARAB."IdCondicionDeLiquidacion" WHEN 'T0' THEN CILI."ConceptoPH" ELSE CILI."Concepto" END AS "Concepto",
	CILI."Movimiento" AS "TipoMovimiento",
	LPAD(CARAB."IdComprobante" , 12, '0') AS "FolioOperacion",
	LPAD(CARAB."DocumentoIdentidad", 12, '0') AS "RutCliente",
	RPAD("NombreCliente", 50, ' ') AS "NombreCliente",
	"MontoOperacion" AS "MontoDecimal",
	'CLP' AS "Moneda",
	TO_NVARCHAR(CARAB."FechaLiquidacionOperacion", 'YYYYMMDD') AS "FechaLiquidacion",
	CASE CLI."IdTipoCliente"
	WHEN 'INTERPOSITA' THEN '00'
	WHEN 'INVERSIONISTA' THEN '01'
	WHEN 'INSTITUCIONAL' THEN '02'
	ELSE 'XX' END AS "TipoCliente",
	'00' AS "EstadoOperacion"
FROM CargosAbonos CARAB
INNER JOIN "BSCL.Plataforma.BD::PLA.CodigoInterfazLiquidezIntradia" CILI
  			ON 		CILI."CodigoOperacion" 	= 	CARAB."IdTipoOperacion"
 			AND 	CARAB."EsCuentaPropia" 	= 	CILI."EsCuentaPropia"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CARAB."IdCliente" = cli."IdCliente"