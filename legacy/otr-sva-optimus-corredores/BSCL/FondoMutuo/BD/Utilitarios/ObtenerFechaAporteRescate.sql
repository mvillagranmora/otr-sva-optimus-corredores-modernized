SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.FondoMutuo.BD.Utilitarios::ObtenerFechaAporteRescate";
GO
CREATE PROCEDURE
-- 
--  Proposito: Obtener el siguiente dia de corrido o habil, para distintos paises, segun fecha enviada.
--  Autor: - OCT - Oscar Carreno Tolmo
--  Fecha Creacion: 30/11/2017
--   
"BSCL.FondoMutuo.BD.Utilitarios::ObtenerFechaAporteRescate" (
    --IN p_DiaHabil   NVARCHAR(1) DEFAULT 'S', --SOLO DIAS HABILES
    IN p_FechasIN   TABLE (
                            "IdInterno"     BIGINT,
                            "Fecha"         DATE,
                            "NumeroDias"    BIGINT,
                            "EsDiaHabil"    NVARCHAR(1),
                            "IdPaisLocal"   BIGINT,
                            "IdPaisEmisor"  BIGINT,
                            "IdPaisFondo"   BIGINT,
                            "IdPaisMoneda"  BIGINT),
    OUT p_FechasOUT TABLE (
                            "IdInterno"     BIGINT,
                            "Fecha"         DATE,
                            "NumeroDias"    BIGINT,
                            "FechaFinal"    DATE,
                            "EsDiaHabil"    NVARCHAR(1)),
    OUT p_FeriadosOUT  TABLE (
                            "IdInterno"     BIGINT,
                            "Fecha"         DATE,
                            "IdPais"        BIGINT,
                            "NombrePais"    NVARCHAR(100),
                            "DescripcionDia" NVARCHAR(100))
)
    LANGUAGE SQLSCRIPT
    SQL SECURITY INVOKER 
    DEFAULT SCHEMA BSCL
    READS SQL DATA AS
BEGIN
    DECLARE v_min_date DATE;
    DECLARE v_max_date DATE;
    DECLARE v_fechas TABLE ("IdInterno" BIGINT,"Fecha" DATE,"NumeroDias" BIGINT,"FechaDiasCorrido" DATE,"FechaFinal" DATE);
    DECLARE v_calendario_inhabiles TABLE ("Fecha" DATE,"IdPais" BIGINT,"DescripcionDia" NVARCHAR(100));
    DECLARE v_calendario_habiles TABLE ("Fecha" DATE,"IdInterno" BIGINT);
    DECLARE v_fechasPais TABLE ("IdInterno" BIGINT,"IdPais" BIGINT);
    DECLARE v_fechasOUT TABLE ("IdInterno" BIGINT,"Fecha" DATE,"NumeroDias" BIGINT,"FechaDiasCorrido" DATE,"FechaFinal" DATE);
                            
    v_calendario_inhabiles =
        SELECT '0001-01-01' AS "Fecha",0 AS "IdPais",'' AS "DescripcionDia" FROM DUMMY WHERE 1=2;
    
    v_fechas =
        SELECT 0 AS "IdInterno",'0001-01-01' AS "Fecha",0 AS "NumeroDias",'0001-01-01' AS "FechaDiasCorrido",'0001-01-01' AS "FechaFinal" FROM DUMMY WHERE 1=2;

    v_calendario_habiles =
        SELECT '0001-01-01' AS "Fecha",0 AS "IdInterno" FROM DUMMY WHERE 1=2;
        
    v_fechas =
        SELECT 
            "IdInterno","Fecha","NumeroDias",ADD_DAYS (TO_DATE ("Fecha",'YYYY-MM-DD'), "NumeroDias") AS "FechaDiasCorrido",
            ADD_DAYS (TO_DATE ("Fecha",'YYYY-MM-DD'), "NumeroDias") AS "FechaFinal"
        FROM :p_FechasIN;
    
    v_fechasPais =
        SELECT 0 AS "IdInterno",0 AS "IdPais" FROM DUMMY WHERE 1=2;

    v_fechasOUT = 
        SELECT 0 AS "IdInterno",'0001-01-01' AS "Fecha",0 AS "NumeroDias",'0001-01-01' AS "FechaDiasCorrido",'0001-01-01' AS "FechaFinal" FROM DUMMY WHERE 1=2;
    
    v_fechasPais =
        SELECT
            "IdInterno","Valor" AS "IdPais"
        FROM(
            SELECT
                "IdInterno",
                COALESCE(
                    "1" ||"IdPaisLocal", "2" ||"IdPaisEmisor", "3" ||"IdPaisFondo",
                    "4" ||"IdPaisMoneda") AS "Valor",
                COALESCE(
                    "1" ||'IdPaisLocal', "2" ||'IdPaisEmisor', "3" ||'IdPaisFondo',
                    "4" ||'IdPaisMoneda') AS "Llave"    
             FROM :p_FechasIN,"BSCL.Plataforma.BD::PLA.Identidad"
        )
        WHERE "Valor" IS NOT NULL;   
    
    --SELECT * FROM :v_fechasPais; --BORRAR
                            
    --Se obtiene la minima y maxima fecha, de los datos de entrada enviados, para crear el calendario
    SELECT
        ADD_DAYS (TO_DATE ( MIN("Fecha") ,'YYYY-MM-DD') , -1), ADD_DAYS (TO_DATE ( MAX("FechaFinal") ,'YYYY-MM-DD') , 30) INTO v_min_date, v_max_date
    FROM :v_fechas;
    
    --SE genra el calendario
    v_calendario =
        SELECT
            GENERATED_PERIOD_START AS "Fecha" 
        FROM SERIES_GENERATE_DATE('INTERVAL 1 DAY', :v_min_date, :v_max_date);
        
    --IF (:p_DiaHabil = 'S') THEN
        --Se obtienen los dias inhabiles y feriados para los paises enviados como entrada
        v_calendario_inhabiles =
            SELECT
                cal."Fecha",fer."IdPais",fer."DescripcionDia"
            FROM :v_calendario cal,"BSCL.Plataforma.BD::REG.DiaFeriado" fer 
            WHERE fer."IdPais" IN (SELECT DISTINCT "IdPais" FROM :v_fechasPais)
              AND ((WEEKDAY (cal."Fecha") = fer."Dia" and fer."Tipo" = 'INHABIL') OR (fer."Tipo" ='FERIADO' and cal."Fecha" = fer."Fecha")) AND  fer."IdEstado"='ACT';
    --END IF;
    
    --Se obtienen los fechas habiles por registro de la tabla de entrada, realizando un except entre el calendario generado y los dias inhabiles y feriados por
    --pais
    v_calendario_habiles =
        SELECT c."Fecha",p."IdInterno" FROM :v_calendario c,:p_FechasIN p
        EXCEPT 
        SELECT c."Fecha",p."IdInterno"
        FROM :v_calendario_inhabiles AS c
        INNER JOIN :v_fechasPais AS p ON (p."IdPais" = c."IdPais")
        INNER JOIN :p_FechasIN AS f ON (f."IdInterno" = p."IdInterno"
                                    AND f."EsDiaHabil" = 'S');--NUEVO
    
    --Se genera una secuencia de fechas
    v_DiasSolicitadosConDiasHabiles =
        SELECT
            h."Fecha",s."IdInterno",(ROW_NUMBER() over (PARTITION BY s."IdInterno" ORDER BY h."Fecha")-1) AS "NumeroDias"
        FROM :v_fechas AS s,:v_calendario_habiles AS h
        WHERE h."Fecha" >= s."Fecha"
          AND h."IdInterno" = s."IdInterno";

    v_fechasOUT =
        SELECT 
            s."IdInterno",s."Fecha",s."NumeroDias",s."FechaDiasCorrido",d."Fecha" AS "FechaFinal"
        FROM :v_fechas AS s
        LEFT JOIN :v_DiasSolicitadosConDiasHabiles AS d ON (d."IdInterno" = s."IdInterno" 
                                                        AND d."NumeroDias" = s."NumeroDias");
    
    p_FeriadosOUT =
        SELECT
            f."IdInterno",f."Fecha",f."IdPais",p."NombrePais",f."DescripcionDia"
        FROM
        (
            SELECT
                f."IdInterno",i."Fecha",i."IdPais",i."DescripcionDia"
            FROM :v_calendario_inhabiles AS i,:v_fechasOUT AS f
            WHERE (i."Fecha" >= f."FechaDiasCorrido" AND i."Fecha" <= f."FechaFinal")
        ) AS f
        INNER JOIN (SELECT DISTINCT "IdInterno","IdPais" FROM :v_fechasPais) AS fp ON (fp."IdInterno" = f."IdInterno"
                                                                                   AND fp."IdPais" = f."IdPais")
        INNER JOIN "BSCL.Plataforma.BD::REG.Pais" AS p on (p."IdPais" = fp."IdPais")
        ORDER BY f."IdInterno",f."Fecha",p."NombrePais";
    
    p_FechasOUT =
        SELECT 
            f."IdInterno",f."Fecha",f."NumeroDias",f."FechaFinal",CASE WHEN COALESCE(fe."IdInterno",-1) = -1 THEN 'S' ELSE 'N' END AS "EsDiaHabil"
        FROM :v_fechasOUT AS f
        LEFT JOIN (SELECT DISTINCT "IdInterno","Fecha" FROM :p_FeriadosOUT) AS fe ON (fe."IdInterno" = f."IdInterno"
                                                                                  AND fe."Fecha" = f."FechaFinal");
END;
GO