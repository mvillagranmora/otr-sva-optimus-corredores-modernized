SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Custodia.BD.Utilitarios::GenerarOrdenEjecucionMovimientoCustodia";
GO
CREATE PROCEDURE "BSCL.Custodia.BD.Utilitarios::GenerarOrdenEjecucionMovimientoCustodia"(
-- 
--  Proposito: Devolver orden de ejecucion para el movimiento custodia, segun entrada enviada
--  EJ. INPUNT-->1,'MIOPER','MICONC','ƒ18T?,18!ƒ20T?®OPERAC©CONCEP,20->04T?®OPERAC©CONCEP[20->04T?®OPERAC©CONCEP]';
--      OUTPUT<--
--IdInterno;Orden;PasoEjecucion                                 ;OrdenEjecucion;TipoRegistro;SeValoriza;IdTipoOperacion;IdConcepto;OrigenDatos;Relacionado;TienePasoContingencia;PasoContingencia
--1        ;1    ;ƒ18T?                                         ;18            ;T           ;S         ;MIOPER         ;MICONC    ;0          ;0          ;N                    ;
--1        ;2    ;18!ƒ20T?®OPERAC©CONCEP                        ;20            ;T           ;S         ;OPERAC         ;CONCEP    ;0          ;18         ;N                    ;
--1        ;3    ;20->04T?®OPERAC©CONCEP[20->04T?®OPERAC©CONCEP];4             ;T           ;N         ;OPERAC         ;CONCEP    ;20         ;20         ;S                    ;20->04T?®OPERAC©CONCEP
--Notas: -->Cada paso de ejecucion es separado por coma ','
		 -->El orden de ejecucion, sera indicado por el campo 'Orden'
		 -->El simbolo ƒ, indica que sera valorizado el paso de ejecucion campo 'SeValoriza'
		 -->El simbolo ?, directamente al lado izquierdo se indica el tipo de registro (T=Titulo,O=Operacion)
		 -->El simbolo !, indica que el paso a ejecutarse tiene como paso relacionado el valor indicado antes de este simbolo. ejemplo 18 (segundo registro)
		 -->El simbolo ~, indica que los datos para grabar en la base de datos, provienen de un paso anterior, y no de los datos enviados al servicio. ejemplo 20 (tercer registro)
		 --                como registro relacionado, quedan los folios generados por el segundo registro.
		 -->El simbolto ë, indica que se aplicara el estado enviado al utilitario, si no, se devolvera null.
		 -->El simbolto ï, indica el origen de al informacion, DAT = Tabla de entrada, CAR = Cartera, SUB = Subyacente, GAR = Garantia.
		 -->El simbolo Æ, indica que se utiliza el nemo relacionado.
		 -->El simbolo >, indica que tiene paso de contingencia.
		 -->El simbolo <, indica que este es el paso de contingencia.
		 -->El simbolo Ø, indica que el servicio debe continuar aunque no tenga saldo, se creara un movimiento en transito pendiente
		 -->Los simbolos ×sal, indican que el paso no se ejecutara puesto que hubo un paso sin saldo, y se creara un movimiento en transito
		 -->Los simbolos _sal, indican que el paso se ejecutara puesto que hubo un paso sin saldo.
		 -->El simbolo ®, indica que se ocuparara el folio origen o relacionado para reitrar la cartera totalmente, para ocupar el folio original y no genear puchos
		 -->Los simbolos _rcar, indican que es la cartera origen que se intenta retirar, si no esta activa, porque no tiene saldo, debe segir el proceso y no hacer el retiro.
		 -->Los simbolos _add, indican que suman las cantidades de los registros, para ser ingresados en la operacion que se indique
		 -->Los simbolos _regAdd, indican que a este registro se le añade la suma de las custodias y mantendra el folio de cartera
		 -->los simbolos [], indican el tipo de paso que esta configurado para le proceso.
		 -- [IDE] IDEAL,[NOM] NOMINAL, [GTA] HACE UN INGRESO O RETIRO EN LA TABLA DE GARANTIA EN FIRNA AUTOMATICA
		 -->Si en el registro no hay tipo de operacion o concepto, se utilizaran los de la operacion, que son enviados como parametros de entrada. ejemplo mioper, miconc (primer registro).
		 -->Un registro no puede ser indicado como origen de datos y relacionado, es solo uno a la vez, si es indicado como que sus datos vienen de un origen, internamente se graba el campo relacionado.
--  Autor: Oscar Carreno Tolmo - OCT.
--  Fecha Creacion: 23/10/2016
--MODIFICACIONES
--  Autor: Oscar Carreno Tolmo - OCT 
--  Fecha Creacion: 13/12/2016
--  Proposito: Se guaradaran las configuraciones en una tabla fisica, para no hacerlo cada vez que se llama al servicio de generarmovimientocustodia
	p_IdEmpresa			BIGINT		DEFAULT NULL,
	p_IdTipoOperacion	NVARCHAR(6) DEFAULT NULL,
	p_IdUsuario			BIGINT,
	p_EsDemo			NVARCHAR(1) DEFAULT 'N'
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA BSCL
AS
BEGIN
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		INSERT INTO "BSCL.Plataforma.BD::PLA.Depuracion_Entrada_Depuracion" (
			"IdEmpresa","Fecha","IdUsuario","IdServicioAgrupador","ServicioAgrupador","Servicio","Bandera","Detalle") 
		SELECT
			0,CURRENT_UTCTIMESTAMP,:p_IdUsuario,1,::CURRENT_OBJECT_NAME,::CURRENT_OBJECT_NAME,
			'Fin',('Error: ' || ::SQL_ERROR_CODE ||', ' || ::SQL_ERROR_MESSAGE)
		FROM DUMMY;
		
		CALL "BSCL.Plataforma.BD.Servicios::DepuracionMM"();
		RESIGNAL;
	END;
	
  	BEGIN
  		DECLARE v_IdServicioAgrupador		  BIGINT :=0;
  		--CLIENTE INGRESO
  		DECLARE v_CLIING_ING_TRA,v_CLIING_ING_CAR,v_CLIING_ING_SUB,v_CLIING_ING_GAR INT :=0;
  		DECLARE v_CLIING_EGR_TRA,v_CLIING_EGR_CAR,v_CLIING_EGR_SUB,v_CLIING_EGR_GAR INT :=0;
  		DECLARE v_CLIING_ING_AUTO_TRA,v_CLIING_ING_AUTO_CAR,v_CLIING_ING_AUTO_SUB,v_CLIING_ING_AUTO_GAR INT :=0;
  		DECLARE v_CLIING_EGR_AUTO_TRA,v_CLIING_EGR_AUTO_CAR,v_CLIING_EGR_AUTO_SUB,v_CLIING_EGR_AUTO_GAR INT :=0;
		--CLIENTE EGRESO
		DECLARE v_CLIEGR_ING_TRA,v_CLIEGR_ING_CAR,v_CLIEGR_ING_SUB,v_CLIEGR_ING_GAR INT :=0;
  		DECLARE v_CLIEGR_EGR_TRA,v_CLIEGR_EGR_CAR,v_CLIEGR_EGR_SUB,v_CLIEGR_EGR_GAR INT :=0;
  		DECLARE v_CLIEGR_ING_AUTO_TRA,v_CLIEGR_ING_AUTO_CAR,v_CLIEGR_ING_AUTO_SUB,v_CLIEGR_ING_AUTO_GAR INT :=0;
  		DECLARE v_CLIEGR_EGR_AUTO_TRA,v_CLIEGR_EGR_AUTO_CAR,v_CLIEGR_EGR_AUTO_SUB,v_CLIEGR_EGR_AUTO_GAR INT :=0;
  		
		DECLARE v_Configuraciones			  TABLE ("IdInterno" BIGINT,"Texto" NVARCHAR(1000))
			DEFAULT SELECT 0 AS "IdInterno",'' AS "Texto" FROM DUMMY WHERE 1=2;
        DECLARE v_out_ItemsConfiguraciones	  TABLE ("IdInterno" BIGINT,"Orden" INTEGER,"Item" NVARCHAR(1000))
        	DEFAULT SELECT 0 AS "IdInterno",0 AS "Orden",'' AS "Item" FROM DUMMY WHERE 1=2;
        DECLARE v_out_ItemsConfiguracionesCorregido	  TABLE ("IdInterno" BIGINT,"Orden" INTEGER,"IdInternoCorregido" BIGINT,"Item" NVARCHAR(1000))
        	DEFAULT SELECT 0 AS "IdInterno",0 AS "Orden",0 AS "IdInternoCorregido",'' AS "Item" FROM DUMMY WHERE 1=2;	
        DECLARE v_OrdenEjecucionMovimientos	  TABLE ("IdInterno" BIGINT,"Texto" NVARCHAR(1000))
        	DEFAULT SELECT 0 AS "IdInterno",'' AS "Texto" FROM DUMMY WHERE 1=2;
        DECLARE v_out_Items					  TABLE ("IdInterno" BIGINT,"Orden" INTEGER,"Item" NVARCHAR(1000))
        	DEFAULT SELECT 0 AS "IdInterno",0 AS "Orden",'' AS "Item" FROM DUMMY WHERE 1=2;
        
        DECLARE v_PasosPorOperacion TABLE (
        	"IdConfiguracionMovimientoCustodia" BIGINT,"IdTipoPaso" NVARCHAR(6),"IdTipoOperacion" NVARCHAR(6),"IdOrdenPaso" BIGINT,"IdEmpresa" BIGINT,
			------------------------------------------------------------------------------------------------
			"PasoEjecucion" NVARCHAR(1000),"IdPasoEjecucion" INT,"TablaOrigenDatosDelPaso" NVARCHAR(3),"OrigenDatosDelPaso" INT,"PasoRelacionadoCon" INT,
			"IdEstadoPaso" NVARCHAR(3),
			---------------------------------------------------------
			"PasoMantieneFolioCustodia" NVARCHAR(1),"PasoUtilizaInstrumentoRelacionado" NVARCHAR(1),"TipoRegistroDelPaso" NVARCHAR(1),"SeValorizaPaso" NVARCHAR(1),
			"PasoTieneConfiguradaContingencia" NVARCHAR(1),"EsPasoContingencia" NVARCHAR(1),"EjecutarPasoHabiendoContingencia" NVARCHAR(1),
			"ContinuarEjecucionPasosNoTeniendoSaldo" NVARCHAR(1),"EjecutarPasoNoTeniendoSaldo" NVARCHAR(1),"PasoRetiraCarteraOperacionOrigen" NVARCHAR(1),
			"AgregarSumaDeCustodiaAlPaso" NVARCHAR(1),"IdTipoOperacionPaso" NVARCHAR(6),"IdConceptoPaso" NVARCHAR(6),"RetornaCarteraAlPrecioTasaPaso" NVARCHAR(4),
			"PasoEsCustodiaRetornada" NVARCHAR(1),"PasoMueveCustodia" NVARCHAR(1),"PasoPermiteCertificacion" NVARCHAR(1),"EjecutarPasoSiendoNominal" NVARCHAR(1),
			"CodigoMensajePaso" NVARCHAR(100),"PasoValidaCustodiaOrigenTransito" NVARCHAR(1))
		DEFAULT SELECT
					0 AS "IdConfiguracionMovimientoCustodia",'' AS "IdTipoPaso",'' AS "IdTipoOperacion",0 AS "IdOrdenPaso",0 AS "IdEmpresa",
					------------------------------------------------------------------------------------------------
					'' AS "PasoEjecucion",0 AS "IdPasoEjecucion",'' AS "TablaOrigenDatosDelPaso",0 AS "OrigenDatosDelPaso",0 AS "PasoRelacionadoCon",
					'' AS "IdEstadoPaso",
					---------------------------------------------------------
					'' AS "PasoMantieneFolioCustodia",'' AS "PasoUtilizaInstrumentoRelacionado",'' AS "TipoRegistroDelPaso",'' AS "SeValorizaPaso",
					'' AS "PasoTieneConfiguradaContingencia",'' AS "EsPasoContingencia",'' AS "EjecutarPasoHabiendoContingencia",
					'' AS "ContinuarEjecucionPasosNoTeniendoSaldo",'' AS "EjecutarPasoNoTeniendoSaldo",'' AS "PasoRetiraCarteraOperacionOrigen",
					'' AS "AgregarSumaDeCustodiaAlPaso",'' AS "IdTipoOperacionPaso",'' AS "IdConceptoPaso",'' AS "RetornaCarteraAlPrecioTasaPaso",
					'' AS "PasoEsCustodiaRetornada",'' AS "PasoMueveCustodia",'' AS "PasoPermiteCertificacion",'' AS "EjecutarPasoSiendoNominal",
					'' AS "CodigoMensajePaso",'' AS "PasoValidaCustodiaOrigenTransito"
				FROM DUMMY WHERE 1=2;
	  
       DECLARE v_ConfiguracionMovimientoCustodia "BSCL.Custodia.BD::TYP.ConfiguracionMovimientoCustodia"
			DEFAULT SELECT
						ROW_NUMBER() over (ORDER BY "IdTipoOperacion") AS "IdConfiguracionMovimientoCustodia","IdTipoOperacion","IdEmpresa","IdAccionOperacion",
						"UtilizaPlantillaMovimientoOriginal","InvierteClientesPlantillaMovimientoOriginal","BuscaMovimientosGeneradosPorCustodia",
						"PoseeInstrumentoSubyacente","EsGarantia","PermiteGarantiaAutomatica","GeneraCarteraNueva","RetornaCarteraAlPrecioTasa",
						"GrabaClienteRelacionado","EsClienteIngresoClienteRelacionado","PoseeInstrumentoRelacionado","UtilizaFechaLiquidacionBusqueda",
						"PermiteAnulacion","PermiteAnularOperacionesCertificadas","InstrumentoPorIdTipoOperacion",
						--conceptos para venta corta
						"IdConceptoVentaCorta","IdTipoOperacionVentaCorta","IdTipoPrestamoVentaCorta","IdEstadoOperacion","PoseeRetiroDeTitulosAutomatico",
						"RetiroAutomaticoEsInstrumentoRelacionado",
						-----------------------------------------------------------------------------------------
						"OrdenEjecucionMovimientos",
						--------------------------------Cliente Ingreso-----------------------------------------
						"CliIngGeneraMvtoEnTransitoSinCustodia","CliIngMueveCarteraEnTransitoING","CliIngMueveCarteraING","CliIngMueveSubyacenteING",
						"CliIngMueveGarantiaING","CliIngMueveCarteraEnTransitoEGR","CliIngMueveCarteraEGR","CliIngMueveSubyacenteEGR","CliIngMueveGarantiaEGR",
						-----------------------Cliente Ingreso Carteras Automaticas------------------------------
						"CliIngMueveCarteraEnTransitoAutomaticaING","CliIngMueveCarteraAutomaticaING","CliIngMueveSubyacenteAutomaticaING",
						"CliIngMueveGarantiaAutomaticaING","CliIngMueveCarteraEnTransitoAutomaticaEGR","CliIngMueveCarteraAutomaticaEGR",
						"CliIngMueveSubyacenteAutomaticaEGR","CliIngMueveGarantiaAutomaticaEGR",
						----------------------------------------------------------------------------------------
						--Si la condicion de liquidacion esta en blanco ocupa la de la operacion
						"CliIngIdCondicionLiquidacionTransitoAutoING","CliIngIdCondicionLiquidacionCarteraAutoING","CliIngIdCondicionLiquidacionSubyacenteAutoING",
						"CliIngIdCondicionLiquidacionGarantiaAutoING","CliIngIdCondicionLiquidacionTransitoAutoEGR","CliIngIdCondicionLiquidacionCarteraAutoEGR",
						"CliIngIdCondicionLiquidacionSubyacenteAutoEGR","CliIngIdCondicionLiquidacionGarantiaAutoEGR",
						----------------------------------------------------------------------------------------
						--------------------------------Cliente Retiro------------------------------------------
						"CliEgrGeneraMvtoEnTransitoSinCustodia","CliEgrMueveCarteraEnTransitoING","CliEgrMueveCarteraING","CliEgrMueveSubyacenteING",
						"CliEgrMueveGarantiaING","CliEgrMueveCarteraEnTransitoEGR","CliEgrMueveCarteraEGR","CliEgrMueveSubyacenteEGR","CliEgrMueveGarantiaEGR",
						-----------------------Cliente Retiro Carteras Automaticas------------------------------
						"CliEgrMueveCarteraEnTransitoAutomaticaING","CliEgrMueveCarteraAutomaticaING","CliEgrMueveSubyacenteAutomaticaING",
						"CliEgrMueveGarantiaAutomaticaING","CliEgrMueveCarteraEnTransitoAutomaticaEGR","CliEgrMueveCarteraAutomaticaEGR",
						"CliEgrMueveSubyacenteAutomaticaEGR","CliEgrMueveGarantiaAutomaticaEGR",
						----------------------------------------------------------------------------------------
						--Si la condicion de liquidacion esta en blanco ocupa la de la operacion
						"CliEgrIdCondicionLiquidacionTransitoAutoING","CliEgrIdCondicionLiquidacionCarteraAutoING","CliEgrIdCondicionLiquidacionSubyacenteAutoING",
						"CliEgrIdCondicionLiquidacionGarantiaAutoING","CliEgrIdCondicionLiquidacionTransitoAutoEGR","CliEgrIdCondicionLiquidacionCarteraAutoEGR",
						"CliEgrIdCondicionLiquidacionSubyacenteAutoEGR","CliEgrIdCondicionLiquidacionGarantiaAutoEGR",
						-----------------------------------------------------------------------------------------
						"IdEstado","IdUsuarioModificacionRegistro","FechaModificacionRegistro"
					FROM "BSCL.Custodia.BD::CUS.ConfiguracionMovimientoCustodia"
					WHERE ((:p_IdEmpresa IS NULL OR "IdEmpresa" = :p_IdEmpresa)
						AND (:p_IdTipoOperacion IS NULL OR "IdTipoOperacion" = :p_IdTipoOperacion));
		
		SELECT COALESCE(MAX("IdServicioAgrupador"),0) + 1 INTO v_IdServicioAgrupador FROM "BSCL.Plataforma.BD::PLA.Depuracion" WHERE "ServicioAgrupador" = ::CURRENT_OBJECT_NAME;
		
		INSERT INTO "BSCL.Plataforma.BD::PLA.Depuracion_Entrada_Depuracion" (
			"IdEmpresa","Fecha","IdUsuario","IdServicioAgrupador","ServicioAgrupador","Servicio","Bandera","Detalle") 
		SELECT
			COALESCE(:p_IdEmpresa,0),CURRENT_UTCTIMESTAMP,:p_IdUsuario,:v_IdServicioAgrupador,::CURRENT_OBJECT_NAME,::CURRENT_OBJECT_NAME,
			'Inicio','Inicio Utilitario'
		FROM DUMMY;
		
		SELECT
			--CLIENTE INGRESO
	  		MAX("CLIING_ING_TRA"),MAX("CLIING_ING_CAR"),MAX("CLIING_ING_SUB"),MAX("CLIING_ING_GAR"),
	  		MAX("CLIING_EGR_TRA"),MAX("CLIING_EGR_CAR"),MAX("CLIING_EGR_SUB"),MAX("CLIING_EGR_GAR"),
	  		MAX("CLIING_ING_AUTO_TRA"),MAX("CLIING_ING_AUTO_CAR"),MAX("CLIING_ING_AUTO_SUB"),MAX("CLIING_ING_AUTO_GAR"),
	  		MAX("CLIING_EGR_AUTO_TRA"),MAX("CLIING_EGR_AUTO_CAR"),MAX("CLIING_EGR_AUTO_SUB"),MAX("CLIING_EGR_AUTO_GAR"),
			--CLIENTE EGRESO
			MAX("CLIEGR_ING_TRA"),MAX("CLIEGR_ING_CAR"),MAX("CLIEGR_ING_SUB"),MAX("CLIEGR_ING_GAR"),
	  		MAX("CLIEGR_EGR_TRA"),MAX("CLIEGR_EGR_CAR"),MAX("CLIEGR_EGR_SUB"),MAX("CLIEGR_EGR_GAR"),
	  		MAX("CLIEGR_ING_AUTO_TRA"),MAX("CLIEGR_ING_AUTO_CAR"),MAX("CLIEGR_ING_AUTO_SUB"),MAX("CLIEGR_ING_AUTO_GAR"),
	  		MAX("CLIEGR_EGR_AUTO_TRA"),MAX("CLIEGR_EGR_AUTO_CAR"),MAX("CLIEGR_EGR_AUTO_SUB"),MAX("CLIEGR_EGR_AUTO_GAR")
	  		INTO
	  		--CLIENTE INGRESO
	  		v_CLIING_ING_TRA,v_CLIING_ING_CAR,v_CLIING_ING_SUB,v_CLIING_ING_GAR,
	  		v_CLIING_EGR_TRA,v_CLIING_EGR_CAR,v_CLIING_EGR_SUB,v_CLIING_EGR_GAR,
	  		v_CLIING_ING_AUTO_TRA,v_CLIING_ING_AUTO_CAR,v_CLIING_ING_AUTO_SUB,v_CLIING_ING_AUTO_GAR,
	  		v_CLIING_EGR_AUTO_TRA,v_CLIING_EGR_AUTO_CAR,v_CLIING_EGR_AUTO_SUB,v_CLIING_EGR_AUTO_GAR,
			--CLIENTE EGRESO
			v_CLIEGR_ING_TRA,v_CLIEGR_ING_CAR,v_CLIEGR_ING_SUB,v_CLIEGR_ING_GAR,
	  		v_CLIEGR_EGR_TRA,v_CLIEGR_EGR_CAR,v_CLIEGR_EGR_SUB,v_CLIEGR_EGR_GAR,
	  		v_CLIEGR_ING_AUTO_TRA,v_CLIEGR_ING_AUTO_CAR,v_CLIEGR_ING_AUTO_SUB,v_CLIEGR_ING_AUTO_GAR,
	  		v_CLIEGR_EGR_AUTO_TRA,v_CLIEGR_EGR_AUTO_CAR,v_CLIEGR_EGR_AUTO_SUB,v_CLIEGR_EGR_AUTO_GAR		
		FROM
		(
			SELECT
				--CLIENTE INGRESO
				CASE WHEN "COLUMN_NAME" = 'CliIngMueveCarteraEnTransitoING' THEN "POSITION" ELSE NULL END AS "CLIING_ING_TRA",
				CASE WHEN "COLUMN_NAME" = 'CliIngMueveCarteraING' THEN "POSITION" ELSE NULL END AS "CLIING_ING_CAR",
				CASE WHEN "COLUMN_NAME" = 'CliIngMueveSubyacenteING' THEN "POSITION" ELSE NULL END AS "CLIING_ING_SUB",
				CASE WHEN "COLUMN_NAME" = 'CliIngMueveGarantiaING' THEN "POSITION" ELSE NULL END AS "CLIING_ING_GAR",
				CASE WHEN "COLUMN_NAME" = 'CliIngMueveCarteraEnTransitoEGR' THEN "POSITION" ELSE NULL END AS "CLIING_EGR_TRA",
				CASE WHEN "COLUMN_NAME" = 'CliIngMueveCarteraEGR' THEN "POSITION" ELSE NULL END AS "CLIING_EGR_CAR",
				CASE WHEN "COLUMN_NAME" = 'CliIngMueveSubyacenteEGR' THEN "POSITION" ELSE NULL END AS "CLIING_EGR_SUB",
				CASE WHEN "COLUMN_NAME" = 'CliIngMueveGarantiaEGR' THEN "POSITION" ELSE NULL END AS "CLIING_EGR_GAR",
				CASE WHEN "COLUMN_NAME" = 'CliIngMueveCarteraEnTransitoAutomaticaING' THEN "POSITION" ELSE NULL END AS "CLIING_ING_AUTO_TRA",
				CASE WHEN "COLUMN_NAME" = 'CliIngMueveCarteraAutomaticaING' THEN "POSITION" ELSE NULL END AS "CLIING_ING_AUTO_CAR",
				CASE WHEN "COLUMN_NAME" = 'CliIngMueveSubyacenteAutomaticaING' THEN "POSITION" ELSE NULL END AS "CLIING_ING_AUTO_SUB",
				CASE WHEN "COLUMN_NAME" = 'CliIngMueveGarantiaAutomaticaING' THEN "POSITION" ELSE NULL END AS "CLIING_ING_AUTO_GAR",
				CASE WHEN "COLUMN_NAME" = 'CliIngMueveCarteraEnTransitoAutomaticaEGR' THEN "POSITION" ELSE NULL END AS "CLIING_EGR_AUTO_TRA",
				CASE WHEN "COLUMN_NAME" = 'CliIngMueveCarteraAutomaticaEGR' THEN "POSITION" ELSE NULL END AS "CLIING_EGR_AUTO_CAR",
				CASE WHEN "COLUMN_NAME" = 'CliIngMueveSubyacenteAutomaticaEGR' THEN "POSITION" ELSE NULL END AS "CLIING_EGR_AUTO_SUB",
				CASE WHEN "COLUMN_NAME" = 'CliIngMueveGarantiaAutomaticaEGR' THEN "POSITION" ELSE NULL END AS "CLIING_EGR_AUTO_GAR",
				--CLIENTE EGRESO
				CASE WHEN "COLUMN_NAME" = 'CliEgrMueveCarteraEnTransitoING' THEN "POSITION" ELSE NULL END AS "CLIEGR_ING_TRA",
				CASE WHEN "COLUMN_NAME" = 'CliEgrMueveCarteraING' THEN "POSITION" ELSE NULL END AS "CLIEGR_ING_CAR",
				CASE WHEN "COLUMN_NAME" = 'CliEgrMueveSubyacenteING' THEN "POSITION" ELSE NULL END AS "CLIEGR_ING_SUB",
				CASE WHEN "COLUMN_NAME" = 'CliEgrMueveGarantiaING' THEN "POSITION" ELSE NULL END AS "CLIEGR_ING_GAR",
				CASE WHEN "COLUMN_NAME" = 'CliEgrMueveCarteraEnTransitoEGR' THEN "POSITION" ELSE NULL END AS "CLIEGR_EGR_TRA",
				CASE WHEN "COLUMN_NAME" = 'CliEgrMueveCarteraEGR' THEN "POSITION" ELSE NULL END AS "CLIEGR_EGR_CAR",
				CASE WHEN "COLUMN_NAME" = 'CliEgrMueveSubyacenteEGR' THEN "POSITION" ELSE NULL END AS "CLIEGR_EGR_SUB",
				CASE WHEN "COLUMN_NAME" = 'CliEgrMueveGarantiaEGR' THEN "POSITION" ELSE NULL END AS "CLIEGR_EGR_GAR",
				CASE WHEN "COLUMN_NAME" = 'CliEgrMueveCarteraEnTransitoAutomaticaING' THEN "POSITION" ELSE NULL END AS "CLIEGR_ING_AUTO_TRA",
				CASE WHEN "COLUMN_NAME" = 'CliEgrMueveCarteraAutomaticaING' THEN "POSITION" ELSE NULL END AS "CLIEGR_ING_AUTO_CAR",
				CASE WHEN "COLUMN_NAME" = 'CliEgrMueveSubyacenteAutomaticaING' THEN "POSITION" ELSE NULL END AS "CLIEGR_ING_AUTO_SUB",
				CASE WHEN "COLUMN_NAME" = 'CliEgrMueveGarantiaAutomaticaING' THEN "POSITION" ELSE NULL END AS "CLIEGR_ING_AUTO_GAR",
				CASE WHEN "COLUMN_NAME" = 'CliEgrMueveCarteraEnTransitoAutomaticaEGR' THEN "POSITION" ELSE NULL END AS "CLIEGR_EGR_AUTO_TRA",
				CASE WHEN "COLUMN_NAME" = 'CliEgrMueveCarteraAutomaticaEGR' THEN "POSITION" ELSE NULL END AS "CLIEGR_EGR_AUTO_CAR",
				CASE WHEN "COLUMN_NAME" = 'CliEgrMueveSubyacenteAutomaticaEGR' THEN "POSITION" ELSE NULL END AS "CLIEGR_EGR_AUTO_SUB",
				CASE WHEN "COLUMN_NAME" = 'CliEgrMueveGarantiaAutomaticaEGR' THEN "POSITION" ELSE NULL END AS "CLIEGR_EGR_AUTO_GAR"
			FROM "SYS"."TABLE_COLUMNS" AS t
			WHERE t."SCHEMA_NAME" = 'BSCL'
			  AND t."TABLE_NAME" = 'BSCL.Custodia.BD::CUS.ConfiguracionMovimientoCustodia'
		);
		
		--EL TEXTO CON EL ORDEN DE EJECUCION SE ENVIA AL SPLIT
		v_Configuraciones =
			SELECT "IdConfiguracionMovimientoCustodia" AS "IdInterno","OrdenEjecucionMovimientos" AS "Texto"
			FROM :v_ConfiguracionMovimientoCustodia;
		
		CALL "BSCL.Plataforma.BD.Utilitarios::SplitLlaveValor"(
			p_Texto		=>	:v_Configuraciones,
			p_Split		=>	';',
			p_out_Items	=>	v_out_ItemsConfiguraciones);
		
		INSERT INTO "BSCL.Plataforma.BD::PLA.Depuracion_Entrada_Depuracion" (
			"IdEmpresa","Fecha","IdUsuario","IdServicioAgrupador","ServicioAgrupador","Servicio","Bandera","Detalle") 
		SELECT
			COALESCE(:p_IdEmpresa,0),CURRENT_UTCTIMESTAMP,:p_IdUsuario,:v_IdServicioAgrupador,::CURRENT_OBJECT_NAME,::CURRENT_OBJECT_NAME,
			'Split','Fin llamada utilitario BSCL.Plataforma.BD.Utilitarios::SplitLlaveValor'
		FROM DUMMY;
		
		v_out_ItemsConfiguracionesCorregido =
			SELECT "IdInterno","Orden",ROW_NUMBER() over (ORDER BY "IdInterno") AS "IdInternoCorregido","Item" FROM :v_out_ItemsConfiguraciones;
		
		v_OrdenEjecucionMovimientos =
			SELECT "IdInternoCorregido" AS "IdInterno","Item" AS "Texto" FROM :v_out_ItemsConfiguracionesCorregido;
			
		CALL "BSCL.Plataforma.BD.Utilitarios::SplitLlaveValor"(
			p_Texto		=>	:v_OrdenEjecucionMovimientos,
			p_Split		=>	',',
			p_out_Items	=>	v_out_Items);
		
		INSERT INTO "BSCL.Plataforma.BD::PLA.Depuracion_Entrada_Depuracion" (
			"IdEmpresa","Fecha","IdUsuario","IdServicioAgrupador","ServicioAgrupador","Servicio","Bandera","Detalle") 
		SELECT
			COALESCE(:p_IdEmpresa,0),CURRENT_UTCTIMESTAMP,:p_IdUsuario,:v_IdServicioAgrupador,::CURRENT_OBJECT_NAME,::CURRENT_OBJECT_NAME,
			'Split','Fin llamada utilitario BSCL.Plataforma.BD.Utilitarios::SplitLlaveValor'
		FROM DUMMY;
		
		v_PasosPorOperacion =
		SELECT
			z."IdConfiguracionMovimientoCustodia",SUBSTRING(z."IdTipoPaso",2,6) AS "IdTipoPaso",z."IdTipoOperacion",z."IdOrdenPaso",z."IdEmpresa",
			------------------------------------------------------------------------------------------------
			CAST(z."PasoEjecucion" AS NVARCHAR(1000)) AS "PasoEjecucion",CAST(SUBSTR_REGEXPR( '([[:digit:]]{2})' IN z."OrdenEjecucionFull") AS INT) AS "IdPasoEjecucion",
			COALESCE(z."OrigenInformacion",'DAT') AS "TablaOrigenDatosDelPaso",
			CAST(COALESCE(SUBSTR_REGEXPR( '([[:digit:]]{2})' IN z."OrigenDatosFull"),0) AS INT) AS "OrigenDatosDelPaso",
			--CASE WHEN "OrigenDatosFull" != '' THEN CAST(COALESCE(z."OrigenDatosFull",0) AS INT)
			--	 ELSE CAST(COALESCE(z."PasoRelacionadoCon",0) AS INT)
			--END AS "PasoRelacionadoCon",
			CAST(COALESCE(z."PasoRelacionadoCon",0) AS INT) AS "PasoRelacionadoCon",
			CASE WHEN OCCURRENCES_REGEXPR('ë' IN z."OrdenEjecucionFull") > 0 THEN COALESCE(z."IdEstadoPaso",z."IdEstadoOperacion") ELSE COALESCE(z."IdEstadoPaso",NULL) END AS "IdEstadoPaso",
			---------------------------------------------------------
			CASE WHEN OCCURRENCES_REGEXPR('Ý' IN z."OrdenEjecucionFull") > 0 THEN 'S' ELSE 'N' END AS "PasoMantieneFolioCustodia",
			CASE WHEN OCCURRENCES_REGEXPR('Æ' IN z."OrdenEjecucionFull") > 0 THEN 'S' ELSE 'N' END AS "PasoUtilizaInstrumentoRelacionado",
			CAST(SUBSTRING(z."PasoEjecucion",z."PosTipoRegistro"-1,1) AS NVARCHAR(1)) AS "TipoRegistroDelPaso",
			CASE WHEN OCCURRENCES_REGEXPR('ƒ' IN z."OrdenEjecucionFull") > 0 THEN 'S' ELSE 'N' END AS "SeValorizaPaso",
			CASE WHEN OCCURRENCES_REGEXPR('>' IN z."OrdenEjecucionFull") > 0 THEN 'S' ELSE 'N' END AS "PasoTieneConfiguradaContingencia",
			CASE WHEN OCCURRENCES_REGEXPR('<' IN z."OrdenEjecucionFull") > 0 THEN 'S' ELSE 'N' END AS "EsPasoContingencia",
			CASE WHEN OCCURRENCES_REGEXPR('×con' IN z."OrdenEjecucionFull") > 0 THEN 'N'
				 WHEN OCCURRENCES_REGEXPR('_con' IN z."OrdenEjecucionFull") > 0 THEN 'S'
				 ELSE NULL END AS "EjecutarPasoHabiendoContingencia",	
			CASE WHEN OCCURRENCES_REGEXPR('Ø' IN z."OrdenEjecucionFull") > 0 THEN 'S' ELSE 'N' END AS "ContinuarEjecucionPasosNoTeniendoSaldo",
			CASE WHEN OCCURRENCES_REGEXPR('×sal' IN z."OrdenEjecucionFull") > 0 THEN 'N'
				 WHEN OCCURRENCES_REGEXPR('_sal' IN z."OrdenEjecucionFull") > 0 THEN 'S'
				 ELSE NULL END AS "EjecutarPasoNoTeniendoSaldo",
			CASE WHEN OCCURRENCES_REGEXPR('®' IN z."OrdenEjecucionFull") > 0 THEN 'S' ELSE 'N' END AS "PasoRetiraCarteraOperacionOrigen",
			CASE WHEN OCCURRENCES_REGEXPR('_regAdd' IN z."OrdenEjecucionFull") > 0 THEN 'S' ELSE 'N' END AS "AgregarSumaDeCustodiaAlPaso",
			z."IdTipoOperacionPaso",z."IdConceptoPaso",z."IdTipoTasaRetorno" AS "RetornaCarteraAlPrecioTasaPaso",
			CASE WHEN OCCURRENCES_REGEXPR('ç' IN z."OrdenEjecucionFull") > 0 THEN 'S' ELSE 'N' END AS "PasoEsCustodiaRetornada",
			CASE WHEN OCCURRENCES_REGEXPR('¢' IN z."OrdenEjecucionFull") > 0 THEN 'N' ELSE 'S' END AS "PasoMueveCustodia",
			CASE WHEN OCCURRENCES_REGEXPR('¥' IN z."OrdenEjecucionFull") > 0 THEN 'N' ELSE 'S' END AS "PasoPermiteCertificacion",
			CASE WHEN SUBSTRING(z."IdTipoPaso",2,6) = 'NOMING' AND OCCURRENCES_REGEXPR('x_regNom' IN z."OrdenEjecucionFull") > 0 THEN 'N'
				 WHEN SUBSTRING(z."IdTipoPaso",2,6) = 'NOMING' THEN 'S'
				 WHEN SUBSTRING(z."IdTipoPaso",2,6) = 'NOMRET' AND OCCURRENCES_REGEXPR('x_regNom' IN z."OrdenEjecucionFull") > 0 THEN 'N'
				 WHEN SUBSTRING(z."IdTipoPaso",2,6) = 'NOMRET' THEN 'S'
				 WHEN SUBSTRING(z."IdTipoPaso",2,6) = 'NOMAMB' AND OCCURRENCES_REGEXPR('x_regNom' IN z."OrdenEjecucionFull") > 0 THEN 'N'
				 WHEN SUBSTRING(z."IdTipoPaso",2,6) = 'NOMAMB' THEN 'S'
				 WHEN SUBSTRING(z."IdTipoPaso",2,6) = 'GTANOM' AND OCCURRENCES_REGEXPR('x_regNom' IN z."OrdenEjecucionFull") > 0 THEN 'N'
				 WHEN SUBSTRING(z."IdTipoPaso",2,6) = 'GTANOM' THEN 'S'	
				 WHEN OCCURRENCES_REGEXPR('x_regNom' IN z."OrdenEjecucionFull") > 0 THEN 'S'
				 ELSE 'N'
			END AS "EjecutarPasoSiendoNominal",z."CodigoMensajePaso",
			CASE WHEN OCCURRENCES_REGEXPR('_vTran' IN z."OrdenEjecucionFull") > 0 THEN 'S' ELSE 'N' END AS "PasoValidaCustodiaOrigenTransito"
		FROM
		(SELECT
			CASE WHEN OCCURRENCES_REGEXPR('~' IN y."OrdenFull") > 0 THEN SUBSTR_AFTER(y."OrdenFull",'~') ELSE y."OrdenFull" END AS "OrdenEjecucionFull",
			SUBSTR_BEFORE(y."OrdenFull",'~') AS "OrigenDatosFull",
			y.*
		FROM
		(SELECT
			SUBSTR_REGEXPR( '([[:digit:]]{2})' IN SUBSTRING (x."PasoEjecucion",1,x."PosFinalRelacionado")) AS "PasoRelacionadoCon",
			SUBSTRING (x."PasoEjecucion",x."PosInicioOrden","PosFinalOrden"-"PosInicioOrden") AS "OrdenFull",
			SUBSTRING (x."PasoEjecucion",x."PosIdTipoOperacion"+1,6) AS "IdTipoOperacionPaso",
			SUBSTRING (x."PasoEjecucion",x."PosIdConcepto"+1,6) AS "IdConceptoPaso",
			SUBSTRING (x."PasoEjecucion",x."PosOrigenInformacion"+1,3) AS "OrigenInformacion",
			SUBSTRING (x."PasoEjecucion",x."PosIdTipoTasaRetorno"+1,4) AS "IdTipoTasaRetorno",
			SUBSTRING (x."PasoEjecucion",x."PosIdEstado"+1,3) AS "IdEstadoPaso",
			SUBSTRING (x."PasoEjecucion",x."PosIniMensaje","PosFinMensaje"-"PosIniMensaje") AS "CodigoMensajePaso",
			x.*
		FROM
		(SELECT
			w."PosFinalRelacionado" + 1 AS "PosInicioOrden",
			LENGTH(w."PasoEjecucion")+1 AS "PosFinalOrden",
			w."PosTipoRegistro",w."PosFinalRelacionado",w."IdTipoPaso",
			CASE WHEN w."PosIdTipoOperacion" > 0 THEN w."PosIdTipoOperacion" ELSE NULL END AS "PosIdTipoOperacion",
			CASE WHEN w."PosIdConcepto" > 0 THEN w."PosIdConcepto" ELSE NULL END AS "PosIdConcepto",
			CASE WHEN w."PosIdTipoTasaRetorno" > 0 THEN w."PosIdTipoTasaRetorno" ELSE NULL END AS "PosIdTipoTasaRetorno",
			CASE WHEN w."PosIdEstado" > 0 THEN w."PosIdEstado" ELSE NULL END AS "PosIdEstado",w."PosOrigenInformacion",
			w."PosIniMensaje" + 4 AS "PosIniMensaje",w."PosFinMensaje",
			--ORIGEN INFORMACION
			w."IdConfiguracionMovimientoCustodia",w."IdTipoOperacion",w."IdEmpresa",w."EsGarantia",
			w."RetornaCarteraAlPrecioTasa",w."IdEstadoOperacion",w."IdOrdenPaso",w."PasoEjecucion",
			--
			w."CliIngGeneraMvtoEnTransitoSinCustodia",w."CliEgrGeneraMvtoEnTransitoSinCustodia"
		FROM
		(SELECT
			v."IdTipoPaso",v."PasoEjecucion",
			LOCATE (v."PasoEjecucion",'?',1) AS "PosTipoRegistro",
			LOCATE (v."PasoEjecucion",'!',1) AS "PosFinalRelacionado",
			LOCATE (v."PasoEjecucion",'Õ',1) AS "PosIdTipoOperacion",
			LOCATE (v."PasoEjecucion",'©',1) AS "PosIdConcepto",
			LOCATE (v."PasoEjecucion",'ï',1) AS "PosOrigenInformacion",
			LOCATE (v."PasoEjecucion",'%',1) AS "PosIdTipoTasaRetorno",
			LOCATE (v."PasoEjecucion",'ê',1) AS "PosIdEstado",
			LOCATE (v."PasoEjecucion",'_men',1) AS "PosIniMensaje",
			LOCATE (v."PasoEjecucion",'men_',1) AS "PosFinMensaje",
			v."IdConfiguracionMovimientoCustodia",v."IdTipoOperacion",v."IdEmpresa",v."EsGarantia",
			v."RetornaCarteraAlPrecioTasa",v."IdEstadoOperacion",v."IdOrdenPaso",
			--
			v."CliIngGeneraMvtoEnTransitoSinCustodia",v."CliEgrGeneraMvtoEnTransitoSinCustodia"
		 FROM
		(SELECT
			REPLACE(REPLACE(REPLACE(REPLACE(
											REPLACE(REPLACE(REPLACE(REPLACE(
																			REPLACE(REPLACE(REPLACE(REPLACE(
																											REPLACE(REPLACE(REPLACE(REPLACE("PasoEjecucionAux",'CLIING_ING_TRA',:v_CLIING_ING_TRA),
																													'CLIING_ING_CAR',:v_CLIING_ING_CAR),'CLIING_ING_SUB',:v_CLIING_ING_SUB),'CLIING_ING_GAR',:v_CLIING_ING_GAR),
																					'CLIING_EGR_TRA',:v_CLIING_EGR_TRA),'CLIING_EGR_CAR',:v_CLIING_EGR_CAR),'CLIING_EGR_SUB',:v_CLIING_EGR_SUB),'CLIING_EGR_GAR',:v_CLIING_EGR_GAR),
													'CLIING_ING_AUTO_TRA',:v_CLIING_ING_AUTO_TRA),'CLIING_ING_AUTO_CAR',:v_CLIING_ING_AUTO_CAR),'CLIING_ING_AUTO_SUB',:v_CLIING_ING_AUTO_SUB),'CLIING_ING_AUTO_GAR',:v_CLIING_ING_AUTO_GAR),
					'CLIING_EGR_AUTO_TRA',:v_CLIING_EGR_AUTO_TRA),'CLIING_EGR_AUTO_CAR',:v_CLIING_EGR_AUTO_CAR),'CLIING_EGR_AUTO_SUB',:v_CLIING_EGR_AUTO_SUB),'CLIING_EGR_AUTO_GAR',:v_CLIING_EGR_AUTO_GAR)  AS "PasoEjecucion",
			u.*
		FROM
		(SELECT
			REPLACE(REPLACE(REPLACE(REPLACE(
											REPLACE(REPLACE(REPLACE(REPLACE(
																			REPLACE(REPLACE(REPLACE(REPLACE(
																											REPLACE(REPLACE(REPLACE(REPLACE("PasoEjecucionOriginal",'CLIEGR_ING_TRA',:v_CLIEGR_ING_TRA),
																													'CLIEGR_ING_CAR',:v_CLIEGR_ING_CAR),'CLIEGR_ING_SUB',:v_CLIEGR_ING_SUB),'CLIEGR_ING_GAR',:v_CLIEGR_ING_GAR),
																					'CLIEGR_EGR_TRA',:v_CLIEGR_EGR_TRA),'CLIEGR_EGR_CAR',:v_CLIEGR_EGR_CAR),'CLIEGR_EGR_SUB',:v_CLIEGR_EGR_SUB),'CLIEGR_EGR_GAR',:v_CLIEGR_EGR_GAR),
													'CLIEGR_ING_AUTO_TRA',:v_CLIEGR_ING_AUTO_TRA),'CLIEGR_ING_AUTO_CAR',:v_CLIEGR_ING_AUTO_CAR),'CLIEGR_ING_AUTO_SUB',:v_CLIEGR_ING_AUTO_SUB),'CLIEGR_ING_AUTO_GAR',:v_CLIEGR_ING_AUTO_GAR),
					'CLIEGR_EGR_AUTO_TRA',:v_CLIEGR_EGR_AUTO_TRA),'CLIEGR_EGR_AUTO_CAR',:v_CLIEGR_EGR_AUTO_CAR),'CLIEGR_EGR_AUTO_SUB',:v_CLIEGR_EGR_AUTO_SUB),'CLIEGR_EGR_AUTO_GAR',:v_CLIEGR_EGR_AUTO_GAR) AS "PasoEjecucionAux",
			t.*
		 FROM
		(SELECT
			SUBSTRING(p."Item",1,8) AS "IdTipoPaso",
			CASE WHEN i."Orden" = 1 THEN SUBSTRING(i."Item",9,LENGTH(i."Item")-8) ELSE i."Item" END AS "PasoEjecucionOriginal",
			c."IdConfiguracionMovimientoCustodia",c."IdTipoOperacion",c."IdEmpresa",c."EsGarantia",
			c."RetornaCarteraAlPrecioTasa",c."IdEstadoOperacion",i."Orden" AS "IdOrdenPaso",
			--
			c."CliIngGeneraMvtoEnTransitoSinCustodia",c."CliEgrGeneraMvtoEnTransitoSinCustodia"
		FROM :v_ConfiguracionMovimientoCustodia AS c
		INNER JOIN :v_out_ItemsConfiguraciones AS ic ON (ic."IdInterno" = c."IdConfiguracionMovimientoCustodia")
		INNER JOIN :v_out_ItemsConfiguracionesCorregido AS icc ON (icc."IdInterno" = ic."IdInterno"
															   AND icc."Orden" = ic."Orden")
		INNER JOIN :v_out_Items AS i ON (i."IdInterno" = icc."IdInternoCorregido")
		INNER JOIN :v_out_Items AS p ON (p."IdInterno" = i."IdInterno"
									 AND p."Orden" = 1)) AS t)AS u) AS v) AS w)AS x) AS y) AS z;
	 	
		IF (:p_EsDemo = 'N') THEN
			UPDATE "BSCL.Custodia.BD::CUS.OrdenEjecucionOperacionesMovimientoCustodia"
			SET
				"IdEstadoRegistro" = 'INA',"IdUsuarioModificacionRegistro" = :p_IdUsuario,"FechaModificacionRegistro" = CURRENT_UTCTIMESTAMP
			WHERE "IdEstadoRegistro" = 'ACT';
			
			INSERT INTO "BSCL.Custodia.BD::CUS.OrdenEjecucionOperacionesMovimientoCustodia" (
				"IdOrdenEjecucionOperacionesMovimientoCustodia","IdEmpresa","IdTipoOperacion",
				----------------------------------------------------
				"IdTipoPaso","IdOrdenPaso","IdPasoEjecucion","NombreColumnaPaso","PasoEsIngreso","TablaAfectadaPorPaso",
				"PasoPerteneceClienteRetiro",
				------------------------------------------------------------------------------------------------
				"PasoEjecucion","TablaOrigenDatosDelPaso","OrigenDatosDelPaso","PasoRelacionadoCon","IdEstadoPaso",
				---------------------------------------------------------
				"PasoMantieneFolioCustodia","PasoUtilizaInstrumentoRelacionado","TipoRegistroDelPaso","SeValorizaPaso","PasoTieneConfiguradaContingencia",
				"EsPasoContingencia","EjecutarPasoHabiendoContingencia","ContinuarEjecucionPasosNoTeniendoSaldo","EjecutarPasoNoTeniendoSaldo",
				"PasoRetiraCarteraOperacionOrigen","AgregarSumaDeCustodiaAlPaso","IdTipoOperacionPaso","IdConceptoPaso","RetornaCarteraAlPrecioTasaPaso",
				"PasoEsCustodiaRetornada","PasoMueveCustodia","PasoPermiteCertificacion","EjecutarPasoSiendoNominal","CodigoMensajePaso",
				"PasoValidaCustodiaOrigenTransito",
				----------------------------------------------------------------------
				"IdEstadoRegistro","IdUsuarioModificacionRegistro","FechaModificacionRegistro")
			SELECT
				"BSCL.Custodia.BD.Secuencias::IdOrdenEjecucionOperacionesMovimientoCustodia".NEXTVAL,
				"IdEmpresa","IdTipoOperacion",
				-------------------------------
				"IdTipoPaso","IdOrdenPaso","IdPasoEjecucion","COLUMN_NAME",CASE WHEN "PasoEsIngreso" = 0 THEN 'N' ELSE 'S' END,
				"TablaAfectadaPorPaso",CASE WHEN "PasoPerteneceClienteRetiro" = 0 THEN 'N' ELSE 'S' END,
				------------------------------------------------------------------------------------------------
				"PasoEjecucion",CASE WHEN "OrigenDatosDelPaso" = 0 THEN NULL ELSE "TablaOrigenDatosDelPaso" END,
				"OrigenDatosDelPaso",
				--"PasoRelacionadoCon",
				CASE WHEN "TablaAfectadaPorPaso" = 'TRA' THEN "PasoRelacionadoCon"
				     WHEN "OrigenDatosDelPaso" != 0 THEN "OrigenDatosDelPaso"
                     ELSE "PasoRelacionadoCon"
                END,
				"IdEstadoPaso",
				---------------------------------------------------------
				"PasoMantieneFolioCustodia","PasoUtilizaInstrumentoRelacionado","TipoRegistroDelPaso","SeValorizaPaso","PasoTieneConfiguradaContingencia",
				"EsPasoContingencia","EjecutarPasoHabiendoContingencia","ContinuarEjecucionPasosNoTeniendoSaldo","EjecutarPasoNoTeniendoSaldo",
				"PasoRetiraCarteraOperacionOrigen","AgregarSumaDeCustodiaAlPaso","IdTipoOperacionPaso","IdConceptoPaso","RetornaCarteraAlPrecioTasaPaso",
				"PasoEsCustodiaRetornada","PasoMueveCustodia","PasoPermiteCertificacion","EjecutarPasoSiendoNominal","CodigoMensajePaso",
				"PasoValidaCustodiaOrigenTransito",
				------------
				'ACT',:p_IdUsuario,CURRENT_UTCTIMESTAMP
			FROM (SELECT
					t."COLUMN_NAME",OCCURRENCES_REGEXPR('ING' IN t."COLUMN_NAME") AS "PasoEsIngreso",
					CASE WHEN OCCURRENCES_REGEXPR('MueveCarteraEnTransito' IN t."COLUMN_NAME") > 0 THEN 'TRA'
						 WHEN OCCURRENCES_REGEXPR('MueveCartera' IN t."COLUMN_NAME") > 0 THEN 'CAR'
						 WHEN OCCURRENCES_REGEXPR('MueveSubyacente' IN t."COLUMN_NAME") > 0 THEN 'SUB'
						 WHEN OCCURRENCES_REGEXPR('MueveGarantia' IN t."COLUMN_NAME") > 0 THEN 'GAR'
						 ELSE ''
					END AS "TablaAfectadaPorPaso",
					OCCURRENCES_REGEXPR('CliEgr' IN t."COLUMN_NAME") AS "PasoPerteneceClienteRetiro",
					p.*
				FROM "SYS"."TABLE_COLUMNS" AS t
				INNER JOIN :v_PasosPorOperacion AS p ON (p."IdPasoEjecucion" = t."POSITION")
				WHERE t."SCHEMA_NAME" = 'BSCL'
				  AND t."TABLE_NAME" = 'BSCL.Custodia.BD::CUS.ConfiguracionMovimientoCustodia')
			ORDER BY "IdEmpresa","IdTipoOperacion","IdTipoPaso","IdOrdenPaso";
		ELSE
			SELECT 'v_out_ItemsConfiguraciones', * FROM :v_out_ItemsConfiguraciones ORDER BY "IdInterno","Orden";
			SELECT 'v_out_Items', * FROM :v_out_Items ORDER BY "IdInterno","Orden";
			SELECT
				"IdConfiguracionMovimientoCustodia","IdEmpresa","IdTipoOperacion",
				-------------------------------
				"IdTipoPaso","IdOrdenPaso","IdPasoEjecucion","COLUMN_NAME" AS "NombreColumnaPaso",
				CASE WHEN "PasoEsIngreso" = 0 THEN 'N' ELSE 'S' END AS "PasoEsIngreso","TablaAfectadaPorPaso",
				CASE WHEN "PasoPerteneceClienteRetiro" = 0 THEN 'N' ELSE 'S' END AS "PasoPerteneceClienteRetiro",
				------------------------------------------------------------------------------------------------
				"PasoEjecucion",CASE WHEN "OrigenDatosDelPaso" = 0 THEN NULL ELSE "TablaOrigenDatosDelPaso" END AS "TablaOrigenDatosDelPaso",
				"OrigenDatosDelPaso",
				--"PasoRelacionadoCon",
				CASE WHEN "TablaAfectadaPorPaso" = 'TRA' THEN "PasoRelacionadoCon"
                     WHEN "OrigenDatosDelPaso" != 0 THEN "OrigenDatosDelPaso"
                     ELSE "PasoRelacionadoCon"
                END,
				"IdEstadoPaso",
				---------------------------------------------------------
				"PasoMantieneFolioCustodia","PasoUtilizaInstrumentoRelacionado","TipoRegistroDelPaso","SeValorizaPaso","PasoTieneConfiguradaContingencia",
				"EsPasoContingencia","EjecutarPasoHabiendoContingencia","ContinuarEjecucionPasosNoTeniendoSaldo","EjecutarPasoNoTeniendoSaldo",
				"PasoRetiraCarteraOperacionOrigen","AgregarSumaDeCustodiaAlPaso","IdTipoOperacionPaso","IdConceptoPaso","RetornaCarteraAlPrecioTasaPaso",
				"PasoEsCustodiaRetornada","PasoMueveCustodia","PasoPermiteCertificacion","EjecutarPasoSiendoNominal","CodigoMensajePaso",
				"PasoValidaCustodiaOrigenTransito",
				------------
				'ACT' AS "IdEstadoRegistro",:p_IdUsuario AS "IdUsuarioModificacionRegistro",CURRENT_UTCTIMESTAMP AS "FechaModificacionRegistro"
			FROM (SELECT
					t."COLUMN_NAME",OCCURRENCES_REGEXPR('ING' IN t."COLUMN_NAME") AS "PasoEsIngreso",
					CASE WHEN OCCURRENCES_REGEXPR('MueveCarteraEnTransito' IN t."COLUMN_NAME") > 0 THEN 'TRA'
						 WHEN OCCURRENCES_REGEXPR('MueveCartera' IN t."COLUMN_NAME") > 0 THEN 'CAR'
						 WHEN OCCURRENCES_REGEXPR('MueveSubyacente' IN t."COLUMN_NAME") > 0 THEN 'SUB'
						 WHEN OCCURRENCES_REGEXPR('MueveGarantia' IN t."COLUMN_NAME") > 0 THEN 'GAR'
						 ELSE ''
					END AS "TablaAfectadaPorPaso",
					OCCURRENCES_REGEXPR('CliEgr' IN t."COLUMN_NAME") AS "PasoPerteneceClienteRetiro",
					p.*
				FROM "SYS"."TABLE_COLUMNS" AS t
				INNER JOIN :v_PasosPorOperacion AS p ON (p."IdPasoEjecucion" = t."POSITION")
				WHERE t."SCHEMA_NAME" = 'BSCL'
				  AND t."TABLE_NAME" = 'BSCL.Custodia.BD::CUS.ConfiguracionMovimientoCustodia')
			ORDER BY "IdEmpresa","IdTipoOperacion","IdTipoPaso","IdOrdenPaso";
		END IF;--:p_EsDemo = 'N'
		
		INSERT INTO "BSCL.Plataforma.BD::PLA.Depuracion_Entrada_Depuracion" (
			"IdEmpresa","Fecha","IdUsuario","IdServicioAgrupador","ServicioAgrupador","Servicio","Bandera","Detalle") 
		SELECT
			COALESCE(:p_IdEmpresa,0),CURRENT_UTCTIMESTAMP,:p_IdUsuario,:v_IdServicioAgrupador,::CURRENT_OBJECT_NAME,::CURRENT_OBJECT_NAME,
			'Pasos','Fin pasos por operación'
		FROM DUMMY;
		
		CALL "BSCL.Plataforma.BD.Servicios::DepuracionMM"();
	END;
END; 
GO