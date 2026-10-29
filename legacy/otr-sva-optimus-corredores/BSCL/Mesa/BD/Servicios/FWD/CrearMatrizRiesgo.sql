SET SCHEMA "BSCL";   
GO
DROP PROCEDURE "BSCL.Mesa.BD.Servicios::FWD.CrearMatrizRiesgo";
GO
CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::FWD.CrearMatrizRiesgo" (
	--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          		BIGINT,
    IN p_IdUsuario          		BIGINT,
    IN p_IdModulo           		NVARCHAR(3),
    IN p_UsaReglasNegocio			NVARCHAR(1),
	IN p_tablaMatrizRiesgo 		    TABLE(	"IdMatrizRiesgo"		BIGINT,
											"IdTipoProducto"		NVARCHAR(4),
											"IdInstrumento"			BIGINT,
											"IdMonedaPrincipal"		NVARCHAR(3),	
											"IdMonedaSecundaria"	NVARCHAR(3),	
											"IdTipoOperacion"		NVARCHAR(6),	
											"Plazo"					BIGINT,
											"Porcentaje"			DECIMAL(26,8)),
	-- Parametros de Salida
	OUT p_out_AdvertenciasContador int,
	OUT p_out_AdvertenciasDetalle table ("IdInterno" BIGINT,"Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),
	OUT p_out_ErroresContador int,   
	OUT p_out_ErroresDetalle table ("IdInterno" BIGINT,"Codigo" nvarchar(100),"Descripcion" nvarchar(5000))
)   
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	DECLARE v_IdGrupoLog BIGINT;
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(100),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);	        	
	DECLARE v_out_AdvertenciasDetalle  TABLE ("IdInterno" 				BIGINT
											 ,"Codigo" 					NVARCHAR(100)
											 ,"RequiereAutorizacion" 	NVARCHAR(1)
											 ,"ValorParametro" 			NVARCHAR(100)
											 ,"NombreParametro" 		NVARCHAR(40)
									   )DEFAULT SELECT 0  AS "IdInterno"
									                 , '' AS "Codigo"
									                 , '' AS "RequiereAutorizacion"
									                 , '' AS "ValorParametro"
									                 , '' AS "NombreParametro"
									            FROM DUMMY
									            WHERE 1=2;
	DECLARE v_out_ErroresDetalle  	   TABLE ("IdInterno" 		BIGINT
											 ,"Codigo" 			NVARCHAR(100)
											 ,"ValorParametro" 	NVARCHAR(100)
											 ,"NombreParametro" NVARCHAR(40)
									   )
									   DEFAULT SELECT 0  AS "IdInterno"
									                , '' AS "Codigo"
									                , '' AS "ValorParametro"
									                , '' AS "NombreParametro"
									           FROM DUMMY
									           WHERE 1=2;
	
	DECLARE v_count_operaciones				BIGINT;
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador from :v_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador from :v_out_AdvertenciasDetalle;
		
		v_TablaCodigosMensajes = SELECT "Codigo" AS "CodigoMensaje",
								        "NombreParametro" as "CodigoDato",
  								        "ValorParametro" as "ValorDato" 
								 FROM :v_out_ErroresDetalle
								 UNION ALL
								 SELECT "Codigo" AS "CodigoMensaje",
								        "NombreParametro" as "CodigoDato",
								        "ValorParametro" as "ValorDato" 
								 FROM :v_out_AdvertenciasDetalle;
		  
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes,v_out_ErroresDetalleMensajes);
		
		p_out_ErroresDetalle = 	SELECT errores."IdInterno",
									   errores."Codigo",
									   msj."Descripcion"
 								FROM :v_out_ErroresDetalle errores
								INNER JOIN :v_out_ErroresDetalleMensajes msj on errores."Codigo" = msj."Codigo";
								
		p_out_AdvertenciasDetalle = SELECT adv."IdInterno",
									       adv."Codigo",
 									       adv."RequiereAutorizacion",
 									       msj."Descripcion"
 								    FROM :v_out_AdvertenciasDetalle adv
								    INNER JOIN :v_out_ErroresDetalleMensajes msj on adv."Codigo" = msj."Codigo";
				 						 	
	END;
	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	
	v_out_AdvertenciasDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'N' as "RequiereAutorizacion",'' as "ValorParametro",'' as "NombreParametro" from dummy where 1=2;
	v_out_ErroresDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" from dummy where 1=2;
	--------------------
	--Negocio Aqui
	--------------------
	BEGIN
    	
		DECLARE v_resultadoRegla		NVARCHAR(40);
		DECLARE v_cantRepetidos			BIGINT := 0;
		DECLARE v_IdTipoProducto		NVARCHAR(4);
		DECLARE v_IdInstrumento			BIGINT;
		DECLARE v_IdTipoOperacion		NVARCHAR(6);
		DECLARE v_IdMonedaPrincipal 	NVARCHAR(3);
		DECLARE v_IdMonedaSecundaria 	NVARCHAR(3);
		DECLARE v_nroRegistros			INT;
		
		--*******************************
		--*VALIDACION REGLAS BASICAS *
		--*******************************
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa, :p_IdUsuario, :p_IdModulo, :v_out_ErroresDetalle_Basicos, :v_resultadoRegla);
		IF :v_resultadoRegla = 'RECH' THEN
			v_out_ErroresDetalle = SELECT 0 as "IdInterno"
			                            , "Codigo"
			                            , '' as "ValorParametro"
			                            , '' as "NombreParametro" 
			                         FROM :v_out_ErroresDetalle_Basicos;
			SIGNAL SALIDA_CONTROLADA; 	
		END IF;
		
		
		--Validacion Plazo repetido con tabla de entrada a si misma
		v_TablaValidacion =
			SELECT
				p_IdEmpresa AS "IdEmpresa","IdTipoProducto","IdInstrumento","IdMonedaPrincipal","IdMonedaSecundaria","IdTipoOperacion","Plazo"
			FROM :p_tablaMatrizRiesgo;
		--SELECT 'v_TablaValidacion', * FROM :v_TablaValidacion;
		
		v_RegistrosPlazoRepetido =
			SELECT
				"IdEmpresa","IdTipoProducto","IdInstrumento","IdMonedaPrincipal","IdMonedaSecundaria","IdTipoOperacion","Plazo", COUNT(*) AS "Cantidad"
			FROM :v_TablaValidacion
			GROUP BY "IdEmpresa","IdTipoProducto","IdInstrumento","IdMonedaPrincipal","IdMonedaSecundaria","IdTipoOperacion","Plazo"
			HAVING COUNT(*) > 1;
		--SELECT 'v_RegistrosPlazoRepetido', * FROM :v_RegistrosPlazoRepetido;
		
		SELECT COUNT(*) INTO v_cantRepetidos FROM :v_RegistrosPlazoRepetido;
		
		IF (:v_cantRepetidos > 0) THEN
			v_out_ErroresDetalle =
				SELECT 0 as "IdInterno", 'PLAZO_REPETIDO_EN_DATOS_DE_ENTRADA' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		
		--Validacion mismo Plazo mas de una vez para los nuevos registros
		SELECT COUNT(1) INTO v_cantRepetidos
		FROM "BSCL.Mesa.BD::FWD.MatrizRiesgo" tMAT
		INNER JOIN :p_tablaMatrizRiesgo pMAT
			ON (tMAT."IdEmpresa"			= p_IdEmpresa AND
				tMAT."IdTipoProducto"		= pMAT."IdTipoProducto" AND
				tMAT."IdInstrumento"		= pMAT."IdInstrumento" AND
				tMAT."IdMonedaPrincipal"	= pMAT."IdMonedaPrincipal" AND
				tMAT."IdMonedaSecundaria"	= pMAT."IdMonedaSecundaria" AND
				tMAT."IdTipoOperacion"		= pMAT."IdTipoOperacion" AND
				tMAT."Plazo"				= pMAT."Plazo")
		WHERE pMAT."IdMatrizRiesgo" = 0;
		
		IF :v_cantRepetidos > 0 THEN
			v_out_ErroresDetalle =
				SELECT 0 as "IdInterno", 'PLAZO_REPETIDO_NUEVO_REGISTRO' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
				
			SIGNAL SALIDA_CONTROLADA; 	
		END IF;
		
		
		SELECT COUNT(*) INTO v_nroRegistros FROM :p_tablaMAtrizRiesgo;
	
		IF (v_nroRegistros > 0) THEN
			--Se eliminan registros para luego crear todos los vienen de la UI
			SELECT TOP 1 "IdTipoProducto" INTO v_IdTipoProducto FROM :p_tablaMatrizRiesgo;
			SELECT TOP 1 "IdInstrumento" INTO v_IdInstrumento FROM :p_tablaMatrizRiesgo;
			SELECT TOP 1 "IdTipoOperacion" INTO v_IdTipoOperacion FROM :p_tablaMatrizRiesgo;
			SELECT TOP 1 "IdMonedaPrincipal" INTO v_IdMonedaPrincipal FROM :p_tablaMatrizRiesgo;
			SELECT TOP 1 "IdMonedaSecundaria" INTO v_IdMonedaSecundaria FROM :p_tablaMatrizRiesgo;
		
			DELETE FROM "BSCL.Mesa.BD::FWD.MatrizRiesgo"
			WHERE "IdEmpresa" = :p_IdEmpresa
			  AND "IdTipoProducto" = :v_IdTipoProducto
			  AND "IdInstrumento" = :v_IdInstrumento
			  AND "IdTipoOperacion" = :v_IdTipoOperacion
			  AND "IdMonedaPrincipal" = :v_IdMonedaPrincipal
			  AND "IdMonedaSecundaria" = :v_IdMonedaSecundaria;
			
			DELETE FROM "BSCL.Mesa.BD::FWD.MatrizRiesgo"
			WHERE "IdMatrizRiesgo" IN (SELECT "IdMatrizRiesgo" FROM :p_tablaMatrizRiesgo);
	
			
			INSERT INTO "BSCL.Mesa.BD::FWD.MatrizRiesgo"(
				 "IdEmpresa"
				,"IdMatrizRiesgo"
				,"IdTipoProducto"
				,"IdInstrumento"
				,"IdMonedaPrincipal"
				,"IdMonedaSecundaria"
				,"IdTipoOperacion"
				,"Plazo"
				,"Porcentaje"
			)
			SELECT
				 :p_IdEmpresa AS "IdEmpresa"
				,"BSCL.Mesa.BD.Secuencias::FWD.IdMatrizRiesgo".NEXTVAL AS "IdMatrizRiesgo"		
	 			,"IdTipoProducto"
				,"IdInstrumento"			
				,"IdMonedaPrincipal"		
				,"IdMonedaSecundaria"	
				,"IdTipoOperacion"	    
				,"Plazo"					
				,"Porcentaje"
			FROM :p_tablaMatrizRiesgo;
		ELSE
			DELETE FROM "BSCL.Mesa.BD::FWD.MatrizRiesgo";
		END IF;
 		  
		--CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(1,CURRENT_UTCTIMESTAMP,1,'CREAR MATRIZ DE RIESGO','FIN','FIN');	

	END;
END; 
GO