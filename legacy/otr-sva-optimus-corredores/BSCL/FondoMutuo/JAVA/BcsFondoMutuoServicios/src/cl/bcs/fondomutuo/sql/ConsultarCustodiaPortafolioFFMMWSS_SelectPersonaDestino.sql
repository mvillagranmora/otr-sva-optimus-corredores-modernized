SELECT DISTINCT REL."IdPersonaDestino" 
FROM "BSCL.SocioNegocio.BD.Vistas::REL.ObtenerRelacionPersona" REL 
WHERE REL."IdPersona" = :IdPersonaCuentaPropia 
AND REL."IdEstado" = 'ACT';