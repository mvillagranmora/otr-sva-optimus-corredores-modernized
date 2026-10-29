SELECT 
CAR."IdCartera",
CAR."DocumentoIdentidad",
CAR."RazonSocial" AS "SocioNegocio",
CAR."CodigoPortafolio",
CAR."IdClasificacionTipoInstrumento",
CAR."IdTipoInstrumento",
CAR."IdInstrumento",
CAR."Nemotecnico" AS "Serie",
SUB."IdEmisor" AS "Subyacente",
INS."EsPutEsCall" AS "TipoOpcion",
CASE WHEN INS."EstiloOpcion" = 'A' THEN 'AMERICANA' WHEN INS."EstiloOpcion" = 'E' THEN 'EUROPEA' ELSE '' END AS "Estilo",
CASE WHEN INS."ModalidadLiquidacion" = 'P' THEN 'FISICA' WHEN INS."ModalidadLiquidacion" = 'C' THEN 'FINANCIERA' ELSE '' END AS "ModalidadLiquidacion",
CASE WHEN INS."ModalidadEjercicio" = 'A' THEN 'AUTOMATICA' WHEN INS."ModalidadEjercicio" = 'M' THEN 'MANUAL' ELSE '' END AS "ModalidadEjercicio",
CAR."FechaVencimientoOperacion" AS "FechaVencimiento",
INS."PrecioStrike" AS "PrecioEjercicio",
CAR."INGCantidad" AS "ContratosEjercibles",
CASE WHEN EJE."ContratosEjercidos" IS NULL THEN 0 ELSE EJE."ContratosEjercidos" END AS "ContratosEjercidos",
CASE WHEN EJE."Bloqueado" IS NULL THEN 'NO' ELSE EJE."Bloqueado" END AS "Bloqueado",
IFNULL(EJE."IdEstado",'') AS "IdEstado",
CAR."IdCliente",
CAR."IdPortafolio",
CAR."FolioCartera",
CAR."Tamano"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" CAR
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS INS ON (INS."Nemotecnico" = CAR."Nemotecnico")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarSubyacenteOpciones" AS SUB ON (SUB."Nemotecnico" = CAR."Nemotecnico")
LEFT JOIN "BSCL.Custodia.BD::DER.OpcionesEjercicioBloqueo" AS EJE ON (EJE."IdCartera" = CAR."IdCartera" AND EJE."IdEstado" = 'GUA')
WHERE CAR."IdClasificacionTipoInstrumento" = 'DV'
AND CAR."IdTipoProducto" != 'MEFW'
AND (SUBSTRING(CAR."IdTipoOperacion", 0, 4) = 'COMO' OR SUBSTRING(CAR."IdTipoOperacion", 0, 4) = 'VENO')
AND "FolioCarteraRelacionada" != 0
AND CAR."FechaVencimientoOperacion" <= :p_FechaConsulta AND CAR."FechaVencimientoOperacion" >= :p_FechaActual
AND INS."ModalidadEjercicio" = :p_Modalidad;