select current_timestamp,start_time as startt,floor(cpu_time/1000) as "CPUTIME(MS)",floor(duration_microsec/1000) as "DURATION(MS)",records,db_user,operation,statement_string,memory_size,*
from M_EXPENSIVE_STATEMENTS 
where start_time >= '2022-01-26 07:00:00'
and records > 1000000
AND db_user = 'JOBS'
--and cpu_time > 60000000
--and floor(cpu_time/1000) > 8000 or floor(duration_microsec/1000) > 8000 or records > 2000
ORDER BY startt desc

SELECT * from M_EXPENSIVE_STATEMENTS WHERE
connection_id = 305758
ORDER BY start_time DESC

select start_time,floor(cpu_time/1000) as "CPUTIME(MS)",floor(duration_microsec/1000) as "DURATION(MS)",records,db_user,operation,statement_string,memory_size
from M_EXPENSIVE_STATEMENTS 
where start_time >= '2021-04-26 07:00:00'
--and records > 10000
--and cpu_time > 60000000
--and floor(cpu_time/1000) > 8000 or floor(duration_microsec/1000) > 10000-- or records > 2000
and statement_string like '%ConsultarClientePortafolioUI%'
ORDER BY START_TIME desc

START_TIME;CPUTIME(MS);DURATION(MS);RECORDS;DB_USER;OPERATION;STATEMENT_STRING;MEMORY_SIZE
26-04-2021 12:41:10.561521;77.644;70.403;4.535.700;JOBS;AGGREGATED_EXECUTION;SELECT "Agrupa", ce."IdConfiguracionEvento", cte."CodigoTipoEvento","Llave", CASE WHEN "ValorBlob" IS NOT NULL THEN TO_NCLOB("ValorBlob") ELSE TO_NCLOB("Valor") END AS "Valor",e."IdEvento","IdPersonaDestinatario"  FROM BSCL."BSCL.Plataforma.BD::EVT.Evento" e LEFT JOIN "BSCL"."BSCL.Plataforma.BD::EVT.EventoDetalle" ed ON ed."IdEvento" = e."IdEvento" LEFT JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionTipoEvento" cte ON cte."CodigoTipoEvento" = e."CodigoTipoEvento" LEFT JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEventoDetalle" ced ON ced."CodigoTipoEvento" = cte."CodigoTipoEvento"  LEFT JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" ce ON ce."IdConfiguracionEvento" = ced."IdConfiguracionEvento" WHERE e."Procesado" = 'N' AND ed."IdEvento" IS NOT NULL AND cte."CodigoTipoEvento" IS NOT NULL AND ced."CodigoTipoEvento" IS NOT NULL AND ce."IdConfiguracionEvento" IS NOT NULL ORDER BY e."IdEvento" DESC;9.735.837.528

START_TIME;CPUTIME(MS);DURATION(MS);RECORDS;DB_USER;OPERATION;STATEMENT_STRING;MEMORY_SIZE
26-04-2021 12:46:12.094221;42.504;43.422;0;APPJAVA;AGGREGATED_EXECUTION;SELECT
       CAR."IdClasificacionTipoInstrumento"
       ,CAR."IdTipoInstrumento"
       ,CAR."IdInstrumento"
       ,Case when CAR."IdClasificacionTipoInstrumento" = 'IF' then CAR."NemoSVS" else CAR."Nemotecnico" end as "Nemotecnico"
       ,CASE WHEN coalesce(CAR."IdEmisor",inst3."IdEmisor",'') <> '' THEN coalesce(CAR."IdEmisor",inst3."IdEmisor",'') ELSE inst3."IdEmisor" END AS "IdEmisor"
       ,case when CAR."IdClasificacionTipoInstrumento" = 'IF' then CAR."EsFungible" else 'N' end as "EsFungible"
       ,CAR."IdCliente"
       ,CLI."IdPersona"
       ,CAR."IdPortafolio"
       ,COALESCE(MADOR."NombreProductoAgrupador", 'Otros (No APV)') AS "Producto"
       ,CASE WHEN ?='N' THEN ? ELSE COALESCE(CAR."IdBoveda",?) END AS "IdBoveda" 
       ,CASE WHEN ?='N' THEN  0 ELSE COALESCE(CCPI."IdCuentaCustodioPortafolio",0) END AS "IdCuentaCustodioPortafolio"
       ,CASE WHEN ?='N' THEN '' ELSE COALESCE(CCPI."Codigo",'') END AS "NumeroCuentaBoveda"      
        ,CAST(IFNULL(CASE WHEN CAR."IdClasific;356.212.744
        
        
        START_TIME;CPUTIME(MS);DURATION(MS);RECORDS;DB_USER;OPERATION;STATEMENT_STRING;MEMORY_SIZE
26-04-2021 13:16:36.372158;8.969;3.120;5;APPJAVA;AGGREGATED_EXECUTION;select * from "BSCL"."BSCL.Portafolio.BD.Vistas::ConsultarClientePortafolioUI"(PLACEHOLDER."$$p_idempresa$$"=>1,PLACEHOLDER."$$p_idusuario$$"=>36,PLACEHOLDER."$$p_idmodulo$$"=>'PLA',PLACEHOLDER."$$p_idempleado$$"=>827,PLACEHOLDER."$$p_ignorafiltro$$"=>'N') where  (  LOCATE (UCASE("NombreCompletoPortafolio"), ?) > 0  )  order by "NombreCompleto","CodigoPortafolioPad" limit 100;1.181.860.012

CURRENT_TIMESTAMP;START_TIME;CPUTIME(MS);DURATION(MS);RECORDS;DB_USER;OPERATION;STATEMENT_STRING;MEMORY_SIZE
29-04-2021 15:12:56.487;29-04-2021 15:01:44.549559;316;329;32.766;APPJAVA;FETCH;select * from "BSCL"."BSCL.Portafolio.BD.Vistas::ConsultarPortafolio"  order by "CodigoPortafolio";950.109.104

CURRENT_TIMESTAMP;STARTT;CPUTIME(MS);DURATION(MS);RECORDS;DB_USER;OPERATION;STATEMENT_STRING;MEMORY_SIZE;HOST;PORT;CONNECTION_ID;TRANSACTION_ID;UPDATE_TRANSACTION_ID;STATEMENT_ID;STATEMENT_EXECUTION_ID;PARENT_STATEMENT_ID;STATEMENT_HASH;DB_USER;SCHEMA_NAME;APP_USER;START_TIME;DURATION_MICROSEC;OBJECT_NAME;OPERATION;RECORDS;STATEMENT_STRING;PARAMETERS;ERROR_CODE;ERROR_TEXT;LOCK_WAIT_COUNT;LOCK_WAIT_DURATION;ALLOC_MEM_SIZE_ROWSTORE;ALLOC_MEM_SIZE_COLSTORE;MEMORY_SIZE;REUSED_MEMORY_SIZE;CPU_TIME;PASSPORT_ROOT_CONTEXT_ID;PASSPORT_TRANSACTION_ID;PASSPORT_CONNECTION_ID;PASSPORT_CONNECTION_COUNTER;STATEMENT_START_TIME;APPLICATION_SOURCE;APPLICATION_NAME;NETWORK_MESSAGE_ID;WORKLOAD_CLASS_NAME;PRIORITY;STATEMENT_THREAD_LIMIT;STATEMENT_MEMORY_LIMIT
30-04-2021 10:06:35.219;30-04-2021 10:05:07.827708;145;141;10.406;JOBS;FETCH;SELECT "Agrupa", ce."IdConfiguracionEvento", cte."CodigoTipoEvento","Llave", CASE WHEN "ValorBlob" IS NOT NULL THEN TO_NCLOB("ValorBlob") ELSE TO_NCLOB("Valor") END AS "Valor",e."IdEvento","IdPersonaDestinatario"  FROM BSCL."BSCL.Plataforma.BD::EVT.Evento" e LEFT JOIN "BSCL"."BSCL.Plataforma.BD::EVT.EventoDetalle" ed ON ed."IdEvento" = e."IdEvento" LEFT JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionTipoEvento" cte ON cte."CodigoTipoEvento" = e."CodigoTipoEvento" LEFT JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEventoDetalle" ced ON ced."CodigoTipoEvento" = cte."CodigoTipoEvento"  LEFT JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" ce ON ce."IdConfiguracionEvento" = ced."IdConfiguracionEvento" WHERE e."Procesado" = 'N' AND ed."IdEvento" IS NOT NULL AND cte."CodigoTipoEvento" IS NOT NULL AND ced."CodigoTipoEvento" IS NOT NULL AND ce."IdConfiguracionEvento" IS NOT NULL ORDER BY e."IdEvento" DESC;10.371.233.320;bsxprsurhno1;30.403;351.384;138;1.307.506.405;1509184642038526;562.963.318.860.387;0;850e8648de63f00157a28d217221293a;JOBS;BSCL;;30-04-2021 10:05:07.827708;141.918;BSCL.BSCL.Plataforma.BD::EVT.ConfiguracionEvento,BSCL.BSCL.Plataforma.BD::EVT.ConfiguracionEventoDetalle,BSCL.BSCL.Plataforma.BD::EVT.ConfiguracionTipoEvento,BSCL.BSCL.Plataforma.BD::EVT.Evento,BSCL.BSCL.Plataforma.BD::EVT.EventoDetalle;FETCH;10.406;SELECT "Agrupa", ce."IdConfiguracionEvento", cte."CodigoTipoEvento","Llave", CASE WHEN "ValorBlob" IS NOT NULL THEN TO_NCLOB("ValorBlob") ELSE TO_NCLOB("Valor") END AS "Valor",e."IdEvento","IdPersonaDestinatario"  FROM BSCL."BSCL.Plataforma.BD::EVT.Evento" e LEFT JOIN "BSCL"."BSCL.Plataforma.BD::EVT.EventoDetalle" ed ON ed."IdEvento" = e."IdEvento" LEFT JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionTipoEvento" cte ON cte."CodigoTipoEvento" = e."CodigoTipoEvento" LEFT JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEventoDetalle" ced ON ced."CodigoTipoEvento" = cte."CodigoTipoEvento"  LEFT JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" ce ON ce."IdConfiguracionEvento" = ced."IdConfiguracionEvento" WHERE e."Procesado" = 'N' AND ed."IdEvento" IS NOT NULL AND cte."CodigoTipoEvento" IS NOT NULL AND ced."CodigoTipoEvento" IS NOT NULL AND ce."IdConfiguracionEvento" IS NOT NULL ORDER BY e."IdEvento" DESC;;0;;0;0;-1;-1;10.371.233.320;-1;145.678;;;;0;30-04-2021 10:01:55.738557;;;359.249;_SYS_DEFAULT;5;0;80
