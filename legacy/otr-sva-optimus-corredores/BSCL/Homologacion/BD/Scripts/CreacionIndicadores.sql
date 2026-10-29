SET schema bscl;

select
'insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ("IdTipoIndicador","IdTipoEntidad","IdEmpresa","ValorIndicadorDefecto","ClaseDato","NombreTipoIndicador","GrupoIndicador","CantidadDecimales","LongitudMinima","LongitudMaxima","IdUso","IdEstado","RequiereAutorizacion","CodigoTipoIndicador","IdTipoPersona","RequiereFechaVencimiento","IdCarga","IdIndicadorMigracion")' 
||
' values (' 
||
"BSCL.Plataforma.BD.Secuencias::TipoIndicador".NEXTVAL 
||
','
||
''''
||
 CASE WHEN A1."ind_ambito" = 'CLI' THEN 'CLIENTE' ELSE 'PORTAFOLIO' END
||
''''
||
','
||
1 
||
','
||
''''
||
(select MIN(A2."din_id") from "BSCL_VIRTUAL"."GES_DETALLE_INDICADOR" as A2 where  A2."ind_id" = A1."ind_id") 
||
''''
||
','
||
''''
||
case when A1."ind_tipo_operador" ='B' THEN 'CHECKBOX' ELSE 'COMBO' END 
||
''''
||
','
||
''''
||
A1."ind_nombre"
||
''''
||
','
||
''''
||
'GENERALES'
||
''''
||
','
||
 0 
||
','
||
0 
||
','
||
0
||
','
||
'''' 
||
case when A1."ind_ind_requisito" ='S' THEN 'REQ' ELSE 'IND' END 
||
''''
||
','
||
''''
||
'ACT'
||
''''
||
','
||
''''
||
'N'
||
''''
||
','
||
''''
||
a1."reg_id"
||
''''
||
','
||
''''
||
'A'
||
''''
||
','
||
''''
||
'N'
||
''''
||
','
||
88
||
','
||
 A1."ind_id" 
||
');'  
from "BSCL_VIRTUAL"."GES_INDICADOR" AS A1
ORDER by A1."ind_ambito",A1."ind_tipo";

--- cargar el detalle
select 
'insert into "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","DetalleIndicador","Codigo","IdCarga") values ('
||
"BSCL.Plataforma.BD.Secuencias::DetalleIndicador".NEXTVAL
||
','
||
A3."IdTipoIndicador"
||
','
||
''''
||
A1."din_valor"
||
''''
||
','
||
''''
||
A1."din_id"  
||
''''
||
','
||
88
||
');'
from "BSCL_VIRTUAL"."GES_DETALLE_INDICADOR" AS A1 
INNER JOIN "BSCL_VIRTUAL"."GES_INDICADOR" AS A2
ON A1."ind_id" = A2."ind_id"
INNER JOIN "BSCL"."BSCL.Plataforma.BD::IND.TipoIndicador" AS A3
ON A3."IdIndicadorMigracion" = A1."ind_id" 
order by A1."ind_id"