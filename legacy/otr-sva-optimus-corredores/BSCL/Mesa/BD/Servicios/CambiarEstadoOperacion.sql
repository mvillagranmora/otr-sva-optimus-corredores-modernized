SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Mesa.BD.Servicios::CambiarEstadoOperacion";

CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::CambiarEstadoOperacion" (

	--* ---------------------------------------------------
	--* Proposito ..: Ingresa/Mantien la cabecera de una operacion
	--* Autor ......: Miguel Sandoval
	--* Fecha ......: Junio 2016
	--* ----------------------------------------------------
    --* VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa 				BIGINT,
    IN p_IdUsuario 				BIGINT,
    IN p_IdModulo 				NVARCHAR(3),	-- CUS / MES
    IN p_simularOperacion		NVARCHAR(1),	-- S/N se usa para simular operacion (algunos casos) 
	IN p_UsaReglasNegocio	 	NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
	IN p_IdPais 				BIGINT
    --* VARIABLES DEL PROCESO OBLIGATORIAS
	--IN "BSCL.Mesa.BD::PAR.CambiarEstadoOperacion_IN"
    --* SALIDA DEL SP
	--OUT "BSCL.Mesa.BD::PAR.CambiarEstadoOperacion_OUT"
	
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA BSCL
AS
BEGIN

	DECLARE v_Cierre_OP BIGINT;
	DECLARE v_Cierre_OP_MRF BIGINT;
	DECLARE v_Anula_OP_MRF BIGINT;	
	DECLARE v_Anula_Anticipo BIGINT;
	DECLARE	v_FechaOperacionSecondate SECONDDATE;

	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema" 
		( :p_IdEmpresa, v_FechaOperacionSecondate );	

	--* Existen operaciones ???
	--* -----------------------
	p_TblEstActual = 
	SELECT	DISTINCT Prm."IdOperacion"
			,COALESCE(Ope."IdEstado", '') AS "IdEstadoActual"
			,CASE WHEN COALESCE(Ope."IdEstado", '') = '' THEN 'NO' ELSE 'SI' END AS "ExisteOpe"
	FROM	"BSCL.Mesa.BD::PAR.CambiarEstadoOperacion_IN" AS Prm
			LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" AS Ope 
			ON 	Prm."IdOperacion" = Ope."IdOperacion"
				AND Prm."IdTipoProducto" = Ope."IdTipoProducto";


	--* Estados nuevos son validos???
	--* -------------------------------
	p_TblEstNuevo =
	SELECT	DISTINCT Prm."IdOperacion"
			,CASE WHEN COALESCE(Pla."IdEstado", '') = '' THEN 'NO' ELSE 'OK' END AS "NuevoEstadoOk"
	FROM	"BSCL.Mesa.BD::PAR.CambiarEstadoOperacion_IN" Prm
			LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarEstadosOperacionMesa" Mes 
			ON 	Mes."IdEstado" = Prm."IdNuevoEstado" 
				AND Mes."NombreEntidad" = 'INGRESO'
				AND Mes."IdTipoProducto" = Prm."IdTipoProducto"
			LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstado" Pla 
			ON 	Pla."IdEstado" = Prm."IdNuevoEstado";


	--* Cambio de estados permitido???
	--* -------------------------------
	p_TblCambio =
	SELECT	DISTINCT Prm."IdOperacion"
			,CASE WHEN COALESCE(Mes."IdEstado", '') = '' THEN 'NO' ELSE 'OK' END AS "CambioEstadoOk"
	FROM	"BSCL.Mesa.BD::PAR.CambiarEstadoOperacion_IN" AS Prm
			LEFT JOIN :p_TblEstActual AS Ope 
			ON 	Prm."IdOperacion" = Ope."IdOperacion"
			LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarEstadosOperacionMesa" AS Mes 
			ON 	Mes."IdEstado" = Ope."IdEstadoActual" 
				AND Mes."IdEstadoSiguiente" = Prm."IdNuevoEstado" 
				AND Mes."IdTipoProducto" = Prm."IdTipoProducto"
				AND Mes."NombreEntidad" = 'CAMBIO_ESTADO';


	--* Resultado de las validaciones
	--* -------------------------------
	p_TblFinal = 
	SELECT	DISTINCT 
			Prm."IdOperacion"
			,Prm."IdNuevoEstado"
			,Ope."IdEstadoActual"
			,Prm."IdTipoProducto"
			,Prm."FolioCarteraCustodia"
			,Prm."MontoOperacionAnticipo"
			,Prm."MontoOperacionInicialAnticipo"
			,Prm."MontoOperacionFinalAnticipo"
			,Prm."TasaAnticipo"
			,Prm."MontoOperacionMonedaAnticipo"
			,Prm."MontoOperacionInicialMonedaAnticipo"
			,Prm."MontoOperacionFinalMonedaAnticipo"
			,Prm."IdMonedaLiquidacionAnticipo"
			,Prm."IdCondicionLiquidacionAnticipo"
			,Prm."FechaCondicionLiquidacionAnticipo"
			,Prm."Glosa"
			,CASE 
				WHEN Ope."ExisteOpe" = 'NO' THEN 'ERR' 
				WHEN Est."NuevoEstadoOk" = 'NO' THEN 'ERR' 				
				WHEN Cam."CambioEstadoOk" = 'NO' THEN 'ERR' 								
				ELSE '' 
			END AS "CodResultado"	
			,CASE 
				WHEN Ope."ExisteOpe" = 'NO' THEN 'OPERACION_NO_EXISTE' 
				WHEN Est."NuevoEstadoOk" = 'NO' THEN 'ESTADO_MESA_NO_VALIDO' 								
				WHEN Cam."CambioEstadoOk" = 'NO' THEN 'CAMBIO_ESTADO_NO_PERMITIDO' 				
				ELSE '' 
			END AS "MsgResultado"
	FROM	"BSCL.Mesa.BD::PAR.CambiarEstadoOperacion_IN" AS Prm
			LEFT JOIN :p_TblEstActual AS Ope ON Prm."IdOperacion" = Ope."IdOperacion"
			LEFT JOIN :p_TblEstNuevo AS Est ON Prm."IdOperacion" = Est."IdOperacion"
			LEFT JOIN :p_TblCambio AS Cam ON Prm."IdOperacion" = Cam."IdOperacion";


	--* -------------------------------------------------------
	--* Actualiza el estado de las operaciones
	--* -------------------------------------------------------
	UPDATE	Operaciones
	SET		Operaciones."IdEstado" = Prm."IdNuevoEstado", Operaciones."Glosa" = COALESCE(Prm."Glosa", '')
	FROM	"BSCL.Mesa.BD::MES.Operacion" AS Operaciones
			INNER JOIN :p_TblFinal AS Prm 
			ON Operaciones."IdOperacion" = Prm."IdOperacion" 
				AND Prm."CodResultado" = ''
				AND Prm."IdTipoProducto" = Operaciones."IdTipoProducto";


	--* Si cierra la operacion => Debe generar Folio
	--* Tambien puede que este dejando Vigente un anticipo (anulando el anticipo) => No considerar
	--* --------------------------------------------------------------------------------------------
	SELECT	COUNT(1)
	INTO	v_Cierre_OP
	FROM	:p_TblFinal
	WHERE	"IdNuevoEstado" = 'VIG' AND "CodResultado" = '' AND "IdEstadoActual" != 'ANT';

	IF v_Cierre_OP > 0 THEN
		--* Genera los folios de operacion
		--* ------------------------------------------------------
		UPDATE	Operacion
		SET		Operacion."FolioOperacion" = "BSCL.Mesa.BD.Secuencias::OperacionMesa".NEXTVAL 
		FROM	"BSCL.Mesa.BD::MES.Operacion" AS Operacion
				JOIN :p_TblFinal AS Prm 
				ON 	Prm."IdOperacion" = Operacion."IdOperacion"
					AND Prm."IdTipoProducto" = Operacion."IdTipoProducto"
					AND Prm."CodResultado" = ''
					AND Prm."IdNuevoEstado" = 'VIG'
					AND Prm."IdEstadoActual" != 'ANT';
	END IF;

			
	--* Para la Mesa RF, si cierra actualiza la reserva
	--* ------------------------------------------------
	SELECT	COUNT(1)
	INTO	v_Cierre_OP_MRF
	FROM	:p_TblFinal
	WHERE	"IdNuevoEstado" = 'VIG' 
			AND "CodResultado" = '' 
			AND "IdTipoProducto" = 'MERF'
			AND "IdEstadoActual" != 'ANT';

	
	IF v_Cierre_OP_MRF > 0 THEN
	
		--* Actualiza las reservas
		--* ------------------------------------------------------
		UPDATE	Reserva
		SET		Reserva."IdEstado" = 'LIQ',
				Reserva."FolioOperacion" = Ope."FolioOperacion",
				Reserva."FolioCarteraCustodia" = IdRsv."FolioCarteraCustodia"
		FROM	"BSCL.Custodia.BD::CUS.Reserva" AS Reserva
				JOIN "BSCL.Mesa.BD::MES.Operacion" AS Ope
				ON	Ope."IdOperacion" = Reserva."IdOperacion"
					AND Ope."IdTipoProducto" = Reserva."IdTipoProducto"
				JOIN :p_TblFinal AS Prm 
				ON 	Prm."IdOperacion" = Reserva."IdOperacion"
					AND Prm."IdTipoProducto" = Reserva."IdTipoProducto"
					AND Prm."CodResultado" = ''
					AND Prm."IdNuevoEstado" = 'VIG'
					AND Prm."IdTipoProducto" = 'MERF'
					AND "IdEstadoActual" != 'ANT'
				JOIN "BSCL.Mesa.BD::PAR.CambiarEstadoOperacion_IN" IdRsv
				ON	Prm."IdOperacion" = IdRsv."IdOperacion"
					AND IdRsv."IdReserva" = Reserva."IdReserva";		

	END IF;


	--* Para la Mesa RF, ANULACION...
	--* ------------------------------------------------
	SELECT	COUNT(1)
	INTO	v_Cierre_OP_MRF
	FROM	:p_TblFinal
	WHERE	"IdNuevoEstado" = 'ANU' AND "CodResultado" = '' AND "IdTipoProducto" = 'MERF';

	IF v_Cierre_OP_MRF > 0 THEN
	
		--* Anula las reservas
		--* ------------------------------------------------------
		UPDATE	Reserva
		SET		Reserva."IdEstado" = 'ANU',
				Reserva."FolioOperacion" = Ope."FolioOperacion"
		FROM	"BSCL.Custodia.BD::CUS.Reserva" AS Reserva
				JOIN "BSCL.Mesa.BD::MES.Operacion" AS Ope
				ON	Ope."IdOperacion" = Reserva."IdOperacion"
					AND Ope."IdTipoProducto" = Reserva."IdTipoProducto"
				JOIN :p_TblFinal AS Prm 
				ON 	Prm."IdOperacion" = Reserva."IdOperacion"
					AND Prm."IdTipoProducto" = Reserva."IdTipoProducto"
					AND Prm."CodResultado" = ''
					AND Prm."IdNuevoEstado" = 'ANU'
					AND Prm."IdTipoProducto" = 'MERF';		

	END IF;
	
	

	--* Anticipo o Vencimiento de la operacion => 
	--* Solo cambia estado.
	--* ------------------------------------------------
	SELECT	COUNT(1)
	INTO	v_Cierre_OP
	FROM	:p_TblFinal
	WHERE	( "IdNuevoEstado" = 'LIQ' OR "IdNuevoEstado" = 'ANT' ) AND "CodResultado" = '';

	IF v_Cierre_OP > 0 THEN
		--* Genera los folios de operacion
		--* ------------------------------------------------------
		UPDATE	Operacion
		SET		 Operacion."FechaLiquidacion" 					= v_FechaOperacionSecondate
				,Operacion."MontoOperacionAnticipo" 			= (CASE WHEN Prm."IdNuevoEstado" = 'ANT' THEN Prm."MontoOperacionAnticipo" ElSE 0 END)
				,Operacion."MontoOperacionInicialAnticipo" 		= (CASE WHEN Prm."IdNuevoEstado" = 'ANT' THEN Prm."MontoOperacionInicialAnticipo" ElSE 0 END)
				,Operacion."MontoOperacionFinalAnticipo" 		= (CASE WHEN Prm."IdNuevoEstado" = 'ANT' THEN Prm."MontoOperacionFinalAnticipo" ElSE 0 END)
				,Operacion."TasaAnticipo" 						= (CASE WHEN Prm."IdNuevoEstado" = 'ANT' THEN Prm."TasaAnticipo" ElSE 0 END)
				,Operacion."MontoOperacionMonedaAnticipo" 		= (CASE WHEN Prm."IdNuevoEstado" = 'ANT' THEN Prm."MontoOperacionMonedaAnticipo" ElSE 0 END)
				,Operacion."MontoOperacionInicialMonedaAnticipo"= (CASE WHEN Prm."IdNuevoEstado" = 'ANT' THEN Prm."MontoOperacionInicialMonedaAnticipo" ElSE 0 END)
				,Operacion."MontoOperacionFinalMonedaAnticipo" 	= (CASE WHEN Prm."IdNuevoEstado" = 'ANT' THEN Prm."MontoOperacionFinalMonedaAnticipo" ElSE 0 END)
				,Operacion."IdMonedaLiquidacionAnticipo" 		= (CASE WHEN Prm."IdNuevoEstado" = 'ANT' THEN Prm."IdMonedaLiquidacionAnticipo" ElSE '' END)
				,Operacion."IdCondicionLiquidacionAnticipo" 	= (CASE WHEN Prm."IdNuevoEstado" = 'ANT' THEN Prm."IdCondicionLiquidacionAnticipo" ElSE '' END)
				,Operacion."FechaCondicionLiquidacionAnticipo" 	= (CASE WHEN Prm."IdNuevoEstado" = 'ANT' THEN Prm."FechaCondicionLiquidacionAnticipo" ElSE '0001-01-01' END)
		FROM	"BSCL.Mesa.BD::MES.Operacion" AS Operacion
				JOIN :p_TblFinal AS Prm 
				ON 	Prm."IdOperacion" = Operacion."IdOperacion"
					AND Prm."IdTipoProducto" = Operacion."IdTipoProducto"
					AND Prm."CodResultado" = ''
					AND ( Prm."IdNuevoEstado" = 'LIQ' OR Prm."IdNuevoEstado" = 'ANT' ) AND "CodResultado" = '';
	END IF;


	--* Esta anulando un anticipo => Vuelve a dejar la Fecha de liquidacion = a la F. de Vcto.
	--* --------------------------------------------------------------------------------------
	SELECT	COUNT(1)
	INTO	v_Anula_Anticipo
	FROM	:p_TblFinal
	WHERE	"IdEstadoActual" = 'ANT' AND "IdNuevoEstado" = 'VIG' AND "CodResultado" = '';

	IF v_Anula_Anticipo > 0 THEN
		--* Dejo las fechade anulacion en Null
		--* ------------------------------------------------------
		UPDATE	Operacion
		SET		 Operacion."FechaLiquidacion" = '0001-01-01'
				,Operacion."MontoOperacionAnticipo" 			= 0
				,Operacion."MontoOperacionInicialAnticipo" 		= 0
				,Operacion."MontoOperacionFinalAnticipo" 		= 0
				,Operacion."TasaAnticipo" 						= 0
				,Operacion."MontoOperacionMonedaAnticipo" 		= 0
				,Operacion."MontoOperacionInicialMonedaAnticipo"= 0
				,Operacion."MontoOperacionFinalMonedaAnticipo" 	= 0
				,Operacion."IdMonedaLiquidacionAnticipo" 		= ''
				,Operacion."IdCondicionLiquidacionAnticipo" 	= ''
				,Operacion."FechaCondicionLiquidacionAnticipo" 	= '0001-01-01'
		FROM	"BSCL.Mesa.BD::MES.Operacion" AS Operacion
				JOIN :p_TblFinal AS Prm 
				ON 	Prm."IdOperacion" = Operacion."IdOperacion"
					AND Prm."IdTipoProducto" = Operacion."IdTipoProducto"
					AND Prm."CodResultado" = ''
					AND "IdEstadoActual" = 'ANT' AND "IdNuevoEstado" = 'VIG' AND "CodResultado" = '';
	END IF;

	
	--p_out_TblResultado = 
	DELETE FROM "BSCL.Mesa.BD::PAR.CambiarEstadoOperacion_OUT";
	INSERT INTO "BSCL.Mesa.BD::PAR.CambiarEstadoOperacion_OUT"
	SELECT  DISTINCT
			Tmp."IdOperacion"
			,COALESCE(Ope."FolioOperacion", 0) AS "FolioOperacion"
			,IdRsv."IdReserva"
			,CASE WHEN Tmp."CodResultado" = '' THEN 'OK' ELSE Tmp."CodResultado" END AS "CodResultado"
			,Tmp."MsgResultado"
	FROM	:p_TblFinal AS Tmp
			LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" AS Ope
			ON 	Tmp."IdOperacion" = Ope."IdOperacion"
				AND Tmp."IdTipoProducto" = Ope."IdTipoProducto"
			JOIN "BSCL.Mesa.BD::PAR.CambiarEstadoOperacion_IN" IdRsv
			ON	Tmp."IdOperacion" = IdRsv."IdOperacion";


END;


