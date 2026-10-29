WITH CargoAbonoPACVCD AS (
	SELECT * FROM ( -- Consulta Vencimientos de pacto
		SELECT
			CASE WHEN CARAB."IdTipoOperacion" = 'COMPAC' THEN 'VENPAC' ELSE 'COMPAC' END AS "IdTipoOperacion",
			CASE CARAB."IdCondicionDeLiquidacion" WHEN 'T0' THEN CILI."ConceptoPH" ELSE CILI."Concepto" END AS "Concepto",
			carab."IdConcepto",
			CARAB."IdComprobante",
			CARAB."DocumentoIdentidad",
			CARAB."NombreCliente",
			CARAB."Monto" AS "MontoOperacion",
			CARAB."IdCliente",
			CARAB."IdCondicionDeLiquidacion",
			CARAB."FechaLiquidacionOperacion" as "FechaLiquidacionOperacion",
			CARAB."IdEmpresa",			
			CARAB."IdTipoMovimiento",
			CARAB."AccionTipoOperacion",
			CARAB."IdEstado",
			carab."FechaComprobante" AS "FechaOperacion",
			CILI."Movimiento" ,
			CASE CARAB."DocumentoIdentidad" WHEN EMP."DocumentoIdentidad" THEN 'S' ELSE 'N' END AS "EsCuentaPropia",
			ROW_NUMBER() OVER (PARTITION BY CARAB."IdOperacion" ORDER BY CARAB."FechaComprobante" DESC) AS RN
		FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarCargosAbonos" 		AS		CARAB
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" 		AS 		EMP 	ON 	EMP."DocumentoIdentidad" 	= 	CARAB."DocumentoIdentidad"
		INNER JOIN  "BSCL.Plataforma.BD::PLA.CodigoInterfazLiquidezIntradia" 	AS 		CILI	ON 	CILI."CodigoOperacion" 		= 	CARAB."IdTipoOperacion" and CILI."IdTipoMovimiento" = CARAB."IdTipoMovimiento"
		WHERE CARAB."IdEmpresa" = :IdEmpresa 
		AND CARAB."IdEstado" = 'CRT'
		AND CARAB."IdTipoOperacion" IN ('COMPAC', 'VENPAC')
		AND CARAB."FechaLiquidacionOperacion" = :FechaProceso
		AND CARAB."IdConcepto" = 'VCDPAC'
		) WHERE RN = 1
	)
	
SELECT
	CARAB."Concepto"							AS "Concepto",
	CARAB."Movimiento" 							AS "TipoMovimiento",
	LPAD(CARAB."IdComprobante" , 12, '0') 		AS "FolioOperacion",
	LPAD(CARAB."DocumentoIdentidad", 12, '0') 	AS "RutCliente",
	RPAD("NombreCliente", 50, ' ') 				AS "NombreCliente",
	"MontoOperacion" 							AS "MontoDecimal",
	'CLP' 										AS "Moneda",	
	TO_NVARCHAR(CARAB."FechaLiquidacionOperacion", 'YYYYMMDD') AS "FechaLiquidacion",
	CASE CLI."IdTipoCliente"
	WHEN 'INTERPOSITA' 		THEN 	'00'
	WHEN 'INVERSIONISTA' 	THEN 	'01'
	WHEN 'INSTITUCIONAL' 	THEN 	'02'
	ELSE 							'XX' END 	AS "TipoCliente",
	'00' 										AS "EstadoOperacion"
FROM CargoAbonoPACVCD CARAB
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CARAB."IdCliente" = cli."IdCliente"


UNION

SELECT -- Consulta pactos
	CASE OPE."FechaCondicionLiquidacion" WHEN ope."FechaOperacion" THEN CILI."ConceptoPH" ELSE CILI."Concepto" END AS "Concepto",
	CILI."Movimiento" AS "TipoMovimiento",
	LPAD(OPE."FolioOperacion" , 12, '0') AS "FolioOperacion",
	LPAD("DocumentoIdentidad", 12, '0') AS "RutCliente",
	RPAD("NombreCompleto", 50, ' ') AS "NombreCliente",
	ope."MontoOperacionInicial" AS "MontoDecimal",
	'CLP' AS "Moneda",
	TO_NVARCHAR(OPE."FechaCondicionLiquidacion", 'YYYYMMDD') AS "FechaLiquidacion",
	CASE CLI."IdTipoCliente"
	WHEN 'INTERPOSITA' THEN '00'
	WHEN 'INVERSIONISTA' THEN '01'
	WHEN 'INSTITUCIONAL' THEN '02'
	ELSE 'XX' END AS "TipoCliente",
	'00' AS "EstadoOperacion"
FROM "BSCL.Mesa.BD::MES.Operacion" OPE
INNER JOIN "BSCL.Plataforma.BD::PLA.CodigoInterfazLiquidezIntradia" CILI 
ON  CILI."IdTipoMovimiento" != '' AND CILI."CodigoOperacion" =   CASE  WHEN OPE."IdTipoOperacion" = 'VENPAC' THEN 'COMPAC'  ELSE 'VENPAC'   END
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TIOP ON (OPE."IdTipoOperacion" = TIOP."IdTipoOperacion")
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON OPE."IdCliente" = cli."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaCliente" AS  PER ON CLI."IdPersona" = PER."IdPersona" 
WHERE OPE."IdTipoProducto" = 'MERF' AND OPE."IdEmpresa" = :IdEmpresa
AND OPE."IdTipoOperacion" IN ('COMPAC', 'VENPAC') AND OPE."IdEstado" IN ('VIG', 'LIQ')
AND TO_NVARCHAR(OPE."FechaCondicionLiquidacion" , 'YYYY-MM-DD') = :FechaProceso;
