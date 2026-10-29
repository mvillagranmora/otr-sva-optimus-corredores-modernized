SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.Custodia.BD.Servicios::OPE.CambiaEstadoOperacionesPendientes";

CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::OPE.CambiaEstadoOperacionesPendientes"
(
     -- Parametros de Validacion
	IN p_IdEmpresa BIGINT,  
	IN p_IdUsuario BIGINT,   
	IN p_IdModulo  NVARCHAR(4), 
     
	IN 	p_Registros	TABLE ( "IdOperacionPendiente" BIGINT,
							"IdTipoOperacion" NVARCHAR(6),
							"FolioOperacion" BIGINT,
							"IdEstado" NVARCHAR(6) ),
							
	OUT p_out_Data	TABLE ( "IdOperacionPendiente" BIGINT,
							"IdEstado" NVARCHAR(5) ),
    
     --Salidas para mensajes de advertencias y errores
	OUT p_out_ErroresContador 	INT,
	OUT p_out_ErroresDetalle 	TABLE (	
									"IdInterno" NVARCHAR(40), 
									"Codigo" NVARCHAR(203),
									"Descripcion" NVARCHAR(520) )
  )   
    
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 

AS
BEGIN
    --------------------
	--DECLARE Manejo de Errores
	--------------------	  
	DECLARE SALIDA_CONTROLADA 		CONDITION; 
	DECLARE v_out_ErroresDetalle  	TABLE (
											"IdInterno" NVARCHAR(40), 
											"Codigo" NVARCHAR(100),
											"ValorParametro" NVARCHAR(100),
											"NombreParametro" NVARCHAR(40) )
									DEFAULT SELECT 0  AS "IdInterno" 
												, ''  AS "Codigo"
												, ''  AS "ValorParametro" 
												, ''  AS "NombreParametro" 
											FROM dummy 
											WHERE 1=2;

	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT COUNT(1) INTO p_out_ErroresContador FROM :v_out_ErroresDetalle;

		v_TablaCodigosMensajes = 
			SELECT 	"Codigo" AS "CodigoMensaje",
					"NombreParametro" AS "CodigoDato",
					"ValorParametro" AS "ValorDato" 
			FROM 	:v_out_ErroresDetalle;
		
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" 
			(:v_TablaCodigosMensajes, v_out_ErroresDetalleMensajes );
		
		p_out_ErroresDetalle = 	
			SELECT 	Err."IdInterno",
					Err."Codigo"||' - '||"ValorParametro" as "Codigo",
					Msj."Descripcion"
			FROM 	:v_out_ErroresDetalle Err
					INNER JOIN :v_out_ErroresDetalleMensajes Msj on Err."Codigo" = Msj."Codigo";
         
	END;
	
	p_out_ErroresContador	   := 0;
	
	
	BEGIN  --Logica

		DECLARE	v_FechaOperacionSecondate SECONDDATE;
		DECLARE v_NroRegistro BIGINT;
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema" 
			( :p_IdEmpresa, v_FechaOperacionSecondate );		

		-- Si hay registros que necesiten clave y se esta cambiando el estado por Completado => Error
		tbl_SinClaves = 
			SELECT	COALESCE("Clave", '') AS "Clave"
			FROM 	:p_Registros Reg
					INNER JOIN "BSCL.Facturacion.BD.Vistas::ConsultarDetalleMovimientoDeComprobante"	DCOMP
					ON	Reg."FolioOperacion" = DCOMP."IdMovimientoComprobante" 
						AND Reg."IdTipoOperacion" = DCOMP."IdTipoComprobante"
						AND DCOMP."IdEstadoComprobante" <> 'ANU'
						AND DCOMP."IdTipoProducto" IN ('INIF', 'INRF', 'INAC')
					LEFT JOIN "BSCL.Custodia.BD::CUS.OperacionesPendientes" OpPendientes
					ON	OpPendientes."IdOperacionPendiente" = Reg."IdOperacionPendiente"
						AND OpPendientes."IdTipoOperacion" = Reg."IdTipoOperacion" 
						AND OpPendientes."FolioOperacion" = Reg."FolioOperacion";


		SELECT	COUNT(1)
		INTO	p_out_ErroresContador
		FROM	:tbl_SinClaves
		WHERE	RTRIM("Clave") = '';


		/*
		IF	p_out_ErroresContador > 0 THEN
			v_out_ErroresDetalle = 
				SELECT 	0 AS "IdInterno",
						'REGISTRO_SIN_CLAVE' 	AS "Codigo",
						'' AS "ValorParametro",
						'' AS "NombreParametro" 
				FROM 	DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		*/
		
		-- Registros sin clave o que no la necesitan y que se estan marcando como completados.
		tbl_Nuevos = 					
			SELECT	"BSCL.Custodia.BD.Secuencias::IdOperacionPendiente".NEXTVAL AS "IdOperacionPendiente"
					,"IdTipoOperacion"	AS "IdTipoOperacion"
					,"FolioOperacion"	AS "FolioOperacion"
					,''					AS "Clave"
					,'PEN'				AS "IdEstado"
					,p_IdUsuario		AS "IdUsuario"
					,v_FechaOperacionSecondate	AS "FechaEstado"
			FROM	:p_Registros
			WHERE	"IdOperacionPendiente" = 0;

		-- Inserta nuevos registros que este recien procesando (Garantias, Traspasos)
		INSERT	INTO "BSCL.Custodia.BD::CUS.OperacionesPendientes"
				( "IdOperacionPendiente", "IdTipoOperacion", "FolioOperacion", "Clave", "IdEstado",
				  "IdUsuario", "FechaEstado"	)
		SELECT	"IdOperacionPendiente", "IdTipoOperacion", "FolioOperacion", "Clave", "IdEstado",
				"IdUsuario", "FechaEstado"
		FROM	:tbl_Nuevos;
				

		tbl_Registros = 
			SELECT	"IdOperacionPendiente",
					"IdTipoOperacion",
					"FolioOperacion",
					"IdEstado"
			FROM	:p_Registros
			WHERE	"IdOperacionPendiente" > 0 
			-----------------------------------------
			UNION ALL
			-----------------------------------------
			SELECT	Nuevo."IdOperacionPendiente",
					Prm."IdTipoOperacion",
					Prm."FolioOperacion",
					Prm."IdEstado"
			FROM	:tbl_Nuevos Nuevo, :p_Registros Prm
			WHERE	Nuevo."IdTipoOperacion" = Prm."IdTipoOperacion" 
					AND Nuevo."FolioOperacion" = Prm."FolioOperacion";
		

		--------------------------------------------------------------
		-- ACTUALIZA EN LA TABLA DE OPERACIONES EL ESTADO COMO ENVIADO
		--------------------------------------------------------------
		UPDATE	OpPendientes
		SET		OpPendientes."IdEstado" = TMP."IdEstado"
		FROM	"BSCL.Custodia.BD::CUS.OperacionesPendientes" OpPendientes,
				:tbl_Registros TMP
		WHERE	OpPendientes."IdOperacionPendiente" = TMP."IdOperacionPendiente"
				AND OpPendientes."IdTipoOperacion" = TMP."IdTipoOperacion" 
				AND OpPendientes."FolioOperacion" = TMP."FolioOperacion";


		p_out_Data = 
			SELECT 	TMP."IdOperacionPendiente",
			 		COALESCE(OpPendientes."IdEstado", 'ERR') AS "IdEstado"
			FROM	:tbl_Registros TMP,
			 		"BSCL.Custodia.BD::CUS.OperacionesPendientes" OpPendientes
			WHERE	TMP."IdOperacionPendiente" = OpPendientes."IdOperacionPendiente";
		
		
		
    END; --Logica
END;   