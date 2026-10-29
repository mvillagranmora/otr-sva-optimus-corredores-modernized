SELECT
 "IdPersona"
,"Tipo"
,"IdEstado"
,"IdTipoCanalIngresoCorredor"
FROM "BSCL.SocioNegocio.BD::PER.Persona"
WHERE "IdPersona" IN (:idPersona)