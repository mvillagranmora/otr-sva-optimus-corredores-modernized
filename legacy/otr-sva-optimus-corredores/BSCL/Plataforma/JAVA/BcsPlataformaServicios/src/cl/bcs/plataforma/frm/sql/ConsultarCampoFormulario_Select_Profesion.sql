SELECT
 TO_NVARCHAR("IdProfesion") AS "IdProfesion", 
"Nombre"
FROM "BSCL.Plataforma.BD.Vistas::REG.ConsultarProfesion"
WHERE TO_NVARCHAR("IdProfesion") in (:IdsProfesion);