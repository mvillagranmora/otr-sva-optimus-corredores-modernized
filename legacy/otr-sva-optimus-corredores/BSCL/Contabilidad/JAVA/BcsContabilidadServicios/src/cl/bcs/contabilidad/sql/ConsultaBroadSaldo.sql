SELECT 
SUBSTRING('1',0,1) AS "CodInstitucion",
SUBSTRING('B1',0,2) AS "CodAplicativo",
SUBSTRING((SELECT "FreeZoneNo" FROM OADM),0,7) AS "CodEntidad",
T0."RefDate" AS "FecDia",
SUBSTRING('',0,100) AS "NumRegistro",
SUBSTRING(T1."Account",0,20) AS "CodCtaContable",
SUBSTRING(T2."GroupMask",0,20) AS "CodTipoCuenta",
SUBSTRING((CASE T3."GrpCode" WHEN  'PD' THEN T1."OcrCode5" ELSE '' END ),0,8) AS "CodProductoContable",
SUBSTRING((CASE T3."GrpCode" WHEN  'CC' THEN T1."OcrCode5" ELSE '' END ),0,8) AS "CodCentroCosto",
SUBSTRING(IFNULL(T1."FCCurrency",'PEN'),0,4) AS "CodMonedaContable",
ABS(T2."CurrTotal") AS "MontoSaldoContable",
SUBSTRING('',0,6) AS "Filler01", 
SUBSTRING('',0,6) AS "Filler02"
FROM OJDT T0 
	INNER JOIN JDT1 T1 ON T0."TransId" = T1."TransId"
	INNER JOIN OACT T2 ON T2."AcctCode" = T1."Account"
	LEFT JOIN OPRC T3 ON T1."OcrCode5" = T3."PrcCode"
WHERE T0."TransType" NOT IN (-2,-3)
	  AND T1."RefDate" = :FechaEjecucion
GROUP BY T1."Account",
		 T0."RefDate",
		 T1."RefDate",
		 T1."OcrCode5",
		 T0."BaseRef",
		 T1."Line_ID",
		 T1."DebCred",
		 T1."Credit",
		 T2."GroupMask",
		 T0."Number",
		 T0."FolioPref",
		 T0."FolioNum",
		 T0."TransCurr",
		 T0."CreateTime",
		 T3."GrpCode",
		 T1."U_BCS_Afiliada",
		 T1."FCCurrency",
		 T0."StornoToTr",
		 T2."CurrTotal"
ORDER BY T0."Number", T1."Line_ID"
		 