SELECT 
	SUBSTRING("PrcCode",0,8) AS "CodccPro",
	SUBSTRING("PrcName",0,100) AS "Descripcion", 
	SUBSTRING("GrpCode",0,3) AS "Tipo"
FROM OPRC
