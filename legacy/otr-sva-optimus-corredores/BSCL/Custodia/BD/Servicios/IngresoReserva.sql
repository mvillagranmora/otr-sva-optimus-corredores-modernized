SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.Custodia.BD.Servicios::IngresoReserva";

CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::IngresoReserva" (

	--* ---------------------------------------------------
	--* Proposito ..: Reserva/libera una porción o toda la cartera
	--* Autor ......: Miguel Sandoval
	--* Fecha ......: Junio 2016
	--* ----------------------------------------------------
         
    --* VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa 				BIGINT,
    IN p_IdUsuario 				BIGINT,
    IN p_IdModulo 				NVARCHAR(3),	-- CUS / MES
    IN p_simularOperacion		nvarchar(1),	-- S/N se usa para simular operacion (algunos casos) 
	IN p_UsaReglasNegocio	 	nvarchar(1),	-- SN se usa en caso de servicios anidados.
	IN p_IdPais 				BIGINT,
    
    --* VARIABLES DEL PROCESO OBLIGATORIAS
	IN p_FechaOperacion			DATE,
	--tablas globales IN
	--IN :p_TblCarteraReserva
    IN p_TblCarteraReserva  TABLE( "IdOperacion"			BIGINT,
    									"IdRegistro"			BIGINT,
    									"IdReserva"				BIGINT,
    									"IdConcepto"			NVARCHAR(6),	
    									"FolioOperacion"		BIGINT,
    									"FolioCartera" 			BIGINT, 
    									"FolioCarteraCustodia" 	BIGINT,     									
    									"IdTipoProducto"		NVARCHAR(4),
    									"Cantidad" 				DECIMAL(26,8),
    									"Tasa" 					DECIMAL(26,8),
    									"TasaMercado"           DECIMAL(26,8),
    									"Precio"				DECIMAL(26,8),
    									"PrecioMercado"         DECIMAL(26,8),
    									"ValorRescate"			DECIMAL(26,8),
    									"MontoValorizado"		DECIMAL(26,8),
    									"IdClasificacionTipoInstrumento" NVARCHAR(3),
    									"IdTipoInstrumento"		NVARCHAR(3),
    									"IdInstrumento"			BIGINT,
    									"IdTipoReajuste"		NVARCHAR(3),
    									"IdEmisor"				NVARCHAR(40),
    									"CorteMinimo"			DECIMAL(26,8),
    									"FechaVencimiento"		DATE,
    									"EsFungible"			NVARCHAR(1),
    									"Glosa"					NVARCHAR(260),
    									"IdBolsa"				NVARCHAR(3),
										"IdBoveda"				NVARCHAR(3),	
										"CodigoIFRS"			NVARCHAR(40)
    								 ),
	
    --* SALIDA DEL SP
	--OUT "BSCL.Custodia.BD::PAR.IngresoReserva_OUT"
	/*OUT p_out_IdReserva 	TABLE ( "IdRegistro"		BIGINT,
									"IdReserva"			BIGINT,
									"IdOperacion"		BIGINT
	    						  ),*/

/*
	--* Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador int,
	OUT p_out_AdvertenciasDetalle 	TABLE (	"IdRegistro"	BIGINT,
											"Codigo" 		NVARCHAR(100),
											"RequiereAutorizacion" NVARCHAR(40),
											"Descripcion" 	NVARCHAR(5000)
										  ),	
*/
	OUT p_out_ErroresContador 	INT
	--OUT "BSCL.Custodia.BD::PAR.IngresoReserva_Errores_OUT"
/*	OUT p_out_ErroresDetalle	TABLE (	"IdRegistro"	BIGINT,
										"Codigo" 		NVARCHAR(100),
										"Descripcion" 	NVARCHAR(5000) 
									  )*/
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
AS
BEGIN

	--* ----------------------------------------	
	--* DECLARE Manejo de Errores
	--* ----------------------------------------	
	--* DECLARE SALIDA_CONTROLADA CONDITION; 

	/*
	--> No aplica, ya que siempre procesa todos los registros...	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :p_out_AdvertenciasDetalle;		 					 	
	END;
	*/


	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		DECLARE v_errorExt NVARCHAR(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);
			--p_out_ErroresDetalle = 
			--	SELECT * FROM :p_out_ErroresDetalle UNION ALL  
			INSERT INTO "BSCL.Custodia.BD::PAR.IngresoReserva_Errores_OUT"
				SELECT 0, v_errorExt, ::SQL_ERROR_MESSAGE FROM DUMMY;				
				
			SELECT 	COUNT (1) 
			INTO 	p_out_ErroresContador 
			FROM	"BSCL.Custodia.BD::PAR.IngresoReserva_Errores_OUT";--:p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log sera captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;
	
	

	--* --------------
	--* Proceso
	--* --------------
	BEGIN 
	
		--* ---------------------------------------------------
		--* Declaraciones Variables
		--* ---------------------------------------------------
		DECLARE v_CantidadRegistros			INT;
		DECLARE v_Nro_Registros_Ingreso		INT;
		DECLARE v_Nro_Registros_Modifica	INT;		
		DECLARE v_Nro_Registros_Ret_Reserva	INT;
		DECLARE v_Nro_Registros_Ret_Detalle	INT;	
		
		--* ---------------------------------------------------
		--* Validaciones basicas (Codigos,Existencias,etc)
		--* ---------------------------------------------------
	
		--* Para el retiro de la garantia debe ir sobre la Cartera Reservada.
		--* p_TblValidaRet

		--* Chequear que el concepto que viene en los parametros coincida con los registrado.
		--* SELECT * FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarConceptosCustodia"

		p_out_ErroresContador := 0;

-- * -------------------------------------------------------------------------------
-- * PARA MIRARRRR... LUEGO SACAR
-- * DELETE FROM "BSCL.Custodia.BD::CUS.OperacionLOG";
-- * INSERT INTO "BSCL.Custodia.BD::CUS.OperacionLOG"
-- * SELECT * FROM :p_TblCarteraReserva;
-- * -------------------------------------------------------------------------------

		--* Para el ingreso de la garantia debe ir sobre la Cartera.
		p_TblValidaPrm = 
			SELECT 	Prm."IdOperacion",
					Prm."IdRegistro",
					Prm."IdReserva" AS "IdReservaPrm",
					COALESCE(Rsv."IdReserva", 0) AS "IdReservaRsv",
					Prm."FolioCartera" As "FolioCarteraPrm",					
					COALESCE(Cus."FolioCartera", CusFwd."FolioOperacion", 0) As "FolioCarteraCus",
					Prm."IdTipoProducto",
					Prm."IdConcepto",					
					Prm."Cantidad" As "CantidadReserva",
					COALESCE(Cus."IdUsrReserva", 0) As "IdUsuario",
					COALESCE(Cus."Cantidad", 0) As "CantidadTotalDisponible",
					COALESCE(Cus."CantidadTotalReservada", 0) As "CantidadTotalReservada",
					COALESCE(Cus."CantidadReservadaCus", 0) As "CantidadUsuarioReservada",
					CASE WHEN COALESCE(Cus."Cantidad", -99) = -99 THEN 
						CASE WHEN COALESCE(CusFwd."MontoCalce",-99) = -99 THEN 
							'N'
						ELSE 'S' 
						END
					ELSE 'S' 
					END	 AS "ExisteCartera",
					CASE WHEN COALESCE(Rsv."IdReserva", 0) = 0 THEN 
						CASE WHEN COALESCE(CusFwd."MontoCalce",0) = 0 THEN 
							'N'
						ELSE 'S' 
						END 
					ELSE 'S' 
					END AS "ExisteReserva",
					COALESCE( CASE WHEN Prm."EsFungible" = 'S' THEN Rsv."ValorRescateInicial" ELSE Rsv."CantidadInicial" END, 0) As "CantidadOperacion",					
					Prm."CorteMinimo",
					Prm."ValorRescate",
					Prm."EsFungible",
					Prm."IdClasificacionTipoInstrumento"
					--* Valor de rescate para los papeles fungibles
			FROM 	:p_TblCarteraReserva As Prm 
					LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraReservada" AS Cus 
					ON 	Prm."FolioCartera" = Cus."FolioCartera" AND 
						( ( Prm."IdConcepto" <> 'RETRES' OR Prm."IdTipoProducto" <> 'CUST' ) OR
						  ( Prm."IdConcepto" = 'RETRES' AND Prm."IdTipoProducto" = 'CUST' AND Cus."IdUsrReserva" = p_IdUsuario )
						)
					LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" AS Rsv
					ON Prm."IdReserva" = Rsv."IdReserva" 
					LEFT JOIN "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWD" AS CusFwd
					ON CusFwd."FolioOperacion" = Prm."FolioCartera";

		--* Existe el folio de la cartera???
		v_TblCartera =
			SELECT 	Val."IdRegistro",
					CASE WHEN COALESCE(Val."FolioCarteraCus",0) = 0 THEN 'NO' ELSE 'SI' END AS "ExisteCartera"
			FROM 	:p_TblValidaPrm As Val 
			WHERE	Val."FolioCarteraPrm" > 0;
		
		--* Existe la reserva ??
		v_TblReserva =
			SELECT 	"IdRegistro",
					CASE WHEN COALESCE(Val."IdReservaRsv",0) = 0 THEN 'NO' ELSE 'SI' END AS "ExisteReserva"
			FROM 	:p_TblValidaPrm As Val 
			WHERE 	"IdReservaPrm" > 0;	



        v_TblCarteraSin_Cantidad =
            --* Control de Carteras que tengan suficiente para reservar en el blotter de reserva
            --* Es una nueva reserva?? => La vista ya tiene el total disponible
            SELECT  "IdRegistro",
                    CASE WHEN "CantidadReserva" > "CantidadTotalDisponible"--( "CantidadTotalDisponible" - "CantidadTotalReservada" )
                    THEN 'NO' ELSE 'SI' END AS "CarteraSinDisp"
            FROM    :p_TblValidaPrm 
            WHERE   "IdConcepto" = 'INGRES' AND "IdTipoProducto" = 'CUST' 
            UNION ALL   
            --* En caso que este modificando un registro existente y sea para la mesa
            --* la mesa no trabaja con registros historicos, siempre es 1 detalle == 1 reserva
            SELECT  "IdRegistro",
                    CASE WHEN 
                    	 CASE WHEN "EsFungible" = 'S' AND "IdClasificacionTipoInstrumento" = 'IF' THEN "ValorRescate" ELSE "CantidadReserva" END  > 
                    	 ( "CantidadTotalDisponible" + "CantidadOperacion" ) 
                    THEN 'NO' ELSE 'SI' END AS "CarteraSinDisp"
            FROM    :p_TblValidaPrm
            WHERE   "IdConcepto" = 'INGRES' AND "IdTipoProducto" = 'MERF' AND "FolioCarteraPrm" > 0; 
        /*
        Error....
            --* Control de Carteras que tengan suficiente para reservar en el detalle MRF
            SELECT  "IdRegistro",
                    CASE WHEN "CantidadReserva" > ( "CantidadTotalDisponible" - 
                                                  ( "CantidadTotalReservada" - "CantidadOperacion" ) )
                    THEN 'NO' ELSE 'SI' END AS "CarteraSinDisp"
            FROM    :p_TblValidaPrm
            WHERE   "IdConcepto" = 'INGRES' AND "IdTipoProducto" = 'MERF' AND "FolioCarteraPrm" > 0; 
        */
            



		v_TblCorteMinimo =
			--* Valido que la reserva sea multiplo de corte minimo...
			SELECT	"IdRegistro",
					CASE WHEN "CorteMinimo" > 0 THEN
						 CASE WHEN "EsFungible" = 'N' AND "IdClasificacionTipoInstrumento" = 'IF' AND MOD("ValorRescate" , "CorteMinimo" ) > 0 THEN 'NO'
						 	  WHEN ( "EsFungible" = 'S' OR "IdClasificacionTipoInstrumento" = 'RF' ) AND MOD("CantidadReserva" , "CorteMinimo" ) > 0 THEN 'NO' 
						 	  ELSE 'SI' 
						 END 
						 ELSE 'SI' 
					END AS "CorteMinimoOk"
/*						 CASE WHEN MOD(CASE WHEN "IdTipoProducto" = 'IF' AND "EsFungible"='N' THEN "ValorRescate" ELSE  "CantidadReserva" END , "CorteMinimo" ) > 0 
						 THEN 'NO' ELSE 'SI' END 
					ELSE 'SI' END AS "CorteMinimoOk" 
*/
			FROM	:p_TblValidaPrm;


		--* Retiro de Reserva...
/*		v_TblReservaSin_Cantidad = 
			SELECT	"IdRegistro",
					CASE WHEN "IdUsuario" = p_IdUsuario 
					THEN 	CASE WHEN "CantidadReserva" > "CantidadUsuarioReservada" 
							THEN 'NO' ELSE 'SI' 
							END 
					ELSE	'NO'
					END AS "ReservaOk"		
			FROM	:p_TblValidaPrm	
			WHERE 	"IdConcepto" = 'RETRES' AND "IdTipoProducto" = 'CUST';
*/




		v_TblReservaSin_Cantidad = 
			SELECT	"IdRegistro",
					CASE WHEN "CantidadReserva" > "CantidadUsuarioReservada" 
						 THEN 'NO' 
						 ELSE 'SI' 
					END AS "ReservaOk"		
			FROM	:p_TblValidaPrm	
			WHERE 	"IdConcepto" = 'RETRES' AND "IdTipoProducto" = 'CUST' AND "IdUsuario" = p_IdUsuario;

		--* Resultado de las validaciones
		--* -------------------------------
		v_TblErrores = 
			SELECT	Prm."IdRegistro"
					,CASE 
						WHEN Cus."ExisteCartera" = 'NO' THEN 'ERR' 
						WHEN Rsv."ExisteReserva" = 'NO' THEN 'ERR' 
						WHEN SCn."CarteraSinDisp" = 'NO' THEN 'ERR' 
						WHEN SRn."ReservaOk" = 'NO' THEN 'ERR' 		
						WHEN Crt."CorteMinimoOk" = 'NO' THEN 'ERR'			
						ELSE '' 
					END AS "Codigo"	
					,CASE 
						WHEN Cus."ExisteCartera" = 'NO' THEN 'NO_EXISTE_FOLIO_CARTERA' 
						WHEN Rsv."ExisteReserva" = 'NO' THEN 'NO_EXISTE_RESERVA' 								
						WHEN SCn."CarteraSinDisp" = 'NO' THEN 'RESERVA_SUPERA_CANTIDAD_DISPONIBLE' 	
						WHEN SRn."ReservaOk" = 'NO' THEN 'LIBERACION_SUPERA_CANTIDAD_RESERVADA'
						WHEN Crt."CorteMinimoOk" = 'NO' THEN 'CORTE_MINIMO_INVALIDO'						
						ELSE '' 
					END AS "CodigoDiccionario"
			FROM	:p_TblCarteraReserva AS Prm
					LEFT JOIN :v_TblCartera AS Cus ON Prm."IdRegistro" = Cus."IdRegistro"
					LEFT JOIN :v_TblReserva AS Rsv ON Prm."IdRegistro" = Rsv."IdRegistro"
					LEFT JOIN :v_TblCarteraSin_Cantidad AS SCn ON Prm."IdRegistro" = SCn."IdRegistro"
					LEFT JOIN :v_TblReservaSin_Cantidad AS SRn ON Prm."IdRegistro" = SRn."IdRegistro"					
					LEFT JOIN :v_TblCorteMinimo AS Crt ON Prm."IdRegistro" = Crt."IdRegistro";					


		v_TablaCodigosMensajes = 
			SELECT 	"CodigoDiccionario" AS "CodigoMensaje",
					'' AS "CodigoDato",
					'' AS "ValorDato" 
			FROM 	:v_TblErrores
			WHERE	"Codigo" = 'ERR';

		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" 
			(:v_TablaCodigosMensajes, v_out_ErroresDetalleMensajes);
			
		--p_out_ErroresDetalle = 	
		DELETE FROM "BSCL.Custodia.BD::PAR.IngresoReserva_Errores_OUT";
		INSERT INTO "BSCL.Custodia.BD::PAR.IngresoReserva_Errores_OUT"
			SELECT 	Err."IdRegistro",
					Err."Codigo",
					Msg."Descripcion"
			FROM 	:v_TblErrores Err
					INNER JOIN :v_out_ErroresDetalleMensajes Msg 
					ON Err."CodigoDiccionario" = Msg."Codigo";

		SELECT	COUNT(1) 
		INTO 	p_out_ErroresContador
		FROM	"BSCL.Custodia.BD::PAR.IngresoReserva_Errores_OUT";--:p_out_ErroresDetalle;

		--* -------------------------------------------------
		--* Negocio a partir de aqui
		--* -------------------------------------------------
		SELECT	COUNT(1) INTO v_Nro_Registros_Ingreso
		FROM	:p_TblValidaPrm	WHERE "IdConcepto" = 'INGRES' AND "IdReservaPrm" = 0 ;		

		--* Solo la Mesa envia IDReserva
		SELECT	COUNT(1) INTO v_Nro_Registros_Modifica
		FROM	:p_TblValidaPrm	WHERE "IdConcepto" = 'INGRES' AND "IdReservaPrm" > 0 ;		

		SELECT	COUNT(1) INTO v_Nro_Registros_Ret_Reserva
		FROM	:p_TblValidaPrm	WHERE "IdConcepto" = 'RETRES' AND "IdTipoProducto" = 'CUST';		

		SELECT	COUNT(1) INTO v_Nro_Registros_Ret_Detalle
		FROM	:p_TblValidaPrm	WHERE "IdConcepto" = 'RETRES' AND "IdTipoProducto" = 'MERF';		

		--* Ingreso => Solo agrega registro
		IF v_Nro_Registros_Ingreso > 0 THEN

			--* Ahora el que solo retiro una porcion
			v_TblReservaNueva =					
				SELECT	"BSCL.Custodia.BD.Secuencias::Reserva".NEXTVAL 	AS "IdReserva"
						,Tmp."IdOperacion"		AS "IdOperacion" 
						,Tmp."IdRegistro"		AS "IdRegistro"
						,Tmp."FolioCartera"		AS "FolioCarteraOrigen" 
						,Tmp."FolioCarteraCustodia" AS "FolioCarteraCustodia"
						,Tmp."FolioOperacion"	AS "FolioOperacion"
				--*		,'VIG'					AS "IdEstado" 
				--*		,p_IdUsuario			AS "IdUsuarioCreacion" 
						,Tmp."IdTipoProducto"	AS "IdTipoProducto" 
						,Tmp."IdConcepto"		AS "IdConcepto" 
						----
						,Tmp."Tasa"				AS "Tasa"
						,Tmp."TasaMercado"               AS "TasaMercado"
						,Tmp."Precio"			AS "Precio"
						,Tmp."PrecioMercado"         AS "PrecioMercado"
						,Tmp."MontoValorizado"	AS "MontoValorizado"
						,Tmp."IdClasificacionTipoInstrumento"	AS "IdClasificacionTipoInstrumento"
						,Tmp."IdTipoInstrumento"	AS "IdTipoInstrumento"
						,Tmp."IdInstrumento"	AS "IdInstrumento"
						,Tmp."IdTipoReajuste"	AS "IdTipoReajuste"
						,Tmp."IdEmisor"			AS "IdEmisor"
						,Tmp."CorteMinimo"		AS "CorteMinimo"
						,Tmp."FechaVencimiento"	AS "FechaVencimiento"
						,Tmp."EsFungible"		AS "EsFungible"
						,Tmp."IdBolsa"			AS "IdBolsa"
						,Tmp."IdBoveda"			AS "IdBoveda"
						,Tmp."CodigoIFRS"		AS "CodigoIFRS"
						----
						,Tmp."Cantidad"			AS "Cantidad"
						,Tmp."ValorRescate"		AS "ValorRescate"
				--*		,0						AS "MontoValorizadoIncial" 
				--*		,'0001-01-01'			AS "FechaVencimientoReserva" 
				--*		,'0001-01-01'			AS "FechaVencimientoProducto" 
				--*		,p_FechaOperacion		AS "FechaCreacion" 
				--*		,p_IdUsuario			AS "IngIdUsuario" 
						,"Cantidad"				AS "IngCantidad" 
				--*		,0						AS "IngMontoValorizado" 
				--*		,p_FechaOperacion		AS "IngFecha" 
				--*		,0						AS "EgrIdUsuario" 
				--*		,0						AS "EgrCantidad" 
				--*		,0						AS "EgrMontoValorizado" 
				--*		,'0001-01-01'			AS "EgrFecha";  
				FROM 	:p_TblCarteraReserva Tmp
				WHERE	"IdConcepto" = 'INGRES' AND "IdReserva" = 0 ;		

			INSERT INTO "BSCL.Custodia.BD::CUS.Reserva" (
					"IdReserva"
					,"IdOperacion"		
					,"FolioCarteraOrigen" 
					,"FolioCarteraCustodia"
					,"FolioOperacion"	
					,"IdEstado" 
					,"IdUsuarioCreacion" 
					,"IdTipoProducto" 
					,"IdConcepto" 
					----
					,"Tasa"
					,"TasaMercado"
					,"Precio"
					,"PrecioMercado"
					,"MontoValorizado"
					,"IdClasificacionTipoInstrumento"
					,"IdInstrumento"				
					,"IdTipoInstrumento"
					,"IdTipoReajuste"	
					,"IdEmisor"			
					,"CorteMinimo"
					,"FechaVencimiento"	
					,"EsFungible"
					,"IdBolsa"		
					,"IdBoveda"
					,"CodigoIFRS"
					----
					,"CantidadInicial"
					,"ValorRescateInicial"
					,"MontoValorizadoReserva" 
					,"FechaVencimientoReserva" 
					,"FechaVencimientoProducto" 
					,"FechaCreacion" 
					,"IngIdUsuario" 
					,"IngCantidad" 
					,"IngValorRescate"
					,"IngMontoValorizado" 
					,"IngFecha" 
					,"EgrIdUsuario" 
					,"EgrCantidad" 
					,"EgrValorRescate"
					,"EgrMontoValorizado" 
					,"EgrFecha")
			SELECT 	Tmp."IdReserva"
					,Tmp."IdOperacion"		
					,Tmp."FolioCarteraOrigen" 
					,Tmp."FolioCarteraCustodia"
					,Tmp."FolioOperacion"	
					,'VIG'	AS "IdEstado" 
					,p_IdUsuario AS "IdUsuarioCreacion" 
					,Tmp."IdTipoProducto" 
					,Tmp."IdConcepto" 
					----
					,Tmp."Tasa"
					,Tmp."TasaMercado"
					,Tmp."Precio"
					,Tmp."PrecioMercado"
					,Tmp."MontoValorizado"
					,Tmp."IdClasificacionTipoInstrumento"
					,Tmp."IdInstrumento"				
					,Tmp."IdTipoInstrumento"
					,Tmp."IdTipoReajuste"	
					,Tmp."IdEmisor"		
					,Tmp."CorteMinimo"	
					,Tmp."FechaVencimiento"	
					,Tmp."EsFungible"
					,Tmp."IdBolsa"		
					,Tmp."IdBoveda"
					,Tmp."CodigoIFRS"
					----
					,Tmp."Cantidad" as "CantidadInicial"
					,Tmp."ValorRescate" AS "ValorRescateInicial"
					,0 AS "MontoValorizadoReserva" 
					,'0001-01-01' AS "FechaVencimientoReserva" 
					,'0001-01-01' AS "FechaVencimientoProducto" 
					,p_FechaOperacion AS "FechaCreacion" 
					,p_IdUsuario AS "IngIdUsuario" 
					,Tmp."Cantidad" AS "IngCantidad"
					,Tmp."ValorRescate" AS "IngValorRescate"
					,0 AS "IngMontoValorizado" 
					,p_FechaOperacion AS "IngFecha" 
					,0 AS "EgrIdUsuario" 
					,0 AS "EgrCantidad" 
					,0 AS "EgrValorRescate"
					,0 AS "EgrMontoValorizado" 
					,'0001-01-01' AS "EgrFecha"		
			FROM 	:v_TblReservaNueva Tmp;

			--* Nuevas reservas
			--p_out_IdReserva  =
			INSERT INTO "BSCL.Custodia.BD::PAR.IngresoReserva_OUT"
				SELECT	Tmp."IdRegistro"
						,Tmp."IdReserva"
						,Tmp."IdOperacion"
				FROM	:v_TblReservaNueva Tmp;
			
		END IF; -- IF p_IdConcepto = 'INGRES' OR p_IdConcepto = 'INGDET' THEN



		--* Registro ya Existe!! => Se actualiza
		IF v_Nro_Registros_Modifica > 0 THEN

			--* Estas las esta retirando
			DELETE 	FROM "BSCL.Custodia.BD::CUS.Reserva" AS Rsv
			WHERE	Rsv."IdReserva" IN 
					( 	SELECT 	Prm."IdReserva" 
						FROM 	:p_TblCarteraReserva AS Prm
						WHERE	Prm."IdConcepto" = 'INGRES' AND Prm."IdReserva" > 0 And Prm."Cantidad" = 0 );
						
			--* Estas existen y esta disminuyendo la cantidad en la operacion => No chequeo??. 
			UPDATE	Rsv
			SET		Rsv."IngCantidad" = Prm."Cantidad"
					,Rsv."Tasa" = Prm."Tasa"
					,Rsv."TasaMercado" = Prm."TasaMercado"
					,Rsv."Precio" = Prm."Precio"
					,Rsv."PrecioMercado" = Prm."PrecioMercado"
					,Rsv."MontoValorizado" = Prm."MontoValorizado"
					,Rsv."CantidadInicial" = Prm."Cantidad"
					,Rsv."ValorRescateInicial" = Prm."ValorRescate"
					,Rsv."MontoValorizadoReserva" = Prm."MontoValorizado"
					,Rsv."IngValorRescate" = Prm."ValorRescate"
					,Rsv."IngMontoValorizado" = Prm."MontoValorizado"
			FROM	"BSCL.Custodia.BD::CUS.Reserva" Rsv,
					:p_TblCarteraReserva Prm 
			WHERE	Prm."IdReserva" = Rsv."IdReserva" 
					AND Prm."IdConcepto" = 'INGRES' 
					AND Prm."IdReserva" > 0;
			
			--* A estas se les esta aumentando la cantidad a tomar desde el detalle de la op.
			/*p_out_IdReserva  =
				SELECT	*
				FROM	:p_out_IdReserva
				UNION*/
			INSERT INTO "BSCL.Custodia.BD::PAR.IngresoReserva_OUT"
				SELECT	Tmp."IdRegistro"
						,CASE WHEN Tmp."Cantidad" = 0 THEN 0 ELSE Tmp."IdReserva" END AS "IdReserva"
						,Tmp."IdOperacion"
				FROM	:p_TblCarteraReserva Tmp
				WHERE	Tmp."IdConcepto" = 'INGRES' 
						AND Tmp."IdReserva" > 0;

		END IF;


		--* Retiro => Esta cerrando operacion MRF o eliminando papel del detalle
		IF v_Nro_Registros_Ret_Detalle > 0  THEN			
		
			--* En este proceso no se valida nada, solo elimina detalle.
			DELETE FROM "BSCL.Custodia.BD::CUS.Reserva" AS Rsv
			WHERE	Rsv."IdReserva" IN ( SELECT Prm."IdReserva" FROM :p_TblCarteraReserva AS Prm WHERE	"IdConcepto" = 'RETDET' );

		END IF; -- IF p_IdConcepto = 'RETDET' THEN			


		--* Retiro => Actualizacion de registro y eventual creación de otro...
		IF v_Nro_Registros_Ret_Reserva > 0  THEN	

			--* Creo tabla con carteras del usuario e indicadores de cuanto retirar por cada reserva vigente
			v_TblCarterasUpdate =
				SELECT 	Tmp.*,
						Rsv."IngCantidad" AS "CantidadInicial",
						CASE WHEN (Tmp."Cantidad_Acumulada" <= Tmp."Cantidad_A_Retirar" ) THEN 'S' 
							 WHEN (Tmp."Cantidad_A_Retirar" > COALESCE(Tmp."Cantidad_Acumulada_Anterior",0) AND Tmp."Cantidad_A_Retirar" <= Tmp."Cantidad_Acumulada") THEN 'S'     
							 ELSE 'N'
						END AS "Retira",
						CASE  
							WHEN (Tmp."Cantidad_Acumulada" <= Tmp."Cantidad_A_Retirar") THEN CAST((Rsv."IngCantidad") AS DECIMAL(26,8))
							WHEN (Tmp."Cantidad_A_Retirar" > COALESCE(Tmp."Cantidad_Acumulada_Anterior",0) AND Tmp."Cantidad_A_Retirar" <= Tmp."Cantidad_Acumulada") THEN CAST((Tmp."Cantidad_A_Retirar"-COALESCE(Tmp."Cantidad_Acumulada_Anterior",0)) AS DECIMAL(26,8))
							ELSE 0 
						END AS "TotalRetiro"		
				FROM	"BSCL.Custodia.BD::CUS.Reserva" Rsv
						JOIN 	(	SELECT 	RES."IdReserva"
												,"Cantidad_Acumulada"
												,Tmp2."Cantidad" AS "Cantidad_A_Retirar"
												,LAG("Cantidad_Acumulada") OVER ( PARTITION BY RES."FolioCarteraOrigen"
										        		                       	ORDER BY RES."IdReserva"   
											            		              ) AS "Cantidad_Acumulada_Anterior"
									FROM	"BSCL.Custodia.BD::CUS.Reserva" AS RES
											JOIN :p_TblCarteraReserva AS Tmp2 ON Res."FolioCarteraOrigen" = Tmp2."FolioCartera" AND Tmp2."IdConcepto" = 'RETRES'
											LEFT JOIN (	SELECT 	Rsv1."IdReserva"
														,SUM("IngCantidad") OVER ( PARTITION BY "FolioCarteraOrigen"
										    		                                   ORDER BY Rsv1."IdReserva"   
										            		                         ) AS "Cantidad_Acumulada"
														FROM	"BSCL.Custodia.BD::CUS.Reserva" AS Rsv1
														JOIN	:p_TblCarteraReserva AS Tmp1 
														ON 		Rsv1."FolioCarteraOrigen" = Tmp1."FolioCartera" AND Tmp1."IdConcepto" = 'RETRES'
														WHERE	"IdUsuarioCreacion" = p_IdUsuario 
																AND "IdEstado" = 'VIG'  AND Rsv1."IdTipoProducto" = 'CUST') AS Acum
											ON Acum."IdReserva" = RES."IdReserva"
									WHERE	"IdUsuarioCreacion" = p_IdUsuario 
											AND "IdEstado" = 'VIG' AND RES."IdTipoProducto" = 'CUST'
								) AS Tmp
						ON Tmp."IdReserva" = Rsv."IdReserva"
				WHERE	"IdEstado" = 'VIG';
				

			--* Actualiza los registros (Modificados o Liquidados)
			UPDATE	Reserva
			SET		"IdEstado" = CASE WHEN "TotalRetiro" = Tmp."CantidadInicial" THEN 'LIQ' ELSE 'MOD' END
					,"EgrIdUsuario" = p_IdUsuario
					,"EgrCantidad" = "TotalRetiro"
					,"EgrMontoValorizado" = 0
					,"EgrFecha" = p_FechaOperacion
			FROM	"BSCL.Custodia.BD::CUS.Reserva" AS Reserva
					JOIN :v_TblCarterasUpdate AS Tmp 
					ON Tmp."IdReserva" = Reserva."IdReserva" 
					AND Tmp."Retira" = 'S';		

			SELECT COUNT(1) INTO v_CantidadRegistros 
			FROM (	SELECT	1
					FROM	"BSCL.Custodia.BD::CUS.Reserva" AS Rsv
					JOIN 	:v_TblCarterasUpdate AS Tmp
							ON Tmp."IdReserva" = Rsv."IdReserva" 
							AND Tmp."Retira" = 'S' AND Tmp."CantidadInicial" > "TotalRetiro");


			IF v_CantidadRegistros > 0 THEN
				--* Ahora el que solo retiro una porcion
				v_TblCarteraVigente =					
						SELECT	"BSCL.Custodia.BD.Secuencias::Reserva".NEXTVAL  as "IdReserva"
								,Rsv."IdOperacion"
								,Rsv."FolioCarteraOrigen" 
								,Rsv."FolioCarteraCustodia"
								,Rsv."FolioOperacion"
								,'VIG' AS "IdEstado" 
								,Rsv."IdUsuarioCreacion" 
								,Rsv."IdTipoProducto" 
								,Rsv."IdConcepto" 
								----------------------------------------------
								,Rsv."Tasa"
								,Rsv."TasaMercado"
								,Rsv."Precio"
								,Rsv."PrecioMercado"
								,Rsv."MontoValorizado"
								,Rsv."IdClasificacionTipoInstrumento"
								,Rsv."IdInstrumento"
								,Rsv."IdTipoInstrumento"
								,Rsv."IdTipoReajuste"
								,Rsv."IdEmisor"
								,Rsv."CorteMinimo"
								,Rsv."FechaVencimiento"
								,Rsv."EsFungible"
								,Rsv."IdBolsa"
								,Rsv."IdBoveda"
								,Rsv."CodigoIFRS"
								----------------------------------------------								
								,Rsv."CantidadInicial" 
								,Rsv."ValorRescateInicial" 
								,Rsv."MontoValorizadoReserva" 
								,Rsv."FechaVencimientoReserva" 
								,Rsv."FechaVencimientoProducto" 
								,Rsv."FechaCreacion" 
								,p_IdUsuario AS "IngIdUsuario" 
								,Rsv."IngCantidad" - Tmp."TotalRetiro" AS "IngCantidad"
								,Rsv."IngValorRescate" 
								,Rsv."IngMontoValorizado" 
								,p_FechaOperacion AS "IngFecha" 
								,0 AS "EgrIdUsuario" 
								,0 AS "EgrCantidad" 
								,0 AS "EgrValorRescate"
								,0 AS "EgrMontoValorizado" 
								,'0001-01-01' AS "EgrFecha"
						FROM	"BSCL.Custodia.BD::CUS.Reserva" AS Rsv
						JOIN 	:v_TblCarterasUpdate AS Tmp
								ON Tmp."IdReserva" = Rsv."IdReserva" 
								AND Tmp."Retira" = 'S' AND Tmp."CantidadInicial" > "TotalRetiro";
					
				INSERT INTO "BSCL.Custodia.BD::CUS.Reserva" (
								"IdReserva"
								,"IdOperacion"
								,"FolioCarteraOrigen" 
								,"FolioCarteraCustodia"
								,"FolioOperacion"
								,"IdEstado" 
								,"IdUsuarioCreacion" 
								,"IdTipoProducto" 
								,"IdConcepto" 
								----------------------------------------------
								,"Tasa"
								,"TasaMercado"
								,"Precio"
								,"PrecioMercado"
								,"MontoValorizado"
								,"IdClasificacionTipoInstrumento"
								,"IdInstrumento"
								,"IdTipoInstrumento"
								,"IdTipoReajuste"
								,"IdEmisor"
								,"CorteMinimo"
								,"FechaVencimiento"
								,"EsFungible"
								,"IdBolsa"
								,"IdBoveda"
								,"CodigoIFRS"
								----------------------------------------------								
								,"CantidadInicial" 
								,"ValorRescateInicial" 
								,"MontoValorizadoReserva" 
								,"FechaVencimientoReserva" 
								,"FechaVencimientoProducto" 
								,"FechaCreacion" 
								,"IngIdUsuario" 
								,"IngCantidad"
								,"IngValorRescate" 
								,"IngMontoValorizado" 
								,"IngFecha" 
								,"EgrIdUsuario" 
								,"EgrCantidad" 
								,"EgrValorRescate"
								,"EgrMontoValorizado" 
								,"EgrFecha") 
				SELECT			"IdReserva"
								,"IdOperacion"
								,"FolioCarteraOrigen" 
								,"FolioCarteraCustodia"
								,"FolioOperacion"
								,"IdEstado" 
								,"IdUsuarioCreacion" 
								,"IdTipoProducto" 
								,"IdConcepto" 
								----------------------------------------------
								,"Tasa"
								,"TasaMercado"
								,"Precio"
								,"PrecioMercado"
								,"MontoValorizado"
								,"IdClasificacionTipoInstrumento"
								,"IdInstrumento"
								,"IdTipoInstrumento"
								,"IdTipoReajuste"
								,"IdEmisor"
								,"CorteMinimo"
								,"FechaVencimiento"
								,"EsFungible"
								,"IdBolsa"
								,"IdBoveda"
								,"CodigoIFRS"
								----------------------------------------------								
								,"CantidadInicial" 
								,"ValorRescateInicial" 
								,"MontoValorizadoReserva" 
								,"FechaVencimientoReserva" 
								,"FechaVencimientoProducto" 
								,"FechaCreacion" 
								,"IngIdUsuario" 
								,"IngCantidad"
								,"IngValorRescate" 
								,"IngMontoValorizado" 
								,"IngFecha" 
								,"EgrIdUsuario" 
								,"EgrCantidad" 
								,"EgrValorRescate"
								,"EgrMontoValorizado" 
								,"EgrFecha"
				FROM :v_TblCarteraVigente; 
				
			END IF;	
						
		END IF; --IF p_IdConcepto = 'RETRES' THEN			

		
	END;  
	--* --------------
	--* Proceso
	--* --------------
	
END;