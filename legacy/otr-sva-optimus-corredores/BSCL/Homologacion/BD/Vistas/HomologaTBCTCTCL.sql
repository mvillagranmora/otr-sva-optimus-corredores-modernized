SET SCHEMA "BSCL";
DROP VIEW "BSCL.Homologacion.BD.Vistas::HomologaTBCTCTCL";

CREATE VIEW "BSCL.Homologacion.BD.Vistas::HomologaTBCTCTCL" AS
WITH 
	v_Saldo_Rank as (
		SELECT   "IdCuentaInversion"
				,CAST("FechaSistema" as NVARCHAR(7)) AS "PeriodoPorFechaMovimiento"
				,"FechaSistema" AS "FechaUltimoSaldoFechaMovimiento"
				,"SaldoPorFechaMovimiento"
				,RANK() over (PARTITION BY "IdCuentaInversion", CAST("FechaSistema" as NVARCHAR(7)) ORDER BY "FechaSistema","FechaCertificacion","IdCargoAbonoMovimiento") as "RankFechaMovimiento"
				
				,CAST("FechaEfectivaMovimiento" as NVARCHAR(7)) AS "PeriodoPorFechaLiquidacion"
				,"FechaEfectivaMovimiento" AS "FechaUltimoSaldoFechaLiquidacion"
				,"Saldo" AS "SaldoPorFechaLiquidacion"
				,RANK() over (PARTITION BY "IdCuentaInversion", CAST("FechaEfectivaMovimiento" as NVARCHAR(7)) ORDER BY "FechaEfectivaMovimiento","FechaCertificacion","IdCargoAbonoMovimiento") as "RankFechaLiquidacion"
		FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento"
		WHERE "IdEstado" = 'CRT'
	)
SELECT
	 SCI."Periodo" AS "COD_PER"								--char(7)
	,TRIM(PER."DocumentoIdentidad") AS "RUT_CLI"			--char(11) 
	,PORT."CodigoPortafolio" AS "SEC_RUT_CLI"						--decimal(2, 0)  corresponde a la cuenta
	,CASE
		WHEN CI."IdMoneda" = 'CLP' THEN 'CCTE'
		WHEN CI."IdMoneda" = 'USD' THEN 'CDOL'
		WHEN CI."IdMoneda" = 'EUR' THEN 'EURO'
	 END AS "CTA_CLI"										--char(4)
	,'0001-01-01' AS "FEC_COBRO_GASTO"						--char(10)
	,'0001-01-01' AS "FEC_SALDO_NEG"						--char(10)
	,SCI."FechaSaldo" AS "FEC_ULT_OPE" 						--char(10)
	,0 AS "MONTO_OPE_ACC" 					--decimal(17, 2) 
	,0 AS "SALDO_COMIS" 					--decimal(17, 2) 
	,0 AS "SALDO_COMIS_DIA" 				--decimal(17, 2) 
	,0 AS "SALDO_COMIS_DIA_CALC"			--decimal(17, 2) 
	,0 AS "SALDO_COMIS_DIA_MIN" 			--decimal(17, 2) 
	,0 AS "SALDO_COMPROM" 					--decimal(17, 2) 
	,0 AS "SALDO_DERECHOS" 					--decimal(17, 2) 
	,SCI."Saldo" AS "SALDO_DISP" 			--decimal(17, 2)
	,0 AS "SALDO_GASTOS" 					--decimal(17, 2) 
	,0 AS "SALDO_LIBRE_01" 					--decimal(17, 2)  
	,0 AS "SALDO_LIBRE_02" 					--decimal(17, 2)  
	,0 AS "SALDO_LIBRE_03" 					--decimal(17, 2)  
	,0 AS "SALDO_NEG_INST" 					--decimal(15, 4) 
	,0 AS "SALDO_RETEN" 					--decimal(17, 2) 
	,SCI."Saldo" AS "SALDO_TP" 				--decimal(17, 2) 
	,0 AS "COMPRAS_24" 						--decimal(17, 2)  
	,0 AS "VENTAS_24" 						--decimal(17, 2)  
	,0 AS "COMPRAS_48" 						--decimal(17, 2)  
	,0 AS "VENTAS_48" 						--decimal(17, 2)  
	,0 AS "RECIBIR_24" 						--decimal(17, 2)  
	,0 AS "TRANSF_24" 						--decimal(17, 2)  
	,0 AS "TRANSF_48" 						--decimal(17, 2)  
	,0 AS "APORTES_24" 						--decimal(17, 2)  
	,0 AS "APORTES_48" 						--decimal(17, 2)  
	,0 AS "RESCATE_24" 						--decimal(17, 2)  
	,0 AS "RESCATE_48" 						--decimal(17, 2)  
	,0 AS "RESCATE_72" 						--decimal(17, 2)  
	,0 AS "RESCATE_96" 						--decimal(17, 2)  
	,0 AS "RESCATE_120" 					--decimal(17, 2)  
	,0 AS "SALDO_AFECTO_GMF" 				--decimal(13, 2)  
	,0 AS "MONTO_RETENIDO" 					--decimal(18, 4)  
	,0 AS "COMPRAS_72" 						--decimal(17, 2)  
	,0 AS "VENTAS_72" 						--decimal(17, 2)  
	,0 AS "TRANSF_72" 						--decimal(17, 2)  
	,0 AS "SALDO_HABERES" 					--decimal(17, 2)
FROM "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS CI
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdCliente" = CI."IdCliente"
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORT ON PORT."IdPortafolio" = CI."IdPortafolio"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER ON PER."IdPersona" = CLI."IdPersona"
INNER JOIN (
	SELECT	 
		 Saldo."IdCuentaInversion"
	  	,Saldo."PeriodoPorFechaMovimiento" AS "Periodo"
	  	,Saldo."FechaUltimoSaldoFechaMovimiento" AS "FechaSaldo"
	  	,Saldo."SaldoPorFechaMovimiento" AS "Saldo"
	FROM v_Saldo_Rank as Saldo,
	  (
		SELECT "IdCuentaInversion","PeriodoPorFechaMovimiento", MAX("RankFechaMovimiento") as "MaxRankFechaPeriodo"
		FROM v_Saldo_Rank
		GROUP BY "IdCuentaInversion","PeriodoPorFechaMovimiento"
	   ) FMAX
	WHERE FMAX."IdCuentaInversion" = Saldo."IdCuentaInversion"
	  AND FMAX."PeriodoPorFechaMovimiento" = Saldo."PeriodoPorFechaMovimiento"
	  AND FMAX."MaxRankFechaPeriodo" = Saldo."RankFechaMovimiento"
) AS SCI ON CI."IdCuentaInversion" = SCI."IdCuentaInversion"

WITH READ ONLY;


GRANT SELECT ON "BSCL.Homologacion.BD.Vistas::HomologaTBCTCTCL" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
