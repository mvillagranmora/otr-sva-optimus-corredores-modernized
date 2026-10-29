SELECT TOP 1
TO_NVARCHAR("IdPersonaRepresentante") AS "IdPersonaRepresentante" 
, "NombreCompletoRepresentante"
FROM "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRepresentanteLegal"
WHERE TO_NVARCHAR("IdPersonaRepresentante") in (:IdPersonaRepresentante);
