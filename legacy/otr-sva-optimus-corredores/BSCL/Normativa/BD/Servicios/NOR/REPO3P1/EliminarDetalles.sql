SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::NOR.REPO3P1.EliminarDetalles";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::NOR.REPO3P1.EliminarDetalles"
(
    ------------ Parametros de Entrada Comunes ----------

    -- ID empresa del login.
    IN p_IdEmpresa BIGINT,

    -- ID usuario del login.
    IN p_IdUsuario BIGINT,

    -- ID modulo invocador.
    IN p_IdModulo NVARCHAR(3),

    ------------ Parametros de Entrada Propios ----------

    -- ID del documento.
    IN p_IdDocumento BIGINT DEFAULT NULL,

    -- Lista de los ID's de los detalles a eliminar.
    IN p_List table("ID" BIGINT),

    ------------ Parametros de Salida Propios ----------

    ------------ Salidas Advertencias ----------

    -- Contador de advertencias.
    OUT p_out_AdvertenciasContador INT,

    -- Tabla de advertencias.
    OUT p_out_AdvertenciasDetalle TABLE("Codigo" NVARCHAR(40), "RequiereAutorizacion" NVARCHAR(40), "Descripcion" NVARCHAR(5000)),

    ------------ Salidas Errores ----------

    -- Contador de errores
    -- Importante: Debe ser cero para que se haga commit.
    OUT p_out_ErroresContador INT,

    -- Tabla de errores.
    OUT p_out_ErroresDetalle TABLE("Codigo" NVARCHAR(40), "Descripcion" NVARCHAR(5000))
)
    LANGUAGE sqlscript
    SQL security invoker
    DEFAULT SCHEMA "BSCL"
AS
BEGIN
    ------------ Variables Propias ----------
    DECLARE v_i int;
    DECLARE v_arr_ids bigint array;
    
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
            resignal;
        END IF;
    END;
    
    
    p_out_AdvertenciasContador := 0;
    p_out_ErroresContador := 0;
    p_out_AdvertenciasDetalle = SELECT '' AS "Codigo", '' AS "RequiereAutorizacion", '' AS "Descripcion" FROM dummy WHERE 1 = 2;
    p_out_ErroresDetalle = SELECT '' AS "Codigo", '' AS "Descripcion" FROM dummy WHERE 1 = 2;

    --Inicializacion de variables autorizacion:
	v_secuencia_IdSolicitudCambio 	:= -1;
	v_estadoAutorizacion 			:= 'PEN';
	v_tipoEntidad					:= 'ID';
	v_IdConcepto					:= 'R3P1EL';			
	p_out_RefId						:=0;
	
	call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
	p_IdEmpresa => p_IdEmpresa,
	p_out_FechaHoraSistema => v_fechaHoraSistema
	);	
    
    SELECT "BSCL.Plataforma.BD.Secuencias::PLA.IdSolicitudCambio".NEXTVAL into v_secuencia_IdSolicitudCambio from dummy;
	IF p_IdDocumento IS NOT NULL THEN
	    V_RegistroSolicitudCambio =SELECT	
		v_secuencia_IdSolicitudCambio 	AS 	"IdSolicitudCambio",	
		0 								AS	"IdSecuenciaCambio", --- Se crea interno
		'IdDocumento'					AS 	"IdTipoEntidad",	
		CAST (:p_IdDocumento AS NVARCHAR(40))	AS 	"IdEntidadOrigen",
		'IdDocumento' 					AS 	"CampoModificado",
		'BIGINT' 						AS	"TipoDato",
		CAST (:p_IdDocumento AS NVARCHAR(255)) 		AS 	"ValorAntiguo",
		CAST (:p_IdDocumento AS NVARCHAR(255)) 		AS 	"ValorNuevo",
		CURRENT_UTCTIMESTAMP 			AS 	"FechaSolicitud",--SE CREAN INTERNOS
		CURRENT_UTCTIMESTAMP 			AS 	"FechaRevision",--SE CREAN INTERNOS
		v_estadoAutorizacion 			AS 	"IdEstadoAutorizacion"
		FROM DUMMY;
	 ELSE
        V_RegistroSolicitudCambio =SELECT	
		v_secuencia_IdSolicitudCambio 	AS 	"IdSolicitudCambio",	
		0 								AS	"IdSecuenciaCambio", --- Se crea interno
		v_tipoEntidad					AS 	"IdTipoEntidad",	
		CAST (LST."ID" AS NVARCHAR(40))	AS 	"IdEntidadOrigen",
		'ID' 							AS 	"CampoModificado",
		'BIGINT' 						AS 	"TipoDato",
		CAST (LST."ID" AS NVARCHAR(255))	AS 	"ValorAntiguo",
		CAST (LST."ID" AS NVARCHAR(255))	AS 	"ValorNuevo",
		CURRENT_UTCTIMESTAMP 			AS 	"FechaSolicitud",--SE CREAN INTERNOS
		CURRENT_UTCTIMESTAMP 			AS 	"FechaRevision",--SE CREAN INTERNOS
		v_estadoAutorizacion 			AS 	"IdEstadoAutorizacion"
		FROM :p_List LST;

    END IF;
	
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
			
			SELECT  'SICAM REPORTE 3.1 / FECHA : ' || "FechaReporte" into  v_Glosa 	FROM "BSCL.Normativa.BD.Vistas::NOR.REPO3P1.FechaReporte";
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
			V_RegistrosCambio =
			SELECT 
				RSC."IdSolicitudCambio",
				RSC."IdSecuenciaCambio",
				RSC."IdTipoEntidad",
				RSC."IdEntidadOrigen",
				RSC."CampoModificado",
				RSC."TipoDato",
				RSC."ValorAntiguo",
				RSC."ValorNuevo",
				RSC."FechaSolicitud",
				RSC."FechaRevision",
				RSC."IdEstadoAutorizacion",
				REPO."Identificador"
			FROM :V_RegistroSolicitudCambio AS RSC
			INNER JOIN "BSCL.Normativa.BD::NOR.DatosREPO3P1" AS REPO
				ON RSC."IdEntidadOrigen" = REPO."ID";		
		    
		    ------------ Logica de Actualizacion ----------
		    IF p_IdDocumento IS NOT NULL THEN
		        DELETE FROM "BSCL.Normativa.BD::NOR.DatosREPO3P1" WHERE "IdDocumento" = :p_IdDocumento AND "EstadoSICAM"='';
		    ELSE
		        v_arr_ids := array_agg(:p_List."ID" ORDER BY "ID" DESC);
		        FOR v_i IN 1 .. cardinality(:v_arr_ids) DO
		            DELETE FROM "BSCL.Normativa.BD::NOR.DatosREPO3P1" WHERE "ID" = :v_arr_ids[v_i] AND "EstadoSICAM"='';
		        END FOR;
		    END IF;
			--LOG DE AUDITORIA (Inicio)			
				
				BEGIN
					DECLARE p_IdLog BIGINT;
					DECLARE v_fechaHoraSistema NVARCHAR(40);

					SELECT "BSCL.Plataforma.BD.Secuencias::Log".NEXTVAL INTO p_IdLog FROM dummy;

					call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
						p_IdEmpresa => p_IdEmpresa,
						p_out_FechaHoraSistema => v_fechaHoraSistema
					);

					v_log = SELECT :p_IdLog AS "IdCabeceraLog"
									,'ELIMINACION' AS "IdTipoLog"
									,:p_IdModulo AS "IdModulo"
									,:p_IdUsuario AS "IdUsuario"
									,:p_IdEmpresa AS "IdEmpresa"
									,TO_VARCHAR (COALESCE(:v_fechaHoraSistema, '0001-01-01'), 'YYYY-MM-DD') as "FechaSistemaLog"
									,:p_IdUsuario AS "IdUsuarioAtendido"
									,0  AS "IdPersona"
									,0  AS "IdCliente"
									,'' AS "EstadoCliente"
									,'' AS "NombreCliente"
									,'' AS "IdTipoDocumentoIdentidad"
									,'' AS "IdDocumentoIdentidad"
									,0  AS "IdPortafolio"
									,'' AS "EstadoPortafolio"
									,'ELIMINAR_REPO3P1' AS "IdTipoRegistro"
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
									,'EliminarRepo3P1' AS "NombreProceso"
							FROM DUMMY;
					IF p_IdDocumento IS NOT NULL THEN
						v_LogDetalle =
							SELECT
								:p_IdLog as "IdCabeceraLog"
								,'ELIMINA' as "SubRegistro"
								,'IdDocumento' as "CampoRegistro"
								,COALESCE(TO_VARCHAR(p_IdDocumento),'') as "ValorAntiguo"
								,'' as "ValorNuevo"
							FROM DUMMY;
					ELSE
						v_LogDetalle =
							SELECT
								:p_IdLog as "IdCabeceraLog"
								,'ELIMINA' as "SubRegistro"
								,'Identificador' as "CampoRegistro"
								,COALESCE(logDetalle."Identificador",'') as "ValorAntiguo"
								,'' as "ValorNuevo"
							FROM :V_RegistrosCambio as logDetalle;
					END IF;					

					CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle => :v_LogDetalle);
				END;
			--LOG DE AUDITORIA (Fin)
				
	 	end if;	
END;




