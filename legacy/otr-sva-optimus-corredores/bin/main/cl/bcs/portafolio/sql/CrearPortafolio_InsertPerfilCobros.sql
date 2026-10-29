INSERT INTO "BSCL.Plataforma.BD::COM.PerfilCobros" (
    "IdPerfilCobros",
    "DescripcionPerfilCobros",
    "EsPerfilPersonalizado",
    "TipoEntidadReferencia",
    "IdReferencia",
    "FechaCreacion",
    "FechaModificacion",
    "IdEstado",
    "PorDefecto",
    "IdEmpresa",
    "FechaIngreso",
    "IdEstadoAutorizacion",
    "IdUsuarioCreador" ) 
VALUES (
      :IdPlantillaExtra,
      :IdPlantillaExtra,
      'S',
      '0',
      '0',
      CURRENT_UTCTIMESTAMP,
      CURRENT_UTCTIMESTAMP,               
      'ACT',
      'N',
      :IdEmpresa,
      :FechaIngreso,
      'AUT',
      :IdUsuario
);