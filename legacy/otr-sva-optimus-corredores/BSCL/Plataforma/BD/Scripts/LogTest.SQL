

-----------------------------------------
DROP PROCEDURE "BSCL"."BSCL.Plataforma.BD.Scripts::LogCreaTest";
CREATE PROCEDURE "BSCL"."BSCL.Plataforma.BD.Scripts::LogCreaTest" (
	 IdEmpresa			BIGINT			,
	 Fecha 				TIMESTAMP 	    ,	
	 IdUsuario			BIGINT          ,
	 Servicio 			NVARCHAR(255)   ,
	 Bandera 				NVARCHAR(80)   ,
	 Detalle 				NVARCHAR(520) 
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS		
BEGIN
	IF SESSION_CONTEXT('DEPURACION')='S' THEN 
 		BEGIN AUTONOMOUS TRANSACTION
			insert into "BSCL.Plataforma.BD::PLA.Depuracion" ("IdEmpresa","Fecha","IdUsuario","Bandera","Servicio","Detalle") values (IdEmpresa,Fecha,IdUsuario,Bandera,Servicio,Detalle);
			COMMIT;
		END;		
	END IF;
END;

call "BSCL"."BSCL.Plataforma.BD.Servicios::Depuracion"(1,now(),1,'SERVICIO.X.X.L','S-FOR1','INICIO LOOP DE CARTERA PEW PEW');
call "BSCL"."BSCL.Plataforma.BD.Servicios::Depuracion"(1,now(),1,'SERVICIO.X.X.L','E-FOR1','TERMINO LOOP DE CARTERA PEW PEW');
select * from "BSCL"."BSCL.Plataforma.BD::PLA.Depuracion"
SET 'DEPURACION' = 'N';
call "BSCL"."BSCL.Plataforma.BD.Servicios::Depuracion"(1,now(),1,'SERVICIO.X.X.L','E-FOR1','TERMINO LOOP DE CARTERA PEW PEW');


delete from "BSCL"."BSCL.Plataforma.BD::PLA.Depuracion";


-----------------------------------------
DROP PROCEDURE "BSCL"."BSCL.Plataforma.BD.Scripts::LogTest";
CREATE PROCEDURE "BSCL"."BSCL.Plataforma.BD.Scripts::LogTest" () 

LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
		
BEGIN
declare v_count int;
declare v_ini TIMESTAMP;
declare v_fin TIMESTAMP;
declare v_res nvarchar(100);

----------------------------------------------	
----------------- TEST LOG INSERT CALL
----------------------------------------------	
v_ini:=now();
v_count:=5000;
 WHILE v_count > 0 DO 
	--call "BSCL"."BSCL.Plataforma.BD.Scripts::LogCreaTest"(1,now(),1,'SERVICIO.X.X.L','I-FOR1','INICIO  LOOP DE CARTERA');
	--call "BSCL"."BSCL.Plataforma.BD.Scripts::LogCreaTest"(1,now(),1,'SERVICIO.X.X.L','E-FOR1','TERMINO LOOP DE CARTERA');
	v_count:=v_count-1;
END WHILE;
v_fin:=now();
select 'INSERT CALL',v_ini,v_fin,seconds_between(v_ini,v_fin) as "DSEGS"  from dummy;
--delete from "BSCL.Plataforma.BD::PLA.Depuracion";
----------------------------------------------	
----------------- TEST LOG INSERT DIRECTO
----------------------------------------------	
v_ini:=now();
v_count:=5000;
 WHILE v_count > 0 DO 
 	BEGIN AUTONOMOUS TRANSACTION
		--insert into "BSCL.Plataforma.BD::PLA.Depuracion" ("IdEmpresa","Fecha","IdUsuario","Bandera","Servicio","Detalle") values (1,now(),1,'SERVICIO.X.X.L','I-FOR1','INICIO  LOOP DE CARTERA');
		--COMMIT;
	END;
 	BEGIN AUTONOMOUS TRANSACTION
		--insert into "BSCL.Plataforma.BD::PLA.Depuracion" ("IdEmpresa","Fecha","IdUsuario","Bandera","Servicio","Detalle") values (1,now(),1,'SERVICIO.X.X.L','E-FOR1','TERMINO LOOP DE CARTERA');
		--COMMIT;
	END;				
	v_count:=v_count-1;
END WHILE;
v_fin:=now();
select 'INSERT DIRECTO',v_ini,v_fin,seconds_between(v_ini,v_fin) as "DSEGS"  from dummy;
--delete from "BSCL.Plataforma.BD::PLA.Depuracion";
----------------------------------------------	
----------------- TEST LOG FUNCTION
----------------------------------------------	
v_ini:=now();
v_count:=5000;
 WHILE v_count > 0 DO 
    v_res:="BSCL"."BSCL.Plataforma.BD.Utilitarios::EsFecha" ('2000-01-01');
	v_count:=v_count-1;
END WHILE;
v_fin:=now();
select 'FUNCTION LOGIC',v_ini,v_fin,seconds_between(v_ini,v_fin) as "DSEGS"  from dummy;
-----
END;
------------------------------



DROP PROCEDURE "BSCL"."BSCL.Plataforma.BD.Scripts::FnTest";
CREATE PROCEDURE "BSCL"."BSCL.Plataforma.BD.Scripts::FnTest" () 

LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
		
BEGIN
declare v_count int;
declare v_ini TIMESTAMP;
declare v_fin TIMESTAMP;
declare v_res nvarchar(100);

----------------------------------------------	
----------------- TEST LOG FUNCTION
----------------------------------------------	
v_ini:=now();
v_count:=5000;
 WHILE v_count > 0 DO 
    v_res:="BSCL"."BSCL.Plataforma.BD.Utilitarios::EsFecha" ('2000-01-01');
	v_count:=v_count-1;
END WHILE;
v_fin:=now();
select 'FUNCTION LOGIC',v_ini,v_fin,seconds_between(v_ini,v_fin) as "DSEGS"  from dummy;
-----
----------------------------------------------	
----------------- TEST LOG NO FUNCTION
----------------------------------------------	
v_ini:=now();
v_count:=5000;
 WHILE v_count > 0 DO 
	BEGIN
		DECLARE v_Fecha DATE;
		DECLARE EXIT HANDLER FOR SQLEXCEPTION
		BEGIN
			p_out_resultado := 'N';
		END;
		v_Fecha:= TO_DATE(p_Fecha, 'YYYY-MM-DD');
	
		p_out_resultado := 'S';
	END;
	v_count:=v_count-1;
END WHILE;
v_fin:=now();
select 'NO FUNCTION LOGIC',v_ini,v_fin,seconds_between(v_ini,v_fin) as "DSEGS"  from dummy;
-----
END;

SET 'MY_VAR' = 'abc';
You select the variable MY_VAR from the current session.
 SELECT SESSION_CONTEXT('MY_VARx') FROM DUMMY;
 
 -----------------------------------------
DROP PROCEDURE "BSCL"."BSCL.Plataforma.BD.Scripts::SecuenceTest"; 
CREATE PROCEDURE "BSCL"."BSCL.Plataforma.BD.Scripts::SecuenceTest" () 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS		
BEGIN
    
	declare V_TIMEOUT DATETIME;
	declare V_TIME_TO_TOUT DATETIME;
	declare  seq bigint default 0;
	declare  seqp bigint default 0;
	
 
    V_TIMEOUT      := NOW();
	V_TIME_TO_TOUT := ADD_SECONDS (NOW(), 3600 );
	WHILE V_TIMEOUT < V_TIME_TO_TOUT DO 
		select "BSCL"."BSCL.Orden.BD.Secuencias::Transaccion".NEXTVAL into seq from dummy;
		if (seq<seqp) then
			V_TIME_TO_TOUT := ADD_SECONDS (NOW(), -100 );
			select 'CAMBIO EN ' as TXT,seq,seqp from dummy;
		end if;
	    --
		V_TIMEOUT := NOW();
	END WHILE;

END;

 call "BSCL"."BSCL.Plataforma.BD.Scripts::SecuenceTest" ();

 