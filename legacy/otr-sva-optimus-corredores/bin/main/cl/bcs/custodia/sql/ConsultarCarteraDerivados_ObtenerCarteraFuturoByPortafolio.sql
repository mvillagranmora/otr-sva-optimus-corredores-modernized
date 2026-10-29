SELECT 
CAR."DocumentoIdentidad",
CAR."RazonSocial",
CAR."CodigoPortafolio",
CAR."FolioCartera",
CAR."Nemotecnico" AS "Instrumento",
CAR."TipoInstrumento",
OPE."Accion" AS "Tipo",
CAR."Cantidad",
CAR."FechaOperacion",
CAR."FechaVencimientoOperacion",
CAR."PrecioCompra",
CAR."TasaCompra",
CAR."INGTasaMercadoActual" AS "TasaMercado",
CAR."MontoCompra",
CAR."INGMontoATasaMercadoActual" AS "ValorMercado",
CAR."FolioExternoReferencia" AS "FolioTransaccion",
CAR."FechaLiquidacion",
EMP."NombreCompleto" AS "Agente",
SUC."NombreSucursal",
BOL."Nombre" AS "BolsaOrigen",
CAR."IdTipoComprobante" AS "Documento",  
CAR."FolioComprobante" AS "FolioComprobante",
INS."Precio" AS "Precio",
CAR."IdCondicionLiquidacion" AS "Liquidacion",
CAR."FolioCartera" AS "SecuenciaMovimiento",
CAR."INGPrecioMercadoActual" AS "PrecioBaseFuturos",
0.0 AS "MargenExigido", --Agregar margen exigido
0.0 AS "ResultadoDiario", --Agregar resultado diario
0.0 AS "ResultadoAcumulado", --Agregar resultado acumulado
CAR."IdPortafolio"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" CAR
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS INS ON (INS."Nemotecnico" = CAR."Nemotecnico")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarSubyacenteOpciones" AS SUB ON (SUB."Nemotecnico" = CAR."Nemotecnico")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion" AS OPE ON (OPE."IdTipoOperacion" = CAR."IdTipoOperacion")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Sucursal" AS SUC ON (SUC."IdSucursal" = CAR."IdSucursal")
LEFT JOIN "BSCL.Plataforma.BD::INS.Bolsa" AS BOL ON (BOL."IdBolsa" = CAR."IdBolsa")
LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS FAC ON (CAR."FolioComprobante" = FAC."FolioComprobante")
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" AS EMP ON (EMP."IdEmpleado" = CAR."IdAgente")
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI ON (CLI."IdCliente" = CAR."IdCliente")
WHERE CAR."IdClasificacionTipoInstrumento" = 'DV'
AND CAR."IdTipoProducto" != 'MEFW'
AND (SUBSTRING(CAR."IdTipoOperacion", 0, 4) = 'COMF' OR SUBSTRING(CAR."IdTipoOperacion", 0, 4) = 'VENF')
AND CAR."FechaOperacion" = :p_FechaOperacion
AND CAR."IdPortafolio" = :p_IdPortafolio;