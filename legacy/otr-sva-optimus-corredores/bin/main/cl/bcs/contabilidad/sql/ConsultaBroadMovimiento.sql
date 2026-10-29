SELECT 
SUBSTRING('1',0,1) AS "CodInstitucion",
SUBSTRING('B1',0,2) AS "CodAplicativo",
SUBSTRING((SELECT IFNULL("FreeZoneNo",'') FROM OADM),0,7) AS "CodEntidad",
T0."RefDate" AS "FecDia",
T0."CreateTime" AS "HraTransaccion",
SUBSTRING(T0."Number",0,10) AS "NumOperacion",
SUBSTRING(T1."Line_ID" + 1,0,10) AS "NumPosOperacion",
SUBSTRING('',0,10) AS "NumRegistro",
SUBSTRING(TO_VARCHAR(T1."Account"),0,20) AS "CodCtaContable",
SUBSTRING(TO_VARCHAR (T2."GroupMask"),0,20) AS "CodTipoCuenta",
SUBSTRING(TO_VARCHAR((CASE T3."GrpCode" WHEN  'PD' THEN T1."OcrCode5" ELSE '' END )),0,8) AS "CodProductoContable",
SUBSTRING(TO_VARCHAR((CASE T3."GrpCode" WHEN  'CC' THEN T1."OcrCode5" ELSE '' END )),0,8) AS "CodCentroCosto",
T0."RefDate" AS "FecDocumento",
SUBSTRING(TO_VARCHAR(T0."BaseRef"),0,10) AS "NroSerieDoc",
SUBSTRING(TO_VARCHAR(T0."FolioNum"),0,11) AS "NroDoc",
SUBSTRING(TO_VARCHAR(T1."U_BCS_Afiliada"),0,4) AS "CodAfiliada",
SUBSTRING(TO_VARCHAR(T1."DebCred"),0,1) AS "TipCargoAbono",
SUBSTRING(IFNULL(T1."FCCurrency",'PEN'),0,4) AS "CodMonedaContable",
TO_DECIMAL(ABS(T1."Debit" - T1."Credit"),20,4) AS "MontoTransaccionContable",
SUBSTRING(TO_VARCHAR(T0."StornoToTr"),0,1) AS "IndAnulado",
SUBSTRING(CASE WHEN T0."StornoToTr" > 0 THEN 'X' ELSE '' END,0,1)  AS "IndOpeAfiliada",				    
SUBSTRING('',0,4) AS "Filler01", 
SUBSTRING('',0,4) AS "Filler02"
FROM OJDT T0 
	INNER JOIN JDT1 T1 ON T0."TransId" = T1."TransId"
	INNER JOIN OACT T2 ON T2."AcctCode" = T1."Account"
	LEFT JOIN OPRC T3 ON T1."OcrCode5" = T3."PrcCode"
WHERE T0."TransType" NOT IN (-2,-3)
	  AND T1."RefDate" = :FechaEjecucion
GROUP BY T1."Account",
		 T0."RefDate",
		 T1."OcrCode5",
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
		 T0."BaseRef",
		 T1."U_BCS_Afiliada",
		 T1."FCCurrency",
		 T0."StornoToTr",
		 T1."Debit",
		 T1."Credit"
ORDER BY T0."Number", T1."Line_ID"