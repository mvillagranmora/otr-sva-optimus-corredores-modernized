SET SCHEMA "BSCL";

UPDATE BSCL."BSCL.Plataforma.BD::BAT.Agenda"
SET "habilitado"='N'
WHERE "IdTipoPrograma"='GENERAR1064PAUTA_R';

UPDATE BSCL."BSCL.Plataforma.BD::BAT.Agenda"
SET "habilitado"='N'
WHERE "IdTipoPrograma"='GENERAR1064PAUTA_FR';
