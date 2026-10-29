SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::NOR.REPO2P2.GrabarDetalle";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::NOR.REPO2P2.GrabarDetalle"(
    ------------ Parametros de Entrada Comunes ----------

    -- ID empresa del login.
    IN p_IdEmpresa 								BIGINT		 ,
    -- ID usuario del login.
    IN p_IdUsuario 								BIGINT		 ,
    -- ID modulo invocador.
    IN p_IdModulo 								NVARCHAR(3)	 ,
    ------------ Parametros de Entrada Propios ----------
     -- ID del detalle.
    -- Si el id es mayor que cero, se asume que es un registro existente para actualizar.
    -- En caso contrario, se intenta crear un nuevo registro.
    IN p_ID                                     BIGINT   ,
    -- ID del documento.
    IN p_IdDocumento                            BIGINT   ,
    -- VoBo del usuario sobre el registro (S/N).
    IN p_Estado                             	CHAR(3)   ,
    -- Observacion ingresada por usuario que edito el registro.
    IN p_EstadoSICAM                          	NVARCHAR(3),
    
    IN p_SICAM                          		NVARCHAR(40),
    -- Fuente de los datos.
    IN p_FuenteDatos                            NVARCHAR(40),


    --camos layout    
	IN p_TipoRegistro									NVARCHAR(2),
	IN p_RUTEntidadMCF									NVARCHAR(11),
	IN p_Fecha											DATE,
	IN p_SaldoCuentaCambioConversion					DECIMAL(26,8),
	IN p_SaldoMonedaOriginal							DECIMAL(26,8),
	IN p_MonedaSaldo									NVARCHAR(3),
	IN p_Paridad										DECIMAL(26,8),
	
	
	
--campos de control interno especifico
	IN p_IdCuentaInversion								BIGINT,
	IN p_IdCliente										BIGINT,
	IN p_IdCargoAbonoMovimiento							BIGINT,
	IN p_IdTipoMovimiento								NVARCHAR(6),
	IN p_FechaEfectivaMovimiento						DATE,
			
    ------------ Parametros de Salida Propios ----------
    -- ID del registro.
    OUT p_out_id                                BIGINT,

    ------------ Salidas Advertencias ----------
    -- Contador de advertencias.
    OUT p_out_AdvertenciasContador              INT,

    -- Tabla de advertencias.
    OUT p_out_AdvertenciasDetalle TABLE("Codigo" NVARCHAR(100), "RequiereAutorizacion" NVARCHAR(40), "Descripcion" NVARCHAR(5000)),

    ------------ Salidas Errores ----------
    -- Contador de errores
    -- Importante: Debe ser cero para que se haga commit.
    OUT p_out_ErroresContador                   INT,

    -- Tabla de errores.
    OUT p_out_ErroresDetalle TABLE("Codigo" NVARCHAR(100), "Descripcion" NVARCHAR(5000))
)
    LANGUAGE sqlscript
    SQL security invoker
    DEFAULT SCHEMA "BSCL"
AS BEGIN
    ------------ Variables Propias ----------

    ------------ Variables autorizacion:
	DECLARE v_tipoEntidad						NVARCHAR(40);
	DECLARE v_IdConcepto	       				NVARCHAR(6);						
	DECLARE v_countEmpleado				 		BIGINT;
	DECLARE v_estadoAutorizacion				NVARCHAR(3);
	DECLARE v_RequiereAutorizacion				NVARCHAR(1);
	DECLARE v_secuencia_IdSolicitudCambio		BIGINT;
	DECLARE v_countCodigoEmpleado 				BIGINT:=0;
	DECLARE v_CodigoEmpleado 					NVARCHAR(40);
	DECLARE v_count_CodigoEmpleadoExiste		BIGINT:=0;
	DECLARE v_out_IdSecuencias 					table ("IdSecuencia" BIGINT);
	DECLARE p_out_RefId 						BIGINT;
	DECLARE v_fechaHoraSistema 					NVARCHAR(40);
	DECLARE v_erroroutmsg  						nvarchar(520);
	DECLARE v_RUTEntidadMCF						NVARCHAR(11);
	DECLARE	v_Version							NVARCHAR(10);
	DECLARE v_Fecha								DATE;
	DECLARE v_ContadorEdicion					BIGINT:=0;
	DECLARE v_ContadorOperacionMoneda			BIGINT:=0;
	
    ------------ Bloque Comun de Inicializacion ----------
    DECLARE salida_controlada condition;
    DECLARE exit handler FOR salida_controlada
    BEGIN
        SELECT COUNT(1) INTO p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
        SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
    END;

    DECLARE exit handler FOR sqlexception
    BEGIN
        DECLARE v_errorExt NVARCHAR(40);
        IF ::sql_error_code > 10000 THEN
             -- Error pseudo-controlado pero desde un sub-sp.
            CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError"(::sql_error_code, v_errorExt);
            p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL
                                   SELECT v_errorExt, ::sql_error_message FROM dummy;
            SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
        ELSE
            -- Error interno no manejado (el log sera captado por la capa de XS control).
            RESIGNAL;
        END IF;
    END;

    p_out_AdvertenciasContador := 0;
    p_out_ErroresContador := 0;
    p_out_AdvertenciasDetalle = select '' as "Codigo", '' as "RequiereAutorizacion", '' as "Descripcion" from dummy where 1 = 2;
    p_out_ErroresDetalle = select '' as "Codigo", '' as "Descripcion" from dummy where 1 = 2;
    
    --Inicializacion de variables autorizacion:
	v_secuencia_IdSolicitudCambio 	:= -1;
	v_estadoAutorizacion 			:= 'PEN';
	v_tipoEntidad					:= 'ID';
	v_IdConcepto					:= 'R2P2MD';			
	p_out_RefId						:=0;
	v_Version						:= '01';
	v_ContadorEdicion				:= 0;
	
	call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
	p_IdEmpresa => p_IdEmpresa,
	p_out_FechaHoraSistema => v_fechaHoraSistema
	);

    ----------- VALIDACIONES ---------------
	

	IF p_Paridad <= CAST(0 AS DECIMAL(26,8)) THEN
        p_out_AdvertenciasContador := p_out_AdvertenciasContador + 1;
            v_hash = SELECT '' AS "Codigo", '' AS "Valor" FROM DUMMY;
            CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('UI_PARIDAD_MAYOR_A_CERO_REPO2P2', :v_hash, :v_erroroutmsg);
            p_out_AdvertenciasDetalle =
                SELECT 'UI_PARIDAD_MAYOR_A_CERO_REPO2P2' AS "Codigo",
                    'N' AS "RequiereAutorizacion",
                    CAST('VALIDACION->' || :v_erroroutmsg AS NVARCHAR(520)) AS "Descripcion"
                FROM dummy
                UNION ALL
                SELECT * FROM :p_out_AdvertenciasDetalle;

            SIGNAL salida_controlada;
    END IF;

	--------- FIN VALIDACIONES -----------

    ------------ Logica de Actualizacion ----------
    IF p_ID > 0 THEN
    
    
    SELECT COUNT(1) INTO v_ContadorEdicion
	      	FROM "BSCL.Normativa.BD::DOC.Documento" DOC 
			INNER JOIN "BSCL.Normativa.BD::DOC.Campo" ESQ ON (ESQ."Esquema"=DOC."Esquema" AND ESQ."Version"=DOC."Version")
			WHERE ESQ."Editable"='S' AND DOC."IdDocumento"=p_IdDocumento;
	 if v_ContadorEdicion = 0 then
	
				--Mensaje de Salida
				p_out_AdvertenciasContador := p_out_AdvertenciasContador + 1;	
				v_hash= select '' as "Codigo",'' as "Valor" FROM DUMMY;
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('UI_LA_OPCION_NO_ESTA_HABILITADA',:v_hash,:v_erroroutmsg);
				p_out_AdvertenciasDetalle =  select 'UI_LA_OPCION_NO_ESTA_HABILITADA' as "Codigo",'N' as "RequiereAutorizacion",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
										     select * from :p_out_AdvertenciasDetalle;
				SIGNAL salida_controlada;
	  end if;		
    
        p_out_id := p_ID;

        	SELECT "BSCL.Plataforma.BD.Secuencias::PLA.IdSolicitudCambio".NEXTVAL into v_secuencia_IdSolicitudCambio from dummy;
			
			    V_RegistroSolicitudCambio =SELECT	
					v_secuencia_IdSolicitudCambio AS "IdSolicitudCambio",	
					0 AS		"IdSecuenciaCambio", --- Se crea interno
					v_tipoEntidad	AS "IdTipoEntidad",	
					CAST (p_ID AS NVARCHAR(40))		AS "IdEntidadOrigen",
					MOD."COLUMN_NAME" as "CampoModificado",
					MOD."DATA_TYPE_NAME" AS "TipoDato",
					(CASE MOD."COLUMN_NAME" WHEN 'TipoRegistro'					THEN IFNULL(CAST (DJ."TipoRegistro" AS NVARCHAR(255)),'')
											WHEN 'RUTEntidadMCF'				THEN IFNULL(CAST (DJ."RUTEntidadMCF" AS NVARCHAR(255)),'')
											WHEN 'Fecha'						THEN IFNULL(CAST (DJ."Fecha" AS NVARCHAR(255)),'')
											WHEN 'SaldoCuentaCambioConversion'	THEN IFNULL(CAST (DJ."SaldoCuentaCambioConversion" AS NVARCHAR(255)),'')
											WHEN 'SaldoMonedaOriginal'			THEN IFNULL(CAST (DJ."SaldoMonedaOriginal" AS NVARCHAR(255)),'')
											WHEN 'MonedaSaldo'					THEN IFNULL(CAST (DJ."MonedaSaldo" AS NVARCHAR(255)),'')
											WHEN 'Paridad'						THEN IFNULL(CAST (DJ."Paridad" AS NVARCHAR(255)),'')
											
					END) AS "ValorAntiguo",
					(CASE MOD."COLUMN_NAME" WHEN 'TipoRegistro'					THEN IFNULL(CAST (p_TipoRegistro AS NVARCHAR(255)),'')
											WHEN 'RUTEntidadMCF'				THEN IFNULL(CAST (p_RUTEntidadMCF AS NVARCHAR(255)),'')
											WHEN 'Fecha'						THEN IFNULL(CAST (p_Fecha AS NVARCHAR(255)),'')
											WHEN 'SaldoCuentaCambioConversion'	THEN IFNULL(CAST (p_SaldoCuentaCambioConversion AS NVARCHAR(255)),'')
											WHEN 'SaldoMonedaOriginal'			THEN IFNULL(CAST (p_SaldoMonedaOriginal AS NVARCHAR(255)),'')
											WHEN 'MonedaSaldo'					THEN IFNULL(CAST (p_MonedaSaldo AS NVARCHAR(255)),'')
											WHEN 'Paridad'						THEN IFNULL(CAST (p_Paridad AS NVARCHAR(255)),'')
											
					END) AS "ValorNuevo",	
					CURRENT_UTCTIMESTAMP AS "FechaSolicitud",
					CURRENT_UTCTIMESTAMP AS "FechaRevision",
					'PEN' AS "IdEstadoAutorizacion"
				FROM "BSCL.Normativa.BD::NOR.DatosREPO2P2" DJ
				INNER JOIN 	BSCL."BSCL.Normativa.BD::DOC.Documento" DOC ON (DOC."IdDocumento"= DJ."IdDocumento" AND DOC."Esquema"='REPO2P2')
				INNER JOIN  BSCL."BSCL.Normativa.BD::DOC.Campo" 	CAM ON (CAM."Esquema"=DOC."Esquema" AND CAM."Version"=DOC."Version" AND CAM."Editable"='S')
				INNER JOIN 	(SELECT COL."DATA_TYPE_NAME",
										COL."COLUMN_NAME"
								FROM SYS.TABLE_COLUMNS COL
								WHERE COL."TABLE_NAME"='BSCL.Normativa.BD::NOR.DatosREPO2P2' AND COL.SCHEMA_NAME='BSCL'
							) MOD ON (MOD."COLUMN_NAME"=CAM."Nombre")
				WHERE DJ."ID"=	:p_ID;
	
			-- 1) DETERMINA SI LA MODIFICACION  REQUIERE DE UNA AUTORIZACION PREVIA
							
			------------------------------------------
			--Pregunta Si requiere autorizacion
			------------------------------------------
			call "BSCL.Plataforma.BD.Servicios::AUT.RequiereEventoAutorizacion" (
				:p_IdEmpresa,
				:p_IdUsuario,
				:p_IdModulo,
				:V_RegistroSolicitudCambio,
				:v_IdConcepto,
				:v_RequiereAutorizacion
			);
			
        
        
      	if v_RequiereAutorizacion='S' THEN
	
	


		------------------------------------------
		--Genera Evento de Autorizacion
		------------------------------------------
			DECLARE	v_Glosa				NVARCHAR(520);
			declare v_out_idAuth 		bigint;
			declare v_out_AuthAdvertenciasContador 	int;
			declare v_out_AuthErroresContador 		int;
			v_out_AuthAdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
			v_out_AuthErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
			
			SELECT top 1 "Version" INTO v_Version
			FROM "BSCL.Normativa.BD::DOC.Documento"
			WHERE "Esquema"='REPO2P2' AND "IdDocumento"=:p_IdDocumento;
			
			SELECT  'SICAM REPORTE 2.2 / VERSION : ' || v_Version into  v_Glosa 	FROM DUMMY;
			CALL  "BSCL.Plataforma.BD.Servicios::PLA.CrearRegistroSolicitudCambio"
			(
				:V_RegistroSolicitudCambio,
				v_out_IdSecuencias
				
			);
				
			CALL "BSCL.Plataforma.BD.Servicios::AUT.CrearEventoAutorizacion" (
				p_IdEmpresa 				=> p_IdEmpresa,
				p_IdUsuario 				=> p_IdUsuario,
				p_IdModulo 					=> p_IdModulo,
				p_UsaReglasNegocio 			=> 'N',								
				p_IdTipoEvento 				=> v_IdConcepto,
				p_IdModuloSolicita 			=> p_IdModulo,
				p_IdUsuarioSolicita 		=> p_IdUsuario,
				p_Fecha 					=> TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD'), 
				p_IdConcepto 				=> v_IdConcepto,
				p_NumeroOperacionConcepto 	=> CAST (v_secuencia_IdSolicitudCambio AS NVARCHAR(40)), --v_secuencia_IdSolicitudCambio,
				p_FechaOperacionConcepto 	=> TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD'),
				p_IdFuncionario             => 0,
				p_IdCliente 				=> 0, 
				p_Glosa 					=> v_Glosa,
				--salida
				p_out_IdEvento 				=> v_out_idAuth,
				p_out_AdvertenciasContador 	=> v_out_AuthAdvertenciasContador,
				p_out_AdvertenciasDetalle 	=> v_out_AuthAdvertenciasDetalle,
				p_out_ErroresContador 		=> v_out_AuthErroresContador,
				p_out_ErroresDetalle 		=> v_out_AuthErroresDetalle);				

			
			 p_out_AdvertenciasContador := p_out_AdvertenciasContador + v_out_AuthAdvertenciasContador;	
  			 
			 p_out_AdvertenciasDetalle = Select "Codigo","RequiereAutorizacion", "Descripcion" 
                                          From :v_out_AuthAdvertenciasDetalle Union All 
                                          Select "Codigo","RequiereAutorizacion", "Descripcion"
                                          From :p_out_AdvertenciasDetalle;  
                                          
                                          
              p_out_ErroresContador := p_out_ErroresContador + v_out_AuthErroresContador;
                                         
              p_out_ErroresDetalle = Select "Codigo", "Descripcion" 
                                     From :v_out_AuthErroresDetalle Union All 
                                     Select "Codigo", "Descripcion" 
                                     From :p_out_ErroresDetalle; 

						                 
                                     
			  if 	p_out_ErroresContador > 0 then
						SIGNAL salida_controlada;
						
					else
						--Mensaje de Salida
						p_out_AdvertenciasContador := p_out_AdvertenciasContador + 1;	
						v_hash= select '' as "Codigo",'' as "Valor" FROM DUMMY;
						CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('REQUIERE_AUTORIZACION',:v_hash,:v_erroroutmsg);
						p_out_AdvertenciasDetalle =  select 'REQUIERE_AUTORIZACION' as "Codigo",'S' as "RequiereAutorizacion",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
												     select * from :p_out_AdvertenciasDetalle;
						SIGNAL salida_controlada;
			  end if;								
			
				
                                         
				
		ELSE
	        UPDATE "BSCL.Normativa.BD::NOR.DatosREPO2P2" 
			SET
	            "IdDocumento"								= p_IdDocumento
	            ,"Estado"									= p_Estado
				,"EstadoSICAM"								= p_EstadoSICAM
				,"SICAM"									= p_SICAM
				,"FuenteDatos"								= p_FuenteDatos
			    ,"TipoRegistro"								= p_TipoRegistro
			    ,"RUTEntidadMCF"							= p_RUTEntidadMCF
			    ,"Fecha"									= p_Fecha
			    ,"SaldoCuentaCambioConversion"				= p_SaldoCuentaCambioConversion
			    ,"SaldoMonedaOriginal"						= p_SaldoMonedaOriginal
				,"MonedaSaldo"								= p_MonedaSaldo
				,"Paridad"									= p_Paridad
				,"IdCuentaInversion"						= p_IdCuentaInversion			
				,"IdCliente"								= p_IdCliente
				,"IdCargoAbonoMovimiento"					= p_IdCargoAbonoMovimiento
				,"IdTipoMovimiento"							= p_IdTipoMovimiento
				,"FechaEfectivaMovimiento"					= p_FechaEfectivaMovimiento	
	        WHERE "ID" = p_ID;

			--LOG DE AUDITORIA (Inicio)
			V_CambiosModificados = SELECT * FROM :V_RegistroSolicitudCambio WHERE "ValorAntiguo" <> "ValorNuevo";
				
				BEGIN
					DECLARE p_IdLog BIGINT;
					DECLARE v_fechaHoraSistema NVARCHAR(40);

					SELECT "BSCL.Plataforma.BD.Secuencias::Log".NEXTVAL INTO p_IdLog FROM dummy;

					call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
						p_IdEmpresa => p_IdEmpresa,
						p_out_FechaHoraSistema => v_fechaHoraSistema
					);

					v_log = SELECT :p_IdLog AS "IdCabeceraLog"
									,'MODIFICACION' AS "IdTipoLog"
									,:p_IdModulo AS "IdModulo"
									,:p_IdUsuario AS "IdUsuario"
									,:p_IdEmpresa AS "IdEmpresa"
									,TO_VARCHAR (COALESCE(:v_fechaHoraSistema, '0001-01-01'), 'YYYY-MM-DD') as "FechaSistemaLog"
									,:p_IdUsuario AS "IdUsuarioAtendido"
									,0  AS "IdPersona"
									,TO_VARCHAR(COALESCE(:p_IdCliente, 0))  AS "IdCliente"
									,'' AS "EstadoCliente"
									,'' AS "NombreCliente"
									,'' AS "IdTipoDocumentoIdentidad"
									,'' AS "IdDocumentoIdentidad"
									,0  AS "IdPortafolio"
									,'' AS "EstadoPortafolio"
									,'MODIFICAR_REPO2P2' AS "IdTipoRegistro"
									,'' AS "IdRegistro"
									,'' AS "EstadoRegistro"
									,'' AS "IdTipoOperacion"
									,0 AS "Folio"
									,0 AS "Cantidad"
									,0 AS "PrecioTasa"
									,0 AS "Monto"
									,'' AS "IdMonedaOrigen"
									,'' AS "IdMonedaLiquidacion"
									,'' AS "TipoCambio"
									,'' AS "Nemo"
									,0  AS "IdProceso" 
									,'' AS "EstadoProceso"
									,'ModificarRepo2P2' AS "NombreProceso"
							FROM DUMMY;

					v_LogDetalle =
							SELECT
								:p_IdLog as "IdCabeceraLog"
								,'IdCargoAbonoMovimiento: ' || TO_VARCHAR(p_IdCargoAbonoMovimiento) as "SubRegistro"
								,COALESCE(logDetalle1."CampoModificado",'CAMPO') as "CampoRegistro"
								,COALESCE(logDetalle1."ValorAntiguo",'') as "ValorAntiguo"
								,COALESCE(logDetalle1."ValorNuevo", '') as "ValorNuevo"
							FROM :V_CambiosModificados AS logDetalle1;

					CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle => :v_LogDetalle);
				END;
			--LOG DE AUDITORIA (Fin)
        
       END IF; 
        
        
        
    ELSE
        --zona datos por defecto del layout
		SELECT CLI."DocumentoIdentidad" INTO v_RUTEntidadMCF
			FROM "BSCL.Plataforma.BD::PLA.Empresa" EMP
			INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC ON (SOC."IdSocioDeNegocio"=EMP."IdSocioDeNegocio")	
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI  ON (CLI."IdCliente"=SOC."IdCliente")
			WHERE EMP."IdEmpresa"=p_IdEmpresa;
			
	    SELECT "FechaHasta"  INTO v_Fecha 
	      	FROM "BSCL.Normativa.BD::DOC.Documento" DOC 
			INNER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarEsquemaHabil" CEH ON (CEH."Esquema"=DOC."Esquema" AND CEH."Version"=DOC."Version")
			WHERE "IdDocumento"=p_IdDocumento;
			
			
		SELECT COUNT(1) INTO v_ContadorOperacionMoneda
	      	FROM BSCL."BSCL.Normativa.BD::NOR.DatosREPO2P2" DOC 
			WHERE DOC."IdDocumento"=p_IdDocumento AND "MonedaSaldo"=p_MonedaSaldo;
			
		if v_ContadorOperacionMoneda > 0 then	
			
	
       		 p_out_AdvertenciasContador := p_out_AdvertenciasContador + 1;
            v_hash = SELECT '' AS "Codigo", '' AS "Valor" FROM DUMMY;
            CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('UI_YA_EXISTE_SALDO_EN_MONEDA_INGRESADA', :v_hash, :v_erroroutmsg);
            p_out_AdvertenciasDetalle =
                SELECT 'UI_YA_EXISTE_SALDO_EN_MONEDA_INGRESADA' AS "Codigo",
                    'N' AS "RequiereAutorizacion",
                    CAST('VALIDACION->' || :v_erroroutmsg AS NVARCHAR(520)) AS "Descripcion"
                FROM dummy
                UNION ALL
                SELECT * FROM :p_out_AdvertenciasDetalle;

            SIGNAL salida_controlada;
    	END IF;	
			
    
        SELECT IFNULL(MAX("ID"),0)+1 INTO p_out_id FROM "BSCL"."BSCL.Normativa.BD::NOR.DatosREPO2P2";
        INSERT INTO "BSCL.Normativa.BD::NOR.DatosREPO2P2"(
            "ID"
			,"IdDocumento"
			,"Estado"
			,"EstadoSICAM"
			,"SICAM"
			,"FuenteDatos"
		    ,"TipoRegistro"	
		    ,"RUTEntidadMCF"
		    ,"Fecha"
		    ,"SaldoCuentaCambioConversion"
		    ,"SaldoMonedaOriginal"
			,"MonedaSaldo"
			,"Paridad"
			,"IdCuentaInversion"
			,"IdCliente"
			,"IdCargoAbonoMovimiento"
			,"IdTipoMovimiento"
			,"FechaEfectivaMovimiento"
			
        ) VALUES (
            p_out_id
            ,p_IdDocumento
			,COALESCE(p_Estado,'')
			,COALESCE(p_EstadoSICAM,'')
			,COALESCE(p_SICAM,'')
            ,'MANTENEDOR'
		    ,COALESCE(p_TipoRegistro,'01')	
		    ,COALESCE(p_RUTEntidadMCF,v_RUTEntidadMCF)
		    ,COALESCE(p_Fecha,v_Fecha)
		    ,p_SaldoCuentaCambioConversion
		    ,p_SaldoMonedaOriginal
			,p_MonedaSaldo
			,p_Paridad
			,COALESCE(p_IdCuentaInversion,0)
			,COALESCE(p_IdCliente,0)
			,COALESCE(p_IdCargoAbonoMovimiento,0)
			,COALESCE(p_IdTipoMovimiento,'')
			,COALESCE(p_FechaEfectivaMovimiento,'0001-01-01')
			
         );
		 --LOG DE AUDITORIA (Inicio)
			V_CambiosModificados = SELECT * FROM :V_RegistroSolicitudCambio WHERE "ValorAntiguo" <> "ValorNuevo";
				
				BEGIN
					DECLARE p_IdLog BIGINT;
					DECLARE v_fechaHoraSistema NVARCHAR(40);

					SELECT "BSCL.Plataforma.BD.Secuencias::Log".NEXTVAL INTO p_IdLog FROM dummy;

					call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
						p_IdEmpresa => p_IdEmpresa,
						p_out_FechaHoraSistema => v_fechaHoraSistema
					);

					v_log = SELECT :p_IdLog AS "IdCabeceraLog"
									,'INGRESO' AS "IdTipoLog"
									,:p_IdModulo AS "IdModulo"
									,:p_IdUsuario AS "IdUsuario"
									,:p_IdEmpresa AS "IdEmpresa"
									,TO_VARCHAR (COALESCE(:v_fechaHoraSistema, '0001-01-01'), 'YYYY-MM-DD') as "FechaSistemaLog"
									,:p_IdUsuario AS "IdUsuarioAtendido"
									,0  AS "IdPersona"
									,TO_VARCHAR(COALESCE(:p_IdCliente, 0))  AS "IdCliente"
									,'' AS "EstadoCliente"
									,'' AS "NombreCliente"
									,'' AS "IdTipoDocumentoIdentidad"
									,'' AS "IdDocumentoIdentidad"
									,0  AS "IdPortafolio"
									,'' AS "EstadoPortafolio"
									,'CREAR_REPO2P2' AS "IdTipoRegistro"
									,'' AS "IdRegistro"
									,'' AS "EstadoRegistro"
									,'' AS "IdTipoOperacion"
									,0 AS "Folio"
									,0 AS "Cantidad"
									,0 AS "PrecioTasa"
									,0 AS "Monto"
									,'' AS "IdMonedaOrigen"
									,'' AS "IdMonedaLiquidacion"
									,'' AS "TipoCambio"
									,'' AS "Nemo"
									,0  AS "IdProceso" 
									,'' AS "EstadoProceso"
									,'CrearRepo2P2' AS "NombreProceso"
							FROM DUMMY;

					v_LogDetalle =
							SELECT
								:p_IdLog as "IdCabeceraLog"
								,'NUEVO' as "SubRegistro"
								,'IdCargoAbonoMovimiento' as "CampoRegistro"
								,'' as "ValorAntiguo"
								,COALESCE(TO_VARCHAR(p_IdCargoAbonoMovimiento), '') as "ValorNuevo"
							FROM DUMMY;

					CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle => :v_LogDetalle);
				END;
			--LOG DE AUDITORIA (Fin)
    END IF;
END;
