SELECT
	TO_NVARCHAR("IdDivisionPais") AS "IdDivisionPais"
	,"NombreDivisionPaisClasificacion1"
FROM "BSCL.Plataforma.BD.Vistas::REG.ConsultarDivisionPais"
WHERE TO_NVARCHAR("IdDivisionPais") in (:IdsDivisionPais);