set schema "BSCL";
DROP VIEW "BSCL.Normativa.BD.Vistas::NOR.ConsultarClienteOnline";


CREATE VIEW  "BSCL.Normativa.BD.Vistas::NOR.ConsultarClienteOnline" ( 
     "IDCLIENTE",
     "IDPERSONA",
	 "RUT",
	 "DIG",
	 "PASAPORTE",
	 "GLOBALLEGALENTITY",
	 "APELLIDOPATERNO",
	 "APELLIDOMATERNO",
	 "PRIMERNOMBRE",
	 "SEGUNDONOMBRE",
	 "RAZONSOCIAL",
	 "TELEFONO",
	 "EMAIL",
	 "TIPODIRE",
	 "NOMBRE",
	 "NUMERO",
	 "DEPTO",
	 "COMUNA",
	 "CIUDAD",
	 "ESTADOPROVINCIA",
	 "PAIS",
	 "CODIGOPOSTAL",
	 "NACIONALIDAD",
	 "RELACIONINTERMEDIARIO",
	 "ADMINISTRACIONCARTERA",
	 "RUTENTEMPL",
	 "DIGENTEMPL",
	 "RAZONSOCIALENTEMPL",
	 "ESTADOCIVIL",
	 "INDDERPREV",
	 "CONYRUT",
	 "CONYDIG",
	 "CONYPASAPORTE",
	 "CONYAPELLIDOPATERNO",
	 "CONYAPELLIDOMATERNO",
	 "CONYPRIMERNOMBRE",
	 "CONYSEGUNDONOMBRE",
	 "CONYRAZONSOCIAL",
	 "CONYTELEFONO",
	 "CONYEMAIL",
	 "CONYTIPODIRE",
	 "CONYNOMBRE",
	 "CONYNUMERO",
	 "CONYDEPTO",
	 "CONYCOMUNA",
	 "CONYCIUDAD",
	 "CONYESTADOPROVINCIA",
	 "CONYPAIS",
	 "CONYCODIGOPOSTAL",
	 "CONYNACIONALIDAD",
	 "CONYRELACIONINTERMEDIARIO",
	 "REPRUT",
	 "REPDIG",
	 "REPPASAPORTE",
	 "REPGLOBALLEGALENTITY",
	 "REPAPELLIDOPATERNO",
	 "REPAPELLIDOMATERNO",
	 "REPPRIMERNOMBRE",
	 "REPSEGUNDONOMBRE",
	 "REPRAZONSOCIAL",
	 "REPTELEFONO",
	 "REPEMAIL",
	 "REPTIPODIRE",
	 "REPNOMBRE",
	 "REPNUMERO",
	 "REPDEPTO",
	 "REPCOMUNA",
	 "REPCIUDAD",
	 "REPESTADOPROVINCIA",
	 "REPPAIS",
	 "REPCODIGOPOSTAL",
	 "REPNACIONALIDAD",
	 "REPRELACIONINTERMEDIARIO",
	 "REPESTADOCIVIL",
	 "CONYREPRUT",
	 "CONYREPDIG",
	 "CONYREPPASAPORTE",
	 "CONYREPAPELLIDOPATERNO",
	 "CONYREPAPELLIDOMATERNO",
	 "CONYREPPRIMERNOMBRE",
	 "CONYREPSEGUNDONOMBRE",
	 "CONYREPRAZONSOCIAL",
	 "CONYREPTELEFONO",
	 "CONYREPEMAIL",
	 "CONYREPTIPODIRE",
	 "CONYREPNOMBRE",
	 "CONYREPNUMERO",
	 "CONYREPDEPTO",
	 "CONYREPCOMUNA",
	 "CONYREPCIUDAD",
	 "CONYREPESTADOPROVINCIA",
	 "CONYREPPAIS",
	 "CONYREPCODIGOPOSTAL",
	 "CONYREPNACIONALIDAD",
	 "CONYREPRELACIONINTERMEDIARIO" ,
	 "CUENTABANCARIAUNO",
	 "CUENTABANCARIADOS",
	 "CUENTABANCARIATRES",
	 "CLASEORDEN",
	 "PERFIL",
	 "PERFILANTERIOR",
	 "FECHAPERFILANT",
	 "CONCATENADOS" ) 
	 AS select
	 CLIENTE.IdCliente,
	 CLIENTE.IdPersona,
	 CLIENTE.Rut,
	 CLIENTE.DIG,
	 CLIENTE.Pasaporte,
	 CLIENTE.GlobalLegalEntity,
	 CLIENTE.ApellidoPaterno,
	 CLIENTE.ApellidoMaterno,
	 CLIENTE.PrimerNombre,
	 CLIENTE.SegundoNombre,
	 CLIENTE.RazonSocial,
	 CLIENTE.Telefono,
	 CLIENTE.Email,
	 CLIENTE.TipoDire,
	 CLIENTE.Nombre,
	 CLIENTE.Numero,
	 CLIENTE.Depto,
	 CLIENTE.Comuna,
	 CLIENTE.Ciudad,
	 CLIENTE.EstadoProvincia,
	 CLIENTE.Pais,
	 CLIENTE.CodigoPostal,
	 CLIENTE.Nacionalidad,
	 CLIENTE.RelacionIntermediario,
	 CLIENTE.AdministracionCartera,
	 CLIENTE.RutEntEmpl,
	 CLIENTE.DigEntEmpl,
	 CLIENTE.RazonSocialEntEmpl,
	 CLIENTE.EstadoCivil,
	 -------------------------------Indicadores Cliente
	 CLIENTE.IndDerPrev,
	 -------------------------------Datos Conyugue
	 Conyugue.Rut as ConyRut,
	 Conyugue.DIG as ConyDig,
	 Conyugue.Pasaporte as ConyPasaporte,
	 Conyugue.ApellidoPaterno as ConyApellidoPaterno ,
	 Conyugue.ApellidoMaterno as ConyApellidoMaterno ,
	 Conyugue.PrimerNombre as ConyPrimerNombre ,
	 Conyugue.SegundoNombre as ConySegundoNombre ,
	 Conyugue.RazonSocial as ConyRazonSocial,
	 Conyugue.Telefono as ConyTelefono ,
	 Conyugue.Email as ConyEmail ,
	 Conyugue.TipoDire as ConyTipoDire ,
	 Conyugue.Nombre as ConyNombre ,
	 Conyugue.Numero as ConyNumero ,
	 Conyugue.Depto as ConyDepto ,
	 Conyugue.Comuna as ConyComuna ,
	 Conyugue.Ciudad as ConyCiudad ,
	 Conyugue.EstadoProvincia as ConyEstadoProvincia ,
	 Conyugue.Pais as ConyPais ,
	 Conyugue.CodigoPostal as ConyCodigoPostal ,
	 Conyugue.Nacionalidad as ConyNacionalidad ,
	 Conyugue.RelacionIntermediario as ConyRelacionIntermediario ,
	 -------------------------------Datos Representante Legal
	 RepLegal.Rut as RepRut ,
	 RepLegal.DIG as RepDIG ,
	 RepLegal.Pasaporte as RepPasaporte ,
	 RepLegal.GlobalLegalEntity as RepGlobalLegalEntity,
	 RepLegal.ApellidoPaterno as RepApellidoPaterno ,
	 RepLegal.ApellidoMaterno as RepApellidoMaterno ,
	 RepLegal.PrimerNombre as RepPrimerNombre ,
	 RepLegal.SegundoNombre as RepSegundoNombre ,
	 RepLegal.RazonSocial as RepRazonSocial ,
	 RepLegal.Telefono as RepTelefono ,
	 RepLegal.Email as RepEmail ,
	 RepLegal.TipoDire as RepTipoDire ,
	 RepLegal.Nombre as RepNombre ,
	 RepLegal.Numero as RepNumero,
	 RepLegal.Depto as RepDepto ,
	 RepLegal.Comuna as RepComuna ,
	 RepLegal.Ciudad as RepCiudad ,
	 RepLegal.EstadoProvincia as RepEstadoProvincia ,
	 RepLegal.Pais as RepPais ,
	 RepLegal.CodigoPostal as RepCodigoPostal ,
	 RepLegal.Nacionalidad as RepNacionalidad ,
	 RepLegal.RelacionIntermediario as RepRelacionIntermediario ,
	 RepLegal.EstadoCivil as RepEstadoCivil ,
	 -------------------------------Datos Conyugue Representante Legal
	 ConyRepLegal.Rut as ConyRepRut ,
	 ConyRepLegal.DIG as ConyRepDIG ,
	 ConyRepLegal.Pasaporte as ConyRepPasaporte ,
	 ConyRepLegal.ApellidoPaterno as ConyRepApellidoPaterno ,
	 ConyRepLegal.ApellidoMaterno as ConyRepApellidoMaterno ,
	 ConyRepLegal.PrimerNombre as ConyRepPrimerNombre ,
	 ConyRepLegal.SegundoNombre as ConyRepSegundoNombre ,
	 ConyRepLegal.RazonSocial as ConyRepRazonSocial ,
	 ConyRepLegal.Telefono as ConyRepTelefono ,
	 ConyRepLegal.Email as ConyRepEmail ,
	 ConyRepLegal.TipoDire as ConyRepTipoDire ,
	 ConyRepLegal.Nombre as ConyRepNombre ,
	 ConyRepLegal.Numero as ConyRepNumero ,
	 ConyRepLegal.Depto as ConyRepDepto ,
	 ConyRepLegal.Comuna as ConyRepComuna ,
	 ConyRepLegal.Ciudad as ConyRepCiudad ,
	 ConyRepLegal.EstadoProvincia as ConyRepEstadoProvincia ,
	 ConyRepLegal.Pais as ConyRepPais ,
	 ConyRepLegal.CodigoPostal as ConyRepCodigoPostal ,
	 ConyRepLegal.Nacionalidad as ConyRepNacionalidad ,
	 ConyRepLegal.RelacionIntermediario as ConyRepRelacionIntermediario ,
	 -------------------------------Segunda parte datos Cliente
	 CLIENTE.cta1 as CuentaBancariaUno,
	 case when CLIENTE.cta1 = CLIENTE.cta2 
	 then '' 	
	 else CLIENTE.cta2 
 	 end as CuentaBancariaDos ,
	 (select "CTA"."NumeroCuentaBancaria" from "BSCL.SocioNegocio.BD::PER.CuentaBancaria" as "CTA" 	
	 where "CTA"."IdCuentaBancaria" 
	 in 
	 (select MAX("CTA"."IdCuentaBancaria") 
	 from "BSCL.SocioNegocio.BD::PER.CuentaBancaria" as "CTA" 
	 WHERE "CTA"."IdPersona" = CLIENTE.IdPersona 
	 AND "CTA"."NumeroCuentaBancaria" <> CLIENTE.cta2 
	 AND "CTA"."NumeroCuentaBancaria" <> CLIENTE.cta1 )
	 ) as CuentaBancariaTres,
	 CLIENTE.ClaseOrden,
	 CLIENTE.Perfil,
	 CLIENTE.PerfilAnterior,
	 CLIENTE.FechaPerfilAnt,
	 IFNULL(CAST(CLIENTE.IdCliente AS VARCHAR),'')||','||IFNULL(CAST(CLIENTE.Rut AS VARCHAR),'')||','||IFNULL(CAST(CLIENTE.DIG AS VARCHAR),'')||','||IFNULL(CAST(CLIENTE.Pasaporte AS VARCHAR),'')||','||IFNULL(CLIENTE.GlobalLegalEntity,'')||','||IFNULL(CLIENTE.ApellidoPaterno,'')||','||IFNULL(CLIENTE.ApellidoMaterno,'')||','||IFNULL(CLIENTE.PrimerNombre,'')||','||IFNULL(CLIENTE.SegundoNombre,'')||','||IFNULL(CLIENTE.RazonSocial,'')||','||IFNULL(CAST(CLIENTE.Telefono AS VARCHAR),'')||','||IFNULL(CLIENTE.Email,'')||','||IFNULL(CLIENTE.TipoDire,'')||','||IFNULL(CLIENTE.Nombre,'')||','||IFNULL(CAST(CLIENTE.Numero AS VARCHAR),'')||','||IFNULL(CAST(CLIENTE.Depto AS VARCHAR),'')||','||IFNULL(CLIENTE.Comuna,'')||','||IFNULL(CLIENTE.Ciudad,'')||','||IFNULL(CLIENTE.EstadoProvincia,'')||','||IFNULL(CLIENTE.Pais,'')||','||IFNULL(CAST(CLIENTE.CodigoPostal AS VARCHAR),'')||','||IFNULL(CLIENTE.Nacionalidad,'')||','||IFNULL(CLIENTE.RelacionIntermediario,'')||','||IFNULL(CLIENTE.AdministracionCartera,'')||','||IFNULL(CAST(CLIENTE.RutEntEmpl AS VARCHAR),'')||','||IFNULL(CAST(CLIENTE.DigEntEmpl AS VARCHAR),'')||','||IFNULL(CLIENTE.RazonSocialEntEmpl,'')||','||IFNULL(CLIENTE.EstadoCivil,'')||','||IFNULL(Conyugue.Rut,'')||','||IFNULL(Conyugue.DIG,'')||','||IFNULL(Conyugue.Pasaporte,'')||','||IFNULL(Conyugue.ApellidoPaterno,	'')||','||IFNULL(Conyugue.ApellidoMaterno,'')||','||IFNULL(Conyugue.PrimerNombre,'')||','||IFNULL(Conyugue.SegundoNombre,'')||','||IFNULL(Conyugue.RazonSocial,'')||','||IFNULL(Conyugue.Telefono,'')||','||IFNULL(Conyugue.Email,'')||','||IFNULL(Conyugue.TipoDire,'')||','||IFNULL(Conyugue.Nombre,'')||','||IFNULL(Conyugue.Numero,'')||','||IFNULL(Conyugue.Depto,'')||','||IFNULL(Conyugue.Comuna,'')||','||IFNULL(Conyugue.Ciudad,'')||','||IFNULL(Conyugue.EstadoProvincia,'')||','||IFNULL(Conyugue.Pais,'')||','||IFNULL(Conyugue.CodigoPostal,'')||','||IFNULL(Conyugue.Nacionalidad,'')||','||IFNULL(Conyugue.RelacionIntermediario,'')||','||IFNULL(RepLegal.Rut ,'')||','||IFNULL(RepLegal.DIG ,'')||','||IFNULL(RepLegal.Pasaporte,'')||','||IFNULL(RepLegal.GlobalLegalEntity,'')||','||IFNULL(RepLegal.ApellidoPaterno,'')||','||IFNULL(RepLegal.ApellidoMaterno,'')||','||IFNULL(RepLegal.PrimerNombre,'')||','||IFNULL(RepLegal.SegundoNombre,'')||','||IFNULL(RepLegal.RazonSocial ,'')||','||IFNULL(RepLegal.Telefono,'')||','||IFNULL(RepLegal.Email ,'')||','||IFNULL(RepLegal.TipoDire ,'')||','||IFNULL(RepLegal.Nombre ,'')||','||IFNULL(RepLegal.Numero ,'')||','||IFNULL(RepLegal.Depto ,'')||','||IFNULL(RepLegal.Comuna,'')||','||IFNULL(RepLegal.Ciudad ,'')||','||IFNULL(RepLegal.EstadoProvincia,'')||','||IFNULL(RepLegal.Pais ,'')||','||IFNULL(RepLegal.CodigoPostal,'')||','||IFNULL(RepLegal.Nacionalidad,'')||','||IFNULL(RepLegal.RelacionIntermediario ,'')||','||IFNULL(RepLegal.EstadoCivil,'')||','||IFNULL(CLIENTE.cta1,'')||','||IFNULL(case when CLIENTE.cta1 = CLIENTE.cta2 then '' else CLIENTE.cta2 end,'')||','||IFNULL((select "CTA"."NumeroCuentaBancaria" from "BSCL.SocioNegocio.BD::PER.CuentaBancaria" as "CTA" where "CTA"."IdCuentaBancaria" in (select MAX("CTA"."IdCuentaBancaria") from "BSCL.SocioNegocio.BD::PER.CuentaBancaria" as "CTA" WHERE "CTA"."IdPersona" = CLIENTE.IdPersona 		AND "CTA"."NumeroCuentaBancaria" <> CLIENTE.cta2	AND "CTA"."NumeroCuentaBancaria" <> CLIENTE.cta1 )) ,'')||','||IFNULL(CLIENTE.ClaseOrden,	'')||','||IFNULL(CLIENTE.Perfil,	'')||','||IFNULL(CLIENTE.PerfilAnterior,	'')||','||IFNULL(CLIENTE.FechaPerfilAnt,	'')
	 ||','||IFNULL(CLIENTE.IndDerPrev,	'') as concatenados 
	 
from 	----------------------------------------------------------------------------DATOS CLIENTE
( 
	select "CLI"."IdCliente" as IdCliente,
	(select case when "DOC"."IdTipoDocumentoIdentidad" = 'R' then (select(ltrim(rtrim(substring("DOC"."DocumentoIdentidad",1,LENGTH("DOC"."DocumentoIdentidad")-2))))from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as "DOC" where "PER"."IdDocumentoIdentidad" = "DOC"."IdDocumentoIdentidad") else '' end 
	from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as "DOC" where "PER"."IdDocumentoIdentidad" = "DOC"."IdDocumentoIdentidad" )
	as Rut,

	(select
    case when "DOC"."IdTipoDocumentoIdentidad" = 'R' 
	then (select (ltrim(rtrim(substring("DOC"."DocumentoIdentidad",LENGTH("DOC"."DocumentoIdentidad"),LENGTH("DOC"."DocumentoIdentidad")))))from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as "DOC" where "PER"."IdDocumentoIdentidad" = "DOC"."IdDocumentoIdentidad") 
	else '' 
	end 
	from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as "DOC" where "PER"."IdDocumentoIdentidad" = "DOC"."IdDocumentoIdentidad" )
	as DIG,
	
	(select
	 case when "DOC"."IdTipoDocumentoIdentidad" = 'P' 
	 then (select "DOC"."DocumentoIdentidad" from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as "DOC" where "PER"."IdDocumentoIdentidad" = "DOC"."IdDocumentoIdentidad") 
	 else '' 
	 end 
	 from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as "DOC" where "PER"."IdDocumentoIdentidad" = "DOC"."IdDocumentoIdentidad" ) as Pasaporte,
	
	"PER"."GlobalLegalEntity" as GlobalLegalEntity,
	"PER"."ApellidoPaterno" as ApellidoPaterno,
	"PER"."ApellidoMaterno" as ApellidoMaterno,
	case when (SUBSTR_BEFORE(ltrim(rtrim("PER"."Nombres")),' ')) = '' 
	then (ltrim(rtrim("PER"."Nombres"))) 
	else SUBSTR_BEFORE(ltrim(rtrim("PER"."Nombres")),' ') 
	end 
	as PrimerNombre,
	
	SUBSTR_AFTER((ltrim(rtrim("PER"."Nombres"))),' ') as SegundoNombre,
	
	case when "PER"."Tipo" = 'J' 
	then "PER"."RazonSocial" 
	else '' 
	end as RazonSocial,
	
	"TEL"."Numero" as Telefono ,
	"DIRE"."Direccion" as Email,
-- Comienzo de Campos Asociados a la Dirección Fisica del Cliente
	'' as TipoDire,
	"DIRFIS"."Calle" as Nombre,
	"DIRFIS"."Numero" as Numero,
	"DIRFIS"."Departamento" as Depto,
	"DIRFIS"."NombreDivisionPais" as Comuna,
	"DIRFIS"."NombreCiudad" as Ciudad,
	'' as EstadoProvincia,
	"PAIS"."NombrePais" as Pais,
	"DIRF"."CodigoPostal" as CodigoPostal,
	 --modificar nacionalidad, agregar columna con gentilicio
	(select "PAISNAC"."NombrePais" 	from "BSCL.Plataforma.BD::REG.Pais" AS "PAISNAC" where "PAISNAC"."IdPais" = "PER"."IdPaisNacionalidadPrincipal") AS Nacionalidad,
	case 
		when "RELCORR"."CodigoValorIndicador" = 'SOCIO' then 'SO'
		when "RELCORR"."CodigoValorIndicador" = 'MIEMBRO' then 'MG'
		when "RELCORR"."CodigoValorIndicador" = 'OTRA' then 'XX'
		when "RELCORR"."CodigoValorIndicador" = 'DIRECTOR' then 'DI'
		when "RELCORR"."CodigoValorIndicador" = 'TRABAJADOR' then 'TR'
		else 'SR'  
	end					as RelacionIntermediario,
	--"PER"."CodigoTipoCargoEmpleado" AS RelacionIntermediario,
	--agregar case para ajustar a formato 380
	(case 
	when (select Count("Proposito") from "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" where "IdCliente" = "CLI"."IdCliente" and "Proposito" = 'ADMINCARTERA') > 0	
	then 'S' else 'N' end) 
	AS AdministracionCartera,
	(select (ltrim(rtrim(substring("DOC"."DocumentoIdentidad",1,LENGTH("DOC"."DocumentoIdentidad")-2))))from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS "DOC" ,"BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "PEREMPL" where "PEREMPL"."IdPersona" = "PER"."IdPersonaEmpleador" AND "PEREMPL"."IdDocumentoIdentidad" = "DOC"."IdDocumentoIdentidad") AS RutEntEmpl,
	(select	(ltrim(rtrim(substring("DOC"."DocumentoIdentidad",LENGTH("DOC"."DocumentoIdentidad"),LENGTH("DOC"."DocumentoIdentidad")))))from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS "DOC","BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "PEREMPL" where "PEREMPL"."IdPersona" = "PER"."IdPersonaEmpleador" AND "PEREMPL"."IdDocumentoIdentidad" = "DOC"."IdDocumentoIdentidad") AS DigEntEmpl,
	(select "PEREMPL"."RazonSocial" FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "PEREMPL" where "PEREMPL"."IdPersona" = "PER"."IdPersonaEmpleador") as RazonSocialEntEmpl,
	case
	when "PER"."EstadoCivil" = 'CA' then 'C'
	when "PER"."EstadoCivil" = 'SO' then 'S'
	when "PER"."EstadoCivil" = 'DI' then 'D'
	when "PER"."EstadoCivil" = 'SE' then 'D'
	when "PER"."EstadoCivil" = 'VI' then 'V'
	when "PER"."EstadoCivil" = 'CC' then 'U'
	else ''	
	end as EstadoCivil,
	"PER"."IdPersonaConyuge" AS IdPersonaConyuge,
	"PER"."IdPersonaRepresentante" AS IdPersonaRepresentante,
	"PER"."IdPersona" AS IdPersona,
	"CAT"."NombreCategoriaInversionista" AS Perfil,
	'PerfilAnterior' AS PerfilAnterior,
	'FechaPerfilAnt' AS FechaPerfilAnt,
	(select "DET"."DetalleIndicador" from "BSCL.Plataforma.BD::IND.Indicador" AS "IND" left join "BSCL.Plataforma.BD::IND.DetalleIndicador" "DET" on ("DET"."IdTipoIndicador" = "IND"."IdTipoIndicador") and  ("IND"."ValorIndicador" = TO_NVARCHAR("DET"."IdDetalleIndicador")) where ("IND"."IdEntidad" = "CLI"."IdCliente") and "IND"."IdTipoIndicador" in (select "IdTipoIndicador" from "BSCL.Plataforma.BD::IND.TipoIndicador" where "CodigoTipoIndicador" = 'CLASEORDEN')) AS ClaseOrden,
	(select "CTA"."NumeroCuentaBancaria" from "BSCL.SocioNegocio.BD::PER.CuentaBancaria" as "CTA" 	where "CTA"."IdCuentaBancaria" in (select MAX("CTA"."IdCuentaBancaria") from "BSCL.SocioNegocio.BD::PER.CuentaBancaria" as "CTA" WHERE "CTA"."IdPersona" = "PER"."IdPersona" )) as cta1,
	(select "CTA"."NumeroCuentaBancaria" from "BSCL.SocioNegocio.BD::PER.CuentaBancaria" as "CTA" 	where "CTA"."IdCuentaBancaria" in (select MIN("CTA"."IdCuentaBancaria") from "BSCL.SocioNegocio.BD::PER.CuentaBancaria" as "CTA" WHERE "CTA"."IdPersona" = "PER"."IdPersona" )) as cta2, 
	"IND_DERPREV"."ValorIndicador" AS IndDerPrev
	FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" as "PER" 
	INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" as "CLI" ON ( "PER"."IdPersona" = "CLI"."IdPersona" ) 
	LEFT join "BSCL.SocioNegocio.BD::PER.Telefono" as "TEL" on ("TEL"."IdTelefono" = "PER"."IdTelefonoPrincipal") 
	LEFT join "BSCL.SocioNegocio.BD::PER.DireccionElectronica" as "DIRE" on ("DIRE"."IdDireccionElectronica" = "PER"."IdDireccionElectronicaPrincipal" AND "DIRE"."Borrado"='N') 
	LEFT join "BSCL.SocioNegocio.BD::PER.DireccionFisica" as "DIRF" on ("DIRF"."IdDireccionFisica" = "PER"."IdDireccionFisicaPrincipal" AND "DIRF"."Borrado"='N') 
	LEFT join "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" as "DIRFIS" on ("DIRFIS"."IdDireccionFisica" = "PER"."IdDireccionFisicaPrincipal") 
	LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" as "PAIS" on ("PAIS"."IdPais" = "PER"."IdPaisNacionalidadPrincipal") 
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"  as "IND_DERPREV" ON ("IND_DERPREV"."IdUso"='IND' AND "IND_DERPREV"."CodigoTipoIndicador"='DERPRIV' AND  "IND_DERPREV"."IdCliente" ="CLI"."IdCliente")
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarCategoriaInversionista" as "CAT" on ("CAT"."IdCategoriaInversionista" ="CLI"."IdCategoriaInversionista")
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" as "RELCORR" on ("RELCORR"."IdPersona" = "PER"."IdPersona" AND "RELCORR"."CodigoTipoIndicador" = 'NCG380' AND "RELCORR"."NombreTipoIndicador" = 'Relación Corredora (NCG 380)')
	) as CLIENTE 
	----------------------------------------------------------------------------DATOS CONYUGUE

LEFT JOIN 
(select
	 "CONY"."IdEstado",
	(select case when "DOC"."IdTipoDocumentoIdentidad" = 'R' then (select (ltrim(rtrim(substring("DOC"."DocumentoIdentidad",1,	LENGTH("DOC"."DocumentoIdentidad")-2))))from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as "DOC" where "CONY"."IdDocumentoIdentidad" = "DOC"."IdDocumentoIdentidad") 
	else '' 
	end 
	from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as "DOC" 
	where "CONY"."IdDocumentoIdentidad" = "DOC"."IdDocumentoIdentidad" )as Rut,
	
	(select case when "DOC"."IdTipoDocumentoIdentidad" = 'R' then (select(ltrim(rtrim(substring("DOC"."DocumentoIdentidad",LENGTH("DOC"."DocumentoIdentidad"),LENGTH("DOC"."DocumentoIdentidad")))))from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as "DOC" where "CONY"."IdDocumentoIdentidad" = "DOC"."IdDocumentoIdentidad") 
	else '' 
	end 
	from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as "DOC" 
	where "CONY"."IdDocumentoIdentidad" = "DOC"."IdDocumentoIdentidad" )as DIG,
	
	(select case when "DOC"."IdTipoDocumentoIdentidad" = 'P' then (select "DOC"."DocumentoIdentidad" from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as "DOC" where "CONY"."IdDocumentoIdentidad" = "DOC"."IdDocumentoIdentidad") 
	else '' 
	end 
	from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as "DOC" 
	where "CONY"."IdDocumentoIdentidad" = "DOC"."IdDocumentoIdentidad" )as Pasaporte,
	
	"CONY"."ApellidoPaterno" as ApellidoPaterno,
	"CONY"."ApellidoMaterno" as ApellidoMaterno,
	case when (SUBSTR_BEFORE(ltrim(rtrim("CONY"."Nombres")),' ')) = ''
	then (ltrim(rtrim("CONY"."Nombres"))) 
	else SUBSTR_BEFORE(ltrim(rtrim("CONY"."Nombres")),	' ') 
	end as PrimerNombre,
	
    SUBSTR_AFTER((ltrim(rtrim("CONY"."Nombres"))),' ') as SegundoNombre,
	
	case when "CONY"."Tipo" = 'J' 
	then "CONY"."RazonSocial" 
	else '' 
	end as RazonSocial,
	(select	 "TEL"."Numero" from "BSCL.SocioNegocio.BD::PER.Telefono" as "TEL" 	where "TEL"."IdTelefono" = "CONY"."IdTelefonoPrincipal") as Telefono ,
	
	(select "DIRE"."Direccion" 	from "BSCL.SocioNegocio.BD::PER.DireccionElectronica" as "DIRE" where "DIRE"."IdDireccionElectronica" = "CONY"."IdDireccionElectronicaPrincipal") as Email,
	
	-- Comienzo de Campos Asociados a la Dirección Fisica del Cliente
	' ' as TipoDire,
	(select "DIRF"."Calle" from "BSCL.SocioNegocio.BD::PER.DireccionFisica" as "DIRF" 	where "DIRF"."IdDireccionFisica" = "CONY"."IdDireccionFisicaPrincipal" ) as Nombre,
	(select "DIRF"."Numero" from "BSCL.SocioNegocio.BD::PER.DireccionFisica" as "DIRF" 	where "DIRF"."IdDireccionFisica" = "CONY"."IdDireccionFisicaPrincipal") as Numero,
	(select "DIRF"."Departamento" from "BSCL.SocioNegocio.BD::PER.DireccionFisica" as "DIRF" where "DIRF"."IdDireccionFisica" = "CONY"."IdDireccionFisicaPrincipal") as Depto,
	(select "DIRFIS"."NombreDivisionPais" from "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" as "DIRFIS" where "DIRFIS"."IdDireccionFisica" = "CONY"."IdDireccionFisicaPrincipal" ) as Comuna,
	(select "CIU"."NombreCiudad" from "BSCL.SocioNegocio.BD::PER.DireccionFisica" as "DIRF"	LEFT JOIN "BSCL.Plataforma.BD::REG.Ciudad" as "CIU" on "CIU"."IdCiudad" = "DIRF"."IdCiudad"	where "DIRF"."IdDireccionFisica" = "CONY"."IdDireccionFisicaPrincipal" AND "DIRF"."IdDireccionFisica" = "CONY"."IdDireccionFisicaPrincipal" AND "DIRF"."Borrado"='N') as Ciudad,
	' ' as EstadoProvincia,
	(select "PAIS"."NombrePais" from "BSCL.Plataforma.BD::REG.Pais" as "PAIS" where "PAIS"."IdPais" = "CONY"."IdPaisNacionalidadPrincipal") as Pais,
	(select	"DIRF"."CodigoPostal" from "BSCL.SocioNegocio.BD::PER.DireccionFisica" as "DIRF" where "DIRF"."IdDireccionFisica" = "CONY"."IdDireccionFisicaPrincipal"	AND "DIRF"."Borrado"='N' ) as CodigoPostal,
	 --modificar nacionalidad, agregar columna con gentilicio
	(select "PAISNAC"."NombrePais" from "BSCL.Plataforma.BD::REG.Pais" as "PAISNAC" where "PAISNAC"."IdPais" = "CONY"."IdPaisNacionalidadPrincipal") as Nacionalidad,

	 case 
		when "RELCORRCY"."CodigoValorIndicador" = 'SOCIO' 		then 'SO'
		when "RELCORRCY"."CodigoValorIndicador" = 'MIEMBRO' 	then 'MG'
		when "RELCORRCY"."CodigoValorIndicador" = 'OTRA' 		then 'XX'
		when "RELCORRCY"."CodigoValorIndicador" = 'DIRECTOR' 	then 'DI'
		when "RELCORRCY"."CodigoValorIndicador" = 'TRABAJADOR' 	then 'TR'
		else 'SR'  
	end	 as RelacionIntermediario,
	--agregar case para ajustar a formato 380
	"CONY"."IdPersona" as IdPersona 
	FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "CONY" 
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS "CLI" ON ( "CONY"."IdPersona" = "CLI"."IdPersona" )
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" as "RELCORRCY" on ("RELCORRCY"."IdPersona" = "CONY"."IdPersona" AND "RELCORRCY"."CodigoTipoIndicador" = 'NCG380' AND "RELCORRCY"."NombreTipoIndicador" = 'Relación Corredora (NCG 380)')
	
) as Conyugue on CLIENTE.IdPersonaConyuge = Conyugue.idPersona 

LEFT JOIN 
	(select
	 "REP"."IdEstado",
	(select case when "DOC"."IdTipoDocumentoIdentidad" = 'R' then (select (ltrim(rtrim(substring("DOC"."DocumentoIdentidad",1,LENGTH("DOC"."DocumentoIdentidad")-2))))	from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as "DOC" where "REP"."IdDocumentoIdentidad" = "DOC"."IdDocumentoIdentidad") 
	else '' 
	end 
	from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as "DOC" 
	where "REP"."IdDocumentoIdentidad" = "DOC"."IdDocumentoIdentidad" )as Rut,
	
	(select case when "DOC"."IdTipoDocumentoIdentidad" = 'R' then (select (ltrim(rtrim(substring("DOC"."DocumentoIdentidad",LENGTH("DOC"."DocumentoIdentidad"),	LENGTH("DOC"."DocumentoIdentidad")))))
	from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as "DOC" where "REP"."IdDocumentoIdentidad" = "DOC"."IdDocumentoIdentidad") 
	else '' 
	end 
	from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as "DOC" 
	where "REP"."IdDocumentoIdentidad" = "DOC"."IdDocumentoIdentidad" )as DIG,
	
	(select case when "DOC"."IdTipoDocumentoIdentidad" = 'P' then (select "DOC"."DocumentoIdentidad" from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as "DOC" 
	where "REP"."IdDocumentoIdentidad" = "DOC"."IdDocumentoIdentidad") 
	else '' 
	end 
	from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as "DOC" 
	where "REP"."IdDocumentoIdentidad" = "DOC"."IdDocumentoIdentidad" )as Pasaporte,
	
	"REP"."GlobalLegalEntity" as GlobalLegalEntity,
	 "REP"."ApellidoPaterno" as ApellidoPaterno,
	 "REP"."ApellidoMaterno" as ApellidoMaterno,
	 case when (SUBSTR_BEFORE(ltrim(rtrim("REP"."Nombres")),
	' ')) = '' 
	then (ltrim(rtrim("REP"."Nombres"))) 
	else SUBSTR_BEFORE(ltrim(rtrim("REP"."Nombres")),
	' ') 
	end as PrimerNombre,
    SUBSTR_AFTER((ltrim(rtrim("REP"."Nombres"))),' ') as SegundoNombre,
	
	case when "REP"."Tipo" = 'J' 
	then "REP"."RazonSocial" 
	else '' 
	end as RazonSocial,
	(select	 "TEL"."Numero" from "BSCL.SocioNegocio.BD::PER.Telefono" as "TEL" 	where "TEL"."IdTelefono" = "REP"."IdTelefonoPrincipal") as Telefono ,
	(select	 "DIRE"."Direccion" from "BSCL.SocioNegocio.BD::PER.DireccionElectronica" as "DIRE" where "DIRE"."IdDireccionElectronica" = "REP"."IdDireccionElectronicaPrincipal") as Email,
	 -- Comienzo de Campos Asociados a la Dirección Fisica del Cliente
	' ' as TipoDire,
	(select	 "DIRF"."Calle" from "BSCL.SocioNegocio.BD::PER.DireccionFisica" as "DIRF" where "DIRF"."IdDireccionFisica" = "REP"."IdDireccionFisicaPrincipal" ) as Nombre,
	(select	 "DIRF"."Numero" from "BSCL.SocioNegocio.BD::PER.DireccionFisica" as "DIRF" where "DIRF"."IdDireccionFisica" = "REP"."IdDireccionFisicaPrincipal") as Numero,
	(select	 "DIRF"."Departamento" from "BSCL.SocioNegocio.BD::PER.DireccionFisica" as "DIRF" where "DIRF"."IdDireccionFisica" = "REP"."IdDireccionFisicaPrincipal") as Depto,
	(select	 "DIRFIS"."NombreDivisionPais" from "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" as "DIRFIS" where "DIRFIS"."IdDireccionFisica" = "REP"."IdDireccionFisicaPrincipal" ) as Comuna,
	(select	 "CIU"."NombreCiudad" from "BSCL.SocioNegocio.BD::PER.DireccionFisica" as "DIRF" LEFT JOIN "BSCL.Plataforma.BD::REG.Ciudad" as "CIU" on "CIU"."IdCiudad" = "DIRF"."IdCiudad" where "DIRF"."IdDireccionFisica" = "REP"."IdDireccionFisicaPrincipal" AND "DIRF"."IdDireccionFisica" = "REP"."IdDireccionFisicaPrincipal" AND "DIRF"."Borrado"='N') as Ciudad,
	 ' ' as EstadoProvincia,
	 (select "PAIS"."NombrePais" from "BSCL.Plataforma.BD::REG.Pais" as "PAIS" 	where "PAIS"."IdPais" = "REP"."IdPaisNacionalidadPrincipal") as Pais,
	 (select "DIRF"."CodigoPostal" from "BSCL.SocioNegocio.BD::PER.DireccionFisica" as "DIRF" where "DIRF"."IdDireccionFisica" = "REP"."IdDireccionFisicaPrincipal" AND "DIRF"."Borrado"='N' ) as CodigoPostal,
	 --modificar nacionalidad, agregar columna con gentilicio
	(select	 "PAISNAC"."NombrePais" from "BSCL.Plataforma.BD::REG.Pais" as "PAISNAC" where "PAISNAC"."IdPais" = "REP"."IdPaisNacionalidadPrincipal") as Nacionalidad,
	 "REP"."CodigoTipoCargoEmpleado" as RelacionIntermediario,
	--agregar case para ajustar a formato 380
	case
	when "REP"."EstadoCivil" = 'CA' then 'C'
	when "REP"."EstadoCivil" = 'SO' then 'S'
	when "REP"."EstadoCivil" = 'DI' then 'D'
	when "REP"."EstadoCivil" = 'SE' then 'D'
	when "REP"."EstadoCivil" = 'VI' then 'V'
	when "REP"."EstadoCivil" = 'CC' then 'U'
	else ''	
	end
	as EstadoCivil,
	"REP"."IdPersona" as IdPersona,
	"REP"."IdPersonaConyuge" as IdPersonaConyuge 
	FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "REP" 
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS "CLI" ON ( "REP"."IdPersona" = "CLI"."IdPersona" ) 
	) as RepLegal on CLIENTE.IdPersonaRepresentante = RepLegal.IdPersona 

LEFT JOIN ( select
	 "CONYREP"."IdEstado",
	(select case when "DOC"."IdTipoDocumentoIdentidad" = 'R' then (select(ltrim(rtrim(substring("DOC"."DocumentoIdentidad",1,LENGTH("DOC"."DocumentoIdentidad")-2))))from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as "DOC" where "CONYREP"."IdDocumentoIdentidad" = "DOC"."IdDocumentoIdentidad") 
	 else ''
	 end 
    from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as "DOC" where "CONYREP"."IdDocumentoIdentidad" = "DOC"."IdDocumentoIdentidad" )as Rut,
	(select case when "DOC"."IdTipoDocumentoIdentidad" = 'R' then (select (ltrim(rtrim(substring("DOC"."DocumentoIdentidad",LENGTH("DOC"."DocumentoIdentidad"),	LENGTH("DOC"."DocumentoIdentidad")))))from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as "DOC" where "CONYREP"."IdDocumentoIdentidad" = "DOC"."IdDocumentoIdentidad") 
	else '' 
	end 
	from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as "DOC" 
	where "CONYREP"."IdDocumentoIdentidad" = "DOC"."IdDocumentoIdentidad" )as DIG,
	(select	 case when "DOC"."IdTipoDocumentoIdentidad" = 'P' then (select "DOC"."DocumentoIdentidad" from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as "DOC" 	where "CONYREP"."IdDocumentoIdentidad" = "DOC"."IdDocumentoIdentidad") 		
	else '' 
	end 
	from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as "DOC" 
	where "CONYREP"."IdDocumentoIdentidad" = "DOC"."IdDocumentoIdentidad" )as Pasaporte,
	
	"CONYREP"."GlobalLegalEntity" as GlobalLegalEntity,
	"CONYREP"."ApellidoPaterno" as ApellidoPaterno,
	"CONYREP"."ApellidoMaterno" as ApellidoMaterno,
	case when (SUBSTR_BEFORE(ltrim(rtrim("CONYREP"."Nombres")),' ')) = ''
	then (ltrim(rtrim("CONYREP"."Nombres"))) 
	else SUBSTR_BEFORE(ltrim(rtrim("CONYREP"."Nombres")),' ') 
	end as PrimerNombre,
	
	SUBSTR_AFTER((ltrim(rtrim("CONYREP"."Nombres"))),' ') as SegundoNombre,
	 case when "CONYREP"."Tipo" = 'J' 
	then "CONYREP"."RazonSocial" 
	else '' 
	end as RazonSocial,
	(select	"TEL"."Numero" from "BSCL.SocioNegocio.BD::PER.Telefono" as "TEL" where "TEL"."IdTelefono" = "CONYREP"."IdTelefonoPrincipal") as Telefono ,
	(select "DIRE"."Direccion" from "BSCL.SocioNegocio.BD::PER.DireccionElectronica" as "DIRE" where "DIRE"."IdDireccionElectronica" = "CONYREP"."IdDireccionElectronicaPrincipal") as Email,
	 -- Comienzo de Campos Asociados a la Dirección Fisica del Cliente
	' ' as TipoDire,
	(select	 "DIRF"."Calle" from "BSCL.SocioNegocio.BD::PER.DireccionFisica" as "DIRF" where "DIRF"."IdDireccionFisica" = "CONYREP"."IdDireccionFisicaPrincipal" ) as Nombre,
	(select	 "DIRF"."Numero" from "BSCL.SocioNegocio.BD::PER.DireccionFisica" as "DIRF" where "DIRF"."IdDireccionFisica" = "CONYREP"."IdDireccionFisicaPrincipal") as Numero,
	(select "DIRF"."Departamento" from "BSCL.SocioNegocio.BD::PER.DireccionFisica" as "DIRF" where "DIRF"."IdDireccionFisica" = "CONYREP"."IdDireccionFisicaPrincipal") as Depto,
	 --------------------------------------
	(select "DIRFIS"."NombreDivisionPais" from "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" as "DIRFIS" 	where "DIRFIS"."IdDireccionFisica" = "CONYREP"."IdDireccionFisicaPrincipal" ) as Comuna,
	(select	 "CIU"."NombreCiudad" from "BSCL.SocioNegocio.BD::PER.DireccionFisica" as "DIRF" LEFT JOIN "BSCL.Plataforma.BD::REG.Ciudad" as "CIU" on "CIU"."IdCiudad" = "DIRF"."IdCiudad" 	where "DIRF"."IdDireccionFisica" = "CONYREP"."IdDireccionFisicaPrincipal" AND "DIRF"."IdDireccionFisica" = "CONYREP"."IdDireccionFisicaPrincipal" AND "DIRF"."Borrado"='N') as Ciudad,
	'' as EstadoProvincia,
	(select	 "PAIS"."NombrePais" from "BSCL.Plataforma.BD::REG.Pais" as "PAIS" 	where "PAIS"."IdPais" = "CONYREP"."IdPaisNacionalidadPrincipal") as Pais,
	(select	 "DIRF"."CodigoPostal" from "BSCL.SocioNegocio.BD::PER.DireccionFisica" as "DIRF" where "DIRF"."IdDireccionFisica" = "CONYREP"."IdDireccionFisicaPrincipal"	AND "DIRF"."Borrado"='N' ) as CodigoPostal,
	 --modificar nacionalidad, agregar columna con gentilicio
	(select "PAISNAC"."NombrePais" from "BSCL.Plataforma.BD::REG.Pais" as "PAISNAC" where "PAISNAC"."IdPais" = "CONYREP"."IdPaisNacionalidadPrincipal") as Nacionalidad,
	 case 
		when "RELCORR"."CodigoValorIndicador" = 'SOCIO' 	 then 'SO'
		when "RELCORR"."CodigoValorIndicador" = 'MIEMBRO' 	 then 'MG'
		when "RELCORR"."CodigoValorIndicador" = 'OTRA' 		 then 'XX'
		when "RELCORR"."CodigoValorIndicador" = 'DIRECTOR' 	 then 'DI'
		when "RELCORR"."CodigoValorIndicador" = 'TRABAJADOR' then 'TR'
		else 'SR'  
	end							as RelacionIntermediario,
	-- "CONYREP"."CodigoTipoCargoEmpleado" as RelacionIntermediario,
	 --'RelacionIntermediario' as RelaIntermediario, --select * from "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarTipoRelacion" cruzar con TipoClasificacionRelacion
	"CONYREP"."IdPersona" as IdPersona,
	"CONYREP"."IdPersonaConyuge" as IdPersonaConyuge,
	"CONYREP"."IdPersonaRepresentante" as IdPersonaRepresentante 
	FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "CONYREP" 
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS "CLI" ON ( "CONYREP"."IdPersona" = "CLI"."IdPersona" )
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" as "RELCORR" on ("RELCORR"."IdPersona" = "CONYREP"."IdPersona" AND "RELCORR"."CodigoTipoIndicador" = 'NCG380' AND "RELCORR"."NombreTipoIndicador" = 'Relación Corredora (NCG 380)')
	
	 ) as ConyRepLegal on RepLegal.IdPersonaConyuge = ConyRepLegal.IdPersona 
	
	WITH READ ONLY;
	
	
--GRANT SELECT ON "BSCL.Normativa.BD.Vistas::NOR.ConsultarClienteOnline" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
	
