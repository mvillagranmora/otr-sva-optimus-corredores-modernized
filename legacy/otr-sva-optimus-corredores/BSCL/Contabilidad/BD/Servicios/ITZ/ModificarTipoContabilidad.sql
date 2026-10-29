SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Contabilidad.BD.Servicios::ITZ.ModificarTipoContabilidad";

CREATE PROCEDURE 
-- 
--  Proposito: Modifica
--             
--  Empresa: Ingeniería ProNet IT SpA.
--  Autor: Hans Muller :: HMR
--  Fecha Creacion: 13/10/2016
--  Id Servicio = 12024
--  
"BSCL.Contabilidad.BD.Servicios::ITZ.ModificarTipoContabilidad" (
    --VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa                  BIGINT,
    IN p_IdUsuario                  BIGINT,
    IN p_IdModulo                   NVARCHAR(3),
    IN p_TabTipoContabilidad        TABLE(
                                            "IdTipoContabilidad"    BIGINT,
                                            "Nombre"                NVARCHAR(60),
                                            "Nemo"                  NVARCHAR(20),
                                            "Tipo"                  NVARCHAR(1),
                                            "Reversa"               NVARCHAR(1),
                                            "Agrupa"                NVARCHAR(1),
                                            "Condicionado"          NVARCHAR(1),
                                            "TipoGeneracion"        NVARCHAR(1),
                                            "IdEmpresa"             BIGINT,
                                            "Ejercicio"             BIGINT,
                                            "Formato"               NVARCHAR(1),
                                            "Estado"                NVARCHAR(1) 
                                          ),
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
        DECLARE v_Operaciones_Modificar     BIGINT;
        DECLARE v_Operaciones_Existentes    BIGINT;
        DECLARE v_nemoEntrada               NVARCHAR(20);
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
        
        SELECT TRIM("Nemo") INTO v_nemoEntrada FROM :p_TabTipoContabilidad;
        
        SELECT COUNT(ContabilidadTipo."IdTipoContabilidad") INTO v_existeNemo FROM "BSCL.Contabilidad.BD::ITZ.ContabilidadTipo" ContabilidadTipo
            INNER JOIN :p_TabTipoContabilidad Tmp ON Tmp."IdTipoContabilidad" <> ContabilidadTipo."IdTipoContabilidad"
            WHERE TRIM (ContabilidadTipo."Nemo") = v_nemoEntrada;
        
        IF (:v_existeNemo > 0) THEN
          p_out_ErroresContador = 1;
          p_out_ErroresDetalle = select 0 AS "IdInterno", 'NEMOTECNICO_EXISTE' AS "Codigo", 'NEMOTECNICO_EXISTE' as "Descripcion" from dummy;
        ELSE
            UPDATE ContabilidadTipo
                SET ContabilidadTipo."Nombre"            = Tmp."Nombre",
                ContabilidadTipo."Nemo"              = Tmp."Nemo",
                ContabilidadTipo."Tipo"              = Tmp."Tipo",
                ContabilidadTipo."Reversa"           = Tmp."Reversa",
                ContabilidadTipo."Agrupa"            = Tmp."Agrupa",
                ContabilidadTipo."Condicionado"      = Tmp."Condicionado",
                ContabilidadTipo."TipoGeneracion"    = Tmp."TipoGeneracion",
                ContabilidadTipo."IdEmpresa"         = Tmp."IdEmpresa",
                ContabilidadTipo."Ejercicio"         = Tmp."Ejercicio",
                ContabilidadTipo."Formato"           = Tmp."Formato",
                ContabilidadTipo."Estado"            = Tmp."Estado"
            FROM "BSCL.Contabilidad.BD::ITZ.ContabilidadTipo" ContabilidadTipo
                            INNER JOIN :p_TabTipoContabilidad Tmp ON
                                            Tmp."IdTipoContabilidad" = ContabilidadTipo."IdTipoContabilidad";
                                            
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
									,'MODIFICACION' AS "IdTipoLog"
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
									,'MODIFICAR_TIPO_CONTABILIDAD' AS "IdTipoRegistro"
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
									,'ModificarTipoContabilidad' AS "NombreProceso"
							FROM DUMMY;

					v_LogDetalle =
							SELECT
								:p_IdLog as "IdCabeceraLog"
								,'MODIFICACION' as "SubRegistro"
								,'Nombre' as "CampoRegistro"
								,'' as "ValorAntiguo"
								,COALESCE(logDetalle1."Nombre", '') as "ValorNuevo"
							FROM :p_TabTipoContabilidad AS logDetalle1
							
							UNION ALL
							SELECT
								:p_IdLog as "IdCabeceraLog"
								,'MODIFICACION' as "SubRegistro"
								,'Nemo' as "CampoRegistro"
								,'' as "ValorAntiguo"
								,COALESCE(logDetalle2."Nemo", '') as "ValorNuevo"
							FROM :p_TabTipoContabilidad AS logDetalle2
							
							UNION ALL
							SELECT
								:p_IdLog as "IdCabeceraLog"
								,'MODIFICACION' as "SubRegistro"
								,'Tipo Generacion' as "CampoRegistro"
								,'' as "ValorAntiguo"
								,COALESCE(logDetalle3."TipoGeneracion", '') as "ValorNuevo"
							FROM :p_TabTipoContabilidad AS logDetalle3
							
							UNION ALL
							SELECT
								:p_IdLog as "IdCabeceraLog"
								,'MODIFICACION' as "SubRegistro"
								,'Reversa' as "CampoRegistro"
								,'' as "ValorAntiguo"
								,COALESCE(logDetalle4."Reversa", '') as "ValorNuevo"
							FROM :p_TabTipoContabilidad AS logDetalle4
							
							UNION ALL
							SELECT
								:p_IdLog as "IdCabeceraLog"
								,'MODIFICACION' as "SubRegistro"
								,'Agrupa' as "CampoRegistro"
								,'' as "ValorAntiguo"
								,COALESCE(logDetalle5."Agrupa", '') as "ValorNuevo"
							FROM :p_TabTipoContabilidad AS logDetalle5
							
							UNION ALL
							SELECT
								:p_IdLog as "IdCabeceraLog"
								,'MODIFICACION' as "SubRegistro"
								,'Formato' as "CampoRegistro"
								,'' as "ValorAntiguo"
								,COALESCE(TO_VARCHAR(logDetalle6."Formato"), '') AS "ValorNuevo"
							FROM :p_TabTipoContabilidad AS logDetalle6;

					CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle => :v_LogDetalle);
				END;
				--LOG DE AUDITORIA (Fin)                                          
        END IF;
    END;
END;