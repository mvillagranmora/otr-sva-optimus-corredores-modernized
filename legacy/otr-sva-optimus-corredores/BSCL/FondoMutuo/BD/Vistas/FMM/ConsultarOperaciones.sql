SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarOperaciones";
GO
CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarOperaciones" AS
((SELECT
    FMM."IdCartera",
	FMM."FechaIngresoRegistro"AS  "FechaCreacion",
	FMM."FechaAceptacion" AS "FechaMovimiento",
	FMM."IdSolicitudAporteRescate",
	FMM."FolioContrato",
	FMM."IdCliente" as "IdCliente",
	FMM."IdPortafolio" AS "IdPortafolio",
	FONDO."IdEmisor" as "IdEmisor",
	INS."Nemotecnico" as "Nemotecnico",
	FMM."NumeroCuotas" AS "Cantidad",
	FMM."ValorCuota" AS "Precio",
	FMM."IdMoneda" AS "IdMoneda",
	FMM."Monto" ,
	CASE FMM."TipoSolicitud" WHEN 'APO' THEN 'APORTE'
	                         WHEN 'RES' THEN 'RESCATE'
							 ELSE ''    END  AS "AccionOperacion",
	CASE FMM."TipoSolicitud" when 'APO' THEN FMM."Monto"  ELSE 0 END AS "MontoCompra",
	CASE FMM."TipoSolicitud" when 'RES' THEN FMM."Monto"  ELSE 0 END AS "MontoVenta",
	FMM."MontoComision" AS "Comision",
	TINS."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumento",
	INS."IdTipoInstrumento" AS "IdTipoInstrumento",
	FMM."IdTipoMovimientoFondoMutuo",
	FMM."FechaAceptacion" AS "FechaTransaccion",
	'BCS' AS "Bolsa",
	'INFM' AS  "IdTipoProducto",
	FMM."TipoSolicitud",
	CASE FMM."TipoSolicitud" when 'APO' THEN 'APORFM'
							 when 'RES' THEN 'RESCFM'
	                         ELSE '' END AS "IdTipoOperacion",
	TipoMov."Descripcion" AS "TipoOperacion",
	INS."IdInstrumento" as "IdInstrumento",
	FMM."Estado"  AS "IdEstado",
	
	IDENT."DocumentoIdentidad",	
	CASE PER."Tipo" 
		WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"
		ELSE PER."RazonSocial"
	END  as "NombreCompleto",
	FMM."IdTipoCanalIngreso" AS "Canal"
	
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS FMM
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SERIE ON FMM."IdSerieFondo" = SERIE."IdSerieFondo"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS FONDO ON SERIE."IdFondo" = FONDO."IdFondo"
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON SERIE."IdInstrumento" = INS."IdInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TINS ON TINS."IdTipoInstrumento" = INS."IdTipoInstrumento"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" AS TipoMov ON (TipoMov."IdTipoMovimientoFondoMutuo" = FMM."IdTipoMovimientoFondoMutuo")
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (FMM."IdCliente" = CLI."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON CLI."IdPersona" = PER."IdPersona" 
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad" and IDENT."Predeterminado" = 'S'
WHERE FMM."Estado" <> 'ANU' AND FMM."IdTipoMovimientoFondoMutuo"  IN ('APONORM','APOOVER','APOREINV','APOTRASP','APORESPRO','RESNORM','RESREINV','RESTRANSF','RESTRASP','RESCAM')) UNION ALL (SELECT
	FMM."IdCartera",
	FMM."FechaIngresoRegistro"AS  "FechaCreacion",
	FMM."FechaAceptacion" AS "FechaMovimiento",
	FMM."IdSolicitudAporteRescate",
	FMM."FolioContrato",
	CASE FMM."IdTipoMovimientoFondoMutuo" 
	WHEN 'RESTRANSF' THEN FMM."IdClienteDestino"
	ELSE FMM."IdCliente" END AS "IdCliente",
	CASE FMM."IdTipoMovimientoFondoMutuo" 
	WHEN 'RESCAM' THEN FMM."IdPortafolio"
	ELSE FMM."IdPortafolioDestino" END AS "IdPortafolio",
	FONDO."IdEmisor" as "IdEmisor",
	CUSTO."Nemotecnico" as "Nemotecnico",
	CUSTO."Cantidad"     AS "Cantidad",
	CUSTO."PrecioCompra" AS "Precio",
	FMM."IdMoneda" AS "IdMoneda",
	CUSTO."MontoCompra" AS "Monto",
	CASE FMM."IdTipoMovimientoFondoMutuo" WHEN 'RESTRANSF' THEN 'APORTE'
							 			  WHEN 'RESTRASP'  THEN 'APORTE'
							 			  WHEN 'RESCAM'    THEN 'APORTE'
							 			  WHEN 'INGTIT'    THEN 'APORTE'
							 			  WHEN 'INGGTA'    THEN 'APORTE'
							 			  WHEN 'APORESPRO' THEN 'APORTE'
							 			  WHEN 'APOREINV'  THEN 'APORTE'
							 			  WHEN 'APOOVER'   THEN 'APORTE'
							 			  WHEN 'APONORM'   THEN 'APORTE'
							 			  
							 			  WHEN 'RESNORM'   THEN 'RESCATE'
							 			  WHEN 'RESREINV'  THEN 'RESCATE'
							 			  WHEN 'RETGTA'    THEN 'RESCATE'
							 			  WHEN 'RETTIT'    THEN 'RESCATE'
							 ELSE '' END AS "AccionOperacion",

	CASE FMM."IdTipoMovimientoFondoMutuo" WHEN 'RESTRANSF' THEN CUSTO."MontoCompra"
							 			  WHEN 'RESTRASP'  THEN CUSTO."MontoCompra"
							 			  WHEN 'RESCAM'    THEN CUSTO."MontoCompra"
							 			  WHEN 'INGTIT'    THEN CUSTO."MontoCompra"
							 			  WHEN 'INGGTA'    THEN CUSTO."MontoCompra"
							 			  WHEN 'APORESPRO' THEN CUSTO."MontoCompra"
							 			  WHEN 'APOREINV'  THEN CUSTO."MontoCompra"
							 			  WHEN 'APOOVER'   THEN CUSTO."MontoCompra"
							 			  WHEN 'APONORM'   THEN CUSTO."MontoCompra"
							 	ELSE 0 END AS "MontoCompra",
	
	CASE FMM."IdTipoMovimientoFondoMutuo" WHEN 'RESNORM'   THEN CUSTO."MontoCompra"
							 			  WHEN 'RESREINV'  THEN CUSTO."MontoCompra"
							 			  WHEN 'RETGTA'    THEN CUSTO."MontoCompra"
							 			  WHEN 'RETTIT'    THEN CUSTO."MontoCompra" ELSE 0 END AS "MontoVenta",	
	FMM."MontoComision" AS "Comision",
	TINS."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumento",
	INS."IdTipoInstrumento" AS "IdTipoInstrumento",
	FMM."IdTipoMovimientoFondoMutuo",
	FMM."FechaAceptacion" AS "FechaTransaccion",
	'BCS' AS "Bolsa",
	'INFM' AS  "IdTipoProducto",
	FMM."TipoSolicitud",
	CASE FMM."IdTipoMovimientoFondoMutuo" WHEN 'RESTRANSF' THEN 'APORFM'
							 			  WHEN 'RESTRASP'  THEN 'APORFM'
							 			  WHEN 'RESCAM'    THEN 'APORFM'
							 			  WHEN 'INGTIT'    THEN 'APORFM'
							 			  WHEN 'INGGTA'    THEN 'APORFM'
							 			  WHEN 'APORESPRO' THEN 'APORFM'
							 			  WHEN 'APOREINV'  THEN 'APORFM'
							 			  WHEN 'APOOVER'   THEN 'APORFM'
							 			  WHEN 'APONORM'   THEN 'APORFM'
							 			  
							 			  WHEN 'RESNORM'   THEN 'RESCFM'
							 			  WHEN 'RESREINV'  THEN 'RESCFM'
							 			  WHEN 'RETGTA'    THEN 'RESCFM'
							 			  WHEN 'RETTIT'    THEN 'RESCFM' END AS "IdTipoOperacion",
	TipoMov."Descripcion" AS "TipoOperacion",
	INS."IdInstrumento" as "IdInstrumento",
	FMM."Estado"  AS "IdEstado",
	
	IDENT."DocumentoIdentidad",	
	CASE PER."Tipo" 
		WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"
		ELSE PER."RazonSocial"
	END  as "NombreCompleto",
	FMM."IdTipoCanalIngreso" AS "Canal"

FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS FMM
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SERIE ON FMM."IdSerieFondo" = SERIE."IdSerieFondo"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS FONDO ON SERIE."IdFondo" = FONDO."IdFondo"
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON SERIE."IdInstrumento" = INS."IdInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TINS ON TINS."IdTipoInstrumento" = INS."IdTipoInstrumento"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" AS TipoMov ON (TipoMov."IdTipoMovimientoFondoMutuo" = FMM."IdTipoMovimientoFondoMutuo")
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CUSTO ON CUSTO."IdCartera" = FMM."IdCartera" --AND CUSTO."IdPortafolio" = FMM."IdPortafolio" AND

LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (FMM."IdCliente" = CLI."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON CLI."IdPersona" = PER."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad" and IDENT."Predeterminado" = 'S'

WHERE FMM."Estado" <> 'ANU' AND FMM."IdTipoMovimientoFondoMutuo"  IN ('RESTRANSF','RESTRASP','RESCAM')));
GO 
GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarOperaciones" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
