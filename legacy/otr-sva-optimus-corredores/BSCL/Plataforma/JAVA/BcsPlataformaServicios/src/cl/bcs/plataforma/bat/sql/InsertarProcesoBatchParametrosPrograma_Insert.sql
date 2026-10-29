INSERT INTO "BSCL.Plataforma.BD::BAT.ParametrosPrograma"
("IdSecuencia","IdGrupoParametro","IdPrograma","Orden","NombreParametro","Valor","ValorBlob")
values ("BSCL.Plataforma.BD.Secuencias::ParametrosProgramaSecuencia".nextval, :IdGrupoParametro, :IdPrograma,1,:NombreParametro,:Valor,:BlobStream);
