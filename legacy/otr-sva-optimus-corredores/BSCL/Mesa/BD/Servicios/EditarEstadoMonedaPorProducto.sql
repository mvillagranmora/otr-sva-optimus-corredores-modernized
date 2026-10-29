		
SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Mesa.BD.Servicios::EditarEstadoMonedaPorProducto";

CREATE PROCEDURE 
-- 
--  Proposito: 
--  Fecha Creacion: 01/07/2016
--  
"BSCL.Mesa.BD.Servicios::EditarEstadoMonedaPorProducto" ( 
	--Entrada
	--IN p_IdEstado NVARCHAR(3),INNECESARIO
	IN p_IdEstadoNuevo NVARCHAR(3),
	IN p_IdInstrumento BIGINT,
	IN p_IdInstrumentoNuevo BIGINT,
	IN p_IdTipoInstrumentoNuevo NVARCHAR(3),
	IN p_IdTipoProducto NVARCHAR(4),
	IN p_IdTipoProductoNuevo NVARCHAR(4),
	IN p_IdModulo NVARCHAR(3),
	IN p_IdMonedaPrincipal NVARCHAR(3),
	IN p_IdMonedaPrincipalNueva NVARCHAR(3),
    IN p_IdMonedaSecundaria NVARCHAR(3),
	IN p_IdMonedaSecundariaNueva NVARCHAR(3),
	IN p_NemotecnicoNuevo NVARCHAR(6),
    IN p_IdEmpresa BIGINT,
    IN p_IdUsuario BIGINT,
	IN p_FeriadoMonedaPrincipal NVARCHAR(1),
	IN p_MonedaPorProducto TABLE(
								"IdInstrumento" BIGINT,
								"IdEmpresa" BIGINT,
								"Nemotecnico"	NVARCHAR(6), 
								"IdTipoProducto" NVARCHAR(4),
								"IdTipoInstrumento" NVARCHAR(3), 
								"IdMonedaPrincipal"	NVARCHAR(3),
    							"IdMonedaSecundaria"	NVARCHAR(3),
    							"IdEstado" NVARCHAR(3),
								"FeriadoMonedaPrincipal" NVARCHAR(1)
    							),
    
	--Salida
   	 -- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador			INT,
	OUT p_out_AdvertenciasDetalle			TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)),	
	OUT p_out_ErroresContador 				INT,
	OUT p_out_ErroresDetalle 				TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(520))
   ) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN 
	--------------------
	--DECLARE Manejo de Errores
	--------------------
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(100),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);	        	
	DECLARE v_out_AdvertenciasDetalle  	TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40))
											DEFAULT SELECT  0  AS "IdInterno"
													,'' AS "Codigo"
													,'' AS "RequiereAutorizacion"
													,'' AS "ValorParametro"
													,'' AS "NombreParametro"
													FROM DUMMY
													WHERE 1=2;
	DECLARE v_out_ErroresDetalle  		TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40))
											DEFAULT SELECT 0  AS "IdInterno"
														   ,'' AS "Codigo"
														   ,'' AS "ValorParametro"
														   ,'' AS "NombreParametro"
													FROM DUMMY
													WHERE 1=2;
	DECLARE v_count_operaciones				BIGINT;
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :v_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :v_out_AdvertenciasDetalle; 	
		
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
	
    BEGIN

		DECLARE v_resultadoRegla 			NVARCHAR(40);
		
		--*******************************
		--*VALIDACION REGLAS BASICAS *
		--*******************************
    
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
		if :v_resultadoRegla = 'RECH' then
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
			SIGNAL SALIDA_CONTROLADA; 	
		end if;

		--* Verifica que el registro no exista
		--* ----------------------------------
		v_TblErrores = 
			SELECT	TBL."IdInstrumento",
					TBL."FeriadoMonedaPrincipal",
					TBL."Nemotecnico",
					TBL."IdTipoInstrumento",
					TBL."IdMonedaPrincipal",
					TBL."IdMonedaSecundaria"
			FROM	"BSCL.Mesa.BD::MES.InstrumentoMoneda" AS TBL,
					:p_MonedaPorProducto AS TMP
			WHERE	TBL."IdEmpresa" = TMP."IdEmpresa"
					AND TBL."Nemotecnico" = TMP."Nemotecnico"
					AND TBL."IdTipoProducto" = TMP."IdTipoProducto"
					AND TBL."IdTipoInstrumento" = TMP."IdTipoInstrumento"
					AND TBL."IdMonedaPrincipal"	= TMP."IdMonedaPrincipal"	
					AND TBL."IdMonedaSecundaria" = TMP."IdMonedaSecundaria";


		SELECT 	COUNT(1) AS "NroRegistros" 
		INTO	v_count_operaciones
		FROM 	:v_TblErrores;

		IF v_count_operaciones > 0 THEN
			v_out_ErroresDetalle = 	
				SELECT	"IdInstrumento" AS "IdInterno",
						'PRODUCTO_MONEDA_YA_EXISTE' AS "Codigo",
						'' AS "ValorParametro",
						'' AS "NombreParametro" 
				FROM	:v_TblErrores;
			SIGNAL SALIDA_CONTROLADA;
		END IF;

		
	    --************************************************************************
		--*INSERT TABLA "BSCL.Mesa.BD::MES.TipoInstrumentoMoneda"  *
		--************************************************************************
		
		UPDATE "BSCL.Mesa.BD::MES.InstrumentoMoneda"
		SET "IdEstado" = p_IdEstadoNuevo
		,"IdInstrumento" = p_IdInstrumentoNuevo
		,"Nemotecnico" = p_NemotecnicoNuevo
		,"IdTipoProducto" = p_IdTipoProductoNuevo
		,"IdTipoInstrumento" = p_IdTipoInstrumentoNuevo
		,"IdMonedaPrincipal" = p_IdMonedaPrincipalNueva
		,"IdMonedaSecundaria" = p_IdMonedaSecundariaNueva
		,"FeriadoMonedaPrincipal" = p_FeriadoMonedaPrincipal
		WHERE 
		"IdInstrumento" = p_IdInstrumento
		AND "IdTipoProducto" = p_IdTipoProducto
		AND "IdMonedaPrincipal" = p_IdMonedaPrincipal
		AND "IdMonedaSecundaria" = p_IdMonedaSecundaria;

		--*******************************
	END;
END;


