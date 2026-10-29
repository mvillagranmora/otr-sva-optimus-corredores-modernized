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
CASE WHEN INS."ModalidadLiquidacion" = 'F' THEN 'FISICA' WHEN INS."ModalidadLiquidacion" = 'C' THEN 'FINANCIERA' ELSE '' END AS "ModalidadLiquidacion",
CASE WHEN INS."ModalidadEjercicio" = 'A' THEN 'AUTOMATICA' WHEN INS."ModalidadEjercicio" = 'M' THEN 'MANUAL' ELSE '' END AS "ModalidadEjercicio",
CASE WHEN INS."EstiloOpcion" = 'A' THEN 'AMERICANA' WHEN INS."EstiloOpcion" = 'E' THEN 'EUROPEA' ELSE '' END AS "Estilo",
INS."FechaVencimiento",
CAR."PrecioCompra" AS "PrecioEjercicio",
CAR."FechaOperacion" AS "FechaInver"
FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS INS ON (INS."Nemotecnico" = CAR."Nemotecnico")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarSubyacenteOpciones" AS SUB ON (SUB."Nemotecnico" = CAR."Nemotecnico")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion" AS OPE ON (OPE."IdTipoOperacion" = CAR."IdTipoOperacion")
WHERE CAR."IdClasificacionTipoInstrumento" = 'DV'
AND CAR."IdTipoProducto" != 'MEFW'
AND (SUBSTRING(CAR."IdTipoOperacion", 0, 4) = 'COMF' OR SUBSTRING(CAR."IdTipoOperacion", 0, 4) = 'VENF')
AND CAR."FolioCarteraRelacionada" = 0
AND CAR."FechaOperacion" = :p_FechaOperacion
AND CAR."IdCliente" = :p_IdCliente
AND CAR."IdPortafolio" = :p_IdPortafolio
AND OPE."Accion" = :p_Accion
ORDER BY CAR."IdCartera";
