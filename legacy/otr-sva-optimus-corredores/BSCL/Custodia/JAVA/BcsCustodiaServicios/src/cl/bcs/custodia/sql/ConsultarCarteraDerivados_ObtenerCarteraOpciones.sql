SELECT 
CAR."DocumentoIdentidad",
CAR."RazonSocial" AS "SocioNegocio",
CAR."IdPortafolio",
CAR."CodigoPortafolio",
CAR."IdEstado", 
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
CAR."PrecioCompra" AS "PrecioEjercicio",
CAR."FechaOperacion" AS "FechaInver",
CASE WHEN CAR."FolioCarteraRelacionada" > 0 THEN 'POSICION' WHEN CAR."FolioCarteraRelacionada" = 0 THEN 'TRANSACCION' END AS "TipoPosicion",
CASE WHEN EJE."ContratosEjercidos" IS NULL THEN 0 ELSE EJE."ContratosEjercidos" END AS "ContratosEjercidos"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" CAR
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS INS ON (INS."Nemotecnico" = CAR."Nemotecnico")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarSubyacenteOpciones" AS SUB ON (SUB."Nemotecnico" = CAR."Nemotecnico")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion" AS OPE ON (OPE."IdTipoOperacion" = CAR."IdTipoOperacion")
LEFT JOIN "BSCL.Custodia.BD::DER.OpcionesEjercicioBloqueo" AS EJE ON (EJE."IdCartera" = CAR."IdCartera")
WHERE CAR."IdClasificacionTipoInstrumento" = 'DV'
AND CAR."IdTipoProducto" != 'MEFW'
AND (SUBSTRING(CAR."IdTipoOperacion", 0, 4) = 'COMO' OR SUBSTRING(CAR."IdTipoOperacion", 0, 4) = 'VENO')
AND CAR."FechaOperacion" = :p_FechaOperacion
ORDER BY CAR."IdCartera";