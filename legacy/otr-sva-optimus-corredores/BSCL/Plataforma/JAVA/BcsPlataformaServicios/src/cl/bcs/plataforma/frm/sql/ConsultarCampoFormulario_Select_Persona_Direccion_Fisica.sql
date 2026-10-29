SELECT
TO_NVARCHAR("IdDireccionFisica") AS "IdDireccionFisica", 
"NombreLargo"
FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica"
WHERE TO_NVARCHAR("IdDireccionFisica") in (:IdsDireccionFisica);