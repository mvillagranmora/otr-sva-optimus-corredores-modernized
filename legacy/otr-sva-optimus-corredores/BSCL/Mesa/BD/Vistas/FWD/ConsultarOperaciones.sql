SET SCHEMA "BSCL";
GO 
DROP VIEW "BSCL.Mesa.BD.Vistas::FWD.ConsultarOperaciones";

-- OPERACIONES
--  Vista para Normativa.
--  Autor: CESPINOSA - BCS
--  Fecha Creacion: 16/09/2016
--  

GO
CREATE VIEW "BSCL.Mesa.BD.Vistas::FWD.ConsultarOperaciones" AS

	
SELECT
	 TO_SECONDDATE(CAR."FechaOperacion" || ' ' || TO_CHAR(OPE."FechaCreacion",'HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS') as "FechaCreacion"
	,TO_SECONDDATE(CAR."FechaOperacion" || ' ' || TO_CHAR(CASE 
															WHEN TO_DATE(OPE."FechaEdicion") <> '0001-01-01' AND OPE."FechaCreacion" <> OPE."FechaEdicion" THEN OPE."FechaOperacion"
															ELSE LOCALTOUTC(OPE."FechaOperacion", 'America/Santiago','platform')				
														  END,'HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS') as "FechaMovimiento"
	,CAR."FechaOperacion"
	,TO_CHAR(OPE."FechaCreacion",'HH24:MI')		AS "HoraOperacion"
	,CAR."FolioCartera"							AS "IdOperacion"
	,CAR."IdOperacion"							AS "FolioOperacion"
	,CLI."IdCliente"
	,CAR."IdPortafolio"
	,IDENT."DocumentoIdentidad"	
	,CASE PER."Tipo" 
		WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"
		ELSE PER."RazonSocial"
	END  as "NombreCompleto"
	,CAR."Nemotecnico"
	,CAR."FechaVencimientoOperacion" 			AS "FechaVencimiento"
	,CASE WHEN FPE."FechaLiquidacionComprobantePagoOperacion" > FPR."FechaLiquidacionComprobantePagoOperacion" THEN FPE."FechaLiquidacionComprobantePagoOperacion"
		   WHEN FPE."FechaLiquidacionComprobantePagoOperacion" < FPR."FechaLiquidacionComprobantePagoOperacion" THEN FPR."FechaLiquidacionComprobantePagoOperacion"
		   ELSE COALESCE(FPE."FechaLiquidacionComprobantePagoOperacion",FPR."FechaLiquidacionComprobantePagoOperacion") 
	   END AS "FechaLiquidacion"
		   
	,CAR."INGCantidad"
	,CAR."ValorTipoCambioCierre" 				AS "TipoCambioCierre"
	,COALESCE(CAR."EGRValorParidad",0)			AS "Precio"
	,CAR."INGMontoMonedaPrincipal"
	,CAR."INGMontoMonedaSecundaria"
	,CAR."IdMonedaPrincipal"
	,CAR."IdMonedaSecundaria"
	,CAR."INGTasaPuntosMonedaPrincipal"			AS "PuntosForward"
	,CAR."INGTasaPuntosMonedaSecundaria"		AS "TasaForward"
	,CAR."ValorParidadCierre" 					AS "ParidadCierre"	
	,TOPE."Accion" 								AS "AccionOperacion"
	
	,CASE TOPE."Accion" WHEN 'COMPRA' THEN CAR."INGMontoMonedaSecundaria"   ELSE 0 END AS "MontoCompra"
	,CASE TOPE."Accion" WHEN 'VENTA'  THEN CAR."INGMontoMonedaSecundaria"   ELSE 0 END AS "MontoVenta"
	,CASE TOPE."Accion" WHEN 'COMPRA' THEN CAR."ValorParidadCierre"  		ELSE 0 END AS "TasaCompra"
	,CASE TOPE."Accion" WHEN 'VENTA'  THEN CAR."ValorParidadCierre"  		ELSE 0 END AS "TasaVenta"
	
	,'CUS' as  "Custodia"
	,TOPE."IdTipoProducto"
	,CAR."IdClasificacionTipoInstrumento"
	,CAR."IdTipoInstrumento"
	,CAR."IdTipoOperacion"
	,CAR."IdInstrumento"
	,TOPE."Nombre"								AS "TipoOperacion"
	,CAR."IdTipoCanalIngreso"
    ,OPE."IdEstado" 
	,CAR."IdEstado" AS "IdEstadoCartera"
FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
INNER JOIN "BSCL.Mesa.BD::MES.Operacion" AS OPE ON (OPE."IdOperacion" = CAR."IdOperacion" AND OPE."IdTipoProducto" = CAR."IdTipoProducto")
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TOPE ON (CAR."IdTipoOperacion" = TOPE."IdTipoOperacion")
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CAR."IdCliente" = CLI."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON CLI."IdPersona" = PER."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad"

LEFT JOIN (
        SELECT DISTINCT "IdOperacion","FechaLiquidacionComprobantePagoOperacion","IdEmpresa","FechaIngresoComprobantePagoOperacion"
        FROM "BSCL.Mesa.BD::MES.ComprobantePagoOperacion"
        WHERE "IdTipoProducto" = 'MEFW' AND "RecibePaga" = 'P'
    ) AS FPE
    ON (FPE."IdOperacion" = CAR."IdCartera" AND FPE."IdEmpresa" = CAR."IdEmpresa" AND FPE."FechaIngresoComprobantePagoOperacion" BETWEEN ADD_DAYS(TO_DATE(OPE."FechaOperacion"),-2) AND ADD_DAYS(TO_DATE(OPE."FechaOperacion"),2))
    
LEFT JOIN (
        SELECT DISTINCT "IdOperacion","FechaLiquidacionComprobantePagoOperacion","IdEmpresa","FechaIngresoComprobantePagoOperacion"
        FROM "BSCL.Mesa.BD::MES.ComprobantePagoOperacion"
        WHERE "IdTipoProducto" = 'MEFW' AND "RecibePaga" = 'R'
    ) AS FPR
   ON (FPR."IdOperacion" = CAR."IdCartera" AND FPR."IdEmpresa" = CAR."IdEmpresa" AND FPR."FechaIngresoComprobantePagoOperacion" BETWEEN ADD_DAYS(TO_DATE(OPE."FechaOperacion"),-2) AND ADD_DAYS(TO_DATE(OPE."FechaOperacion"),2))
GO 
GRANT SELECT ON "BSCL.Mesa.BD.Vistas::FWD.ConsultarOperaciones" TO BCS_DESARROLLADOR WITH GRANT OPTION;
