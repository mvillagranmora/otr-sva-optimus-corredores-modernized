SELECT
TO_NVARCHAR("IdTelefono") AS "IdTelefono", 
"Numero"
FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono"
WHERE TO_NVARCHAR("IdTelefono") in (:IdsTelefono);