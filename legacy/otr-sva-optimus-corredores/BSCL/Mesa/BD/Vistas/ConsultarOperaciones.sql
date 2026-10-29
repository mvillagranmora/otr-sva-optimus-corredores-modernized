
--* ------------------------------------------------------------------------
--* Nombre .....: ConsultarOperaciones
--* Modulo .....: Custodia
--* F. Creación : Agosto 2016
--* Proposito ..: Consultar las operaciones de la mesa RF
--* ------------------------------------------------------------------------

SET SCHEMA "BSCL";

--* ------------------------------------------------------------------------

DROP VIEW "BSCL.Mesa.BD.Vistas::ConsultarOperaciones";
--* ------------------------------------------------------------------------

CREATE VIEW "BSCL.Mesa.BD.Vistas::ConsultarOperaciones" 
AS
WITH "CalculoSpread" AS (
	SELECT SUM(CAR."EGRSpread") AS "SumaSpread", OPE."IdOperacion" FROM "BSCL.Mesa.BD::MES.Operacion" OPE
	INNER JOIN "BSCL.Custodia.BD::CUS.Reserva" AS RES ON OPE."IdOperacion"= RES."IdOperacion"
	INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR ON CAR."IdCartera"  = RES."IdCarteraOrigen"
	WHERE OPE."IdTipoOperacion" IN ('VENTER','VENPAC')
	GROUP BY OPE."IdOperacion"
	)
SELECT  OP."IdOperacion",
		OP."FolioOperacion",
		OP."FechaOperacion",
		OP."IdTipoProducto",
		OP."IdTipoOperacion",
		T_Ope."Nombre" AS "NombreOperacion",
		OP."IdOperacionRelacionada",
		OP."Intermesa",
		OP."IdMesaOrigen",
		mesOri."Nombre" AS "NombreMesaOrigen",
		mesOri."DocumentoIdentidad" AS "DocumentoIdentidadMesa",
		OP."IdMesaDestino",
		mesDes."Nombre" AS "NombreMesaDestino",
		OP."IdCliente",
		OP."IdPortafolio",
		PORT."CodigoPortafolio",
		OP."IdClienteCorredor",
		OP."IdPortafolioCorredor",
		CASE WHEN CLI."Tipo"='J' THEN CLI."RazonSocial" ELSE CLI."NombreCompleto" END AS "RazonSocial",
		OP."MontoOperacion",
		OP."MontoOperacionInicial",
		OP."MontoOperacionFinal",
		OP."IdTipoReajuste",
		OP."IdMoneda",
		OP."IdMonedaPrincipal",
		OP."IdMonedaSecundaria",
		OP."Tasa",
		OP."MontoOperacionMoneda",
		OP."MontoOperacionInicialMoneda",
		OP."MontoOperacionFinalMoneda",
		OP."IdMonedaLiquidacion",
		OP."PlazoEnDias",
		OP."FechaVencimiento",	
		CASE WHEN OP."IdTipoOperacion" IN ('VENTER', 'COMTER')
				then OP."FechaCondicionLiquidacion"
				else OP."FechaLiquidacion"
		END AS "FechaLiquidacion",
		CASE WHEN OP."IdTipoOperacion" IN ('VENTER', 'COMTER')
			THEN CASE WHEN OP."FechaCondicionLiquidacion" = '0001-01-01 00:00:00' THEN '' 
				ELSE TO_NVARCHAR(OP."FechaCondicionLiquidacion",'dd-MM-yyyy') END
		ELSE
			CASE
	 		   WHEN OP."FechaLiquidacion" = '0001-01-01 00:00:00' THEN '' 
			ELSE TO_NVARCHAR(OP."FechaLiquidacion",'dd-MM-yyyy')  END
		END AS "FechaLiquidacionString",			
		OP."IdCondicionLiquidacion",
		OP."FechaCondicionLiquidacion",
		OP."IdAgente",
		OP."TasaAgente",
		OP."IdEstado",
		Est_PLA."Nombre" AS "NombreEstado",
		OP."FechaCreacion",
		OP."FechaEdicion",
		OP."IdUsuarioCreacion",
		OP."IdUsuarioEdicion",
		USR."CodigoUsuario",
		OP."Glosa",
		CLI."IdEmpresa",
		CLI."DocumentoIdentidad",
		CLI."IdSucursalEjecutivo",
		CLI."IdPersonaEjecutivo",
		OP."IdTipoCanalIngreso",
		OP."AfectaCustodia"
		,OP."MontoOperacionAnticipo"
		,OP."MontoOperacionInicialAnticipo"
		,OP."MontoOperacionFinalAnticipo"
		,OP."TasaAnticipo"
		,OP."MontoOperacionMonedaAnticipo"
		,OP."MontoOperacionInicialMonedaAnticipo"
		,OP."MontoOperacionFinalMonedaAnticipo"
		,OP."IdMonedaLiquidacionAnticipo"
		,OP."IdCondicionLiquidacionAnticipo"
		,OP."FechaCondicionLiquidacionAnticipo"
		,OP."IdModalidadLiquidacion"
		,CASE WHEN TO_NVARCHAR(OP."FechaLiquidacion",'DD-MM-YYYY') = '01-01-0001' THEN OP."FechaVencimiento"
		ELSE OP."FechaLiquidacion" END AS "FechaAnticipo"
		,CASE WHEN TO_NVARCHAR(OP."FechaLiquidacion",'DD-MM-YYYY') <> '01-01-0001' AND OP."IdEstado" <> 'ANU'
		AND TO_NVARCHAR(OP."FechaLiquidacion",'DD-MM-YYYY') <> TO_NVARCHAR(OP."FechaVencimiento",'DD-MM-YYYY')
		THEN 'S'
		ELSE 'N' END AS "Anticipado"
		,CASE WHEN TO_NVARCHAR(OP."FechaLiquidacion",'DD-MM-YYYY') <> '01-01-0001' AND OP."IdEstado" <> 'ANU'
		AND TO_NVARCHAR(OP."FechaLiquidacion",'DD-MM-YYYY') = TO_NVARCHAR(OP."FechaVencimiento",'DD-MM-YYYY')
		THEN 'S'
		ELSE 'N' END AS "Liquidado"
		,TO_VARCHAR(OP."FechaVencimiento",'YYYY-MM-DD') AS "FechaVencimientoUI"
		,OP."TipoInformeGenerado"
		,COALESCE(PARAM."Valor",'N')  AS "ParametroConfirmacion"
		,OP."EstadoConfirmacion"
		,OP."TipoDocumento"
		,CASE   WHEN OP."IdTipoOperacion" = 'COMPAC' OR OP."IdTipoOperacion" = 'VENPAC' 
				THEN COALESCE(OP."ConfirmaAdemdum",'N') 
				ELSE ''
				END AS "ConfirmaAdemdum"
		
		,OP."IdUsuarioConfirma" AS "IdUsuarioConfirma"	-- para codigo usuario usar el campo CodigoUsuarioConfirma	
		,USRC."CodigoUsuario" AS "CodigoUsuarioConfirma"
		
		,OP."CostoFondo"
		,OP."UtilidadPerdida" AS "UtilidadPerdida"
		,OP."UtilidadFinal" AS "UtilidadFinal"
		,INS."IdTipoInstrumento"
		,OP."IdOperacionOrigenReasignacion"
		,OP."ValorizacionManual"
		,COALESCE(CALSPR."SumaSpread", 0.0) AS "Spread"
		,OP."BloqueoPorEdicion"
		,OP."CodigoUsuarioBloqueoEdicion"
		,OP."FechaHoraInicioEdicion"
	FROM "BSCL.Mesa.BD::MES.Operacion" OP 
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON OP."IdCliente" = CLI."IdCliente"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORT ON PORT."IdPortafolio" = OP."IdPortafolio"
		LEFT JOIN "BSCL.Mesa.BD::MES.ConfiguracionMesa" PARAM
	    ON CLI."IdEmpresa" = PARAM."IdEmpresa" AND PARAM."CodigoParametro" = 'CONFIRMA_OPERACIONES_MESARF'						
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" T_Ope
		ON OP."IdTipoProducto" = T_Ope."IdTipoProducto" AND OP."IdTipoOperacion" = T_Ope."IdTipoOperacion"
		LEFT JOIN "CalculoSpread" AS CALSPR ON OP."IdOperacion" = CALSPR."IdOperacion"
		LEFT JOIN "BSCL.Mesa.BD::MES.EstadoOperacion" Est_MRF
		ON  OP."IdTipoProducto" = Est_MRF."IdTipoProducto" AND OP."IdEstado" = Est_MRF."IdEstado" AND Est_MRF."NombreEntidad" = 'INGRESO'
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" Est_PLA
		ON  Est_MRF."IdEstado" = Est_PLA."IdEstado"
		LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarMesas" mesOri ON (OP."IdMesaOrigen" = mesOri."IdMesa")
		LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarMesas" mesDes ON (OP."IdMesaDestino" = mesDes."IdMesa")
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioCompleto" USR	ON (OP."IdUsuarioCreacion" = USR."IdUsuario")
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioCompleto" USRC ON (OP."IdUsuarioConfirma" = USRC."IdUsuario")
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON INS."IdInstrumento" = OP."IdInstrumento"
   WITH READ ONLY;	
		
--* ------------------------------------------------------------------------

GRANT 	SELECT ON "BSCL.Mesa.BD.Vistas::ConsultarOperaciones"  
		TO  BCS_DESARROLLADOR 
		WITH GRANT OPTION;
