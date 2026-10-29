UPSERT "BSCL.Plataforma.BD::INS.EntidadVectorPrecio"
("Fecha","Nemotecnico","Entidad","TipoRegistro","ValorRegistro") 
VALUES

(:FECHA,:INSTRUMENTO,:ENTIDAD,:TIPOREGISTRO,:VALOR)
WHERE "Nemotecnico"=:INSTRUMENTO
AND "Fecha" =:FECHA ;