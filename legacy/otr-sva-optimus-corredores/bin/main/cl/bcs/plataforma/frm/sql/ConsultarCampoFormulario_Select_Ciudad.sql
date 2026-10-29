SELECT
TO_NVARCHAR("IdCiudad") AS "IdCiudad", 
"NombreCiudad"
FROM "BSCL.Plataforma.BD.Vistas::REG.ConsultarCiudad"
WHERE TO_NVARCHAR("IdCiudad") in (:IdsCiudad);