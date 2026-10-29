SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCTCTCL"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCTCTCL" AS

WITH S AS (
		SELECT "IdCargoAbonoMovimiento","Saldo","IdCuentaInversion","FechaSistema",LAG("Saldo",1,1) OVER (PARTITION BY "IdCuentaInversion" ORDER BY "IdCargoAbonoMovimiento") AS LAGG 
		FROM BSCL."BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento"
		ORDER BY "IdCargoAbonoMovimiento"
	)
SELECT   
	   ctcl."IdCuentaInversion",
	   ctcl."FechaSaldo",
	   ctcl."DocumentoIdentidad",
	   ctcl."CodigoPortafolio",
	   ctcl."IdPortafolio",
	   ctcl."IdMoneda",
	   por."IdCarga",
	 LEFT("FechaSaldo",7)  AS COD_PER								--char(7)
	,COALESCE("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(ctcl."DocumentoIdentidad"),'0-0') AS RUT_CLI
	,COALESCE (CAST(ctcl."CodigoPortafolio" AS VARCHAR(100)), '') AS SEC_RUT_CLI				--decimal(2, 0)
	,CASE
		WHEN ctcl."IdMoneda" = 'CLP' THEN 'CCTE'
		WHEN ctcl."IdMoneda" = 'USD' THEN 'CDOL'
		WHEN ctcl."IdMoneda" = 'EUR' THEN 'EURO'
		ELSE RPAD(LEFT(ctcl."IdMoneda",4),4)
	 END AS CTA_CLI										--char(4)
	,CASE WHEN ctcl."IdMoneda" = 'USD' THEN '001-01-01' else '0001-01-01' END AS FEC_COBRO_GASTO					--char(10)
	,CASE WHEN COALESCE("SaldoDisponible", 0) >= 0 THEN '0001-01-01' ELSE COALESCE(TO_NVARCHAR(SALDO_NEG."FechaSistema",'YYYY-MM-DD'),'0001-01-01') END  AS FEC_SALDO_NEG	--char(10)
	,CASE WHEN ctcl."IdMoneda" = 'USD' THEN '001-01-01' else '0001-01-01' END  AS FEC_ULT_OPE 					--char(10)
	,0 AS MONTO_OPE_ACC 					--decimal(17, 2) 
	,0 AS SALDO_COMIS 					--decimal(17, 2) 
	,0 AS SALDO_COMIS_DIA 				--decimal(17, 2) 
	,0 AS SALDO_COMIS_DIA_CALC			--decimal(17, 2) 
	,0 AS SALDO_COMIS_DIA_MIN 			--decimal(17, 2) 
	,0 AS SALDO_COMPROM 					--decimal(17, 2) 
	,0 AS SALDO_DERECHOS 					--decimal(17, 2) 
	,CAST(COALESCE("SaldoDisponible", 0) AS DECIMAL(17, 2)) AS SALDO_DISP 			--decimal(17, 2)
	,0 AS SALDO_GASTOS 					--decimal(17, 2) 
	,CAST(COALESCE("SaldoEnGarantia", 0) AS DECIMAL(17, 2)) AS SALDO_LIBRE_01 					--decimal(17, 2)   -- JK saldo_garantia
	,0 AS SALDO_LIBRE_02 					--decimal(17, 2)  
	,0 AS SALDO_LIBRE_03 					--decimal(17, 2)  
	,0 AS SALDO_NEG_INST 					--decimal(15, 4) 
	,CAST(COALESCE("SaldoRetenido", 0) AS DECIMAL(17, 2)) AS SALDO_RETEN 	--decimal(17, 2)
	,CAST(COALESCE("SaldoAPlazo", 0) AS DECIMAL(17, 2)) AS SALDO_TP 				--decimal(17, 2) -- saldo en simultaneas compra - ventas
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
FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarSaldoCuentaInversionFechaEfectivaAnalitica" (
	PLACEHOLDER."$$p_idempresa$$" => 1,
 	PLACEHOLDER."$$p_fechaconsulta$$" => '0001-01-01'
  ) ctcl
LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" por ON por."IdCuentaInversion" = ctcl."IdCuentaInversion"
LEFT JOIN (
	SELECT "IdCuentaInversion",MAX("FechaSistema")  as "FechaSistema"
	FROM S WHERE "Saldo" < 0 AND LAGG >0 
	GROUP BY "IdCuentaInversion"
) AS SALDO_NEG ON SALDO_NEG."IdCuentaInversion" = por."IdCuentaInversion"
WHERE "BSCL.Replica.BD.Servicios::func_IsNumeric"(ctcl."CodigoPortafolio") != 0 and "DocumentoIdentidad" IS NOT NULL
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.TBCTCTCL" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
