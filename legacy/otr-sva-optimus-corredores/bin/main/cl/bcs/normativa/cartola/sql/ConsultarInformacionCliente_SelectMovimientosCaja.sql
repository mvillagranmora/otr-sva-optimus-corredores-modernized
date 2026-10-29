SELECT
B."IdCliente"
,B."IdPortafolio"
,B."IdConcepto"
,B."NombreTipoMovimiento"

,COALESCE(B."AfectaRentabilidad",'S') AS "AfectaRentabilidad"

,B."TipoConcepto"
,B."IdTipoOperacion"
,B."IdCargoAbonoMovimiento" AS "PosicionRegistro"
,B."IdCargoAbonoMovimiento"
,CASE WHEN B."IdTipoOperacion" = 'DIVDIN' AND "IdTipoMovimiento" = 'MOVABN' THEN B."Descripcion"
 ELSE CASE   WHEN B."ExclusivoCartola" = 'S'  THEN "BSCL.Plataforma.BD.Utilitarios::CamelCase"(B."NombreConceptoCartola")  
			 ELSE "BSCL.Plataforma.BD.Utilitarios::CamelCase"(B."NombreConcepto")
	  END
 END AS "NombreConcepto"
,CONCAT(CONCAT(B."IdTipoComprobante",' - '), "BSCL.Plataforma.BD.Utilitarios::CamelCase"(B."NombreTipoComprobante")) AS "TipoComprobante"
,CASE WHEN B."IdComprobante" = 0 THEN NULL 
 	  WHEN B."IdModulo" = 'FAC'  THEN B."FolioComprobanteFacturacion"
 ELSE B."IdComprobante" END AS "IdComprobante"
,CASE WHEN COALESCE(B."NemotecnicoOriginal",'') = '' THEN '' ELSE B."NemotecnicoOriginal" END AS "Nemotecnico"
,B."Cantidad"
,TO_VARCHAR(B."FechaIngresoRegistro",'DD-MM-YYYY') AS "FechaIngreso"
,TO_VARCHAR(B."FechaSistema",'DD-MM-YYYY') AS "FechaOperacion"
,TO_VARCHAR(B."FechaEfectivaMovimiento",'DD-MM-YYYY') AS "FechaLiquidacion"
,B."IdMoneda" AS "Moneda"
,CONCAT('Movimientos de caja ', B."IdMoneda") AS "TituloCuentaInversion"
,CASE WHEN B."IdTipoMovimiento" = 'MOVCAR' THEN COALESCE(B."Monto",0) ELSE 0 END "Cargo"
,CASE WHEN B."IdTipoMovimiento" = 'MOVABN' THEN COALESCE(B."Monto",0) ELSE 0 END "Abono"
,COALESCE(B."SaldoPorFechaMovimiento",0) AS "Saldo"
	, B."AccionTipoOperacion"
	, B."IdModulo"
FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarCartolaDeMovimientos" as B 
WHERE 
B."IdPortafolio" IN (:IdPortafolio)
AND B."IdEstado" = 'CRT'
AND B."IdMoneda" IN (:IdMoneda)
AND B."FechaSistema" BETWEEN :FechaDesde AND :FechaHasta
AND B."IdConcepto" NOT IN ('CAMIGR','ABMIGR','ABMIGI','CAMIGI')
ORDER BY B."IdPortafolio",B."IdMoneda",B."FechaSistema",B."IdCargoAbonoMovimiento";