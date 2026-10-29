INSERT INTO "BSCL.Plataforma.BD::INS.RelacionClasificacionActivos"
(
 "IdRelacionClasificacion",
 "IdArbolClasificacionActivo",
 "IdClasificacionTipoInstrumento",
 "IdTipoInstrumento",
 "IdInstrumento")
VALUES("BSCL.Plataforma.BD.Secuencias::RelacionInstrumento".NEXTVAL,
:IdArbolClasificacionActivo,
:IdClasificacionTipoInstrumento,
:IdTipoInstrumento,
:IdInstrumento);