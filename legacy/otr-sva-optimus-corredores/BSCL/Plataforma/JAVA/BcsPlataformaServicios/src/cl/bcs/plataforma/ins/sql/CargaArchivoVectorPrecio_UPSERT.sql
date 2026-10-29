UPSERT "BSCL.Plataforma.BD::INS.EntidadVectorPrecio"
("Fecha", "Nemotecnico", "Entidad", "TipoRegistro", "ValorRegistro") 
VALUES(
:FECHA,
:NEMOTECNICO,
:ENTIDAD,
:TIPOREGISTRO,
:VALORREGISTRO) 
WHERE "Fecha"=:FECHA AND "Nemotecnico"=:NEMOTECNICO AND "Entidad"=:ENTIDAD AND "TipoRegistro"=:TIPOREGISTRO;