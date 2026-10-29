SET SCHEMA BSCL;

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::BAT.ObtenerSiguienteFechaAgendada";

CREATE PROCEDURE
-- 
--  Proposito: Obtiene la siguiente fecha agendada según la agenda
--  Autor: - BCS 
--  Fecha Creacion: 22/10/2015
--   
"BSCL.Plataforma.BD.Servicios::BAT.ObtenerSiguienteFechaAgendada"
 (
                                    IN p_IdEmpresa                              BIGINT,
                                    IN p_IdUsuario                              BIGINT,
                                    IN p_IdModulo                               NVARCHAR(3),
                                                                     
                                                                    
                                    IN  p_IdAgenda                              BIGINT,
                                    out p_Fecha_Salida                          table("FechaAgendar" BIGINT),
                                    OUT p_out_msg                               nvarchar(540)
-- Salidas para mensajes de advertencias y errores                                  
) 
    LANGUAGE SQLSCRIPT
    SQL SECURITY INVOKER 
    DEFAULT SCHEMA "BSCL"
AS
BEGIN 
 
 
        declare v_IdPaisSistema BIGINT;
        declare v_IdEmpresa BIGINT;
        declare v_IdUsuario BIGINT;
        declare v_IdModulo   NVARCHAR(3);
        declare contt BIGINT;
        declare v_ExisteAgenda INT;
        declare v_Comodin       NVARCHAR(2);
        declare v_DiaDeMes                  NVARCHAR(2); 
        declare v_DiaDeSemana               NVARCHAR(1);
        declare v_Mes                       NVARCHAR(2);
        declare v_Hora                      NVARCHAR(2);
        declare v_Minuto                    NVARCHAR(2);
        declare p_DiaDeMes                  INT; 
        declare p_DiaDeSemana               INT;
        declare p_Mes                       INT;
        declare p_Hora                      INT;
        declare p_Minuto                    INT;
        declare v_contadorMesDia                    INT;
        declare v_contadorHora              INT;
        declare v_contadorMinuto            INT;
        declare v_existePrograma            INT;
        declare v_IdAgenda                  BIGINT;
        declare v_IdPrograma                BIGINT;
        declare v_dateSeconds               BIGINT;
        declare v_FechaEjecucionAntigua     NVARCHAR(40);
        declare v_FechaEjecucionNueva       NVARCHAR(40);
        declare fecha_auxiliar              NVARCHAR(40);
        declare v_SoloDiasHabiles           NVARCHAR(40);
        declare v_SoloFinDeMes              NVARCHAR(40);
        declare v_SoloPrincipioDeMes        NVARCHAR(40);
        declare v_habilitado                NVARCHAR(40);
        declare v_diaHabilResultado         NVARCHAR(1);    
        declare v_diadesemanaresultado      BIGINT;  
        declare v_fechaEjecucionUltima      NVARCHAR(40);
        declare p_FechaSalida               NVARCHAR(80);
        declare pSiguienteFechaDeEjecucion  NVARCHAR(80);
                
        declare diasentre                   BIGINT;
        declare ZonaHoraria                 NVARCHAR(80);
        declare fechaLocal                  timestamp;
            
        v_SoloDiasHabiles   :='-1'      ;
        v_SoloFinDeMes      :='-1'      ;
        v_SoloPrincipioDeMes    :='-1'  ;
        v_habilitado        :='-1'      ; 
        p_DiaDeMes := 0; 
        p_DiaDeSemana:= 0;
        p_Mes:= 0;
        P_Hora:= 0;
        p_Minuto:= 0;
        v_FechaEjecucionAntigua :='';
        v_IdEmpresa := '1';
        v_IdUsuario := '1';
        v_IdModulo :='ORD';

        --FECHA LOCAL       
        --SELECT "Valor" INTO ZonaHoraria FROM "BSCL.Plataforma.BD::PLA.Configuracion" where "CodigoParametro" = 'PLA.ZonaHoraria' AND "IdEmpresa" = :p_IdEmpresa;     
        --fechaLocal := "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaLocal"(CURRENT_UTCTIMESTAMP,:ZonaHoraria);
        
        SELECT UTCTOLOCAL (TO_TIMESTAMP(CURRENT_UTCTIMESTAMP, 'YYYY-MM-DD HH24:MI:SS.FF7'), 'America/Santiago','platform') AS "Fecha"
        into fechaLocal
		from dummy;
        
        select 'fechalocal' , :fechaLocal from dummy;
        
        --ID PAIS
        SELECT "IdPais" INTO v_IdPaisSistema FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" WHERE "IdEmpresa" = :p_IdEmpresa;
            
            select count(1) into    v_existePrograma from "BSCL.Plataforma.BD.Vistas::BAT.ConsultarPrograma" programa
            
                where programa."IdAgenda" = p_IdAgenda ;
                
                IF :v_existePrograma > 0 THEN                   
                select distinct "SiguienteFechaDeEjecucion" into    pSiguienteFechaDeEjecucion from "BSCL.Plataforma.BD.Vistas::BAT.ConsultarAgenda"    
                where "IdAgenda" = p_IdAgenda ;
                
                --v_fechaEjecucionUltima := "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaLocal"(pSiguienteFechaDeEjecucion,:ZonaHoraria);
                
                SELECT UTCTOLOCAL (TO_TIMESTAMP(pSiguienteFechaDeEjecucion, 'YYYY-MM-DD HH24:MI:SS.FF7'), 'America/Santiago','platform') AS "Fecha"
		        into v_fechaEjecucionUltima
				from dummy;
         
            END IF;
            
            
            
             
         select 
         "DiaDeMes",
         "DiaDeSemana",
         "Mes",
         "Hora", 
         "Minuto",
         map("SoloDiasHabiles",'N',0,1),
         map("SoloFinDeMes",'N',0,1),
         map("SoloPrincipioMes",'N',0,1),
         map("habilitado",'N',0,1)
            
         into v_DiaDeMes,v_DiaDeSemana,v_Mes,v_Hora,v_minuto,v_SoloDiasHabiles,v_SoloFinDeMes,v_SoloPrincipioDeMes,v_habilitado from "BSCL.Plataforma.BD::BAT.Agenda" where "IdAgenda" = p_IdAgenda ;
         
         v_contadorMinuto := 0;
         v_contadorHora :=0;
         v_contadorMesDia :=0;
     
         IF :v_DiaDeMes = '**' THEN 
                    SELECT to_char(fechaLocal,'DD') INTO v_DiaDeMes FROM DUMMY;
                
         ELSE
            --es necesario el el dia tenga formato DD, si no falla al comparar fechas.
            SELECT CASE WHEN LENGTH(v_DiaDeMes) = 1 THEN '0' || v_DiaDeMes ELSE v_DiaDeMes END INTO v_DiaDeMes FROM DUMMY;
            
            p_DiaDeMes := to_number(v_DiaDeMes);
                 
                    IF :v_DiaDeMes > 0 AND :v_DiaDeMes < 32 THEN
                        v_contadorMesDia := (v_contadorMesDia +1);
                        SELECT to_char(fechaLocal,'DD') INTO fecha_auxiliar FROM DUMMY;
                        IF :fecha_auxiliar != :v_DiaDeMes THEN
                            IF :v_Hora = '**' THEN 
                                     v_Hora := '00';
                         END IF;
                            IF :v_Minuto = '**' THEN 
                                     v_Minuto := '00';
                         END IF;
                         
                        ELSE
                            
                         
                        END IF;
                    ELSE
                    p_FechaSalida := 'Dia de mes no valido';
                    END IF;
         END IF;
         --SELECT p_DiaDeMes FROM dummy;
         
         IF :v_DiaDeSemana = '*' THEN 
         
         ELSE
            p_DiaDeSemana := to_number(v_DiaDeSemana);
                 
                    IF :v_DiaDeSemana > 0 AND :v_DiaDeSemana < 8 THEN
                        v_contadorMesDia := (v_contadorMesDia +1);
                    ELSE
                    p_FechaSalida := 'Dia de semana no valido';
                    END IF;
         END IF;
         
         IF :v_Mes = '**' THEN  
            SELECT to_char(fechaLocal,'MM') INTO v_Mes FROM DUMMY;
         ELSE
            p_Mes := to_number(v_Mes);
                        SELECT to_char(fechaLocal,'DD') INTO fecha_auxiliar FROM DUMMY;
                
                    IF :v_Mes > 0 AND :v_Mes < 13 THEN
                        v_contadorMesDia := (v_contadorMesDia +1);
                         IF :v_Mes != fecha_auxiliar THEN
                             IF :v_DiaDeMes = '**' THEN 
                                         v_DiaDeMes := '01';
                             END IF;
                                IF :v_Hora = '**' THEN 
                                         v_Hora := '00';
                             END IF;
                                IF :v_Minuto = '**' THEN 
                                         v_Minuto := '00';
                             END IF;
                     ELSE
                            
                         END IF;
                    
                    ELSE
                    p_FechaSalida := 'Mes no valido';
                    END IF;
         END IF;
         
         
         IF :v_Hora = '**' THEN 
                select to_char(fechaLocal,'HH24') into v_Hora from dummy;
                v_contadorHora := (v_contadorHora+1);
                
         ELSE
            p_Hora := to_number(v_Hora);
                 
                    IF :p_Hora >= 0 AND :p_Hora < 24 THEN
                     
                    ELSE
                    p_FechaSalida := 'Hora no valido';
                     
                    END IF;
         END IF;
         
         IF :v_Minuto = '**' THEN 
                select to_char(fechaLocal,'MI') into v_Minuto from dummy;
                v_contadorMinuto := (v_contadorMinuto +1);
                
         ELSE
            p_Minuto := to_number(v_Minuto);
                 
                    IF :v_Minuto >= 0 AND :v_Minuto < 60 THEN
                    
                        
                    ELSE
                 
                    p_FechaSalida := 'Minuto no valido'|| '  '|| v_Minuto;
                    END IF;
         END IF;


         IF :v_habilitado = '0' THEN
             v_contadorMesDia := -1;
             v_contadorMinuto := -1;
             v_contadorHora := -1;
             p_FechaSalida :='NO HABILITADO';
         END IF;
            
        
         
         
         IF :v_contadorMesDia > 0 THEN
    
                select to_char(fechaLocal,'YYYY') into  v_FechaEjecucionNueva from dummy;
             v_FechaEjecucionNueva := v_FechaEjecucionNueva||'-'||v_Mes||'-'||v_DiaDeMes;
             v_contadorMesDia := 1;
         
         ELSE
                
            IF :v_existePrograma = 0 THEN
                 select to_char(fechaLocal,'YYYY-MM-DD HH24:MI:SS.FF7') into    v_FechaEjecucionAntigua from DUMMY;
             ELSE                   
                 v_FechaEjecucionAntigua := v_fechaEjecucionUltima;
            
             END IF;
                
                v_FechaEjecucionNueva := v_FechaEjecucionAntigua;
             -- v_contadorMesDia := 0;
             
         END IF;
         SELECT v_FechaEjecucionNueva FROM dummy;
         
         /* el contador minuto, valida si se ingreso o no minutos, lo mismo para el contador horas 
            1 = NO
            0 = SI
        */
         IF :v_contadorMinuto = 1 and :v_contadorHora= 0 THEN
                 IF :v_contadorMesDia = 0 THEN
                    v_FechaEjecucionAntigua := v_FechaEjecucionAntigua||' '|| v_Hora||':'||'00:00';             
                    SELECT fechaLocal INTO fecha_auxiliar FROM DUMMY;
                    IF :v_FechaEjecucionAntigua < :fecha_auxiliar THEN
                         SELECT to_char(fechaLocal,'YYYY-MM-DD') INTO v_FechaEjecucionAntigua FROM DUMMY;
                         select next_day(TO_DATE(v_FechaEjecucionAntigua,'YYYY-MM-DD')) into v_FechaEjecucionAntigua from dummy;
                         v_FechaEjecucionAntigua := v_FechaEjecucionAntigua||' '|| v_Hora||':'||'00:00';
                            p_FechaSalida :=v_FechaEjecucionAntigua;
                    END IF;
                    
                 END IF;
                 IF :v_contadorMesDia = 1 THEN
                     v_FechaEjecucionNueva := v_FechaEjecucionNueva||' '|| v_Hora||':'||v_Minuto||':00';
                     p_FechaSalida := v_FechaEjecucionNueva;
                 END IF;         
         END IF;
         
         IF :v_contadorMinuto = 0 and :v_contadorHora= 1 and p_DiaDeSemana = 0 THEN
         
                 IF :v_contadorMesDia = 0 THEN
                 
                     
                        IF :v_existePrograma > 0 THEN
                        
                            select to_char(ADD_SECONDS(TO_TIMESTAMP(v_FechaEjecucionNueva),60*p_Minuto),'YYYY-MM-DD HH24:MI:SS.FF7') into v_FechaEjecucionAntigua from dummy;
                         
                        ELSE
                             SELECT to_char(ADD_SECONDS(fechaLocal,60*p_Minuto),'YYYY-MM-DD HH24:MI:SS.FF7') INTO v_FechaEjecucionAntigua FROM DUMMY;
                        
                        END IF;
                        
                         
                         p_FechaSalida := v_FechaEjecucionAntigua;
                         
                 END IF;
                 IF :v_contadorMesDia = 1 THEN
                     declare v_HoraMinutoAux NVARCHAR(20);
                     select to_char(v_fechaEjecucionUltima,'HH24:MI:SS.FF7') into   v_HoraMinutoAux from dummy;
                     v_FechaEjecucionNueva := v_FechaEjecucionNueva||' '|| v_HoraMinutoAux;
                        
                     select ADD_SECONDS(TO_TIMESTAMP(v_FechaEjecucionNueva),60*p_Minuto) into v_FechaEjecucionNueva from dummy;
                     SELECT v_FechaEjecucionNueva, 'contador media = 1' AS "FLAG" FROM dummy;
                     p_FechaSalida := v_FechaEjecucionNueva;
                     
                 END IF;
            
         
         END IF;
         
         IF :v_contadorMinuto = 1 and :v_contadorHora= 1 and p_DiaDeSemana = 0 THEN

             IF :v_contadorMesDia = 0 THEN
                    v_FechaEjecucionAntigua := v_FechaEjecucionAntigua||' '|| v_Hora||':'||v_Minuto||':00';
                     p_FechaSalida :=v_FechaEjecucionAntigua ;
                 END IF;
                 IF :v_contadorMesDia = 1 THEN
                     v_FechaEjecucionNueva := v_FechaEjecucionNueva||' '|| v_Hora||':'||v_Minuto||':00';
                     p_FechaSalida :=v_FechaEjecucionNueva;
                     
                 END IF; 
         
         END IF;
         
         IF :v_contadorMinuto = 0 and :v_contadorHora= 0    and :p_DiaDeSemana = 0 THEN
                     
                IF :v_contadorMesDia = 0 THEN
                select v_FechaEjecucionAntigua AS "ASD" from dummy;
                     select to_char(to_date(v_FechaEjecucionAntigua) || ' ' || v_Hora||':'||v_Minuto||':00', 'YYYY-MM-DD HH24:MI:SS.FF7') into v_FechaEjecucionAntigua from dummy;
                 
                    
                    SELECT to_char(fechaLocal,'YYYY-MM-DD HH24:MI:SS.FF7') INTO fecha_auxiliar FROM DUMMY;
                    IF :v_FechaEjecucionAntigua <= :fecha_auxiliar THEN
                                                     
                        SELECT DAYS_BETWEEN (v_FechaEjecucionAntigua, fecha_auxiliar) INTO diasentre FROM DUMMY;
                        
                        IF :diasentre = 0 THEN
                            select to_char(next_day(v_FechaEjecucionAntigua)|| ' ' || v_Hora||':'||v_Minuto||':00', 'YYYY-MM-DD HH24:MI:SS.FF7') into v_FechaEjecucionAntigua from dummy;
                        ELSE
                            diasentre := diasentre +1;
                            SELECT to_char(ADD_DAYS(v_FechaEjecucionNueva,diasentre),'YYYY-MM-DD HH24:MI:SS.FF7') into v_FechaEjecucionAntigua from dummy;
                                
                        END IF;
                    END IF;
                    
                     p_FechaSalida :=   v_FechaEjecucionAntigua;
                 END IF;
                 IF :v_contadorMesDia = 1 THEN
                 
                     v_FechaEjecucionNueva := v_FechaEjecucionNueva||' '|| v_Hora||':'||v_Minuto||':00';
                     SELECT v_FechaEjecucionNueva, 'contador mes dia = 1' AS "FLAG" FROM dummy;
                     
                     SELECT to_char(fechaLocal,'YYYY-MM-DD HH24:MI:SS.FF7') INTO fecha_auxiliar FROM DUMMY;
                     
                     SELECT v_FechaEjecucionNueva, fecha_auxiliar, p_Mes, p_DiaDeMes, CASE WHEN :v_FechaEjecucionNueva < :fecha_auxiliar THEN 'TRUE' ELSE 'FALSE' END FROM dummy;
                                          
                     IF :v_FechaEjecucionNueva < :fecha_auxiliar THEN
                     
                             IF :p_Mes != 0 and p_DiaDeMes != 0 THEN
                                SELECT TO_CHAR(ADD_YEARS (v_FechaEjecucionNueva, 1),'YYYY-MM-DD HH24:MI:SS.FF7') INTO v_FechaEjecucionNueva FROM DUMMY;
                                    SELECT v_FechaEjecucionNueva, 'addyears' AS "FLAG" FROM dummy;
                             END IF;
                             
                             IF :p_Mes = 0 AND :p_DiaDeMes=0 THEN

                                 SELECT to_char(ADD_DAYS(v_FechaEjecucionNueva,1),'YYYY-MM-DD HH24:MI:SS.FF7') into v_FechaEjecucionNueva from dummy;
                                 SELECT v_FechaEjecucionNueva, 'adddays' AS "FLAG" FROM dummy;
                             END IF;
                             
                             IF :p_DiaDeMes != 0 AND :p_Mes = 0 THEN

                                 SELECT to_char(ADD_MONTHS(v_FechaEjecucionNueva,1),'YYYY-MM-DD HH24:MI:SS.FF7') into v_FechaEjecucionNueva from dummy;
                                 SELECT v_FechaEjecucionNueva, 'addmonths' AS "FLAG" FROM dummy;
                             END IF;

                     END IF;
                     p_FechaSalida :=   v_FechaEjecucionNueva;
                 END IF; 

         END IF;
         SELECT p_FechaSalida FROM dummy;
        
        
        
    
            IF :v_SoloPrincipioDeMes = '1' THEN
            select to_char(fechaLocal,'YYYY') into  v_FechaEjecucionNueva from dummy;
                fecha_auxiliar := v_FechaEjecucionNueva||'-'||v_Mes||'-'||'01';
                    IF :v_SoloDiasHabiles = '0' THEN
                    select to_char(fechaLocal,'YYYY') into  v_FechaEjecucionNueva from dummy;
                    v_FechaEjecucionNueva := v_FechaEjecucionNueva||'-'||v_Mes||'-'||'01';
                    v_contadorMesDia := -1;
                    v_contadorMinuto := -1;
                    v_contadorHora := -1;
                     ELSE
                         call "BSCL.Plataforma.BD.Utilitarios::DiaEsHabil"(TO_CHAR(fecha_auxiliar,'YYYY-MM-DD'),v_IdPaisSistema,v_diaHabilResultado);
                         WHILE v_diaHabilResultado = 'N' 
                         DO
                            
                             SELECT ADD_DAYS(TO_CHAR(fecha_auxiliar,'YYYY-MM-DD'), 1) INTO fecha_auxiliar FROM DUMMY;
                                 call "BSCL.Plataforma.BD.Utilitarios::DiaEsHabil"(TO_CHAR(fecha_auxiliar,'YYYY-MM-DD'),v_IdPaisSistema,v_diaHabilResultado);
                             IF :fecha_auxiliar < to_char(fechaLocal, 'YYYY-MM-DD') THEN
                                    SELECT ADD_MONTHS(to_date(v_FechaEjecucionNueva||'-'||v_Mes||'-'||'01','YYYY-MM-DD'), 1) INTO fecha_auxiliar FROM DUMMY;
                             END IF;
                         
                         END WHILE;
                         v_FechaEjecucionNueva := fecha_auxiliar;
                        
                END IF;
                
             p_FechaSalida  := v_FechaEjecucionNueva;
             p_DiaDeMes := (p_DiaDeMes+1);
         END IF;
         IF :v_SoloFinDeMes = '1' THEN
         
             IF :v_SoloDiasHabiles = '0' THEN
                 select to_char(fechaLocal,'YYYY-MM-DD HH24:MI:SS.FF7') into    v_FechaEjecucionNueva from dummy;
                 SELECT LAST_DAY (v_FechaEjecucionNueva)||' '||'00:00:00' into v_FechaEjecucionNueva FROM DUMMY;
                     
             ELSE
                     v_contadorMesDia := 0;
                 select to_char(fechaLocal,'YYYY-MM-DD HH24:MI:SS.FF7')into v_FechaEjecucionNueva from dummy;
                     SELECT LAST_DAY (v_FechaEjecucionNueva)||' '||'00:00:00' into v_FechaEjecucionNueva FROM DUMMY;
                     SELECT LAST_DAY (to_date(v_FechaEjecucionNueva,'YYYY-MM-DD HH24:MI:SS.FF7'))   into fecha_auxiliar FROM DUMMY;
                        
                     call "BSCL.Plataforma.BD.Utilitarios::DiaEsHabil"(TO_CHAR(fecha_auxiliar,'YYYY-MM-DD'),v_IdPaisSistema,v_diaHabilResultado);

                     WHILE v_diaHabilResultado = 'N' 
                        DO
                            SELECT ADD_DAYS(TO_CHAR(fecha_auxiliar,'YYYY-MM-DD'), -1) INTO fecha_auxiliar FROM DUMMY;
                            
                            call "BSCL.Plataforma.BD.Utilitarios::DiaEsHabil"(TO_CHAR(fecha_auxiliar,'YYYY-MM-DD'),v_IdPaisSistema,v_diaHabilResultado);
                            
                         
                     END WHILE;
                     v_FechaEjecucionNueva := fecha_auxiliar;
                    
                     
             END IF;
                
                p_FechaSalida   := v_FechaEjecucionNueva;
                p_DiaDeMes := (p_DiaDeMes+1);
         END IF;
         
            
                
         
         IF :p_DiaDeSemana > 0 THEN
                    declare v_HoraMinutoAux NVARCHAR(20);
                SELECT WEEKDAY(TO_CHAR(v_FechaEjecucionNueva,'YYYY-MM-DD'))+1 INTO v_diadesemanaresultado FROM DUMMY;
                 select to_char(fechaLocal,'YYYY-MM-DD') INTO fecha_auxiliar from dummy; 
                IF :v_diadesemanaresultado = p_DiaDeSemana THEN
                     select to_char(fechaLocal,'YYYY-MM-DD') INTO fecha_auxiliar from dummy;
                        
                     IF :fecha_auxiliar = to_char(:v_FechaEjecucionNueva,'YYYY-MM-DD') THEN
                     
                            select to_char(v_fechaEjecucionUltima,'YYYY-MM-DD HH24:MI:SS.FF7') into v_FechaEjecucionNueva from DUMMY;
                                select to_char(ADD_SECONDS(TO_TIMESTAMP(v_FechaEjecucionNueva),60*p_Minuto),'YYYY-MM-DD HH24:MI:SS.FF7') into v_FechaEjecucionNueva from dummy;
                    p_FechaSalida := v_FechaEjecucionNueva;
                 ELSE
                            p_FechaSalida := to_char(v_FechaEjecucionNueva,'YYYY-MM-DD')||' 00:00:00';
                     END IF;
                ELSE
                
                     --AGREGAR AÑO  (EJEMPLO TODOS LOS MARTES QUE SEAN 25 DE JUNIO)
                    IF :p_Mes > 0 AND :p_DiaDeMes > 0 THEN
                            p_FechaSalida := p_Mes||' ' || p_DiaDeMes;
                     WHILE v_diadesemanaresultado != p_DiaDeSemana
                     DO
                     
                            SELECT ADD_YEARS (v_FechaEjecucionNueva, 1) INTO v_FechaEjecucionNueva FROM DUMMY;
                            select weekday(v_FechaEjecucionNueva)+1 INTO v_diadesemanaresultado FROM DUMMy;
                        
                     END WHILE;
                     
                    
                    END IF; 
                -- AGREGAR UN MES (EJEMPLO TODOS LOS MARTES QUE SEAN 25)
                    IF :p_Mes <= 0 AND :p_DiaDeMes > 0 THEN
                    
                 
                     WHILE v_diadesemanaresultado != p_DiaDeSemana
                     DO

                 
                        SELECT ADD_MONTHS (v_FechaEjecucionNueva, 1) INTO v_FechaEjecucionNueva FROM DUMMY;
                        select weekday(v_FechaEjecucionNueva)+1 INTO v_diadesemanaresultado FROM DUMMy; 
                        
                     END WHILE; 
                     p_FechaSalida := to_char(v_FechaEjecucionNueva,'YYYY-MM-DD')||' 00:00:00';
                    -- p_FechaSalida := v_FechaEjecucionNueva;
                    END IF; 
                    --AGREGAR UN AÑO SI ES QUE ES MAYOR EL MES ACTUAL QUE EL P_MES  (EJEMPLO TODOS LOS MARTES DE JUNIO) 
                    IF :p_Mes > 0 AND :p_DiaDeMes <= 0 THEN

                         WHILE v_diadesemanaresultado != p_DiaDeSemana
                         DO
                        
                            IF :p_Mes < v_Comodin THEN 
                            
                                 SELECT ADD_YEARS (v_FechaEjecucionNueva, 1) into v_FechaEjecucionNueva FROM DUMMY;
                                
                            
                            END IF;
                            select TO_CHAR(v_FechaEjecucionNueva,'MM') INTO v_Comodin from dummy;
                            SELECT ADD_DAYS (v_FechaEjecucionNueva, 1) INTO v_FechaEjecucionNueva FROM DUMMY;
                            select weekday(v_FechaEjecucionNueva)+1 INTO v_diadesemanaresultado FROM DUMMy;
                            
                         END WHILE;
                        
                    END IF; 
                        
                    IF :p_Mes = 0 AND :p_DiaDeMes = 0 THEN
                    
                         WHILE v_diadesemanaresultado != p_DiaDeSemana
                         DO
                         
                            SELECT ADD_DAYS (v_FechaEjecucionNueva, 1) INTO v_FechaEjecucionNueva FROM DUMMY;
                            select weekday(v_FechaEjecucionNueva)+1 INTO v_diadesemanaresultado FROM DUMMy;
                            
                         END WHILE;
                         
                    
                    END IF;
                    
                     IF :fecha_auxiliar = to_char(:v_FechaEjecucionNueva,'YYYY-MM-DD') THEN
                     
                            select to_char(v_FechaEjecucionNueva,'YYYY-MM-DD HH24:MI:SS.FF7') into  v_FechaEjecucionNueva from dummy;
                                select to_char(ADD_SECONDS(TO_TIMESTAMP(v_FechaEjecucionNueva),60*p_Minuto),'YYYY-MM-DD HH24:MI:SS.FF7') into v_FechaEjecucionNueva from dummy;
                    p_FechaSalida := v_FechaEjecucionNueva;
                 ELSE
                            p_FechaSalida := to_char(v_FechaEjecucionNueva,'YYYY-MM-DD')||' 00:00:00';
                     END IF;
                    
                
                END IF;
                
         
         END IF; 
         
         IF :p_DiaDeSemana=0 and :v_SoloPrincipioDeMes='0' and :p_DiaDeSemana ='0' and :v_SoloDiasHabiles ='1' THEN
                declare contador_habil BIGINT;
                CALL "BSCL.Plataforma.BD.Utilitarios::DiaEsHabil"(TO_CHAR(p_FechaSalida,'YYYY-MM-DD'),v_IdPaisSistema,v_diaHabilResultado);
                WHILE :v_diaHabilResultado = 'N' DO
                 SELECT ADD_DAYS(p_FechaSalida, 1) INTO p_FechaSalida FROM DUMMY;
                 CALL "BSCL.Plataforma.BD.Utilitarios::DiaEsHabil"(TO_CHAR(p_FechaSalida,'YYYY-MM-DD'),v_IdPaisSistema,v_diaHabilResultado);
                    SELECT DAYS_BETWEEN (fechaLocal, p_FechaSalida) into contador_habil FROM DUMMY;
                    IF:contador_habil < 0 THEN
                        v_diaHabilResultado:= 'N';
                    ELSE                
                         IF :p_FechaSalida < fechaLocal THEN    
                             v_diaHabilResultado:= 'N'; 
                         END IF;
                    END IF; 
                END WHILE;
                p_FechaSalida := TO_CHAR(p_FechaSalida,'YYYY-MM-DD HH24:MI:SS.FF7');
         END IF;
                      
             SELECT seconds_between(fechaLocal,to_seconddate(p_FechaSalida,'YYYY-MM-DD HH24:MI:SS.FF7')) INTO v_dateSeconds from dummy;
             SELECT TO_CHAR(to_seconddate(p_FechaSalida,'YYYY-MM-DD HH24:MI:SS.FF7'),'YYYY-MM-DD HH24:MI:SS.FF7') INTO p_FechaSalida FROM DUMMY;
         
            
            --fix agregar un milisegundo para que se muestre bien en la grilla
            p_Fecha_Salida = SELECT (v_dateSeconds +1) as "FechaAgendar" from dummy;
            p_out_msg := p_FechaSalida ;        
     
 END;
 
 