INSERT INTO "BSCL.Plataforma.BD::COM.PerfilProductos" (
    "IdPerfilProductos",
    "DescripcionPerfilProductos",
    "IdCategoriaInversionista",
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
      '0',
      'S',
      '0',
      '0',
      CURRENT_UTCTIMESTAMP, --p_Creacion, -- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
      CURRENT_UTCTIMESTAMP,               
      'ACT',
      'N',
      :IdEmpresa,
      :FechaIngreso,
      'AUT',
      :IdUsuario
);