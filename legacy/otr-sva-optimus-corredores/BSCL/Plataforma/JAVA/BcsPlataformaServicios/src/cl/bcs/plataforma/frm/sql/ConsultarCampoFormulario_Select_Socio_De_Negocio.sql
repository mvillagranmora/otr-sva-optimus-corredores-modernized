SELECT
TO_NVARCHAR("IdSocioDeNegocio") AS "IdSocioDeNegocio" 
,"NombreCompleto"
FROM "BSCL.SocioNegocio.BD.Vistas.UI::ConsultarSocioDeNegocio"
WHERE TO_NVARCHAR("IdSocioDeNegocio") in (:IdSocioDeNegocio);
