INSERT INTO "BSCL.Plataforma.BD::COM_2.PerfilCobros" (
    "IdPerfilCobros",
    "DescripcionPerfilCobros",
    "EsPerfilPersonalizado",
    "TipoEntidadReferencia",
    "IdReferencia",
    "FechaCreacion",
    "FechaModificacion",
    "IdEstado",
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
      :IdEmpresa,
      :FechaIngreso,
      'AUT',
      :IdUsuario
);