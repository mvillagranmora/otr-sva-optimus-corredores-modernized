SELECT
TO_NVARCHAR("CodigoTipoCargoPersona") AS "CodigoTipoCargoPersona", 
"NombreTipoCargoPersona"
FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarTipoCargoPersona"
WHERE TO_NVARCHAR("CodigoTipoCargoPersona") in (:IdsCodigoTipoCargoPersona);