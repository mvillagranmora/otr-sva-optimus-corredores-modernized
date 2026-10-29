SET SCHEMA "BSCL"; 
--GO 
DROP VIEW "BSCL.Homologacion.BD.Vistas::HomologaParametros";
--GO
create view "BSCL.Homologacion.BD.Vistas::HomologaParametros" as
SELECT 
'ECIVIACUER' AS par_id,--varchar(10) NOT NULL,**************
'     ' AS tpa_id,--char(5) NOT NULL,
'     ' AS par_codigo,--varchar(5) NOT NULL,
'Conviviente Civil' AS par_nombre,--varchar(50) NOT NULL,*****************
'     ' AS par_agrupador,--varchar(20) NOT NULL,
'     ' AS par_valor,--varchar(20) NOT NULL,
'     ' AS par_descripcion--varchar(500) NOT NULL,
FROM DUMMY
UNION
SELECT 
'ECIVICASA' AS par_id,--varchar(10) NOT NULL,**************
'' AS tpa_id,--char(5) NOT NULL,
'' AS par_codigo,--varchar(5) NOT NULL,
'Casado' AS par_nombre,--varchar(50) NOT NULL,*****************
'' AS par_agrupador,--varchar(20) NOT NULL,
'' AS par_valor,--varchar(20) NOT NULL,
'' AS par_descripcion--varchar(500) NOT NULL,
FROM DUMMY
UNION
SELECT
'ECIVIDIVOR' AS par_id,--varchar(10) NOT NULL,**************
'' AS tpa_id,--char(5) NOT NULL,
'' AS par_codigo,--varchar(5) NOT NULL,
'Divorciado' AS par_nombre,--varchar(50) NOT NULL,*****************
'' AS par_agrupador,--varchar(20) NOT NULL,
'' AS par_valor,--varchar(20) NOT NULL,
'' AS par_descripcion--varchar(500) NOT NULL,
FROM DUMMY
UNION
SELECT 
'ECIVISOLTE' AS par_id,--varchar(10) NOT NULL,**************
'' AS tpa_id,--char(5) NOT NULL,
'' AS par_codigo,--varchar(5) NOT NULL,
'Soltero' AS par_nombre,--varchar(50) NOT NULL,*****************
'' AS par_agrupador,--varchar(20) NOT NULL,
'' AS par_valor,--varchar(20) NOT NULL,
'' AS par_descripcion--varchar(500) NOT NULL,
FROM DUMMY
UNION
SELECT 
'ECIVIVIUDO' AS par_id,--varchar(10) NOT NULL,**************
'' AS tpa_id,--char(5) NOT NULL,
'' AS par_codigo,--varchar(5) NOT NULL,
'Viudo' AS par_nombre,--varchar(50) NOT NULL,*****************
'' AS par_agrupador,--varchar(20) NOT NULL,
'' AS par_valor,--varchar(20) NOT NULL,
'' AS par_descripcion--varchar(500) NOT NULL,
FROM DUMMY
;
--GO
GRANT SELECT ON "BSCL.Homologacion.BD.Vistas::HomologaParametros" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
--GO