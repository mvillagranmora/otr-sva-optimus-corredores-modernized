set schema bscl;

call "BSCL.Plataforma.BD.Utilitarios::POCNuevaEmpresa" (
	p_IdPais						=> 166,
	p_IdEmpresa						=> 1313,
	p_CodigoEmpresa	   	 			=> 'EMPRESAPE',
	p_NombreEmpresa					=> 'EMPRESA PERÚ',
	p_RutEmpleadoMaster 			=> '16929326-0',
	p_IdEmpresaCopiarConfiguracion 	=> 1,
	p_SobreescribirEmpresa			=> 'S',
	p_out_Usuario 					=> ?,
	p_out_Clave 					=> ?,
	p_out_IdSocioNegocio			=> ?,
	p_out_IdPersona 				=> ?,
	p_out_IdEmpleado 				=> ?,
	p_out_Mensaje					=> ?
);

--update  "BSCL"."BSCL.Plataforma.BD::PLA.Empresa"  
--set "Codigo" = 'CREDIPE',"Nombre" = 'CREDICORP CAPITAL PERU - DESAROLLO',"IdPais" = 166 where "IdEmpresa" = 1;


set schema bscl;
call "BSCL.Plataforma.BD.Servicios::CrearPojo"(
	p_nombreTabla => 'BSCL.SocioNegocio.BD::CLI.BloqueoAutomaticoClientes',
	p_nombrePojo => 'BloqueoAutomaticoClientes', 
	p_tipo => 'T', 
	p_out_pojo => ?
);
call "BSCL.Plataforma.BD.Servicios::CrearPojo"(
	p_nombreTabla => 'BSCL.SocioNegocio.BD::CLI.DetalleBloqueoAutomaticoClientes',
	p_nombrePojo => 'DetalleBloqueoAutomaticoClientes', 
	p_tipo => 'T', 
	p_out_pojo => ?
);

--BLOQUEO AUTOMATICO CLIENTES:

select 
	cli."IdCliente",
	cli."IdEstado" as "IdEstadoCliente",
	conf."IdTipoDocumento",
	doc."IdDocumento",
	doc."IdUltimoAnexo",
	anx."IdEstado" as "IdEstadoAnexo",
	ANX."FechaVigencia"
from "BSCL.SocioNegocio.BD::CLI.Cliente" cli 
join "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarBloqueoAutomaticoClientes" conf
	on cli."IdEmpresa" = conf."IdEmpresa"
left join "BSCL.Plataforma.BD::DOC.Documento" doc 
	on doc."IdTipoDocumento" = conf."IdTipoDocumento" and doc."IdCliente" = cli."IdCliente"
left join "BSCL"."BSCL.Plataforma.BD::DOC.Anexo" anx 
	on anx."IdDocumento" = DOC."IdDocumento" and anx."IdAnexo" = doc."IdUltimoAnexo"
where cli."IdEmpresa" = 1 and conf."IdEstadoTipoDocumento"='ACT'
order by cli."IdCliente",conf."IdTipoDocumento"
	
	
update "BSCL.SocioNegocio.BD::CLI.Cliente" 
set "IdEstado"='BLO',"IdTipoBloqueo"=:IdTipoBloqueo,
	"ComentarioBloqueo"=:ComentarioBloqueo,
	"FechaBloqueo"=CURRENT_UTCTIMESTAMP,"FechaModificacion"=CURRENT_UTCTIMESTAMP
where "IdCliente" in (:ListaIdCliente)


update "BSCL.SocioNegocio.BD::CLI.Cliente" 
set "IdEstado"='ACT' where "IdCliente" in (524891);
commit;

rollback;	
select * from "BSCL.SocioNegocio.BD::CLI.Cliente" where "IdCliente" = 524891


--PROBAR BLOQUEO/ACTIVACION AUTOMATICO:

update "BSCL.SocioNegocio.BD::CLI.Cliente"  
set "IdEstado" = 'ACT',"IdTipoDesbloqueo"='ATEMPO' 
where "IdCliente" = 524891;
commit;

--BLOQUEAR PROBAR
set schema bscl;
update "BSCL.SocioNegocio.BD::CLI.Cliente"  
set "IdEstado" = 'BLO',"IdTipoBloqueo"='BTEMPO',
"FechaBloqueo"='2017-04-01 00:00:00',"FechaBloqueoTemporal"='2017-05-01 00:00:00' 
where "IdCliente" = 524891;
commit;
-- BLOQUEO CONSULTAR
select "IdCliente","FechaBloqueo","FechaBloqueoTemporal" 
from "BSCL.SocioNegocio.BD::CLI.Cliente" 
where "IdEstado" = 'BLO' and "IdTipoBloqueo"='BTEMPO' 
and "IdEmpresa" = 1 and "FechaBloqueoTemporal"<='2017-05-02'


--DESBLOQUEAR PROBAR
set schema bscl;
update "BSCL.SocioNegocio.BD::CLI.Cliente"  
set "IdEstado" = 'ACT',"IdTipoDesbloqueo"='ATEMPO',
"FechaDesbloqueo"='2017-04-01 00:00:00',
"FechaDesbloqueoTemporal"='2017-05-01 00:00:00' 
where "IdCliente" = 524891;
commit;
-- DESBLOQUEO CONSULTAR
select "IdCliente","FechaBloqueo","FechaBloqueoTemporal" 
from "BSCL.SocioNegocio.BD::CLI.Cliente" 
where "IdEstado" = 'ACT' and "IdTipoDesbloqueo"='ATEMPO' 
and "IdEmpresa" = 1 and "FechaDesbloqueoTemporal"<='2017-05-02'

/*
select top 10 * from "BSCL.Plataforma.BD::PLA.SolicitudCambio" order by "IdSolicitudCambio" desc;
call "BSCL.SocioNegocio.BD.Servicios::CLI.ModificarEstadoCliente" (
	p_IdEmpresa						=> 1,
	p_IdUsuario						=> 1,
	p_IdModulo						=> 'SOC',
	p_UsaReglasNegocio	 			=> 'S',
	p_IdCliente						=> 524891,
	p_IdEstado						=> 'BLO',
	p_IdTipo 						=> 'BTEMPO',
	p_Fecha 						=> '2017-05-02 20:00:00',
	p_FechaHasta					=> '2017-05-07 20:00:00',
	p_Comentario 					=> 'ASD',
	p_out_RefId 					=> ?,
	p_out_AdvertenciasContador 		=> ?,
	p_out_AdvertenciasDetalle 		=> ?,
	p_out_ErroresContador 			=> ?,
	p_out_ErroresDetalle 			=> ?
);*/

--PRUEBAS PAUTA BLOQUEAR:

-- PAUTA BLOQUEAR 
set schema bscl;
INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" ("IdTipoPrograma","Descripcion","MaximoIntentos","Modulo","NombreClasePrograma","TipoClasePrograma","Nodo","EsPauta")
VALUES('BLOQUEO_AUTOMATICO_CLIENTES', 'Bloqueo automático de clientes', 1, 'PLATAFORMA', 'cl.bcs.socionegocio.jobs.PautaBloqueoAutomaticoClientes', 'JAVA', '','S');
DROP table #parametros;
create local temporary table #parametros("NombreParametro" nvarchar(100), "Valor" nvarchar(100));
INSERT INTO #parametros Select 'IdEmpresa' as "NombreParametro", '1' as "Valor" from dummy;
INSERT INTO #parametros Select 'IdUsuario' as "NombreParametro", '37' as "Valor" from dummy;
INSERT INTO #parametros Select 'SimularOperacion' as "NombreParametro", 'N' as "Valor" from dummy;
CALL "BSCL.Plataforma.BD.Servicios::BAT.AgregarAgenda"(
	p_IdEmpresa  =>  1,
	p_IdUsuario  =>  37,
	p_IdModulo   =>  'PLA',
	p_IdTipoTarea  =>  'BLOQUEO_AUTOMATICO_CLIENTES',
	p_EsDemonio    =>  'N'  ,
	p_DiaDeMes    =>  '**',
	p_DiaDeSemana  =>  '*',
	p_Mes      =>  '**',
	p_Hora      =>  '20',
	p_Minuto    =>  '15',
	p_SoloDiasHabiales  => 'S',
	p_SoloPrincipioMes  => 'N',
	p_SoloFinDeMes    => 'N',
	p_habilitado    =>  'S', 
	p_TablaParametros  =>  #parametros,              
	p_IdAgenda    => ?,
	p_IdPrograma    => ?,
	p_out_AdvertenciasContador => ?,
	p_out_AdvertenciasDetalle => ?,
	p_out_ErroresContador => ?,
	p_out_ErroresDetalle => ?              
);	 
commit;

-- PAUTA DESBLOQUEAR 
set schema bscl;
INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" ("IdTipoPrograma","Descripcion","MaximoIntentos","Modulo","NombreClasePrograma","TipoClasePrograma","Nodo","EsPauta")
VALUES('DESBLOQUEO_AUTOMATICO_CLIENTES', 'Desbloqueo automático de clientes', 1, 'PLATAFORMA', 'cl.bcs.socionegocio.jobs.PautaDesbloqueoAutomaticoClientes', 'JAVA', '','S');
DROP table #parametros;
create local temporary table #parametros("NombreParametro" nvarchar(100), "Valor" nvarchar(100));
INSERT INTO #parametros Select 'IdEmpresa' as "NombreParametro", '1' as "Valor" from dummy;
INSERT INTO #parametros Select 'IdUsuario' as "NombreParametro", '37' as "Valor" from dummy;
INSERT INTO #parametros Select 'SimularOperacion' as "NombreParametro", 'N' as "Valor" from dummy;
CALL "BSCL.Plataforma.BD.Servicios::BAT.AgregarAgenda"(
	p_IdEmpresa  =>  1,
	p_IdUsuario  =>  37,
	p_IdModulo   =>  'PLA',
	p_IdTipoTarea  =>  'DESBLOQUEO_AUTOMATICO_CLIENTES',
	p_EsDemonio    =>  'N'  ,
	p_DiaDeMes    =>  '**',
	p_DiaDeSemana  =>  '*',
	p_Mes      =>  '**',
	p_Hora      =>  '20',
	p_Minuto    =>  '20',
	p_SoloDiasHabiales  => 'S',
	p_SoloPrincipioMes  => 'N',
	p_SoloFinDeMes    => 'N',
	p_habilitado    =>  'S', 
	p_TablaParametros  =>  #parametros,              
	p_IdAgenda    => ?,
	p_IdPrograma    => ?,
	p_out_AdvertenciasContador => ?,
	p_out_AdvertenciasDetalle => ?,
	p_out_ErroresContador => ?,
	p_out_ErroresDetalle => ?              
);	 
commit;





--select * from "BSCL.Plataforma.BD::REG.Ciudad" where "IdPais" = 166; --1821
--select * from "BSCL.Plataforma.BD::REG.DivisionPais" where "IdPais" = 166 and "IdCiudad" = 0;
update "BSCL.Plataforma.BD::REG.DivisionPais" set "IdCiudad" = 1821 where "IdPais" = 166 and "IdCiudad" = 0;


/*select 'alter table "' || tc.table_name || '" alter( "IdTipoDocumentoIdentidad" NVARCHAR(3) '|| 
case tc.is_nullable when 'FALSE' then 'NOT NULL DEFAULT ''''' else 'NULL' end || ');' as "--LE_ALTER" 
--, *
from table_columns tc join sys.tables t on t.table_name = tc.table_name and t.schema_name = tc.schema_name
where tc.schema_name = 'BSCL' and tc.column_name = 'IdTipoDocumentoIdentidad'
and tc.data_type_name = 'NVARCHAR' and length = 1 and t.is_user_defined_type = 'FALSE';*/

/*call "BSCL.Plataforma.BD.Utilitarios::GeneraAlterTable" ( 	
    p_SchemaName	=> 'BSCL',
    p_Action		=> 'ALTER', -- ALTER, ADD, DROP
	p_TableName		=> '',
    p_ColumnName	=> 'IdTipoDocumentoIdentidad',
    p_DataTypeName	=> 'NVARCHAR',-- NVARCHAR, BIGINT, INT, DATE, DECIMAL, etc
    p_LengthNew		=> 3,
	p_out_Salida	=> ?
);*/ 





--select * from sys.procedures where schema_name = 'BSCL' and is_valid = 'FALSE'

/*
CREATE COLUMN TABLE "BSCL"."BSCL.Plataforma.BD::PLA.ConfiguracionGrilla"(
	"IdConfiguracionGrilla"	BIGINT NOT NULL,
	"IdEmpresa"					BIGINT NOT NULL,
	"Llave"						NVARCHAR(5000) NOT NULL,
	"Valor"						NVARCHAR(5000) NOT NULL,
	primary key ("IdConfiguracionGrilla")
);*/


/*
set schema bscl;
select * from  "BSCL.Plataforma.BD::PLA.Empresa"; 
update "BSCL.Plataforma.BD::PLA.Empresa" set "IdEstado" = 'INA' where "IdEmpresa" !=1;
update "BSCL.Plataforma.BD::PLA.Empresa" set "Nombre" = 'CREDICORP CAPITAL PERU - QA' where "IdEmpresa" =1;
commit;
*/

--ASIGNAR MENU FULL
update "BSCL.Plataforma.BD::ACC.Usuario"
set "CodigoMenu" = 'BCS_FULLMENU_PERU' where "CodigoUsuario" != 'MAMASTER';
COMMIT;


call "BSCL.Custodia.BD.Servicios::ConsultarCustodiaPortafolioPASO" (
	p_IdEmpresa                		 => 1,
	p_IdUsuario                		 => 37,
	p_IdModulo                 		 => 'CUS',
	p_IdCliente 					 => 116323,
	p_IdPortafolio 					 => 20021,
	p_IdClasificacionTipoInstrumento => '',
	p_IdTipoInstrumento 			 => '',
	p_IdInstrumento 				 => 1368,
	p_IdBoveda 						 => '',
	p_AgrupaPorBoveda 				 => 'S',
	p_AgrupaPorRelacion 			 => 'S',
	p_FechaConsulta 				 => '2017-04-05',
	p_TipoFechaConsulta				 => 'LIQ',
	p_out_tbl						 => ?
);

select "INGPrecioPromedioCompra","IdInstrumento","INGCantidad",* 
from "BSCL.Custodia.BD::CUS.Cartera" where "Nemotecnico"='LAN' 
and "IdCliente"=116323 and "IdPortafolio"=20021 and "IdEstado"='VIG';


select top 1 "suby_INGPrecioPromedioCompra",* 

select *
from "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultanea"
where "IdCliente"=116323 and "IdPortafolio"=20021 and "IdEstado"='VIG';


SELECT * 
FROM "BSCL.Custodia.BD::CUS.Subyacente"
where "Nemotecnico"='LAN' 
and "IdClienteOperacionRelacionada"=116323 
and "IdPortafolioOperacionRelacionada"=20021 and "IdEstado"='VIG';

select "INGPrecioPromedioCompra",* from "BSCL.Custodia.BD::CUS.Cartera" where "Nemotecnico"='LAN'
and "INGPrecioPromedioCompra">0 and "IdEstado"='VIG';


select 
	per."IdPersona",per."Tipo" as "TipoPersona",di."IdTipoDocumentoIdentidad",di."DocumentoIdentidad", 
	cli."IdCliente",cli."IdEstado" as "IdEstadoCliente",
	por."IdPortafolio",por."CodigoPortafolio",por."IdEstado" as "IdEstadoPortafolio",
	cli."IdTipoBloqueo" as "IdTipoBloqueoCliente", cli."IdTipoDesbloqueo" as "IdTipoDesbloqueoCliente",
	cli."ComentarioBloqueo" as "ComentarioBloqueoCliente",cli."ComentarioDesbloqueo" as "ComentarioDesbloqueoCliente",
	por."IdTipoBloqueo" as "IdTipoBloqueoPortafolio", por."IdTipoDesbloqueo" as "IdTipoDesbloqueoPortafolio",
	por."ComentarioBloqueo" as "ComentarioBloqueoPortafolio",por."ComentarioDesbloqueo" as "ComentarioDesbloqueoPortafolio"
from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" di
join "BSCL.SocioNegocio.BD::PER.Persona" per on per."IdPersona"=di."IdPersona"
join "BSCL.SocioNegocio.BD::CLI.Cliente" cli on cli."IdPersona"=per."IdPersona"
join "BSCL.Portafolio.BD::POR.Portafolio" por on por."IdCliente"=cli."IdCliente"
where cli."IdEmpresa"=IdEmpresa
and "DocumentoIdentidad" in (:ListaDocumentoIdentidad);


set schema bscl;
select 
 per."IdPersona",per."Tipo" as "TipoPersona",di."IdTipoDocumentoIdentidad",di."DocumentoIdentidad", 
 cli."IdCliente",cli."IdEstado" as "IdEstadoCliente", 
 por."IdPortafolio",por."CodigoPortafolio",por."IdEstado" as "IdEstadoPortafolio", 
 cli."IdTipoBloqueo" as "IdTipoBloqueoCliente", cli."IdTipoDesbloqueo" as "IdTipoDesbloqueoCliente", 
 cli."ComentarioBloqueo" as "ComentarioBloqueoCliente",cli."ComentarioDesbloqueo" as "ComentarioDesbloqueoCliente", 
 por."IdTipoBloqueo" as "IdTipoBloqueoPortafolio", por."IdTipoDesbloqueo" as "IdTipoDesbloqueoPortafolio", 
 por."ComentarioBloqueo" as "ComentarioBloqueoPortafolio",por."ComentarioDesbloqueo" as "ComentarioDesbloqueoPortafolio" 
 from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" di 
 join "BSCL.SocioNegocio.BD::PER.Persona" per 
 join "BSCL.SocioNegocio.BD::CLI.Cliente" cli on cli."IdPersona"=per."IdPersona" 
 join "BSCL.Portafolio.BD::POR.Portafolio" por on por."IdCliente"=cli."IdCliente" ;


select * from "BSCL.SocioNegocio.BD::PER.Persona"
select * from "BSCL.SocioNegocio.BD::CLI.Cliente"
select * from "BSCL.Portafolio.BD::POR.Portafolio";
select * from "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarTipoBloqueoSocioDeNegocio";




TIPOPERSONA;TIPODOCUMENTOIDENTIDAD;DOCUMENTOIDENTIDAD;CODIGOPORTAFOLIO;TIPOCAMBIOESTADOCLIENTE;TIPOCAMBIOESTADOPORTAFOLIO
N;16929326-0;1;BNOUSO;BNOUSO
N;16929326-0;2;BNOUSO;BFRAUD
J;11301384-2;1;BMUERT;BMUERT
N;22126550-5;0;;
N;22126550-5;0;;
N;40488049-7;;BFRAUD;
N;6654073-1;1;;BMUERT





-- MONITOR 
set schema bscl;

select * from "BSCL.Plataforma.BD::BAT.ParametrosAgenda" where "IdAgenda" in (
select "IdAgenda" from  "BSCL.Plataforma.BD::BAT.Agenda" where "IdTipoPrograma" = 'MONITOR_PLATAFORMA');

delete from "BSCL.Plataforma.BD::BAT.ParametroAgenda"
where "IdTipoPrograma" = 'MONITOR_PLATAFORMA';

delete from "BSCL.Plataforma.BD::BAT.Agenda" where "IdTipoPrograma" = 'MONITOR_PLATAFORMA';
commit;


delete from "BSCL.Plataforma.BD::BAT.ParametrosAgenda" where "IdAgenda" in (
select "IdAgenda" from  "BSCL.Plataforma.BD::BAT.Agenda" where "IdTipoPrograma" = 'MONITOR_PLATAFORMA');

delete from "BSCL.Plataforma.BD::BAT.Agenda" where "IdTipoPrograma" = 'MONITOR_PLATAFORMA';

commit;

set schema bscl;
select * from "BSCL.Plataforma.BD::BAT.TipoPrograma" where "IdTipoPrograma" = 'MONITOR_PLATAFORMA';
select top 20 * from "BSCL.Plataforma.BD::BAT.Programa" where "IdTipoPrograma" = 'MONITOR_PLATAFORMA' order by "IdPrograma" desc;

set schema bscl;
update "BSCL.Plataforma.BD::BAT.Programa" set "fechaEjecucion"=CURRENT_UTCTIMESTAMP
where "IdTipoPrograma" = 'MONITOR_PLATAFORMA';
commit;

set schema bscl;
update "BSCL.Plataforma.BD::BAT.Agenda" set
"SiguienteFechaDeEjecucion" = CURRENT_UTCTIMESTAMP
where "IdTipoPrograma" = 'MONITOR_PLATAFORMA';
commit;


/*******************
VALORIZACION PERU
********************/
set schema bscl;
--ENTRADAS SP VALORIZAR
call "BSCL.Plataforma.BD.Servicios::CrearPojo"(
	p_nombreTabla => 'BSCL.Plataforma.BD::VAL.Valorizar_Entrada_InstrumentosPQ',
	p_nombrePojo => 'InstrumentosPQ', 
	p_tipo => 'T', 
	p_out_pojo => ?
);
call "BSCL.Plataforma.BD.Servicios::CrearPojo"(
	p_nombreTabla => 'BSCL.Plataforma.BD::VAL.Valorizar_Entrada_InstrumentosWS',
	p_nombrePojo => 'InstrumentosWS', 
	p_tipo => 'T', 
	p_out_pojo => ?
);
--SALIDAS SP VALORIZAR
call "BSCL.Plataforma.BD.Servicios::CrearPojo"(
	p_nombreTabla => 'BSCL.Plataforma.BD::VAL.Valorizar_Salida_InstrumentosValorizadosPQ',
	p_nombrePojo => 'InstrumentosPQ', 
	p_tipo => 'T', 
	p_out_pojo => ?
);
call "BSCL.Plataforma.BD.Servicios::CrearPojo"(
	p_nombreTabla => 'BSCL.Plataforma.BD::VAL.Valorizar_Salida_InstrumentosValorizadosWS',
	p_nombrePojo => 'InstrumentosWS', 
	p_tipo => 'T', 
	p_out_pojo => ?
);

SELECT *
FROM "_SYS_BIC"."BSCL.Custodia.BD.Analytics/CustodiaFechaOperacion"('PLACEHOLDER' = ('$$FechaConsulta$$','0001-01-01'))


set schema bscl;
SELECT * FROM  "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" WHERE          
"IdPersona"=214068


select * from sys.tables where upper(table_name) 
like '%RELACIO%%';






-----
-- FORMULARIO INGRESO DE ORDENES RF
set schema bscl;


commit;



-- CONSULTA DE CUSTODIA A VISTAS:
set schema bscl;

call "BSCL.Custodia.BD.Servicios::ConsultarCustodiaPortafolioANTESCAMBIO" (
	p_IdEmpresa                			=>1,
	p_IdUsuario                			=>1,
	p_IdModulo                 			=>'CUS', 
	p_IdClasificacionTipoInstrumento 	=>'RV', 
	p_IdTipoInstrumento 				=>'ACN',
	p_IdCliente 						=>524896,
	p_IdPortafolio   					=>512357,
	p_TipoFechaConsulta					=>'ADQ', 
	p_out_tbl 							=> ?
);

set schema bscl;
call "BSCL.Custodia.BD.Servicios::ConsultarCustodiaPortafolioANTESCAMBIO" (
	p_IdEmpresa                			=>1,
	p_IdUsuario                			=>1,
	p_IdModulo                 			=>'CUS', 
	p_IdClasificacionTipoInstrumento 	=>'', 
	p_IdTipoInstrumento 				=>'',
	p_IdCliente 						=>524896,
	p_IdPortafolio   					=>512357,
	p_TipoFechaConsulta					=>'ADQ', 
	p_FechaConsulta						=>'2017-05-08',
	p_out_tbl 							=> ?
);

set schema bscl;
select * from
"BSCL.Portafolio.BD::POR.Portafolio"

set schema 
select cast(definition as varchar) from sys.views where view_name like '%UtilConsultarCustodia%'
"BSCL.Portafolio.BD::POR.Portafolio"


select * from "BSCL.Custodia.BD.Vistas::CUS.ConsultarCustodiaCarteraFechaAdquisicion"
where "IdCliente"=524896;

select * from "BSCL.Plataforma.BD::PLA.Configuracion" where "PLA.FechaSistema"

select * 
from "BSCL.Custodia.BD.Vistas::CUS.ConsultarCustodiaCartera" as ccc
	join "BSCL.Plataforma.BD::PLA.Configuracion" conf
		on (conf."IdEmpresa" = ccc."IdEmpresa" and conf."CodigoParametro"='PLA.FechaSistema')
where conf."Valor" >= ccc."EGRFechaOperacion" and conf."Valor" <= ccc."EGRFechaOperacion"	
	
select * from "BSCL.Custodia.BD.Vistas::CUS.ConsultarCustodiaCarteraFechaAdquisicion"

select * from (
	select "IdEmpresa","IdPersonaOrigen","IdPersonaDestino"
	from "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionPersona"
	where "IdEstado" = 'ACT' group by "IdEmpresa","IdPersonaOrigen","IdPersonaDestino") as rel 
where 







set schema bscl;
select * from "BSCL.Plataforma.BD::BAT.TipoPrograma";

set schema bscl;
select * from "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultanea";


select * from sys.procedures where is_valid = 'FALSE';

select * from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" where "DocumentoIdentidad" in(
'456456464-3',
'456456465-1',
'456456466-K',
'456456467-8');




--AGREGAR UNA VEZ CONFIGURADO EL BANCO CUSTODIO EN EL SISTEMA.
update "BSCL.Portafolio.BD::POR.TipoCustodia"
set "CodigoBancoCustodioDefault" = 'RENZO EL BANCO CUSTODIO'
where "IdTipoCustodia" = 'MATRIZCCB';
commit;
update "BSCL.Portafolio.BD::POR.TipoCustodia"
set "CodigoBancoLiquidadorDefault" = 'RENZO EL BANCO CUSTODIO'
where "IdTipoCustodia" = 'MATRIZCCB';
commit;

select * from  "BSCL.Portafolio.BD::POR.TipoCustodia"
where "IdTipoCustodia" = 'MATRIZCCB'


select * from "BSCL.Plataforma.BD.Vistas::PLA.ConsultarBancoCustodio";
select * from "BSCL.Plataforma.BD.Vistas::PLA.ConsultarBancoLiquidador";




-- OPTIMUSPE-72
set schema bscl;
select * from "BSCL.Plataforma.BD::IND.TipoIndicador" where "CodigoTipoIndicador" = 'REL_PEP';

select "CodigoTipoIndicador",count(1) as cant from "BSCL.Plataforma.BD::IND.TipoIndicador" group by "CodigoTipoIndicador"
order by cant desc;

set schema bscl;
insert into "BSCL.Plataforma.BD::IND.TipoIndicador"
("IdTipoIndicador","IdTipoEntidad","IdEmpresa","ValorIndicadorDefecto","ClaseDato","NombreTipoIndicador","GrupoIndicador","CantidadDecimales","LongitudMinima","LongitudMaxima","IdUso","IdEstado","RequiereAutorizacion","CodigoTipoIndicador","IdTipoPersona","RequiereFechaVencimiento","IdCarga","IdIndicadorMigracion")
VALUES(10027,'PERSONA','1','N','CHECKBOX','FUNCION PÚBLICA','PEP','0','0','0','IND','ACT','N','REL_PEP','NJ','N','0','0');
commit;

set schema bscl;
update "BSCL.Plataforma.BD::IND.TipoIndicador" set "CodigoTipoIndicador" = 'REL_FPU'
where "NombreTipoIndicador" = 'FUNCION PÚBLICA';
commit;

select * from  "BSCL.Plataforma.BD::IND.TipoIndicador" 
where "NombreTipoIndicador" = 'FUNCION PÚBLICA';

select * from "BSCL.Plataforma.BD::IND.TipoIndicador" where "CodigoTipoIndicador" in ('REL_FPU','REL_PEP');

select * from "BSCL.Plataforma.BD::IND.TipoIndicador" where "IdTipoEntidad" = 'PERSONA';

select * from "BSCL.Plataforma.BD::IND.Indicador" where "IdTipoIndicador" in (
	select "IdTipoIndicador" from "BSCL.Plataforma.BD::IND.TipoIndicador" 
	where "CodigoTipoIndicador" in ('REL_FPU','REL_PEP')
);

delete from "BSCL.Plataforma.BD::IND.Indicador" where "IdTipoIndicador" in (
	select "IdTipoIndicador" from "BSCL.Plataforma.BD::IND.TipoIndicador" 
	where "CodigoTipoIndicador" in ('REL_FPU','REL_PEP')
); 
commit;

select * from "BSCL.Plataforma.BD::IND.Indicador" where "IdEntidad" = 89903;

select * from "BSCL.Plataforma.BD.Vistas::IND.ConsultarTipoIndicador"
where "IdTipoIndicador" = 21


-- CORRECCION MANCOMUNA:
set schema bscl;
select * from "BSCL.SocioNegocio.BD::REL.TipoRelacion"; 

set schema bscl;
insert into "BSCL.SocioNegocio.BD::REL.TipoRelacion"
("IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion",
"IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion","UsaPorcentajeParticipacion",
"RetiraDocumentos","UsaFinalidadRelacion","UsaCondicionesRelacion","UsaGradoVinculacionParental")
values('COTITU',1'S','','COTITULAR','','N','ACT','RELACIONADOS','S','N','S','N','N');
commit;



--orden:
--BSCL.Portafolio.BD.Servicios::ConfirmarCrearPortafolioMasivo
--BSCL.SocioNegocio.BD.Servicios::CLI.ConfirmaAutorizacionEnrolamientoMasivo
--BSCL.SocioNegocio.BD.Servicios::PER.ConfirmaAutorizacionEnrolamientoPersonaMasivo

select "EsEnrolado","IdEstado",* from "BSCL.SocioNegocio.BD::PER.Persona" where "IdPersona" in (
  select "IdPersona" from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" where "DocumentoIdentidad" = '59704977-3'
);
  
select "EsEnrolado","IdEstado",* from "BSCL.SocioNegocio.BD::CLI.Cliente" where "IdPersona" in (
  select "IdPersona" from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" where "DocumentoIdentidad" = '59704977-3'
);

select "IdEstadoAutorizacion",* from "BSCL.Portafolio.BD::POR.Portafolio" where "IdCliente" in (
  select "IdCliente" from "BSCL.SocioNegocio.BD::CLI.Cliente" where "IdPersona" in (
    select "IdPersona" from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" where "DocumentoIdentidad" = '59704977-3')
);

drop table #tpor;
create local temporary table #tpor("IdPortafolio"	BIGINT);
insert into #tpor values(1450130);
commit;
call "BSCL"."BSCL.Portafolio.BD.Servicios::ConfirmarCrearPortafolioMasivo"(
	p_IdEmpresa						=> 1,
	p_IdUsuario						=> 1,
	p_IdModulo						=> 'SOC',
	p_UsaReglasNegocio	 			=> 'S',
	p_Portafolio					=> #tpor,
	p_IdEstadoAutorizacion			=> 'AUT',	
	p_out_AdvertenciasContador 		=> ?,
	p_out_AdvertenciasDetalle 		=> ?,
	p_out_ErroresContador 			=> ?,
	p_out_ErroresDetalle 			=> ?
);
rollback;


SELECT cli.*
FROM "BSCL.Portafolio.BD::POR.Portafolio" POR
JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI on POR."IdCliente" = CLI."IdCliente"
JOIN #vm P on P."IdPortafolio" = POR."IdPortafolio"
WHERE CLI."EsEnrolado" != 'S';


SELECT * FROM "BSCL.Plataforma.BD::BAT.TipoPrograma";

set schema bscl;

SELECT * FROM "BSCL.Plataforma.BD::BAT.TipoPrograma" tp
join "BSCL.Plataforma.BD::BAT.Agenda" a on tp."IdTipoPrograma" = a."IdTipoPrograma"
where tp."EsPauta" = 'S'
and tp."IdTipoPrograma" = 'ENTREGAR_SISTEMA';


SELECT * FROM "BSCL.Plataforma.BD::BAT.Programa" where "IdTipoPrograma" = 'ENTREGAR_SISTEMA';

update "BSCL.Plataforma.BD::BAT.Programa"
set "fechaEjecucion" = CURRENT_UTCTIMESTAMP
where "IdTipoPrograma" = 'ENTREGAR_SISTEMA' and "fechaEjecucion">CURRENT_UTCTIMESTAMP
and "idEstado" = 'PEN';
commit;

SELECT * FROM "BSCL.Plataforma.BD::BAT.TipoPrograma" where "IdTipoPrograma" = 'PLATAFORMA'


select * 
from "BSCL.Plataforma.BD::EVT.Mail" m join
"BSCL.Plataforma.BD::EVT.Destinatario" d on m."IdEmail" = d."IdEmail"
where m."Asunto" like '%NOTIFICACION%' ORDER BY m."IdEmail" desc;

--select top 50 * from "BSCL.Plataforma.BD::EVT.Destinatario"


select * from sys.tables where upper(table_name) like '%MAIL%'



-- INCIDENCIA: http://jira.bcs.cl/browse/SAP-5774	
-- Revisar que no exista una autorización de nivel inferior pendiente.
set schema bscl;	
select * from "BSCL.Plataforma.BD::AUT.TipoEvento" where "IdTipoEvento" in ('ENROL','ENRPER','NUEPOR') and "IdEmpresa" = 1;
update "BSCL.Plataforma.BD::AUT.TipoEvento" set "HorasVigencia" = 24
where "IdTipoEvento" in ('ENROL','ENRPER','NUEPOR'); --and "IdEmpresa" = 1;
commit;
select "SPConfirma",* from "BSCL.Plataforma.BD::AUT.TipoEvento" where "IdTipoEvento" in ('ENROL','ENRPER','NUEPOR') and "IdEmpresa" = 1;

set schema bscl;
update "BSCL.Plataforma.BD::AUT.TipoEvento" set "SPFormato" = 8 where "IdTipoEvento" in('ENROL');-- and "IdEmpresa" = 1;
commit;


-- http://jira.bcs.cl/browse/OPTIMUSPE-178
-- PERSONA: Rango Patrimonio no obligatorio.
-- CLIENTE: Segmento cliente no obligatorio.
-- PORTAFOLIO: Propósito no obligatorio.

-- http://jira.bcs.cl/browse/OPTIMUSPE-196
-- ANALISIS:
	set schema bscl;
	select * from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad";
	select * from "BSCL.SocioNegocio.BD::PER.Persona" where  "IdPaisNacimiento" = 166; --"RazonSocial" like '%PROFUTURO%'
	select * from "BSCL.SocioNegocio.BD::PER.Persona" where "IdCarga">0 and "RazonSocial" like '%PROFUTURO%'
	select * from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" 
	where "IdPersona" in (
		select "IdPersona" from "BSCL.SocioNegocio.BD::PER.Persona" where  "IdPaisNacimiento" = 166)
	and "IdTipoDocumentoIdentidad" = 'R';	
--SOLUCION:
	update "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" 
	set "IdTipoDocumentoIdentidad" = 'RUT'
	where "IdPersona" in (
		select "IdPersona" from "BSCL.SocioNegocio.BD::PER.Persona" where  "IdPaisNacimiento" = 166)
	and "IdTipoDocumentoIdentidad" = 'R';
	commit;



-- ORDEN RF
set schema bscl;
select * from "BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio" in (1571);
select * from "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento";

-- BUG  
-- http://jira.bcs.cl/browse/IMPCREDOPT-395
set schema bscl;
select * from "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" where "NombreCorto" = 'BTU';

DESA
IdEmisor;NombreCorto;CodigoEmisor;CodigoRegulador;Regulador;EmiteIIF;EmiteIRF;EmiteFungible;ValorClasificacionRiesgoIIF;IdEntidadClasificadoraRiesgo;IndicadorAGF;IdPersona;IdEstado;FechaActualizacion;DocumentoIdentidad;TipoDocumentoIdentidad;NombreCompleto;TipoEntidad
BTU;BTU;KX;BTU;SVS;N;S;N;;;N;1.766.287;ACT;01-06-2017 19:35:52.0;60805000-0;R;BONO TESORERIA GRAL.REP. CHILE EN PESOS;1

MARCHA BLANCA
IdEmisor;NombreCorto;CodigoEmisor;CodigoRegulador;Regulador;EmiteIIF;EmiteIRF;EmiteFungible;ValorClasificacionRiesgoIIF;IdEntidadClasificadoraRiesgo;IndicadorAGF;IdPersona;IdEstado;FechaActualizacion;DocumentoIdentidad;TipoDocumentoIdentidad;NombreCompleto;TipoEntidad
BTU;BTU;KX;BTU;SVS;N;S;N;;;N;219.409;ACT;02-06-2017 8:46:42.0;60805000-0;R;TESORERIA GENERAL DE LA REPUBLICA;1


select "IdTipoReajuste",* from "BSCL.Plataforma.BD::INS.Instrumento" where "IdTipoInstrumento" = 'BON';

select * from "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoTipoReajuste" 
where "IdTipoInstrumento" = 'BON' and "Nemotecnico" = 'CONTI5BC2A';

set schema bscl;
select * FROM "BSCL.Plataforma.BD::INS.Instrumento" inst 
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS tipoins ON tipoins."IdTipoInstrumento" = inst."IdTipoInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoTipoReajuste" as ITI on ITI."IdInstrumento" = inst."IdInstrumento"
JOIN "BSCL.Plataforma.BD::PLA.TipoReajuste" as TR on (TR."IdTipoReajuste" = case when tipoins."IdClasificacionTipoInstrumento" = 'IF' then ITI."IdTipoReajuste" else inst."IdTipoReajuste" end)
where inst."Nemotecnico" = 'CONTI5BC2A';

select "IdTipoReajuste","IdMoneda",* FROM "BSCL.Plataforma.BD::INS.Instrumento" where "Nemotecnico" = 'CONTI5BC2A';

select "IdTipoReajuste",* from "BSCL.Plataforma.BD::INS.Instrumento" where "IdTipoInstrumento" = 'BON';




set schema bscl;
drop table #t;
create local temporary table #t("IdInterno" BIGINT,"IdOrden" BIGINT,"IdBolsa" NVARCHAR(3));
insert into #t
select 312609,312609,'' from dummy; 
commit;
--{"p_IdEmpresa":1313,"p_IdUsuario":89,"p_IdModulo":"ORD","p_Ordenes":[{"IdInterno":312609,"IdOrden":312609,"IdBolsa":""}]}

call "BSCL.Orden.BD.Servicios::CantarOrdenes" ( 
    p_IdEmpresa => 1313,
    p_IdUsuario => 89,
    p_IdModulo =>'ORD',
	p_Ordenes => #t,
	p_out_InformacionContador =>?,
	p_out_InformacionDetalle =>?,
	p_out_AdvertenciasContador =>?,
	p_out_AdvertenciasDetalle =>?,
	p_out_ErroresContador =>?,
	p_out_ErroresDetalle =>?
);

CALL "BSCL.Orden.BD.Servicios::CambiarEstadoOrdenMasivo" (
	p_Ordenes				=> #t,	
	p_NuevoEstado			=> 'OFE',
	p_out_SalidaDetalle		=> ?,
	p_out_SalidaResumen		=> ?
);

"BSCL.Orden.BD.Servicios::NotificarEstadoOrden" ( 
    IN p_IdEmpresa                        BIGINT,
    IN p_IdUsuario                        BIGINT,
    IN p_IdModulo                         NVARCHAR(3),
	
	IN p_Ordenes						  table ("IdOrden" BIGINT)
	)



--PIRUETAS VALORIZADOR	
set schema bscl;

select * from "BSCL.Plataforma.BD::PLA.Configuracion" 
select * from "BSCL.Plataforma.BD::MER.ParidadYTipoCambio";
select * from "BSCL.Plataforma.BD::MER.ParametroFinanciero"; 
select * from "BSCL.Plataforma.BD::VAL.ConfigValorizador";
SELECT * FROM "BSCL.Plataforma.BD::VAL.Config";

ALTER SYSTEM CANCEL SESSION '316715';

drop table #t;
create local temporary table #t(
"IdInterno"				BIGINT,
"IdPortafolio" 			BIGINT, 
"IdTipoProducto" 		NVARCHAR(4), 
"IdTipoOperacion" 		NVARCHAR(6), 
"IdCanalIngreso" 		NVARCHAR(4),
"IdBolsa" 					NVARCHAR(3),
"IdCorredorCorresponsal" 	BIGINT
);
insert into #t select 4095 as "IdInterno",1450115 as "IdPortafolio",'INRF','COMRF','AGE','BCS',null as "IdCorredorCorresponsal" from dummy;
commit;
CALL "BSCL.Orden.BD.Servicios::ConsultarCobrosPortafolios"(
	p_IdEmpresa				=> 1,
	p_IdUsuario				=> 87,
	p_IdModulo				=> 'PLA',
	p_Portafolios			=> #t,
	p_out_MatrizOperacional	=> ?,
	p_out_AdvertenciasContador	=> ?,
	p_out_AdvertenciasDetalle	=> ?,
	p_out_ErroresContador		=> ?,
	p_out_ErroresDetalle		=> ?);
	
	
	





select * from "BSCL"."BSCL.Plataforma.BD::PLA.Moneda";
select * from "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumento";
select * from "BSCL"."BSCL.Plataforma.BD::INS.TipoOperacion";
select * from "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" where "IdTipoInstrumento" = 'BON';
delete from "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" where "IdTipoInstrumento" = 'BON';
commit;
select max("IdMonedaFormato") from "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato";


commit;
select * from "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" where "IdTipoInstrumento" = 'BON'
select "PrecioOrden",* from "BSCL"."BSCL.Orden.BD::ORD.Orden" where "IdOrden" = 312437
set schema bscl;
select * from "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento"
where "IdTipoOperacion" like '%RF%' and "IdTipoInstrumento" = 'BON';
select * from "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" where "IdTipoInstrumento" = 'BON';


	
	
	
select * from "BSCL.Plataforma.BD::REG.DivisionPais"


set schema bscl;
SELECT"Nombre", sum("Id") AS "Id" FROM "_SYS_BIC"."BSCL.Custodia.BD.Analytics/demo4" GROUP BY "Nombre";

select "IdEmpresa","TipoFechaConsulta" from "BSCL.Custodia.BD::CUS.PasoCustodiaPortafolio"

insert into "BSCL.Custodia.BD::CUS.PasoCustodiaPortafolio" ("IdEmpresa","TipoFechaConsulta") select 1,'HOA' from dummy;
select * from "BSCL.Custodia.BD::CUS.PasoCustodiaPortafolio";
commit;

--EXECUTE IMMEDIATE 'insert into "BSCL.Custodia.BD::CUS.PasoCustodiaPortafolio" ("IdEmpresa","TipoFechaConsulta") select 1,''HOA'' from dummy;';

set schema bscl;
select * from "BSCL.Plataforma.BD::INS.TipoInstrumento" where "IdClasificacionTipoInstrumento"='RF';

select distinct "IdPaisEmisor" from "BSCL.Plataforma.BD::INS.Instrumento" 
where "IdClasificacionTipoInstrumento"='RF';

select * from "BSCL.Plataforma.BD::INS.Instrumento" where "IdPaisEmisor" = 166

SELECT * FROM "BSCL.Plataforma.BD::VAL.Config";
	
	
	
set schema bscl;

select * from (
select distinct TTO."AccionOrden",TTO."IdTipoOperacion",TI."IdTipoInstrumento",TTO."IdTipoProducto",TTO."EsVentaCorta" 
from "BSCL"."BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" TI 
join "BSCL"."BSCL.Plataforma.BD::INS.TipoOperacion" tto ON TI."IdTipoOperacion" = TTO."IdTipoOperacion"  
WHERE TTO."AccionOrden" IS NOT NULL) t
where "IdTipoInstrumento" in ('BEM');






set schema bscl;
call "BSCL.Orden.BD.Servicios::ConsultarRequisitosBasicosCrearOrdenes" (
	p_IdEmpresa						=>1313,
	p_IdUsuario						=>1,
	p_IdModulo						=>'ORD', 
	p_IdEmpleado					=>11439,
	p_IdTipoOperacion				=>'VENRF',
	P_IdPortafolio					=>1450115,
	p_out_InformacionContador		=>?,
	p_out_InformacionDetalle 		=>?,
	p_out_AdvertenciasContador		=>?,
	p_out_AdvertenciasDetalle		=>?,
	p_out_ErroresContador 			=>?,
	p_out_ErroresDetalle 			=>?);
	
	
SELECT 
	CASE 
		WHEN det1."IdTipoOperacion" IS NOT NULL THEN 'S' 
		WHEN det2."IdTipoOperacion" IS NOT NULL THEN 'S' ELSE 'N' END as "PEC"
	,*
	FROM "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" emp
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" tipoope on tipoope."IdTipoOperacion" = 'VENRF'
	LEFT JOIN "BSCL.Plataforma.BD::COM.PlantillaEmpleado" pnt on emp."IdPlantillaEmpleado" = pnt."IdPlantillaEmpleado"
	LEFT JOIN "BSCL.Plataforma.BD::COM.DetallePlantillaEmpleado" det1 on det1."IdPlantillaEmpleado" = pnt."IdPlantillaEmpleado" and det1."IdEstado" = 'ACT' and det1."IdTipoProducto" = tipoope."IdTipoProducto" and det1."IdTipoOperacion" = 'VENRF'
	LEFT JOIN "BSCL.Plataforma.BD::COM.DetallePlantillaEmpleado" det2 on det2."IdPlantillaEmpleado" = pnt."IdPlantillaEmpleado" and det2."IdEstado" = 'ACT' and det2."IdTipoProducto" = tipoope."IdTipoProducto" and det2."IdTipoOperacion" = '0'
	WHERE emp."IdEmpleado" = 11439;	

select * 
FROM "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado"
WHERE "IdEmpleado" = 11439;

	
select * 
FROM "BSCL.SocioNegocio.BD::EMP.Empleado" 
WHERE "IdEmpleado" = 11439;
set schema bscl;
update "BSCL.SocioNegocio.BD::EMP.Empleado" 
set "UtilizaPlantillaEmpleado" = 'S'
where "IdEmpleado" = 11439;
commit;
	
	
select * from "BSCL.Plataforma.BD::PLA.SolicitudCambio" where "IdTipoEntidad"='EMPLEADO' and "IdEntidadOrigen" = 11439;
delete from "BSCL.Plataforma.BD::PLA.SolicitudCambio" where "IdTipoEntidad"='EMPLEADO' and "IdEntidadOrigen" = 11439;
commit;

select * from "BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio" = 1580;


select * from "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion"where "IdTipoInstrumento" = 'BON'




1;BBC;UF;CLP;PESO CHILENO

SELECT 
	"MonedaLiquidacion"."IdTipoInstrumentoMonedaLiquidacion"
	,"MonedaLiquidacion"."IdTipoInstrumento"
	,"MonedaLiquidacion"."IdMoneda"
	,"MonedaLiquidacion"."IdMonedaLiquidacion"
	,"Moneda"."Nombre" as "NombreMonedaLiquidacion"
FROM  "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" AS "MonedaLiquidacion"
INNER JOIN "BSCL.Plataforma.BD::PLA.Moneda" as "Moneda" on "MonedaLiquidacion"."IdMonedaLiquidacion" = "Moneda"."IdMoneda"

select * from  "BSCL.Plataforma.BD::PLA.Moneda"
select * from  "BSCL.Plataforma.BD::INS.Instrumento" where "Nemotecnico" = 'COLOM0628'
select * from  "BSCL.Plataforma.BD::INS.Instrumento" where "IdMoneda" = 'PEN'


SELECT * FROM "BSCL.Plataforma.BD.Vistas::VAL.ConsultarBonos" 
where "IdMoneda" = 'PEN' and "FechaVencimiento" >'2017-05-15';

select * from "BSCL.Plataforma.BD::CRE.ModuloEmpresa" where "IdModulo" = 'SIS';

update "BSCL.Plataforma.BD::CRE.ModuloEmpresa" set "TipoEstado" = 'A' where "IdModulo" = 'SIS';
commit;



select * from "BSCL.Plataforma.BD::BAT.Programa" where "IdTipoPrograma" = 'TIPIFICAR_INSTRUMENTOS' ORDER BY "IdPrograma" desc;

select * from "BSCL.Plataforma.BD.Vistas::PAU.ConsultarPauta"

select  top 1 p."FechaEjecucionPrograma",a."UltimaFechaDeEjecucion",
			p."IdTipoPrograma",p."Descripcion",p."MaximoIntentos",p."Modulo",p."NombreClasePrograma", 
			p."TipoClasePrograma",p."EsPauta",p."Ejecucion",p."RequiereTipoPrograma",p."SiguienteTipoPrograma",p."IdPrograma", 
			p."IdGrupoParametro",p."IdAgenda",p."Intento",p."IdUsuario",p."IdEstadoPrograma",p."NombreEstadoPrograma",p."ResultadoPrograma", 
			p."FechaCreacionPrograma",p."FechaEjecucionPrograma",p."FechaFinPrograma",p."FechaSistemaPrograma",p."IdProgramaReprocesado",
			p."IdControlPauta",p."FechaSistema",p."FechaCreacion",p."IdEstado",p."NombreEstado",p."Mensaje"
			from "BSCL.Plataforma.BD.Vistas::PAU.ConsultarPauta" p
			join "BSCL.Plataforma.BD::BAT.Agenda" a on p."IdTipoPrograma" = a."IdTipoPrograma"
			where p."IdTipoPrograma"= 'TIPIFICAR_INSTRUMENTOS' and CURRENT_UTCTIMESTAMP > coalesce(a."UltimaFechaDeEjecucion",'1900-01-01') 
			and p."FechaEjecucionPrograma"<=a."UltimaFechaDeEjecucion"
			and p."FechaEjecucionPrograma"!='0001-01-01 00:00:00.000' order by p."FechaEjecucionPrograma" desc; 
			
	
set schema bscl;
select * from "BSCL.Plataforma.BD::BAT.TipoPrograma" where "IdTipoPrograma" = 'VALORIZADOR';

select count(1) from "BSCL.Plataforma.BD::BAT.Programa" 
			
			
	
	

--INCIDENCIAS EXPRESS:
--http://jira.bcs.cl/browse/OPTIMUSPE-141
--ORDEN CL:
	set schema bscl;
	update "BSCL.Plataforma.BD::PLA.Moneda" set "Orden" = 0 where "IdMoneda" = 'CLP';
	update "BSCL.Plataforma.BD::PLA.Moneda" set "Orden" = 1 where "IdMoneda" = 'UF';
	update "BSCL.Plataforma.BD::PLA.Moneda" set "Orden" = 2 where "IdMoneda" = 'USD';
	update "BSCL.Plataforma.BD::PLA.Moneda" set "Orden" = 3 where "IdMoneda" = 'EUR';
	update "BSCL.Plataforma.BD::PLA.Moneda" set "Orden" = 4 where "IdMoneda" = 'PEN';
	commit;
--ORDEN PE:
	set schema bscl;
	update "BSCL.Plataforma.BD::PLA.Moneda" set "Orden" = 0 where "IdMoneda" = 'PEN';
	update "BSCL.Plataforma.BD::PLA.Moneda" set "Orden" = 1 where "IdMoneda" = 'USD';
	update "BSCL.Plataforma.BD::PLA.Moneda" set "Orden" = 2 where "IdMoneda" = 'EUR';
	update "BSCL.Plataforma.BD::PLA.Moneda" set "Orden" = 3 where "IdMoneda" = 'CLP';
	update "BSCL.Plataforma.BD::PLA.Moneda" set "Orden" = 4 where "IdMoneda" = 'UF';
	commit;
	
	
	
set schema bscl;
	select * from "BSCL.Plataforma.BD::INS.InstrumentoBono";
	update "BSCL.Plataforma.BD::INS.InstrumentoBono" set "ConvencionDias" = replace("ConvencionDias",'-','/');
	select * from "BSCL.Plataforma.BD::INS.InstrumentoBono";
commit;
	
	
set schema bscl;

select * from "BSCL.Plataforma.BD.Vistas::PLA.ConsultarCondicionDeLiquidacion" where "IdTipoOperacion" in ('COMIRF','VENIRF');
select * from "BSCL.Plataforma.BD::INS.TipoOperacion";



select * from "BSCL.Plataforma.BD::PLA.TipoOperacion" 




set schema bscl;
	-- T0
	insert into "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion"
	("IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado")
	select max("IdTipoOperacionYCondicionDeLiquidacion")+1,'S','T0','COMIRF','ACT' from "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion";
	
	insert into "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion"
	("IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado")
	select max("IdTipoOperacionYCondicionDeLiquidacion")+1,'S','T0','VENIRF','ACT' from "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion";
	-- T1
	insert into "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion"
	("IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado")
	select max("IdTipoOperacionYCondicionDeLiquidacion")+1,'S','T1','COMIRF','ACT' from "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion";
	
	insert into "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion"
	("IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado")
	select max("IdTipoOperacionYCondicionDeLiquidacion")+1,'S','T1','VENIRF','ACT' from "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion";
	-- T2
	insert into "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion"
	("IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado")
	select max("IdTipoOperacionYCondicionDeLiquidacion")+1,'S','T2','COMIRF','ACT' from "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion";
	
	insert into "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion"
	("IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado")
	select max("IdTipoOperacionYCondicionDeLiquidacion")+1,'S','T2','VENIRF','ACT' from "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion";
	
	select * from "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" where "IdTipoOperacion" in ('COMIRF','VENIRF')
commit;

--
SELECT * FROM "BSCL.Plataforma.BD::INS.TipoProducto";

SELECT * FROM "BSCL.Plataforma.BD::INS.TipoInstrumento" where "IdTipoInstrumento" = 'BON';
SELECT * FROM BSCL."BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" where "IdTipoInstrumento" = 'BON';

select * from "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion";

select * from "BSCL.Orden.BD.Vistas::ConsultarCorredorCorresponsal";

select * from "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" where "RutaServicio" = 'PLUGIN_CUS.jar@cl.bcs.custodia.EjecutarVencimiento'

set schema bscl;
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" 
	("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT") 
	values (99999,'PLUGIN_CUS.jar@cl.bcs.custodia.EjecutarVencimiento',1,'P','','','');
commit


set schema bscl;
	select * from "BSCL.Plataforma.BD::AUT.TipoEvento" where "IdTipoEvento" in ('ENROL','ENRPER','NUEPOR') and "IdEmpresa" = 1;
	update "BSCL.Plataforma.BD::AUT.TipoEvento" set "HorasVigencia" = 24 where "IdTipoEvento" in ('ENROL','ENRPER','NUEPOR'); --and "IdEmpresa" = 1;
	--select "SPConfirma",* from "BSCL.Plataforma.BD::AUT.TipoEvento" where "IdTipoEvento" in ('ENROL','ENRPER','NUEPOR') and "IdEmpresa" = 1;
	update "BSCL.Plataforma.BD::AUT.TipoEvento" set "SPFormato" = 8 where "IdTipoEvento" in('ENROL');-- and "IdEmpresa" = 1;	
commit;


--QUERY DE ADELANTAR PAUTA.
set schema bscl;
select p."FechaEjecucionPrograma",a."UltimaFechaDeEjecucion",
p."IdTipoPrograma",p."Descripcion",p."MaximoIntentos",p."Modulo",p."NombreClasePrograma",
p."TipoClasePrograma",p."EsPauta",p."Ejecucion",p."RequiereTipoPrograma",p."SiguienteTipoPrograma",p."IdPrograma",
p."IdGrupoParametro",p."IdAgenda",p."Intento",p."IdUsuario",p."IdEstadoPrograma",p."NombreEstadoPrograma",p."ResultadoPrograma",
p."FechaCreacionPrograma",p."FechaEjecucionPrograma",p."FechaFinPrograma",p."FechaSistemaPrograma",p."IdProgramaReprocesado",
p."IdControlPauta",p."FechaSistema",p."FechaCreacion",p."IdEstado",p."NombreEstado",p."Mensaje" 
from "BSCL.Plataforma.BD.Vistas::PAU.ConsultarPauta" p 
join "BSCL.Plataforma.BD::BAT.Agenda" a on p."IdTipoPrograma" = a."IdTipoPrograma" 
where p."IdTipoPrograma" = 'ABRIR_SISTEMA' --and :FechaEjecucionPrograma < coalesce(a."UltimaFechaDeEjecucion",'2999-01-01') 
and p."FechaEjecucionPrograma">=a."UltimaFechaDeEjecucion" 
and p."FechaEjecucionPrograma"!='0001-01-01 00:00:00.000' order by p."FechaEjecucionPrograma" asc;


set schema bscl;
select * from "BSCL.Custodia.BD::GBL.CrearMovimientoCustodia_Entrada_Configuracion";

select * from "BSCL.Plataforma.BD::PAU.Dependencia";
select * from "BSCL.Plataforma.BD.Vistas::PAU.ConsultarPauta";

select * from "BSCL.Plataforma.BD::BAT.TipoPrograma";
select * from "BSCL.Plataforma.BD::PAU.Dependencia"; 
select top 100 * from "BSCL.Plataforma.BD::BAT.Programa" where "idEstado" = 'PEN' order by "IdPrograma" desc;
select * from "BSCL.Plataforma.BD::PAU.ControlPauta"



insert into "BSCL.Plataforma.BD::BAT.Agenda"(
"IdAgenda","IdTipoPrograma","EsDemonio","DiaDeMes","DiaDeSemana","Mes","Hora","Minuto","SoloDiasHabiles","SoloPrincipioMes","SoloFinDeMes","habilitado","SiguienteFechaDeEjecucion","UltimaFechaDeEjecucion"
)
select 99999,'PROCESO_SISTEMA_DUMMY','N','**','*','**','**','05','S','N','N','S','','' from dummy;
commit;




select * from "BSCL.Plataforma.BD::BAT.Agenda" where "IdTipoPrograma" = 'PROCESO_SISTEMA_DUMMY';

update "BSCL.Plataforma.BD::BAT.Agenda" set --"Hora" = '**', "Minuto" = '05', 
"SiguienteFechaDeEjecucion"=CURRENT_UTCTIMESTAMP,"UltimaFechaDeEjecucion"=CURRENT_UTCTIMESTAMP 
where "IdAgenda" = 10610;
commit;
delete from "BSCL.Plataforma.BD::BAT.Programa" where "IdTipoPrograma" = 'PROCESO_SISTEMA_DUMMY';
commit;

select * from "BSCL.Plataforma.BD::BAT.Agenda" where "IdTipoPrograma" = 'PROCESO_SISTEMA_DUMMY';
select top 3 CURRENT_UTCTIMESTAMP as kuky,"fechaEjecucion",* from "BSCL.Plataforma.BD::BAT.Programa" where "IdTipoPrograma" = 'PROCESO_SISTEMA_DUMMY'order by "IdPrograma" desc;

select * from "BSCL.Plataforma.BD::BAT.ParametrosPrograma" where "IdPrograma" in (
	select top 1 "IdPrograma" from "BSCL.Plataforma.BD::BAT.Programa" where "IdTipoPrograma" = 'PROCESO_SISTEMA_DUMMY'order by "IdPrograma" desc
);

commit
insert into "BSCL.Plataforma.BD::BAT.Programa" (
"fechaEjecucion","IdPrograma","IdGrupoParametro","IdAgenda","IdTipoPrograma","Intento","IdUsuario","idEstado","Resultado","fechaCreacion","fechaEjecucion","fechaFin","fechaSistema","Bloqueo","Nodo","ZonaHoraria","IdProgramaReprocesado","RutaSalida","RutaLog"
)
select "fechaEjecucion","IdPrograma","IdGrupoParametro","IdAgenda","IdTipoPrograma","Intento","IdUsuario","idEstado","Resultado","fechaCreacion","fechaEjecucion","fechaFin","fechaSistema","Bloqueo","Nodo","ZonaHoraria","IdProgramaReprocesado","RutaSalida","RutaLog"
from "BSCL.Plataforma.BD::BAT.Programa" where "IdPrograma" = 1120472;


update  ag set "UltimaFechaDeEjecucion" = CURRENT_UTCTIMESTAMP
from "BSCL.Plataforma.BD::BAT.Programa" pg INNER JOIN "BSCL.Plataforma.BD::BAT.Agenda" ag 
ON pg."IdAgenda"=ag."IdAgenda" and pg."IdPrograma" = 1120472 where ag."UltimaFechaDeEjecucion"<CURRENT_UTCTIMESTAMP
			        			
			        			
delete from bscl."BSCL.Plataforma.BD::BAT.Programa";
commit;	      

select * from  bscl."BSCL.Plataforma.BD::BAT.Nodos";  			

delete from "BSCL.Plataforma.BD::BAT.Agenda" where "IdAgenda" = 99999;
commit;
--ultimo registro ControlPauta
left join (select max("IdControlPauta") as "IdControlPauta","IdPrograma" from "BSCL.Plataforma.BD::PAU.ControlPauta" group by "IdPrograma") controlMax 
	on prog."IdPrograma"=controlMax."IdPrograma"
left join "BSCL.Plataforma.BD::PAU.ControlPauta" control o

set schema bscl;
select * from "BSCL.Plataforma.BD::BAT.Programa";
select * from "BSCL.Plataforma.BD::BAT.Agenda";



set schema bscl;

select * from "BSCL.Custodia.BD::CUS.ConfiguracionMovimientoCustodia" where "IdEmpresa" = 1313;
insert into "BSCL.Custodia.BD::CUS.ConfiguracionMovimientoCustodia"
	("IdTipoOperacion","IdEmpresa","IdAccionOperacion","UtilizaPlantillaMovimientoOriginal","InvierteClientesPlantillaMovimientoOriginal","BuscaMovimientosGeneradosPorCustodia",
	"PoseeInstrumentoSubyacente","EsGarantia","PermiteGarantiaAutomatica","GeneraCarteraNueva","RetornaCarteraAlPrecioTasa","GrabaClienteRelacionado",
	"EsClienteIngresoClienteRelacionado","PoseeInstrumentoRelacionado","UtilizaFechaLiquidacionBusqueda","PermiteAnulacion","PermiteAnularOperacionesCertificadas",
	"InstrumentoPorIdTipoOperacion","IdConceptoVentaCorta","IdTipoOperacionVentaCorta","IdTipoPrestamoVentaCorta","IdEstadoOperacion","AgrupaPorIdGrupoOperacionInterno",
	"OrdenEjecucionMovimientos","CliIngGeneraMvtoEnTransitoSinCustodia","CliIngMueveCarteraEnTransitoING","CliIngMueveCarteraING","CliIngMueveSubyacenteING",
	"CliIngMueveGarantiaING","CliIngMueveCarteraEnTransitoEGR","CliIngMueveCarteraEGR","CliIngMueveSubyacenteEGR","CliIngMueveGarantiaEGR",
	"CliIngMueveCarteraEnTransitoAutomaticaING","CliIngMueveCarteraAutomaticaING","CliIngMueveSubyacenteAutomaticaING","CliIngMueveGarantiaAutomaticaING",
	"CliIngMueveCarteraEnTransitoAutomaticaEGR","CliIngMueveCarteraAutomaticaEGR","CliIngMueveSubyacenteAutomaticaEGR","CliIngMueveGarantiaAutomaticaEGR",
	"CliIngIdCondicionLiquidacionTransitoAutoING","CliIngIdCondicionLiquidacionCarteraAutoING","CliIngIdCondicionLiquidacionSubyacenteAutoING",
	"CliIngIdCondicionLiquidacionGarantiaAutoING","CliIngIdCondicionLiquidacionTransitoAutoEGR","CliIngIdCondicionLiquidacionCarteraAutoEGR",
	"CliIngIdCondicionLiquidacionSubyacenteAutoEGR","CliIngIdCondicionLiquidacionGarantiaAutoEGR","CliEgrGeneraMvtoEnTransitoSinCustodia","CliEgrMueveCarteraEnTransitoING",
	"CliEgrMueveCarteraING","CliEgrMueveSubyacenteING","CliEgrMueveGarantiaING","CliEgrMueveCarteraEnTransitoEGR","CliEgrMueveCarteraEGR","CliEgrMueveSubyacenteEGR",
	"CliEgrMueveGarantiaEGR","CliEgrMueveCarteraEnTransitoAutomaticaING","CliEgrMueveCarteraAutomaticaING","CliEgrMueveSubyacenteAutomaticaING","CliEgrMueveGarantiaAutomaticaING",
	"CliEgrMueveCarteraEnTransitoAutomaticaEGR","CliEgrMueveCarteraAutomaticaEGR","CliEgrMueveSubyacenteAutomaticaEGR","CliEgrMueveGarantiaAutomaticaEGR",
	"CliEgrIdCondicionLiquidacionTransitoAutoING","CliEgrIdCondicionLiquidacionCarteraAutoING","CliEgrIdCondicionLiquidacionSubyacenteAutoING",
	"CliEgrIdCondicionLiquidacionGarantiaAutoING","CliEgrIdCondicionLiquidacionTransitoAutoEGR","CliEgrIdCondicionLiquidacionCarteraAutoEGR",
	"CliEgrIdCondicionLiquidacionSubyacenteAutoEGR","CliEgrIdCondicionLiquidacionGarantiaAutoEGR","IdEstado","IdUsuarioModificacionRegistro","FechaModificacionRegistro",
	"PermiteRetiroParcialSubyacente","BaseCalculoDeInteres","PoseeRetiroDeTitulosAutomatico","RetiroAutomaticoEsInstrumentoRelacionado")
select 
	"IdTipoOperacion",1313 as "IdEmpresa","IdAccionOperacion","UtilizaPlantillaMovimientoOriginal","InvierteClientesPlantillaMovimientoOriginal","BuscaMovimientosGeneradosPorCustodia",
	"PoseeInstrumentoSubyacente","EsGarantia","PermiteGarantiaAutomatica","GeneraCarteraNueva","RetornaCarteraAlPrecioTasa","GrabaClienteRelacionado",
	"EsClienteIngresoClienteRelacionado","PoseeInstrumentoRelacionado","UtilizaFechaLiquidacionBusqueda","PermiteAnulacion","PermiteAnularOperacionesCertificadas",
	"InstrumentoPorIdTipoOperacion","IdConceptoVentaCorta","IdTipoOperacionVentaCorta","IdTipoPrestamoVentaCorta","IdEstadoOperacion","AgrupaPorIdGrupoOperacionInterno",
	"OrdenEjecucionMovimientos","CliIngGeneraMvtoEnTransitoSinCustodia","CliIngMueveCarteraEnTransitoING","CliIngMueveCarteraING","CliIngMueveSubyacenteING",
	"CliIngMueveGarantiaING","CliIngMueveCarteraEnTransitoEGR","CliIngMueveCarteraEGR","CliIngMueveSubyacenteEGR","CliIngMueveGarantiaEGR",
	"CliIngMueveCarteraEnTransitoAutomaticaING","CliIngMueveCarteraAutomaticaING","CliIngMueveSubyacenteAutomaticaING","CliIngMueveGarantiaAutomaticaING",
	"CliIngMueveCarteraEnTransitoAutomaticaEGR","CliIngMueveCarteraAutomaticaEGR","CliIngMueveSubyacenteAutomaticaEGR","CliIngMueveGarantiaAutomaticaEGR",
	"CliIngIdCondicionLiquidacionTransitoAutoING","CliIngIdCondicionLiquidacionCarteraAutoING","CliIngIdCondicionLiquidacionSubyacenteAutoING",
	"CliIngIdCondicionLiquidacionGarantiaAutoING","CliIngIdCondicionLiquidacionTransitoAutoEGR","CliIngIdCondicionLiquidacionCarteraAutoEGR",
	"CliIngIdCondicionLiquidacionSubyacenteAutoEGR","CliIngIdCondicionLiquidacionGarantiaAutoEGR","CliEgrGeneraMvtoEnTransitoSinCustodia","CliEgrMueveCarteraEnTransitoING",
	"CliEgrMueveCarteraING","CliEgrMueveSubyacenteING","CliEgrMueveGarantiaING","CliEgrMueveCarteraEnTransitoEGR","CliEgrMueveCarteraEGR","CliEgrMueveSubyacenteEGR",
	"CliEgrMueveGarantiaEGR","CliEgrMueveCarteraEnTransitoAutomaticaING","CliEgrMueveCarteraAutomaticaING","CliEgrMueveSubyacenteAutomaticaING","CliEgrMueveGarantiaAutomaticaING",
	"CliEgrMueveCarteraEnTransitoAutomaticaEGR","CliEgrMueveCarteraAutomaticaEGR","CliEgrMueveSubyacenteAutomaticaEGR","CliEgrMueveGarantiaAutomaticaEGR",
	"CliEgrIdCondicionLiquidacionTransitoAutoING","CliEgrIdCondicionLiquidacionCarteraAutoING","CliEgrIdCondicionLiquidacionSubyacenteAutoING",
	"CliEgrIdCondicionLiquidacionGarantiaAutoING","CliEgrIdCondicionLiquidacionTransitoAutoEGR","CliEgrIdCondicionLiquidacionCarteraAutoEGR",
	"CliEgrIdCondicionLiquidacionSubyacenteAutoEGR","CliEgrIdCondicionLiquidacionGarantiaAutoEGR","IdEstado","IdUsuarioModificacionRegistro","FechaModificacionRegistro",
	"PermiteRetiroParcialSubyacente","BaseCalculoDeInteres","PoseeRetiroDeTitulosAutomatico","RetiroAutomaticoEsInstrumentoRelacionado"
from "BSCL.Custodia.BD::CUS.ConfiguracionMovimientoCustodia" where "IdEmpresa" = 1;
commit;


-- CONFIGURACION ARCHIVOS PROPERTIES PARA SER USADOS EN LOS JAR.
set schema bscl;
	delete from "BSCL.Plataforma.BD::PLA.Configuracion"  where "CodigoParametro" = 'PLA.RutaArchivosConfiguracion';
	
	insert into "BSCL.Plataforma.BD::PLA.Configuracion" 
	values (1,'PLA.RutaArchivosConfiguracion','Ruta archivos de configuracion estaticos usados por Optimus para invocar servicios externos','/home/optimusDES/jobm/','S');
	
	insert into "BSCL.Plataforma.BD::PLA.Configuracion" 
	values (1313,'PLA.RutaArchivosConfiguracion','Ruta archivos de configuracion estaticos usados por Optimus para invocar servicios externos','/home/optimusDES/jobm/','S');
commit;
