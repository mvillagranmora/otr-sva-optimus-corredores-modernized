SELECT 
	 COALESCE("PorcentajeCastigo",0) AS "PorcentajeCastigo"
FROM "BSCL.Plataforma.BD::INS.Instrumento" 
WHERE "IdInstrumento" = :IdInstrumento;
