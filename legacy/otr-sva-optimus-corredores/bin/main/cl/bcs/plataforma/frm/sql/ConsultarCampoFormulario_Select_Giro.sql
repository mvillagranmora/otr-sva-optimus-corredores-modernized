SELECT
	TO_NVARCHAR("IdGiro") AS "IdGiro"
	,"Nombre"
FROM "BSCL.Plataforma.BD.Vistas::REG.ConsultarGiro"
WHERE TO_NVARCHAR("IdGiro") in (:IdsGiro);