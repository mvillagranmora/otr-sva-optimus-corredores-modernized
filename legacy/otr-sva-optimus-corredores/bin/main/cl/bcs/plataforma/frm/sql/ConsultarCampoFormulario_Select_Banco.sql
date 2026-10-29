SELECT
	TO_NVARCHAR("IdBanco") AS "IdBanco"
	,"NombreBancoPais"
FROM "BSCL.Plataforma.BD.Vistas::BAN.ConsultarBanco"
WHERE TO_NVARCHAR("IdBanco") in (:IdsBanco);