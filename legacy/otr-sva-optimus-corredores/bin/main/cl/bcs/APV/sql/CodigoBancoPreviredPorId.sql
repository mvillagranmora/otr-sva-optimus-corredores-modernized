SELECT 
	 "IdBanco"
	,"CodigoBancoPrevired"
FROM "BSCL.APV.BD.Vistas::REC.ConsultarBancoPrevired"
WHERE "CodigoBancoPrevired" = :p_CodigoBanco AND "IdBanco" IS NOT NULL;