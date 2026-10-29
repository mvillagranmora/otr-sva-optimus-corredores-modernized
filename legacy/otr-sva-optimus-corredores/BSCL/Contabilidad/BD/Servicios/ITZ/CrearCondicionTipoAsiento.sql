SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Contabilidad.BD.Servicios::ITZ.CrearCondicionTipoAsiento";

CREATE PROCEDURE 
-- 
--  Proposito: Creación de Registro
--			   
--  Empresa: Ingeniería ProNet IT SpA.
--  Autor: Hans Muller :: HMR
--  Fecha Creacion: 23/10/2016
--  Id Servicio = 12034
--  
"BSCL.Contabilidad.BD.Servicios::ITZ.CrearCondicionTipoAsiento" (
	--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          		BIGINT,
    IN p_IdUsuario          		BIGINT,
    IN p_IdModulo           		NVARCHAR(3),
    IN p_UsaReglasNegocio			NVARCHAR(1),
	------------------------------------------------------------------
	IN p_IdDefinicionAsiento		BIGINT,
	IN p_IdTipoContabilidad			BIGINT,
	IN p_IdTipoDatoEvento			NVARCHAR(30),
	IN p_ValorVarchar				NVARCHAR(60),
	IN p_ValorDecimal				DECIMAL(18,4),
	IN p_Condicion					NVARCHAR(10),
	IN p_Conector					NVARCHAR(10),
	IN p_TipoCondicion				NVARCHAR(1),
	IN p_Estado						NVARCHAR(1),
	IN p_OmiteCondicionCmbte		NVARCHAR(1),
	-- Parametros de Salida
	OUT p_IdCondicion BIGINT,
	OUT p_out_AdvertenciasContador INT,
	OUT p_out_AdvertenciasDetalle TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100), "RequiereAutorizacion" NVARCHAR(40), "Descripcion" NVARCHAR(5000)) DEFAULT EMPTY,
	OUT p_out_ErroresContador INT,
	OUT p_out_ErroresDetalle TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)) DEFAULT EMPTY
 )   
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	AS
	   
BEGIN
	DECLARE v_IdCondicion	BIGINT;
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	
	DECLARE v_out_AdvertenciasDetalle	TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" NVARCHAR(100),"NombreParametro" NVARCHAR(40))
										DEFAULT SELECT 0 as "IdInterno",'' as "Codigo",'' as "RequiereAutorizacion",'' as "ValorParametro",'' as  "NombreParametro" FROM DUMMY WHERE 1=2 ;
	DECLARE v_out_ErroresDetalle		TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100),"ValorParametro" NVARCHAR(100),"NombreParametro" NVARCHAR(40))
										DEFAULT SELECT 0 as "IdInterno",'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" FROM DUMMY WHERE 1=2 ;
	
	DECLARE v_count_operaciones			BIGINT;
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT COUNT(1) INTO p_out_ErroresContador 
		  FROM :v_out_ErroresDetalle;
		
		SELECT COUNT(1) INTO p_out_AdvertenciasContador 
		  FROM :v_out_AdvertenciasDetalle; 	
		
		v_TablaCodigosMensajes = SELECT "Codigo" 			AS "CodigoMensaje",
								        "NombreParametro" 	AS "CodigoDato",
  								        "ValorParametro" 	AS "ValorDato" 
								   FROM :v_out_ErroresDetalle
								 UNION ALL
								 SELECT "Codigo" 			AS "CodigoMensaje",
								        "NombreParametro" 	AS "CodigoDato",
								        "ValorParametro" 	AS "ValorDato" 
								   FROM :v_out_AdvertenciasDetalle;

		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes, v_out_ErroresDetalleMensajes);
		
		p_out_ErroresDetalle = 	SELECT errores."IdInterno",
									   errores."Codigo",
									   msj."Descripcion"
 								  FROM :v_out_ErroresDetalle errores INNER JOIN :v_out_ErroresDetalleMensajes msj on 
 								  														errores."Codigo" = msj."Codigo";

		p_out_AdvertenciasDetalle = SELECT adv."IdInterno",
									       adv."Codigo",
 									       adv."RequiereAutorizacion",
 									       msj."Descripcion"
 								    FROM :v_out_AdvertenciasDetalle adv INNER JOIN :v_out_ErroresDetalleMensajes msj on 
 								    													adv."Codigo" = msj."Codigo";
	END;

	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	
	--------------------
	--Negocio Aqui
	--------------------
	BEGIN -- proceso de negocio      
    	--PARAMETROS REGLAS NEGOCIO
		DECLARE v_resultadoRegla 			NVARCHAR(40);
		DECLARE v_countCondiciones 			BIGINT;

		--*******************************
		--*VALIDACION REGLAS BASICAS *
		--*******************************
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa
		                                                                 ,:p_IdUsuario
		                                                                 ,:p_IdModulo
		                                                                 ,:v_out_ErroresDetalle_Basicos
		                                                                 ,:v_resultadoRegla);
		IF :v_resultadoRegla = 'RECH' THEN
			v_out_ErroresDetalle = SELECT 0 	AS "IdInterno",
			                              '' 	AS "Codigo",
			                              '' 	AS "ValorParametro",
			                              '' 	AS "NombreParametro"
			                         FROM :v_out_ErroresDetalle_Basicos;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		select count(1) into v_countCondiciones from "BSCL.Contabilidad.BD::ITZ.ContabilidadTipoAsientoCondicion"	 
		Where "IdTipoContabilidad" = p_IdTipoContabilidad
		AND "IdDefinicionAsiento" = p_IdDefinicionAsiento
		AND "IdTipoDatoEvento" = p_IdTipoDatoEvento
		AND "ValorVarchar" = p_ValorVarchar
		AND "ValorDecimal" = p_ValorDecimal
		AND "Estado" = p_Estado;
		
		IF :v_countCondiciones > 0 THEN
			v_out_ErroresDetalle = SELECT 0 	AS "IdInterno",
			                              'CONDICION_ASIENTO_REPETIDA' 	AS "Codigo",
			                              '' 	AS "ValorParametro",
			                              '' 	AS "NombreParametro"
			                         FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		

		SELECT "BSCL.Contabilidad.BD.Secuencias::IdTipoAsientoCondicion".NEXTVAL
		  INTO v_IdCondicion
		  FROM DUMMY;

		INSERT INTO "BSCL.Contabilidad.BD::ITZ.ContabilidadTipoAsientoCondicion" (
					"IdCondicion",
					"IdDefinicionAsiento",
					"IdTipoContabilidad",
					"IdTipoDatoEvento",
					"ValorVarchar",
					"ValorDecimal",
					"Condicion",
					"Conector",
					"TipoCondicion",
					"Estado")
			 VALUES (
			 		:v_IdCondicion,
			 		:p_IdDefinicionAsiento,
			 		:p_IdTipoContabilidad,
			 		:p_IdTipoDatoEvento,
			 		:p_ValorVarchar,
			 		:p_ValorDecimal,
			 		:p_Condicion,
			 		:p_Conector,
			 		:p_TipoCondicion,
			 		:p_Estado);

		IF :p_IdDefinicionAsiento > 0 AND :p_IdTipoContabilidad = 0 THEN
			UPDATE "BSCL.Contabilidad.BD::ITZ.ContabilidadDefinicionAsiento"
			   SET "Condicionado" = 'S',
			   	   "OmiteCondicionCmbte" = :p_OmiteCondicionCmbte
			 WHERE "IdDefinicionAsiento" = :p_IdDefinicionAsiento;
		END IF;

		IF :p_IdTipoContabilidad > 0 AND :p_IdDefinicionAsiento = 0 THEN
			UPDATE "BSCL.Contabilidad.BD::ITZ.ContabilidadTipo"
			   SET "Condicionado" = 'S'
			 WHERE "IdTipoContabilidad" = :p_IdTipoContabilidad;
		END IF;
		
		p_IdCondicion := :v_IdCondicion;

	END;
END;