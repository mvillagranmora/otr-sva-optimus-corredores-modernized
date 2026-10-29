--Obtener las notificaciones no enviadas y que deban ser reenviadas:
--cliente , se validan sus configuraciones
--portafolio, se validan las configuraciones de su portafolio
--empleado, no tiene configuracion adicional, solo se valida que el correo corresponda a la persona del empleado
SELECT "IdEnvio", "MensajeCuerpo", "Remitente", "Asunto"
FROM
(
    SELECT DISTINCT
        CASE WHEN CE."Agrupa" = 'N' THEN E."IdEvento" ELSE N."IdNotificacion" END AS "IdEnvio",
        to_char(E."MensajeBody") as "MensajeCuerpo", 
        'emisor' as "Remitente",
        P."Asunto",
        CASE
            WHEN CE."PermitePersonalizar" = 'N' THEN 'S' --VER SI ES MEJOR PONER OR 'TIPOENTIDAD'='EMP'
            ELSE
                CASE WHEN COALESCE(CCEP."IdConfiguracionEvento",0)=0 THEN 'S' -- se puede personalizar, pero no se ha personalizado el cliente.
                ELSE
                    CASE WHEN COALESCE(CCEP."IdEntidad",0)=0 THEN 'N'
                    ELSE
                        CASE WHEN CCEP."EstadoConfiguracionPortafolio" = 'INA' THEN 'N'
                        ELSE
                            CASE WHEN CCEP."SuspensionTemporal" = 'N' THEN 'S'
                            ELSE
                                CASE WHEN CURRENT_UTCTIMESTAMP BETWEEN CCEP."FechaInicioSuspension" AND CCEP."FechaTerminoSuspension" THEN 'N'
                                ELSE 'S'
        END END END END END END AS "Valido"

    FROM "BSCL.Plataforma.BD::EVT.Notificacion" N
    INNER JOIN "BSCL.Plataforma.BD::EVT.NotificacionDetalle" ND ON ND."IdNotificacion" = N."IdNotificacion" AND (N."Enviado"='N' OR ( N."ReEnviar"='S' AND N."ReEnviado" = 'N'))
    INNER JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" CE ON CE."IdConfiguracionEvento" = N."IdConfiguracionEvento"
    INNER JOIN "BSCL.Plataforma.BD::EVT.Evento" E ON ND."IdEvento" = E."IdEvento"  AND E."MensajeBody" is not null
    INNER JOIN "BSCL.Plataforma.BD::EVT.EventoDirecciones" ED ON ND."IdEvento" = ED."IdEvento"
    INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" CPDE ON CPDE."IdDireccionElectronica" =  ED."IdDireccionElectronica" 
    INNER JOIN "BSCL.Plataforma.BD::EVT.Plantilla" P ON P."IdPlantilla" = CE."IdPlantilla"
    LEFT JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarConfiguracionEventoEntidad" CCEP ON (CCEP."TipoEntidad" ='POR' OR CCEP."TipoEntidad" ='CLI') AND CCEP."IdConfiguracionEvento" = N."IdConfiguracionEvento"
    WHERE N."IdNotificacion" IN (:Notificaciones)
    AND CE."ControlProceso" = 'N' OR (CE."ControlProceso" = 'S' AND N."Enviar" = 'S')
) WHERE "Valido" = 'S';
