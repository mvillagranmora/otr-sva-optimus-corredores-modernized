SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Custodia.BD.Utilitarios::OrdenEjecucionMovimientoCustodia";

CREATE PROCEDURE "BSCL.Custodia.BD.Utilitarios::OrdenEjecucionMovimientoCustodia"(
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
		 -->los simbolos [], indican que existe un paso de contingencia para el paso, si no hay saldo para el registro, no enviara error, intentara ejecutar el siguiente paso
		 -->Si en el registro no hay tipo de operacion o concepto, se utilizaran los de la operacion, que son enviados como parametros de entrada. ejemplo mioper, miconc (primer registro).
		 -->Un registro no puede ser indicado como origen de datos y relacionado, es solo uno a la vez, si es indicado como que sus datos vienen de un origen, internamente se graba el campo relacionado.
--  Autor: Oscar Carreno Tolmo - OCT
--  Fecha Creacion: 23/10/2016
	IN p_TicketAtencion		BIGINT	DEFAULT 0,
	IN p_EsDemo				NVARCHAR(1)	DEFAULT 'N', --No graba registros en las tablas de parametros.
	IN p_NumeroEjecucion	TINYINT DEFAULT 0
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA BSCL
AS
BEGIN
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		BEGIN AUTONOMOUS TRANSACTION
			UPDATE "BSCL.Custodia.BD::PAR.GenerarMovimientoCustodiaParametrosEntrada"
				SET "ProcesoConProblemas" = 'S',"FechaFinProceso" = CURRENT_UTCTIMESTAMP
			WHERE "IdProcesoMovimientoCustodia" = :p_TicketAtencion;
	
			INSERT INTO "BSCL.Custodia.BD::CUS.GenerarMovimientoCustodiaLog"(
				"IdGenerarMovimientoCustodiaLog","IdProcesoMovimientoCustodia",
				"FechaProcesoMovimientoCustodia","IdInterno",
				--CAMPOS DE ERRORES
				"OrigenError","TipoProblema","CodigoMensaje","CodigoDato1","ValorDato1")
			SELECT
				"BSCL.Custodia.BD.Secuencias::GenerarMovimientoCustodiaLog".NEXTVAL,
				:p_TicketAtencion,CURRENT_UTCTIMESTAMP,0,
				--CAMPOS DE ERRORES
				'BSCL.Custodia.BD.Utilitarios::OrdenEjecucionMovimientoCustodia','E',
				'ERROR_INESPERADO',::SQL_ERROR_CODE,::SQL_ERROR_MESSAGE
			FROM DUMMY;
			COMMIT;
		END;--AUTONOMOUS TRANSACTION
		RESIGNAL;
	END;
	
	BEGIN
		--VARIABLES DE ENTRADA
		DECLARE v_IdEmpresa,v_IdUsuario,v_IdProceso,v_IdServicioAgrupador BIGINT :=0;
		DECLARE v_ServicioAgrupador NVARCHAR(255) := '';
		
		DECLARE v_OrdenEjecucionMovimientos	  TABLE ("IdInterno" BIGINT,"Texto" NVARCHAR(500));
        DECLARE v_out_Items					  TABLE ("IdInterno" BIGINT,"Orden" INTEGER,"Item" NVARCHAR(500));
        
        DECLARE v_Orden TABLE (
			"IdInterno" BIGINT,"IdOrden" BIGINT,"IdContingencia" BIGINT,"IdSinSaldo" BIGINT,"IdRetornoMismaCartera" BIGINT,
			"IdSinRetirarCustodia" BIGINT,"PasoEjecucion" NVARCHAR(500),"OrdenEjecucion" INT,"TipoRegistro" NVARCHAR(1),"SeValoriza" NVARCHAR(1),
			"IdEstado" NVARCHAR(3),"IdTipoOperacion" NVARCHAR(6),"IdConcepto" NVARCHAR(6),"RetornaCarteraAlPrecioTasa" NVARCHAR(4),
			"TablaOrigenDatos" NVARCHAR(3),"OrigenDatos" INT,"Relacionado" INT,"TienePasoContingencia" NVARCHAR(1),
			"PasoContingencia" NVARCHAR(500),"TienePasoSinSaldo" NVARCHAR(1),"PasoSinSaldo" NVARCHAR(500),"TienePasoRetornoMismaCartera" NVARCHAR(1),
			"PasoRetornoMismaCartera" NVARCHAR(500),"TienePasoSinRetirarCustodia" NVARCHAR(1),"PasoSinRetirarCustodia" NVARCHAR(500),
			"EsCustodiaRetornada" NVARCHAR(1));
		
		DECLARE v_OrdenEjecutarOperacionSplit TABLE (
        	"IdInterno" BIGINT,"IdContingencia" BIGINT,"IdEstado" NVARCHAR(3),"IdTipoOperacion" NVARCHAR(6),
        	"IdConcepto" NVARCHAR(6),"Orden" INTEGER,"Item" NVARCHAR(500));
        
        SELECT COALESCE(MAX("IdProceso"),0) + 1 INTO v_IdProceso FROM "BSCL.Custodia.BD::GBL.GenerarMovimientoCustodia_Salida_Depuracion" ;
		
        SELECT "IdEmpresa","IdUsuario","IdServicioAgrupador","ServicioAgrupador" INTO v_IdEmpresa,v_IdUsuario,v_IdServicioAgrupador,v_ServicioAgrupador
        FROM "BSCL.Custodia.BD::GBL.GenerarMovimientoCustodia_Entrada_Parametros";
		
		INSERT INTO "BSCL.Custodia.BD::GBL.GenerarMovimientoCustodia_Salida_Depuracion" ("IdProceso","NombreProceso","FechaInicioProceso")
		SELECT :v_IdProceso,::CURRENT_OBJECT_NAME,CURRENT_UTCTIMESTAMP FROM DUMMY;

		INSERT INTO "BSCL.Plataforma.BD::PLA.Depuracion_Entrada_Depuracion" (
			"IdEmpresa","Fecha","IdUsuario","IdServicioAgrupador","ServicioAgrupador","Servicio","Bandera","Detalle") 
		SELECT
			:v_IdEmpresa,CURRENT_UTCTIMESTAMP,:v_IdUsuario,:v_IdServicioAgrupador,:v_ServicioAgrupador,::CURRENT_OBJECT_NAME,
			'Inicio','Inicio Utilitario'
		FROM DUMMY;
		
		--EL TEXTO CON EL ORDEN DE EJECUCION SE ENVIA AL SPLIT
		v_OrdenEjecucionMovimientos =
			SELECT COALESCE("IdContingencia","IdInterno") AS "IdInterno","Texto"
			FROM "BSCL.Custodia.BD::PAR.OrdenEjecucionMovimientoCustodia"
			WHERE "IdProcesoMovimientoCustodia" = :p_TicketAtencion
			  AND "NumeroEjecucion" = :p_NumeroEjecucion;
		
		CALL "BSCL.Plataforma.BD.Utilitarios::SplitLlaveValor"(
			p_Texto		=>	:v_OrdenEjecucionMovimientos,
			p_Split		=>	',',
			p_out_Items	=>	v_out_Items);
		
		INSERT INTO "BSCL.Plataforma.BD::PLA.Depuracion_Entrada_Depuracion" (
			"IdEmpresa","Fecha","IdUsuario","IdServicioAgrupador","ServicioAgrupador","Servicio","Bandera","Detalle") 
		SELECT
			:v_IdEmpresa,CURRENT_UTCTIMESTAMP,:v_IdUsuario,:v_IdServicioAgrupador,:v_ServicioAgrupador,::CURRENT_OBJECT_NAME,
			'SplitLlaveValor','Fin llamada al servicio BSCL.Plataforma.BD.Utilitarios::SplitLlaveValor'
		FROM DUMMY;
		
		v_OrdenEjecutarOperacionSplit =
		SELECT
			e."IdInterno","IdContingencia",CASE WHEN COALESCE(e."IdEstado",'') = '' THEN NULL ELSE e."IdEstado" END AS "IdEstado",
			e."IdTipoOperacion",e."IdConcepto",i."Orden",CAST(i."Item" AS NVARCHAR(500)) AS "Item"
		FROM "BSCL.Custodia.BD::PAR.OrdenEjecucionMovimientoCustodia" AS e
		INNER JOIN :v_out_Items AS i ON(i."IdInterno" = COALESCE(e."IdContingencia",e."IdInterno"))
		WHERE "IdProcesoMovimientoCustodia" = :p_TicketAtencion
		  AND "NumeroEjecucion" = :p_NumeroEjecucion;
			
		--TODO, HACER EL PASO DE SALDO Y PASO DE VOLVER A LA MISMA CARTERA
		v_Orden =
		SELECT
			"IdInterno","Orden" AS "IdOrden",COALESCE("IdContingencia",0) AS "IdContingencia",NULL AS "IdSinSaldo",
			NULL AS "IdRetornoMismaCartera",NULL AS "IdSinRetirarCustodia","Item" AS "PasoEjecucion",
			CAST(SUBSTR_REGEXPR( '([[:digit:]]{2})' IN "OrdenEjecucionFull") AS INT) AS "OrdenEjecucion",
			CAST(SUBSTRING("Item","PosTipoRegistro"-1,1) AS NVARCHAR(1)) AS "TipoRegistro",
			CASE WHEN OCCURRENCES_REGEXPR('ƒ' IN "OrdenEjecucionFull") > 0 THEN 'S' ELSE 'N' END AS "SeValoriza",
			CASE WHEN OCCURRENCES_REGEXPR('ë' IN "OrdenEjecucionFull") > 0 THEN "IdEstado" ELSE NULL END AS "IdEstado",
			COALESCE("IdTipoOperacionNuevo","IdTipoOperacion") AS "IdTipoOperacion",
			COALESCE("IdConceptoNuevo","IdConcepto") AS "IdConcepto",
			NULL AS "RetornaCarteraAlPrecioTasa",
			COALESCE("OrigenDatos",'DAT') AS "TablaOrigenDatos",
			CAST(COALESCE(SUBSTR_REGEXPR( '([[:digit:]]{2})' IN "OrigenDatosFull"),0) AS INT) AS "OrigenDatos",
			CASE WHEN "OrigenDatosFull" != '' THEN CAST(COALESCE("OrigenDatosFull",0) AS INT)
				 ELSE CAST(COALESCE("Relacionado",0) AS INT)
			END AS "Relacionado",
			CASE WHEN LENGTH("PasoContingencia") > 0 THEN 'S' ELSE 'N' END AS "TienePasoContingencia",
			CAST(REPLACE("PasoContingencia",'.',',') AS NVARCHAR(500)) AS "PasoContingencia",NULL AS "TienePasoSinSaldo",
			NULL AS "PasoSinSaldo",NULL AS "TienePasoRetornoMismaCartera",NULL AS "PasoRetornoMismaCartera",
			NULL AS "TienePasoSinRetirarCustodia",NULL AS "PasoSinRetirarCustodia",
			CASE WHEN OCCURRENCES_REGEXPR('ç' IN "OrdenEjecucionFull") > 0 THEN 'S' ELSE 'N' END AS "EsCustodiaRetornada"
		FROM
		(SELECT
			CASE WHEN OCCURRENCES_REGEXPR('~' IN "OrdenFull") > 0 THEN SUBSTR_AFTER("OrdenFull",'~') ELSE "OrdenFull" END AS "OrdenEjecucionFull",
			SUBSTR_BEFORE("OrdenFull",'~') AS "OrigenDatosFull",
			*
		FROM
		(
		SELECT
			SUBSTR_REGEXPR( '([[:digit:]]{2})' IN SUBSTRING ("Item",1,"PosFinalRelacionado")) AS "Relacionado",
			SUBSTRING ("Item","PosInicioContingencia"+1,"PosFinalContingencia"-"PosInicioContingencia"-1) AS "PasoContingencia",
			SUBSTRING ("Item","PosInicioOrden","PosFinalOrden"-"PosInicioOrden") AS "OrdenFull",
			SUBSTRING ("Item","PosIdTipoOperacion"+1,6) AS "IdTipoOperacionNuevo",
			SUBSTRING ("Item","PosIdConcepto"+1,6) AS "IdConceptoNuevo",
			SUBSTRING ("Item","PosOrigenDatos"+1,3) AS "OrigenDatos",
			*
		FROM
		(SELECT
			"PosFinalRelacionado" + 1 AS "PosInicioOrden",
			CASE WHEN "PosInicioContingencia" > 0 THEN "PosInicioContingencia"
				 ELSE LENGTH("Item")+1
			END AS "PosFinalOrden",
			"PosTipoRegistro","PosFinalRelacionado","PosInicioContingencia","PosFinalContingencia",
			CASE WHEN "PosIdTipoOperacion" > 0 THEN "PosIdTipoOperacion" ELSE NULL END AS "PosIdTipoOperacion",
			CASE WHEN "PosIdConcepto" > 0 THEN "PosIdConcepto" ELSE NULL END AS "PosIdConcepto",
			"PosOrigenDatos","IdInterno","IdContingencia","IdEstado","IdTipoOperacion","IdConcepto","Orden","Item"
		FROM
		(SELECT
			LOCATE ("Item",'?',1) AS "PosTipoRegistro",
			LOCATE ("Item",'!',1) AS "PosFinalRelacionado",
			LOCATE ("Item",'[',1) AS "PosInicioContingencia",
			LOCATE ("Item",']',1) AS "PosFinalContingencia",
			LOCATE ("Item",'®',1) AS "PosIdTipoOperacion",
			LOCATE ("Item",'©',1) AS "PosIdConcepto",
			LOCATE ("Item",'ï',1) AS "PosOrigenDatos",
			"IdInterno","IdContingencia","IdEstado","IdTipoOperacion","IdConcepto","Orden","Item"
		FROM :v_OrdenEjecutarOperacionSplit) AS w) AS x) AS y) AS z;
			
		--TODO, Cambiar a la tabla definitiva que creara el nuevo servicio
		INSERT INTO "BSCL.Custodia.BD::GBL.CrearMovimientoCustodia_Salida_OrdenEjecucion"(
			"IdInterno","IdOrden","IdContingencia","IdSinSaldo","IdRetornoMismaCartera",
			"IdSinRetirarCustodia","PasoEjecucion","OrdenEjecucion","TipoRegistro",
			"SeValoriza","IdEstado","IdTipoOperacion","IdConcepto","RetornaCarteraAlPrecioTasa",
			"TablaOrigenDatos","OrigenDatos","Relacionado","TienePasoContingencia",
			"PasoContingencia","TienePasoSinSaldo","PasoSinSaldo","TienePasoRetornoMismaCartera",
			"PasoRetornoMismaCartera","TienePasoSinRetirarCustodia","PasoSinRetirarCustodia",
			"EsCustodiaRetornada")
		SELECT
			"IdInterno","IdOrden","IdContingencia","IdSinSaldo","IdRetornoMismaCartera",
			"IdSinRetirarCustodia","PasoEjecucion","OrdenEjecucion","TipoRegistro",
			"SeValoriza","IdEstado","IdTipoOperacion","IdConcepto","RetornaCarteraAlPrecioTasa",
			CASE WHEN "OrigenDatos" = 0 THEN NULL ELSE "TablaOrigenDatos" END,"OrigenDatos","Relacionado",
			"TienePasoContingencia","PasoContingencia","TienePasoSinSaldo","PasoSinSaldo","TienePasoRetornoMismaCartera",
			"PasoRetornoMismaCartera","TienePasoSinRetirarCustodia","PasoSinRetirarCustodia",
			"EsCustodiaRetornada"
		FROM :v_Orden;
			
		IF (:p_EsDemo = 'S') THEN
			SELECT
				'Salida' AS "v_Orden","IdInterno","IdOrden","IdContingencia","IdSinSaldo","IdRetornoMismaCartera",
				"IdSinRetirarCustodia","PasoEjecucion","OrdenEjecucion","TipoRegistro","SeValoriza","IdEstado",
				"IdTipoOperacion","IdConcepto","RetornaCarteraAlPrecioTasa",
				CASE WHEN "OrigenDatos" = 0 THEN NULL ELSE "TablaOrigenDatos" END,"OrigenDatos","Relacionado",
				"TienePasoContingencia","PasoContingencia","TienePasoSinSaldo","PasoSinSaldo","TienePasoRetornoMismaCartera",
				"PasoRetornoMismaCartera","TienePasoSinRetirarCustodia","PasoSinRetirarCustodia"
			FROM :v_Orden;
		END IF;
		
		INSERT INTO "BSCL.Plataforma.BD::PLA.Depuracion_Entrada_Depuracion" (
			"IdEmpresa","Fecha","IdUsuario","IdServicioAgrupador","ServicioAgrupador","Servicio","Bandera","Detalle") 
		SELECT
			:v_IdEmpresa,CURRENT_UTCTIMESTAMP,:v_IdUsuario,:v_IdServicioAgrupador,:v_ServicioAgrupador,::CURRENT_OBJECT_NAME,
			'Fin','Fin utilitario'
		FROM DUMMY;
		
		UPDATE "BSCL.Custodia.BD::GBL.GenerarMovimientoCustodia_Salida_Depuracion"  SET "FechaFinProceso" = CURRENT_UTCTIMESTAMP,
			   "Diferencia" = SECONDS_BETWEEN ("FechaInicioProceso",CURRENT_UTCTIMESTAMP)
		WHERE "IdProceso" = :v_IdProceso;
	END;
END; 
