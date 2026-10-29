SELECT 
SUBSTRING("AcctCode",0,20) AS "TipoCuenta", 
SUBSTRING("AcctName", 0,100) AS "Descripcion" 
FROM OACT C1 
WHERE "Levels" = 1 