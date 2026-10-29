SET SCHEMA "BSCL";  

DROP PROCEDURE "BSCL.Normativa.BD.Servicios.FATCA::ConsultarDatosFatcaResumen";

--CALL "BSCL.Normativa.BD.Servicios.NOR::ExportarCircular"(336,'FATCAR',1046547)
--select * from "BSCL.Normativa.BD::NOR.SolicitudParametros" where "IdSolicitudCircular" =384
--call "BSCL.Normativa.BD.Servicios.FATCA::ConsultarDatosFatcaResumen"(384,?);

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios.FATCA::ConsultarDatosFatcaResumen"
( 

    -- Parametros de Entrada
 	IN p_IdSolicitudCircular 		  BIGINT DEFAULT 0, 
	IN p_TablaParametros    		  TABLE("TipoParametro" nvarchar(100) , "Valor" nvarchar(100))
    --VARIABLES DE SALIDA 
	,OUT p_out_TablaSalida	 		 "BSCL.Normativa.BD::NOR.SALIDA_FATCARESUMEN" --Se debe llamar siemte p_out_TablaSalida
			  
   
  
)  	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
AS
  BEGIN
  
  
  --declare v_LogCondicion BIGINT DEFAULT 0;


  --DECLARARVARIABLES

declare v_LogCondicion,p_IdCliente,v_condfinal,v_cond_mmin,v_cond_mmax BIGINT DEFAULT 0;


declare p_MontoMin, p_MontoMax decimal(26,4) DEFAULT 0;

declare p_FecEjec, p_FecEnrol, p_FecDesde  NVARCHAR(30) default '0001-01-01';

declare p_FecHasta  NVARCHAR(30) default '9999-01-01';	

declare p_UsoFecha NVARCHAR(30) default '';	

declare p_ClasificacionFatca,p_IndicadorFatca,p_registroIndicios,p_registroIndiciosFinal NVARCHAR(20) default '';

declare p_TipoPersona  NVARCHAR(10) default '0';

 



select count(1) into v_cond_mmin from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'MMIN';
 
select count(1) into v_cond_mmax  from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'MMAX';



IF(v_cond_mmin>0)
THEN

select top 1 CAST(TRIM(IFNULL("Valor",'0')) AS DECIMAL (26,8))  into p_MontoMin from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'MMIN';

END IF;

IF(v_cond_mmin>0)
THEN

select top 1 CAST(TRIM(IFNULL("Valor",'0')) AS DECIMAL (26,8))  into p_MontoMax from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'MMAX';

END IF;




select top 1 IFNULL("Valor",'0001-01-01') into p_FecEjec from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'FEJEC';

select top 1 IFNULL("Valor",'0001-01-01') into p_FecEnrol from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'FENR';

select top 1 IFNULL("Valor",'0001-01-01') into p_FecDesde from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'FEDES';

select top 1 IFNULL("Valor",'9999-01-01') into p_FecHasta from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'FEHAS';

--select top 1 IFNULL("Valor",'') into p_UsoFecha from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'UFEC';

select top 1 IFNULL("Valor",'') into p_ClasificacionFatca from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'CFAT';

select top 1 IFNULL("Valor",'') into p_IndicadorFatca from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'IFAT';

select top 1 IFNULL("Valor",'') into p_TipoPersona from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'TP';

select top 1 (case when "Valor" = 'true' then '0' when "Valor" = 'false' then '1' else '-1' end ) into p_registroIndicios from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'RIND';

select top 1 IFNULL("Valor",0)  into p_IdCliente from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'IDCLI';


--select top 1 CAST(TRIM(IFNULL("Valor",'0')) AS DECIMAL (26,4))   from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'MMAX';


--select * from "BSCL.Normativa.BD::NOR.SolicitudParametros" where "TipoParametro" = 'RIND'

select p_FecEjec as fecej ,p_FecEnrol as fecenrol, p_ClasificacionFatca as clasifi,p_IdCliente as pcli,
p_TipoPersona as tpersona,p_registroIndicios as regindicios ,p_FecDesde fdesde,p_FecHasta fhasta,p_MontoMin as monmin,p_MontoMax
as monmax from dummy;

--------------------
--LOGICA
--------------------


/*
select "DF"."fecha_ult_modificacion",*  
FROM "BSCL.Normativa.BD::NOR.ClienteFatca" as "CF"
INNER JOIN "BSCL.Normativa.BD::NOR.DatosFatca" as "DF" on ("CF"."IdCliente" = "DF"."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" as "CP" on ("CP"."IdPersona" = "CF"."IdPersona")
where "CF"."IdCliente" = '1421452'
and 

"CF"."IdCliente" = "CF"."IdCliente"
and
"DF"."ind_us_person" = 1
and 
"DF"."FechaCargaInformacion" = "DF"."FechaCargaInformacion" 
and 
CAST ("DF"."fecha_enrolamiento"  AS DATE) >=  "DF"."fecha_enrolamiento" 	 -- todos los clientes enrolados en una fecha igual o anterior a la seleccionada en el filtro “Fecha de Enrolamiento”
and CAST (IFNULL("DF"."fecha_ult_modificacion",'2020-01-01') AS DATE) > '2016-01-01'-- p_FecDesde 
and
--CAST ("DF"."fecha_ult_modificacion" AS DATE)  < p_FecHasta;
*/

select count(1) into v_condfinal
FROM "BSCL.Normativa.BD::NOR.ClienteFatca" as "CF"
INNER JOIN "BSCL.Normativa.BD::NOR.DatosFatca" as "DF" on ("CF"."IdCliente" = "DF"."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" as "CP" on ("CP"."IdPersona" = "CF"."IdPersona")
where 
"CF"."IdCliente" = (case when p_IdCliente=0 then "CF"."IdCliente" else p_IdCliente end)
and
"DF"."ind_us_person" = (case when p_registroIndicios='' then "DF"."ind_us_person" else p_registroIndicios end) 
and 
"DF"."FechaCargaInformacion" = (case when p_FecEjec = '0001-01-01' then "DF"."FechaCargaInformacion" else p_FecEjec end)
and 
CAST ("DF"."fecha_enrolamiento"  AS DATE) >= (case when p_FecEnrol = '0001-01-01' then "DF"."fecha_enrolamiento"  else p_FecEnrol end) -- todos los clientes enrolados en una fecha igual o anterior a la seleccionada en el filtro “Fecha de Enrolamiento”
and CAST (IFNULL("DF"."fecha_ult_modificacion",'2000-01-01') AS DATE) >= (case when p_FecDesde = '0001-01-01' then "DF"."fecha_ult_modificacion"  else p_FecDesde end)  
and
CAST (IFNULL("DF"."fecha_ult_modificacion",'2030-01-01') AS DATE)  <= (case when p_FecHasta = '0001-01-01' then "DF"."fecha_ult_modificacion"  else p_FecHasta end);



IF(v_condfinal>0)

then

p_out_TablaSalida = 
select 

"CP"."DocumentoIdentidad" 				as "Rut",    
"DF"."tipo_persona" 					as "TipoPersona",    
case "CF"."IndUsPerson"
when '1' then 'Si'
when '0' then 'No'
when 'E' then 'Excepción'
else ''
end				 					    as "Indicios",    
"CF"."ClasificacionFatca" 				as "ClasificacionFatca",    
"DF"."indicador_fatca" 					as "ClienteFatca",    
case "DF"."auth_fatca"  
when '1' then 'Si'
when '0' then 'No'
when 'E' then 'Excepción'
else ''
end					 					as "AutorizaOrgFatca",    
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
'No'										as "PoderEfectivo",    
	
case "DF"."ind_benef_empresa"
when '1' then 'Si'
when '0' then 'No'
else ''
end	
										as "Beneficiario",    
"DF"."FechaCargaInformacion"			as "FechaCargaInformacion"   

FROM "BSCL.Normativa.BD::NOR.ClienteFatca" as "CF"
INNER JOIN "BSCL.Normativa.BD::NOR.DatosFatca" as "DF" on ("CF"."IdCliente" = "DF"."IdCliente" and "Estado" = 'V')
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" as "CP" on ("CP"."IdPersona" = "CF"."IdPersona")
where
"DF"."clasificacion_fatca"  = (case when p_ClasificacionFatca='0' then "DF"."clasificacion_fatca" else p_ClasificacionFatca end) 
and 
"DF"."clasificacion_fatca" <> 'NOUSENTITY'
and 
"DF"."clasificacion_fatca" <> 'NOUSPERSON'
and 
"DF"."clasificacion_fatca" is not null
and 
"DF"."IdCliente" = (case when p_IdCliente=0 then "DF"."IdCliente" else p_IdCliente end)
and
"DF"."ind_us_person" = (case when p_registroIndicios='-1' then "DF"."ind_us_person" else p_registroIndicios end) 
and 
"DF"."FechaCargaInformacion" = (case when p_FecEjec = '0001-01-01' then "DF"."FechaCargaInformacion" else p_FecEjec end)
and 
CAST ("DF"."fecha_enrolamiento"  AS DATE) >= (case when p_FecEnrol = '0001-01-01' then "DF"."fecha_enrolamiento"   else p_FecEnrol end) -- todos los clientes enrolados en una fecha igual o anterior a la seleccionada en el filtro “Fecha de Enrolamiento”
and 
CAST (IFNULL("DF"."fecha_ult_modificacion",'2000-01-01')  AS DATE) >= (case when p_FecDesde = '0001-01-01' then IFNULL("DF"."fecha_ult_modificacion",'2000-01-01')   else p_FecDesde end)   
and
CAST (IFNULL("DF"."fecha_ult_modificacion",'2030-01-01')  AS DATE)  <= (case when p_FecHasta = '9999-01-01' then IFNULL("DF"."fecha_ult_modificacion",'2030-01-01')   else p_FecHasta end)   
and 
"DF"."patrimonio_peso" >= (case when p_MontoMin = 0 then "DF"."patrimonio_peso" else p_MontoMin end)
and 
"DF"."patrimonio_peso" <= (case when p_MontoMax = 0 then "DF"."patrimonio_peso" else p_MontoMax end)
and
"DF"."tipo_persona" = (case when p_TipoPersona = '0' then "DF"."tipo_persona" else TRIM(p_TipoPersona) end) 


--select * from "BSCL.Normativa.BD::NOR.DatosFatca"
/*
where 
"CF"."IdCliente" = (case when p_IdCliente=0 then "CF"."IdCliente" else p_IdCliente end)
and
"DF"."ind_us_person" = (case when p_registroIndicios=-1 then "DF"."ind_us_person" else p_registroIndicios end) 
and 
"DF"."FechaCargaInformacion" = (case when p_FecEjec = '0001-01-01' then "DF"."FechaCargaInformacion" else p_FecEjec end)
and 
CAST ("DF"."fecha_enrolamiento"  AS DATE) >= (case when p_FecEnrol = '0001-01-01' then "DF"."fecha_enrolamiento"  else p_FecEnrol end) -- todos los clientes enrolados en una fecha igual o anterior a la seleccionada en el filtro “Fecha de Enrolamiento”
and 
CAST ("DF"."fecha_ult_modificacion" AS DATE) >  p_FecDesde 
and
CAST ("DF"."fecha_ult_modificacion" AS DATE)  < p_FecHasta
and 
"CF"."ClasificacionFatca" <> 'NOUSENTITY'
and 
"CF"."ClasificacionFatca" <> 'NOUSPERSON'
*/

;

ELSE

p_out_TablaSalida = 

select  
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
'' as "FechaCargaInformacion"  
from dummy where 1=2;

END IF;




 


/*
--("DF"."patrimonio_peso"	 >= p_MontoMin and "DF"."patrimonio_peso"	 <= p_MontoMax)     --INCLUIR CASOS X SI NO LLEGAN LOS DATOS 
--and 
"DF"."FechaCargaInformacion" = (case when p_FecEjec = '0001-01-01' then "DF"."FechaCargaInformacion" else p_FecEjec end)
and 
"DF"."fecha_enrolamiento"  >= (case when p_FecEnrol = '0001-01-01' then "DF"."fecha_enrolamiento"  else p_FecEnrol end) -- todos los clientes enrolados en una fecha igual o anterior a la seleccionada en el filtro “Fecha de Enrolamiento”
and 
"CF"."ClasificacionFatca"  = (case when p_ClasificacionFatca=0 then "CF"."ClasificacionFatca"  else p_ClasificacionFatca end)
--INCLUIR INDICADOR FATCA...revisar crm y ver que hacer
and 
--UPPER(LEFT("DF"."tipo_persona",1))  = UPPER((case when p_TipoPersona='' then "DF"."tipo_persona" else p_TipoPersona end))
--and tipo de persona viene como descripcion, debo corregir en generar datos fatca 
"DF"."ind_us_person" = p_registroIndicios --(case when p_registroIndicios='' then "CF"."IndUsPerson" else p_registroIndicios end) --NO ENTIENDO RQ
--"CF"."IndUsPerson" = (case when p_registroIndicios='' then "CF"."IndUsPerson" else p_registroIndicios end) --NO ENTIENDO RQ
--and
--"DF"."fecha_ult_modificacion" >  p_FecDesde 
--and
--"DF"."fecha_ult_modificacion" < p_FecHasta
--corregir la data con fec ult mod
;
*/

--select * from "BSCL.Normativa.BD::NOR.ClienteFatca"
--select * from "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaFatca"
END;


