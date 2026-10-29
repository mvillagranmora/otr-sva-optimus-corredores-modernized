SELECT INS."IdInstrumento",
INS."EsPutEsCall" AS "TipoOpcion",
INS."EstiloOpcion" AS "IdEstiloOpcion",
CASE WHEN INS."EstiloOpcion" = 'A' THEN 'AMERICANA' WHEN INS."EstiloOpcion" = 'E' THEN 'EUROPEA' ELSE '' END AS "EstiloOpcion",
INS."ModalidadLiquidacion" AS "IdModalidadLiquidacion",
CASE WHEN INS."ModalidadLiquidacion" = 'P' THEN 'FISICA' WHEN INS."ModalidadLiquidacion" = 'C' THEN 'FINANCIERA' ELSE '' END AS "ModalidadLiquidacion",
INS."ModalidadEjercicio" AS "IdModalidadEjercicio",
CASE WHEN INS."ModalidadEjercicio" = 'A' THEN 'AUTOMATICA' WHEN INS."ModalidadEjercicio" = 'M' THEN 'MANUAL' ELSE '' END AS "ModalidadEjercicio",
INS."Precio",
INS."FechaVencimiento",
INS."Tamano" AS "LotePadron"
FROM "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" INS
WHERE INS."Nemotecnico" = :p_Nemotecnico;