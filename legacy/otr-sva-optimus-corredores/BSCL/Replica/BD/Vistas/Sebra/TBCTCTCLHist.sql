SET SCHEMA "BSCL"; 
DROP PROCEDURE "BSCL.Replica.BD.Servicios::Sebra.TBCTCTCLHist"; 

CREATE PROCEDURE "BSCL.Replica.BD.Servicios::Sebra.TBCTCTCLHist"(
IN p_Fecha NVARCHAR(10),
OUT p_out TABLE (
	"COD_PER" CHAR(7)  ,
	"RUT_CLI" CHAR(11)  ,
	"SEC_RUT_CLI" DECIMAL(2)  ,
	"CTA_CLI" CHAR(4)  ,
	"FEC_COBRO_GASTO" CHAR(10)  ,
	"FEC_SALDO_NEG" CHAR(10)  ,
	"FEC_ULT_OPE" CHAR(10)  ,
	"MONTO_OPE_ACC" DECIMAL(17,2)  ,
	"SALDO_COMIS" DECIMAL(17,2)  ,
	"SALDO_COMIS_DIA" DECIMAL(17,2)  ,
	"SALDO_COMIS_DIA_CALC" DECIMAL(17,2)  ,
	"SALDO_COMIS_DIA_MIN" DECIMAL(17,2)  ,
	"SALDO_COMPROM" DECIMAL(17,2)  ,
	"SALDO_DERECHOS" DECIMAL(17,2)  ,
	"SALDO_DISP" DECIMAL(17,2)  ,
	"SALDO_GASTOS" DECIMAL(17,2)  ,
	"SALDO_LIBRE_01" DECIMAL(17,2)   ,
	"SALDO_LIBRE_02" DECIMAL(17,2)   ,
	"SALDO_LIBRE_03" DECIMAL(17,2)   ,
	"SALDO_NEG_INST" DECIMAL(15,4)  ,
	"SALDO_RETEN" DECIMAL(17,2)  ,
	"SALDO_TP" DECIMAL(17,2)  ,
	"COMPRAS_24" DECIMAL(17,2)   ,
	"VENTAS_24" DECIMAL(17,2)   ,
	"COMPRAS_48" DECIMAL(17,2)   ,
	"VENTAS_48" DECIMAL(17,2)   ,
	"RECIBIR_24" DECIMAL(17,2)   ,
	"TRANSF_24" DECIMAL(17,2)   ,
	"TRANSF_48" DECIMAL(17,2)   ,
	"APORTES_24" DECIMAL(17,2)   ,
	"APORTES_48" DECIMAL(17,2)   ,
	"RESCATE_24" DECIMAL(17,2)   ,
	"RESCATE_48" DECIMAL(17,2)   ,
	"RESCATE_72" DECIMAL(17,2)   ,
	"RESCATE_96" DECIMAL(17,2)   ,
	"RESCATE_120" DECIMAL(17,2)   ,
	"SALDO_AFECTO_GMF" DECIMAL(13,2),   
	"MONTO_RETENIDO" DECIMAL(18,4)   ,
	"COMPRAS_72" DECIMAL(17,2)   ,
	"VENTAS_72" DECIMAL(17,2)   ,
	"TRANSF_72" DECIMAL(17,2)   ,
	"SALDO_HABERES" DECIMAL(17,2)  
	)
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
READS SQL DATA WITH RESULT VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCTCTCLHist" AS 
BEGIN

p_out = 

WITH S AS (
		SELECT "IdCargoAbonoMovimiento","Saldo","IdCuentaInversion","FechaSistema",LAG("Saldo",1,1) OVER (PARTITION BY "IdCuentaInversion" ORDER BY "IdCargoAbonoMovimiento") AS LAGG 
		FROM BSCL."BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento"
		ORDER BY "IdCargoAbonoMovimiento"
	)
SELECT   
	 LEFT("FechaSaldo",7)  AS COD_PER								--char(7)
	,COALESCE("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(ctcl."DocumentoIdentidad"),'0-0') AS RUT_CLI
	,COALESCE (CAST(ctcl."CodigoPortafolio" AS VARCHAR(100)), '') AS SEC_RUT_CLI				--decimal(2, 0)
	,CASE
		WHEN ctcl."IdMoneda" = 'CLP' THEN 'CCTE'
		WHEN ctcl."IdMoneda" = 'USD' THEN 'CDOL'
		WHEN ctcl."IdMoneda" = 'EUR' THEN 'EURO'
		ELSE LEFT(ctcl."IdMoneda",4)
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
	,CASE ctcl."IdMoneda" WHEN 'USD' THEN COALESCE("SaldoDisponible", 0) * dolar."Valor"  
	ELSE COALESCE("SaldoDisponible", 0) END AS SALDO_DISP 			--decimal(17, 2)
	,0 AS SALDO_GASTOS 					--decimal(17, 2) 
	,COALESCE("SaldoEnGarantia", 0) AS SALDO_LIBRE_01 					--decimal(17, 2)   -- JK saldo_garantia
	,0 AS SALDO_LIBRE_02 					--decimal(17, 2)  
	,0 AS SALDO_LIBRE_03 					--decimal(17, 2)  
	,0 AS SALDO_NEG_INST 					--decimal(15, 4) 
	,COALESCE("SaldoRetenido", 0) AS SALDO_RETEN 	--decimal(17, 2)
	,COALESCE("SaldoAPlazo", 0) AS SALDO_TP 				--decimal(17, 2) -- saldo en simultaneas compra - ventas
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
 	PLACEHOLDER."$$p_fechaconsulta$$" => :p_Fecha
  ) ctcl
LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" por ON por."IdCuentaInversion" = ctcl."IdCuentaInversion"
LEFT JOIN (
	select top 1 f."Valor"
	from "BSCL"."BSCL.Plataforma.BD::MER.ParametroFinanciero" f
	left join "BSCL"."BSCL.Plataforma.BD::PLA.Configuracion" conf on conf."CodigoParametro" = 'PLA.FechaSistema'
	where "IdParametroFinanciero" = 'DO' and "Fecha" <= :P_fecha
	order by f."Fecha" desc
) AS dolar on 1 = 1
LEFT JOIN (
	SELECT "IdCuentaInversion",MAX("FechaSistema")  as "FechaSistema"
	FROM S WHERE "Saldo" < 0 AND LAGG >0 
	GROUP BY "IdCuentaInversion"
) AS SALDO_NEG ON SALDO_NEG."IdCuentaInversion" = por."IdCuentaInversion"
WHERE "BSCL.Replica.BD.Servicios::func_IsNumeric"(ctcl."CodigoPortafolio") != 0;

END;

