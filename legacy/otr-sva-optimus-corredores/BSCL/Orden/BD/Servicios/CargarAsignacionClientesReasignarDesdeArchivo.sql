SET SCHEMA "BSCL";
GO

DROP PROCEDURE "BSCL.Orden.BD.Servicios::CargarAsignacionClientesReasignarDesdeArchivo";
GO

CREATE PROCEDURE "BSCL.Orden.BD.Servicios::CargarAsignacionClientesReasignarDesdeArchivo"(

	-- Parametros entrada comunes en todos los servicios privados
	IN p_IdEmpresa				BIGINT,			--ID Empresa destino
    IN p_IdUsuario				BIGINT 			default 0,
	IN p_IdModulo				NVARCHAR(3),	--ID del Modulo que lo invoca

	-- Parametros entrada del servicio privado
	IN p_DataArchivo			TABLE (	"CANTIDADSIMULACIONCLIENTE" NVARCHAR(100),
	                                    "CODIGOPORTAFOLIO"          NVARCHAR(100), 
	                                    "DOCUMENTOIDENTIDAD"        NVARCHAR(40),
	                                    "MENSAJEVALIDACION"         NVARCHAR(40),
	                                    "PORCENTAJESIMULACION"      DECIMAL(26,8),
	                                    "TIPOPERSONA"               NVARCHAR(1),
	                                    "TIPODOCUMENTOIDENTIDAD"    NVARCHAR(3)	
	                                  ),  
	                                    

	-- Salidas para mensajes de advertencias y errores
    OUT p_out_DataArchivo                   table ("NombreCompleto" NVARCHAR(100), 
                                                   "DocumentoIdentidad" NVARCHAR(40), 
                                                   "CodigoPortafolio" NVARCHAR(100), 
                                                   "IdMoneda" NVARCHAR(3), 
                                                   "CantidadSimulacionCliente" DECIMAL(26,8),
                                                   "PorcentajeSimulacion" DECIMAL(26,8),
                                                   "DescripcionPortafolio" NVARCHAR(100),
                                                   "IdCliente" BIGINT,
                                                   "IdEstado" NVARCHAR(3),
                                                   "IdEstadoPortafolio" NVARCHAR(3),
                                                   "IdLibroOrdenes" BIGINT,
                                                   "IdPersona" BIGINT,
                                                   "IdPortafolio" BIGINT,
                                                   "Proposito" NVARCHAR(520),
                                                   "RazonSocial" NVARCHAR(100),
                                                   "TipoDocumentoIdentidad" NVARCHAR(3)
                                                   ),
    OUT p_out_AdvertenciasContador  	 	int,
	OUT p_out_AdvertenciasDetalle  	 	 	table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),
	OUT p_out_ErroresContador  	 	 	 	int,
	OUT p_out_ErroresDetalle  	 	 	 	table ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000))
)

LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
AS
BEGIN

	--------------------
	--DECLARE Manejo de Errores
	--------------------
	/*DECLARE SALIDA_CONTROLADA CONDITION;
	DECLARE v_errorcuenta INT DEFAULT 0;
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador
		from :p_out_AdvertenciasDetalle;
	END;

	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt nvarchar(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);
			p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL
								   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;
			select count(1) into p_out_ErroresContador
			from :p_out_ErroresDetalle;
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)
			RESIGNAL;
		END IF;
	END;*/
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;

	BEGIN
		--------------------
		--Variables
		--------------------
		DECLARE v_p_IdMonedaSistema Nvarchar(3) DEFAULT '';
		
		/*DECLARE	v_FechaSistemaCreacion	TIMESTAMP;
		DECLARE	v_countTransacciones	BIGINT;
		DECLARE	v_countOrdenes			BIGINT;
		DECLARE	v_countClientes			BIGINT;
		DECLARE	v_countAsignacion		BIGINT;*/

		--------------------
		-- Validaciones
		-- Si se encuentra algún registro, no se puede cargar el archivo.
		--------------------
/*
		p_OUT_DataArchivo = select * FROM :p_DataArchivo			AS Archivo
			WHERE
				Archivo.MONEDA		<=	0	-- MONEDA NO PUEDE SER INFERIOR O IGUAL A CERO
			OR TRIM(Archivo.NEMO)	= 	''	-- NEMO NO PUEDE VENIR EN BLANCO
			OR Archivo.SORTEADO	<=	0	-- MONEDA NO PUEDE SER INFERIOR O IGUAL A CERO
			OR Archivo.APAGAR		<=	0	-- MONEDA NO PUEDE SER INFERIOR O IGUAL A CERO
			OR Archivo.APAGARPESOS	<=	0;

*/
		/*BEGIN
			SELECT COUNT(1) INTO p_out_ErroresContador
			FROM :p_DataArchivo			AS Archivo
			WHERE
				Archivo.MONEDA		<=	0	-- MONEDA NO PUEDE SER INFERIOR O IGUAL A CERO
			OR TRIM(Archivo.NEMO)	= 	''	-- NEMO NO PUEDE VENIR EN BLANCO
			OR Archivo.SORTEADO		<=	0	-- MONEDA NO PUEDE SER INFERIOR O IGUAL A CERO
			OR Archivo.APAGAR		<=	0	-- MONEDA NO PUEDE SER INFERIOR O IGUAL A CERO
			OR Archivo.APAGARPESOS	<=	0;	-- MONEDA NO PUEDE SER INFERIOR O IGUAL A CERO

			IF(p_out_ErroresContador > 0) THEN
				p_out_ErroresDetalle = select 'ARCHIVO_SORTEO_LETRAS_CON_ERRORES_REVISAR' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA;
			END IF;

		END;*/


		--CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema" (:p_IdEmpresa,v_FechaSistemaCreacion);

		--------------------------------------------------
		--Insert de Sorteos de letras en evento de capital
		--------------------------------------------------
		--Obtiene Moneda de Sistema
	    Call "BSCL.Plataforma.BD.Utilitarios::ObtenerConfiguracion" (:p_IdEmpresa, 'PLA.MonedaSistema', v_p_IdMonedaSistema);

		p_out_DataArchivo = SELECT ConsCli."NombreCompleto" AS "NombreCompleto", ConsCli."DocumentoIdentidad" AS "DocumentoIdentidad", Coalesce(Port."CodigoPortafolio", '')  AS "CodigoPortafolio", 
		                            v_p_IdMonedaSistema AS "IdMoneda", Archivo."CANTIDADSIMULACIONCLIENTE" AS "CantidadSimulacionCliente", Archivo."PORCENTAJESIMULACION" AS "PorcentajeSimulacion", 
		                            Coalesce(Port."DescripcionPortafolio", '') AS "DescripcionPortafolio", ConsCli."IdCliente" AS "IdCliente", 'ACT' AS "IdEstado", 'ACT' AS "IdEstadoPortafolio",  
		                            Coalesce("ValorIndicador", '') AS "IdLibroOrdenes", ConsCli."IdPersona" AS "IdPersona", Coalesce(Port."IdPortafolio", 0) AS "IdPortafolio", Coalesce(Port."Proposito", '') AS "Proposito", 
		                            ConsCli."RazonSocial" AS "RazonSocial", ConsCli."TipoDocumentoIdentidad" AS "TipoDocumentoIdentidad"
		                    FROM :p_DataArchivo AS Archivo
		                      INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" ConsCli
		                         ON ConsCli."DocumentoIdentidad" = Archivo."DOCUMENTOIDENTIDAD"
		                        AND ConsCli."TipoDocumentoIdentidad" =  Archivo."TIPODOCUMENTOIDENTIDAD"
		                        AND ConsCli."Tipo" = Archivo."TIPOPERSONA"
		                      LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Port 
		                         ON Port."IdCliente" = ConsCli."IdCliente"
		                      LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS ConsIndPor
		                         ON ConsIndPor."IdPortafolio" = Port."IdPortafolio"
		                        AND ConsIndPor."CodigoTipoIndicador" = 'ORDLIBRO';
		         
				
		--------------------
		--Reglas Negocio 
		--------------------		
		
		--------------------
		--Lógica de Negocio 
		--------------------

		--------------------
		--FIN SP 
		--------------------
	END;
END;
GO