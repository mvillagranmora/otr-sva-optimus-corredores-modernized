
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Validacion" WHERE "Esquema" = 'DJ1922' AND "Codigo" = 'H2924';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Validacion" WHERE "Esquema" = 'DJ1922' AND "Codigo" = 'H2925';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Validacion" WHERE "Esquema" = 'DJ1922' AND "Codigo" = 'H2926';





insert into "BSCL"."BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1922' as "Esquema",
    '2021' as "Version",
    'H2924' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Rut de la administradora (C3)es campo obligatorio y debe cumplir con modulo 11' as "Descripcion",
    'E' as "Severidad",
    'C3 <>  Blanco .o. vacío .y. C3 debe cumplir modulo 11 .'  as "Formula"
from dummy;

insert into "BSCL"."BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1922' as "Esquema",
    '2021' as "Version",
    'H2925' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'RUT del Fondo C4 es campo obligatorio y debe cumplir con modulo 11' as "Descripcion",
    'E' as "Severidad",
    'C4 <>  Blanco .o. vacío .y. C4 debe cumplir modulo 11 .'  as "Formula"
from dummy;

insert into "BSCL"."BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1922' as "Esquema",
    '2021' as "Version",
    'H2926' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'RUT del participe o aportante  C7 es campo obligatorio y debe cumplir con modulo 11' as "Descripcion",
    'E' as "Severidad",
    'C7 <>  Blanco .o. vacío .y. C7 debe cumplir modulo 11 .'  as "Formula"
from dummy;


insert into "BSCL"."BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1922' as "Esquema",
    '2022' as "Version",
    'H2924' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Rut de la administradora (C3)es campo obligatorio y debe cumplir con modulo 11' as "Descripcion",
    'E' as "Severidad",
    'C3 <>  Blanco .o. vacío .y. C3 debe cumplir modulo 11 .'  as "Formula"
from dummy;

insert into "BSCL"."BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1922' as "Esquema",
    '2022' as "Version",
    'H2925' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'RUT del Fondo C4 es campo obligatorio y debe cumplir con modulo 11' as "Descripcion",
    'E' as "Severidad",
    'C4 <>  Blanco .o. vacío .y. C4 debe cumplir modulo 11 .'  as "Formula"
from dummy;

insert into "BSCL"."BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1922' as "Esquema",
    '2022' as "Version",
    'H2926' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'RUT del participe o aportante  C7 es campo obligatorio y debe cumplir con modulo 11' as "Descripcion",
    'E' as "Severidad",
    'C7 <>  Blanco .o. vacío .y. C7 debe cumplir modulo 11 .'  as "Formula"
from dummy;



insert into "BSCL"."BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1922' as "Esquema",
    '2020' as "Version",
    'H2924' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Rut de la administradora (C3)es campo obligatorio y debe cumplir con modulo 11' as "Descripcion",
    'E' as "Severidad",
    'C3 <>  Blanco .o. vacío .y. C3 debe cumplir modulo 11 .'  as "Formula"
from dummy;

insert into "BSCL"."BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1922' as "Esquema",
    '2020' as "Version",
    'H2925' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'RUT del Fondo C4 es campo obligatorio y debe cumplir con modulo 11' as "Descripcion",
    'E' as "Severidad",
    'C4 <>  Blanco .o. vacío .y. C4 debe cumplir modulo 11 .'  as "Formula"
from dummy;

insert into "BSCL"."BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1922' as "Esquema",
    '2020' as "Version",
    'H2926' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'RUT del participe o aportante  C7 es campo obligatorio y debe cumplir con modulo 11' as "Descripcion",
    'E' as "Severidad",
    'C7 <>  Blanco .o. vacío .y. C7 debe cumplir modulo 11 .'  as "Formula"
from dummy;

