SELECT 
"IdGrupoDireccion"
,"Grupo"
,"Comentario"
,"IdEnrolamiento"
,"IdGrupoDireccionPersona"
,"IdGrupoDireccionPersona" AS "IdGrupoDireccionReal"
FROM "BSCL.SocioNegocio.BD::ENR.GrupoDireccion"
WHERE "IdEnrolamiento" IN (:IdEnrolamiento);
