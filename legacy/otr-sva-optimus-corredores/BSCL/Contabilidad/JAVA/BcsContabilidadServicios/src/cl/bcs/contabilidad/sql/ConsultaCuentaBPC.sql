SELECT 
SUBSTRING((SELECT "FreeZoneNo" FROM OADM),0,4) AS "CodigoSociedad",
SUBSTRING(T0."AcctCode",0,10) AS "CuentaOperativa",
SUBSTRING(T0."AcctName",0,60) AS "Descripcion",
SUBSTRING(T0."GroupMask",0,3) AS "TipoCuenta",
SUBSTRING(IFNULL(T0."AccntntCod",''),0,10) AS "CuentaGrupo"
FROM OACT T0;