INSERT INTO "BSCL.Mesa.BD::CAR.CargaMasiva"(
"IdCargaMasiva"
, "IdEmpresa"
, "IdUsuario"
, "CantidadRegistros"
, "NombreArchivo"
, "IdEstado"
, "FechaCarga"
, "CodigoTipoCargaMasiva"
, "IdMesa")
VALUES(
:IdCargaMasiva
, :IdEmpresa
, :IdUsuario
, :CantidadRegistros
, :NombreArchivo
, 'ING'
, :FechaCarga
, 'CARGA_OPERACIONES_A_TERMINO_RF'
, :IdMesa
);
