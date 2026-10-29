SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL"."BSCL.Contabilidad.BD.Servicios::ITZ.ReplicarTipoContabilidad";
GO
CREATE PROCEDURE 
-- 
--  Proposito: Replica de Registros
--  
--  Empresa: Ingenieria ProNet IT SpA.
--  Autor: Hans Muller :: HMR
--  Fecha Creacion: 20/10/2016
--  Id Servicio = 12027
--  
"BSCL.Contabilidad.BD.Servicios::ITZ.ReplicarTipoContabilidad" (
    --VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa                  BIGINT,
    IN p_IdUsuario                  BIGINT,
    IN p_IdModulo                   NVARCHAR(3),
    IN p_UsaReglasNegocio           NVARCHAR(1),
    --Parametros particulares
    IN p_IdTipoContabilidad         BIGINT,
    IN p_Nombre                     NVARCHAR(60),
    IN p_Nemo                       NVARCHAR(20),
    IN p_Ejercicio                  BIGINT,
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

    DECLARE v_out_AdvertenciasDetalle   TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" NVARCHAR(100),"NombreParametro" NVARCHAR(40))
                                        DEFAULT SELECT 0 as "IdInterno",'' as "Codigo",'' as "RequiereAutorizacion",'' as "ValorParametro",'' as  "NombreParametro" FROM DUMMY WHERE 1=2;
    DECLARE v_out_ErroresDetalle        TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100),"ValorParametro" NVARCHAR(100),"NombreParametro" NVARCHAR(40))
                                        DEFAULT SELECT 0 as "IdInterno",'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" FROM DUMMY WHERE 1=2;

    DECLARE v_count_operaciones         BIGINT;
    
    DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA   
    BEGIN
        --salida/error/advertencia controlado por este servicio
        SELECT COUNT(1) INTO p_out_ErroresContador 
          FROM :v_out_ErroresDetalle;
        
        SELECT COUNT(1) INTO p_out_AdvertenciasContador 
          FROM :v_out_AdvertenciasDetalle;  
        
        v_TablaCodigosMensajes = SELECT "Codigo"            AS "CodigoMensaje",
                                        "NombreParametro"   AS "CodigoDato",
                                        "ValorParametro"    AS "ValorDato" 
                                   FROM :v_out_ErroresDetalle
                                 UNION ALL
                                 SELECT "Codigo"            AS "CodigoMensaje",
                                        "NombreParametro"   AS "CodigoDato",
                                        "ValorParametro"    AS "ValorDato" 
                                   FROM :v_out_AdvertenciasDetalle;

        CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes, v_out_ErroresDetalleMensajes);
        
        p_out_ErroresDetalle =  SELECT errores."IdInterno",
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

    p_out_AdvertenciasContador  := 0;
    p_out_ErroresContador       := 0;
    
    --------------------
    --Negocio Aqui
    --------------------
    BEGIN -- proceso de negocio      
        --PARAMETROS REGLAS NEGOCIO
        DECLARE v_resultadoRegla            NVARCHAR(40);
        
        DECLARE v_Orden                     BIGINT;
        DECLARE v_IdTipoContabilidadNuevo   BIGINT;
        DECLARE v_IdTipoCentralizacion      BIGINT;
        DECLARE v_IdDefinicionAsiento       BIGINT;
        DECLARE v_IdDefinicionAsientoNew    BIGINT;
        DECLARE v_ContadorDocReg            BIGINT;
        DECLARE v_ContadorDefinicionAsiento BIGINT;
        
        DECLARE v_Registros                 BIGINT;
        DECLARE v_existeNemo                BIGINT DEFAULT 0;
        --*******************************
        --*VALIDACION REGLAS BASICAS *
        --*******************************
        CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa
                                                                         ,:p_IdUsuario
                                                                         ,:p_IdModulo
                                                                         ,:v_out_ErroresDetalle_Basicos
                                                                         ,:v_resultadoRegla);
        IF :v_resultadoRegla = 'RECH' THEN
            v_out_ErroresDetalle = SELECT 0     AS "IdInterno",
                                          ''    AS "Codigo",
                                          ''    AS "ValorParametro",
                                          ''    AS "NombreParametro"
                                     FROM :v_out_ErroresDetalle_Basicos;
            SIGNAL SALIDA_CONTROLADA;
        END IF;
        
        
        SELECT COUNT("IdTipoContabilidad") INTO v_existeNemo FROM "BSCL.Contabilidad.BD::ITZ.ContabilidadTipo" WHERE TRIM ("Nemo") = TRIM(:p_Nemo);
        SELECT COUNT(CTB_itzContabilidadTipoCentralizacionTipo."IdTipoCentralizacion")
            INTO v_IdTipoCentralizacion 
            FROM "BSCL.Contabilidad.BD::ITZ.ContabilidadTipoCentralizacionTipo" AS CTB_itzContabilidadTipoCentralizacionTipo
            WHERE CTB_itzContabilidadTipoCentralizacionTipo."IdTipoContabilidad" = :p_IdTipoContabilidad;
           
        IF (:v_existeNemo > 0) THEN
            p_out_ErroresContador = 1;
            p_out_ErroresDetalle = select 0 AS "IdInterno", 'NEMOTECNICO_EXISTE' AS "Codigo", 'NEMOTECNICO_EXISTE' as "Descripcion" from dummy;            
        ELSEIF (:v_IdTipoCentralizacion = 0) THEN 
        	p_out_ErroresContador = 1;
            p_out_ErroresDetalle = select 0 AS "IdInterno", 'CENTRALIZACIONES_NO_ASOCIADAS' AS "Codigo", 'CENTRALIZACIONES_NO_ASOCIADAS' as "Descripcion" from dummy;
        ELSE
            -- Crear una tabla temporal para almacenar los IdTipoCentralizacion
			CREATE LOCAL TEMPORARY TABLE #TempCentralizacion (
			    IdTipoCentralizacion INT
			);
			
			-- Insertar los registros en la tabla temporal
			INSERT INTO #TempCentralizacion (IdTipoCentralizacion)
			SELECT "IdTipoCentralizacion"
			FROM "BSCL.Contabilidad.BD::ITZ.ContabilidadTipoCentralizacionTipo"
			WHERE "IdTipoContabilidad" = :p_IdTipoContabilidad;
            
            SELECT "BSCL.Contabilidad.BD.Secuencias::IdTipoContabilidad".NEXTVAL
            INTO v_IdTipoContabilidadNuevo
            FROM DUMMY;
            
            -- Tipo Contabilidad
            INSERT INTO "BSCL.Contabilidad.BD::ITZ.ContabilidadTipo"( "IdTipoContabilidad",
                                                                      "Nombre",
                                                                      "Nemo",
                                                                      "Tipo",
                                                                      "Reversa",
                                                                      "Agrupa",
                                                                      "Condicionado",
                                                                      "TipoGeneracion",
                                                                      "IdEmpresa",
                                                                      "Ejercicio",
                                                                      "Formato",
                                                                      "Estado") 
                    SELECT :v_IdTipoContabilidadNuevo,
                        CASE WHEN :p_Nombre = '' THEN 
                            CTB_itzContabilidadTipo."Nombre" 
                        ELSE
                            :p_Nombre 
                        END,
                        CASE WHEN :p_Nemo = '' THEN 
                            CTB_itzContabilidadTipo."Nemo" 
                        ELSE 
                            :p_Nemo 
                        END, 
                        CTB_itzContabilidadTipo."Tipo", 
                        CTB_itzContabilidadTipo."Reversa", 
                        CTB_itzContabilidadTipo."Agrupa", 
                        CTB_itzContabilidadTipo."Condicionado", 
                        CTB_itzContabilidadTipo."TipoGeneracion", 
                        CASE WHEN :p_IdEmpresa = -1 THEN 
                            CTB_itzContabilidadTipo."IdEmpresa" 
                        ELSE 
                            :p_IdEmpresa 
                        END, 
                        CASE WHEN :p_Ejercicio = 0 THEN 
                            CTB_itzContabilidadTipo."Ejercicio" 
                        ELSE 
                            :p_Ejercicio 
                        END, 
                        CTB_itzContabilidadTipo."Formato", 
                        CTB_itzContabilidadTipo."Estado" 
                    FROM "BSCL.Contabilidad.BD::ITZ.ContabilidadTipo" AS CTB_itzContabilidadTipo
                    WHERE CTB_itzContabilidadTipo."IdTipoContabilidad" = :p_IdTipoContabilidad;

            --Definicion Asiento
            INSERT INTO "BSCL.Contabilidad.BD::ITZ.ContabilidadDefinicionAsiento" ( "IdDefinicionAsiento",
                                                                                    "IdTipoContabilidad", 
                                                                                    "IdCuentaContable", 
                                                                                    "Ejercicio", 
                                                                                    "Orden", 
                                                                                    "DebeHaber", 
                                                                                    "TipoEvento", 
                                                                                    "Condicionado",
                                                                                    "OmiteCondicionCmbte", 
                                                                                    "Estado",
                                                                                    "Formato") 
                    SELECT "BSCL.Contabilidad.BD.Secuencias::IdDefinicionAsiento".NEXTVAL,
                        :v_IdTipoContabilidadNuevo,
                        CTB_itzContabilidadDefinicionAsiento."IdCuentaContable",
                        CTB_itzContabilidadDefinicionAsiento."Ejercicio",
                        CTB_itzContabilidadDefinicionAsiento."Orden",
                        CTB_itzContabilidadDefinicionAsiento."DebeHaber",
                        CTB_itzContabilidadDefinicionAsiento."TipoEvento",
                        CTB_itzContabilidadDefinicionAsiento."Condicionado",
                        CTB_itzContabilidadDefinicionAsiento."OmiteCondicionCmbte",
                        CTB_itzContabilidadDefinicionAsiento."Estado",
                        CTB_itzContabilidadDefinicionAsiento."Formato"
                    FROM "BSCL"."BSCL.Contabilidad.BD::ITZ.ContabilidadDefinicionAsiento" AS CTB_itzContabilidadDefinicionAsiento
                    WHERE "IdTipoContabilidad" = :p_IdTipoContabilidad;

            --Contabilidad TipoAsiento Condicion
            INSERT INTO "BSCL.Contabilidad.BD::ITZ.ContabilidadTipoAsientoCondicion" (  "IdCondicion",
                                                                                    "IdDefinicionAsiento", 
                                                                                    "IdTipoContabilidad", 
                                                                                    "IdTipoDatoEvento", 
                                                                                    "ValorVarchar", 
                                                                                    "ValorDecimal", 
                                                                                    "Condicion", 
                                                                                    "Conector", 
                                                                                    "TipoCondicion", 
                                                                                    "Estado")
                    SELECT "BSCL.Contabilidad.BD.Secuencias::IdTipoAsientoCondicion".NEXTVAL,
                        0, --[IdDefinicionAsiento]
                        :v_IdTipoContabilidadNuevo,
                        "IdTipoDatoEvento",
                        "ValorVarchar",
                        "ValorDecimal",
                        "Condicion",
                        "Conector",
                        "TipoCondicion",
                        "Estado"
                    FROM "BSCL.Contabilidad.BD::ITZ.ContabilidadTipoAsientoCondicion"
                    WHERE "IdTipoContabilidad" = :p_IdTipoContabilidad;

			-- Relaci�n Centralizaci�n/Tipo Contabilidad
			INSERT INTO "BSCL.Contabilidad.BD::ITZ.ContabilidadTipoCentralizacionTipo" 
			    ("IdTipoCentralizacion", "IdTipoContabilidad", "Estado")
			SELECT 
			    T.IdTipoCentralizacion,  -- Tomado de la tabla temporal
			    :v_IdTipoContabilidadNuevo,  
			    C."Estado"
			FROM #TempCentralizacion T  
			JOIN "BSCL.Contabilidad.BD::ITZ.ContabilidadTipoCentralizacionTipo" C 
			    ON C."IdTipoCentralizacion" = T.IdTipoCentralizacion
			WHERE C."IdTipoContabilidad" = :p_IdTipoContabilidad;
          
            -----------------------------------------------------------------------------------------------------------------------------------------------
            -----------------         Relación de Tipo Evento/Definicion Asiento       --------------------------------------------------------------------
            -----------------------------------------------------------------------------------------------------------------------------------------------
            SELECT MAX(CTB_itzContabilidadDefinicionAsiento."Orden")
            INTO v_Registros
            FROM "BSCL.Contabilidad.BD::ITZ.ContabilidadDefinicionAsiento" AS CTB_itzContabilidadDefinicionAsiento 
            WHERE CTB_itzContabilidadDefinicionAsiento."IdTipoContabilidad" = :p_IdTipoContabilidad; 
            
            --v_ContadorDocReg := 1;
            SELECT MIN(CTB_itzContabilidadDefinicionAsiento."Orden")
            INTO v_ContadorDocReg
            FROM "BSCL.Contabilidad.BD::ITZ.ContabilidadDefinicionAsiento" AS CTB_itzContabilidadDefinicionAsiento 
            WHERE CTB_itzContabilidadDefinicionAsiento."IdTipoContabilidad" = :p_IdTipoContabilidad
            AND "Orden" > 0; 

        
        
            WHILE :v_ContadorDocReg <= :v_Registros DO
            
                    SELECT COUNT(1) 
                    INTO v_ContadorDefinicionAsiento 
                    FROM "BSCL.Contabilidad.BD::ITZ.ContabilidadDefinicionAsiento"
                    WHERE "IdTipoContabilidad" = :p_IdTipoContabilidad
                    AND "Estado" = 'V'
                    AND "Orden" = :v_ContadorDocReg;
            
                IF v_ContadorDefinicionAsiento > 0 THEN 
                    SELECT "IdDefinicionAsiento"
                    INTO v_IdDefinicionAsiento
                    FROM "BSCL.Contabilidad.BD::ITZ.ContabilidadDefinicionAsiento"
                    WHERE "IdTipoContabilidad" = :p_IdTipoContabilidad
                    AND "Estado" = 'V'
                    AND "Orden" = :v_ContadorDocReg;
                    
                    SELECT "IdDefinicionAsiento"
                    INTO v_IdDefinicionAsientoNew
                    FROM "BSCL.Contabilidad.BD::ITZ.ContabilidadDefinicionAsiento"
                    WHERE "IdTipoContabilidad" = :v_IdTipoContabilidadNuevo
                    AND "Estado" = 'V'
                    AND "Orden" = :v_ContadorDocReg;
    
                    --------------------------------------------------------------------------------------------------------------------------------------------------          
                    INSERT INTO "BSCL.Contabilidad.BD::ITZ.ContabilidadDefinicionTipoEvento" ( "IdTipoEvento",
                                                                                               "IdDefinicionAsiento",
                                                                                               "Operador",
                                                                                               "Estado") 
                        SELECT "IdTipoEvento",
                            :v_IdDefinicionAsientoNew,
                            "Operador",
                            "Estado" 
                        FROM "BSCL.Contabilidad.BD::ITZ.ContabilidadDefinicionTipoEvento" AS CTB_itzContabilidadDefinicionTipoEvento
                        WHERE CTB_itzContabilidadDefinicionTipoEvento."IdDefinicionAsiento" = :v_IdDefinicionAsiento;
                  
                    --------------------------------------------------------------------------------------------------------------------------------------------------
                    INSERT INTO "BSCL.Contabilidad.BD::ITZ.ContabilidadTipoAsientoCondicion" ( "IdCondicion",
                                                                                               "IdDefinicionAsiento",
                                                                                               "IdTipoContabilidad",
                                                                                               "IdTipoDatoEvento",
                                                                                               "ValorVarchar",
                                                                                               "ValorDecimal",
                                                                                               "Condicion",
                                                                                               "Conector",
                                                                                               "TipoCondicion",
                                                                                               "Estado") 
                        SELECT "BSCL.Contabilidad.BD.Secuencias::IdTipoAsientoCondicion".NEXTVAL,
                            :v_IdDefinicionAsientoNew, 
                            0, --"BSCL.Contabilidad.BD.Secuencias::IdDefinicionAsiento".NEXTVAL
                            "IdTipoDatoEvento", 
                            "ValorVarchar", 
                            "ValorDecimal", 
                            "Condicion", 
                            "Conector", 
                            "TipoCondicion", 
                            "Estado" 
                        FROM "BSCL.Contabilidad.BD::ITZ.ContabilidadTipoAsientoCondicion"
                        WHERE "IdDefinicionAsiento" = :v_IdDefinicionAsiento;
    
                    --------------------------------------------------------------------------------------------------------------------------------------------------            
    
                    -- Glosa del Tipo Contabilidad
                    INSERT INTO "BSCL.Contabilidad.BD::ITZ.ContabilidadTipoGlosa" ( "IdTipoContabilidad",
                                                                                    "IdDefinicionAsiento",
                                                                                    "Origen",
                                                                                    "Orden",
                                                                                    "GlosaCampo",
                                                                                    "NombreCampoMovimiento",
                                                                                    "IdTipoDatoEvento",
                                                                                    "Estado")
                        SELECT :v_IdTipoContabilidadNuevo,
                            :v_IdDefinicionAsientoNew,
                            "Origen",
                            "Orden",
                            "GlosaCampo",
                            "NombreCampoMovimiento",
                            "IdTipoDatoEvento",
                            "Estado"
                        FROM "BSCL.Contabilidad.BD::ITZ.ContabilidadTipoGlosa" 
                        WHERE "IdTipoContabilidad" = :p_IdTipoContabilidad
                        AND "IdDefinicionAsiento" = :v_IdDefinicionAsiento;
                END IF;
             --------------------------------------------------------------------------------------------------------------------------------------------------          
             v_ContadorDocReg := :v_ContadorDocReg + 1;                 
          END WHILE;
          
			--LOG DE AUDITORIA (Inicio)
			BEGIN
				DECLARE p_IdLog BIGINT;
				DECLARE v_fechaHoraSistema NVARCHAR(40);

				SELECT "BSCL"."BSCL.Plataforma.BD.Secuencias::Log".NEXTVAL INTO p_IdLog FROM dummy;

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
								,0  AS "IdCliente"
								,'' AS "EstadoCliente"
								,'' AS "NombreCliente"
								,'' AS "IdTipoDocumentoIdentidad"
								,'' AS "IdDocumentoIdentidad"
								,0  AS "IdPortafolio"
								,'' AS "EstadoPortafolio"
								,'REPLICAR_TIPO_CONTABILIDAD' AS "IdTipoRegistro"
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
								,'ReplicarTipoContabilidad' AS "NombreProceso"
						FROM DUMMY;

				v_LogDetalle =
						SELECT
							:p_IdLog as "IdCabeceraLog"
							,'INGRESO' as "SubRegistro"
							,'Nombre' as "CampoRegistro"
							,'' as "ValorAntiguo"
							,COALESCE(:p_Nombre, '') as "ValorNuevo"
						FROM DUMMY
						
						UNION ALL
						SELECT
							:p_IdLog as "IdCabeceraLog"
							,'INGRESO' as "SubRegistro"
							,'Nemo' as "CampoRegistro"
							,'' as "ValorAntiguo"
							,COALESCE(:p_Nemo, '') as "ValorNuevo"
						FROM DUMMY
						
						UNION ALL
						SELECT
							:p_IdLog as "IdCabeceraLog"
							,'INGRESO' as "SubRegistro"
							,'Ejercicio' as "CampoRegistro"
							,'' as "ValorAntiguo"
							,COALESCE(TO_VARCHAR(:p_Ejercicio), '') AS "ValorNuevo"
						FROM DUMMY;

				CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle => :v_LogDetalle);
			END;
			--LOG DE AUDITORIA (Fin)
        END IF;
    END;
END;
GO