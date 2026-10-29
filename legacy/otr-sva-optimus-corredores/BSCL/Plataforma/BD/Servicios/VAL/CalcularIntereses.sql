SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::VAL.CalcularIntereses";
GO
CREATE PROCEDURE   
----------------------------------------------------------------------------------------------------- 
--  Proposito: Calculo de intereses
--  Autor: Oscar Carreno Tolmo :: OCT
--  Fecha Creacion: 13/07/2016  
"BSCL.Plataforma.BD.Servicios::VAL.CalcularIntereses" (
    --VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa				BIGINT,
    IN p_IdUsuario				BIGINT,
    IN p_IdModulo				NVARCHAR(3),
    IN p_IdPais					BIGINT DEFAULT - 1,
    --ENUMERAR VARIABLES DE ENTRADA
	IN p_UsaDiccionario			NVARCHAR(1) DEFAULT 'S'
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	--READS SQL DATA
AS
BEGIN
		--TODO--> SI NO VIENE LA BASE QUISAS TRAERLA DESDE UNA CONFIGURACION
		
		DECLARE v_IdTrabajo BIGINT :=0;
		
		DECLARE v_Diccionario TABLE (
			"IdInterno" BIGINT,"Codigo" NVARCHAR(100),"CodigoDato1" NVARCHAR(100),"ValorDato1" NVARCHAR(520),
			"CodigoDato2" NVARCHAR(100),"ValorDato2" NVARCHAR(520))
			DEFAULT SELECT
						0 AS "IdInterno",'' AS "Codigo",'' AS "CodigoDato1",'' AS "ValorDato1",'' AS "CodigoDato2",'' AS "ValorDato2"
					FROM DUMMY WHERE 1 = 2;
		
		DECLARE v_TablaMensajes TABLE (
			"IdInterno" BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000))
			DEFAULT SELECT
						0 AS "IdInterno",'' AS "Codigo",'' AS "Descripcion"
					FROM DUMMY WHERE 1 = 2;
		
		DELETE FROM "BSCL.Plataforma.BD::VAL.CalcularIntereses_Proceso_Indicador";
		DELETE FROM "BSCL.Plataforma.BD::VAL.CalcularIntereses_Salida_Salida";
	
					
		--CALCULO DE INTERESES PARA SIMULTANEAS, PRESTAMOS
		INSERT INTO "BSCL.Plataforma.BD::VAL.CalcularIntereses_Salida_Salida"(
			"IdProceso","CodigoMensaje","Fecha","IdMoneda","Cantidad","Precio","Tasa","Plazo","PlazoResidual","Base",
    		--DEVUELTOS
			"Monto","DiasTranscurridos","TasaPorPlazo","TasaPorDiasTranscurridos","InteresDiario","InteresAcumulado","ValorPresente",
			"MontoAPlazo")
		SELECT distinct
			"IdProceso","CodigoMensaje","Fecha","IdMoneda","Cantidad","Precio","Tasa","Plazo","PlazoResidual","Base",
			--DEVUELTOS
			"Monto","DiasTranscurridos","TasaPorPlazo","TasaPorDiasTranscurridos",
			("Monto" * (1 + "TasaPorPlazo")) - ("Monto" * (1 + "TasaPorDiasTranscurridos")),-- AS "InteresDiario",
			("Monto" * (1 + "TasaPorDiasTranscurridos")) - "Monto",-- AS "InteresAcumulado",
			("Monto" * (1 + "TasaPorDiasTranscurridos")),-- AS "ValorPresente",
			("Monto" * (1 + "TasaPorPlazo"))-- AS "MontoAPlazo"
		FROM
			(SELECT 
				"IdProceso","Fecha","IdMoneda","Cantidad","Precio","Tasa","Plazo","PlazoResidual","Base",
				("Precio" * "Cantidad") AS "Monto",("Plazo" - "PlazoResidual") AS "DiasTranscurridos",
				CASE WHEN "Base" = 0 THEN NULL ELSE ("Tasa"/100)*("Plazo"/"Base") END AS "TasaPorPlazo",
				CASE WHEN "Base" = 0 THEN NULL ELSE ("Tasa"/100)*(("Plazo" - "PlazoResidual")/"Base") END AS "TasaPorDiasTranscurridos",
				CASE WHEN "Base" = 0 THEN 'BASE_CERO' ELSE '' END AS "CodigoMensaje"
			 FROM "BSCL.Plataforma.BD::VAL.CalcularIntereses_Entrada_Entrada");
		
		
		INSERT INTO "BSCL.Plataforma.BD::VAL.CalcularIntereses_Proceso_Indicador"(
			"IdProceso","Fecha","MonedaOrigen","MontoOrigen","EsMontoAPlazo")
		SELECT
			"IdProceso","Fecha","IdMoneda","Monto",'N'
		FROM "BSCL.Plataforma.BD::VAL.CalcularIntereses_Salida_Salida"
		WHERE "CodigoMensaje" = '';
		
		INSERT INTO "BSCL.Plataforma.BD::VAL.CalcularIntereses_Proceso_Indicador"(
			"IdProceso","Fecha","MonedaOrigen","MontoOrigen","EsMontoAPlazo")
		SELECT
			"IdProceso","Fecha","IdMoneda","MontoAPlazo",'S'
		FROM "BSCL.Plataforma.BD::VAL.CalcularIntereses_Salida_Salida"
		WHERE "CodigoMensaje" = '';
		
		IF (NOT IS_EMPTY("BSCL.Plataforma.BD::VAL.CalcularIntereses_Proceso_Indicador")) THEN
			UPDATE i
			SET
				"IdIndicador" = si."IdIndicador"
			FROM "BSCL.Plataforma.BD::VAL.CalcularIntereses_Proceso_Indicador" i
			INNER JOIN (SELECT "IdProceso",ROW_NUMBER() over (ORDER BY "IdProceso") AS "IdIndicador"
					    FROM "BSCL.Plataforma.BD::VAL.CalcularIntereses_Proceso_Indicador") AS si ON (si."IdProceso" = i."IdProceso");
				
			SELECT "BSCL.Plataforma.BD.Secuencias::MER.IdTrabajoParidad".NEXTVAL INTO v_IdTrabajo FROM DUMMY;
			
			INSERT INTO "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada"(
				"IdTrabajo","IdInterno","Fecha","IdMonedaOrigen","IdMonedaDestino","ValorCambioLocalModificado","MontoOrigen","IdUso")
			SELECT
				:v_IdTrabajo,"IdIndicador","Fecha","MonedaOrigen",'USD',0,"MontoOrigen",'CARTERA'
			FROM "BSCL.Plataforma.BD::VAL.CalcularIntereses_Proceso_Indicador";
	
			CALL "BSCL.Plataforma.BD.Servicios::MER.CalcularParidadYTipoCambio"(
				p_IdEmpresa	=> :p_IdEmpresa,
				p_IdTrabajo	=> :v_IdTrabajo);	
			
			UPDATE s
			SET
				"MontoMonedaLocal" = COALESCE(m."MontoLocal",0),
				"MontoAPlazoMonedaLocal" = COALESCE(mp."MontoLocal",0),
				"CodigoMensaje" = COALESCE(m."CodigoMensaje",mp."CodigoMensaje",'')
			FROM "BSCL.Plataforma.BD::VAL.CalcularIntereses_Salida_Salida" s
			INNER JOIN (SELECT
							i."IdProceso",p."MontoLocal",p."Resultado",p."CodigoMensaje"
						FROM "BSCL.Plataforma.BD::VAL.CalcularIntereses_Proceso_Indicador" i
						INNER JOIN "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida" p ON (p."IdInterno" = i."IdIndicador"
																										AND p."IdTrabajo" = :v_IdTrabajo)
						WHERE i."EsMontoAPlazo" = 'N') AS m ON (m."IdProceso" = s."IdProceso")
			INNER JOIN (SELECT
							i."IdProceso",p."MontoLocal",p."Resultado",p."CodigoMensaje"
						FROM "BSCL.Plataforma.BD::VAL.CalcularIntereses_Proceso_Indicador" i
						INNER JOIN "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida" p ON (p."IdInterno" = i."IdIndicador"
																										AND p."IdTrabajo" = :v_IdTrabajo)
						WHERE i."EsMontoAPlazo" = 'S') AS mp ON (mp."IdProceso" = s."IdProceso");
		END IF; --IF (NOT IS_EMPTY("BSCL.Plataforma.BD::VAL.CalcularIntereses_Proceso_Indicador")) THEN
		
		IF (:p_UsaDiccionario = 'S') THEN
			v_Diccionario =
				SELECT
					"IdProceso" AS "IdInterno","CodigoMensaje" AS "Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2"
				FROM "BSCL.Plataforma.BD::VAL.CalcularIntereses_Salida_Salida"
				WHERE "CodigoMensaje" != '';
			
			IF (NOT IS_EMPTY(:v_Diccionario)) THEN
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (
					P_TablaCodigosMensajes	=> :v_Diccionario,
					p_out_TablaMensajes		=> v_TablaMensajes);
				
				UPDATE s
				SET
					"DescripcionMensaje" = COALESCE(m."Descripcion",'')
				FROM "BSCL.Plataforma.BD::VAL.CalcularIntereses_Salida_Salida" s
				INNER JOIN :v_TablaMensajes AS m ON (m."IdInterno" = s."IdProceso");
			END IF;
		END IF;
		
		DELETE FROM "BSCL.Plataforma.BD::VAL.CalcularIntereses_Entrada_Entrada";
END;
GO
