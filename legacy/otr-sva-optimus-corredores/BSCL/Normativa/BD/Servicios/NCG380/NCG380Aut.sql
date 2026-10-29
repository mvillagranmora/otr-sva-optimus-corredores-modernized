SET SCHEMA "BSCL";  

DROP PROCEDURE "BSCL.Normativa.BD.Servicios.NCG380::NCG380Aut";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios.NCG380::NCG380Aut"
( 
    -- Parametros de Entrada
 	IN p_IdSolicitudCircular 		  BIGINT DEFAULT 0, 
	IN p_TablaParametros    		  TABLE("TipoParametro" nvarchar(100) , "Valor" nvarchar(100))
    --VARIABLES DE SALIDA 
	,OUT p_out_TablaSalida	 		 "BSCL.Normativa.BD::NOR.SALIDA_NCG380AUT" --Se debe llamar siemte p_out_TablaSalida

      
 
)  	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
AS
  BEGIN
  
--DECLARACIONES	

	declare v_ParametrosSolicitudFini DATE DEFAULT '0001-01-01';
	declare v_ParametrosSolicitudFter DATE DEFAULT '2050-01-01';
	declare v_Secuencia int;	
	DECLARE v_IdDocumento,v_IdEmpresa, v_Count INT:=0;
	
	declare v_CondRut      	 int DEFAULT 0;
	declare v_CondFinal		 int DEFAULT 0;
	declare v_CondAut      	 int DEFAULT 0;
	declare v_CondFecha      int DEFAULT 0;
	declare v_CondRela       int DEFAULT 0;

	declare v_TRut				int DEFAULT 0;	
	declare v_TAut		 		int DEFAULT 0;	
	declare v_Relacion          NVARCHAR(10) default '';
	declare v_TRel		 		int DEFAULT 0;	
	declare v_CondTodosRUT		 	 int DEFAULT 0;
	declare v_CondTodosAUT		 	 int DEFAULT 0;
	
	

	DECLARE v_ParametrosSolicitudRut      TABLE("TipoParametro" NVARCHAR(100),"Valor" NVARCHAR(100));
	DECLARE v_ParametrosSolicitudRutAut   TABLE("TipoParametro" NVARCHAR(100),"Valor" NVARCHAR(100));
   
   --tabla agrupadora de resultados
   	DECLARE v_AuxAutorizados      
   	TABLE(
   	"NumeroFolioCliente"    NVARCHAR(15),
   	"RutCLI"		   NVARCHAR(15),
   	"DigCLI"           NVARCHAR(10),
   	"NumeroPersona"    BIGINT,
   	"Rut" 			   NVARCHAR(15),
   	"Dig" 			   NVARCHAR(10),
   	"Pasaporte" 	   NVARCHAR(100),
   	"Glei" 			   NVARCHAR(512),
   	"ApellidoPaterno"  NVARCHAR(100),
   	"ApellidoMaterno"  NVARCHAR(100),
   	"PrimerNombre" 	   NVARCHAR(100),
   	"SegundoNombre"    NVARCHAR(100),
   	"RazonSocial" 	   NVARCHAR(100),
   	"Telefono" 		   NVARCHAR(100),
   	"Email"            NVARCHAR(100),
   	"DireccionTipo"    NVARCHAR(100),
   	"DireccionNombre"  NVARCHAR(100),
   	"DireccionNumero"  NVARCHAR(100),
   	"DireccionDepto"   NVARCHAR(100),
   	"DireccionComuna"  NVARCHAR(100),
   	"DireccionCiudad"  NVARCHAR(100),
   	"DireccionEstado"  NVARCHAR(100),
   	"DireccionPais"    NVARCHAR(100),
   	"DireccionCodigoPostal" NVARCHAR(100),
   	"Nacionalidad"     NVARCHAR(100),
   	"RelacionIntermediario" NVARCHAR(100),
   	"TipoRelacion" NVARCHAR(100)
   	);
   
    
	        	
--select * from "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionPersona" 

	v_AuxAutorizados = 
	Select 
	'' AS "NumeroFolioCliente", 
	'' AS "RutCLI",
	'' AS "DigCLI",
	0 as "NumeroPersona",   
	'' as "Rut", 			  
	'' as "Dig", 			  
	'' as "Pasaporte", 	  
	'' as "Glei", 			  
	'' as "ApellidoPaterno", 
	'' as "ApellidoMaterno", 
	'' as "PrimerNombre", 	  
	'' as "SegundoNombre",   
	'' as "RazonSocial", 	  
	'' as "Telefono", 		  
	'' as "Email",           
	'' as "DireccionTipo",   
	'' as "DireccionNombre", 
	0 as "DireccionNumero", 
	'' as "DireccionDepto",  
	'' as "DireccionComuna", 
	'' as "DireccionCiudad",
	'' as "DireccionEstado", 
	'' as "DireccionPais",   
	'' as "DireccionCodigoPostal", 
	'' as "Nacionalidad",
	'' as "RelacionIntermediario",
	'' as "TipoRelacion"
	from dummy where 1=2;




--------------------
--LOGICA
--------------------


--select top 1 "IdSolicitudCircular" into p_IdSolicitudCircular from "BSCL.Normativa.BD::NOR.SolicitudCircular" where "IdDocumento" = p_IdDocumento;

--Rescato Valores tabla parametros

SELECT  COUNT(1) INTO v_Count FROM :p_TablaParametros;			
if v_Count>0 THEN
	SELECT TOP 1  "Valor" INTO v_IdEmpresa
		FROM :p_TablaParametros SOL WHERE SOL."TipoParametro"='IdEmpresa';
		
	v_Datos = SELECT 
		  CASE WHEN SOL."TipoParametro"='fechaDesde'	THEN "Valor" ELSE NULL END AS "FINI"
		, CASE WHEN SOL."TipoParametro"='fechaHasta'	THEN "Valor" ELSE NULL END AS "FTER"
		FROM :p_TablaParametros SOL;
END IF;


--Rescato id empresa

SELECT TOP 1 "IdEmpresa" INTO v_IdEmpresa 
		FROM "BSCL.Normativa.BD::DOC.Documento"
		WHERE "IdTipoDocumento" = 'NCG380AUT' order by "IdDocumento" desc;


-- consulto si se seleccionó boton "Todos - Rut"
select count(1) into v_CondTodosRUT   from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'TODOS_CLI';


--guardo valor de que se seleccionaron todos los rut
IF(v_CondTodosRUT>0)
THEN
select "SP"."Valor" into v_TRut   from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'TODOS_CLI';
END IF;


--select 0 into v_CondTodos from dummy;

select count(1) into v_CondTodosAUT   from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'TODOS_AUT';

IF(v_CondTodosAUT>0)
THEN
select "SP"."Valor" into v_TAut  from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'TODOS_AUT';
END IF;


select count(1) into v_CondRut from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'RUT';
select count(1) into v_CondAut from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'RUTAUT';
--select 0 into v_CondAut from dummy;
--select '' into v_Relacion from dummy;



v_ParametrosSolicitudRut = select '' as "TipoParametro", '' as "Valor" from dummy where 1=2;

v_ParametrosSolicitudRutAut = select '' as "TipoParametro", '' as "Valor" from dummy where 1=2;


select count(1) into v_CondRela from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'RELA';

IF(v_CondRela >0) THEN
select top 1 "Valor" into v_Relacion from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'RELA';

ELSE 

select '' into v_Relacion from dummy ;

END IF;

--select * from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = 972 and "SP"."TipoParametro" = 'RELA';



select top 1 "Valor" into v_ParametrosSolicitudFini from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'FINI';

select count(1) into v_CondFecha from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" 
where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular 
and "SP"."TipoParametro" = 'FTER';

IF(v_CondFecha >0) THEN
select top 1 "Valor" into v_ParametrosSolicitudFter from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'FTER';
END IF;


IF v_TRut = 0 THEN

		IF (v_CondRut >0) THEN
			v_ParametrosSolicitudRut = 
			select "SP"."TipoParametro" as "TipoParametro", 
			"SP"."Valor" as "Valor" 
	    	from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" 
			where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular 
			and "SP"."TipoParametro" = 'RUT';
		END IF;	
		
END IF;	

IF v_TAut = 0 THEN

	IF (v_CondAut >0) THEN
		v_ParametrosSolicitudRutAut = 
		select "SP"."TipoParametro" as "TipoParametro", 
		"SP"."Valor" as "Valor" 
    	from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" 
		where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular 
		and "SP"."TipoParametro" = 'RUTAUT';
		
	END IF;	
	
END IF;	






v_ClientesFiltradosFecha = select '' as "DocumentoIdentidadRut" , '' as "FechaInicioCargo" from dummy where 1=2;



--INICIO CONDICIONES


--select top 10 * from "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona";


--1era condicion, botones Todos rut y Todos aut seleccionados
IF (v_TRut = 1) AND (v_TAut = 1) THEN 

	v_AuxAutorizados = 
	select 
	ltrim(rtrim(substring("PERAUT"."DocumentoIdentidad",1,LENGTH("PERAUT"."DocumentoIdentidad")-2))) as "NumeroFolioCliente",
	'' as "RutCLI",	
	'' as "DigCLI",
	"CP"."IdPersonaDestino" as "NumeroPersona",
	case when "PERAUT"."TipoDocumentoIdentidad" = 'R' then
	ltrim(rtrim(substring("PERAUT"."DocumentoIdentidad",1,LENGTH("PERAUT"."DocumentoIdentidad")-2))) 
	else ''
	end
	as "Rut",
	case when "PERAUT"."TipoDocumentoIdentidad" = 'R' then
	(ltrim(rtrim(substring("PERAUT"."DocumentoIdentidad",LENGTH("PERAUT"."DocumentoIdentidad"),LENGTH("CP"."DocumentoIdentidadDefecto")))))
	else ''
	end
	as "Dig",
	case when "PERAUT"."TipoDocumentoIdentidad" = 'P' then
	"PERAUT"."DocumentoIdentidad"
	else ''
	end
	as "Pasaporte", 
	"PERAUT"."GlobalLegalEntity"  as "Glei", 
	"PERAUT"."ApellidoPaterno" as "ApellidoPaterno",
	"PERAUT"."ApellidoMaterno" as "ApellidoMaterno",
	"PERAUT"."Nombres" as "PrimerNombre",
	"PERAUT"."Nombres" as "SegundoNombre",
	case when "PERAUT"."Tipo" = 'J' then
	"PERAUT"."RazonSocial"
	else ''
	end
	 as "RazonSocial",
	"TEL"."Numero" as "Telefono",
	"DIRE"."Direccion" as "Email",
	'' as "DireccionTipo",
	"DIRFIS"."Calle" as "DireccionNombre",
	"DIRFIS"."Numero" as "DireccionNumero",
	"DIRFIS"."Departamento" as "DireccionDepto",
	"DIRFIS"."NombreDivisionPais" as "DireccionComuna",
	"DIRFIS"."NombreCiudad" as "DireccionCiudad",
	'EstadoProvincia' as "DireccionEstado",
	"PAIS"."NombrePais" as "DireccionPais",
	"DIRF"."CodigoPostal" as "DireccionCodigoPostal",
	"PAIS"."NombrePais" as "Nacionalidad",
	"PERAUT"."CodigoTipoCargoEmpleado" as "RelacionIntermediario",
	"CP"."NombreTipoRelacion" AS "TipoRelacion"
	
	from "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionPersona" as "CP"
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "PERAUT" on ("PERAUT"."IdPersona" = "CP"."IdPersonaDestino")
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS "CLIAUT" ON ( "PERAUT"."IdPersona" = "CLIAUT"."IdPersona" )
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.Telefono" as "TEL" on ("TEL"."IdTelefono" = "PERAUT"."IdTelefonoPrincipal") 
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" as "DIRE" on ("DIRE"."IdDireccionElectronica" = "PERAUT"."IdDireccionElectronicaPrincipal" AND "DIRE"."Borrado"='N') 
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionFisica" as "DIRF" on ("DIRF"."IdDireccionFisica" = "PERAUT"."IdDireccionFisicaPrincipal" AND "DIRF"."Borrado"='N') 
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" as "DIRFIS" on ("DIRFIS"."IdDireccionFisica" = "PERAUT"."IdDireccionFisicaPrincipal") 
	LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" as "PAIS" on ("PAIS"."IdPais" = "PERAUT"."IdPaisNacionalidadPrincipal") 
	--para cruce idcli
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "PER" on ("PER"."IdPersona" = "CP"."IdPersonaOrigen")
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS "CLI" ON ( "PER"."IdPersona" = "CLI"."IdPersona" )
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" as "RELCORR" on ("RELCORR"."IdPersona" = "CP"."IdPersonaDestino" AND "RELCORR"."CodigoTipoIndicador" = 'NCG380' AND "RELCORR"."NombreTipoIndicador" = 'Relación Corredora (NCG 380)')
	where "CP"."IdTipoRelacion" = case when (v_Relacion <> 'SIN' AND v_Relacion <> '' AND v_TRel = 0) then v_Relacion else "CP"."IdTipoRelacion" end
	and "CP"."FechaInicioRelacion" >= case when ("CP"."IdTipoRelacion" <> 'PARSOC') then v_ParametrosSolicitudFini else "CP"."FechaInicioRelacion" end
	AND "CLI"."TipoEnrolamiento" = 'FULL'
	--and "FechaFinRelacion" <=   IFNULL (v_ParametrosSolicitudFter,"FechaFinRelacion")
	;

END IF;	

--2da condicion, boton Todos rut activado y Todos aut desactivado

IF (v_TRut = 1) AND (v_TAut = 0) THEN 


	IF (v_CondAut > 0) THEN
	
	v_AuxAutorizados = 
	select 
	ltrim(rtrim(substring("PERAUT"."DocumentoIdentidad",1,LENGTH("PERAUT"."DocumentoIdentidad")-2))) as "NumeroFolioCliente",
	'' as "RutCLI",	
	'' as "DigCLI",
	"CP"."IdPersonaDestino" as "NumeroPersona",
	case when "PERAUT"."TipoDocumentoIdentidad" = 'R' then
	(ltrim(rtrim(substring("PERAUT"."DocumentoIdentidad",1,LENGTH("PERAUT"."DocumentoIdentidad")-2)))) 
	else ''
	end
	as "Rut",
	case when "PERAUT"."TipoDocumentoIdentidad" = 'R' then
	(ltrim(rtrim(substring("PERAUT"."DocumentoIdentidad",LENGTH("PERAUT"."DocumentoIdentidad"),LENGTH("CP"."DocumentoIdentidadDefecto")))))
	else ''
	end
	as "Dig",
	case when "PERAUT"."TipoDocumentoIdentidad" = 'P' then
	"PERAUT"."DocumentoIdentidad"
	else ''
	end
	as "Pasaporte", 
	"PERAUT"."GlobalLegalEntity"  as "Glei", 
	"PERAUT"."ApellidoPaterno" as "ApellidoPaterno",
	"PERAUT"."ApellidoMaterno" as "ApellidoMaterno",
	"PERAUT"."Nombres" as "PrimerNombre",
	"PERAUT"."Nombres" as "SegundoNombre",
	case when "PERAUT"."Tipo" = 'J' then
	"PERAUT"."RazonSocial"
	else ''
	end
	 as "RazonSocial",
	"TEL"."Numero" as "Telefono",
	"DIRE"."Direccion" as "Email",
	'' as "DireccionTipo",
	"DIRFIS"."Calle" as "DireccionNombre",
	"DIRFIS"."Numero" as "DireccionNumero",
	"DIRFIS"."Departamento" as "DireccionDepto",
	"DIRFIS"."NombreDivisionPais" as "DireccionComuna",
	"DIRFIS"."NombreCiudad" as "DireccionCiudad",
	'EstadoProvincia' as "DireccionEstado",
	"PAIS"."NombrePais" as "DireccionPais",
	"DIRF"."CodigoPostal" as "DireccionCodigoPostal",
	"PAIS"."NombrePais" as "Nacionalidad",
	"PERAUT"."CodigoTipoCargoEmpleado" as "RelacionIntermediario",
	"CP"."NombreTipoRelacion" AS "TipoRelacion"
	from "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionPersona" as "CP"
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "PERAUT" on ("PERAUT"."IdPersona" = "CP"."IdPersonaDestino")
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS "CLIAUT" ON ( "PERAUT"."IdPersona" = "CLIAUT"."IdPersona" )
	LEFT join "BSCL.SocioNegocio.BD::PER.Telefono" as "TEL" on ("TEL"."IdTelefono" = "PERAUT"."IdTelefonoPrincipal") 
	LEFT join "BSCL.SocioNegocio.BD::PER.DireccionElectronica" as "DIRE" on ("DIRE"."IdDireccionElectronica" = "PERAUT"."IdDireccionElectronicaPrincipal" AND "DIRE"."Borrado"='N') 
	LEFT join "BSCL.SocioNegocio.BD::PER.DireccionFisica" as "DIRF" on ("DIRF"."IdDireccionFisica" = "PERAUT"."IdDireccionFisicaPrincipal" AND "DIRF"."Borrado"='N') 
	LEFT join "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" as "DIRFIS" on ("DIRFIS"."IdDireccionFisica" = "PERAUT"."IdDireccionFisicaPrincipal") 
	LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" as "PAIS" on ("PAIS"."IdPais" = "PERAUT"."IdPaisNacionalidadPrincipal") 
	--para cruce idcli
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "PER" on ("PER"."IdPersona" = "CP"."IdPersonaOrigen")
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS "CLI" ON ( "PER"."IdPersona" = "CLI"."IdPersona" )
	where "CP"."IdTipoRelacion" = case when  (v_Relacion <> 'SIN' AND v_Relacion <> '' AND v_TRel = 0) then v_Relacion else "CP"."IdTipoRelacion" end
	and   (ltrim(rtrim(substring("PERAUT"."DocumentoIdentidad",1,LENGTH("PERAUT"."DocumentoIdentidad")-2)))) in (select "PSAUT"."Valor" from :v_ParametrosSolicitudRutAut as "PSAUT")
	and "CP"."FechaInicioRelacion" >= case when ("CP"."IdTipoRelacion" <> 'PARSOC') then v_ParametrosSolicitudFini else "CP"."FechaInicioRelacion" end
	AND "CLI"."TipoEnrolamiento" = 'FULL'
	--and "FechaFinRelacion" <=   IFNULL (v_ParametrosSolicitudFter,"FechaFinRelacion")
	;	
	
--select * from "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionPersona" where "IdTipoRelacion" = 'APO'
	END IF;	
	
	IF (v_CondAut = 0) THEN
	
	v_AuxAutorizados = 
	select 
	ltrim(rtrim(substring("PERAUT"."DocumentoIdentidad",1,LENGTH("PERAUT"."DocumentoIdentidad")-2))) as "NumeroFolioCliente",
	'' as "RutCLI",	
	'' as "DigCLI",
	"CP"."IdPersonaDestino" as "NumeroPersona",
	case when "PERAUT"."TipoDocumentoIdentidad" = 'R' then
	(ltrim(rtrim(substring("PERAUT"."DocumentoIdentidad",1,LENGTH("PERAUT"."DocumentoIdentidad")-2)))) 
	else ''
	end
	as "Rut",
	case when "PERAUT"."TipoDocumentoIdentidad" = 'R' then
	(ltrim(rtrim(substring("PERAUT"."DocumentoIdentidad",LENGTH("PERAUT"."DocumentoIdentidad"),LENGTH("CP"."DocumentoIdentidadDefecto")))))
	else ''
	end
	as "Dig",
	case when "PERAUT"."TipoDocumentoIdentidad" = 'P' then
	"PERAUT"."DocumentoIdentidad"
	else ''
	end
	as "Pasaporte", 
	"PERAUT"."GlobalLegalEntity"  as "Glei", 
	"PERAUT"."ApellidoPaterno" as "ApellidoPaterno",
	"PERAUT"."ApellidoMaterno" as "ApellidoMaterno",
	"PERAUT"."Nombres" as "PrimerNombre",
	"PERAUT"."Nombres" as "SegundoNombre",
	case when "PERAUT"."Tipo" = 'J' then
	"PERAUT"."RazonSocial"
	else ''
	end
	 as "RazonSocial",
	"TEL"."Numero" as "Telefono",
	"DIRE"."Direccion" as "Email",
	'' as "DireccionTipo",
	"DIRFIS"."Calle" as "DireccionNombre",
	"DIRFIS"."Numero" as "DireccionNumero",
	"DIRFIS"."Departamento" as "DireccionDepto",
	"DIRFIS"."NombreDivisionPais" as "DireccionComuna",
	"DIRFIS"."NombreCiudad" as "DireccionCiudad",
	'EstadoProvincia' as "DireccionEstado",
	"PAIS"."NombrePais" as "DireccionPais",
	"DIRF"."CodigoPostal" as "DireccionCodigoPostal",
	"PAIS"."NombrePais" as "Nacionalidad",
	"PERAUT"."CodigoTipoCargoEmpleado" as "RelacionIntermediario",
	"CP"."NombreTipoRelacion" AS "TipoRelacion"
	from "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionPersona" as "CP"
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "PERAUT" on ("PERAUT"."IdPersona" = "CP"."IdPersonaDestino")
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS "CLIAUT" ON ( "PERAUT"."IdPersona" = "CLIAUT"."IdPersona" )
	LEFT join "BSCL.SocioNegocio.BD::PER.Telefono" as "TEL" on ("TEL"."IdTelefono" = "PERAUT"."IdTelefonoPrincipal") 
	LEFT join "BSCL.SocioNegocio.BD::PER.DireccionElectronica" as "DIRE" on ("DIRE"."IdDireccionElectronica" = "PERAUT"."IdDireccionElectronicaPrincipal" AND "DIRE"."Borrado"='N') 
	LEFT join "BSCL.SocioNegocio.BD::PER.DireccionFisica" as "DIRF" on ("DIRF"."IdDireccionFisica" = "PERAUT"."IdDireccionFisicaPrincipal" AND "DIRF"."Borrado"='N') 
	LEFT join "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" as "DIRFIS" on ("DIRFIS"."IdDireccionFisica" = "PERAUT"."IdDireccionFisicaPrincipal") 
	LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" as "PAIS" on ("PAIS"."IdPais" = "PERAUT"."IdPaisNacionalidadPrincipal") 
	--para cruce idcli
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "PER" on ("PER"."IdPersona" = "CP"."IdPersonaOrigen")
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS "CLI" ON ( "PER"."IdPersona" = "CLI"."IdPersona" )
	where "CP"."IdTipoRelacion" = case when  (v_Relacion <> 'SIN' AND v_Relacion <> '' AND v_TRel = 0) then v_Relacion else "CP"."IdTipoRelacion" end
	and "CP"."FechaInicioRelacion" >= case when ("CP"."IdTipoRelacion" <> 'PARSOC') then v_ParametrosSolicitudFini else "CP"."FechaInicioRelacion" end
	AND "CLI"."TipoEnrolamiento" = 'FULL'
	--and "FechaFinRelacion" <=   IFNULL (v_ParametrosSolicitudFter,"FechaFinRelacion")
	;	

	END IF;	


END IF;	

--3ra condicion, boton Todos rut desactivado y Todos aut activado


IF (v_TRut = 0) AND (v_TAut = 1) THEN 

	IF (v_CondRut > 0) THEN
	
	v_AuxAutorizados = 
	select 
	ltrim(rtrim(substring("PERAUT"."DocumentoIdentidad",1,LENGTH("PERAUT"."DocumentoIdentidad")-2))) as "NumeroFolioCliente",
	'' as "RutCLI",	
	'' as "DigCLI",
	"CP"."IdPersonaDestino" as "NumeroPersona",
	case when "PERAUT"."TipoDocumentoIdentidad" = 'R' then
	(ltrim(rtrim(substring("PERAUT"."DocumentoIdentidad",1,LENGTH("PERAUT"."DocumentoIdentidad")-2)))) 
	else ''
	end
	as "Rut",
	case when "PERAUT"."TipoDocumentoIdentidad" = 'R' then
	(ltrim(rtrim(substring("PERAUT"."DocumentoIdentidad",LENGTH("PERAUT"."DocumentoIdentidad"),LENGTH("CP"."DocumentoIdentidadDefecto")))))
	else ''
	end
	as "Dig",
	case when "PERAUT"."TipoDocumentoIdentidad" = 'P' then
	"PERAUT"."DocumentoIdentidad"
	else ''
	end
	as "Pasaporte", 
	"PERAUT"."GlobalLegalEntity"  as "Glei", 
	"PERAUT"."ApellidoPaterno" as "ApellidoPaterno",
	"PERAUT"."ApellidoMaterno" as "ApellidoMaterno",
	"PERAUT"."Nombres" as "PrimerNombre",
	"PERAUT"."Nombres" as "SegundoNombre",
	case when "PERAUT"."Tipo" = 'J' then
	"PERAUT"."RazonSocial"
	else ''
	end
	 as "RazonSocial",
	"TEL"."Numero" as "Telefono",
	"DIRE"."Direccion" as "Email",
	'' as "DireccionTipo",
	"DIRFIS"."Calle" as "DireccionNombre",
	"DIRFIS"."Numero" as "DireccionNumero",
	"DIRFIS"."Departamento" as "DireccionDepto",
	"DIRFIS"."NombreDivisionPais" as "DireccionComuna",
	"DIRFIS"."NombreCiudad" as "DireccionCiudad",
	'EstadoProvincia' as "DireccionEstado",
	"PAIS"."NombrePais" as "DireccionPais",
	"DIRF"."CodigoPostal" as "DireccionCodigoPostal",
	"PAIS"."NombrePais" as "Nacionalidad",
	"PERAUT"."CodigoTipoCargoEmpleado" as "RelacionIntermediario",
	"CP"."NombreTipoRelacion" AS "TipoRelacion"
	from "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionPersona" as "CP"
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "PERAUT" on ("PERAUT"."IdPersona" = "CP"."IdPersonaDestino")
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS "CLIAUT" ON ( "PERAUT"."IdPersona" = "CLIAUT"."IdPersona" )
	LEFT join "BSCL.SocioNegocio.BD::PER.Telefono" as "TEL" on ("TEL"."IdTelefono" = "PERAUT"."IdTelefonoPrincipal") 
	LEFT join "BSCL.SocioNegocio.BD::PER.DireccionElectronica" as "DIRE" on ("DIRE"."IdDireccionElectronica" = "PERAUT"."IdDireccionElectronicaPrincipal" AND "DIRE"."Borrado"='N') 
	LEFT join "BSCL.SocioNegocio.BD::PER.DireccionFisica" as "DIRF" on ("DIRF"."IdDireccionFisica" = "PERAUT"."IdDireccionFisicaPrincipal" AND "DIRF"."Borrado"='N') 
	LEFT join "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" as "DIRFIS" on ("DIRFIS"."IdDireccionFisica" = "PERAUT"."IdDireccionFisicaPrincipal") 
	LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" as "PAIS" on ("PAIS"."IdPais" = "PERAUT"."IdPaisNacionalidadPrincipal") 
	--para cruce idcli
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "PER" on ("PER"."IdPersona" = "CP"."IdPersonaOrigen")
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS "CLI" ON ( "PER"."IdPersona" = "CLI"."IdPersona" )
	where "CP"."IdTipoRelacion" = case when  (v_Relacion <> 'SIN' AND v_Relacion <> '' AND v_TRel = 0) then v_Relacion else "CP"."IdTipoRelacion" end
	and (ltrim(rtrim(substring("PER"."DocumentoIdentidad",1,LENGTH("PER"."DocumentoIdentidad")-2))))  in (select "PSR"."Valor" from :v_ParametrosSolicitudRut as "PSR")
	and "CP"."FechaInicioRelacion" >= case when ("CP"."IdTipoRelacion" <> 'PARSOC') then v_ParametrosSolicitudFini else "CP"."FechaInicioRelacion" end
	AND "CLI"."TipoEnrolamiento" = 'FULL'
	--and "FechaFinRelacion" <=   IFNULL (v_ParametrosSolicitudFter,"FechaFinRelacion")
	;	
	

	END IF;	
	
	IF (v_CondRut = 0) THEN
	
	v_AuxAutorizados = 
	select 
	ltrim(rtrim(substring("PERAUT"."DocumentoIdentidad",1,LENGTH("PERAUT"."DocumentoIdentidad")-2))) as "NumeroFolioCliente",
	'' as "RutCLI",	
	'' as "DigCLI",
	"CP"."IdPersonaDestino" as "NumeroPersona",
	case when "PERAUT"."TipoDocumentoIdentidad" = 'R' then
	(ltrim(rtrim(substring("PERAUT"."DocumentoIdentidad",1,LENGTH("PERAUT"."DocumentoIdentidad")-2)))) 
	else ''
	end
	as "Rut",
	case when "PERAUT"."TipoDocumentoIdentidad" = 'R' then
	(ltrim(rtrim(substring("PERAUT"."DocumentoIdentidad",LENGTH("PERAUT"."DocumentoIdentidad"),LENGTH("CP"."DocumentoIdentidadDefecto")))))
	else ''
	end
	as "Dig",
	case when "PERAUT"."TipoDocumentoIdentidad" = 'P' then
	"PERAUT"."DocumentoIdentidad"
	else ''
	end
	as "Pasaporte", 
	"PERAUT"."GlobalLegalEntity"  as "Glei", 
	"PERAUT"."ApellidoPaterno" as "ApellidoPaterno",
	"PERAUT"."ApellidoMaterno" as "ApellidoMaterno",
	"PERAUT"."Nombres" as "PrimerNombre",
	"PERAUT"."Nombres" as "SegundoNombre",
	case when "PERAUT"."Tipo" = 'J' then
	"PERAUT"."RazonSocial"
	else ''
	end
	 as "RazonSocial",
	"TEL"."Numero" as "Telefono",
	"DIRE"."Direccion" as "Email",
	'' as "DireccionTipo",
	"DIRFIS"."Calle" as "DireccionNombre",
	"DIRFIS"."Numero" as "DireccionNumero",
	"DIRFIS"."Departamento" as "DireccionDepto",
	"DIRFIS"."NombreDivisionPais" as "DireccionComuna",
	"DIRFIS"."NombreCiudad" as "DireccionCiudad",
	'EstadoProvincia' as "DireccionEstado",
	"PAIS"."NombrePais" as "DireccionPais",
	"DIRF"."CodigoPostal" as "DireccionCodigoPostal",
	"PAIS"."NombrePais" as "Nacionalidad",
	"PERAUT"."CodigoTipoCargoEmpleado" as "RelacionIntermediario",
	"CP"."NombreTipoRelacion" AS "TipoRelacion"
	from "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionPersona" as "CP"
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "PERAUT" on ("PERAUT"."IdPersona" = "CP"."IdPersonaDestino")
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS "CLIAUT" ON ( "PERAUT"."IdPersona" = "CLIAUT"."IdPersona" )
	LEFT join "BSCL.SocioNegocio.BD::PER.Telefono" as "TEL" on ("TEL"."IdTelefono" = "PERAUT"."IdTelefonoPrincipal") 
	LEFT join "BSCL.SocioNegocio.BD::PER.DireccionElectronica" as "DIRE" on ("DIRE"."IdDireccionElectronica" = "PERAUT"."IdDireccionElectronicaPrincipal" AND "DIRE"."Borrado"='N') 
	LEFT join "BSCL.SocioNegocio.BD::PER.DireccionFisica" as "DIRF" on ("DIRF"."IdDireccionFisica" = "PERAUT"."IdDireccionFisicaPrincipal" AND "DIRF"."Borrado"='N') 
	LEFT join "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" as "DIRFIS" on ("DIRFIS"."IdDireccionFisica" = "PERAUT"."IdDireccionFisicaPrincipal") 
	LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" as "PAIS" on ("PAIS"."IdPais" = "PERAUT"."IdPaisNacionalidadPrincipal") 
	--para cruce idcli
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "PER" on ("PER"."IdPersona" = "CP"."IdPersonaOrigen")
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS "CLI" ON ( "PER"."IdPersona" = "CLI"."IdPersona" )
	where "CP"."IdTipoRelacion" = case when  (v_Relacion <> 'SIN' AND v_Relacion <> '' AND v_TRel = 0) then v_Relacion else "CP"."IdTipoRelacion" end
	and "CP"."FechaInicioRelacion" >= case when ("CP"."IdTipoRelacion" <> 'PARSOC') then v_ParametrosSolicitudFini else "CP"."FechaInicioRelacion" end
	AND "CLI"."TipoEnrolamiento" = 'FULL'
	--and "FechaFinRelacion" <=   IFNULL (v_ParametrosSolicitudFter,"FechaFinRelacion")
	;
	

	END IF;	



END IF;	

--4ta condicion, boton Todos rut desactivado y Todos aut desactivado


IF (v_TRut = 0) AND (v_TAut = 0) THEN 


	IF (v_CondRut > 0) AND (v_CondAut > 0)  THEN
	v_AuxAutorizados = 
	SELECT
	ltrim(rtrim(substring("PERAUT"."DocumentoIdentidad",1,LENGTH("PERAUT"."DocumentoIdentidad")-2))) as "NumeroFolioCliente",
	'' as "RutCLI",	
	'' as "DigCLI",
	"CP"."IdPersonaDestino" as "NumeroPersona",
	case when "PERAUT"."TipoDocumentoIdentidad" = 'R' then
	(ltrim(rtrim(substring("PERAUT"."DocumentoIdentidad",1,LENGTH("PERAUT"."DocumentoIdentidad")-2)))) 
	else ''
	end
	as "Rut",
	case when "PERAUT"."TipoDocumentoIdentidad" = 'R' then
	(ltrim(rtrim(substring("PERAUT"."DocumentoIdentidad",LENGTH("PERAUT"."DocumentoIdentidad"),LENGTH("CP"."DocumentoIdentidadDefecto")))))
	else ''
	end
	as "Dig",
	case when "PERAUT"."TipoDocumentoIdentidad" = 'P' then
	"PERAUT"."DocumentoIdentidad"
	else ''
	end
	as "Pasaporte", 
	"PERAUT"."GlobalLegalEntity"  as "Glei", 
	"PERAUT"."ApellidoPaterno" as "ApellidoPaterno",
	"PERAUT"."ApellidoMaterno" as "ApellidoMaterno",
	"PERAUT"."Nombres" as "PrimerNombre",
	"PERAUT"."Nombres" as "SegundoNombre",
	case when "PERAUT"."Tipo" = 'J' then
	"PERAUT"."RazonSocial"
	else ''
	end
	 as "RazonSocial",
	"TEL"."Numero" as "Telefono",
	"DIRE"."Direccion" as "Email",
	'' as "DireccionTipo",
	"DIRFIS"."Calle" as "DireccionNombre",
	"DIRFIS"."Numero" as "DireccionNumero",
	"DIRFIS"."Departamento" as "DireccionDepto",
	"DIRFIS"."NombreDivisionPais" as "DireccionComuna",
	"DIRFIS"."NombreCiudad" as "DireccionCiudad",
	'EstadoProvincia' as "DireccionEstado",
	"PAIS"."NombrePais" as "DireccionPais",
	"DIRF"."CodigoPostal" as "DireccionCodigoPostal",
	"PAIS"."NombrePais" as "Nacionalidad",
	"PERAUT"."CodigoTipoCargoEmpleado" as "RelacionIntermediario",
	"CP"."NombreTipoRelacion" AS "TipoRelacion"
	from "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionPersona" as "CP"
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "PERAUT" on ("PERAUT"."IdPersona" = "CP"."IdPersonaDestino")
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS "CLIAUT" ON ( "PERAUT"."IdPersona" = "CLIAUT"."IdPersona" )
	LEFT join "BSCL.SocioNegocio.BD::PER.Telefono" as "TEL" on ("TEL"."IdTelefono" = "PERAUT"."IdTelefonoPrincipal") 
	LEFT join "BSCL.SocioNegocio.BD::PER.DireccionElectronica" as "DIRE" on ("DIRE"."IdDireccionElectronica" = "PERAUT"."IdDireccionElectronicaPrincipal" AND "DIRE"."Borrado"='N') 
	LEFT join "BSCL.SocioNegocio.BD::PER.DireccionFisica" as "DIRF" on ("DIRF"."IdDireccionFisica" = "PERAUT"."IdDireccionFisicaPrincipal" AND "DIRF"."Borrado"='N') 
	LEFT join "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" as "DIRFIS" on ("DIRFIS"."IdDireccionFisica" = "PERAUT"."IdDireccionFisicaPrincipal") 
	LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" as "PAIS" on ("PAIS"."IdPais" = "PERAUT"."IdPaisNacionalidadPrincipal") 
	--para cruce idcli
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "PER" on ("PER"."IdPersona" = "CP"."IdPersonaOrigen")
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS "CLI" ON ( "PER"."IdPersona" = "CLI"."IdPersona" )
	where "CP"."IdTipoRelacion" = case when  (v_Relacion <> 'SIN' AND v_Relacion <> '' AND v_TRel = 0) then v_Relacion else "CP"."IdTipoRelacion" end
    --and   "PERAUT"."DocumentoIdentidad" in (select "PSAUT"."Valor" from :v_ParametrosSolicitudRutAut as "PSAUT")
	and  (ltrim(rtrim(substring("PERAUT"."DocumentoIdentidad",1,LENGTH("PERAUT"."DocumentoIdentidad")-2)))) in (select "PSAUT"."Valor" from :v_ParametrosSolicitudRutAut as "PSAUT")
	and  (ltrim(rtrim(substring("PER"."DocumentoIdentidad",1,LENGTH("PER"."DocumentoIdentidad")-2))))  in (select "PSR"."Valor" from :v_ParametrosSolicitudRut as "PSR")
	and "CP"."FechaInicioRelacion" >= case when ("CP"."IdTipoRelacion" <> 'PARSOC') then v_ParametrosSolicitudFini else "CP"."FechaInicioRelacion" end
	AND "CLI"."TipoEnrolamiento" = 'FULL'
	--and "FechaFinRelacion" <=   IFNULL (v_ParametrosSolicitudFter,"FechaFinRelacion")
	;

	
	END IF;	
	
	IF (v_CondRut > 0) AND (v_CondAut = 0)  THEN
	v_AuxAutorizados = 
	select 
	ltrim(rtrim(substring("PERAUT"."DocumentoIdentidad",1,LENGTH("PERAUT"."DocumentoIdentidad")-2))) as "NumeroFolioCliente",
	'' as "RutCLI",	
	'' as "DigCLI",
	"CP"."IdPersonaDestino" as "NumeroPersona",
	case when "PERAUT"."TipoDocumentoIdentidad" = 'R' then
	(ltrim(rtrim(substring("PERAUT"."DocumentoIdentidad",1,LENGTH("PERAUT"."DocumentoIdentidad")-2)))) 
	else ''
	end
	as "Rut",
	case when "PERAUT"."TipoDocumentoIdentidad" = 'R' then
	(ltrim(rtrim(substring("PERAUT"."DocumentoIdentidad",LENGTH("PERAUT"."DocumentoIdentidad"),LENGTH("CP"."DocumentoIdentidadDefecto")))))
	else ''
	end
	as "Dig",
	case when "PERAUT"."TipoDocumentoIdentidad" = 'P' then
	"PERAUT"."DocumentoIdentidad"
	else ''
	end
	as "Pasaporte", 
	"PERAUT"."GlobalLegalEntity"  as "Glei", 
	"PERAUT"."ApellidoPaterno" as "ApellidoPaterno",
	"PERAUT"."ApellidoMaterno" as "ApellidoMaterno",
	"PERAUT"."Nombres" as "PrimerNombre",
	"PERAUT"."Nombres" as "SegundoNombre",
	case when "PERAUT"."Tipo" = 'J' then
	"PERAUT"."RazonSocial"
	else ''
	end
	 as "RazonSocial",
	"TEL"."Numero" as "Telefono",
	"DIRE"."Direccion" as "Email",
	'' as "DireccionTipo",
	"DIRFIS"."Calle" as "DireccionNombre",
	"DIRFIS"."Numero" as "DireccionNumero",
	"DIRFIS"."Departamento" as "DireccionDepto",
	"DIRFIS"."NombreDivisionPais" as "DireccionComuna",
	"DIRFIS"."NombreCiudad" as "DireccionCiudad",
	'EstadoProvincia' as "DireccionEstado",
	"PAIS"."NombrePais" as "DireccionPais",
	"DIRF"."CodigoPostal" as "DireccionCodigoPostal",
	"PAIS"."NombrePais" as "Nacionalidad",
	"PERAUT"."CodigoTipoCargoEmpleado" as "RelacionIntermediario",
	"CP"."NombreTipoRelacion" AS "TipoRelacion"
	from "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionPersona" as "CP"
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "PERAUT" on ("PERAUT"."IdPersona" = "CP"."IdPersonaDestino")
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS "CLIAUT" ON ( "PERAUT"."IdPersona" = "CLIAUT"."IdPersona" )
	LEFT join "BSCL.SocioNegocio.BD::PER.Telefono" as "TEL" on ("TEL"."IdTelefono" = "PERAUT"."IdTelefonoPrincipal") 
	LEFT join "BSCL.SocioNegocio.BD::PER.DireccionElectronica" as "DIRE" on ("DIRE"."IdDireccionElectronica" = "PERAUT"."IdDireccionElectronicaPrincipal" AND "DIRE"."Borrado"='N') 
	LEFT join "BSCL.SocioNegocio.BD::PER.DireccionFisica" as "DIRF" on ("DIRF"."IdDireccionFisica" = "PERAUT"."IdDireccionFisicaPrincipal" AND "DIRF"."Borrado"='N') 
	LEFT join "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" as "DIRFIS" on ("DIRFIS"."IdDireccionFisica" = "PERAUT"."IdDireccionFisicaPrincipal") 
	LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" as "PAIS" on ("PAIS"."IdPais" = "PERAUT"."IdPaisNacionalidadPrincipal") 
	--para cruce idcli
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "PER" on ("PER"."IdPersona" = "CP"."IdPersonaOrigen")
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS "CLI" ON ( "PER"."IdPersona" = "CLI"."IdPersona" )
	where "CP"."IdTipoRelacion" = case when  (v_Relacion <> 'SIN' AND v_Relacion <> '' AND v_TRel = 0) then v_Relacion else "CP"."IdTipoRelacion" end
	and  (ltrim(rtrim(substring("PER"."DocumentoIdentidad",1,LENGTH("PER"."DocumentoIdentidad")-2))))  in (select "PSR"."Valor" from :v_ParametrosSolicitudRut as "PSR")
	and "CP"."FechaInicioRelacion" >= case when ("CP"."IdTipoRelacion" <> 'PARSOC') then v_ParametrosSolicitudFini else "CP"."FechaInicioRelacion" end
	AND "CLI"."TipoEnrolamiento" = 'FULL'
	--and "FechaFinRelacion" <=   IFNULL (v_ParametrosSolicitudFter,"FechaFinRelacion")
	;
	
	
	END IF;
	
	IF (v_CondRut = 0) AND (v_CondAut > 0)  THEN
	v_AuxAutorizados = 
	select 
	ltrim(rtrim(substring("PERAUT"."DocumentoIdentidad",1,LENGTH("PERAUT"."DocumentoIdentidad")-2))) as "NumeroFolioCliente",
	'' as "RutCLI",	
	'' as "DigCLI",
	"CP"."IdPersonaDestino" as "NumeroPersona",
	case when "PERAUT"."TipoDocumentoIdentidad" = 'R' then
	(ltrim(rtrim(substring("PERAUT"."DocumentoIdentidad",1,LENGTH("PERAUT"."DocumentoIdentidad")-2)))) 
	else ''
	end
	as "Rut",
	case when "PERAUT"."TipoDocumentoIdentidad" = 'R' then
	(ltrim(rtrim(substring("PERAUT"."DocumentoIdentidad",LENGTH("PERAUT"."DocumentoIdentidad"),LENGTH("CP"."DocumentoIdentidadDefecto")))))
	else ''
	end
	as "Dig",
	case when "PERAUT"."TipoDocumentoIdentidad" = 'P' then
	"PERAUT"."DocumentoIdentidad"
	else ''
	end
	as "Pasaporte", 
	"PERAUT"."GlobalLegalEntity"  as "Glei", 
	"PERAUT"."ApellidoPaterno" as "ApellidoPaterno",
	"PERAUT"."ApellidoMaterno" as "ApellidoMaterno",
	"PERAUT"."Nombres" as "PrimerNombre",
	"PERAUT"."Nombres" as "SegundoNombre",
	case when "PERAUT"."Tipo" = 'J' then
	"PERAUT"."RazonSocial"
	else ''
	end
	 as "RazonSocial",
	"TEL"."Numero" as "Telefono",
	"DIRE"."Direccion" as "Email",
	'' as "DireccionTipo",
	"DIRFIS"."Calle" as "DireccionNombre",
	"DIRFIS"."Numero" as "DireccionNumero",
	"DIRFIS"."Departamento" as "DireccionDepto",
	"DIRFIS"."NombreDivisionPais" as "DireccionComuna",
	"DIRFIS"."NombreCiudad" as "DireccionCiudad",
	'EstadoProvincia' as "DireccionEstado",
	"PAIS"."NombrePais" as "DireccionPais",
	"DIRF"."CodigoPostal" as "DireccionCodigoPostal",
	"PAIS"."NombrePais" as "Nacionalidad",
	"PERAUT"."CodigoTipoCargoEmpleado" as "RelacionIntermediario",
	"CP"."NombreTipoRelacion" AS "TipoRelacion"
	from "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionPersona" as "CP"
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "PERAUT" on ("PERAUT"."IdPersona" = "CP"."IdPersonaDestino")
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS "CLIAUT" ON ( "PERAUT"."IdPersona" = "CLIAUT"."IdPersona" )
	LEFT join "BSCL.SocioNegocio.BD::PER.Telefono" as "TEL" on ("TEL"."IdTelefono" = "PERAUT"."IdTelefonoPrincipal") 
	LEFT join "BSCL.SocioNegocio.BD::PER.DireccionElectronica" as "DIRE" on ("DIRE"."IdDireccionElectronica" = "PERAUT"."IdDireccionElectronicaPrincipal" AND "DIRE"."Borrado"='N') 
	LEFT join "BSCL.SocioNegocio.BD::PER.DireccionFisica" as "DIRF" on ("DIRF"."IdDireccionFisica" = "PERAUT"."IdDireccionFisicaPrincipal" AND "DIRF"."Borrado"='N') 
	LEFT join "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" as "DIRFIS" on ("DIRFIS"."IdDireccionFisica" = "PERAUT"."IdDireccionFisicaPrincipal") 
	LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" as "PAIS" on ("PAIS"."IdPais" = "PERAUT"."IdPaisNacionalidadPrincipal") 
	--para cruce idcli
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "PER" on ("PER"."IdPersona" = "CP"."IdPersonaOrigen")
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS "CLI" ON ( "PER"."IdPersona" = "CLI"."IdPersona" )
	where "CP"."IdTipoRelacion" = case when  (v_Relacion <> 'SIN' AND v_Relacion <> '' AND v_TRel = 0) then v_Relacion else "CP"."IdTipoRelacion" end
	and   (ltrim(rtrim(substring("PERAUT"."DocumentoIdentidad",1,LENGTH("PERAUT"."DocumentoIdentidad")-2)))) in (select "PSAUT"."Valor" from :v_ParametrosSolicitudRutAut as "PSAUT")
	and "CP"."FechaInicioRelacion" >= case when ("CP"."IdTipoRelacion" <> 'PARSOC') then v_ParametrosSolicitudFini else "CP"."FechaInicioRelacion" end
	AND "CLI"."TipoEnrolamiento" = 'FULL'
	--and "FechaFinRelacion" <=   IFNULL (v_ParametrosSolicitudFter,"FechaFinRelacion")
	;
	
	END IF;
	
	
	IF (v_CondRut = 0) AND (v_CondAut = 0)  THEN
	v_AuxAutorizados = 
	select 
	ltrim(rtrim(substring("PERAUT"."DocumentoIdentidad",1,LENGTH("PERAUT"."DocumentoIdentidad")-2))) as "NumeroFolioCliente",
	'' as "RutCLI",	
	'' as "DigCLI",
	"CP"."IdPersonaDestino" as "NumeroPersona",
	case when "PERAUT"."TipoDocumentoIdentidad" = 'R' then
	(ltrim(rtrim(substring("PERAUT"."DocumentoIdentidad",1,LENGTH("PERAUT"."DocumentoIdentidad")-2)))) 
	else ''
	end
	as "Rut",
	case when "PERAUT"."TipoDocumentoIdentidad" = 'R' then
	(ltrim(rtrim(substring("PERAUT"."DocumentoIdentidad",LENGTH("PERAUT"."DocumentoIdentidad"),LENGTH("CP"."DocumentoIdentidadDefecto")))))
	else ''
	end
	as "Dig",
	case when "PERAUT"."TipoDocumentoIdentidad" = 'P' then
	"PERAUT"."DocumentoIdentidad"
	else ''
	end
	as "Pasaporte", 
	"PERAUT"."GlobalLegalEntity"  as "Glei", 
	"PERAUT"."ApellidoPaterno" as "ApellidoPaterno",
	"PERAUT"."ApellidoMaterno" as "ApellidoMaterno",
	"PERAUT"."Nombres" as "PrimerNombre",
	"PERAUT"."Nombres" as "SegundoNombre",
	case when "PERAUT"."Tipo" = 'J' then
	"PERAUT"."RazonSocial"
	else ''
	end
	 as "RazonSocial",
	"TEL"."Numero" as "Telefono",
	"DIRE"."Direccion" as "Email",
	'' as "DireccionTipo",
	"DIRFIS"."Calle" as "DireccionNombre",
	"DIRFIS"."Numero" as "DireccionNumero",
	"DIRFIS"."Departamento" as "DireccionDepto",
	"DIRFIS"."NombreDivisionPais" as "DireccionComuna",
	"DIRFIS"."NombreCiudad" as "DireccionCiudad",
	' ' as "DireccionEstado",
	"PAIS"."NombrePais" as "DireccionPais",
	"DIRF"."CodigoPostal" as "DireccionCodigoPostal",
	"PAIS"."NombrePais" as "Nacionalidad",
	"PERAUT"."CodigoTipoCargoEmpleado" as "RelacionIntermediario",
	"CP"."NombreTipoRelacion" AS "TipoRelacion"
	from "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionPersona" as "CP"
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "PERAUT" on ("PERAUT"."IdPersona" = "CP"."IdPersonaDestino")
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS "CLIAUT" ON ( "PERAUT"."IdPersona" = "CLIAUT"."IdPersona" )
	LEFT join "BSCL.SocioNegocio.BD::PER.Telefono" as "TEL" on ("TEL"."IdTelefono" = "PERAUT"."IdTelefonoPrincipal") 
	LEFT join "BSCL.SocioNegocio.BD::PER.DireccionElectronica" as "DIRE" on ("DIRE"."IdDireccionElectronica" = "PERAUT"."IdDireccionElectronicaPrincipal" AND "DIRE"."Borrado"='N') 
	LEFT join "BSCL.SocioNegocio.BD::PER.DireccionFisica" as "DIRF" on ("DIRF"."IdDireccionFisica" = "PERAUT"."IdDireccionFisicaPrincipal" AND "DIRF"."Borrado"='N') 
	LEFT join "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" as "DIRFIS" on ("DIRFIS"."IdDireccionFisica" = "PERAUT"."IdDireccionFisicaPrincipal") 
	LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" as "PAIS" on ("PAIS"."IdPais" = "PERAUT"."IdPaisNacionalidadPrincipal") 
	--para cruce idcli
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "PER" on ("PER"."IdPersona" = "CP"."IdPersonaOrigen")
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS "CLI" ON ( "PER"."IdPersona" = "CLI"."IdPersona" )
	where "CP"."IdTipoRelacion" = case when  (v_Relacion <> 'SIN' AND v_Relacion <> '' AND v_TRel = 0) then v_Relacion else "CP"."IdTipoRelacion" end
	and "CP"."FechaInicioRelacion" >= case when ("CP"."IdTipoRelacion" <> 'PARSOC') then v_ParametrosSolicitudFini else "CP"."FechaInicioRelacion" end
	AND "CLI"."TipoEnrolamiento" = 'FULL'
	--and "FechaFinRelacion" <=   IFNULL (v_ParametrosSolicitudFter,"FechaFinRelacion")
	;
	
	END IF;	
	




END IF;	


select count(1) into v_CondFinal from :v_AuxAutorizados;

IF (v_CondFinal > 0) THEN 


p_out_TablaSalida=select * from :v_AuxAutorizados;

ELSE 

p_out_TablaSalida=
select 
'' as "NumeroFolioCliente"
,'' as "RutCLI"	
,'' as "DigCLI"
,0  as "NumeroPersona"	
,'' as "Rut"	
,'' as "Dig"			
,'' as "Pasaporte"		
,'' as "Glei"			
,'' as "ApellidoPaterno"
,'' as "ApellidoMaterno"
,'' as "PrimerNombre"	
,'' as "SegundoNombre"	
,'' as "RazonSocial"	
,'' as "Telefono"		
,'' as "Email"			
,'' as "DireccionTipo"	
,'' as "DireccionNombre"
,'' as "DireccionNumero"
,'' as "DireccionDepto"	
,'' as "DireccionComuna"
,'' as "DireccionCiudad"
,'' as "DireccionEstado"
,'' as "DireccionPais"	
,'' as "DireccionCodigoPostal"
,'' as "Nacionalidad"			
,'' as "RelacionIntermediario"
,'' as "TipoRelacion"
from dummy where 1=2;

END IF;

SELECT * FROM :p_out_TablaSalida;


--select  v_TRut as "trut",v_TAut as "taut",v_CondTodosRUT as todosrut , v_CondTodosAUT as todosrutaut ,v_CondRut as "condrut",v_CondAut as "condaut", p_IdSolicitudCircular as "idsol",v_Relacion as "rela" from dummy;
/*
select * from :v_ParametrosSolicitudRut;
select * from :v_ParametrosSolicitudRutAut;
*/




END;
