SET SCHEMA "BSCL";

UPDATE BSCL."BSCL.Plataforma.BD::PAU.Proceso"
SET "CodigoModulo"='SIS', "IdEstado"='ACT'
WHERE "CodigoProceso"='GENERAR1064PAUTA_R';

UPDATE BSCL."BSCL.Plataforma.BD::PAU.Proceso"
SET "CodigoModulo"='SIS', "IdEstado"='ACT'
WHERE "CodigoProceso"='GENERAR1064PAUTA_FR';