SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.MovimientosCaja";
GO
CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.MovimientosCaja" (
    IN p_IdModulo NVARCHAR(40),
    IN p_IdUsuario BIGINT,
    IN p_IdEmpresa BIGINT,
    IN p_FechaDesde DATE,
    IN p_FechaHasta DATE,
    IN p_Datos TABLE ("IdCliente" BIGINT, "IdPortafolio" BIGINT)
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	
	v_MonedasDelSistema = 
	SELECT  "PLA.MonedaSistema",
			CASE WHEN "PLA.MonedaAdicional" IS NULL OR "PLA.MonedaAdicional" = '' THEN 'USD' ELSE "PLA.MonedaAdicional" END "PLA.MonedaAdicional" 
	  FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracionPivot"
	 WHERE "IdEmpresa" = :p_IdEmpresa;
	
	
	v_Movimientos =
	SELECT  B."IdCuentaInversion",
			B."IdCliente",
			B."IdPortafolio",
			B."IdCargoAbonoMovimiento",
			B."NombreConcepto",
			B."IdComprobante",
			B."IdTipoComprobante",
			B."NombreTipoComprobante",
			B."NemotecnicoOriginal" AS "Nemotecnico",
			B."Cantidad",
			B."FechaIngresoRegistro",
			B."FechaSistema",
			B."FechaEfectivaMovimiento",
			B."IdMoneda",
			"PLA.MonedaSistema" AS "IdMonedaSistema",
			"PLA.MonedaAdicional" AS "IdMonedaAdicional",
			CONCAT('Movimientos de caja ', B."IdMoneda") AS "TituloCuentaInversion",			
			CASE WHEN B."IdTipoMovimiento" = 'MOVCAR' THEN COALESCE(B."Monto",0) ELSE 0 END "Cargo",
			CASE WHEN B."IdTipoMovimiento" = 'MOVABN' THEN COALESCE(B."Monto",0) ELSE 0 END "Abono",
			COALESCE(B."SaldoPorFechaMovimiento",0) AS "SaldoPorFechaMovimiento",
			RANK() over (PARTITION BY B."IdCuentaInversion" ORDER BY B."FechaSistema",B."FechaCertificacion",B."IdCargoAbonoMovimiento") as "RankFechaMovimiento",
			MF."MontoDecimales",
			MF."PrecioDecimales",
			MF."MontoTrucaRedondea"
	  FROM :p_Datos AS P
  	 INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCartolaDeMovimientos" as B 
  	 		 ON B."IdCliente" = P."IdCliente" AND B."IdPortafolio" = COALESCE(P."IdPortafolio",B."IdPortafolio")
  	 INNER JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MF 
	 		 ON MF."IdMoneda" 			= B."IdMoneda" 
	 		AND MF."IdUsoMonedaFormato" = 'GEN'
	 		AND MF."IdTipoInstrumento" 	= ''
	 		AND MF."IdTipoOperacion" 	= ''
  	  		,:v_MonedasDelSistema
  	 WHERE B."IdEstado" = 'CRT'
  	   AND B."IdMoneda" IN ("PLA.MonedaSistema", "PLA.MonedaAdicional", 'EUR')
 	   AND B."FechaSistema" <= p_FechaHasta;

 	
	v_MovimientosCTA =
	WITH
		SaldoInicial AS (
			SELECT MOV."IdPortafolio", MOV."IdMoneda", MOV."SaldoPorFechaMovimiento"
			  FROM :v_Movimientos as MOV,			 	  
				  	  (SELECT "IdCuentaInversion", MAX("RankFechaMovimiento") as "RankMovimientoAnterior"
						 FROM :v_Movimientos
						WHERE "FechaSistema" < :p_FechaDesde
						GROUP BY "IdCuentaInversion"
			 	 	   ) FMAX
			 WHERE FMAX."IdCuentaInversion" = MOV."IdCuentaInversion"
			   AND FMAX."RankMovimientoAnterior" = MOV."RankFechaMovimiento"
		),
		SaldoFinal AS (
			SELECT MOV."IdPortafolio", MOV."IdMoneda", MOV."SaldoPorFechaMovimiento"
			  FROM :v_Movimientos as MOV,			 	  
				  	  (SELECT "IdCuentaInversion", MAX("RankFechaMovimiento") as "RankMovimientoAnterior"
						 FROM :v_Movimientos
						WHERE "FechaSistema" <= :p_FechaHasta
						GROUP BY "IdCuentaInversion"
			 	 	   ) FMAX
			 WHERE FMAX."IdCuentaInversion" = MOV."IdCuentaInversion"
			   AND FMAX."RankMovimientoAnterior" = MOV."RankFechaMovimiento"
		),
		SaldoGarantia AS (
			SELECT P."IdPortafolio",
				   CASE WHEN GTA."IdMonedaInstrumento" = MS."PLA.MonedaSistema" 
				   		THEN GTA."IdMonedaInstrumento" ELSE MS."PLA.MonedaAdicional" END AS "IdMoneda",
				   CASE WHEN GTA."IdMonedaInstrumento" = MS."PLA.MonedaSistema" 
				   		THEN SUM(GTA."INGMontoATasaGarantiaActual") ELSE SUM(GTA."INGMontoGarantiaActualMonedaAdicional") END AS "Saldo"
			FROM :p_Datos AS P 
			INNER JOIN "BSCL.Custodia.BD.Vistas::GTA.ConsultarTablaGarantia" GTA ON (GTA."IdCliente" = P."IdCliente" AND GTA."IdPortafolio" = P."IdPortafolio")
				 , :v_MonedasDelSistema AS MS
			WHERE GTA."IdOrigenGarantia" = 'CTA'
			AND GTA."IdEstado" = 'VIG'
			AND GTA."INGFechaOperacion" BETWEEN p_FechaDesde AND p_FechaHasta
			GROUP BY GTA."IdCliente", P."IdPortafolio", GTA."IdMonedaInstrumento",MS."PLA.MonedaSistema",MS."PLA.MonedaAdicional"
		)
	 SELECT M."IdCliente",
			M."IdPortafolio",
			M."IdCargoAbonoMovimiento",
			"BSCL.Plataforma.BD.Utilitarios::CamelCase"(M."NombreConcepto") AS "NombreConcepto",			
			CONCAT(CONCAT(M."IdTipoComprobante",' - '), "BSCL.Plataforma.BD.Utilitarios::CamelCase"(M."NombreTipoComprobante")) AS "TipoComprobante",
			CASE WHEN M."IdComprobante" = 0 THEN NULL ELSE M."IdComprobante" END AS "IdComprobante",
			M."IdTipoComprobante",
			M."Nemotecnico",
			M."Cantidad",
			CAST(M."FechaIngresoRegistro" AS DATE) AS "FechaIngreso",
			CAST(M."FechaSistema" AS DATE) AS "FechaOperacion",
			CAST(M."FechaEfectivaMovimiento" AS DATE) AS "FechaLiquidacion",
			M."IdMoneda",
			M."IdMonedaSistema",
			M."IdMonedaAdicional",
			M."TituloCuentaInversion",
			"BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(M."Cargo",M."MontoDecimales",M."MontoTrucaRedondea") AS "Cargo",
			"BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(M."Abono",M."MontoDecimales",M."MontoTrucaRedondea") AS "Abono",
			"BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(M."SaldoPorFechaMovimiento",M."MontoDecimales",M."MontoTrucaRedondea") AS "Saldo",
			CAST(:p_FechaDesde AS NVARCHAR(10)) AS "PeriodoInicial",
			CAST(LAST_DAY (TO_DATE(:p_FechaHasta, 'YYYY-MM-DD')) AS NVARCHAR(10)) AS "PeriodoFinal",
			COALESCE(I."SaldoPorFechaMovimiento",0) AS "SaldoInicial",
			COALESCE(F."SaldoPorFechaMovimiento",0) AS "SaldoFinal",
			COALESCE(G."Saldo",0) AS "SaldoGarantia"
	  FROM :v_Movimientos M
	  FULL OUTER JOIN SaldoInicial I ON I."IdPortafolio" = M."IdPortafolio" AND I."IdMoneda" = M."IdMoneda"
	  FULL OUTER JOIN SaldoFinal F ON F."IdPortafolio" = M."IdPortafolio" AND F."IdMoneda" = M."IdMoneda"
	  FULL OUTER JOIN SaldoGarantia G ON G."IdPortafolio" = M."IdPortafolio" AND G."IdMoneda" = M."IdMoneda"
	ORDER BY M."IdPortafolio","FechaSistema";
 	
	
	SELECT  M."IdCliente",
			M."IdPortafolio",
			ROW_NUMBER() over (PARTITION BY M."IdCliente",M."IdPortafolio",M."IdMoneda"
									   ORDER BY "IdPortafolio","IdMoneda","FechaOperacion" DESC) AS "PosicionRegistro",
			M."IdCargoAbonoMovimiento",
			M."NombreConcepto",
			M."IdComprobante",
			CAST(M."TipoComprobante" AS NVARCHAR(100)) AS "TipoComprobante",
			CASE WHEN COALESCE(M."Nemotecnico",'') = '' THEN 'EFECTIVO' ELSE M."Nemotecnico" END AS "Nemotecnico",
			M."Cantidad",
			TO_VARCHAR(M."FechaIngreso",'DD-MM-YYYY') AS "FechaIngreso",
			TO_VARCHAR(M."FechaOperacion",'DD-MM-YYYY') AS "FechaOperacion",
			TO_VARCHAR(M."FechaLiquidacion",'DD-MM-YYYY') AS "FechaLiquidacion",
			M."IdMoneda" AS "Moneda",
			M."TituloCuentaInversion",
			M."Cargo",
			M."Abono",
			M."Saldo",			
			M."SaldoInicial",
			M."SaldoFinal",
			M."SaldoGarantia",
			M."PeriodoInicial",
			M."PeriodoFinal"
	FROM :v_MovimientosCTA AS M
    ORDER BY "IdPortafolio","IdMoneda","FechaOperacion";
 	   
END;
GO