INSERT INTO "BSCL.Plataforma.BD::INS.Corredora"
SELECT
"BSCL.Plataforma.BD.Secuencias::Corredora".NEXTVAL AS "IdCorredora",
CP."IdPersona" AS "IdPersona",
TO_DECIMAL(:COD_CORR_SVS,10,0) AS "CodigoInscripcionRegulador",
'ACT' AS "IdEstado",
'N' AS "Borrado",
:NEMO AS "NombreCorto"
FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" CP
WHERE CP."DocumentoIdentidad" = :RUT_CORR;