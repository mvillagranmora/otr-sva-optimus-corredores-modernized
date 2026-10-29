SET SCHEMA "BSCL";  

DROP PROCEDURE "BSCL.Normativa.BD.Servicios.NOR::NCG380Cli";

--call "BSCL.Normativa.BD.Servicios.NOR::NCG380Cli"(2800,?)

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios.NOR::NCG380Cli"
( 
    -- Parametros de Entrada
 	IN p_IdSolicitudCircular 		  BIGINT DEFAULT 0, 
	IN p_TablaParametros    		  TABLE("TipoParametro" nvarchar(100) , "Valor" nvarchar(100))
    --VARIABLES DE SALIDA 
	,OUT p_out_TablaSalida	 		"BSCL.Normativa.BD::NOR.SALIDA_NCG380Cli" --Se debe llamar siemte p_out_TablaSalida

     
)  	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
AS
  BEGIN
  
   	--------------------
	--DECLARE Manejo de Errores
	--------------------	  
/*	IMPLEMENTAR MANEJO DE ERRORES!!!!*/
  	
  	declare v_ParametrosSolicitudFini DATE DEFAULT '0001-01-01';
	declare v_ParametrosSolicitudFter DATE DEFAULT '2050-01-01';
	
	declare v_ParametrosOficio VARCHAR(100) DEFAULT NULL;
	declare v_Secuencia int;
	DECLARE v_IdDocumento,v_IdEmpresa, v_Count INT:=0;
	
	declare v_TRut		 int DEFAULT 0;	
	
	declare v_CondFiltrosRut 	 int DEFAULT 0;
	declare v_CondFinal			 int DEFAULT 0;
	declare v_CondTRut			 int DEFAULT 0;
	declare v_CondFecha          int DEFAULT 0;
	declare v_CondParaRut        int DEFAULT 0;
	
	DECLARE v_ClientesFiltradosFecha    TABLE("IdCliente" NVARCHAR(40),"FechaRegistro" DATE);
--	DECLARE v_ParametrosSolicitudRut    TABLE("TipoParametro" NVARCHAR(100),"Valor" NVARCHAR(100));
    DECLARE v_ClientesFiltradosFechaRUT TABLE("DocumentoIdentidadRut" NVARCHAR(100),"FechaRegistro" DATE);
    DECLARE v_ClientesFiltradosFinal    TABLE("DocumentoIdentidadRut" NVARCHAR(100),"FechaRegistro" DATE);
	DECLARE v_FecSistema date;
	DECLARE v_SecuenciaTablaGlobal BIGINT DEFAULT 0;


--------------------
--LOGICA
--------------------

--select top 1 "IdSolicitudCircular" into p_IdSolicitudCircular from "BSCL.Normativa.BD::NOR.SolicitudCircular" where "IdDocumento" = p_IdDocumento;
--Rescato valores tabla parametros /para futura nueva forma de llamada 

SELECT  COUNT(1) INTO v_Count FROM :p_TablaParametros;			
if v_Count>0 THEN
	SELECT TOP 1  "Valor" INTO v_IdEmpresa
		FROM :p_TablaParametros SOL WHERE SOL."TipoParametro"='IdEmpresa';
		
	v_Datos = SELECT 
		  CASE WHEN SOL."TipoParametro"='fechaDesde'	THEN "Valor" ELSE NULL END AS "FINI"
		, CASE WHEN SOL."TipoParametro"='fechaHasta'	THEN "Valor" ELSE NULL END AS "FTER"
		FROM :p_TablaParametros SOL;
END IF;


--obtengo id empresa segun tipo documento

SELECT TOP 1 "IdEmpresa" INTO v_IdEmpresa 
		FROM "BSCL.Normativa.BD::DOC.Documento"
		WHERE "IdTipoDocumento" = 'NCG380CLI' order by "IdDocumento" desc;

--obtengo Fecha de sistema
select Top 1 "Valor" into v_FecSistema from "BSCL.Plataforma.BD::PLA.Configuracion" where "CodigoParametro" = 'PLA.FechaSistema' and "IdEmpresa" = v_IdEmpresa ;


v_ClientesFiltradosFecha 			= select '' as "IdCliente", '' as "FechaRegistro" from dummy where 1=2;
v_ParametrosSolicitud 				= select '' as "TipoParametro", '' as "Valor" from dummy where 1=2;
v_ParametrosSolicitudRut 			= select '' as "TipoParametro", '' as "Valor" from dummy where 1=2;
v_ClientesFiltradosFinal            = select '' as "DocumentoIdentidadRut", '' as "FechaRegistro" from dummy where 1=2;

--obtengo Fecha Inicio
select top 1 "Valor" into v_ParametrosSolicitudFini from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'FINI';

--revioso si existe ingresado como filtro la fecha de termino(hasta)
select count(1) into v_CondFecha from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" 
where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular 
and "SP"."TipoParametro" = 'FTER';


IF(v_CondFecha >0) THEN
--si existe , guardo valor en v_ParametrosSolicitudFter
select top 1 "Valor" into v_ParametrosSolicitudFter from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'FTER';
END IF;

--se quita temporalmente oficio, hasta que se defina forma de asociacion al archivo
--select top 1 "Valor" into v_ParametrosOficio from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'OFI';

--pregunto si se ingreso valor para check UI Todos Clientes
select count(1) into v_CondTRut from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'TODOS_CLI';


IF v_CondTRut > 0 THEN
--si existe info para check UI Todos Clientes, guardo valor
select top 1 "Valor" into v_TRut from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'TODOS_CLI';
END IF;	


	
--consulto servicio que proporciona la informacion mas actual de cada cliente - segun datos NCG380-	
call "BSCL.Normativa.BD.Servicios.NOR::ConsultarClienteOnline"(IdSecuenciaGlobal=>:v_SecuenciaTablaGlobal);
		
		

/*
select count(1) into v_CondFiltrosRut from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" 
where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular 
and "SP"."TipoParametro" = 'RUT';
*/
--se guarda el rut sin D.V.

--select v_TRut as todosRUT from dummy;
--si el check todos clientes es false 
IF v_TRut = 0 THEN
	--reviso si se ingresó algun cliente como filtro(1 cliente o listado de clientes)
select count(1) into v_CondParaRut from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" 
where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular 
and "SP"."TipoParametro" = 'RUT';
END IF;	

--si el boton todos clientes no está marcado, pero se ingresaron clientes (listado o 1)
IF (v_TRut = 0 and v_CondParaRut >0 )THEN
--guardo info en tabla v_ParametrosSolicitudRut
	v_ParametrosSolicitudRut = 
		select 
		"SP"."TipoParametro" as "TipoParametro", 
		--IFNULL("SP"."Valor",'') as "Valor" 
		(ltrim(rtrim(substring("SP"."Valor",1,LENGTH("SP"."Valor")-2)))) as "Valor" 
		from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" 
		where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular 
		and "SP"."TipoParametro" = 'RUT';
		
		
	ELSE
	v_ParametrosSolicitudRut = 
		select 
		'' as "TipoParametro", 
		'' as "Valor" 
		from dummy where 1=2;
END IF;	




--select v_ParametrosSolicitudFini as fecini,v_FecSistema as fecsistema,v_ParametrosSolicitudFter as fter,  p_IdSolicitudCircular as solcir, v_TRut as todosrut,v_CondParaRut as condpararut from dummy;


--select * from :v_ParametrosSolicitudRut;


--Si se solicita data diaria, se utiliza sólo vista cliente online

IF(v_ParametrosSolicitudFini = v_FecSistema AND (v_ParametrosSolicitudFter = v_FecSistema  OR  v_ParametrosSolicitudFter >= v_FecSistema  )) THEN

	--si el boton todos clientes no está marcado, pero si se ingresaron clientes (listado o 1)
	IF (v_TRut = 0 and v_CondParaRut >0 ) THEN
	
	p_out_TablaSalida=
			
			select 
			"CO"."Rut"				 as   "RutCodigoCliente", --Se remplaza "IdCliente"  por el Rut , se muestra en la columna N° de cliente
			"CO"."Rut"						     as   "Rut",
			"CO"."Dig"						     as   "Dig",
			"CO"."Pasaporte"					 as   "Pasaporte",
			"CO"."Glei"						     as   "Glei",
			"CO"."ApellidoPaterno"			     as   "ApellidoPaterno",
			"CO"."ApellidoMaterno"			     as   "ApellidoMaterno",
			"CO"."PrimerNombre"				     as   "PrimerNombre",
			"CO"."SegundoNombre"				 as   "SegundoNombre",
			"CO"."RazonSocial"				     as   "RazonSocial",
			"CO"."Telefono"					     as   "Telefono",
			"CO"."Email"	 					 as   "Email",
			"CO"."DireccionTipo"			     as   "DireccionTipo",
			"CO"."DireccionNombre"		         as   "DireccionNombre",
			"CO"."DireccionNumero"		         as   "DireccionNumero",
			"CO"."DireccionDepto"		         as   "DireccionDepto",
			"CO"."DireccionComuna"	 	         as   "DireccionComuna",
			"CO"."DireccionCiudad"	 	         as   "DireccionCiudad",
			"CO"."DireccionEstado"		         as   "DireccionEstado",
			"CO"."DireccionPais"	 		     as   "DireccionPais",
			"CO"."CodigoPostal"	 		         as   "CodigoPostal",
			"CO"."Nacionalidad"	 		         as   "Nacionalidad",
			"CO"."RelacionIntermediario"		 as  "RelacionIntermediario",
			"CO"."AdministracionCartera"	 	 as  "AdministracionCartera",
			"CO"."RutEntidadEmpleadora"		     as  "RutEntidadEmpleadora",
			"CO"."DigEntidadEmpleadora"		     as  "DigEntidadEmpleadora",
			"CO"."RazonSocialEntidadEmpleadora"	 as  "RazonSocialEntidadEmpleadora",
			"CO"."EstadoCivil"				     as  "EstadoCivil",
--------Campos Conyuge			 
			"CO"."ConyRut"					     as  "ConyRut",
			"CO"."ConyDig"					     as  "ConyDig",
			"CO"."ConyPasaporte"				 as  "ConyPasaporte",
			"CO"."ConyApellidoPaterno"		     as  "ConyApellidoPaterno",
			"CO"."ConyApellidoMaterno"		     as  "ConyApellidoMaterno",
			"CO"."ConyPrimerNombre"			     as  "ConyPrimerNombre",
			"CO"."ConySegundoNombre"			 as  "ConySegundoNombre",
			"CO"."ConyRazonSocial"			     as  "ConyRazonSocial",
			"CO"."ConyTelefono"				     as  "ConyTelefono",
			"CO"."ConyEmail"	 				 as  "ConyEmail",
			"CO"."ConyDireccionTipo"	 		 as  "ConyDireccionTipo",
			"CO"."ConyDireccionNombre"	 	     as  "ConyDireccionNombre",
			"CO"."ConyDireccionNumero"	 	     as  "ConyDireccionNumero",
			"CO"."ConyDireccionDepto"	 	     as  "ConyDireccionDepto",
			"CO"."ConyDireccionComuna"	 	     as  "ConyDireccionComuna",
			"CO"."ConyDireccionCiudad"	 	     as  "ConyDireccionCiudad",
			"CO"."ConyDireccionEstado"	 	     as  "ConyDireccionEstado",
			"CO"."ConyDireccionPais"	 		 as  "ConyDireccionPais",
			"CO"."ConyCodigoPostal"	 		     as  "ConyCodigoPostal",
			"CO"."ConyNacionalidad"	 		     as  "ConyNacionalidad",
			"CO"."ConyRelacionIntermediario"	 as  "ConyRelacionIntermediario",
--------Campos Representante Legal
			"CO"."RepRut"					    as  "RepRut",
			"CO"."RepDig"					    as  "RepDig",
			"CO"."RepPasaporte"				    as  "RepPasaporte",
			"CO"."RepGlei"					    as  "RepGlei",
			"CO"."RepApellidoPaterno"		    as  "RepApellidoPaterno",
			"CO"."RepApellidoMaterno"		    as  "RepApellidoMaterno",
			"CO"."RepPrimerNombre"			    as  "RepPrimerNombre",
			"CO"."RepSegundoNombre"			    as  "RepSegundoNombre",
			"CO"."RepRazonSocial"			    as  "RepRazonSocial",
			"CO"."RepTelefono"				    as  "RepTelefono",
			"CO"."RepEmail"					    as  "RepEmail",
			"CO"."RepDireccionTipo"	 		    as  "RepDireccionTipo",
			"CO"."RepDireccionNombre"	 	    as  "RepDireccionNombre",
			"CO"."RepDireccionNumero"	 	    as  "RepDireccionNumero",
			"CO"."RepDireccionDepto"	 		as  "RepDireccionDepto",
			"CO"."RepDireccionComuna"	 	    as  "RepDireccionComuna",
			"CO"."RepDireccionCiudad"	 	    as  "RepDireccionCiudad",
			"CO"."RepDireccionProvincia"	 	as  "RepDireccionProvincia",
			"CO"."RepDireccionPais"	 		    as  "RepDireccionPais",
			"CO"."RepCodigoPostal"	 		    as  "RepCodigoPostal",
			"CO"."RepNacionalidad"	 		    as  "RepNacionalidad",
			"CO"."RepRelacionIntermediario"		as  "RepRelacionIntermediario",
			"CO"."RepEstadoCivil"				as  "RepEstadoCivil",
--------Campos Conyugue Representante		
			"CO"."ConyRepRut"				    as  "ConyRepRut",
			"CO"."ConyRepDig"				    as  "ConyRepDig",
			"CO"."ConyRepPasaporte"			    as  "ConyRepPasaporte",
			"CO"."ConyRepApellidoPaterno"	    as  "ConyRepApellidoPaterno",
			"CO"."ConyRepApellidoMaterno"	    as  "ConyRepApellidoMaterno",
			"CO"."ConyRepPrimerNombre"		    as  "ConyRepPrimerNombre",
			"CO"."ConyRepSegundoNombre"		    as  "ConyRepSegundoNombre",
			"CO"."ConyRepRazonSocial"		    as  "ConyRepRazonSocial",
			"CO"."ConyRepTelefono"			    as  "ConyRepTelefono",
			"CO"."ConyRepEmail"	 			    as  "ConyRepEmail",
			"CO"."ConyRepDireccionTipo"	 	    as  "ConyRepDireccionTipo",
			"CO"."ConyRepDireccionNombre"	    as  "ConyRepDireccionNombre",
			"CO"."ConyRepDireccionNumero"	    as  "ConyRepDireccionNumero",
			"CO"."ConyRepDireccionDepto"	 	as  "ConyRepDireccionDepto",
			"CO"."ConyRepDireccionComuna"	    as  "ConyRepDireccionComuna",
			"CO"."ConyRepDireccionCiudad"	    as  "ConyRepDireccionCiudad",
			"CO"."ConyRepDireccionEstado"		as  "ConyRepDireccionEstado",
			"CO"."ConyRepDireccionPais"	 	    as  "ConyRepDireccionPais",
			"CO"."ConyRepCodigoPostal"	 	    as  "ConyRepCodigoPostal",
			"CO"."ConyRepNacionalidad"	 	    as  "ConyRepNacionalidad",
			"CO"."ConyRepRelacionIntermediario"	as 	"ConyRepRelacionIntermediario",
			"CO"."CuentaBancariaUno"	 		as "CuentaBancariaUno",
			"CO"."CuentaBancariaDos"	 		as "CuentaBancariaDos",
			"CO"."CuentaBancariaTres"	 	    as "CuentaBancariaTres",
			"CO"."ClaseOrden"	 			    as  "ClaseOrden",
			"CO"."Perfil"	 				    as  "Perfil",
			"CO"."PerfilAnterior"	 		    as  "PerfilAnterior",
			"CO"."FechaInicioPerfilActual"	    as  "FechaInicioPerfilActual",
			'' as "TipoPersona",
			'' as "TipoCliente",
			'' as "FechaEnrolamiento",
			'' as "ContratoFirmadoNCG30",
			'' as "FechaSuscripcion",
			'' as "ConfirmadoPor",
			'' as "EstadoCliente",
			'' as "ComentarioBloqueo",
			'' as "ContratoCustodia",
			'' as "ConflictoInteres",
			'' as "InfoPrivilegiadaArt171",
			'' as "CargoSVS171",
			'' as "RelCorredoraArt100",
			'' as "RelCorredora_NCG380",
			'' as "RelCorredora_CIRC1064",
			'' as "Cargo_NCG_68_69",
			'' as "PersonaExpuestaPoliticamente",
			'' as "NumeroContrato"
			from "BSCL.Normativa.BD::NOR.GL_SALIDA_CLIENTE_ONLINE" AS "CO"
			INNER JOIN :v_ParametrosSolicitudRut as "PSR" on ("PSR"."Valor" = "CO"."Rut")
			where "IdClienteOnline" = v_SecuenciaTablaGlobal
			;
		

		
		
		ELSE
		p_out_TablaSalida=
			select 
			"CO"."Rut"				 as   "RutCodigoCliente", --Se remplaza "IdCliente"  por el Rut , se muestra en la columna N° de cliente
			"CO"."Rut"						     as   "Rut",
			"CO"."Dig"						     as   "Dig",
			"CO"."Pasaporte"					 as   "Pasaporte",
			"CO"."Glei"						     as   "Glei",
			"CO"."ApellidoPaterno"			     as   "ApellidoPaterno",
			"CO"."ApellidoMaterno"			     as   "ApellidoMaterno",
			"CO"."PrimerNombre"				     as   "PrimerNombre",
			"CO"."SegundoNombre"				 as   "SegundoNombre",
			"CO"."RazonSocial"				     as   "RazonSocial",
			"CO"."Telefono"					     as   "Telefono",
			"CO"."Email"	 					 as   "Email",
			"CO"."DireccionTipo"			     as   "DireccionTipo",
			"CO"."DireccionNombre"		         as   "DireccionNombre",
			"CO"."DireccionNumero"		         as   "DireccionNumero",
			"CO"."DireccionDepto"		         as   "DireccionDepto",
			"CO"."DireccionComuna"	 	         as   "DireccionComuna",
			"CO"."DireccionCiudad"	 	         as   "DireccionCiudad",
			"CO"."DireccionEstado"		         as   "DireccionEstado",
			"CO"."DireccionPais"	 		     as   "DireccionPais",
			"CO"."CodigoPostal"	 		         as   "CodigoPostal",
			"CO"."Nacionalidad"	 		         as   "Nacionalidad",
			"CO"."RelacionIntermediario"		 as  "RelacionIntermediario",
			"CO"."AdministracionCartera"	 	 as  "AdministracionCartera",
			"CO"."RutEntidadEmpleadora"		     as  "RutEntidadEmpleadora",
			"CO"."DigEntidadEmpleadora"		     as  "DigEntidadEmpleadora",
			"CO"."RazonSocialEntidadEmpleadora"	 as  "RazonSocialEntidadEmpleadora",
			"CO"."EstadoCivil"				     as  "EstadoCivil",
--------Campos Conyuge			 
			"CO"."ConyRut"					     as  "ConyRut",
			"CO"."ConyDig"					     as  "ConyDig",
			"CO"."ConyPasaporte"				 as  "ConyPasaporte",
			"CO"."ConyApellidoPaterno"		     as  "ConyApellidoPaterno",
			"CO"."ConyApellidoMaterno"		     as  "ConyApellidoMaterno",
			"CO"."ConyPrimerNombre"			     as  "ConyPrimerNombre",
			"CO"."ConySegundoNombre"			 as  "ConySegundoNombre",
			"CO"."ConyRazonSocial"			     as  "ConyRazonSocial",
			"CO"."ConyTelefono"				     as  "ConyTelefono",
			"CO"."ConyEmail"	 				 as  "ConyEmail",
			"CO"."ConyDireccionTipo"	 		 as  "ConyDireccionTipo",
			"CO"."ConyDireccionNombre"	 	     as  "ConyDireccionNombre",
			"CO"."ConyDireccionNumero"	 	     as  "ConyDireccionNumero",
			"CO"."ConyDireccionDepto"	 	     as  "ConyDireccionDepto",
			"CO"."ConyDireccionComuna"	 	     as  "ConyDireccionComuna",
			"CO"."ConyDireccionCiudad"	 	     as  "ConyDireccionCiudad",
			"CO"."ConyDireccionEstado"	 	     as  "ConyDireccionEstado",
			"CO"."ConyDireccionPais"	 		 as  "ConyDireccionPais",
			"CO"."ConyCodigoPostal"	 		     as  "ConyCodigoPostal",
			"CO"."ConyNacionalidad"	 		     as  "ConyNacionalidad",
			"CO"."ConyRelacionIntermediario"	 as  "ConyRelacionIntermediario",
--------Campos Representante Legal
			"CO"."RepRut"					    as  "RepRut",
			"CO"."RepDig"					    as  "RepDig",
			"CO"."RepPasaporte"				    as  "RepPasaporte",
			"CO"."RepGlei"					    as  "RepGlei",
			"CO"."RepApellidoPaterno"		    as  "RepApellidoPaterno",
			"CO"."RepApellidoMaterno"		    as  "RepApellidoMaterno",
			"CO"."RepPrimerNombre"			    as  "RepPrimerNombre",
			"CO"."RepSegundoNombre"			    as  "RepSegundoNombre",
			"CO"."RepRazonSocial"			    as  "RepRazonSocial",
			"CO"."RepTelefono"				    as  "RepTelefono",
			"CO"."RepEmail"					    as  "RepEmail",
			"CO"."RepDireccionTipo"	 		    as  "RepDireccionTipo",
			"CO"."RepDireccionNombre"	 	    as  "RepDireccionNombre",
			"CO"."RepDireccionNumero"	 	    as  "RepDireccionNumero",
			"CO"."RepDireccionDepto"	 		as  "RepDireccionDepto",
			"CO"."RepDireccionComuna"	 	    as  "RepDireccionComuna",
			"CO"."RepDireccionCiudad"	 	    as  "RepDireccionCiudad",
			"CO"."RepDireccionProvincia"	 	as  "RepDireccionProvincia",
			"CO"."RepDireccionPais"	 		    as  "RepDireccionPais",
			"CO"."RepCodigoPostal"	 		    as  "RepCodigoPostal",
			"CO"."RepNacionalidad"	 		    as  "RepNacionalidad",
			"CO"."RepRelacionIntermediario"		as  "RepRelacionIntermediario",
			"CO"."RepEstadoCivil"				as  "RepEstadoCivil",
--------Campos Conyugue Representante		
			"CO"."ConyRepRut"				    as  "ConyRepRut",
			"CO"."ConyRepDig"				    as  "ConyRepDig",
			"CO"."ConyRepPasaporte"			    as  "ConyRepPasaporte",
			"CO"."ConyRepApellidoPaterno"	    as  "ConyRepApellidoPaterno",
			"CO"."ConyRepApellidoMaterno"	    as  "ConyRepApellidoMaterno",
			"CO"."ConyRepPrimerNombre"		    as  "ConyRepPrimerNombre",
			"CO"."ConyRepSegundoNombre"		    as  "ConyRepSegundoNombre",
			"CO"."ConyRepRazonSocial"		    as  "ConyRepRazonSocial",
			"CO"."ConyRepTelefono"			    as  "ConyRepTelefono",
			"CO"."ConyRepEmail"	 			    as  "ConyRepEmail",
			"CO"."ConyRepDireccionTipo"	 	    as  "ConyRepDireccionTipo",
			"CO"."ConyRepDireccionNombre"	    as  "ConyRepDireccionNombre",
			"CO"."ConyRepDireccionNumero"	    as  "ConyRepDireccionNumero",
			"CO"."ConyRepDireccionDepto"	 	as  "ConyRepDireccionDepto",
			"CO"."ConyRepDireccionComuna"	    as  "ConyRepDireccionComuna",
			"CO"."ConyRepDireccionCiudad"	    as  "ConyRepDireccionCiudad",
			"CO"."ConyRepDireccionEstado"		as  "ConyRepDireccionEstado",
			"CO"."ConyRepDireccionPais"	 	    as  "ConyRepDireccionPais",
			"CO"."ConyRepCodigoPostal"	 	    as  "ConyRepCodigoPostal",
			"CO"."ConyRepNacionalidad"	 	    as  "ConyRepNacionalidad",
			"CO"."ConyRepRelacionIntermediario"	as 	"ConyRepRelacionIntermediario",
			"CO"."CuentaBancariaUno"	 		as "CuentaBancariaUno",
			"CO"."CuentaBancariaDos"	 		as "CuentaBancariaDos",
			"CO"."CuentaBancariaTres"	 	    as "CuentaBancariaTres",
			"CO"."ClaseOrden"	 			    as  "ClaseOrden",
			"CO"."Perfil"	 				    as  "Perfil",
			"CO"."PerfilAnterior"	 		    as  "PerfilAnterior",
			"CO"."FechaInicioPerfilActual"	    as  "FechaInicioPerfilActual",
			'' as "TipoPersona",
			'' as "TipoCliente",
			'' as "FechaEnrolamiento",
			'' as "ContratoFirmadoNCG30",
			'' as "FechaSuscripcion",
			'' as "ConfirmadoPor",
			'' as "EstadoCliente",
			'' as "ComentarioBloqueo",
			'' as "ContratoCustodia",
			'' as "ConflictoInteres",
			'' as "InfoPrivilegiadaArt171",
			'' as "CargoSVS171",
			'' as "RelCorredoraArt100",
			'' as "RelCorredora_NCG380",
			'' as "RelCorredora_CIRC1064",
			'' as "Cargo_NCG_68_69",
			'' as "PersonaExpuestaPoliticamente",
			'' as "NumeroContrato"
			from "BSCL.Normativa.BD::NOR.GL_SALIDA_CLIENTE_ONLINE" AS "CO"
			where "IdClienteOnline" = v_SecuenciaTablaGlobal;
			
		END IF;

ELSE 


--select * from "BSCL.Normativa.BD.Vistas::NOR.ConsultarClienteOnline"




--desde historico
-- si no se está consultando información del día, buscamos el último registro por cada cliente anterior a la fecha de termino (desde filtro)
		v_ClientesFiltradosFecha = select "HIST"."IdCliente" as "IdCliente" , MAX("HIST"."FechaRegistro") as "FechaRegistro" from "BSCL.Normativa.BD::NOR.NCG380RegistroHistoricoCliente" as "HIST" 
		where "HIST"."FechaRegistro" <= v_ParametrosSolicitudFter group by "HIST"."IdCliente";


--v_ClientesFiltradosFechaRUT tiene los rut y fecha registro, previamente filtrado por fecha 
-- posee los mismos clientes de tabla anterior, pero con info de fecha registro y rut
		v_ClientesFiltradosFechaRUT = 
		select 
		distinct "HIST"."DocumentoIdentidadRut" as "DocumentoIdentidadRut",
		"CFF"."FechaRegistro" as "FechaRegistro" 
		from "BSCL.Normativa.BD::NOR.NCG380RegistroHistoricoCliente" as "HIST"  
		inner join :v_ClientesFiltradosFecha as "CFF" 
		on 
		("HIST"."IdCliente" = "CFF"."IdCliente")
		and 
		("CFF"."FechaRegistro" = "HIST"."FechaRegistro");



		--agregar if de condicion listado rut
		--cruce entre clientes filtrados por fecha y los rut ingresados por parametro
		IF (v_TRut = 0 and v_CondParaRut >0)
		THEN
				v_ClientesFiltradosFinal = 
				select "PSR"."Valor" as "DocumentoIdentidadRut",
				"CFFR"."FechaRegistro"  
				as "FechaRegistro" 
				from :v_ParametrosSolicitudRut as "PSR" 
				left join 
				:v_ClientesFiltradosFechaRUT as "CFFR" 
				on 
				("PSR"."Valor" = "CFFR"."DocumentoIdentidadRut");
		--END IF;
		ELSE
		
		--almaceno solo los rut que se encuentran entre las fechas solicitadas, sin cruce con parametro rut
		--IF v_TRut = 1 THEN
		v_ClientesFiltradosFinal = 
		select "CFFR"."DocumentoIdentidadRut" as "DocumentoIdentidadRut",
		"CFFR"."FechaRegistro"  as "FechaRegistro" 
		from  :v_ClientesFiltradosFechaRUT 
		as "CFFR" 
		;
		END IF;



--finalmente, se une la tabla historica, con tabla acotada segun filtros :v_ClientesFiltradosFinal 
		p_out_TablaSalida=
		
		select 
		"HIST"."DocumentoIdentidadRut" as "RutCodigoCliente", --Se remplaza "IdCliente"  por el Rut , se muestra en la columna N° de cliente
		"HIST"."DocumentoIdentidadRut" as "Rut",
		"HIST"."DocumentoIdentidadDig" as "Dig",
		"HIST"."DocumentoIdentidadPasaporte" as "Pasaporte",
		"HIST"."Glei" as "Glei",
		"HIST"."ApellidoPaterno" as "ApellidoPaterno",
		"HIST"."ApellidoMaterno" as "ApellidoMaterno",
		"HIST"."PrimerNombre" as "PrimerNombre",
		"HIST"."SegundoNombre" as "SegundoNombre",
		"HIST"."RazonSocial" as "RazonSocial",
		"HIST"."Telefono" as "Telefono",
		"HIST"."DireccionElectronica" as "Email",
		"HIST"."TipoDireccion" as "DireccionTipo",
		"HIST"."Calle" as "DireccionNombre",
		"HIST"."Numero" as "DireccionNumero",
		"HIST"."Departamento" as "DireccionDepto",
		"HIST"."Comuna" as "DireccionComuna",
		"HIST"."Ciudad" as "DireccionCiudad",
		"HIST"."Provincia" as "DireccionEstado",
		"HIST"."Pais" as "DireccionPais",
		"HIST"."CodigoPostal" as "CodigoPostal",
		"HIST"."Nacionalidad" as "Nacionalidad",
		"HIST"."RelacionEntidad" as "RelacionIntermediario",
		"HIST"."ContratoAdminCartera" as "AdministracionCartera",
		"HIST"."EmpleadorRut" as "RutEntidadEmpleadora",
		"HIST"."EmpleadorDig" as "DigEntidadEmpleadora",
		"HIST"."EmpleadorRazonSocial" as "RazonSocialEntidadEmpleadora",
		"HIST"."EstadoCivil" as "EstadoCivil",
		"HIST"."ConyugeRut" as "ConyRut",
		"HIST"."ConyugeDig" as "ConyDig",
		"HIST"."ConyugePasaporte" as "ConyPasaporte",
		"HIST"."ConyugeApellPaterno" as "ConyApellidoPaterno",
		"HIST"."ConyugeApellMaterno" as "ConyApellidoMaterno",
		"HIST"."ConyugePrimerNombre" as "ConyPrimerNombre",
		"HIST"."ConyugeSegundoNombre" as "ConySegundoNombre",
		"HIST"."ConyugeRazonSocial" as "ConyRazonSocial",
		"HIST"."ConyugeTelefono" as "ConyTelefono",
		"HIST"."ConyugeDireccionElectronica" as "ConyEmail",
		"HIST"."ConyugeTipoDireccion" as "ConyDireccionTipo",
		"HIST"."ConyugeCalle" as "ConyDireccionNombre",
		"HIST"."ConyugeNumero" as "ConyDireccionNumero",
		"HIST"."ConyugeDepartamento" as "ConyDireccionDepto",
		"HIST"."ConyugeComuna" as "ConyDireccionComuna",
		"HIST"."ConyugeCiudad" as "ConyDireccionCiudad",
		"HIST"."ConyugeProvincia" as "ConyDireccionEstado",
		"HIST"."ConyugePais" as "ConyDireccionPais",
		"HIST"."ConyugeCodigoPostal" as "ConyCodigoPostal",
		"HIST"."ConyugeNacionalidad" as "ConyNacionalidad",
		"HIST"."ConyugeRelacionEntidad" as "ConyRelacionIntermediario",
		"HIST"."RepLegalRut" as "RepRut",
		"HIST"."RepLegalDig" as "RepDig",
		"HIST"."RepLegalPasaporte" as "RepPasaporte",
		"HIST"."RepLegalGlei" as "RepGlei",
		"HIST"."RepLegalApellPaterno" as "RepApellidoPaterno",
		"HIST"."RepLegalApellMaterno" as "RepApellidoMaterno",
		"HIST"."RepLegalPrimerNombre" as "RepPrimerNombre",
		"HIST"."RepLegalSegundoNombre" as "RepSegundoNombre",
		"HIST"."RepLegalRazonSocial" as "RepRazonSocial",
		"HIST"."RepLegalTelefono" as "RepTelefono",
		"HIST"."RepLegalDireccionElectronica" as "RepEmail",
		"HIST"."RepLegalTipoDireccion" as "RepDireccionTipo",
		"HIST"."RepLegalCalle" as "RepDireccionNombre",
		"HIST"."RepLegalNumero" as "RepDireccionNumero",
		"HIST"."RepLegalDepartamento" as "RepDireccionDepto",
		"HIST"."RepLegalComuna" as "RepDireccionComuna",
		"HIST"."RepLegalCiudad" as "RepDireccionCiudad",
		"HIST"."RepLegalProvincia" as "RepDireccionProvincia",
		"HIST"."RepLegalPais" as "RepDireccionPais",
		"HIST"."RepLegalCodigoPostal" as "RepCodigoPostal",
		"HIST"."RepLegalNacionalidad" as "RepNacionalidad",
		"HIST"."RepLegalRelacionEntidad" as "RepRelacionIntermediario",
		"HIST"."RepLegalEstadoCivil" as "RepEstadoCivil",
		"HIST"."ConyRepLegalRut" as "ConyRepRut",
		"HIST"."ConyRepLegalDig" as "ConyRepDig",
		"HIST"."ConyRepLegalPasaporte" as "ConyRepPasaporte",
		"HIST"."ConyRepLegalApellPaterno" as "ConyRepApellidoPaterno",
		"HIST"."ConyRepLegalApellMaterno" as "ConyRepApellidoMaterno",
		"HIST"."ConyRepLegalPrimerNombre" as "ConyRepPrimerNombre",
		"HIST"."ConyRepLegalSegundoNombre" as "ConyRepSegundoNombre",
		"HIST"."ConyRepLegalRazonSocial" as "ConyRepRazonSocial",
		"HIST"."ConyRepLegalTelefono" as "ConyRepTelefono",
		"HIST"."ConyRepLegalDireccionElectronica" as "ConyRepEmail",
		"HIST"."ConyRepLegalTipoDireccion" as "ConyRepDireccionTipo",
		"HIST"."ConyRepLegalCalle" as "ConyRepDireccionNombre",
		"HIST"."ConyRepLegalNumero" as "ConyRepDireccionNumero",
		"HIST"."ConyRepLegalDepartamento" as "ConyRepDireccionDepto",
		"HIST"."ConyRepLegalComuna" as "ConyRepDireccionComuna",
		"HIST"."ConyRepLegalCiudad" as "ConyRepDireccionCiudad",
		"HIST"."ConyRepLegalProvincia" as "ConyRepDireccionEstado",
		"HIST"."ConyRepLegalPais" as "ConyRepDireccionPais",
		"HIST"."ConyRepLegalCodigoPostal" as "ConyRepCodigoPostal",
		"HIST"."ConyRepLegalNacionalidad" as "ConyRepNacionalidad",
		"HIST"."ConyRepLegalRelacionEntidad" as "ConyRepRelacionIntermediario",
		"HIST"."CuentaBancariaUno" as "CuentaBancariaUno",
		"HIST"."CuentaBancariaDos" as "CuentaBancariaDos",
		"HIST"."CuentaBancariaTres" as "CuentaBancariaTres",
		"HIST"."ClaseOrden" as "ClaseOrden",
		"HIST"."PerfilActual" as "Perfil",
		"HIST"."PerfilAnterior" as "PerfilAnterior",
		"HIST"."FechaInicioPerfilActual" as "FechaInicioPerfilActual",
		'' as "TipoPersona",
		'' as "TipoCliente",
		'' as "FechaEnrolamiento",
		'' as "ContratoFirmadoNCG30",
		'' as "FechaSuscripcion",
		'' as "ConfirmadoPor",
		'' as "EstadoCliente",
		'' as "ComentarioBloqueo",
		'' as "ContratoCustodia",
		'' as "ConflictoInteres",
		'' as "InfoPrivilegiadaArt171",
		'' as "CargoSVS171",
		'' as "RelCorredoraArt100",
		'' as "RelCorredora_NCG380",
		'' as "RelCorredora_CIRC1064",
		'' as "Cargo_NCG_68_69",
		'' as "PersonaExpuestaPoliticamente",
		'' as "NumeroContrato"
		
		from "BSCL.Normativa.BD::NOR.NCG380RegistroHistoricoCliente" "HIST" 
		inner join :v_ClientesFiltradosFinal as "CFF" 
		on ("HIST"."DocumentoIdentidadRut"  = "CFF"."DocumentoIdentidadRut") 
		and ("HIST"."FechaRegistro" = "CFF"."FechaRegistro" );


END IF;
--p_out_TablaSalida=SELECT '' AS "Salida"  FROM  DUMMY;


SELECT * FROM  :p_out_TablaSalida;	






END;








