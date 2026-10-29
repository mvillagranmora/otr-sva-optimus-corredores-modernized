set schema bscl;

/*********************************************************************************
 (       )       (       )    (              )               )       (       )   
 )\ ) ( /(   (   )\ ) ( /(    )\ )        ( /(     (      ( /(   (   )\ ) ( /(   
(()/( )\())  )\ (()/( )\())  (()/(  (     )\())(   )\ )   )\())  )\ (()/( )\())  
 /(_)|(_)\ (((_) /(_)|(_)\    /(_)) )\   ((_)\ )\ (()/(  ((_)\ (((_) /(_)|(_)\   
(_))   ((_))\___(_))   ((_)  (_))_ ((_)   _((_|(_) /(_))_  ((_))\___(_))   ((_)  
/ __| / _ ((/ __|_ _| / _ \   |   \| __| | \| | __(_)) __|/ _ ((/ __|_ _| / _ \  
\__ \| (_) | (__ | | | (_) |  | |) | _|  | .` | _|  | (_ | (_) | (__ | | | (_) | 
|___/ \___/ \___|___| \___/   |___/|___| |_|\_|___|  \___|\___/ \___|___| \___/  
                                                                                 
**********************************************************************************/
--PERSONA MANCOMUNADA
set schema bscl;
	select * from "BSCL.SocioNegocio.BD::PER.TipoPersona" where "IdTipoPersona" = 'M';
	delete from "BSCL.SocioNegocio.BD::PER.TipoPersona" where "IdTipoPersona" = 'M';
	INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoPersona" ("IdTipoPersona","Descripcion")
	VALUES ('M','PERSONA MANCOMUNADA');
commit;

--habilitar division pais para Perú, esto habilita ver campo: Distrito en modal direccion fisica.
set schema bscl;
	update "BSCL.Plataforma.BD::REG.Pais" set "UsaDivisionPais" = 'S' where "IdPais" = 166;	
commit;

-- Cambia nombre de división país para Perú
set schema bscl;
	update "BSCL.Plataforma.BD::REG.Pais" set "NombreDivisionPais" = 'Distrito' where "IdPais" = 166;
commit;

--clasificacion fatca: VERIFICAR SI SON UNICOS PARA PERU, SE DEBERIA AGREGAR ID_PAIS o ID_EMPRESA
set schema bscl;
	insert into "BSCL.SocioNegocio.BD::PER.ClasificacionFatca" ("IdClasificacionFatca","Nombre")
	select 'US_PERSON','US_PERSON' from dummy union all
	select 'RESIDEEUU','RESIDENTES EEUU' from dummy;
commit;

-- Tipo de bloqueo temporal --validar si requiere "IdPais"
set schema bscl;
	insert into "BSCL"."BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"
	("IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado")
	select 1, 'BTEMPO','CLIENTE','BLOQUEO TEMPORAL','BLO','ACT' from dummy;
commit;

-- TIPO BLOQUEO TEMPORAL CLIENTE
set schema bscl;
	delete from "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio" where "IdTipoEntidad" = 'CLIENTE' and "IdTipoBloqueo" = 'DESBTEMPORAL';
	
	insert into "BSCL"."BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"
	("IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado")
	select 1, 'DESBTEMPO','CLIENTE','DESBLOQUEO TEMPORAL','ACT','ACT' from dummy;
	insert into "BSCL"."BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"
	("IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado")
	select 1313, 'DESBTEMPO','CLIENTE','DESBLOQUEO TEMPORAL','ACT','ACT' from dummy;
	
	select * from "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio" where "IdTipoEntidad" = 'CLIENTE'
commit;

-- SERVICIOS: MODELO BLOQUEO AUTOMATICO DE CLIENTES
set schema bscl;
	--select * from "BSCL"."BSCL.Plataforma.BD::PLA.Servicio"  where "RutaServicio" = 'BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarBloqueoAutomaticoClientes';
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT") 
	values (3500,'BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarBloqueoAutomaticoClientes' , 1,  'Q', '','','');
	
	--select * from  "BSCL.Plataforma.BD::PLA.Servicio" where "IdServicio"=20023;
	INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
	VALUES(20023,'PLUGIN_SOC.jar@cl.bcs.socionegocio.cli.ModificarConfiguracionBloqueoAutomaticoClientes',0,'J','','','');		
commit;

-- MAESTROS: MODELO BLOQUEO AUTOMATICO DE CLIENTES
set schema bscl;
	-- Tipo bloqueo automatico
	insert into "BSCL"."BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"
	("IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado")
	select 1, 'BAUTOM','CLIENTE','BLOQUEO AUTOMÁTICO','BLO','ACT' from dummy;
	-- Tipo desbloqueo automatico
	insert into "BSCL"."BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"
	("IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado")
	select 1, 'AAUTOM','CLIENTE','ACTIVACIÓN AUTOMÁTICA','ACT','ACT' from dummy;
commit;

-- PAUTA BLOQUEO AUTOMATICO CLIENTES
set schema bscl;
	select * from "BSCL.Plataforma.BD::BAT.Programa" where "IdTipoPrograma" = 'BLOQUEO_AUTOMATICO_CLIENTES';
	
	drop table #param;
	create local temporary table #param ("NombreParametro" nvarchar(100) , "Valor" nvarchar(100));
	insert into #param values('IdEmpresa','1');
	insert into #param values('IdUsuario','37');
	insert into #param values('SimularOperacion','N');
	
	call "BSCL.Plataforma.BD.Utilitarios::UtilCrearPautas" (
		p_IdEmpresa				=> 1,
		p_IdTipoPrograma		=> 'BLOQUEO_AUTOMATICO_CLIENTES',
		p_Descripcion			=> 'Pauta de bloqueo automatico clientes',
		p_NombreClasePrograma	=> 'cl.bcs.socionegocio.jobs.PautaBloqueoAutomaticoClientes',
		p_Nodo					=> '',
		p_IdModulo				=> 'SOC',
		p_Hora					=> '21',
		p_Minuto				=> '00',
		p_DependenciaAnterior	=> '',
		p_DependenciaProximo	=> '',
		p_Parametros			=> #param);
commit;

-- PAUTA ACTIVACION AUTOMATICO CLIENTES
set schema bscl;
	select * from "BSCL.Plataforma.BD::BAT.Programa" where "IdTipoPrograma" = 'ACTIVACION_AUTOMATICA_CLIENTES';
	drop table #param;
	create local temporary table #param ("NombreParametro" nvarchar(100) , "Valor" nvarchar(100));
	insert into #param values('IdEmpresa','1');
	insert into #param values('IdUsuario','37');
	insert into #param values('SimularOperacion','N');
	
	call "BSCL.Plataforma.BD.Utilitarios::UtilCrearPautas" (
		p_IdEmpresa				=> 1,
		p_IdTipoPrograma		=> 'ACTIVACION_AUTOMATICA_CLIENTES', --o DESBLOQUEO...
		p_Descripcion			=> 'Pauta de activacion automatica clientes',
		p_NombreClasePrograma	=> 'cl.bcs.socionegocio.jobs.PautaActivacionAutomaticaClientes',
		p_Nodo					=> '',
		p_IdModulo				=> 'SOC',
		p_Hora					=> '21',
		p_Minuto				=> '05',
		p_DependenciaAnterior	=> '',
		p_DependenciaProximo	=> '',
		p_Parametros			=> #param);
		
		--select * from "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarBloqueoAutomaticoClientes";
commit;

-- Define tipos de cliente para Perú (Empresa 1313 Chile DESA)
set schema bscl;
	delete from "BSCL.SocioNegocio.BD::CLI.TipoCliente" where "IdEmpresa" = 1313;
	insert into "BSCL.SocioNegocio.BD::CLI.TipoCliente" ("IdTipoCliente","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
	select 'INSTITUCIONAL','INSTITUCIONAL',1,'2017-01-26 00:00:00','2017-01-26 00:00:00',1313,'ACT' from dummy union all
	select 'INVERSIONISTA','INVERSIONISTA ACREDITADO',1,'2017-01-26 00:00:00','2017-01-26 00:00:00',1313,'ACT' from dummy union all
	select 'BROKERAGE','BROKERAGE',1,'2017-01-26 00:00:00','2017-01-26 00:00:00',1313,'ACT' from dummy union all
	select 'CUSTODIO','CUSTODIO',1,'2017-01-26 00:00:00','2017-01-26 00:00:00',1313,'ACT' from dummy union all
	select 'PATRIMONIOAUTONOMO','PATRIMONIO AUTÓNOMO',1,'2017-01-26 00:00:00','2017-01-26 00:00:00',1313,'ACT' from dummy;
commit;

-- DEFINE TIPOS DOCUMENTO IDENTIDAD PERU:
set schema bscl;
	insert into "BSCL.Plataforma.BD::REG.TipoDocumentoIdentidad"
	("IdTipoDocumentoIdentidad","NombreTipoDocumentoIdentidad","NombreAbrTipoDocumentoIdentidad","IdPais","PorDefecto","IdEstado")
	select 'DNI','DOCUMENTO NACIONAL DE IDENTIDAD','DNI',166,'N','ACT' from dummy union all
	select 'RUC','REGISTRO ÚNICO CONTRIBUYENTE','RUC',166,'N','ACT' from dummy union all
	select 'CE','CARNÉ DE EXTRANJERÍA','CE',166,'N','ACT' from dummy union all
	select 'PAS','PASAPORTE','PAS',166,'N','ACT' from dummy union all
	select 'CDI','CÉDULA DIPLOMÁTICA DE IDENTIDAD','CDI',166,'N','ACT' from dummy union all
	--select 'CIN','PASAPORTE','PAS',166,'N','ACT' from dummy union all
	select 'IUP','IDENTIFICADOR UNICO PRINCIPAL','IUP',166,'S','ACT' from dummy union all
	select 'RUT','REGISTRO ÚNICO DEL TITULAR (CAVALI)','RUT',166,'N','ACT' from dummy;
commit;

--RANGO PATRIMONIO
set schema bscl;
	--select * from "BSCL.SocioNegocio.BD::PER.RangoPatrimonio";
	delete from "BSCL.SocioNegocio.BD::PER.RangoPatrimonio";
	INSERT INTO "BSCL"."BSCL.SocioNegocio.BD::PER.RangoPatrimonio"("IdRangoPatrimonio","DescripcionRango","Minimo","Maximo") VALUES (166001,'Mayor a USD 500,001',500001,-1);
	INSERT INTO "BSCL"."BSCL.SocioNegocio.BD::PER.RangoPatrimonio"("IdRangoPatrimonio","DescripcionRango","Minimo","Maximo") VALUES (166002,'Entre USD 100,001 y USD 500,000',100001,500000);
	INSERT INTO "BSCL"."BSCL.SocioNegocio.BD::PER.RangoPatrimonio"("IdRangoPatrimonio","DescripcionRango","Minimo","Maximo") VALUES (166003,'Entre USD 50,001 y USD 100.000',50001,100000);
	INSERT INTO "BSCL"."BSCL.SocioNegocio.BD::PER.RangoPatrimonio"("IdRangoPatrimonio","DescripcionRango","Minimo","Maximo") VALUES (166004,'Entre USD 15,001 y USD 50,000',15001,50000);
	INSERT INTO "BSCL"."BSCL.SocioNegocio.BD::PER.RangoPatrimonio"("IdRangoPatrimonio","DescripcionRango","Minimo","Maximo") VALUES (166005,'Menor a USD 15,000',-1,15000);
commit;

--CLASE CLIENTE (a partir de planilla)
set schema bscl;
	--select * from "BSCL.SocioNegocio.BD::CLI.ClaseCliente";
	insert into "BSCL.SocioNegocio.BD::CLI.ClaseCliente"("IdClaseCliente","Nombre","IdEmpresa","IdEstado") values('RED','RED',1,'ACT');
	insert into "BSCL.SocioNegocio.BD::CLI.ClaseCliente"("IdClaseCliente","Nombre","IdEmpresa","IdEstado") values('EXTRANJEROS','EXTRANJEROS',1,'ACT');
	insert into "BSCL.SocioNegocio.BD::CLI.ClaseCliente"("IdClaseCliente","Nombre","IdEmpresa","IdEstado") values('AFP','AFP',1,'ACT');
	insert into "BSCL.SocioNegocio.BD::CLI.ClaseCliente"("IdClaseCliente","Nombre","IdEmpresa","IdEstado") values('CIASEGUROS','CIA. DE SEGUROS',1,'ACT');
	insert into "BSCL.SocioNegocio.BD::CLI.ClaseCliente"("IdClaseCliente","Nombre","IdEmpresa","IdEstado") values('INSTITUCIONALES','INSTITUCIONALES',1,'ACT');
	insert into "BSCL.SocioNegocio.BD::CLI.ClaseCliente"("IdClaseCliente","Nombre","IdEmpresa","IdEstado") values('BANCOS','BANCOS',1,'ACT');
	insert into "BSCL.SocioNegocio.BD::CLI.ClaseCliente"("IdClaseCliente","Nombre","IdEmpresa","IdEstado") values('FONDOSMUTUOS','FONDOS MUTUOS',1,'ACT');
	insert into "BSCL.SocioNegocio.BD::CLI.ClaseCliente"("IdClaseCliente","Nombre","IdEmpresa","IdEstado") values('ADMPATRIMONIOS','ADM. PATRIMONIOS',1,'ACT');
	insert into "BSCL.SocioNegocio.BD::CLI.ClaseCliente"("IdClaseCliente","Nombre","IdEmpresa","IdEstado") values('BANCAPRIVADA','BANCA PRIVADA',1,'ACT');
	insert into "BSCL.SocioNegocio.BD::CLI.ClaseCliente"("IdClaseCliente","Nombre","IdEmpresa","IdEstado") values('CREUZA','CREUZA',1,'ACT');
	insert into "BSCL.SocioNegocio.BD::CLI.ClaseCliente"("IdClaseCliente","Nombre","IdEmpresa","IdEstado") values('NIKLAS','NIKLAS',1,'ACT');
	insert into "BSCL.SocioNegocio.BD::CLI.ClaseCliente"("IdClaseCliente","Nombre","IdEmpresa","IdEstado") values('FONDOSMUTUOSCHILE','FONDOS MUTUOS CHILE',1,'ACT');
	insert into "BSCL.SocioNegocio.BD::CLI.ClaseCliente"("IdClaseCliente","Nombre","IdEmpresa","IdEstado") values('CHILE','CHILE',1,'ACT');
	insert into "BSCL.SocioNegocio.BD::CLI.ClaseCliente"("IdClaseCliente","Nombre","IdEmpresa","IdEstado") values('COLOMBIA','COLOMBIA',1,'ACT');
	insert into "BSCL.SocioNegocio.BD::CLI.ClaseCliente"("IdClaseCliente","Nombre","IdEmpresa","IdEstado") values('CREDICORPBOLSA','CREDICORP BOLSA',1,'ACT');
commit;

-- SERVICIO CONSULTA CLASE CLIENTE
set schema bscl;
	INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
	VALUES(3562,'BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarClaseCliente',0,'Q','','','');
commit;

--TIPO CLIENTE
set schema bscl;
	--select * from "BSCL.SocioNegocio.BD::CLI.TipoCliente";
	insert into "BSCL.SocioNegocio.BD::CLI.TipoCliente"("IdTipoCliente","Nombre","IdEmpresa","IdEstado") values('INSTITUCIONAL','INSTITUCIONAL',1,'ACT');
	insert into "BSCL.SocioNegocio.BD::CLI.TipoCliente"("IdTipoCliente","Nombre","IdEmpresa","IdEstado") values('INVERSIONISTA','INVERSIONISTA ACREDITADO',1,'ACT');
	insert into "BSCL.SocioNegocio.BD::CLI.TipoCliente"("IdTipoCliente","Nombre","IdEmpresa","IdEstado") values('BROKEREXT','BROKER EXTRANJERO',1,'ACT');
	insert into "BSCL.SocioNegocio.BD::CLI.TipoCliente"("IdTipoCliente","Nombre","IdEmpresa","IdEstado") values('CUSTODIO','CUSTODIO',1,'ACT');
	insert into "BSCL.SocioNegocio.BD::CLI.TipoCliente"("IdTipoCliente","Nombre","IdEmpresa","IdEstado") values('PATRIMONIOAUTONOMO','PATRIMONIO AUTÓNOMO',1,'ACT');
	insert into "BSCL.SocioNegocio.BD::CLI.TipoCliente"("IdTipoCliente","Nombre","IdEmpresa","IdEstado") values('RETAIL','RETAIL',1,'ACT');
	insert into "BSCL.SocioNegocio.BD::CLI.TipoCliente"("IdTipoCliente","Nombre","IdEmpresa","IdEstado") values('SAFP','SAFP- SOCIEDAD ADMINISTRADORA DE FONDO DE PENSIONES',1,'ACT');
	insert into "BSCL.SocioNegocio.BD::CLI.TipoCliente"("IdTipoCliente","Nombre","IdEmpresa","IdEstado") values('SAFM','SAFM-SOCIEDAD ADMINISTRADORA DE FFMM',1,'ACT');
	--QUITAR BROKERAGE?
commit;

--TIPO BLOQUEO:
set schema bscl;
	--select * from "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio" where "IdEmpresa"=1;
	--delete from "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio" where "IdEmpresa"=1 and "IdTipoEntidad"='CLIENTE' and "IdAccion"='BLO';
	insert into "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"("IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") values(1, 'CLIBLOQ1','CLIENTE','INFORMACIÓN INCOMPLETA','BLO','ACT');
	insert into "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"("IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") values(1, 'CLIBLOQ2','CLIENTE','INFORMACIÓN VENCIDA','BLO','ACT');
	insert into "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"("IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") values(1, 'CLIBLOQ3','CLIENTE','CLIENTE CON RESTRICCIÓN POR VINCULACIÓN GRUPO CREDICORP','BLO','ACT');
	insert into "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"("IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") values(1, 'CLIBLOQ4','CLIENTE','RUT BLOQUEADO CAVALI','BLO','ACT');
	insert into "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"("IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") values(1, 'CLIBLOQ5','CLIENTE','INCUMPLIMIENTO DE PAGO','BLO','ACT');
	insert into "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"("IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") values(1, 'CLIBLOQ6','CLIENTE','SIST. PARTICIPACIÓN CIUDADANA','BLO','ACT');
	insert into "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"("IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") values(1, 'CLIBLOQ7','CLIENTE','PROBLEMA CON CREDIBOLSA','BLO','ACT');
	insert into "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"("IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") values(1, 'CLIBLOQ8','CLIENTE','CONGELAMIENTO DE FONDOS','BLO','ACT');

	insert into "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"("IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") values(1313, 'CLIBLOQ1','PORTAFOLIO','INFORMACIÓN INCOMPLETA','BLO','ACT');
	insert into "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"("IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") values(1313, 'CLIBLOQ2','PORTAFOLIO','INFORMACIÓN VENCIDA','BLO','ACT');
	insert into "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"("IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") values(1313, 'CLIBLOQ3','PORTAFOLIO','CLIENTE CON RESTRICCIÓN POR VINCULACIÓN GRUPO CREDICORP','BLO','ACT');
	insert into "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"("IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") values(1313, 'CLIBLOQ4','PORTAFOLIO','RUT BLOQUEADO CAVALI','BLO','ACT');
	insert into "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"("IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") values(1313, 'CLIBLOQ5','PORTAFOLIO','INCUMPLIMIENTO DE PAGO','BLO','ACT');
	insert into "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"("IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") values(1313, 'CLIBLOQ6','PORTAFOLIO','SIST. PARTICIPACIÓN CIUDADANA','BLO','ACT');
	insert into "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"("IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") values(1313, 'CLIBLOQ7','PORTAFOLIO','PROBLEMA CON CREDIBOLSA','BLO','ACT');
	insert into "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"("IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") values(1313, 'CLIBLOQ8','PORTAFOLIO','CONGELAMIENTO DE FONDOS','BLO','ACT');
commit;


-- TIPOS RELACION. ANALIZAR SI ESTOS HAY QUE EFECTIVAMENTE BORRARLOS!
set schema bscl;
	select * from "BSCL.SocioNegocio.BD::REL.TipoRelacion" where
	"IdTipoEntidadOrigen" in('PERSONA','') and "IdTipoRelacion" not in ('CY','PEMDOR','REP','PARIEN','COTITU','FIDUCI');
	update "BSCL.SocioNegocio.BD::REL.TipoRelacion"
	set "IdEstado"='INA' where "IdTipoEntidadOrigen" in('PERSONA','') and "IdTipoRelacion" not in ('CY','PEMDOR','REP','PARIEN','COTITU','FIDUCI');
	--FALTA VER SI BORRAR LAS OTRAS RELACIONES.
	select * from "BSCL.SocioNegocio.BD::REL.RelacionPersona" where "IdTipoRelacion" not in ('CY','PEMDOR','REP','PARIEN','COTITU','FIDUCI');
commit;

--###########################################
-- RECARTERIZACION MASIVA CLIENTES - ARCHIVO
--###########################################
set schema bscl;
	select * from "BSCL.Plataforma.BD.Vistas::PLA.ConsultarArchivosFormato" where "CodigoArchivo"='RECARTERIZACION_CLIENTES';
	/*
	DELETE FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" where "IdArchivo"=23;
	delete from "BSCL.Plataforma.BD::PLA.Formato" where "IdFormato">=214 and "IdFormato"<=224; 
	delete from "BSCL.Plataforma.BD::PLA.Archivo" where "IdArchivo"=23;
	*/
	insert into "BSCL.Plataforma.BD::PLA.Archivo"("IdArchivo","IdEmpresa","CodigoArchivo","TieneSeparador","SeparadorColumnas","TieneCabecera")
	values(23,1,'RECARTERIZACION_CLIENTES','S',';','S');
	-- FORMATO
	--Tipo persona: N/J/M
	insert into "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales","ForzarMayuscula")
	select 219,'TIPOPERSONA','STRING',0,2,'S','','','N',0,'S' from dummy;
	--Documento identidad
	insert into "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales","ForzarMayuscula")
	select 220,'DOCUMENTOIDENTIDAD','STRING',1,40,'S','','','N',0,'S' from dummy;
	--Codigo de sucursal del cliente
	insert into "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales","ForzarMayuscula")
	select 221,'SUCURSALCLIENTE','STRING',2,40,'N','','','N',0,'S' from dummy;
	--Código ejecutivo del cliente
	insert into "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales","ForzarMayuscula")
	select 222,'EJECUTIVOCLIENTE','STRING',3,40,'N','','','N',0,'S' from dummy;
	--Codigo del portafolio
	insert into "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales","ForzarMayuscula")
	select 223,'CODIGOPORTAFOLIO','STRING',4,100,'N','','','N',0,'S' from dummy;
	--Código ejecutivo del portafolio
	insert into "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales","ForzarMayuscula")
	select 224,'EJECUTIVOPORTAFOLIO','STRING',5,40,'N','','','N',0,'S' from dummy;
	--ArchivoFormato (mapeo)
	insert into "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato")
	values(229,23,219);
	insert into "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato")
	values(230,23,220);
	insert into "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato")
	values(231,23,221);
	insert into "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato")
	values(232,23,222);
	insert into "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato")
	values(233,23,223);
	insert into "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato")
	values(234,23,224);
	--rollback;
commit;

-- INSCRIBIR SERVICIO CambioEstadoMasivoClientesPortafolios
set schema bscl;
	select * from "BSCL.Plataforma.BD::PLA.Servicio" where "IdServicio" = 20020;
	INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
	VALUES(20020,'PLUGIN_SOC.jar@cl.bcs.socionegocio.blo.CambioEstadoMasivoClientesPortafolios',0,'J','','','');
commit;

/*************************
 CREACION CLIENTES NOMINA
**************************/
set schema bscl;
	--select * from "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" where "IdServicio"=3592;-- order by "IdServicio";
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT") 
	values (3592,'BSCL.SocioNegocio.BD.Servicios::CLI.CrearClienteNominaPE' , 1,  'P', '','','');
	--rollback;
commit;

-- CONFIGURACION DE ARCHIVO CARGA CLIENTES: ARCHIVO_CLIENTE_PE
set schema bscl;
	--delete from "BSCL"."BSCL.Plataforma.BD::PLA.Archivo" where "CodigoArchivo" = 'ARCHIVO_CLIENTE';
	/* DELETE FROM "BSCL"."BSCL.Plataforma.BD::PLA.Formato"
	WHERE "IdFormato" between 5 and 33;
	DELETE FROM "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
	WHERE "IdFormato" between 5 and 33;*/
	select * from "BSCL"."BSCL.Plataforma.BD::PLA.Archivo" order by "IdArchivo" desc;
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.Archivo"("IdArchivo","IdEmpresa","CodigoArchivo","TieneSeparador","SeparadorColumnas","TieneCabecera")
	values(26,1,'ARCHIVO_CLIENTE_PE','S',';','S');
	update "BSCL"."BSCL.Plataforma.BD::PLA.Archivo" set "IdEmpresa" = 1313 where "IdArchivo"=26;

	--select * from "BSCL"."BSCL.Plataforma.BD::PLA.Formato" where "IdFormato" between 9 and 37 order by "IdFormato";
	select * from "BSCL.Plataforma.BD::PLA.Formato" where "IdFormato" between 9 and 37 or "IdFormato" in(225,226,227) order by "IdFormato"
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato"
	("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion")--,"CantidadDecimales","IncluyeDecimales","ForzarMayuscula") 
	select 225,'CODIGORUT','string',0,0,'S','','' from dummy union all
	select 226,'BROKER','string',0,0,'S','','' from dummy union all
	select 227,'DISTRITO','string',0,0,'S','','' from dummy;	
	--select * from "BSCL.Plataforma.BD::PLA.Formato" where "IdFormato" in(225,226,227) order by "IdFormato";
	/*
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(9,'TIPODOCUMENTO','string',0,0,'S','','');
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(10,'DOCUMENTOIDENTIDAD','string',0,0,'S','','');
	//225,'CODIGORUT'
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(11,'TIPOPERSONA','string',0,0,'S','','');
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(12,'NOMBRES','string',0,0,'S','','');
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(13,'APELLIDOPATERNO','string',0,0,'S','','');
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(14,'APELLIDOMATERNO','string',0,0,'S','','');
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(15,'PAISNACIMIENTO','string',0,0,'S','','');
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(16,'FECHANACIMIENTO','string',0,0,'S','','');
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(17,'EDOCIVIL','string',0,0,'S','','');
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(18,'GENERO','string',0,0,'S','','');
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(19,'CALLE','string',0,0,'S','','');
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(20,'PAISRESIDENCIA','string',0,0,'S','','');
	227,'DISTRITO'
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(21,'NUMERO','string',0,0,'S','','');
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(22,'TELEFONO','string',0,0,'S','','');
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(23,'EMAIL','string',0,0,'S','','');
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(24,'CATINV','string',0,0,'S','','');
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(25,'PERCOMER','string',0,0,'S','','');
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(32,'RAZONSOCIAL','string',0,0,'S','','');
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(33,'TIPOCLIENTE','STRING',0,0,'S','','');
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(34,'COMPLEMENTO','STRING',0,0,'S','','');
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(35,'TIPOTELEFONO','STRING',0,0,'S','','');
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(37,'CIUDAD','STRING',0,0,'S','','');
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(100,'EJECUTIVO','STRING',0,0,'S','','');
	*/
	select * from "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" where "IdArchivoFormato" between 300 and 324;
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(300 , 26 , 9  );--  9,'TIPODOCUMENTO',
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(301 , 26 , 10 );-- 10,'DOCUMENTOIDENTIDAD'
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(302 , 26 , 225);--225,'CODIGORUT'
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(303 , 26 , 11 );-- 11,'TIPOPERSONA'
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(304 , 26 , 32 );-- 32,'RAZONSOCIAL'
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(305 , 26 , 12 );-- 12,'NOMBRES',
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(306 , 26 , 13 );-- 13,'APELLIDOPATERNO'
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(307 , 26 , 14 );-- 14,'APELLIDOMATERNO'
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(308 , 26 , 15 );-- 15,'PAISNACIMIENTO'
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(309 , 26 , 16 );-- 16,'FECHANACIMIENTO'
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(310 , 26 , 17 );-- 17,'EDOCIVIL'
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(311 , 26 , 18 );-- 18,'GENERO'
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(312 , 26 , 20 );-- 20,'PAISRESIDENCIA'
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(313 , 26 , 227);--227,'DISTRITO'
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(314 , 26 , 19 );-- 19,'CALLE',
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(315 , 26 , 21 );-- 21,'NUMERO'
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(316 , 26 , 34 );-- 34,'COMPLEMENTO'
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(317 , 26 , 35 );-- 35,'TIPOTELEFONO'
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(318 , 26 , 22 );-- 22,'TELEFONO'
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(319 , 26 , 23 );-- 23,'EMAIL'
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(320 , 26 , 24 );-- 24,'CATINV'
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(321 , 26 , 25 );-- 25,'PERCOMER',
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(322 , 26 , 33 );-- 33,'TIPOCLIENTE',
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(323 , 26 , 100);--100,'EJECUTIVO'
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(324 , 26 , 226);--226,'BROKER'

	select * from "BSCL.Plataforma.BD.Vistas::PLA.ConsultarArchivosFormato" where "CodigoArchivo" = 'ARCHIVO_CLIENTE_PE';
commit;


/*****************************************************************************************************************
.______    __          ___   .___________.    ___       _______   ______   .______      .___  ___.      ___      
|   _  \  |  |        /   \  |           |   /   \     |   ____| /  __  \  |   _  \     |   \/   |     /   \     
|  |_)  | |  |       /  ^  \ `---|  |----`  /  ^  \    |  |__   |  |  |  | |  |_)  |    |  \  /  |    /  ^  \    
|   ___/  |  |      /  /_\  \    |  |      /  /_\  \   |   __|  |  |  |  | |      /     |  |\/|  |   /  /_\  \   
|  |      |  `----./  _____  \   |  |     /  _____  \  |  |     |  `--'  | |  |\  \----.|  |  |  |  /  _____  \  
| _|      |_______/__/     \__\  |__|    /__/     \__\ |__|      \______/  | _| `._____||__|  |__| /__/     \__\ 
                                                                                                                 
******************************************************************************************************************/
-- LOCALIZACION
set schema bscl;
	-- AMBIENTE DESA
	update "BSCL.Plataforma.BD::PLA.Configuracion" set "Valor" = 'PE' 
	where "IdEmpresa" = 1313 and "CodigoParametro" in ('PLA.IdiomaSistema','PLA.LocalizacionSistema');
	
	-- AMBIENTE QA
	update "BSCL.Plataforma.BD::PLA.Configuracion" set "Valor" = 'PE' 
	where "IdEmpresa" = 1 and "CodigoParametro" in ('PLA.IdiomaSistema','PLA.LocalizacionSistema');
commit;

-- GRADO DE INSTRUCCION
set schema bscl;
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT") 
	values (3501,'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarGradoInstruccion' , 1,  'Q', '','','');
	
	select "IdGradoInstruccion","Codigo","Nombre","IdPais"from "BSCL.SocioNegocio.BD::PER.GradoInstruccion";
	insert into "BSCL.SocioNegocio.BD::PER.GradoInstruccion"("IdGradoInstruccion","Codigo","Nombre","IdPais") values(1,'PRIMARIA','PRIMARIA',166);
	insert into "BSCL.SocioNegocio.BD::PER.GradoInstruccion"("IdGradoInstruccion","Codigo","Nombre","IdPais") values(2,'SECUNDARIA','SECUNDARIA',166);
	insert into "BSCL.SocioNegocio.BD::PER.GradoInstruccion"("IdGradoInstruccion","Codigo","Nombre","IdPais") values(3,'SUPERIOR','SUPERIOR',166);
	insert into "BSCL.SocioNegocio.BD::PER.GradoInstruccion"("IdGradoInstruccion","Codigo","Nombre","IdPais") values(4,'TÉCNICO','TÉCNICO',166);
	select "IdGradoInstruccion","Codigo","Nombre","IdPais"from "BSCL.SocioNegocio.BD::PER.GradoInstruccion";
commit;

--SUCURSALES PERU (A partir de excel enviado)
set schema bscl;
	delete from "BSCL.Plataforma.BD::PLA.Sucursal";
	INSERT INTO "BSCL.Plataforma.BD::PLA.Sucursal" ( "IdSucursal","IdEmpresa","CodigoSucursal","NombreSucursal","EstadoSucursal" )  VALUES(166001,1,'MESASOFISTICADOS','Mesa sofisticados ','VIG');
	INSERT INTO "BSCL.Plataforma.BD::PLA.Sucursal" ( "IdSucursal","IdEmpresa","CodigoSucursal","NombreSucursal","EstadoSucursal" )  VALUES(166002,1,'MESAESTRUCTURALES','Mesa estructurales ','VIG');
	INSERT INTO "BSCL.Plataforma.BD::PLA.Sucursal" ( "IdSucursal","IdEmpresa","CodigoSucursal","NombreSucursal","EstadoSucursal" )  VALUES(166003,1,'CRONOS','Cronos ','VIG');
	INSERT INTO "BSCL.Plataforma.BD::PLA.Sucursal" ( "IdSucursal","IdEmpresa","CodigoSucursal","NombreSucursal","EstadoSucursal" )  VALUES(166004,1,'SANISIDRO','San Isidro ','VIG');
	INSERT INTO "BSCL.Plataforma.BD::PLA.Sucursal" ( "IdSucursal","IdEmpresa","CodigoSucursal","NombreSucursal","EstadoSucursal" )  VALUES(166005,1,'MIRAFLORES','Miraflores','VIG');
	INSERT INTO "BSCL.Plataforma.BD::PLA.Sucursal" ( "IdSucursal","IdEmpresa","CodigoSucursal","NombreSucursal","EstadoSucursal" )  VALUES(166006,1,'PIURA','Piura ','VIG');
	INSERT INTO "BSCL.Plataforma.BD::PLA.Sucursal" ( "IdSucursal","IdEmpresa","CodigoSucursal","NombreSucursal","EstadoSucursal" )  VALUES(166007,1,'TRUJILLO','Trujillo ','VIG');
	INSERT INTO "BSCL.Plataforma.BD::PLA.Sucursal" ( "IdSucursal","IdEmpresa","CodigoSucursal","NombreSucursal","EstadoSucursal" )  VALUES(166008,1,'AREQUIPA','Arequipa ','VIG');
	INSERT INTO "BSCL.Plataforma.BD::PLA.Sucursal" ( "IdSucursal","IdEmpresa","CodigoSucursal","NombreSucursal","EstadoSucursal" )  VALUES(166009,1,'ILO','Ilo','VIG');
	INSERT INTO "BSCL.Plataforma.BD::PLA.Sucursal" ( "IdSucursal","IdEmpresa","CodigoSucursal","NombreSucursal","EstadoSucursal" )  VALUES(166010,1,'OTROS','Otros ','VIG');
	INSERT INTO "BSCL.Plataforma.BD::PLA.Sucursal" ( "IdSucursal","IdEmpresa","CodigoSucursal","NombreSucursal","EstadoSucursal" )  VALUES(166011,1,'CANALWEB','Canal web','VIG');
commit;

-- GIROS:
-- Script independiente (ubicado en Plataforma)

-- CATEGORIA INVERSIONISTA (Falta Filtro
set schema bscl;
	select * from "BSCL.Plataforma.BD::COM.CategoriaInversionista";
	delete from "BSCL.Plataforma.BD::COM.CategoriaInversionista" where "IdEmpresa"=1;
	insert into "BSCL.Plataforma.BD::COM.CategoriaInversionista"("IdCategoriaInversionista","NombreCategoriaInversionista","IdEmpresa","Jerarquia")values('NOAPLICA','No Aplica',1,4);
	insert into "BSCL.Plataforma.BD::COM.CategoriaInversionista"("IdCategoriaInversionista","NombreCategoriaInversionista","IdEmpresa","Jerarquia")values('CONSERVADOR','CONSERVADOR',1,4);
	insert into "BSCL.Plataforma.BD::COM.CategoriaInversionista"("IdCategoriaInversionista","NombreCategoriaInversionista","IdEmpresa","Jerarquia")values('CONSERVADOR MODERADO','CONSERVADOR MODERADO',1,4);
	insert into "BSCL.Plataforma.BD::COM.CategoriaInversionista"("IdCategoriaInversionista","NombreCategoriaInversionista","IdEmpresa","Jerarquia")values('MODERADO','MODERADO',1,4);
	insert into "BSCL.Plataforma.BD::COM.CategoriaInversionista"("IdCategoriaInversionista","NombreCategoriaInversionista","IdEmpresa","Jerarquia")values('MODERADO AGRESIVO','MODERADO AGRESIVO',1,4);
	insert into "BSCL.Plataforma.BD::COM.CategoriaInversionista"("IdCategoriaInversionista","NombreCategoriaInversionista","IdEmpresa","Jerarquia")values('AGRESIVO','AGRESIVO',1,4);
	--update "BSCL.SocioNegocio.BD::CLI.Cliente" set "IdCategoriaInversionista"='CONSERVADOR';
commit;

--CORRECCION BANCOS (FALTA ANALIZAR EL MODELO)
set schema bscl;
	--select * from "BSCL.Plataforma.BD::BAN.Banco";
	update "BSCL.Plataforma.BD::BAN.Banco" set "IdTipoBanco"='EXT' where "IdPaisOperacion"!=166;
commit;

--PROFESIONES
-- archivo con script.

--RECARTERIZACION CLIENTES
set schema bscl;
	INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
	VALUES(20016,'PLUGIN_SOC.jar@cl.bcs.socionegocio.eje.RecarterizacionClientes',0,'J','','','');
commit;

--CUADRE DIVISION PAIS (ELIMINAR LOS QUE NO TIENEN CUIDAD, BASURA)
set schema bscl;
	update dp
		set dp."IdCiudad"=ciu."IdCiudad", 
		dp."IdClasificacion1"=cdp2."IdClasificacionDivisionPais",
		dp."IdClasificacion2"=cdp."IdClasificacionDivisionPais"
	from "BSCL.Plataforma.BD::REG.DivisionPais" dp
		join "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais" cdp 
			on (substring(dp."Codigo",0,2) = cdp."Codigo" and dp."IdPais"=cdp."IdPais" and cdp."IdPais"=166)
		join "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais" cdp2 
			on (substring(dp."Codigo",0,4) = cdp2."Codigo" and dp."IdPais"=cdp2."IdPais" and cdp2."IdPais"=166)
		join "BSCL.Plataforma.BD::REG.Ciudad" ciu
			on (ciu."NombreCiudad"=cdp."NombreClasificacionDivisionPais" and cdp."IdPais"=ciu."IdPais" and ciu."IdPais"=166);
	where cdp."TipoClasificacionDivisionPais"='DEPARTAMENTO'
		and cdp2."TipoClasificacionDivisionPais"='PROVINCIA';
		
	delete from "BSCL.Plataforma.BD::REG.DivisionPais" where "IdClasificacion1"=0;
commit;

-- INSCRIPCION CONSULTAS CAMBIO EJECUTIVO
set schema bscl;
	INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
		VALUES(3565,'BSCL.Portafolio.BD.Vistas::POR.ConsultarLogCambioEjecutivoPortafolio',0,'Q','','','');
	INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
		VALUES(3566,'BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarLogCambioEjecutivoCliente',0,'Q','','','');
commit;

-- MONITOR DE LA PLATAFORMA
set schema bscl;
	INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" ("IdTipoPrograma","Descripcion","MaximoIntentos","Modulo","NombreClasePrograma","TipoClasePrograma","Nodo","EsPauta")
	VALUES('MONITOR_PLATAFORMA', 'Monitor de plataforma', 1, 'PLATAFORMA', 'cl.bcs.plataforma.jobs.MonitorPlataformaJob', 'JAVA', '','N');
	
	drop table #parametros;
	create local temporary table #parametros("NombreParametro" nvarchar(100), "Valor" nvarchar(100));
	INSERT INTO #parametros Select 'IdEmpresa' as "NombreParametro", '1' as "Valor" from dummy;
	INSERT INTO #parametros Select 'NotificarAdministradoresTI' as "NombreParametro", 'F' as "Valor" from dummy;
	CALL "BSCL.Plataforma.BD.Servicios::BAT.AgregarAgenda"(
		p_IdEmpresa  =>  1,
		p_IdUsuario  =>  37,
		p_IdModulo   =>  'PLA',
		p_IdTipoTarea  =>  'MONITOR_PLATAFORMA',
		p_EsDemonio    =>  'N'  ,
		p_DiaDeMes    =>  '**',
		p_DiaDeSemana  =>  '*',
		p_Mes      =>  '**',
		p_Hora      =>  '**',
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

-- CONFIGURACION ASUNTO EMAIL ADMINISTRACION
set schema bscl;
	insert into "BSCL.Plataforma.BD::PLA.Configuracion" 
	values (1,'EVT.AsuntoEmailAdministracion','Asunto utilizado en correo administración','NOTIFICACION ADMINISTRADOR (<AMBIENTE>)');
commit;

-- VALOR DEFECTO SISTEMA
set schema bscl;
	delete from "BSCL.Plataforma.BD::PLA.ValorDefectoSistema" where "IdValorDefectoSistema" = 'UsaCodigoPortafolioDefault';
	set schema bscl;
	insert into "BSCL.Plataforma.BD::PLA.ValorDefectoSistema"
	("IdValorDefectoSistema","Nombre","Valor","IdEmpresa","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEstado")
	values('UsaCodigoPortafolioDefault','Utiliza el Id del Portafolio como código y no permite su posterior edición','N',1,1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,'ACT');
	
	insert into "BSCL.Plataforma.BD::PLA.ValorDefectoSistema"
	("IdValorDefectoSistema","Nombre","Valor","IdEmpresa","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEstado")
	values('UsaCodigoPortafolioDefault','Utiliza el Id del Portafolio como código y no permite su posterior edición','S',1313,1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,'ACT');
	select * from "BSCL.Plataforma.BD::PLA.ValorDefectoSistema" where "IdValorDefectoSistema" = 'UsaCodigoPortafolioDefault';
commit;

-- MODIFICACION SERVICIOS AUTORIZACION
set schema bscl;	
	--select * from "BSCL.Plataforma.BD::AUT.TipoEvento" where "IdTipoEvento" in ('ENROL','ENRPER','NUEPOR') and "IdEmpresa" = 1;
	update "BSCL.Plataforma.BD::AUT.TipoEvento" set "HorasVigencia" = 24 where "IdTipoEvento" in ('ENROL','ENRPER','NUEPOR'); --and "IdEmpresa" = 1;
	--select "SPConfirma",* from "BSCL.Plataforma.BD::AUT.TipoEvento" where "IdTipoEvento" in ('ENROL','ENRPER','NUEPOR') and "IdEmpresa" = 1;
	update "BSCL.Plataforma.BD::AUT.TipoEvento" set "SPFormato" = 8 where "IdTipoEvento" in('ENROL');-- and "IdEmpresa" = 1;	
commit;

-- INSCRIPCION JAVA VALORIZAR
set schema bscl;
	--select * from "BSCL.Plataforma.BD::PLA.Servicio" where "IdServicio"=20033;
	INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
	VALUES(20033,'PLUGIN_PLA.jar@cl.bcs.plataforma.val.Valorizar',0,'J','','','');
commit;

/***************************
 DETALLE COBRO PERSONALIZADO
 ***************************/
set schema bscl;
	alter table  "BSCL.Plataforma.BD::COM.ValorCobro" add("EsPersonalizado" NVARCHAR(1) not null default 'N');
commit;

-- ORDEN Moneda para visualización en combobox.
set schema bscl;
	--ORDEN CL:
	update "BSCL.Plataforma.BD::PLA.Moneda" set "Orden" = 0 where "IdMoneda" = 'CLP';
	update "BSCL.Plataforma.BD::PLA.Moneda" set "Orden" = 1 where "IdMoneda" = 'UF';
	update "BSCL.Plataforma.BD::PLA.Moneda" set "Orden" = 2 where "IdMoneda" = 'USD';
	update "BSCL.Plataforma.BD::PLA.Moneda" set "Orden" = 3 where "IdMoneda" = 'EUR';
	update "BSCL.Plataforma.BD::PLA.Moneda" set "Orden" = 4 where "IdMoneda" = 'PEN';
	--ORDEN PE:
	update "BSCL.Plataforma.BD::PLA.Moneda" set "Orden" = 0 where "IdMoneda" = 'PEN';
	update "BSCL.Plataforma.BD::PLA.Moneda" set "Orden" = 1 where "IdMoneda" = 'USD';
	update "BSCL.Plataforma.BD::PLA.Moneda" set "Orden" = 2 where "IdMoneda" = 'EUR';
	update "BSCL.Plataforma.BD::PLA.Moneda" set "Orden" = 3 where "IdMoneda" = 'CLP';
	update "BSCL.Plataforma.BD::PLA.Moneda" set "Orden" = 4 where "IdMoneda" = 'UF';
commit;

/***************************
 REPROCESAR EN PAUTA
 ***************************/
set schema bscl;
	--select * from "BSCL.Plataforma.BD::PLA.Servicio" where "IdServicio">=20051 order by "IdServicio";
	INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
	VALUES(20051,'PLUGIN_PLA.jar@cl.bcs.plataforma.bat.ReprocesarPrograma',0,'J','','','');		
commit;


-- CONFIGURACION ARCHIVOS PROPERTIES PARA SER USADOS EN LOS JAR.
set schema bscl;
	delete from "BSCL.Plataforma.BD::PLA.Configuracion"  where "CodigoParametro" = 'PLA.RutaArchivosConfiguracion';
	
	insert into "BSCL.Plataforma.BD::PLA.Configuracion" 
	values (1,'PLA.RutaArchivosConfiguracion','Ruta archivos de configuracion estaticos usados por Optimus para invocar servicios externos','/home/optimusDES/jobm/','S');
	
	insert into "BSCL.Plataforma.BD::PLA.Configuracion" 
	values (1313,'PLA.RutaArchivosConfiguracion','Ruta archivos de configuracion estaticos usados por Optimus para invocar servicios externos','/home/optimusDES/jobm/','S');
commit;

/***********************************************************************************************************************
   ____  _____  _____  ______ _   _ ______  _____     _____  ______ _   _ _______           ______ _____     _         
  / __ \|  __ \|  __ \|  ____| \ | |  ____|/ ____|   |  __ \|  ____| \ | |__   __|/\       |  ____|_   _|   | |  /\    
 | |  | | |__) | |  | | |__  |  \| | |__  | (___     | |__) | |__  |  \| |  | |  /  \      | |__    | |     | | /  \   
 | |  | |  _  /| |  | |  __| | . ` |  __|  \___ \    |  _  /|  __| | . ` |  | | / /\ \     |  __|   | | _   | |/ /\ \  
 | |__| | | \ \| |__| | |____| |\  | |____ ____) |   | | \ \| |____| |\  |  | |/ ____ \    | |     _| || |__| / ____ \ 
  \____/|_|  \_\_____/|______|_| \_|______|_____/    |_|  \_\______|_| \_|  |_/_/    \_\   |_|    |_____\____/_/    \_\
************************************************************************************************************************/

-- CONFIGURACION MONEDA FORMATO
set schema bscl;
	select * from "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" where "IdTipoInstrumento" = 'BON';
	--LIMPIAR CONFIG BONO INTERNACIONAL
	delete from "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" where "IdTipoInstrumento" = 'BON';
	--SOL PERUANO|BONO INTERNACIONAL
	INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") 
	VALUES (400,'PEN','ORD','BON','COMRF',4,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
	INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") 
	VALUES (401,'PEN','ORD','BON','VENRF',4,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
	INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") 
	VALUES (402,'PEN','GEN','BON','COMRF',4,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
	INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") 
	VALUES (403,'PEN','GEN','BON','VENRF',4,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
	INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") 
	VALUES (404,'PEN','ORD','BON','',4,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
	INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") 
	VALUES (405,'PEN','GEN','BON','',4,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
	--PESO CHILENO|BONO INTERNACIONAL
	INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") 
	VALUES (406,'CLP','ORD','BON','COMRF',4,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
	INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") 
	VALUES (407,'CLP','ORD','BON','VENRF',4,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
	INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") 
	VALUES (408,'CLP','GEN','BON','COMRF',4,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
	INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") 
	VALUES (409,'CLP','GEN','BON','VENRF',4,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
	INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") 
	VALUES (410,'CLP','ORD','BON','',4,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
	INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") 
	VALUES (411,'CLP','GEN','BON','',4,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
commit;

-- CONFIGURACION BOLSA TIPO INSTRUMENTO
set schema bscl;
	select * from "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" where "IdTipoInstrumento" = 'BON';
	select max("IdBolsaTipoInstrumento") from "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" where "IdBolsaTipoInstrumento"<60;
	select * from "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" where "IdTipoInstrumento" = 'BON';
	INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
	("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdEstado" )  
	VALUES(58,'BON','BCS','','N','ACT');
	INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
	("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdEstado" )  
	VALUES(59,'BON','BVL','','N','ACT');
	INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
	("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdEstado" )  
	VALUES(60,'BON','BEC','','N','ACT');
	INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
	("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdEstado" )  
	VALUES(61,'BON','BCV','','N','ACT');
commit;

--CONFIGURACION CANALES DE DISTRIBUCIÓN
set schema bscl;
	select * from "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion" where "IdTipoInstrumento" = 'BON';
	--delete from "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion" where "IdEmpresa" = 1313;
	
	insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
	("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa",
	"GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
	values (1,'MAN','AGE','BON','BCS','N','N',0);
	
	insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
	("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa",
	"GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
	values (1313,'MAN','AGE','BON','BCS','N','N',0);
	
	insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
	("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa",
	"GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
	values (1,'MAN','AGE','BON','BVL','N','N',0);
	
	insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
	("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa",
	"GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
	values (1313,'MAN','AGE','BON','BVL','N','N',0);
	
	select * from "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion" where "IdTipoInstrumento" = 'BON';
commit;

-- CONFIGURACION TIPO OPERACION TIPO INSTRUMENTO
set schema bscl;
	select * from "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" where "IdTipoOperacion" in ('COMIRF','VENIRF') and "IdTipoInstrumento" = 'BON';

	insert into "BSCL"."BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento"
	("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
	select max("IdTipoOperacionTipoInstrumento")+1,'COMIRF','BON','ACT',0,'' from "BSCL"."BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" union
	select max("IdTipoOperacionTipoInstrumento")+2,'VENIRF','BON','ACT',0,'' from "BSCL"."BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento";

	select * from "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" where "IdTipoOperacion" in ('COMIRF','VENIRF') and "IdTipoInstrumento" = 'BON';
commit;

-- CONFIGURACION TIPOS DE MONEDA LIQUIDACION.
set schema bscl;
	select * from "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" where "IdTipoInstrumento" = 'BON';
	delete from "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" where "IdTipoInstrumento" = 'BON';
	
	insert into "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion"
	("IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion","EsPorDefecto")
	select max("IdTipoInstrumentoMonedaLiquidacion")+1,'BON','PEN','PEN','N' from "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoInstrumentoMonedaLiquidacion";
	
	insert into "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion"
	("IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion","EsPorDefecto")
	select max("IdTipoInstrumentoMonedaLiquidacion")+1,'BON','PEN','USD','N' from "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoInstrumentoMonedaLiquidacion";
	
	--insert into "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion"
	--("IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion","EsPorDefecto")
	--select max("IdTipoInstrumentoMonedaLiquidacion")+1,'BON','PEN','CLP','N' from "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoInstrumentoMonedaLiquidacion";
	
	select * from "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" where "IdTipoInstrumento" = 'BON';
commit;

-- CONFIGURACION TIPO OPERACION Y CONDICION DE LIQUIDACION
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


--INSCRIBIR SERVICIO ConsultarSistemaBursatil:
set schema bscl;
	insert into "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" 
	("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT") 
	values (3330,'BSCL.Orden.BD.Vistas::ConsultarSistemaBursatil',1,'Q','','','');

	select * from "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" where "IdServicio"=3330;
	
	--aplicar para PERU QA:
	update "BSCL.Orden.BD.Vistas::ConsultarSistemaBursatil" set "IdEmpresa" = 1 where "IdEmpresa" = 1313;
commit;

-- AGREGAR TIPO DE REAJUSTE A BONOS INTERNACIONALES (SE CONVERSÓ CON FELIX QUIEN USARÁ COLUMNA NUEVA PARA VALORIZACION)
set schema bscl;
	select "IdTipoReajuste",* from "BSCL.Plataforma.BD::INS.Instrumento" where "IdTipoInstrumento" = 'BON';
	update "BSCL.Plataforma.BD::INS.Instrumento" 
	set "IdTipoReajuste" = "IdMoneda"
	where "IdTipoInstrumento" = 'BON';
commit;

--CONFIGURAR SOL PERUANO EN TABLA TIPO REAJUSTE
set schema bscl;
	select * from "BSCL.Plataforma.BD::PLA.TipoReajuste" where "IdTipoReajuste" = 'PEN';
	
	insert into "BSCL.Plataforma.BD::PLA.TipoReajuste"("IdTipoReajuste","Codigo","Nombre","Reajustable","IdEstado")
	values('PEN','PEN','NUEVO SOL PERUANO','N','ACT');
	
	select * from "BSCL.Plataforma.BD::PLA.TipoReajuste" where "IdTipoReajuste" = 'PEN';
commit;

-- AGREGAR FILTRO MANUAL EN VENTANA DE ORDENES POR NEGOCIAR.
set schema bscl;
	select * from "BSCL.Plataforma.BD::PLA.ConfiguracionFiltroInterfaz" 
	where "Controlador" = 'OrdenesPorNegociarCtrl' and "NombreObjeto" = 'combobox-clasificacion-tipo-instrumento';
	/*IdConfiguracionFiltroInterfaz;Controlador;TipoObjeto;NombreObjeto;IdEstado;Filtros
	16;OrdenesPorNegociarCtrl;ComboBox;combobox-clasificacion-tipo-instrumento;ACT;{"logic": "or",
	    			"filters": [
	    			            {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": "RV"},
	    			            {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": "DV"}
	    		    ]} 
	*/
	
	delete from "BSCL.Plataforma.BD::PLA.ConfiguracionFiltroInterfaz" 
	where "Controlador" = 'OrdenesPorNegociarCtrl' and "NombreObjeto" = 'combobox-clasificacion-tipo-instrumento';
commit;

--CORREGIR INSTRUMENTO BONO.
set schema bscl;
	select * from "BSCL.Plataforma.BD::INS.InstrumentoBono";
	update "BSCL.Plataforma.BD::INS.InstrumentoBono" set "ConvencionDias" = replace("ConvencionDias",'-','/');
	select * from "BSCL.Plataforma.BD::INS.InstrumentoBono";
commit;

--REPLICAR CONFIGURACION DE MOVIMIENTO CUSTODIA A EMPRESA 1313(DESA)
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
