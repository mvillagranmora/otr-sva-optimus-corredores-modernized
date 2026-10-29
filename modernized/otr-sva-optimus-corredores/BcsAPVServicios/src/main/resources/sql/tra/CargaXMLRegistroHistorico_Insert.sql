INSERT INTO "BSCL.APV.BD::APV.Registro_Historico_XML"(
"IDEMPRESA"
, "IDMODULO"
, "IDUSUARIO"
, "NOMBRECARGA"
, "ORIGEN"
, "FECHACARGA"
, "ARCHIVO"
)
VALUES(
  :p_IdEmpresa
, :p_IdModulo
, :p_IdUsuario
, :p_NombreCarga
, :p_Origen
, :p_FechaCarga
, :p_Archivo
);