SELECT
	TO_NVARCHAR("IdPais") AS "IdPais"
	,"NombrePais"
FROM "BSCL.Plataforma.BD.Vistas::REG.ConsultarPais"
WHERE TO_NVARCHAR("IdPais") in (:IdsPais);