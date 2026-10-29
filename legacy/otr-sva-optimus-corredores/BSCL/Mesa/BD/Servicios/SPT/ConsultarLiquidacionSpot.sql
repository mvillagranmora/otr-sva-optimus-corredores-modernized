SET SCHEMA "BSCL";  

DROP PROCEDURE "BSCL.Mesa.BD.Servicios::SPT.ConsultarLiquidacionSpot";

CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::SPT.ConsultarLiquidacionSpot" (
	 -- Tabla Entrada
	 IN pFechaOperacion NVARCHAR(10),
	 IN pIdTipoOperacion NVARCHAR(6),
	 IN pIdMonedaPrincipal NVARCHAR(5),
	 IN pIdMonedaSecundaria NVARCHAR(5),
	 IN pIdEstadoConfirmacion NVARCHAR(5),
	 IN pIdSocioNegocio BIGINT,
	 IN pIdEstadoCaja NVARCHAR(25),
	 
	 --Salida
	 OUT p_out_spot TABLE (
		 "FolioOperacion" BIGINT
		 , "FechaOperacion" SECONDDATE
		 , "FechaLiquidacion" SECONDDATE
		 , "FechaEntregamos" SECONDDATE
		 , "FechaRecibimos" SECONDDATE
		 , "IdCliente" BIGINT
		 , "IdInstrumento" BIGINT
		 , "NombreInstrumento" NVARCHAR(100)
		 , "Nemotecnico" NVARCHAR(1006)
		 , "IdTipoOperacion" NVARCHAR(6)
		 , "TipoOperacion" NVARCHAR(100)
		 , "RutCliente" NVARCHAR(12)
		 , "NombreCliente"  NVARCHAR(120)
		 , "CodigoPropositoPortafolio" NVARCHAR(25)
		 , "IdMonedaPrincipal" NVARCHAR(3)
		 , "IdMonedaSecundaria" NVARCHAR(3)
		 , "MontoMonedaPrincipal" DECIMAL(26,8)
		 , "MontoMonedaSecundaria" DECIMAL(26,8)
		 , "TipoCambioCierre" DECIMAL(26,8)
		 , "TipoCambioCosto" DECIMAL(26,8)
		 , "ParidadCierre" DECIMAL(26,8)
		 , "ParidadCosto" DECIMAL(26,8)
		 , "MontoEquivalente" DECIMAL(26,8)
		 , "CodigoUsuario" NVARCHAR(20)
		 , "Estado" NVARCHAR(3)
		 , "IdMesa" BIGINT
		 , "Mesa" NVARCHAR(5)
		 , "OrigenForward" NVARCHAR(1)
		 , "Producto" NVARCHAR(5)
		 , "CodigoComercio" BIGINT
		 , "IdUsuario" BIGINT
		 , "IdPortafolio" BIGINT
		 , "IdTipoInstrumento" NVARCHAR(5)
		 , "EstadoConfirmacion" NVARCHAR(3)
		 , "IdTipoCanalIngreso" NVARCHAR(5)
		 , "IdMovimientoComprobante" BIGINT
		 , "TipoDocumento" NVARCHAR(20)
		 , "IdOperacionSpot" BIGINT
		 , "IdAsignacion" BIGINT
		 , "OperacionNoFacturable" NVARCHAR(1)
		 , "IdEstadoCaja" NVARCHAR(3)
		 , "EstadoCaja" NVARCHAR(25)
		 )
	 )
	 LANGUAGE SQLSCRIPT
	 SQL SECURITY INVOKER
	 DEFAULT SCHEMA "BSCL"
	 READS SQL DATA
	 AS
	 
	 BEGIN
	 p_out_spot =
	 SELECT
		 --OS."IdOperacionSpot"  AS "FolioOperacion"
		 OS."IdOperacionSpotOrigen" AS "FolioOperacion"
		 ,TO_DATE(TO_VARCHAR(ADD_SECONDS(OS."FechaOperacion",TO_INTEGER(conf."Valor")),'yyyy-MM-dd'),'yyyy-MM-dd') AS "FechaOperacion"
		 ,OS."FechaLiquidacion"  AS "FechaLiquidacion"
		 ,FormaPagoEnt."FechaCondicionLiquidacionGrilla" AS "FechaEntregamos"
		 ,FormaPagoRec."FechaCondicionLiquidacionGrilla" AS "FechaRecibimos"
		 ,CLI."IdCliente"
		 ,TI."IdInstrumento"
		 ,TI."RazonSocial"  AS "NombreInstrumento"
		 ,TI."Nemotecnico"
		 ,CO."IdTipoOperacion"
		 ,CO."Nombre" AS "TipoOperacion"
		 ,CLI."DocumentoIdentidad"  AS "RutCliente"
		 ,CLI."NombreCompleto" AS "NombreCliente"
		 ,CP."CodigoPropositoPortafolio"
		 ,OS."IdMonedaPrincipal"
		 ,OS."IdMonedaSecundaria"
		 ,COALESCE(OS."MontoMonedaPrincipal",0) AS "MontoMonedaPrincipal"
		 ,COALESCE(OS."MontoMonedaSecundaria",0) AS "MontoMonedaSecundaria"
		 ,COALESCE(OS."TipoCambioCierre",0) AS "TipoCambioCierre"
		 ,COALESCE(OS."TipoCambioCosto",0) AS "TipoCambioCosto"
		 ,COALESCE(OS."ParidadCierre",0) AS "ParidadCierre"
		 ,COALESCE(OS."ParidadCosto",0) AS "ParidadCosto"
		 ,COALESCE(OS."MontoEquivalente",0) AS "MontoEquivalente"
		 ,US."CodigoUsuario"
		 ,OS."Estado"
		 ,OS."IdMesa" AS "IdMesa"
		 ,CO."IdTipoProducto" AS "Mesa"
		 ,CASE WHEN OS."IdTipoCanalIngreso" = 'FWD' THEN 'S' ELSE 'N' END AS "OrigenForward"
		 ,'SPOT' AS "Producto"
		 ,"CodigoComercio"
		 ,OS."IdUsuario"
		 ,OS."IdPortafolio"
		 ,TI."IdTipoInstrumento"
		 ,OS."EstadoConfirmacion"
		 ,OS."IdTipoCanalIngreso"
		 ,OS."IdComprobanteFacturacion" AS "IdMovimientoComprobante"
		 ,OS."TipoDocumento" 
		 ,OS."IdOperacionSpot"
		 ,AN."IdAsignacion"
		 ,OS."OperacionFicticia" AS "OperacionNoFacturable"
		 ,OS."EstadoCaja" AS "IdEstadoCaja"
		 ,COALESCE(EST."Nombre", '') AS "EstadoCaja"
	 FROM  "BSCL.Mesa.BD::SPT.OperacionSpot" AS OS
	 LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" EST ON EST."IdEstado" = OS."EstadoCaja"
	 --* -------------------------------
	 LEFT JOIN "BSCL.Orden.BD::ORD.Asignacion" AN 
	 ON (AN."IdOperacionSpot" = OS."IdOperacionSpot")	
	 --* -------------------------------	 
	 LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" US
	 ON OS."IdUsuario" = US."IdUsuario"
	 --* -------------------------------
	 LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" TI
	 ON OS."IdInstrumento" = TI."IdInstrumento"
	 --* -------------------------------
	 LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AG
	 ON OS."IdAgente" = AG."IdPersona"
	 --* -------------------------------
	 LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
	 ON OS."IdCliente" = CLI."IdCliente"
	 --* -------------------------------
	 LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.UIConsultarSocioDeNegocio" AS SOC
	 ON CLI."IdCliente" = SOC."IdCliente"
	 --* -------------------------------
	 LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarOperacion" CO
	 ON OS."IdTipoOperacion" = CO."IdTipoOperacion"
	 --* -------------------------------
	 LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" CP
	 ON CP."IdPortafolio" = OS."IdPortafolio"
	 --* -------------------------------
	 /*    LEFT JOIN ( SELECT "FolioOperacion", "FechaCondicionLiquidacionGrilla", MAX("FechaCondicionLiquidacion") as "FechaCondicionLiquidacion"
	 FROM "BSCL.Mesa.BD.Vistas::ConsultarFormaDePago"
	 WHERE "IdTipoMovimiento" = 'MOVEGR' AND "IdTipoProducto" = 'MEME'
	 GROUP BY "FolioOperacion", "FechaCondicionLiquidacionGrilla", "FechaCondicionLiquidacion"
	 ) as FormaPagoEnt
	 ON FormaPagoEnt."FolioOperacion" =  OS."IdOperacionSpot"
	 */
	 LEFT JOIN (SELECT cpe."IdOperacion", max(cpe."FechaLiquidacionComprobantePagoOperacion") as "FechaCondicionLiquidacion", max(cpe."FechaLiquidacionComprobantePagoOperacion") AS "FechaCondicionLiquidacionGrilla"
	 FROM "BSCL.Mesa.BD.Vistas::ConsultarComprobantePagoOperacion" cpe
	 WHERE cpe."RecibePaga" = 'P' AND cpe."IdTipoProducto" = 'MEME'
	 GROUP BY cpe."IdOperacion")
	 AS FormaPagoEnt
	 ON FormaPagoEnt."IdOperacion" =  OS."IdOperacionSpot"
	 --* -------------------------------
	 /*    LEFT JOIN ( SELECT "FolioOperacion", "FechaCondicionLiquidacionGrilla", MAX("FechaCondicionLiquidacion") as "FechaCondicionLiquidacion"
	 FROM "BSCL.Mesa.BD.Vistas::ConsultarFormaDePago"
	 WHERE "IdTipoMovimiento" = 'MOVING' AND "IdTipoProducto" = 'MEME'
	 GROUP BY "FolioOperacion", "FechaCondicionLiquidacionGrilla", "FechaCondicionLiquidacion"
	 ) as FormaPagoRec
	 ON FormaPagoRec."FolioOperacion" =  OS."IdOperacionSpot"
	 */
	 LEFT JOIN (SELECT cpr."IdOperacion", max(cpr."FechaLiquidacionComprobantePagoOperacion") as "FechaCondicionLiquidacion", max(cpr."FechaLiquidacionComprobantePagoOperacion") AS "FechaCondicionLiquidacionGrilla"
	 FROM "BSCL.Mesa.BD.Vistas::ConsultarComprobantePagoOperacion" cpr
	 WHERE cpr."RecibePaga" = 'R' AND cpr."IdTipoProducto" = 'MEME'
	 GROUP BY cpr."IdOperacion")
	 AS FormaPagoRec
	 ON FormaPagoRec."IdOperacion" =  OS."IdOperacionSpot"
	 --* -------------------------------
	 LEFT JOIN "BSCL.Mesa.BD::MES.PlanillaBancoCentral" PlanillaBancoCentral
	 ON OS."CodigoComercio" = PlanillaBancoCentral."IdPlanillaBancoCentral"
	 LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS conf
	 ON (conf."IdEmpresa" = OS."IdEmpresa" AND conf."CodigoParametro" = 'PLA.SegundosZonaHoraria')
	 WHERE TO_VARCHAR(TO_DATE(TO_VARCHAR(ADD_SECONDS(OS."FechaOperacion",TO_INTEGER(conf."Valor")),'yyyy-MM-dd'),'yyyy-MM-dd')) = TO_VARCHAR(pFechaOperacion, 'YYYY-MM-DD')
	 AND ( TI."Nemotecnico" = pIdTipoOperacion OR pIdTipoOperacion = '0' OR pIdTipoOperacion = '' )
	 AND ( OS."IdMonedaPrincipal" = pIdMonedaPrincipal OR pIdMonedaPrincipal = '0' OR pIdMonedaPrincipal = '' )
	 AND ( OS."IdMonedaSecundaria" = pIdMonedaSecundaria OR pIdMonedaSecundaria = '0' OR pIdMonedaSecundaria = '' )
	 AND ( OS."EstadoConfirmacion" = pIdEstadoConfirmacion OR pIdEstadoConfirmacion = '0' OR pIdEstadoConfirmacion = '' )
	 AND ( CLI."IdCliente" = pIdSocioNegocio OR pIdSocioNegocio = '0' )
	 AND ( pIdEstadoCaja IS NULL OR pIdEstadoCaja NOT IN ('CNF', 'NRE', 'PEN') OR OS."EstadoCaja" = pIdEstadoCaja )
	 AND OS."Estado" IN ('VIG','PEN');
	 
 END;
