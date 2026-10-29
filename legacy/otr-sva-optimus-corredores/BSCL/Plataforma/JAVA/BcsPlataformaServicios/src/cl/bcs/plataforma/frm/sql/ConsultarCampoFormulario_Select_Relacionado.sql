SELECT  TOP 1
TO_NVARCHAR("IdPersonaDestino") AS "IdPersonaDestino"
,"NombreCompleto"
FROM "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionPersona" 
WHERE TO_NVARCHAR("IdPersonaDestino") IN (:IdPersonaDestino);
