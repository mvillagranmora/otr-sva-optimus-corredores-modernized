SET SCHEMA "BSCL";  

DROP PROCEDURE "BSCL.Normativa.BD.Servicios.FATCA::ConsultarDatosFatcaDetalle";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios.FATCA::ConsultarDatosFatcaDetalle"
( 
    -- Parametros de Entrada
 	IN p_IdSolicitudCircular 		  BIGINT DEFAULT 0, 
	IN p_TablaParametros    		  TABLE("TipoParametro" nvarchar(100) , "Valor" nvarchar(100))
    --VARIABLES DE SALIDA 
	,OUT p_out_TablaSalida	 		 "BSCL.Normativa.BD::NOR.SALIDA_FATCADETALLE" --Se debe llamar siemte p_out_TablaSalida
					  
   
  
)  	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
AS
  BEGIN
  
  
declare v_LogCondicion,p_IdCliente,v_condfinal,v_cond_mmin,v_cond_mmax BIGINT DEFAULT 0;


declare p_MontoMin, p_MontoMax decimal(26,4) DEFAULT 0;

declare p_FecEjec, p_FecEnrol, p_FecDesde  NVARCHAR(30) default '0001-01-01';

declare p_FecHasta  NVARCHAR(30) default '9999-01-01';	

declare p_ClasificacionFatca,p_IndicadorFatca,p_registroIndicios,p_registroIndiciosFinal NVARCHAR(20) default '';

declare p_TipoPersona  NVARCHAR(10) default '0';
  
--DECLARARVARIABLES

select count(1) into v_cond_mmin from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'MMIN';
 
select count(1) into v_cond_mmax  from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'MMAX';



IF(v_cond_mmin>0)
THEN

select top 1 CAST(TRIM(IFNULL("Valor",'0')) AS DECIMAL (26,4))  into p_MontoMin from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'MMIN';

END IF;

IF(v_cond_mmax>0)
THEN

select top 1 CAST(TRIM(IFNULL("Valor",'0')) AS DECIMAL (26,4))  into p_MontoMax from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'MMAX';

END IF;


select top 1 IFNULL("Valor",'0001-01-01') into p_FecEjec from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'FEJEC';

select top 1 IFNULL("Valor",'0001-01-01') into p_FecEnrol from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'FENR';

select top 1 IFNULL("Valor",'0001-01-01') into p_FecDesde from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'FEDES';

select top 1 IFNULL("Valor",'9999-01-01') into p_FecHasta from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'FEHAS';

select top 1 IFNULL("Valor",'') into p_ClasificacionFatca from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'CFAT';

select top 1 IFNULL("Valor",'') into p_IndicadorFatca from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'IFAT';

select top 1 IFNULL("Valor",'') into p_TipoPersona from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'TP';

select top 1 (case when "Valor" = 'true' then '0' when "Valor" = 'false' then '1' else '-1' end ) into p_registroIndicios from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'RIND';

select top 1 IFNULL("Valor",0)  into p_IdCliente from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'IDCLI';


--select top 1 CAST(TRIM(IFNULL("Valor",'0')) AS DECIMAL (26,4))   from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'MMAX';

--select * from "BSCL.Normativa.BD::NOR.SolicitudParametros" where "TipoParametro" = 'RIND'
/*
select p_MontoMin as monmin , p_MontoMax as monmax,p_FecEjec as fecej ,p_FecEnrol as fecenrol, p_ClasificacionFatca as clasifi,
p_TipoPersona as tpersona,p_registroIndicios as regindicios ,p_FecDesde fdesde,p_FecHasta fhasta
from dummy;
*/
  
  
/*  
select * from  "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaFatca"
where "IdPersona" = '1760480'
select * from  "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona"
where "DocumentoIdentidad" = '14286453-3'
*/
--------------------
--LOGICA
--------------------

--Rescatamos todos los relacionados para realizar cruce final
--Aplicamos restricciones a la consulta según RQ
--relacionado debe poseer alguno de estos puntos :
-- 	 	- beneficiario de la empresa y %participación >10%
-- 	 	- tener indicios
--  	- ser USPERSON o USENTITY


v_RelacionadosConPatrimonio = 
select 
"CR"."IdPersonaOrigen"     		 as "IdPersonaOrigen"
,"CR"."IdPersonaDestino"   		 as "IdPersonaDestino"
,"CR"."IdTipoRelacion"     		 as "IdTipoRelacion"
,"CR"."PorcentajeParticipacion"  as "PorcentajeParticipacion"
FROM "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacion" as "CR"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaFatca" as "PFR" on ("PFR"."IdPersona" = "CR"."IdPersonaDestino")
--AGREGAR AQUI CRUCE ConsultarPersonaFatca pero para relacionado destino
LEFT JOIN "BSCL.Normativa.BD::NOR.DatosFatca" as "DFR" on ("DFR"."IdPersona" = "CR"."IdPersonaDestino")
where --("CR"."IdTipoRelacion" = 'BENEMP' and "CR"."PorcentajeParticipacion" >10)
--or 
("DFR"."ind_us_person" = '1' ) or ("DFR"."clasificacion_fatca" = 'USENTITY')or ("DFR"."clasificacion_fatca" = 'USPERSON') ; 

select 'v_RelacionadosConPatrimonio',* from :v_RelacionadosConPatrimonio;

--trae todas las relaciones en donde la relacion de destino es un cliente  (y que tengan indicios)

v_ClientesRelacionadosDestino = 

select 
"RCP"."IdPersonaOrigen"     		as "IdPersonaOrigen"
,"RCP"."IdPersonaDestino"   		as "IdPersonaDestino"
,"RCP"."IdTipoRelacion"     		as "IdTipoRelacion"
,"RCP"."PorcentajeParticipacion"  	as "PorcentajeParticipacion"
,"CFO"."IdCliente"  				as "IdCliente"

FROM :v_RelacionadosConPatrimonio as "RCP"
INNER JOIN "BSCL.Normativa.BD::NOR.ClienteFatca" as "CF" on ("CF"."IdPersona" = "RCP"."IdPersonaDestino" )
LEFT JOIN  "BSCL.Normativa.BD::NOR.ClienteFatca" as "CFO" on ("RCP"."IdPersonaDestino"  = "CFO"."IdPersona")
WHERE "CF"."IndUsPerson" =  '1';



select 'v_ClientesRelacionadosDestino',* from :v_ClientesRelacionadosDestino;

v_ClientesRelacionadosOrigen = 

select 
"RCP"."IdPersonaOrigen"     		as "IdPersonaOrigen"
,"RCP"."IdPersonaDestino"   		as "IdPersonaDestino"
,"RCP"."IdTipoRelacion"     		as "IdTipoRelacion"
,"RCP"."PorcentajeParticipacion"  	as "PorcentajeParticipacion"
,"CF"."IdCliente"  					as "IdCliente"

FROM :v_RelacionadosConPatrimonio as "RCP"
INNER JOIN "BSCL.Normativa.BD::NOR.ClienteFatca" as "CF" on ("CF"."IdPersona" = "RCP"."IdPersonaOrigen")
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as "CC" on ( "RCP"."IdPersonaOrigen" = "CC"."IdPersona")
;

SELECT 'v_ClientesRelacionadosOrigen',* FROM :v_ClientesRelacionadosOrigen;

v_ClientesSoloDestino =

select
"CRD"."IdPersonaDestino"  as "IdPersona",
"CF"."IdCliente" 		as "IdCliente"
 
FROM :v_ClientesRelacionadosDestino as  "CRD"
INNER JOIN "BSCL.Normativa.BD::NOR.ClienteFatca" as "CF" on ("CF"."IdPersona" = "CRD"."IdPersonaDestino")
where "CRD"."IdPersonaDestino" not in (select distinct "IdPersonaOrigen" from :v_ClientesRelacionadosOrigen);

SELECT 'v_ClientesSoloDestino',* FROM :v_ClientesSoloDestino;




--select * from :v_ClientesRelacionadosOrigen;


--Aplicamos filtro según RQ, si sólo son personas (no clientes) se debe mostrar 
--sólo las que indiquen directamente su clasificación como USPERSON o USENTITY

v_PersonasRelacionadasDestino =

select 
"RCP"."IdPersonaOrigen"     		as "IdPersonaOrigen"
,"RCP"."IdPersonaDestino"   		as "IdPersonaDestino"
,"RCP"."IdTipoRelacion"     		as "IdTipoRelacion"
,"RCP"."PorcentajeParticipacion"  	as "PorcentajeParticipacion"
--,"CC"."IdCliente"  					as "IdCliente"

from :v_RelacionadosConPatrimonio as "RCP"
--LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as "CC" 
--on ("RCP"."IdPersonaDestino"= "CC"."IdPersona")
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaFatca" as "PF" on ("RCP"."IdPersonaDestino" = "PF"."IdPersona")
where 
"RCP"."IdPersonaDestino" not in (select distinct "IdPersona" from "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente")
and ("PF"."IdClasificacionFatca" = 'USENTITY' OR "PF"."IdClasificacionFatca" ='USPERSON' OR "PF"."IdClasificacionFatca" ='RECALCITR');

SELECT 'v_RelacionadosConPatrimonio',* FROM :v_RelacionadosConPatrimonio;

--select * from :v_PersonasRelacionadasDestino;

--select * from :v_PersonasRelacionadasDestino;

/*
select distinct "IdClasificacionFatca" from "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaFatca"
select distinct "ClasificacionFatca" from "BSCL.Normativa.BD::NOR.ClienteFatca"
*/


--Obtengo universo de clientes sin relacionados



v_CliSinRelacion = 

select distinct "CF"."IdCliente" from 
"BSCL.Normativa.BD::NOR.ClienteFatca" as "CF"
--LEFT JOIN :v_RelacionadosConPatrimonio as "REL" on "CC"."IdPersona" = "REL"."IdPersonaDestino"
where 
"CF"."IdPersona" not in (select distinct "IdPersonaDestino" from  :v_RelacionadosConPatrimonio)
and 
"CF"."IdPersona" not in (select distinct "IdPersonaOrigen" from :v_RelacionadosConPatrimonio)
;
SELECT 'v_CliSinRelacion',* FROM :v_CliSinRelacion;
/*
V_CliCliRelacion = 

select 
"CRO"."IdCliente"
FROM :v_ClientesRelacionadosOrigen "CRO",
INNER JOIN :v_ClientesRelacionadosDestino "CRD" on ("CRO"."IdPersonaOrigen" =  "CRD"."IdPersonaOrigen" AND "CRO"."IdPersonaDestino" =  "CRD"."IdPersonaDestino")
;


select count(1) as cliclicli from :V_CliCliRelacion;

select count(1) as origen FROM :v_ClientesRelacionadosOrigen;

select count(1) as destino FROM  :v_ClientesRelacionadosDestino;


select count(1) as clisinrela from :v_CliSinRelacion;

*/
--select * from :v_CliSinRelacion;

--preparar caso cliente con cliente

/*
select count(1) as totalrelpatri from :v_RelacionadosConPatrimonio;
select count(1) as clirelaorig from :v_ClientesRelacionadosOrigen;
select count(1) as perreldest from  :v_PersonasRelacionadasDestino;

select count(1) as clientereladest from :v_ClientesRelacionadosDestino;
select count(1) as clientesinrela from :v_CliSinRelacion;
*/

--Listo informacion de Clientes con relacionados de tipo cliente

--------------------------------OJO , MOSTRAR REGISTROS INCLUSO SI NO SON US PERSON PERO SI TIENEN RELACIONADO CON INDICIOS MOSTRAR
v_salida_previa =


select 
"CP"."DocumentoIdentidad" 				as "Rut",    
"DF"."tipo_persona" 					as "TipoPersona",    
"DF"."ind_us_person"					as "Indicios",  
case   
when ("DFR"."ind_us_person" = '1' or "DFR"."clasificacion_fatca" = 'USENTITY' or "DFR"."clasificacion_fatca" = 'USPERSON') and "DF"."clasificacion_fatca" <> 'USPERSON' and "DF"."clasificacion_fatca" <> 'USENTITY'
then (case when "DF"."tipo_persona" = 'N' then 'USPERSON' when "DF"."tipo_persona" = 'J' then 'USENTITY' end )
else "DF"."clasificacion_fatca"
end	   									as "ClasificacionFatca",    --sobreescribo valor si es que el relacionado posee indicios
"DF"."indicador_fatca" 					as "ClienteFatca",    
case "DF"."auth_fatca" 	 
when '1' then 'Si'
when '0' then 'No'
else ''
end	   	
										as "AutorizaOrgFatca",    
"DF"."patrimonio_peso"					as "PatrimonioClp",    
"DF"."patrimonio_dolar"					as "PatrimonioUsd",    
"DF"."fecha_enrolamiento" 				as "FechaEnrolamiento",    
"DF"."fecha_ult_modificacion" 			as "FechaModIndicios",    
"DF"."fecha_ult_modificacion_fatca"		as "FechaModFacta",    
case "DF"."ind_residencia" 
when '1' then 'Si'
when '0' then 'No'
when 'E' then 'Excepción'
else ''
end					 					as "Residencia", 
case "DF"."ind_nacimiento" 
when '1' then 'Si'
when '0' then 'No'
when 'E' then 'Excepción'
else ''
end	   									as "LugarNacimiento",
 	
case "DF"."ind_pais_constitucion" 
when '1' then 'Si'
when '0' then 'No'
when 'E' then 'Excepción'
else ''
end	  									as "PaisConstitucion",    
  				
case "DF"."ind_casa_matriz"
when '1' then 'Si'
when '0' then 'No'
when 'E' then 'Excepción'
else ''
end	  									as "CasaMatriz",    
 		
case "DF"."ind_nacionalidad"
when '1' then 'Si'
when '0' then 'No'
when 'E' then 'Excepción'
else ''
end	  									as "Nacionalidad",
    
case "DF"."ind_direccion"
when '1' then 'Si'
when '0' then 'No'
when 'E' then 'Excepción'
else ''
end	  									as "CodPostal",

case "DF"."ind_telefono" 
when '1' then 'Si'
when '0' then 'No'
when 'E' then 'Excepción'
else ''
end					     				as "NumTelefono", 
	
case "DF"."ind_ordenes"
when '1' then 'Si'
when '0' then 'No'
when 'E' then 'Excepción'
else ''
end										as "OrdenPermanente",    
''										as "PoderEfectivo",    
	
case "DF"."ind_benef_empresa"
when '1' then 'Si'
when '0' then 'No'
else ''
end										as "Beneficiario", 
-----------------------------------------------------------------Datos Relacionado										
IFNULL("CPR"."DocumentoIdentidad",'-')	as "NumIdentificacion",
IFNULL("DFR"."tipo_persona",'-')		as "TipoPersonaRel",
IFNULL("CRD"."IdTipoRelacion",'-') 		as "TipoRelacion",
IFNULL("CFR"."ClasificacionFatca",'-') 	as "ClasificacionFatcaRel",
IFNULL("DFR"."indicador_fatca",'-') 	as "ClienteFatcaRel",
IFNULL("CFR"."IndUsPerson",'-') 		as "UsPersonRel",
case "DFR"."ind_residencia" 
when '1' then 'Si'
when '0' then 'No'
when 'E' then 'Excepción'
else '-'
end										as "ResidenciaRel",
case "DFR"."ind_nacimiento" 
when '1' then 'Si'
when '0' then 'No'
when 'E' then 'Excepción'
else '-'
end	  									as "LugarNacimientoRel",
case "DFR"."ind_pais_constitucion" 
when '1' then 'Si'
when '0' then 'No'
when 'E' then 'Excepción'
else '-'
end	  									as "PaisConstitucionRel",
case "DFR"."ind_casa_matriz"
when '1' then 'Si'
when '0' then 'No'
when 'E' then 'Excepción'
else '-'
end	  									as "CasaMatrizRel",
case "DFR"."ind_nacionalidad"
when '1' then 'Si'
when '0' then 'No'
when 'E' then 'Excepción'
else '-'
end	  									as "NacionalidadRel",
case "DFR"."ind_direccion"
when '1' then 'Si'
when '0' then 'No'
when 'E' then 'Excepción'
else '-'
end	  									as "CodPostalRel",
case "DFR"."ind_telefono" 
when '1' then 'Si'
when '0' then 'No'
when 'E' then 'Excepción'
else '-'
end										as "NumTelefonoRel",
case "DFR"."ind_ordenes"
when '1' then 'Si'
when '0' then 'No'
when 'E' then 'Excepción'
else '-'
end										as "OrdenPermanenteRel",
'-'									  	as "PoderEfectivoRel",
case "DFR"."ind_benef_empresa"
when '1' then 'Si'
when '0' then 'No'
else '-'
end										as "BeneficiarioRel",										
"DF"."IdCliente"						as "IdCliente",										   
"DF"."FechaCargaInformacion"			as "FechaCargaInformacion"   

--FROM "BSCL.Normativa.BD::NOR.ClienteFatca" as "CF"
FROM   :v_ClientesRelacionadosOrigen  as "CRO"
INNER JOIN :v_ClientesRelacionadosDestino as "CRD" on ("CRO"."IdPersonaOrigen" = "CRD"."IdPersonaOrigen" AND "CRO"."IdPersonaDestino" = "CRD"."IdPersonaDestino")
INNER JOIN "BSCL.Normativa.BD::NOR.DatosFatca" as "DF" on ("CRO"."IdCliente" = "DF"."IdCliente" and "DF"."Estado" = 'V') --quitar estado y reemplazar por filtro fecha
LEFT  JOIN "BSCL.Normativa.BD::NOR.ClienteFatca" as "CFR" on ("CRD"."IdPersonaDestino" = "CFR"."IdPersona")
LEFT  JOIN "BSCL.Normativa.BD::NOR.DatosFatca" as "DFR" on ("CFR"."IdCliente" = "DFR"."IdCliente" and "DFR"."Estado" = 'V') --quitar estado y reemplazar por filtro fecha
LEFT  JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" as "CP" on ("CP"."IdPersona" = "CRO"."IdPersonaOrigen")
LEFT  JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" as "CPR" on ("CPR"."IdPersona" = "CFR"."IdPersona")




union all


select 
"CP"."DocumentoIdentidad" 				as "Rut",    
"DF"."tipo_persona" 					as "TipoPersona",    
"DF"."ind_us_person"					as "Indicios",  
"DF"."clasificacion_fatca"
	   									as "ClasificacionFatca",    
"DF"."indicador_fatca" 					as "ClienteFatca",    
case "DF"."auth_fatca" 	 
when '1' then 'Si'
when '0' then 'No'
else ''
end	   	
										as "AutorizaOrgFatca",    
"DF"."patrimonio_peso"					as "PatrimonioClp",    
"DF"."patrimonio_dolar"					as "PatrimonioUsd",    
"DF"."fecha_enrolamiento" 				as "FechaEnrolamiento",    
"DF"."fecha_ult_modificacion" 			as "FechaModIndicios",    
"DF"."fecha_ult_modificacion_fatca"		as "FechaModFacta",    
case "DF"."ind_residencia" 
when '1' then 'Si'
when '0' then 'No'
when 'E' then 'Excepción'
else ''
end					 					as "Residencia", 
case "DF"."ind_nacimiento" 
when '1' then 'Si'
when '0' then 'No'
when 'E' then 'Excepción'
else ''
end	   									as "LugarNacimiento",
 	
case "DF"."ind_pais_constitucion" 
when '1' then 'Si'
when '0' then 'No'
when 'E' then 'Excepción'
else ''
end	  									as "PaisConstitucion",    
  				
case "DF"."ind_casa_matriz"
when '1' then 'Si'
when '0' then 'No'
when 'E' then 'Excepción'
else ''
end	  									as "CasaMatriz",    
 		
case "DF"."ind_nacionalidad"
when '1' then 'Si'
when '0' then 'No'
when 'E' then 'Excepción'
else ''
end	  									as "Nacionalidad",
    
case "DF"."ind_direccion"
when '1' then 'Si'
when '0' then 'No'
when 'E' then 'Excepción'
else ''
end	  									as "CodPostal",

case "DF"."ind_telefono" 
when '1' then 'Si'
when '0' then 'No'
when 'E' then 'Excepción'
else ''
end					     				as "NumTelefono", 
	
case "DF"."ind_ordenes"
when '1' then 'Si'
when '0' then 'No'
when 'E' then 'Excepción'
else ''
end										as "OrdenPermanente",    
''										as "PoderEfectivo",    
	
case "DF"."ind_benef_empresa"
when '1' then 'Si'
when '0' then 'No'
else ''
end										as "Beneficiario", 
-----------------------------------------------------------------Datos Relacionado										
IFNULL('-','-') 						as "NumIdentificacion",
IFNULL('-','-') 						as "TipoPersonaRel",
IFNULL('-','-')  						as "TipoRelacion",
IFNULL('-','-') 					 	as "ClasificacionFatcaRel",
IFNULL('-','-') 						as "ClienteFatcaRel",
IFNULL('-','-') 						as "UsPersonRel",
IFNULL('-','-')							as "ResidenciaRel",
IFNULL('-','-')							as "LugarNacimientoRel",
IFNULL('-','-')							as "PaisConstitucionRel",
IFNULL('-','-')							as "CasaMatrizRel",
IFNULL('-','-')							as "NacionalidadRel",
IFNULL('-','-')							as "CodPostalRel",
IFNULL('-','-')							as "NumTelefonoRel",
IFNULL('-','-')						    as "OrdenPermanenteRel",
IFNULL('-','-') 					  	as "PoderEfectivoRel",
IFNULL('-','-')							as "BeneficiarioRel",	
"DF"."IdCliente"						as "IdCliente",										   
"CF"."CliFechaModificacion"			    as "FechaCargaInformacion"  
FROM :v_ClientesSoloDestino as "CSD"    --si estan como rel destino y no en rel origen, no tiene relacion
INNER JOIN "BSCL.Normativa.BD::NOR.DatosFatca" as "DF" on ("CSD"."IdPersona" = "DF"."IdPersona" and "DF"."Estado" = 'V') --quitar estado y reemplazar por filtro fecha
LEFT  JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" as "CP" on ("CP"."IdPersona" = "CSD"."IdPersona")
LEFT  JOIN "BSCL.Normativa.BD::NOR.ClienteFatca" as "CF" on ("CF"."IdPersona" = "CSD"."IdPersona")




union all
--Listo información de clientes con relacionados del tipo persona 

select 
"CP"."DocumentoIdentidad" 				as "Rut",    
"DF"."tipo_persona" 					as "TipoPersona",    
"CF"."IndUsPerson"						as "Indicios",    
case "PF"."IdClasificacionFatca"
when 'USENTITY' then (case when "DF"."tipo_persona" = 'PERSONA NATURAL' then 'USPERSON' when "DF"."tipo_persona" = 'PERSONA JURIDICA' then 'USENTITY' end )
when 'USPERSON' then (case when "DF"."tipo_persona" = 'PERSONA NATURAL' then 'USPERSON' when "DF"."tipo_persona" = 'PERSONA JURIDICA' then 'USENTITY' end )
else ''
end
										as "ClasificacionFatca",    
"DF"."indicador_fatca" 					as "ClienteFatca",    
case "DF"."auth_fatca" 	 
when '1' then 'Si'
when '0' then 'No'
else ''
end	   									as "AutorizaOrgFatca",    
"DF"."patrimonio_peso"					as "PatrimonioClp",    
"DF"."patrimonio_dolar"					as "PatrimonioUsd",    
"DF"."fecha_enrolamiento" 				as "FechaEnrolamiento",    
"DF"."fecha_ult_modificacion" 			as "FechaModIndicios",    
"DF"."fecha_ult_modificacion_fatca"		as "FechaModFacta",    
case "DF"."ind_residencia" 
when '1' then 'Si'
when '0' then 'No'
when 'E' then 'Excepción'
else ''
end					 					as "Residencia", 
case "DF"."ind_nacimiento" 
when '1' then 'Si'
when '0' then 'No'
when 'E' then 'Excepción'
else ''
end	   									as "LugarNacimiento",
 	
case "DF"."ind_pais_constitucion" 
when '1' then 'Si'
when '0' then 'No'
when 'E' then 'Excepción'
else ''
end	  									as "PaisConstitucion",    
  				
case "DF"."ind_casa_matriz"
when '1' then 'Si'
when '0' then 'No'
when 'E' then 'Excepción'
else ''
end	  									as "CasaMatriz",    
 		
case "DF"."ind_nacionalidad"
when '1' then 'Si'
when '0' then 'No'
when 'E' then 'Excepción'
else ''
end	  									as "Nacionalidad",
    
case "DF"."ind_direccion"
when '1' then 'Si'
when '0' then 'No'
when 'E' then 'Excepción'
else ''
end	  									as "CodPostal",

case "DF"."ind_telefono" 
when '1' then 'Si'
when '0' then 'No'
when 'E' then 'Excepción'
else ''
end					     				as "NumTelefono", 
	
case "DF"."ind_ordenes"
when '1' then 'Si'
when '0' then 'No'
when 'E' then 'Excepción'
else ''
end										as "OrdenPermanente",    
''										as "PoderEfectivo",    
	
case "DF"."ind_benef_empresa"
when '1' then 'Si'
when '0' then 'No'
else ''
end										as "Beneficiario", 
-----------------------------------------------------------------Datos Relacionado										
IFNULL("CP"."DocumentoIdentidad",'-')	as "NumIdentificacion",
IFNULL("CP"."Tipo",'-')					as "TipoPersonaRel",
IFNULL("PRD"."IdTipoRelacion",'-') 		as "TipoRelacion",
IFNULL("PF"."NombreClasificacionFatca",'-') 	as "ClasificacionFatcaRel",
IFNULL('-','-') 						as "ClienteFatcaRel",
IFNULL('-','-') 						as "UsPersonRel",
IFNULL('-','-')							as "ResidenciaRel",
IFNULL('-','-')							as "LugarNacimientoRel",
IFNULL('-','-')							as "PaisConstitucionRel",
IFNULL('-','-')							as "CasaMatrizRel",
IFNULL('-','-')							as "NacionalidadRel",
IFNULL('-','-')							as "CodPostalRel",
IFNULL('-','-')							as "NumTelefonoRel",
IFNULL('-','-')						    as "OrdenPermanenteRel",
'-'									  	as "PoderEfectivoRel",
IFNULL('-','-')							as "BeneficiarioRel",										
"DF"."IdCliente"						as "IdCliente",										   
"CF"."CliFechaModificacion"			    as "FechaCargaInformacion"   

FROM "BSCL.Normativa.BD::NOR.ClienteFatca" as "CF"
INNER JOIN :v_PersonasRelacionadasDestino as "PRD" on ("CF"."IdPersona" = "PRD"."IdPersonaOrigen") 
INNER JOIN "BSCL.Normativa.BD::NOR.DatosFatca" as "DF" on ("CF"."IdCliente" = "DF"."IdCliente" and "DF"."Estado" = 'V') --quitar estado y reemplazar por filtro fecha
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaFatca" as "PF" on ("PF"."IdPersona" = "PRD"."IdPersonaDestino" )
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" as "CP" on ("CP"."IdPersona" = "PRD"."IdPersonaDestino" )


union all


--listo todos detalle para clientes sin relacionados

select 
"CP"."DocumentoIdentidad" 				as "Rut",    
"DF"."tipo_persona" 					as "TipoPersona",    
"CF"."IndUsPerson"					    as "Indicios",    
"CF"."ClasificacionFatca" 				as "ClasificacionFatca",    
"DF"."indicador_fatca" 					as "ClienteFatca",    
case "DF"."auth_fatca" 	 
when '1' then 'Si'
when '0' then 'No'
else ''
end	   									as "AutorizaOrgFatca",    
"DF"."patrimonio_peso"					as "PatrimonioClp",    
"DF"."patrimonio_dolar"					as "PatrimonioUsd",    
"DF"."fecha_enrolamiento" 				as "FechaEnrolamiento",    
"DF"."fecha_ult_modificacion" 			as "FechaModIndicios",    
"DF"."fecha_ult_modificacion_fatca"		as "FechaModFacta",    
case "DF"."ind_residencia" 
when '1' then 'Si'
when '0' then 'No'
when 'E' then 'Excepción'
else ''
end					 					as "Residencia", 
case "DF"."ind_nacimiento" 
when '1' then 'Si'
when '0' then 'No'
when 'E' then 'Excepción'
else ''
end	   									as "LugarNacimiento",
 	
case "DF"."ind_pais_constitucion" 
when '1' then 'Si'
when '0' then 'No'
when 'E' then 'Excepción'
else ''
end	  									as "PaisConstitucion",    
  				
case "DF"."ind_casa_matriz"
when '1' then 'Si'
when '0' then 'No'
when 'E' then 'Excepción'
else ''
end	  									as "CasaMatriz",    
 		
case "DF"."ind_nacionalidad"
when '1' then 'Si'
when '0' then 'No'
when 'E' then 'Excepción'
else ''
end	  									as "Nacionalidad",
    
case "DF"."ind_direccion"
when '1' then 'Si'
when '0' then 'No'
when 'E' then 'Excepción'
else ''
end	  									as "CodPostal",

case "DF"."ind_telefono" 
when '1' then 'Si'
when '0' then 'No'
when 'E' then 'Excepción'
else ''
end					     				as "NumTelefono", 
	
case "DF"."ind_ordenes"
when '1' then 'Si'
when '0' then 'No'
when 'E' then 'Excepción'
else ''
end										as "OrdenPermanente",    
''										as "PoderEfectivo",    
	
case "DF"."ind_benef_empresa"
when '1' then 'Si'
when '0' then 'No'
else ''
end										as "Beneficiario", 
-----------------------------------------------------------------Datos Relacionado										
IFNULL('-','-') 						as "NumIdentificacion",
IFNULL('-','-') 						as "TipoPersonaRel",
IFNULL('-','-')  						as "TipoRelacion",
IFNULL('-','-') 					 	as "ClasificacionFatcaRel",
IFNULL('-','-') 						as "ClienteFatcaRel",
IFNULL('-','-') 						as "UsPersonRel",
IFNULL('-','-')							as "ResidenciaRel",
IFNULL('-','-')							as "LugarNacimientoRel",
IFNULL('-','-')							as "PaisConstitucionRel",
IFNULL('-','-')							as "CasaMatrizRel",
IFNULL('-','-')							as "NacionalidadRel",
IFNULL('-','-')							as "CodPostalRel",
IFNULL('-','-')							as "NumTelefonoRel",
IFNULL('-','-')						    as "OrdenPermanenteRel",
IFNULL('-','-') 					  	as "PoderEfectivoRel",
IFNULL('-','-')							as "BeneficiarioRel",	
"DF"."IdCliente"						as "IdCliente",										   
"CF"."CliFechaModificacion"			    as "FechaCargaInformacion"   

FROM "BSCL.Normativa.BD::NOR.ClienteFatca" as "CF"
INNER JOIN :v_CliSinRelacion as "CSR" on ("CSR"."IdCliente" = "CF"."IdCliente") 
INNER JOIN "BSCL.Normativa.BD::NOR.DatosFatca" as "DF" on ("CF"."IdCliente" = "DF"."IdCliente" and "DF"."Estado" = 'V') --quitar estado y reemplazar por filtro fecha
--LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaFatca" as "PF" on ("PF"."IdPersona" = "PRD"."IdPersonaDestino" )
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" as "CP" on ("CP"."IdPersona" = "CF"."IdPersona" )
--cruzar con relacionados
;
SELECT 'v_salida_previa',* FROM :v_salida_previa "SP";

--select * FROM :v_ClientesSoloDestino;

--select * FROM "BSCL.Normativa.BD::NOR.ClienteFatca" where "IdPersona" = 1752706

--select * from :v_CliSinRelacion where "IdPersona" = 621871;

--select * from "BSCL.Normativa.BD::NOR.DatosFatca" where "IdPersona" = 621871;

--select * from :v_CliSinRelacion;

--select * from :v_ClientesRelacionadosOrigen  where "IdPersonaOrigen" = 1752706;




/*

select count(1) as sin_rela FROM "BSCL.Normativa.BD::NOR.ClienteFatca" as "CF"
INNER JOIN :v_CliSinRelacion as "CSR" on ("CSR"."IdCliente" = "CF"."IdCliente") 
INNER JOIN "BSCL.Normativa.BD::NOR.DatosFatca" as "DF" on ("CF"."IdCliente" = "DF"."IdCliente" and "DF"."Estado" = 'V') --quitar estado y reemplazar por filtro fecha
--LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaFatca" as "PF" on ("PF"."IdPersona" = "PRD"."IdPersonaDestino" )
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" as "CP" on ("CP"."IdPersona" = "CF"."IdPersona" );

select count(1) as personas
FROM "BSCL.Normativa.BD::NOR.ClienteFatca" as "CF"
INNER JOIN :v_PersonasRelacionadasDestino as "PRD" on ("CF"."IdPersona" = "PRD"."IdPersonaOrigen") 
INNER JOIN "BSCL.Normativa.BD::NOR.DatosFatca" as "DF" on ("CF"."IdCliente" = "DF"."IdCliente" and "DF"."Estado" = 'V') --quitar estado y reemplazar por filtro fecha
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaFatca" as "PF" on ("PF"."IdPersona" = "PRD"."IdPersonaDestino" )
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" as "CP" on ("CP"."IdPersona" = "PRD"."IdPersonaDestino" );



select count(1) as clicli
FROM   :v_ClientesRelacionadosOrigen  as "CRO"
INNER JOIN "BSCL.Normativa.BD::NOR.DatosFatca" as "DF" on ("CRO"."IdCliente" = "DF"."IdCliente" and "DF"."Estado" = 'V') --quitar estado y reemplazar por filtro fecha
INNER JOIN :v_ClientesRelacionadosDestino as "CRD" on ("CRO"."IdPersonaOrigen" = "CRD"."IdPersonaOrigen" AND "CRO"."IdPersonaDestino" = "CRD"."IdPersonaDestino")
LEFT  JOIN "BSCL.Normativa.BD::NOR.ClienteFatca" as "CFR" on ("CRD"."IdPersonaDestino" = "CFR"."IdPersona")
LEFT  JOIN "BSCL.Normativa.BD::NOR.DatosFatca" as "DFR" on ("CFR"."IdCliente" = "DFR"."IdCliente" and "DFR"."Estado" = 'V') --quitar estado y reemplazar por filtro fecha
LEFT  JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" as "CP" on ("CP"."IdPersona" = "CRO"."IdPersonaOrigen")
LEFT  JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" as "CPR" on ("CPR"."IdPersona" = "CFR"."IdPersona");

*/


--select * from "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaFatca"
--select * from "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona"

/*select * from "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" 
where "DocumentoIdentidad" = '11329552-K'*/
--1752706

--select * FROM "BSCL.Normativa.BD::NOR.ClienteFatca"


/*

select * from "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono"

select * from "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaFatca"
select * from "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarClasificacionFatca"

select * from "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarClasificacionEmpresaFatca"

*/
--select * from :v_salida_previa;

select count(1) into v_condfinal from :v_salida_previa;
	
IF(	v_condfinal > 0)
THEN

p_out_TablaSalida = 
select 
--0 								as "IdClienteFatca" , 
"SP"."Rut" 						as "Rut",    
"SP"."TipoPersona"				as "TipoPersona",
case "SP"."Indicios"	
when '1' then 'Si'
when '0' then 'No'
when 'E' then 'Excepción'
else ''
end			    
								as "Indicios",    
"SP"."ClasificacionFatca"		as "ClasificacionFatca",    
"SP"."ClienteFatca" 			as "ClienteFatca",    
"SP"."AutorizaOrgFatca"			as "AutorizaOrgFatca",    
"SP"."PatrimonioClp"			as "PatrimonioClp",    
"SP"."PatrimonioUsd"			as "PatrimonioUsd",    
"SP"."FechaEnrolamiento"		as "FechaEnrolamiento",    
"SP"."FechaModIndicios"			as "FechaModIndicios",    
"SP"."FechaModFacta"			as "FechaModFacta",    
"SP"."Residencia"				as "Residencia", 
"SP"."LugarNacimiento"			as "LugarNacimiento",
"SP"."PaisConstitucion"			as "PaisConstitucion",    
"SP"."CasaMatriz"				as "CasaMatriz",    
"SP"."Nacionalidad"				as "Nacionalidad",
"SP"."CodPostal"				as "CodPostal",
"SP"."NumTelefono"				as "NumTelefono", 
"SP"."OrdenPermanente"			as "OrdenPermanente",    
"SP"."PoderEfectivo"			as "PoderEfectivo",    
"SP"."Beneficiario"				as "Beneficiario", 
---------------------------Datos Relacionado										
"SP"."NumIdentificacion"		as "NumIdentificacion",
"SP"."TipoPersonaRel"			as "TipoPersonaRel",
"SP"."TipoRelacion"				as "TipoRelacion",
"SP"."ClasificacionFatcaRel"	as "ClasificacionFatcaRel",
"SP"."ClienteFatcaRel"			as "ClienteFatcaRel",
"SP"."UsPersonRel"				as "UsPersonRel",
"SP"."ResidenciaRel"			as "ResidenciaRel",
"SP"."LugarNacimientoRel"		as "LugarNacimientoRel",
"SP"."PaisConstitucionRel"		as "PaisConstitucionRel",
"SP"."CasaMatrizRel"			as "CasaMatrizRel",
"SP"."NacionalidadRel"			as "NacionalidadRel",
"SP"."CodPostalRel"				as "CodPostalRel",
"SP"."NumTelefonoRel"			as "NumTelefonoRel",
"SP"."OrdenPermanenteRel"		as "OrdenPermanenteRel",
--"SP"."PoderEfectivoRel"			as "PoderEfectivoRel",
'No' 							as "PoderEfectivoRel",
"SP"."BeneficiarioRel"			as "BeneficiarioRel",	
"SP"."FechaCargaInformacion"	as "FechaCargaInformacion"
from :v_salida_previa "SP"
where
"SP"."ClasificacionFatca"  = (case when p_ClasificacionFatca='0' then "SP"."ClasificacionFatca" else p_ClasificacionFatca end) 
and 
"SP"."ClasificacionFatca" <> 'NOUSENTITY'
and 
"SP"."ClasificacionFatca" <> 'NOUSPERSON'
and 
"SP"."ClasificacionFatca" is not null
and 
"SP"."IdCliente" = (case when p_IdCliente=0 then "SP"."IdCliente" else p_IdCliente end)
and
"SP"."Indicios" = (case when p_registroIndicios='-1' then "SP"."Indicios" else p_registroIndicios end) 
and 
"SP"."FechaCargaInformacion" = (case when p_FecEjec = '0001-01-01' then "SP"."FechaCargaInformacion" else p_FecEjec end)
and 
CAST ("SP"."FechaEnrolamiento"  AS DATE) >= (case when p_FecEnrol = '0001-01-01' then "SP"."FechaEnrolamiento"   else p_FecEnrol end) -- todos los clientes enrolados en una fecha igual o anterior a la seleccionada en el filtro “Fecha de Enrolamiento”
and 
CAST (IFNULL("SP"."FechaModIndicios" ,'2000-01-01') AS DATE) >= (case when p_FecDesde = '0001-01-01' then IFNULL("SP"."FechaModIndicios" ,'2000-01-01')   else p_FecDesde end)   
and
CAST (IFNULL("SP"."FechaModIndicios" ,'2030-01-01')AS DATE)  <= (case when p_FecHasta = '9999-01-01' then IFNULL("SP"."FechaModIndicios" ,'2030-01-01')   else p_FecHasta end)   
and 
"SP"."PatrimonioClp" >= (case when p_MontoMin = 0 then "SP"."PatrimonioClp" else p_MontoMin end)
and 
"SP"."PatrimonioClp" <= (case when p_MontoMax = 0 then "SP"."PatrimonioClp" else p_MontoMax end)
and
"SP"."TipoPersona" = (case when p_TipoPersona = '0' then "SP"."TipoPersona" else TRIM(p_TipoPersona) end) 
;
ELSE

select
--0 as "IdClienteFatca",
'' as "Rut",    
'' as "TipoPersona",
'' as "Indicios",    
'' as "ClasificacionFatca",    
'' as "ClienteFatca",    
'' as "AutorizaOrgFatca",    
'' as "PatrimonioClp",    
'' as "PatrimonioUsd",    
'' as "FechaEnrolamiento",    
'' as "FechaModIndicios",    
'' as "FechaModFacta",    
'' as "Residencia", 
'' as "LugarNacimiento",
'' as "PaisConstitucion",    
'' as "CasaMatriz",    
'' as "Nacionalidad",
'' as "CodPostal",
'' as "NumTelefono", 
'' as "OrdenPermanente",    
'' as "PoderEfectivo",    
'' as "Beneficiario", 
'' as "NumIdentificacion",
'' as "TipoPersonaRel",
'' as "TipoRelacion",
'' as "ClasificacionFatcaRel",
'' as "ClienteFatcaRel",
'' as "UsPersonRel",
'' as "ResidenciaRel",
'' as "LugarNacimientoRel",
'' as "PaisConstitucionRel",
'' as "CasaMatrizRel",
'' as "NacionalidadRel",
'' as "CodPostalRel",
'' as "NumTelefonoRel",
'' as "OrdenPermanenteRel",
'' as "PoderEfectivoRel",
'' as "BeneficiarioRel",	
'' as "FechaCargaInformacion"

from dummy where 1=2;

END IF;
	
END;


