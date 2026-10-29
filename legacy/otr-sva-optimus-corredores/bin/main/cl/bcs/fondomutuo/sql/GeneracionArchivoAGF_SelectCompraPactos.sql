SELECT
COALESCE("Operaciones"."FolioTransacion",'0') || ';' ||
 CASE "Operaciones"."FechaMovimiento" WHEN '' THEN '0001-01-01'
                                      ELSE TO_NVARCHAR("Operaciones"."FechaMovimiento",'YYYY-MM-DD')
 END ||	';' || 	
 0 || ';' || --falta FolEve
 COALESCE("SerieDeFondo"."IdFondo",'') || ';' ||
 '' || ';' ||
 COALESCE("ConsultarCliente"."DocumentoIdentidad",'') || ';' ||
 COALESCE("ConsultarPortafolio"."CodigoPortafolio",'') || ';' ||
 COALESCE("Operaciones"."MontoCompra",0) || ';' ||
 COALESCE("Operaciones"."Precio",0) || ';' ||
 COALESCE("Operaciones"."Cantidad",0) || ';' ||
 COALESCE("Operaciones"."Tasa",0) || ';' ||
 CASE "TipoOperacion"."Accion" WHEN 'COMPRA' THEN 'C'
							   ELSE ''
 END || ';' || --TipoMovimiento
 'CI' || ';' || --TipoOperacion
 COALESCE("Operaciones"."Nemotecnico",'') || ';' ||
 COALESCE("Operaciones"."IdEmisor",'') || ';' ||
 CASE "Instrumento"."IdTipoReajuste" WHEN 'USD' THEN 'US$'
 									 WHEN 'CLP' THEN '$'
 									 WHEN ''  THEN '$'
 									 ELSE '$'
 END || ';' ||
 COALESCE("Instrumento"."FechaVencimiento",'0001-01-01') || ';' ||
 COALESCE("Operaciones"."ValorRescateInicial",0) || ';' ||
 COALESCE("Operaciones"."IdBoveda",'') || ';' ||
 COALESCE("CondicionDeLiquidacion"."Codigo",'') || ';' ||
 '' || ';' || --AS "FechaVencimientoPacto"
 '0' || ';' || --AS "PlazoPacto"
 '0' || ';' || --AS "TasaCompromiso"
 CASE 'CLP'                          WHEN 'USD' THEN 'US$'
 									 WHEN 'CLP' THEN '$'
 									 WHEN 'UF' THEN 'UF'
 									 WHEN ''  THEN '$'
 									 ELSE '$'
 END || ';' || --dummy, reajuste por compromiso
 '0' || ';' || --Monto total operacion, hay que ver de donde sale
 'GESFFMM' || ';' ||
 
 '' AS "Registro"
  FROM "BSCL.Mesa.BD.Vistas::MRF.ConsultarOperaciones" AS "Operaciones"
  INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS "ConsultarPortafolio" ON  ( "ConsultarPortafolio"."IdPortafolio" = "Operaciones"."IdPortafolio" )
  INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "ConsultarCliente" ON  ( "ConsultarCliente"."IdCliente" = "ConsultarPortafolio"."IdCliente" )
  INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS "INDICADOR_PORTAFOLIO" ON ("INDICADOR_PORTAFOLIO"."IdPortafolio" = "ConsultarPortafolio"."IdPortafolio" 
											  												AND "INDICADOR_PORTAFOLIO"."CodigoTipoIndicador" = :Ambiente 
											  												AND "INDICADOR_PORTAFOLIO"."ValorIndicador" = 'S')
  LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS "TipoOperacion" ON ("TipoOperacion"."IdTipoOperacion" = "Operaciones"."IdTipoOperacion")
  LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS "Instrumento" ON ("Instrumento"."IdInstrumento" = "Operaciones"."IdInstrumento")
  LEFT OUTER JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSerieDeFondo" AS "SerieDeFondo" ON ("SerieDeFondo"."IdPortafolio" = "Operaciones"."IdPortafolio")
  LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" AS "CondicionDeLiquidacion" ON ("CondicionDeLiquidacion"."IdCondicionDeLiquidacion" = "Operaciones"."IdCondicionLiquidacion")
  WHERE "Operaciones"."IdTipoOperacion" = 'COMPAC' AND (TO_DATE("Operaciones"."FechaMovimiento") BETWEEN TO_DATE(:FechaDesde) AND TO_DATE(:FechaHasta)) AND "ConsultarCliente"."IdEmpresa" = :IdEmpresa