SELECT
"IdPreferenciaPago"
,"NombreTipoOperacion" AS "Nombre"
FROM "BSCL.Plataforma.BD.Vistas::PAG.ConsultarPreferenciaPago"
WHERE "IdPreferenciaPago" in (:IdsPreferenciaPago);