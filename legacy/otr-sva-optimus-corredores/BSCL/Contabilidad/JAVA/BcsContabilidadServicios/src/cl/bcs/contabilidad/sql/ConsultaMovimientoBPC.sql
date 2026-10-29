SELECT 
SUBSTRING((SELECT "FreeZoneNo" FROM OADM),0,4) AS "CodigoSociedad",
SUBSTRING(T2."AcctCode",0,10) AS "CuentaMayor",
SUBSTRING('K4',0,2) AS "VarianteDeEjercicio",
SUBSTRING(TO_VARCHAR(YEAR(T0."RefDate")) || '0' || TO_VARCHAR(MONTH(T0."RefDate")),0,7) AS "EjercicioPeriodo",
SUBSTRING(T0."Number",0,10) AS "NroDocumentoContable",
SUBSTRING(T1."Line_ID" + 1,0,3) AS "NroApunteContable",
SUBSTRING('SB',0,2) AS "Segmento",
SUBSTRING('0L',0,2) AS "Ledger",
SUBSTRING(IFNULL(T1."U_BCS_Afiliada",''),0,4) AS "SocGlAsociada",
SUBSTRING(IFNULL(T1."FCCurrency",'PEN'),0,5) AS "Moneda",
TO_DECIMAL(T1."Debit",20,4) AS "TotalContabDebe",
TO_DECIMAL(T1."Credit",20,4) AS "TotalContaHaber",
SUBSTRING(TO_VARCHAR(''),0,1) AS "ClaseDeRegistro",
SUBSTRING(CASE WHEN T0."StornoToTr" > 0 THEN 'X' ELSE '' END,0,1) AS "IndicadorAnulacion"
FROM OJDT T0 
	INNER JOIN JDT1 T1 ON T0."TransId" = T1."TransId"
	INNER JOIN OACT T2 ON T2."AcctCode" = T1."Account"
WHERE T0."TransType" NOT IN (-2,-3)
	  AND T1."RefDate" = :FechaEjecucion
	  AND T0."TransId" NOT IN (SELECT DISTINCT("TransId") FROM "@BCS_CUENTAEXE" T0 INNER JOIN JDT1 T1 ON T1."Account" = T0."U_Account")