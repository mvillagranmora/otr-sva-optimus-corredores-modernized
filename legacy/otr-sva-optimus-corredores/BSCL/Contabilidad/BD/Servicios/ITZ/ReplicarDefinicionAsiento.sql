SET SCHEMA BSCL;
GO
DROP PROCEDURE "BSCL.Contabilidad.BD.Servicios::ITZ.ReplicarDefinicionAsiento";
GO
CREATE PROCEDURE 
-- 
--  Proposito: Réplica de Registros
--	
--  Empresa: Ingeniería ProNet IT SpA.
--  Autor: Hans Muller :: HMR
--  Fecha Creacion: 16/10/2016
--  Id Servicio = 12026
--  
"BSCL.Contabilidad.BD.Servicios::ITZ.ReplicarDefinicionAsiento" (
	--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          		BIGINT,
    IN p_IdUsuario          		BIGINT,
    IN p_IdModulo           		NVARCHAR(3),
    IN p_UsaReglasNegocio			NVARCHAR(1),
	--Parámetros particulares
	IN p_IdDefinicionAsientoAntiguo	BIGINT,
	IN p_IdCuentaContable			NVARCHAR(12),
	IN p_Ejercicio					BIGINT,
	IN p_DebeHaber					NVARCHAR(1),
	IN p_Formato					NVARCHAR(1),
	-- Parametros de Salida
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
	--DECLARE v_IdGrupoLog BIGINT;
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA CONDITION; 

	DECLARE v_out_AdvertenciasDetalle	TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" NVARCHAR(100),"NombreParametro" NVARCHAR(40))
										DEFAULT SELECT 0 as "IdInterno",'' as "Codigo",'' as "RequiereAutorizacion",'' as "ValorParametro",'' as  "NombreParametro" FROM DUMMY WHERE 1=2;
	DECLARE v_out_ErroresDetalle		TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100),"ValorParametro" NVARCHAR(100),"NombreParametro" NVARCHAR(40))
										DEFAULT SELECT 0 as "IdInterno",'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" FROM DUMMY WHERE 1=2;

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

		DECLARE v_Orden						BIGINT;
		DECLARE v_IdDefinicionAsientoNuevo	BIGINT;

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

		SELECT MAX(DefAsto."Orden") + 1 
		  INTO v_Orden
		  FROM "BSCL.Contabilidad.BD::ITZ.ContabilidadDefinicionAsiento" AS DefAsto
		 WHERE DefAsto."IdTipoContabilidad" = (SELECT TOP 1 CTB_itzContabilidadDefinicionAsiento."IdTipoContabilidad"
												 FROM "BSCL.Contabilidad.BD::ITZ.ContabilidadDefinicionAsiento" AS CTB_itzContabilidadDefinicionAsiento
												WHERE CTB_itzContabilidadDefinicionAsiento."IdDefinicionAsiento" = :p_IdDefinicionAsientoAntiguo)
		   AND DefAsto."Estado" NOT IN ('A','E');

		SELECT "BSCL.Contabilidad.BD.Secuencias::IdDefinicionAsiento".NEXTVAL
		  INTO v_IdDefinicionAsientoNuevo
		  FROM DUMMY;

		INSERT INTO "BSCL.Contabilidad.BD::ITZ.ContabilidadDefinicionAsiento" (
				"IdDefinicionAsiento",
				"IdTipoContabilidad",
				"IdCuentaContable",
				"Ejercicio",
				"Orden",
				"DebeHaber",
				"TipoEvento",
				"Condicionado",
				"OmiteCondicionCmbte",
				"Estado",
				"Formato"
				) 
		SELECT 
				:v_IdDefinicionAsientoNuevo,
				"IdTipoContabilidad",
				CASE :p_IdCuentaContable 
					WHEN '' THEN "IdCuentaContable" 
					ELSE :p_IdCuentaContable 
				END, 
				CASE :p_Ejercicio 
					WHEN 0 THEN "Ejercicio" 
					ELSE :p_Ejercicio 
				END, 
				:v_Orden, 
				CASE :p_DebeHaber 
					WHEN '' THEN "DebeHaber" 
					ELSE :p_DebeHaber 
				END, 
				"TipoEvento", 
				"Condicionado", 
				"OmiteCondicionCmbte", 
				'I',
				CASE :p_Formato
					WHEN '' THEN "Formato"
					ELSE :p_Formato
				END 
					
--					"Formato"
		  FROM  "BSCL.Contabilidad.BD::ITZ.ContabilidadDefinicionAsiento"
		 WHERE  "IdDefinicionAsiento" = :p_IdDefinicionAsientoAntiguo;

		INSERT INTO "BSCL.Contabilidad.BD::ITZ.ContabilidadDefinicionTipoEvento"(
			   "IdTipoEvento", 
			   "IdDefinicionAsiento", 
			   "Operador", 
			   "Estado"
			   ) 
		SELECT 
			   "IdTipoEvento", 
			   :v_IdDefinicionAsientoNuevo, 
			   "Operador", 
			   'I' 
		  FROM "BSCL.Contabilidad.BD::ITZ.ContabilidadDefinicionTipoEvento" 
		 WHERE "IdDefinicionAsiento" = :p_IdDefinicionAsientoAntiguo;

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
			   "Estado"
			   )
		SELECT 
			   "BSCL.Contabilidad.BD.Secuencias::IdTipoAsientoCondicion".NEXTVAL AS "IdCondicion",
			   :v_IdDefinicionAsientoNuevo,
			   0,
			   "IdTipoDatoEvento",
			   "ValorVarchar",
			   "ValorDecimal",
			   "Condicion",
			   "Conector",
			   'A',
			   'I'
		  FROM "BSCL.Contabilidad.BD::ITZ.ContabilidadTipoAsientoCondicion" 
		 WHERE "IdDefinicionAsiento" = :p_IdDefinicionAsientoAntiguo;

	END;
END;
GO