SELECT CAR."IdEstado", 
CAR."IdTipoOperacion",
CAR."Nemotecnico" AS "Instrumento", 
CAR."FolioCartera", 
CAR."FechaOperacion",
CAR."IdCartera",
CAR."INGCantidad" AS "Contratos",
OPE."Accion" AS "Tipo",
CAR."INGCantidad",
CAR."EGRCantidad",
CAR."FolioCarteraRelacionada",
SUB."CodigoClasificacion" AS "Mercado",
SUB."Descripcion" AS "Familia",
SUB."IdEmisor" AS "Subyacente",
INS."EsPutEsCall" AS "TipoOpcion",
CASE WHEN INS."ModalidadLiquidacion" = 'P' THEN 'FISICA' WHEN INS."ModalidadLiquidacion" = 'C' THEN 'FINANCIERA' ELSE '' END AS "ModalidadLiquidacion",
CASE WHEN INS."ModalidadEjercicio" = 'A' THEN 'AUTOMATICA' WHEN INS."ModalidadEjercicio" = 'M' THEN 'MANUAL' ELSE '' END AS "ModalidadEjercicio",
CASE WHEN INS."EstiloOpcion" = 'A' THEN 'AMERICANA' WHEN INS."EstiloOpcion" = 'E' THEN 'EUROPEA' ELSE '' END AS "Estilo",
INS."FechaVencimiento",
CAR."PrecioCompra" AS "PrecioEjercicio"
FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS INS ON (INS."Nemotecnico" = CAR."Nemotecnico")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarSubyacenteOpciones" AS SUB ON (SUB."Nemotecnico" = CAR."Nemotecnico")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion" AS OPE ON (OPE."IdTipoOperacion" = CAR."IdTipoOperacion")
WHERE CAR."IdClasificacionTipoInstrumento" = 'DV'
AND CAR."IdTipoProducto" != 'MEFW'
AND CAR."FolioCarteraRelacionada" > 0
AND CAR."FechaOperacion" = :p_FechaOperacion
AND CAR."IdPortafolio" = :p_IdPortafolio
ORDER BY CAR."IdCartera";
