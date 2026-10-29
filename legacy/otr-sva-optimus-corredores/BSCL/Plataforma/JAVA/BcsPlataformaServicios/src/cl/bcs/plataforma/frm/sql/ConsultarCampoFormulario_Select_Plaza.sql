SELECT
	TO_NVARCHAR("IdPlaza") AS "IdPlaza"
	,"NombrePlaza"
FROM "BSCL.Plataforma.BD.Vistas::BAN.ConsultarPlaza"
WHERE TO_NVARCHAR("IdPlaza") in (:IdsPlaza);