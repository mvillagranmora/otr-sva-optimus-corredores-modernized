SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaTBCTCTCLHist"; 

	
CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaTBCTCTCLHist" AS

WITH 
	v_FechasCierre as (
		SELECT 	TO_VARCHAR("FechaEfectivaMovimiento"	, 'YYYY-MM')AS "COD_PER",   max("FechaEfectivaMovimiento" ) AS "FechaCierre"						  
		FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento"
		WHERE "IdEstado" = 'CRT' 								  
		GROUP BY TO_VARCHAR("FechaEfectivaMovimiento", 'YYYY-MM')
								  
	),
	v_Saldo_Rank as (
		SELECT   "IdCuentaInversion"
				,TO_VARCHAR(CURRENT_TIMESTAMP, 'YYYY-MM') AS "Periodo"
				,"FechaSistema" AS "FechaUltimoSaldoFechaMovimiento"
				,"SaldoPorFechaMovimiento"
				,ROW_NUMBER() over (PARTITION BY "IdCuentaInversion" ORDER BY "IdCuentaInversion" ,
					CASE WHEN "FechaEfectivaMovimiento" >= '2019-04-29'  AND  "FechaCertificacion" <= '2018-01-31' THEN 1 ELSE 0 END asc,
					"FechaEfectivaMovimiento" desc,"FechaCertificacion" desc, "IdCargoAbonoMovimiento" desc, "FechaSistema" desc,"FechaCertificacion" desc) as "RankFechaMovimiento"
				
				,CAST("FechaEfectivaMovimiento" as NVARCHAR(7)) AS "PeriodoPorFechaLiquidacion"
				,"FechaEfectivaMovimiento" AS "FechaUltimoSaldoFechaLiquidacion"
				,"Saldo" AS "SaldoPorFechaLiquidacion"
				,ROW_NUMBER() over (PARTITION BY "IdCuentaInversion" ORDER BY "IdCuentaInversion" ,
				CASE WHEN "FechaEfectivaMovimiento" >= '2019-04-29'  AND  "FechaCertificacion" <= '2018-01-31' THEN 1 ELSE 0 END asc,
				/* "FechaSistema" desc ,*/"FechaEfectivaMovimiento" desc,"FechaCertificacion" desc, "IdCargoAbonoMovimiento" desc, "FechaSistema" desc,"FechaCertificacion" desc) as "RankFechaLiquidacion"
		FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS JOIN v_FechasCierre AS f
						
		WHERE "IdEstado" = 'CRT' 
		AND "FechaEfectivaMovimiento" < '2019-05-01' 
		--AND "IdCarga"  != 0
	---	AND "FechaCertificacion" != '2018-01-31'
		--AND "IdCuentaInversion" = 16697
	),
	v_Saldo_Retenido AS (
		SELECT   "IdCuentaInversion"
				,TO_VARCHAR(CURRENT_TIMESTAMP, 'YYYY-MM') AS "PeriodoPorFechaLiquidacion"
				,CASE WHEN "IdTipoMovimiento" = 'MOVABN' THEN "Monto"
					 ELSE "Monto" * -1 END AS "SaldoPorFechaLiquidacion"
		FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento"
		WHERE-- "IdEstado" = 'PEN' 
		"FechaEfectivaMovimiento" >= '2019-05-01' AND "FechaSistema" <  '2019-05-01' 
	),
	v_Saldo_Plazo AS (
		SELECT 
			C."IdPortafolio",
			SUM(CASE WHEN C."IdTipoOperacion" in ('VENSIM','VENPAC') THEN S."INGMontoATasaSubyacenteActual"
					 WHEN C."IdTipoOperacion" in ('COMSIM','COMPAC') THEN S."INGMontoATasaSubyacenteActual" * -1 
					 ELSE 0 END) AS "SaldoPlazo",
			TO_VARCHAR(CURRENT_TIMESTAMP, 'YYYY-MM') AS "Periodo",
--			CAST(C."INGFechaLiquidacion" AS NVARCHAR(7)) AS "Periodo",
			min (c."IdMonedaInstrumento") AS  "IdMoneda"
		FROM "BSCL.Custodia.BD::CUS.Subyacente" as S 
		INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" as C on S."FolioCarteraRelacionada" = C."FolioCartera"
		WHERE S."IdEstado" = 'VIG'
		  AND S."IdClasificacionTipoInstrumento" = 'RV'
		  AND C."IdTipoInstrumento" IN ('SIM','PAC')
		GROUP BY 
			C."IdPortafolio" --,C."INGFechaLiquidacion"	
	),
	v_Saldo_Garantia AS (
		SELECT 
			G."IdPortafolio",
			SUM(G."INGMontoATasaGarantiaActual") AS "SaldoGarantia",
			
			TO_VARCHAR(CURRENT_TIMESTAMP, 'YYYY-MM') AS "Periodo",
	--		CAST(G."INGFechaLiquidacion" AS NVARCHAR(7)) AS "Periodo",
			g."IdMonedaInstrumento"  AS  "IdMoneda"
		FROM "BSCL.Custodia.BD::GTA.Garantia" AS G
		WHERE G."IdEstado" = 'VIG'
		  AND G."IdOrigenGarantia" = 'CTA'
		GROUP BY 
			G."IdPortafolio",  g."IdMonedaInstrumento"  -- G."INGFechaLiquidacion" ,
	)
SELECT   
	POR."IdPortafolio",SCI."IdCuentaInversion",PER."NombreCompleto",
	 "IdStack" AS "IDSTACK", 
	   LEFT(fec."Valor",7)  ||':'||rtrim(ltrim(CAST(TRIM(PER."DocumentoIdentidad") AS VARCHAR(11)) ))||':'|| rtrim(ltrim(cast(POR."CodigoPortafolio" as varchar)))||':'|| rtrim(ltrim(CASE
		WHEN CI."IdMoneda" = 'CLP' THEN 'CCTE'
		WHEN CI."IdMoneda" = 'USD' THEN 'CDOL'
		WHEN CI."IdMoneda" = 'EUR' THEN 'EURO'
		ELSE LEFT(CI."IdMoneda",4)
	 END)) AS TABLE_KEY,

	 LEFT(fec."Valor",7)  AS COD_PER								--char(7)
	,COALESCE("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(PER."DocumentoIdentidad"),'0-0') AS RUT_CLI
	,COALESCE (CAST(POR."CodigoPortafolio" AS VARCHAR(100)), '') AS SEC_RUT_CLI				--decimal(2, 0)
	,CASE
		WHEN CI."IdMoneda" = 'CLP' THEN 'CCTE'
		WHEN CI."IdMoneda" = 'USD' THEN 'CDOL'
		WHEN CI."IdMoneda" = 'EUR' THEN 'EURO'
		ELSE LEFT(CI."IdMoneda",4)
	 END AS CTA_CLI										--char(4)
	,'1900-01-01' AS FEC_COBRO_GASTO					--char(10)
	,'1900-01-01' AS FEC_SALDO_NEG						--char(10)
	,SCI."FechaUltimoSaldoFechaMovimiento" AS FEC_ULT_OPE 					--char(10)
	,0 AS MONTO_OPE_ACC 					--decimal(17, 2) 
	,0 AS SALDO_COMIS 					--decimal(17, 2) 
	,0 AS SALDO_COMIS_DIA 				--decimal(17, 2) 
	,0 AS SALDO_COMIS_DIA_CALC			--decimal(17, 2) 
	,0 AS SALDO_COMIS_DIA_MIN 			--decimal(17, 2) 
	,0 AS SALDO_COMPROM 					--decimal(17, 2) 
	,0 AS SALDO_DERECHOS 					--decimal(17, 2) 
	,COALESCE(SCI."SaldoPorFechaLiquidacion", 0) AS SALDO_DISP 			--decimal(17, 2)
	,0 AS SALDO_GASTOS 					--decimal(17, 2) 
	,COALESCE(GAR."SaldoGarantia", 0) AS SALDO_LIBRE_01 					--decimal(17, 2)   -- JK saldo_garantia
	,0 AS SALDO_LIBRE_02 					--decimal(17, 2)  
	,0 AS SALDO_LIBRE_03 					--decimal(17, 2)  
	,0 AS SALDO_NEG_INST 					--decimal(15, 4) 
	,COALESCE(SRT."SaldoRetenido", 0) AS SALDO_RETEN 	--decimal(17, 2)
	,COALESCE(PLA."SaldoPlazo", 0) AS SALDO_TP 				--decimal(17, 2) -- saldo en simultaneas compra - ventas
	,0 AS COMPRAS_24 						--decimal(17, 2)  
	,0 AS VENTAS_24 						--decimal(17, 2)  
	,0 AS COMPRAS_48 						--decimal(17, 2)  
	,0 AS VENTAS_48 						--decimal(17, 2)  
	,0 AS RECIBIR_24 						--decimal(17, 2)  
	,0 AS TRANSF_24 						--decimal(17, 2)  
	,0 AS TRANSF_48 						--decimal(17, 2)  
	,0 AS APORTES_24 						--decimal(17, 2)  
	,0 AS APORTES_48 						--decimal(17, 2)  
	,0 AS RESCATE_24 						--decimal(17, 2)  
	,0 AS RESCATE_48 						--decimal(17, 2)  
	,0 AS RESCATE_72 						--decimal(17, 2)  
	,0 AS RESCATE_96 						--decimal(17, 2)  
	,0 AS RESCATE_120 					--decimal(17, 2)  
	,0 AS SALDO_AFECTO_GMF 				--decimal(13, 2)  
	,0 AS MONTO_RETENIDO 					--decimal(18, 4)  
	,0 AS COMPRAS_72 						--decimal(17, 2)  
	,0 AS VENTAS_72 						--decimal(17, 2)  
	,0 AS TRANSF_72 						--decimal(17, 2)  
	,0 AS SALDO_HABERES 					--decimal(17, 2)
FROM "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS CI
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdCliente" = CI."IdCliente"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER ON PER."IdPersona" = CLI."IdPersona"
INNER JOIN v_Saldo_Rank  AS SCI ON (CI."IdCuentaInversion" = SCI."IdCuentaInversion" AND  "RankFechaMovimiento" = 1 ) 
INNER JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS fec ON (fec."IdEmpresa" = 1 and "CodigoParametro" = 'PLA.FechaSistema')

LEFT JOIN (
	SELECT "IdCuentaInversion", SUM("SaldoPorFechaLiquidacion") AS "SaldoRetenido", "PeriodoPorFechaLiquidacion" 
	FROM v_Saldo_Retenido 
	GROUP BY "PeriodoPorFechaLiquidacion","IdCuentaInversion"
) AS SRT ON CI."IdCuentaInversion" = SRT."IdCuentaInversion" AND SRT."PeriodoPorFechaLiquidacion" = SCI."Periodo"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdCliente" = CLI."IdCliente"  AND por."IdPortafolio" = ci."IdPortafolio"
LEFT JOIN v_Saldo_Plazo PLA ON PLA."IdPortafolio" = POR."IdPortafolio" AND SCI."Periodo" = PLA."Periodo" AND pla."IdMoneda"  = CI."IdMoneda"
LEFT JOIN v_Saldo_Garantia GAR ON GAR."IdPortafolio" = POR."IdPortafolio" AND SCI."Periodo" = GAR."Periodo" AND gar."IdMoneda"  = CI."IdMoneda"
LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'TBCTCTCL' AND WST."Key" = cast(CI."IdCuentaInversion" AS varchar)
WHERE abs (COALESCE(SCI."SaldoPorFechaLiquidacion", 0))  <= 999999999999999 AND abs(COALESCE(SRT."SaldoRetenido", 0)) <= 999999999999999

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaTBCTCTCL" TO  BCS_DESARROLLADOR WITH GRANT OPTION;