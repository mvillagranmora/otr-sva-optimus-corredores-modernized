SELECT
TO_NVARCHAR("IdNombreFantasia") AS "IdNombreFantasia", 
"DescripcionNombreFantasia"
FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombresFantasia"
WHERE TO_NVARCHAR("IdNombreFantasia") in (:IdsNombreFantasia);