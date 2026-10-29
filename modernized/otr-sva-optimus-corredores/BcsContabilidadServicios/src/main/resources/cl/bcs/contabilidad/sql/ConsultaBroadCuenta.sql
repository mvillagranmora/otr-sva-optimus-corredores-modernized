SELECT 
SUBSTRING((SELECT "FreeZoneNo" FROM OADM),0,7) AS "CodEntidad",
SUBSTRING(IFNULL(T0."AcctCode",''),0,15) AS "CodCtaContable",
SUBSTRING(IFNULL(T0."AcctName",''),0,100) AS "Descripcion",
SUBSTRING(T0."GroupMask",0,20) AS "TipoCuenta",
SUBSTRING(IFNULL(T0."AccntntCod",''),0,10) AS "CuentaGrupo",
SUBSTRING(IFNULL(T0."ActCurr" ,''),0,4) AS "CodMonedaContable",
SUBSTRING(IFNULL(T0."Protected" ,''),0,1) AS "IndCtaConfidencial",
SUBSTRING(IFNULL(T0."LocManTran",''),0,1) AS "IndCtaAsociada",
SUBSTRING(IFNULL(T0."Finanse",''),0,1) AS "IndCtaMonetaria",
SUBSTRING(IFNULL(T0."CfwRlvnt",''),0,1) AS "IndRelevanteFlujoCaja",
SUBSTRING(IFNULL(CASE "U_BCS_Afiliada" WHEN '0' THEN 'N' ELSE 'Y' END,''),0,1) AS "IndCtaAfiliada"
FROM OACT T0;