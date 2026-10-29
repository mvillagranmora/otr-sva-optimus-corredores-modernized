SET SCHEMA "BSCL"; 
GO

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::PAU.AbrirModulo";
GO

CREATE PROCEDURE
--  Proposito: Abrir Modulo
--  Autor: - Gat-Blac 
--  Fecha Creacion: 31/05/2017
"BSCL.Plataforma.BD.Servicios::PAU.AbrirModulo" (
     --VARIABLES OBLIGATORIAS
    IN p_IdEmpresa                      BIGINT,
    IN p_IdUsuario                      BIGINT,
    IN p_IdModulo                       NVARCHAR(3),
    IN p_Modulo                 TABLE   (
                                            "CodigoModulo"                  NVARCHAR(40),
                                            "Estado"                        NVARCHAR(3),
                                            "CodigoUsuario"                 NVARCHAR(40)
                                        ),
    IN p_MesaAbierta    				NVARCHAR(1),                              
    OUT p_out_Resultado         TABLE   (
                                            "CodigoModulo"                  NVARCHAR(40),
                                            "IdEstado"                      NVARCHAR(3),
                                            "NombreModulo"                  NVARCHAR(40)
                                        ),
    OUT p_out_AdvertenciasContador      INT,
    OUT p_out_AdvertenciasDetalle       TABLE ("IdInterno"  BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)),
    OUT p_out_ErroresContador           INT,
    OUT p_out_ErroresDetalle            TABLE ("IdInterno"  BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(520))
    )
    LANGUAGE SQLSCRIPT
    SQL SECURITY INVOKER 
    DEFAULT SCHEMA "BSCL"
AS
BEGIN
    DECLARE v_out_AdvertenciasDetalle           TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" NVARCHAR(1),"CodigoDato1" NVARCHAR(100),"ValorDato1" NVARCHAR(520),"CodigoDato2" NVARCHAR(100),"ValorDato2" NVARCHAR(520));
    DECLARE v_out_ErroresDetalle                TABLE ("IdInterno" BIGINT,"Codigo" NVARCHAR(100),"CodigoDato1" NVARCHAR(100),"ValorDato1" NVARCHAR(520),"CodigoDato2" NVARCHAR(100),"ValorDato2" NVARCHAR(520));
    BEGIN
        v_out_AdvertenciasDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'N' as "RequiereAutorizacion",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" from DUMMY where 1=2;
        v_out_ErroresDetalle      = SELECT 0 as "IdInterno" ,'' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" from DUMMY where 1=2;
    END;
    BEGIN
        DECLARE SALIDA_CONTROLADA CONDITION;
        DECLARE v_FechaSistema NVARCHAR(10); 
        DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA  
        BEGIN
            select count(1) into p_out_ErroresContador 
            from :v_out_ErroresDetalle;
            select count(1) into p_out_AdvertenciasContador 
            from :v_out_AdvertenciasDetalle;
            
            v_TablaCodigosMensajes = SELECT 0 AS "IdInterno", 
                                    "Codigo" AS "Codigo",
                                    "CodigoDato1" as "CodigoDato1",
                                    "ValorDato1" as "ValorDato1",
                                    "CodigoDato2" as "CodigoDato2",
                                    "ValorDato2" as "ValorDato2"
                                    FROM :v_out_ErroresDetalle
                                    UNION ALL
                                    SELECT 0 AS "IdInterno", 
                                    "Codigo" AS "Codigo",
                                    "CodigoDato1" as "CodigoDato1",
                                    "ValorDato1" as "ValorDato1",
                                    "CodigoDato2" as "CodigoDato2",
                                    "ValorDato2" as "ValorDato2"
                                    FROM :v_out_AdvertenciasDetalle;
                                    
            CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,v_out_ErroresDetalleMensajes);
            
            p_out_ErroresDetalle = SELECT   DISTINCT "errores"."IdInterno",
                                            "errores"."Codigo",
                                            "msj"."Descripcion"
                                    FROM :v_out_ErroresDetalle AS "errores"
                                    INNER JOIN :v_out_ErroresDetalleMensajes AS "msj" ON "errores"."Codigo" = "msj"."Codigo"
                                    GROUP BY "errores"."IdInterno","errores"."Codigo","msj"."Descripcion";
            
            p_out_AdvertenciasDetalle = SELECT  "adv"."IdInterno",
                                                "adv"."Codigo",
                                                "adv"."RequiereAutorizacion",
                                                "msj"."Descripcion"
                                        FROM :v_out_AdvertenciasDetalle AS "adv"
                                        INNER JOIN :v_out_ErroresDetalleMensajes AS "msj" ON "adv"."Codigo" = "msj"."Codigo"; 
        END;
        
        p_out_AdvertenciasContador  := 0;
        p_out_ErroresContador       := 0;
        BEGIN
            DECLARE v_count_modulos                 BIGINT;
            DECLARE v_count_modulos_estado          BIGINT;
            DECLARE v_count_modulo_facturacion      BIGINT;
            DECLARE v_resultadoRegla                NVARCHAR(40);
            DECLARE v_count                         BIGINT;
            DECLARE v_count_familias_validacion     BIGINT;
            DECLARE v_count_sistema_entregado       BIGINT;
            DECLARE v_count_procesos_no_ejecutados  BIGINT;
            DECLARE v_count_valor_uf                BIGINT;
            DECLARE v_count_valor_do                BIGINT;
            DECLARE v_count_mesas_abiertas          BIGINT;
            DECLARE v_count_mesas_modulo            BIGINT;          
           
            DECLARE v_Modulo 						NVARCHAR(40);
           
            DECLARE v_DiaAnterior					NVARCHAR(10);
            DECLARE v_EsHabil						NVARCHAR(1);
           
            DECLARE v_out_AdvertenciasContadorDA 				BIGINT;
		    DECLARE v_out_AdvertenciasDetalleDA table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
			DECLARE v_out_ErroresContadorDA 					BIGINT;
	 		DECLARE v_out_ErroresDetalleDA table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
	 		DECLARE v_Valor 						NVARCHAR(2);
	 		
        
            CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
        
            CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'ABRIR MODULO APERTURA CIERRE','INICIO','INICIO');
                                    
            CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
            
            if :v_resultadoRegla = 'RECH' then
                v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo",'' as "CodigoDato1",
                                              '' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" 
                                       FROM :v_out_ErroresDetalle_Basicos;
                SIGNAL SALIDA_CONTROLADA;   
            end if;
        
            SELECT COUNT(1) AS "Count" into v_count_modulos
            FROM :p_Modulo;
            
            if (v_count_modulos = 0) THEN
                v_out_ErroresDetalle = SELECT 0 as "IdInterno", 'NO_SE_INGRESARON_MODULOS' AS "Codigo",'' as "CodigoDato1",
                                              '' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2"  FROM DUMMY;
                SIGNAL SALIDA_CONTROLADA;
            END IF;
            
            SELECT COUNT(1) AS "Count" into v_count_sistema_entregado
            FROM "BSCL.Plataforma.BD::PAU.Modulo" "M"
            WHERE "M"."IdEstado" = 'ENT'
            AND "CodigoModulo" = 'SIS';
            
            IF (v_count_sistema_entregado > 0) THEN
                v_out_ErroresDetalle = SELECT 0 as "IdInterno", 'NO_SE_PUEDE_ABRIR_SISTEMA_ENTREGADO' AS "Codigo",'' as "CodigoDato1",
                                              '' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2"  FROM DUMMY;
                SIGNAL SALIDA_CONTROLADA;
            END IF;
            
             v_modulo_validacion_estado = SELECT
                                            "p_Modulo"."CodigoModulo"
                                            ,"p_Modulo"."Estado"        
                                            ,"p_Modulo"."CodigoUsuario" 
                                            ,:p_IdEmpresa AS "IdEmpresa"
                                            ,CASE WHEN COALESCE("Modulo"."CodigoModulo",'-1') = '-1' THEN 'OK' ELSE 'ERR' END AS "Resultado"
                                        FROM :p_Modulo AS "p_Modulo"
                                        LEFT OUTER JOIN "BSCL.Plataforma.BD::PAU.Modulo" AS "Modulo" ON ("Modulo"."CodigoModulo" = "p_Modulo"."CodigoModulo" 
                                                                                              AND :p_IdEmpresa = "Modulo"."IdEmpresa"
                                                                                              AND "Modulo"."IdEstado" = "p_Modulo"."Estado");
            
            BEGIN
                v_out_ErroresDetalle = SELECT 0 AS "IdInterno", 'MODULO_YA_ABIERTO' as "Codigo",'CodigoModulo' as "CodigoDato1",
                                              "Modulos"."CodigoModulo" as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" 
                                       FROM :v_modulo_validacion_estado AS "Modulos" WHERE "Resultado" = 'ERR';
                
                SELECT COUNT(1) as "Count" INTO v_count_modulos_estado
                FROM :v_modulo_validacion_estado WHERE "Resultado" = 'OK';

                IF (v_count_modulos_estado = 0) THEN
                    SIGNAL SALIDA_CONTROLADA;
                END IF;
            END;
           
           --CONSULTA DIA ANTERIOR
           CALL "BSCL.Plataforma.BD.Servicios::ConsultarDiaAnterior" ( 
			     p_IdEmpresa=>:p_IdEmpresa
			    ,p_IdUsuario=>:p_IdUsuario
			    ,p_IdModulo=>'PLA'
			    ,p_UsaReglasNegocio=> 'N'
				                                       
			    ,p_Fecha=>:v_FechaSistema
			    ,p_NumeroDias=>1
			    ,p_EsHabil=>'S'
				,p_IdPais=>44
				                                    
				,p_out_FechaSiguiente=> v_DiaAnterior
				,p_out_EsHabil=> v_EsHabil
				                                       
				,p_out_AdvertenciasContador=> v_out_AdvertenciasContadorDA
				,p_out_AdvertenciasDetalle=> v_out_AdvertenciasDetalleDA
				,p_out_ErroresContador=> v_out_ErroresContadorDA
				,p_out_ErroresDetalle=> v_out_ErroresDetalleDA
				                                       
			); 
           
			
            SELECT count(1) INTO v_count_modulo_facturacion FROM :p_Modulo  WHERE "CodigoModulo" = 'FAC';
             
			if (v_count_modulo_facturacion > 0) THEN
				DELETE FROM "BSCL.Plataforma.BD::PAU.ControlProceso" 
								  	WHERE "CodigoProceso" = 'CUADRATURA_FACTURACION' 
								  	AND "FechaSistema" = :v_FechaSistema
								  	AND :p_IdEmpresa = "IdEmpresa";
			END IF;
            
            
            SELECT COUNT(1) INTO v_count_mesas_modulo FROM :p_Modulo WHERE "CodigoModulo" IN ('MRF','MSP','MFW');
            
            IF(v_count_mesas_modulo > 0) THEN 
            
            	SELECT "Valor"  INTO v_Valor FROM  "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "CodigoParametro"='PLA.LocalizacionSistema' AND "IdEmpresa"=:p_IdEmpresa;     
              
                -- Validar Valor UF
                SELECT COUNT("Valor") INTO v_count_valor_uf FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero" WHERE "IdParametroFinanciero" = 'UF' AND "Fecha" = :v_FechaSistema;
               
                IF (v_count_valor_uf = 0) THEN
                   v_out_ErroresDetalle = SELECT 0 AS "IdInterno", 'NO_HAY_VALOR_UF' as "Codigo",'Fecha' as "CodigoDato1",
                                          :v_FechaSistema as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" FROM dummy;   
                   SIGNAL SALIDA_CONTROLADA;
                END IF;

                --Validar Dolar Observado
                SELECT COUNT("Valor") INTO v_count_valor_do FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero" WHERE "IdParametroFinanciero" = 'DO' AND "Fecha" = :v_FechaSistema;
            
                IF (v_count_valor_do = 0) THEN
                    v_out_ErroresDetalle = SELECT 0 AS "IdInterno", 'NO_HAY_VALOR_DOLAR_OBSERVADO' as "Codigo",'Fecha' as "CodigoDato1",
                                           :v_FechaSistema as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" FROM dummy;  
                    SIGNAL SALIDA_CONTROLADA;
                END IF;
                
                --VALIDAR VALOR UF MESAS
                SELECT COUNT(CASE WHEN COALESCE("TipoCambioMercado",0) > 0 THEN 1 ELSE NULL END) INTO v_count_valor_uf FROM "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" WHERE "Fecha" BETWEEN TO_SECONDDATE(:v_FechaSistema) AND ADD_SECONDS(TO_SECONDDATE(:v_FechaSistema),86399) AND "IdMoneda" = 'UF';
                 IF (v_count_valor_uf = 0) THEN
                   v_out_ErroresDetalle = SELECT 0 AS "IdInterno", 'NO_HAY_VALOR_UF' as "Codigo",'Fecha' as "CodigoDato1",
                                          :v_FechaSistema as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" FROM dummy;   
                   SIGNAL SALIDA_CONTROLADA;
                END IF;
                
                --VALIDAR VALOR DOLAR OBSERVADO MESAS
                SELECT COUNT(CASE WHEN COALESCE("TipoCambioMercado",0) > 0 THEN 1 ELSE NULL END) INTO v_count_valor_do FROM "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" WHERE "Fecha" BETWEEN TO_SECONDDATE(:v_FechaSistema) AND ADD_SECONDS(TO_SECONDDATE(:v_FechaSistema),86399) AND "IdMoneda" = 'DO';
                IF (v_count_valor_do = 0) THEN
                    v_out_ErroresDetalle = SELECT 0 AS "IdInterno", 'NO_HAY_VALOR_DOLAR_OBSERVADO' as "Codigo",'Fecha' as "CodigoDato1",
                                           :v_FechaSistema as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" FROM dummy;  
                    SIGNAL SALIDA_CONTROLADA;
                END IF;
                
                -- Validar que mesa asociada al módulo no este abierta 
                SELECT COUNT("IdMesa") INTO v_count_mesas_abiertas FROM "BSCL.Mesa.BD.Vistas::ConsultarMesas" MES
                    INNER JOIN "BSCL.Plataforma.BD.Vistas::PAU.ConsultarModulosDetalle" MDL ON MDL."CodigoModulo" = MES."CodigoModulo" 
                    INNER JOIN :p_Modulo P ON P."CodigoModulo" = MES."CodigoModulo" 
                    AND MDL."IdEmpresa" = :p_IdEmpresa
                    WHERE MES."IdEstadoMesa" = 'A' AND P."CodigoModulo" IN ('MRF','MSP','MFW');
                     
                IF (v_count_mesas_abiertas > 0 AND p_MesaAbierta != 'S') THEN
                    v_out_ErroresDetalle = SELECT 0 AS "IdInterno", 'MODULO_CON_MESA_ABIERTA' as "Codigo",'' as "CodigoDato1",
                                            '' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" FROM dummy;
                    SIGNAL SALIDA_CONTROLADA;
                END IF;
               
            END IF;
           

            
            UPDATE "Modulo"
            SET    "Modulo"."IdEstado" = "PM"."Estado",
                   "Modulo"."CodigoUsuario" = "PM"."CodigoUsuario",
                   "Modulo"."FechaUltimaActualizacion" = CURRENT_UTCTIMESTAMP
            FROM "BSCL.Plataforma.BD::PAU.Modulo" "Modulo"
            JOIN :p_Modulo "PM" ON ("Modulo"."CodigoModulo" = "PM"."CodigoModulo");
            
            INSERT INTO "BSCL.Plataforma.BD::PLA.MensajeWebsocket"(
              "IdEmpresa", 
              "FechaRegistro",
              "Evento",
              "DATA",
              "EsProcesado" 
            )
            SELECT :p_IdEmpresa,
                   CURRENT_UTCTIMESTAMP,
                   'AVISO',
                   'El modulo ' || "ModuloPla"."Nombre" || ' ha sido abierto por el usuario ' || "p_Modulo"."CodigoUsuario",
                   'N'
            FROM :p_Modulo AS "p_Modulo"
            LEFT OUTER JOIN "BSCL.Plataforma.BD::PAU.Modulo" AS "Modulo" ON ("Modulo"."CodigoModulo" = "p_Modulo"."CodigoModulo" 
                                                                             AND :p_IdEmpresa = "Modulo"."IdEmpresa"
                                                                             AND "Modulo"."IdEstado" = "p_Modulo"."Estado")
            LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Modulo" AS "ModuloPla" ON ("ModuloPla"."IdModulo" = "Modulo"."CodigoModulo");
            
            p_out_Resultado = SELECT "Modulo"."CodigoModulo",
                                     "Modulo"."IdEstado",
                                     "ModuloPla"."Nombre" AS "NombreModulo"
                              FROM "BSCL.Plataforma.BD::PAU.Modulo" "Modulo"
                              JOIN :p_Modulo ON ("Modulo"."CodigoModulo" = :p_Modulo."CodigoModulo")
                              LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Modulo" AS "ModuloPla" ON ("ModuloPla"."IdModulo" = "Modulo"."CodigoModulo");
                                
            v_log = SELECT 
                    0 AS "IdCabeceraLog"
                    ,'APERTURA' AS "IdTipoLog"
                    ,'PLA' AS "IdModulo"
                    ,:p_IdUsuario AS "IdUsuario"
                    ,:p_IdEmpresa AS "IdEmpresa"
                    ,:v_FechaSistema AS "FechaSistemaLog"
                    ,:p_IdUsuario AS "IdUsuarioAtendido"
                    ,0 AS "IdPersona"
                    ,0 AS "IdCliente"
                    ,'' AS "EstadoCliente"
                    ,'' AS "NombreCliente"
                    ,'' AS "IdTipoDocumentoIdentidad"
                    ,'' AS "IdDocumentoIdentidad"
                    ,0 AS "IdPortafolio"
                    ,'' AS "EstadoPortafolio"
                    ,'ABRIR_MODULO' AS "IdTipoRegistro"
                    ,0 AS "IdRegistro"
                    ,"Estado" AS "EstadoRegistro"
                    ,'' AS "IdTipoOperacion"
                    ,0 AS "Folio"
                    ,0 AS "Cantidad"
                    ,0 AS "PrecioTasa"
                    ,0 AS "Monto"
                    ,'' AS "IdMonedaOrigen"
                    ,'' AS "IdMonedaLiquidacion"
                    ,'' AS "TipoCambio"
                    ,'' AS "Nemo"
                    ,0 AS "IdProceso"
                    ,'' AS "EstadoProceso"
                    ,'' AS "NombreProceso"
                FROM :v_modulo_validacion_estado;
            
            v_DETALLELOG = SELECT 0 AS "IdCabeceraLog",'' as "SubRegistro", '' as "CampoRegistro", '' as "ValorAntiguo", '' as "ValorNuevo" from dummy where 1=2;
        
            CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log, p_LogDetalle => :v_DETALLELOG);
            
            CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'ABRIR MODULO APERTURA CIERRE','FIN','FIN');
                                
            SIGNAL SALIDA_CONTROLADA;
        END;
    END;
END;
GO
