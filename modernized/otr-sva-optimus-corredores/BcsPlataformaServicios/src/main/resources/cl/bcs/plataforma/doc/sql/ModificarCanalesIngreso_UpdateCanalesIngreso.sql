UPDATE "BSCL.Plataforma.BD::DOC.CanalesDocumento"
SET "IdTipoCanalIngreso"=:IdTipoCanalIngreso
, "IdTipoDocumento"=:IdTipoDocumento
WHERE "IdTipoCanalIngreso"=:IdTipoCanalIngreso
AND "IdTipoDocumento"=:IdTipoDocumento;