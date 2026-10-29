SELECT COUNT("IdPersonaEmpleador") AS "Contador"
FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona"
WHERE "IdPersona" = :IdPersona AND "IdPersonaEmpleador" > 0
