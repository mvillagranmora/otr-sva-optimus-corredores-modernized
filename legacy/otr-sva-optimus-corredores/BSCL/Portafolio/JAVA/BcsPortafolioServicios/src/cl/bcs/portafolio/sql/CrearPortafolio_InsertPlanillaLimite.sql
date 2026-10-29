INSERT INTO "BSCL.Plataforma.BD::LIM.PlantillaLimite" (
    "IdPlantillaLimite",
    "DescripcionPlantillaLimite",
    "FechaCreacion",
    "FechaModificacion",
    "FechaIngreso",
    "IdEstadoAutorizacion",
    "IdUsuarioCreador",
    "IdEstado",
    "PorDefecto",
    "IdEmpresa" ) 
VALUES (
      :IdPlantillaExtra,
      :IdPlantillaExtra,
      CURRENT_UTCTIMESTAMP, --p_Creacion, -- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
      CURRENT_UTCTIMESTAMP,
      :FechaIngreso,
      'AUT',
      :IdUsuario,
      'ACT',
      'N',
      :IdEmpresa
);