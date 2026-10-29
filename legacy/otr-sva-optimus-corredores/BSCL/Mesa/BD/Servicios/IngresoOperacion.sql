SET SCHEMA "BSCL"; 
--*
DROP PROCEDURE "BSCL.Mesa.BD.Servicios::IngresoOperacion";
--*
CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::IngresoOperacion" (

	--* -----------------------------------------------------------------------
	--* Proposito ..: Ingresa/Mantien la cabecera de una operación
	--* Autor ......: Miguel Sandoval
	--* Fecha ......: Junio 2016
	--* -----------------------------------------------------------------------
	--* -----------------------------------------------------------------------
	--* Proposito ..: Se modifica servicio para Incorporar operaciones forward.
	--* Autor ......: Esteban De la Peña
	--* Fecha ......: Junio 2016
	--* -----------------------------------------------------------------------        

    --* VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa 				BIGINT,
    IN p_IdUsuario 				BIGINT,
    IN p_IdModulo 				NVARCHAR(3),
    IN p_simularOperacion		nvarchar(1),	-- S/N se usa para simular operacion (algunos casos) 
	IN p_UsaReglasNegocio	 	nvarchar(1),	-- S/N se usa en caso de servicios anidados.
	IN p_IdPais 				BIGINT
    
    --* VARIABLES DEL PROCESO OBLIGATORIAS
--	,IN "BSCL.Mesa.BD::PAR.IngresoOperacion_IN"

    --* SALIDA DEL SP
--	,OUT "BSCL.Mesa.BD::PAR.IngresoOperacion_OUT"

	--* Salidas para mensajes de advertencias y errores
	,OUT p_out_ErroresContador 	INT
--	,OUT "BSCL.Mesa.BD::PAR.IngresoOperacion_Errores_OUT"
	
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
AS
BEGIN
-------------------------------------------------------------


	--* ---------------------------------
	--* DECLARE Manejo de Errores
	--* ---------------------------------
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		DECLARE	v_errorExt NVARCHAR(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" 
				(::SQL_ERROR_CODE,v_errorExt);
				INSERT INTO "BSCL.Mesa.BD::PAR.IngresoOperacion_Errores_OUT"
				SELECT 0, v_errorExt, ::SQL_ERROR_MESSAGE FROM DUMMY;				
			SELECT 	COUNT(1) 
			INTO 	p_out_ErroresContador 
			FROM	"BSCL.Mesa.BD::PAR.IngresoOperacion_Errores_OUT";										
		ELSE
			--Error interno no manejado  (El log sera captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;
	
	/*
	p_out_TablaOperacion  = SELECT 	0 	AS	"IdAplicacion"
									,0	AS	"IdOperacion"
									,0	AS 	"FolioOperacion"
									,0	AS	"IdOperacionRelacionada"
									,''	AS	"IdEstado"
							FROM	DUMMY WHERE 1 = 2;*/
	/*
	Estados de p_Accion:
	CREAR		: Creación de Operación 	=> Estado PEN
	MODIFICAR   : Modificación de Operación => Estado PEN
	PEND_X_LIM	: Pendiente por Límite 		=> Estado PEL
	AUT_LIM		: Límite Autorizado			=> Estado VIG
	RECH_LIM	: Límite Rechazado			=> Estado RCH
	*/
	--* --------------
	--* Proceso
	--* --------------
	BEGIN 
	
		--* ---------------------------------------------------
		--* Declaraciones Variables
		--* ---------------------------------------------------
		DECLARE v_CantidadRegistros			INT;
		DECLARE v_Nro_Errores				BIGINT;
		DECLARE	v_FechaOperacionSecondate	SECONDDATE;

		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema" 
			( :p_IdEmpresa, v_FechaOperacionSecondate );		

		DELETE FROM "BSCL.Mesa.BD::PAR.IngresoOperacion_OUT";
		DELETE FROM "BSCL.Mesa.BD::PAR.IngresoOperacion_Errores_OUT";
		--* ---------------------------------------------------
		--* Validaciones basicas (Codigos,Existencias,etc)
		--* ---------------------------------------------------
		v_TblOpModificadas = 
			SELECT 	Prm."IdAplicacion",
					CASE WHEN COALESCE(Ope."IdOperacion",0) = 0 THEN 'NO' ELSE 'SI' END AS "ExisteOpe"
			FROM	"BSCL.Mesa.BD::PAR.IngresoOperacion_IN" Prm
					LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" Ope
					ON	Prm."IdOperacion" = Ope."IdOperacion"
			WHERE	Prm."IdOperacion" > 0;

		v_TblValida = 			
			SELECT 	"IdAplicacion",
					CASE 
						WHEN "Accion" = 'CREAR' AND "IdOperacion" = 0 THEN 'CREAR' 
						WHEN "Accion" = 'CREAR' AND "IdOperacion" > 0 THEN 'ERR_CREA'
						WHEN "Accion" = 'MODIFICAR' AND "IdOperacion" > 0 THEN 'MODIFICAR' 
						WHEN "Accion" = 'MODIFICAR' AND "IdOperacion" = 0 THEN 'ERR_MOD'
						WHEN "Accion" = 'CERRAR' AND "IdOperacion" = 0 THEN 'CREAR'
						ELSE 'ERROR'
					END AS "Accion"
			FROM	"BSCL.Mesa.BD::PAR.IngresoOperacion_IN" Prm;

		--p_out_ErroresDetalle = 
		INSERT INTO "BSCL.Mesa.BD::PAR.IngresoOperacion_Errores_OUT"
			SELECT  Prm."IdAplicacion" AS "IdAplicacion",
					CASE 
						WHEN Ope."ExisteOpe" = 'NO' THEN 'ERR' 
					 	WHEN Val."Accion" = 'ERROR' THEN 'ERR' 
						ELSE '' 
					END AS "Codigo",
					CASE 
						WHEN Ope."ExisteOpe" = 'NO' THEN 'ERR' 
						WHEN Val."Accion" = 'ERROR' THEN 'ERR' 
						ELSE '' 
					END AS "Descripcion"
			FROM	"BSCL.Mesa.BD::PAR.IngresoOperacion_IN" AS Prm
					INNER JOIN :v_TblOpModificadas AS Ope ON Ope."IdAplicacion" = Prm."IdAplicacion" and Ope."ExisteOpe" = 'NO'
					INNER JOIN :v_TblValida AS Val ON Val."IdAplicacion" = Prm."IdAplicacion" and Val."Accion" = 'ERROR';
					
		SELECT	COUNT(1)
		INTO	p_out_ErroresContador
		FROM	"BSCL.Mesa.BD::PAR.IngresoOperacion_Errores_OUT"--:p_out_ErroresDetalle
		WHERE	"Codigo" = 'ERR';



		--* -------------------------------------------------
		--* Validación de Bloqueo
		--* -------------------------------------------------
		v_Tbl_Bloqueo = 
			SELECT	CASE WHEN "Accion" = 'CERRAR' 
						THEN 'L' 
						ELSE 'B'
					 END AS "Accion"
					,"IdOperacion" AS "IdOperacion"
			FROM	"BSCL.Mesa.BD::PAR.IngresoOperacion_IN"
			WHERE	"IdOperacion" > 0;

		SELECT 	COUNT(1) 
		INTO 	v_CantidadRegistros 
		FROM	:v_Tbl_Bloqueo;
		
		IF ( v_CantidadRegistros > 0 ) THEN
			
			CALL "BSCL.Mesa.BD.Servicios::MarcarOperacion"
				 ( 	:p_IdEmpresa,
				 	:p_IdUsuario,
				 	:p_IdModulo,
				 	:v_Tbl_Bloqueo,
				 	v_Res_Bloqueo,
				 	v_Nro_Errores,
				 	v_Tbl_Errores
				 );
		END IF;
					
--select * from :v_Tbl_Bloqueo;						
--select * from :v_Tbl_Errores;

		--* -------------------------------------------------
		--* Negocio a partir de aqui
		--* -------------------------------------------------
		SELECT 	COUNT(1) 
		INTO 	v_CantidadRegistros 
		FROM	:v_TblValida 
		WHERE	"Accion" = 'CREAR';
				
		
		IF v_CantidadRegistros  > 0 THEN
		
			v_TblNuevasOperaciones = 
				SELECT	
					"IdAplicacion"
					, "BSCL.Mesa.BD.Secuencias::Operacion".NEXTVAL AS "IdOperacion"
					, "FechaOperacion"	--"FechaOperacion"
					, CASE WHEN "Accion" = 'CERRAR' 
						THEN "BSCL.Mesa.BD.Secuencias::OperacionMesa".NEXTVAL 
						ELSE "FolioOperacion" 
					  END 											   AS "FolioOperacion"	--"FolioOperacion"
					, "IdTipoProducto"	--"IdTipoProducto"
					, "IdTipoOperacion"	--"IdTipoOperacion"
					, "IdOperacionRelacionada"					
					-- Campos Forward----------------------------
				    , "IdInstrumento"			
				    , "IdTipoCanalIngreso"	
				    , "IdMonedaPrincipal"	
				    , "IdMonedaSecundaria"	
				    , "IdModalidadLiquidacion"
				    , "IdReferenciaMercado"  
				    , "IdPlaza"			
				    ----------------------------------------------
					, "Intermesa"			--"Intermesa"
					, "IdMesaOrigen"			--"IdMesa"
					, "IdMesaDestino"			--"IdMesa"
					, "IdCliente"			--"IdCliente"
					, "IdPortafolio"		--"IdPortafolio"
					, "IdClienteCorredor"	--"IdClienteCorredor"
					, "IdPortafolioCorredor"--"IdPortafolioCorredor"
					-- Campo Forward------------------------------
               		, "MontoOperacion"
               		----------------------------------------------
					, "MontoOperacionInicial"	--"MontoOperacionInicial"
					, "MontoOperacionFinal"	--"MontoOperacionFinal"
					, "IdTipoReajuste"		--"IdTipoReajuste"
					, "IdMoneda"				--"IdMoneda"
					, "Tasa"					--"Tasa"
					--Campos Conversion---------------------------
				    ,"MontoOperacionMoneda" 		
	  			    ,"MontoOperacionInicialMoneda"
				    ,"MontoOperacionFinalMoneda"	
				    ,"IdMonedaLiquidacion"		
					--Campos Forward------------------------------
				   	, "TCCierre"				
				   	, "ParidadCierre"			
				   	, "TCCosto"				
				   	, "PuntosTasasForward"	
				   	, "PuntosForward"			
				   	, "TCForward"				
				   	, "TasasMonedaPrincipal"	
				   	, "TasasMonedaSecundaria"	
				   	, "MontoDiferido"			
				   	, "MontoMargen"						
				   	, "IdFixing"				
				   	, "FechaFixing"			
				   	, "HoraFixing"			
				   	, "IdSpotAsociado"		
			   		, "IdClasificacionCartera"
				    -----------------------------------------------
					, "AfectaCustodia"
					, "PlazoEnDias"			--"PlazoEnDias"
					, "FechaVencimiento"		--"FechaVencimiento"
					, "FechaLiquidacion"		--"FechaLiquidacion"
					, "IdCondicionLiquidacion"	--"IdCondicionLiquidacion"
					, "FechaCondicionLiquidacion"	--"FechaCondicionLiquidacion"
					, "IdAgente"				--"IdAgente"
					, "TasaAgente"
					, CASE WHEN "Accion" = 'CREAR' THEN 
							'PEN'
						ELSE 'VIG' 
					  END 						AS "IdEstado"
					, v_FechaOperacionSecondate	AS "FechaCreacion"
					, "IdUsuarioCreacion"		--"IdUsuarioCreacion"
					, "Glosa"					--"Glosa"
				    -----------------------------------------------
					, "IdEmpresa"
				FROM 	"BSCL.Mesa.BD::PAR.IngresoOperacion_IN"
				WHERE	"IdOperacion" = 0 
				AND 	( "Accion" = 'CREAR' OR "Accion" = 'CERRAR' );


			INSERT 	INTO "BSCL.Mesa.BD::MES.Operacion" 
				(	"IdOperacion"
					, "FechaOperacion"	--"FechaOperacion"
					, "FolioOperacion"	--"FolioOperacion"
					, "IdTipoProducto"	--"IdTipoProducto"
					, "IdTipoOperacion"	--"IdTipoOperacion"
					, "IdOperacionRelacionada"
					-- Campos Forward----------------------------
				    , "IdInstrumento"			
				    , "IdMonedaPrincipal"	
				    , "IdMonedaSecundaria"	
				    , "IdModalidadLiquidacion"
				    , "IdReferenciaMercado"  
				    , "IdPlaza"			
				    ----------------------------------------------
					, "Intermesa"
					, "IdMesaOrigen"
					, "IdMesaDestino"
					, "IdCliente"			--"IdCliente"
					, "IdPortafolio"		--"IdPortafolio"
					, "IdClienteCorredor"	--"IdClienteCorredor"
					, "IdPortafolioCorredor"--"IdPortafolioCorredor"
					-- Campo Forward------------------------------
               		, "MontoOperacion"
               		----------------------------------------------
					, "MontoOperacionInicial"	--"MontoOperacionInicial"
					, "MontoOperacionFinal"	--"MontoOperacionFinal"
					, "IdTipoReajuste"		--"IdTipoReajuste"
					, "IdMoneda"
					, "Tasa"					--"Tasa"
					--Campos Conversion---------------------------
				    , "MontoOperacionMoneda" 		
	  			    , "MontoOperacionInicialMoneda"
				    , "MontoOperacionFinalMoneda"	
				    , "IdMonedaLiquidacion"		

					--Campos Forward------------------------------
				   	, "TCCierre"				
				   	, "ParidadCierre"			
				   	, "TCCosto"				
				   	, "PuntosTasasForward"	
				   	, "PuntosForward"			
				   	, "TCForward"				
				   	, "TasasMonedaPrincipal"	
				   	, "TasasMonedaSecundaria"	
				   	, "MontoDiferido"			
				   	, "MontoMargen"						
				   	, "IdFixing"				
				   	, "FechaFixing"			
				   	, "HoraFixing"			
				   	, "IdSpotAsociado"		
			   		, "IdClasificacionCartera"
				    -----------------------------------------------
					, "AfectaCustodia"
					, "PlazoEnDias"			--"PlazoEnDias"
					, "FechaVencimiento"		--"FechaVencimiento"
					, "FechaLiquidacion"		--"FechaLiquidacion"
					, "IdCondicionLiquidacion"	--"IdCondicionLiquidacion"
					, "FechaCondicionLiquidacion"	--"FechaCondicionLiquidacion"
					, "IdAgente"				--"IdAgente"
					, "TasaAgente"
					, "IdEstado"
					, "FechaCreacion"
					, "IdUsuarioCreacion"		--"IdUsuarioCreacion"
					, "IdTipoCanalIngreso"	
					, "Glosa"
				    -----------------------------------------------
					, "IdEmpresa"					
					, "IdUsuarioEdicion"
					, "FechaEdicion"
				)
			SELECT 	"IdOperacion"
					, "FechaOperacion"	--"FechaOperacion"
					, "FolioOperacion"	--"FolioOperacion"
					, "IdTipoProducto"	--"IdTipoProducto"
					, "IdTipoOperacion"	--"IdTipoOperacion"
					, "IdOperacionRelacionada"					
					-- Campos Forward----------------------------
				    , "IdInstrumento"			
				    , "IdMonedaPrincipal"	
				    , "IdMonedaSecundaria"	
				    , "IdModalidadLiquidacion"
				    , "IdReferenciaMercado"  
				    , "IdPlaza"			
				    ----------------------------------------------
					, "Intermesa"			--"Intermesa"
					, "IdMesaOrigen"			--"IdMesaOrigen"
					, "IdMesaDestino"			--"IdMesaDestino"
					, "IdCliente"			--"IdCliente"
					, "IdPortafolio"		--"IdPortafolio"
					, "IdClienteCorredor"	--"IdClienteCorredor"
					, "IdPortafolioCorredor"--"IdPortafolioCorredor"
					-- Campo Forward------------------------------
               		, "MontoOperacion"
               		----------------------------------------------
					, "MontoOperacionInicial"	--"MontoOperacionInicial"
					, "MontoOperacionFinal"	--"MontoOperacionFinal"
					, "IdTipoReajuste"		--"IdTipoReajuste"
					, "IdMoneda"
					, "Tasa"					--"Tasa"
					--Campos Conversion---------------------------
				    , "MontoOperacionMoneda" 		
	  			    , "MontoOperacionInicialMoneda"
				    , "MontoOperacionFinalMoneda"	
				    , "IdMonedaLiquidacion"		
					--Campos Forward------------------------------
				   	, "TCCierre"				
				   	, "ParidadCierre"			
				   	, "TCCosto"				
				   	, "PuntosTasasForward"	
				   	, "PuntosForward"			
				   	, "TCForward"				
				   	, "TasasMonedaPrincipal"	
				   	, "TasasMonedaSecundaria"	
				   	, "MontoDiferido"			
				   	, "MontoMargen"						
				   	, "IdFixing"				
				   	, "FechaFixing"			
				   	, "HoraFixing"			
				   	, "IdSpotAsociado"		
			   		, "IdClasificacionCartera"
				    -----------------------------------------------
					, "AfectaCustodia"
					, "PlazoEnDias"			--"PlazoEnDias"
					, "FechaVencimiento"		--"FechaVencimiento"
					, "FechaLiquidacion"		--"FechaLiquidacion"
					, "IdCondicionLiquidacion"	--"IdCondicionLiquidacion"
					, "FechaCondicionLiquidacion"	--"FechaCondicionLiquidacion"
					, "IdAgente"				--"IdAgente"
					, "TasaAgente"
					, "IdEstado"
					, "FechaCreacion"
					, "IdUsuarioCreacion"		--"IdUsuarioCreacion"
					, "IdTipoCanalIngreso"	
					, "Glosa"
				    -----------------------------------------------
					, "IdEmpresa"		
					, "IdUsuarioCreacion"
					, v_FechaOperacionSecondate
					
			FROM 	:v_TblNuevasOperaciones;

			--* Nuevas reservas
			--p_out_TablaOperacion = 
				INSERT INTO "BSCL.Mesa.BD::PAR.IngresoOperacion_OUT"
				SELECT	"IdAplicacion" 	AS "IdAplicacion"
						,"IdOperacion"	AS "IdOperacion"
						,"FolioOperacion" AS "FolioOperacion"
						,"IdOperacionRelacionada"	AS "IdOperacionRelacionada"
						,"IdEstado"		AS "IdEstado"
				FROM	:v_TblNuevasOperaciones Tmp;
/*				UNION ALL
				SELECT	*
				FROM	:p_out_TablaOperacion;
	*/		
			
		END IF; -- IF p_Accion = 'CREAR' THEN

		--* Modificacion de la operacion (Debe dejar registro modificado y uno nuevo???)
		SELECT COUNT(1) INTO v_CantidadRegistros 
		FROM (	SELECT	1
				FROM	"BSCL.Mesa.BD::PAR.IngresoOperacion_IN"
				WHERE	"IdOperacion" > 0 
						AND	( "Accion" = 'MODIFICAR' OR "Accion" = 'CERRAR' ) );

		IF v_CantidadRegistros > 0  THEN

			v_TblOperacionesExistentes = 
				SELECT	
					"IdAplicacion"
					, "IdOperacion"
					, "FechaOperacion"	--"FechaOperacion"
					, CASE WHEN "Accion" = 'CERRAR' 
						THEN "BSCL.Mesa.BD.Secuencias::OperacionMesa".NEXTVAL 
						ELSE "FolioOperacion" END AS "FolioOperacion"	--"FolioOperacion"
					, "IdTipoProducto"	--"IdTipoProducto"
					, "IdTipoOperacion"	--"IdTipoOperacion"
					, "IdOperacionRelacionada"					
					-- Campos Forward----------------------------
				    , "IdInstrumento"		
				    , "IdTipoCanalIngreso"	
				    , "IdMonedaPrincipal"	
				    , "IdMonedaSecundaria"	
				    , "IdModalidadLiquidacion"
				    , "IdReferenciaMercado"  
				    , "IdPlaza"			
				    ----------------------------------------------
					, "Intermesa"			--"Intermesa"
					, "IdMesaOrigen"			--"IdMesaOrigen"
					, "IdMesaDestino"			--"IdMesaDestino"
					, "IdCliente"			--"IdCliente"
					, "IdPortafolio"		--"IdPortafolio"
					, "IdClienteCorredor"			--"IdClienteCorredor"
					, "IdPortafolioCorredor"		--"IdPortafolioCorredor"
					-- Campo Forward------------------------------
               		, "MontoOperacion"
               		----------------------------------------------
					, "MontoOperacionInicial"	--"MontoOperacionInicial"
					, "MontoOperacionFinal"	--"MontoOperacionFinal"
					, "IdTipoReajuste"		--"IdTipoReajuste"
					, "IdMoneda"
					, "Tasa"					--"Tasa"
					--Campos Conversion---------------------------
				    , "MontoOperacionMoneda" 		
	  			    , "MontoOperacionInicialMoneda"
				    , "MontoOperacionFinalMoneda"	
				    , "IdMonedaLiquidacion"		
					--Campos Forward------------------------------
				   	, "TCCierre"				
				   	, "ParidadCierre"			
				   	, "TCCosto"				
				   	, "PuntosTasasForward"	
				   	, "PuntosForward"			
				   	, "TCForward"				
				   	, "TasasMonedaPrincipal"	
				   	, "TasasMonedaSecundaria"	
				   	, "MontoDiferido"			
				   	, "MontoMargen"						
				   	, "IdFixing"				
				   	, "FechaFixing"			
				   	, "HoraFixing"			
				   	, "IdSpotAsociado"		
			   		, "IdClasificacionCartera"
				    -----------------------------------------------
					, "AfectaCustodia"
					, "PlazoEnDias"			--"PlazoEnDias"
					, "FechaVencimiento"		--"FechaVencimiento"
					, "FechaLiquidacion"		--"FechaLiquidacion"
					, "IdCondicionLiquidacion"	--"IdCondicionLiquidacion"
					, "FechaCondicionLiquidacion"	--"FechaCondicionLiquidacion"
					, "IdAgente"				--"IdAgente"
					, "TasaAgente"
					, CASE WHEN "Accion" = 'MODIFICAR' THEN 
							'PEN' 
						ELSE 'VIG' END AS "IdEstado"
					, v_FechaOperacionSecondate	AS "FechaCreacion"
					, "IdUsuarioCreacion"		--"IdUsuarioCreacion"
					, "Glosa"					--"Glosa"
				    -----------------------------------------------
					, "IdEmpresa"
				FROM 	"BSCL.Mesa.BD::PAR.IngresoOperacion_IN"
				WHERE	"IdOperacion" > 0 
						AND	( "Accion" = 'MODIFICAR' OR "Accion" = 'CERRAR' );
		
			UPDATE 	Operacion
			SET		"FechaOperacion" = Tmp."FechaOperacion"
					,"FolioOperacion" = Tmp."FolioOperacion"
					,"IdTipoProducto" = Tmp."IdTipoProducto"
					,"IdTipoOperacion" = Tmp."IdTipoOperacion"
					,"IdOperacionRelacionada" = Tmp."IdOperacionRelacionada"					
					-- Campos Forward----------------------------
				    , "IdInstrumento"		    = Tmp."IdInstrumento"			
					, "IdTipoCanalIngreso"	    = Tmp."IdTipoCanalIngreso"	
					, "IdMonedaPrincipal"	    = Tmp."IdMonedaPrincipal"	
					, "IdMonedaSecundaria"	    = Tmp."IdMonedaSecundaria"	
					, "IdModalidadLiquidacion"  = Tmp."IdModalidadLiquidacion"
					, "IdReferenciaMercado"     = Tmp."IdReferenciaMercado"  
					, "IdPlaza"					= Tmp."IdPlaza"
				    ----------------------------------------------
					,"Intermesa" = Tmp."Intermesa"			--"Intermesa"
					,"IdMesaOrigen" = Tmp."IdMesaOrigen"			--"IdMesaOrigen"
					,"IdMesaDestino" = Tmp."IdMesaDestino"			--"IdMesaDestino"
					,"IdCliente" = Tmp."IdCliente"
					,"IdPortafolio" = Tmp."IdPortafolio"
					,"IdClienteCorredor" = Tmp."IdClienteCorredor"
					,"IdPortafolioCorredor" = Tmp."IdPortafolioCorredor"
					-- Campo Forward------------------------------
               		, "MontoOperacion" 			= Tmp."MontoOperacion"
               		----------------------------------------------
					,"MontoOperacionInicial" = Tmp."MontoOperacionInicial"
					,"MontoOperacionFinal" = Tmp."MontoOperacionFinal"
					,"IdTipoReajuste" = Tmp."IdTipoReajuste"
					,"IdMoneda"	= Tmp."IdMoneda"
					,"Tasa" = Tmp."Tasa"
					--Campos Conversion---------------------------
				    ,"MontoOperacionMoneda" 		= Tmp."MontoOperacionMoneda" 		
	  			    ,"MontoOperacionInicialMoneda" 	= Tmp."MontoOperacionInicialMoneda"
				    ,"MontoOperacionFinalMoneda" 	= Tmp."MontoOperacionFinalMoneda"	
				    ,"IdMonedaLiquidacion"			= Tmp."IdMonedaLiquidacion"
					--Campos Forward------------------------------
				   	, "TCCierre"			 	= Tmp."TCCierre"				
					, "ParidadCierre"		 	= Tmp."ParidadCierre"			
					, "TCCosto"				 	= Tmp."TCCosto"				
					, "PuntosTasasForward"	 	= Tmp."PuntosTasasForward"	
					, "PuntosForward"		 	= Tmp."PuntosForward"			
					, "TCForward"			 	= Tmp."TCForward"				
					, "TasasMonedaPrincipal" 	= Tmp."TasasMonedaPrincipal"	
					, "TasasMonedaSecundaria"	= Tmp."TasasMonedaSecundaria"	
					, "MontoDiferido"		 	= Tmp."MontoDiferido"			
					, "MontoMargen"			 	= Tmp."MontoMargen"						
					, "IdFixing"			 	= Tmp."IdFixing"				
					, "FechaFixing"			 	= Tmp."FechaFixing"			
					, "HoraFixing"			 	= Tmp."HoraFixing"			
					, "IdSpotAsociado"		 	= Tmp."IdSpotAsociado"		
					, "IdClasificacionCartera" 	= Tmp."IdClasificacionCartera"
				    -----------------------------------------------
					,"AfectaCustodia" = Tmp."AfectaCustodia"
					,"PlazoEnDias" = Tmp."PlazoEnDias"
					,"FechaVencimiento" = Tmp."FechaVencimiento"
					,"FechaLiquidacion" = Tmp."FechaLiquidacion"
					,"IdCondicionLiquidacion" = Tmp."IdCondicionLiquidacion"
					,"FechaCondicionLiquidacion" = Tmp."FechaCondicionLiquidacion"
					,"IdAgente" = Tmp."IdAgente"
					,"TasaAgente" = Tmp."TasaAgente"					
					,"IdEstado" = Tmp."IdEstado"
					--,"FechaCreacion" = "FechaCreacion"
					--,"IdUsuarioCreacion" = "IdUsuarioCreacion"
					,"Glosa" = Tmp."Glosa"
					,"IdEmpresa" = Tmp."IdEmpresa"
			FROM	"BSCL.Mesa.BD::MES.Operacion" Operacion
					JOIN :v_TblOperacionesExistentes Tmp 
					ON	Tmp."IdOperacion" = Operacion."IdOperacion";


			--p_out_TablaOperacion = 
			INSERT INTO "BSCL.Mesa.BD::PAR.IngresoOperacion_OUT"
				SELECT	"IdAplicacion" 	AS "IdAplicacion"
						,"IdOperacion"	AS "IdOperacion"
						,"FolioOperacion" AS "FolioOperacion"
						,"IdOperacionRelacionada"	AS "IdOperacionRelacionada"
						,"IdEstado"		AS "IdEstado"
				FROM	:v_TblOperacionesExistentes Tmp;
			/*	UNION ALL
				SELECT	*
				FROM	:p_out_TablaOperacion;
				*/	
		END IF;	--IF v_CantidadRegistros > 0  THEN

	END;  

	--* --------------
	--* Resumen 
	--* --------------
	
	
END;


