SELECT 
    <<SELECT_CLAUSE>> AS "IdEntidad"
    ,PER."IdPersona" AS "IdPersona"
    <<SELECT_CLIENTE>>
FROM <<FROM_CLAUSE>> AS ENT
<<LEFT_CLIENT>>
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON <<IDENTIDAD>>."IdPersona" = PER."IdPersona"
WHERE <<SELECT_CLAUSE>> IN (:idEntidades) 
