SET SCHEMA "BSCL";  

DROP PROCEDURE "BSCL.Normativa.BD.Servicios.NOR::NCG380Tra";


CREATE PROCEDURE "BSCL.Normativa.BD.Servicios.NOR::NCG380Tra"
( 
    -- Parametros de Entrada
 	IN p_IdSolicitudCircular 		  BIGINT DEFAULT 0, 
	IN p_TablaParametros    		  TABLE("TipoParametro" nvarchar(100) , "Valor" nvarchar(100))
    --VARIABLES DE SALIDA 
	,OUT p_out_TablaSalida	 		"BSCL.Normativa.BD::NOR.SALIDA_NCG380TRA" --Se debe llamar siemte p_out_TablaSalida

   
)  	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
AS
  BEGIN


  	declare v_ParametrosSolicitudFini DATE DEFAULT '0001-01-01';
	declare v_ParametrosSolicitudFter DATE DEFAULT '2050-01-01';
	declare v_Secuencia int;
	DECLARE v_IdDocumento,v_IdEmpresa, v_Count INT:=0;
	
	declare v_CondFiltrosRut 	 int DEFAULT 0;
	declare v_CondFinal			 int DEFAULT 0;
	declare v_CondFecha          int DEFAULT 0;
	
	declare v_TTra				 int DEFAULT 0;
	declare v_CondTodos		 	 int DEFAULT 0;
	
	
	DECLARE v_TrabajadoresFiltradosFecha     TABLE("DocumentoIdentidadRut" NVARCHAR(100),"FechaInicioCargo" DATE);
	DECLARE v_TrabajadoresFecha              TABLE("DocumentoIdentidadRut" NVARCHAR(100),"FechaInicioCargo" DATE);
	DECLARE v_ParametrosSolicitudRut         TABLE("TipoParametro" NVARCHAR(100),"Valor" NVARCHAR(100));
   
   
	        	


--------------------
--LOGICA
--------------------


v_ParametrosSolicitudRut = select '' as "TipoParametro", '' as "Valor" from dummy where 1=2;

v_TrabajadoresFiltradosFecha = select '' as "DocumentoIdentidadRut" , '' as "FechaInicioCargo" from dummy where 1=2;
v_TrabajadoresFecha = select '' as "DocumentoIdentidadRut" , '' as "FechaInicioCargo" from dummy where 1=2;

--select top 1 "IdSolicitudCircular" into p_IdSolicitudCircular from "BSCL.Normativa.BD::NOR.SolicitudCircular" where "IdDocumento" = p_IdDocumento;
--Valores tabla parametros
SELECT  COUNT(1) INTO v_Count FROM :p_TablaParametros;			
if v_Count>0 THEN
	SELECT TOP 1  "Valor" INTO v_IdEmpresa
		FROM :p_TablaParametros SOL WHERE SOL."TipoParametro"='IdEmpresa';
		
	v_Datos = SELECT 
		  CASE WHEN SOL."TipoParametro"='fechaDesde'	THEN "Valor" ELSE NULL END AS "FINI"
		, CASE WHEN SOL."TipoParametro"='fechaHasta'	THEN "Valor" ELSE NULL END AS "FTER"
		FROM :p_TablaParametros SOL;
END IF;

v_ParametrosSolicitud = select '' as "TipoParametro", '' as "Valor" from dummy where 1=2;



SELECT TOP 1 "IdEmpresa" INTO v_IdEmpresa 
		FROM "BSCL.Normativa.BD::DOC.Documento"
		WHERE "IdTipoDocumento" = 'NCG380TRA' order by "IdDocumento" desc;



select top 1 "Valor" into v_ParametrosSolicitudFini from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'FINI';

select count(1) into v_CondFecha from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" 
where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular 
and "SP"."TipoParametro" = 'FTER';

IF(v_CondFecha >0) THEN
select top 1 "Valor" into v_ParametrosSolicitudFter from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'FTER';
END IF;


--rescato valores de checkbox todos

select count(1) into v_CondTodos   from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'TEMPL';


--rescato valores de checkbox todos

IF(v_CondTodos>0)
THEN
select "SP"."Valor" into v_TTra   from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'TEMPL';
END IF;


select count(1) into v_CondFiltrosRut from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" 
where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular 
and "SP"."TipoParametro" = 'RUTEMPL';

--sólo rescato filtro por rut si es que no se seleccionó botón "todos"

IF v_TTra = 0 THEN
		IF v_CondFiltrosRut > 0 THEN 
		
		v_ParametrosSolicitudRut = 
		select "SP"."TipoParametro" as "TipoParametro", 
		"SP"."Valor" as "Valor" 
		--(ltrim(rtrim(substring("SP"."Valor",1,LENGTH("SP"."Valor")-2)))) as "Valor" 
		from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" 
		where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular 
		and "SP"."TipoParametro" = 'RUTEMPL';
		
		END IF;	
END IF;


v_TrabajadoresFecha= 
		select 
		(ltrim(rtrim(substring("DOC"."DocumentoIdentidad",1,LENGTH("DOC"."DocumentoIdentidad")-2)))) as "DocumentoIdentidadRut",
		"CE"."FechaInicioCargo" as "FechaInicioCargo"
		from 
		"BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" as "CE"
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as "DOC" 
		on ("CE"."IdDocumentoIdentidad"="DOC"."IdDocumentoIdentidad")
		where
		CAST("CE"."FechaInicioCargo" AS DATE) >= v_ParametrosSolicitudFini;



--para agregar ejecutivos
/*
v_TrabajadoresFecha = select * from :v_TrabajadoresFecha union all


SELECT
(ltrim(rtrim(substring("DOC"."DocumentoIdentidad",1,LENGTH("DOC"."DocumentoIdentidad")-2)))) as "DocumentoIdentidadRut",
TO_DATE('01-01-1900','DD-MM-YYYY') as "FechaInicioCargo"
FROM  "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" as "CE"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" as "CP" on ("CE"."IdPersonaEjecutivo" = "CP"."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as "DOC" 
on ("CP"."IdDocumentoIdentidad"="DOC"."IdDocumentoIdentidad");
*/





--primera condición, boton"todos" sin marcar
IF (v_TTra = 0) THEN 

		--condicion 1.1 ,  filtro ingresado
		IF (v_CondFiltrosRut > 0) THEN
		
		
		v_TrabajadoresFiltradosFecha= 
		select 
		"TF"."DocumentoIdentidadRut" as "DocumentoIdentidadRut",
		"TF"."FechaInicioCargo" as "FechaInicioCargo"
		from 
		:v_TrabajadoresFecha as "TF"
		where
		 "TF"."DocumentoIdentidadRut" in (select distinct "PSR"."Valor" as "DocumentoIdentidadRut" from :v_ParametrosSolicitudRut as "PSR" ); 

		
		END IF;	
		
		--condicion 1.2 todos sin marcar y sin filtros ingresados
		IF (v_CondFiltrosRut = 0) THEN 

		v_TrabajadoresFiltradosFecha = 
		select 
		"TF"."DocumentoIdentidadRut" as "DocumentoIdentidadRut",
		"TF"."FechaInicioCargo" as "FechaInicioCargo"
		from 
		:v_TrabajadoresFecha as "TF" ;
		
		
		END IF;	
		
		




		
END IF;	

--segunda condición, botón todos rut marcado
IF (v_TTra = 1) THEN 
		
		--condicion 2.1 sin filtros ingresados
		IF (v_CondFiltrosRut = 0) THEN 
		
		
		v_TrabajadoresFiltradosFecha = 
		select 
		"TF"."DocumentoIdentidadRut" as "DocumentoIdentidadRut",
		"TF"."FechaInicioCargo" as "FechaInicioCargo"
		from 
		:v_TrabajadoresFecha as "TF" 
		; 
		END IF;
		
	
		
END IF;	

select count(1) into v_CondFinal from :v_TrabajadoresFiltradosFecha;


IF (v_CondFinal > 0) THEN 

p_out_TablaSalida=
Select "EMPLEADO".*
from(
select 
(select case when "DOC"."IdTipoDocumentoIdentidad" = 'R' then (select(ltrim(rtrim(substring("DOC"."DocumentoIdentidad",1,LENGTH("DOC"."DocumentoIdentidad")-2))))from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as "DOC" where "CE"."IdDocumentoIdentidad"= "DOC"."IdDocumentoIdentidad") else '' end 
from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as "DOC" where "CE"."IdDocumentoIdentidad" = "DOC"."IdDocumentoIdentidad" )as "Rut",
(select
case when "DOC"."IdTipoDocumentoIdentidad" = 'R' 
then (select (ltrim(rtrim(substring("DOC"."DocumentoIdentidad",LENGTH("DOC"."DocumentoIdentidad"),LENGTH("DOC"."DocumentoIdentidad")))))from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as "DOC" where "CE"."IdDocumentoIdentidad" = "DOC"."IdDocumentoIdentidad") 
else '' 
end 
from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as "DOC" where "CE"."IdDocumentoIdentidad" = "DOC"."IdDocumentoIdentidad" )as "DIG",
(select
 case when "DOC"."IdTipoDocumentoIdentidad" = 'P' 
 then (select "DOC"."DocumentoIdentidad" from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as "DOC" where "CE"."IdDocumentoIdentidad" = "DOC"."IdDocumentoIdentidad") 
 else '' 
 end 
 from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as "DOC" where "CE"."IdDocumentoIdentidad" = "DOC"."IdDocumentoIdentidad" ) as "Pasaporte",
"CE"."ApellidoPaterno" as "ApellidoPaterno",
"CE"."ApellidoMaterno" as "ApellidoMaterno",
"CE"."Nombres" as "PrimerNombre" ,
"CE"."Nombres" as "SegundoNombre",
"TEL"."Numero" as "Telefono",
"CE"."IdDireccionElectronicaPrincipal" as "Email",
'' as "TipoDireccion",
"DIRF"."Calle" as "DireccionNombre",
"DIRF"."Numero" as "DireccionNumero",
"DIRF"."Departamento" as "DireccionDepto",
"DIRFIS"."NombreDivisionPais" as "DireccionComuna",
"DIRFIS"."NombreCiudad" as "DireccionCiudad",
'EstadoProvincia'as "DireccionEstado",
"PAIS"."NombrePais" as "DireccionPais",
"DIRF"."CodigoPostal" as "DireccionCodPostal",
"PAIS"."NombrePais" as "Nacionalidad",
"CE"."FechaInicioCargo" as "FechaIniFunciones",
"CE"."FechaTerminoCargo" as "FechaTerFunciones",
"CE"."NombreTipoCargoEmpleado" as "Funcion",
--'-' as "FechaAcreditacion",
"CE"."FechaIngreso" as "FechaAcreditacion",
case 
when "IND"."ValorIndicador" = 'N' then '0'
when "IND"."ValorIndicador" = 'S' then '1'
else '0'
end
as "InfoPrivilegiada"
from 
"BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" as  "CE" 
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionFisica" as "DIRF" on ("DIRF"."IdDireccionFisica" = "CE"."IdDireccionFisicaPrincipal" AND "DIRF"."Borrado"='N') 
LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" as "PAIS" on ("PAIS"."IdPais" = "CE"."IdPaisNacimiento") 
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Telefono" as "TEL" on ("TEL"."IdTelefono" = "CE"."IdTelefonoPrincipal") 
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" as "DIRFIS" on ("DIRFIS"."IdDireccionFisica" = "CE"."IdDireccionFisicaPrincipal") 
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPersona"  as "IND" on ("IND"."IdUso"='IND' AND "IND"."CodigoTipoIndicador"='DERPRIV' AND  "CE"."IdPersona" = "IND"."IdPersona")
) as "EMPLEADO"
INNER JOIN :v_TrabajadoresFiltradosFecha as "TFF" on ("TFF"."DocumentoIdentidadRut" = "EMPLEADO"."Rut");


ELSE
p_out_TablaSalida=
select 
0 as "Rut"				
,''  as "DIG"			 
,''  as "Pasaporte"		 
,''  as "ApellidoPaterno" 
,''  as "ApellidoMaterno" 
,''  as "PrimerNombre" 	 
,''  as "SegundoNombre"	 
,''  as "Telefono"		 
,''  as "Email"			 
,''  as "TipoDireccion"	 
,''  as "DireccionNombre" 
,''  as "DireccionNumero" 
,''  as "DireccionDepto"	
,''  as "DireccionComuna" 
,''  as "DireccionCiudad" 
,''  as "DireccionEstado" 
,''  as "DireccionPais"	 
,''  as "DireccionCodPostal"
,''  as "Nacionalidad"		 
,''  as "FechaIniFunciones"	
,''  as "FechaTerFunciones"	
,''  as "Funcion"			 
,''  as "FechaAcreditacion"		
,''  as "InfoPrivilegiada"	
from dummy where 1=2;




END IF;



--SELECT * FROM  :p_out_TablaSalida;

END;









