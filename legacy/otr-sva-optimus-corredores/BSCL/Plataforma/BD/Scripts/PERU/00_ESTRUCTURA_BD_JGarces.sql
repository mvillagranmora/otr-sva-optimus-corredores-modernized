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

-- MODELO BLOQUEO AUTOMATICO DE CLIENTES
set schema bscl;
	drop table "BSCL.SocioNegocio.BD::CLI.BloqueoAutomaticoClientes";
	drop table "BSCL.SocioNegocio.BD::CLI.DetalleBloqueoAutomaticoClientes";
	
	create table "BSCL.SocioNegocio.BD::CLI.BloqueoAutomaticoClientes"(
		--"BloqueoAutomaticoClientes" 				bigint not null,
		"IdEmpresa"									bigint not null,
		"IdEstado" 				 					nvarchar(3) default 'ACT' not null,
		"BloqueoCumplirMayoriaEdad"					bigint not null default 0, --Años(edad)
		"BloqueoFichaDesactualizada"				bigint not null default 0, --Meses
		"BloqueoPortafolioSinOperaciones"			bigint not null default 0, --Meses
	    "FechaCreacion"     						seconddate default current_utctimestamp not null,
	   	"FechaModificacion"  						seconddate default '0001-01-01 00:00:00' not null,
	   	--primary key ("IdConfiguracionBloqueoAutomaticoClientes")
	   	primary key ("IdEmpresa")
	);
	
	--alter table "BSCL.SocioNegocio.BD::CLI.BloqueoAutomaticoClientes"
	--alter("FechaCreacion" seconddate default current_utctimestamp not null);
	
	create table "BSCL.SocioNegocio.BD::CLI.DetalleBloqueoAutomaticoClientes"(
		"IdEmpresa" 								bigint not null,
		"IdTipoDocumento" 							nvarchar(40) not null,
		"IdEstado" 				 					nvarchar(3) default 'INA' not null,
	   primary key ("IdEmpresa","IdTipoDocumento")
	);
	-- CREAR VISTA: "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarBloqueoAutomaticoClientes";
--end;

--BLOQUEOS/DESBLOQUEO TEMPORAL
set schema bscl;
	alter table "BSCL.SocioNegocio.BD::CLI.Cliente"
	add("FechaBloqueoTemporal" SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL);
	
	alter table "BSCL.SocioNegocio.BD::CLI.Cliente"
	add("FechaDesbloqueoTemporal" SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL);
--end;

-- AGREGA PK A TIPO CLIENTE
set schema bscl;
	drop table "BSCL.SocioNegocio.BD::CLI.TipoClienteRESPALDO";
	CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::CLI.TipoClienteRESPALDO" (
		 "IdTipoCliente" 			NVARCHAR(40)	NOT NULL ,
		 "Nombre"					NVARCHAR(100)	NOT NULL DEFAULT '',
		 "IdUsuarioCreador"			BIGINT			NOT NULL DEFAULT 0,
		 "FechaCreacion" 			SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',
		 "FechaModificacion" 		SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',  
		 "IdEmpresa" 				BIGINT  		NOT NULL,
		 "IdEstado" 				NVARCHAR(3) 	DEFAULT 'ACT' NOT NULL
	);
	insert into "BSCL.SocioNegocio.BD::CLI.TipoClienteRESPALDO" select * from "BSCL.SocioNegocio.BD::CLI.TipoCliente";
	commit;
	select * from "BSCL.SocioNegocio.BD::CLI.TipoClienteRESPALDO";

	 drop table "BSCL.SocioNegocio.BD::CLI.TipoCliente";
	 CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::CLI.TipoCliente" (
		 "IdTipoCliente" 			NVARCHAR(40)	NOT NULL ,
		 "Nombre"					NVARCHAR(100)	NOT NULL DEFAULT '',
		 "IdUsuarioCreador"			BIGINT			NOT NULL DEFAULT 0,
		 "FechaCreacion" 			SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',
		 "FechaModificacion" 		SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',  
		 "IdEmpresa" 				BIGINT  		NOT NULL,
		 "IdEstado" 				NVARCHAR(3) 	DEFAULT 'ACT' NOT NULL,
		 primary key("IdTipoCliente","IdEmpresa")
	);
	insert into "BSCL.SocioNegocio.BD::CLI.TipoCliente" select * from "BSCL.SocioNegocio.BD::CLI.TipoClienteRESPALDO";
	commit;
	select * from "BSCL.SocioNegocio.BD::CLI.TipoCliente";
	
	drop table "BSCL.SocioNegocio.BD::CLI.TipoClienteRESPALDO"; 
	--select * from "BSCL.SocioNegocio.BD::CLI.TipoClienteRESPALDO";
--end; 

-- TIPO DOCUMENTO IDENTIDAD CAMBIO TAMAÑO ID
set schema bscl;
	alter table "BSCL.Plataforma.BD::REG.TipoDocumentoIdentidad" alter ("IdTipoDocumentoIdentidad" nvarchar(3));
--end;

-- NUEVOS CAMPOS DATOS BASICOS SOCIO NEGOCIO:
set schema bscl;
	alter table "BSCL.SocioNegocio.BD::SOC.Enrolamiento"
	alter ( "IdTipoDocumentoIdentidad" NVARCHAR(3) not null);
	
	alter table "BSCL.SocioNegocio.BD::SOC.Enrolamiento"
	add ( "IdTipoDocumentoIdentidadAdicional" NVARCHAR(3) not null default '');
	
	alter table "BSCL.SocioNegocio.BD::SOC.Enrolamiento"
	add ( "DocumentoIdentidadAdicional" NVARCHAR(40) not null default '');
	
	alter table "BSCL.SocioNegocio.BD::SOC.Enrolamiento"
	add ( "CLI_CodigoAdicionalHolding" NVARCHAR(40) not null default '');
	
	alter table "BSCL.SocioNegocio.BD::CLI.Cliente" 
	add ("CodigoAdicionalHolding"	NVARCHAR(40) NOT NULL DEFAULT '');
	
	alter table "BSCL.SocioNegocio.BD::SOC.Enrolamiento" 
	add ("CLI_IdEmpleadoEjecutivo"	BIGINT NOT NULL DEFAULT 0);
	
	alter table "BSCL.SocioNegocio.BD::PER.Persona" 
	add("IdGradoInstruccion" bigint default 0);
	
	alter table "BSCL.SocioNegocio.BD::PER.Persona" 
	add("AgnosServicio" int default 0);
	
	alter table "BSCL.SocioNegocio.BD::PER.Persona" 
	add("IngresoPromedioMensual" decimal(26,8) default 0);
	
	alter table "BSCL.SocioNegocio.BD::PER.Persona" 
	add("IdDocumentoIdentidadAdicional"	BIGINT);
	
	alter table "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"
	alter( "IdTipoDocumentoIdentidad" NVARCHAR(3) DEFAULT '0' NOT NULL);
	
	alter table "BSCL.Homologacion.BD::HOM.ComprobanteTesoreria" 
	alter( "IdTipoDocumentoIdentidad" NVARCHAR(3) NULL);
	
	alter table "BSCL.Homologacion.BD::HOM.IngresoEgreso" 
	alter( "IdTipoDocumentoIdentidad" NVARCHAR(3) NULL);
	
	alter table "BSCL.Tesoreria.BD::TES.Comprobante" 
	alter( "IdTipoDocumentoIdentidad" NVARCHAR(3) NOT NULL DEFAULT '');
	
	alter table "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearComprobantes_Entrada_IngresoEgresoGenerar" 
	alter( "IdTipoDocumentoIdentidad" NVARCHAR(3) NULL);
	
	alter table "BSCL.Custodia.BD::CUS.EventoCapitalCliente" 
	alter( "IdTipoDocumentoIdentidad" NVARCHAR(3) NOT NULL DEFAULT '');
	
	alter table "BSCL.Tesoreria.BD::TES.IngresoEgreso" 
	alter( "IdTipoDocumentoIdentidad" NVARCHAR(3) NOT NULL DEFAULT '');
	
	alter table "BSCL.Tesoreria.BD::GBL.CrearComprobante_Comprobante" 
	alter( "IdTipoDocumentoIdentidad" NVARCHAR(3) NULL);
	
	alter table "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearComprobantes_Entrada_ComprobanteGenerar" 
	alter( "IdTipoDocumentoIdentidad" NVARCHAR(3) NULL);
	
	alter table "BSCL.SocioNegocio.BD::PER.Persona"
	add("IdPaisResidenciaSecundaria"		BIGINT CS_FIXED); 
	
	alter table "BSCL.SocioNegocio.BD::PER.Persona"
	add("IdPaisNacionalidadSecundaria"		BIGINT CS_FIXED);
	
	alter table "BSCL.SocioNegocio.BD::PER.Persona"
	add("IdGiroCIIU"		BIGINT CS_FIXED); -- OJO; VER SI BORRAR YA QUE ES COLUMNA PERUANIZADA xD
	
	alter table "BSCL.Plataforma.BD::REG.Giro"
	alter("Nombre" NVARCHAR(500) not null default '');
	
	
	alter table "BSCL.SocioNegocio.BD::CLI.Cliente"
	add("IdSucursalPreferencia"	BIGINT 	null);

	alter table "BSCL.SocioNegocio.BD::CLI.Cliente"
	add("IdPersonaBrokerExtranjero"	BIGINT 	null);

	alter table "BSCL.SocioNegocio.BD::CLI.Cliente"
	add("IdClaseCliente" NVARCHAR(40) NOT NULL DEFAULT '');
	
	-- va?
	--alter table "BSCL.SocioNegocio.BD::PER.Persona" add("IdGiroCIIU" bigint default 0);
	
	--IR A BUSCAR ULTIMO TIPO EXISTENTE EN DESARROLLO!
	/*drop type "BSCL.SocioNegocio.BD.Tipos::SOC.ConsultarEnrolamiento";
	CREATE TYPE "BSCL.SocioNegocio.BD.Tipos::SOC.ConsultarEnrolamiento" AS TABLE (
	"IdEnrolamiento"			BIGINT,
	"IdEmpresa" 				BIGINT,
	"IdUsuario" 				BIGINT,
	"IdTipoEntidad" 			NVARCHAR(100),
	"IdEntidadEnrolada" 		BIGINT,
	"IdTipoDocumentoIdentidad"  NVARCHAR(3),
	"DocumentoIdentidad" 		NVARCHAR(40),
	"IdTipoDocumentoIdentidadAdicional" NVARCHAR(3) null default '',
	"DocumentoIdentidadAdicional" NVARCHAR(40) null default '',
	"Tipo" 						NVARCHAR(2),
	"Nombres" 					NVARCHAR(100),
	"ApellidoPaterno" 			NVARCHAR(100),
	"ApellidoMaterno" 			NVARCHAR(100),
	"RazonSocial" 				NVARCHAR(100),
	"IdPaisNacimiento" 			BIGINT,
	"FechaNacimiento" 			DATE,
	"Sexo" 						NVARCHAR(2),
	"EstadoCivil" 				NVARCHAR(2),
	"IdCiudad" 					BIGINT,
	"IdDivisionPais" 			BIGINT,
	"IdTipoTelefono" 			NVARCHAR(40),
	"Telefono" 					NVARCHAR(100),
	"Email" 					NVARCHAR(100),
	"DireccionCalle" 			NVARCHAR(100),
	"DireccionNumero" 			NVARCHAR(40),
	"DireccionDepartamento" 	NVARCHAR(40),
	"FechaCreacion" 			SECONDDATE,
	"FechaModificacion" 		SECONDDATE,
	"IdPersonaConyuge"          BIGINT CS_FIXED  NULL DEFAULT 0,
	"RegimenConyugal"           NVARCHAR(2) NULL DEFAULT '',
	"CLI_IdCategoriaInversionista" 	NVARCHAR(40),
	"CLI_IdTipoCliente"				NVARCHAR(40),
	"CLI_IdEmpleadoEjecutivo"   	BIGINT,
	"CLI_CodigoAdicionalHolding"	NVARCHAR(40) null default '',
	"POR_IdPerfilComercial" 		NVARCHAR(40), 
	"EMP_CodigoEmpleado"			NVARCHAR(40),
	"EMP_CodigoTipoCargoEmpleado"	NVARCHAR(6),
	"EMP_IdEmpleadoSupervisor"		BIGINT,
	"EMP_IdSucursal"				BIGINT,
	"EMP_FechaInicioCargo"			DATE,
	"EMP_FechaTerminoCargo" 		DATE NULL DEFAULT '',
	"CNT_CodigoInscripcion" 		NVARCHAR(40),
	"CNT_Emisor" 					NVARCHAR(1),
	"CNT_Depositos"					NVARCHAR(1),
	"CNT_Bolsa" 					NVARCHAR(1),
	"CNT_Corredora" 				NVARCHAR(1),
	"CNT_CamaraDeCompensacion" 		NVARCHAR(1),
	"CNT_Banco" 					NVARCHAR(1),
	"CNT_Boveda" 					NVARCHAR(1),
	"PRO_IdAreaProveedor"			BIGINT,
	"EMP_CargoIndefinido"			NVARCHAR(1) NULL DEFAULT ''
--,"CLI_IdTipoEnrolamiento"		NVARCHAR(40)
	 ); */

--end;

--GRADO INSTRUCCION
set schema bscl;
	CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::PER.GradoInstruccion" (
		 "IdGradoInstruccion" 	BIGINT,
		 "Codigo" 	 			NVARCHAR(40) NOT NULL DEFAULT '' ,
		 "Nombre" 	 			NVARCHAR(100) NOT NULL DEFAULT '' ,
		 "IdPais"				BIGINT NOT NULL DEFAULT 0,
		 PRIMARY KEY ("IdGradoInstruccion"));
		 
	--la vista
	SET SCHEMA "BSCL";
	DROP VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarGradoInstruccion"; 
	CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarGradoInstruccion" AS
		select "IdGradoInstruccion","Codigo","Nombre","IdPais"	
		from "BSCL.SocioNegocio.BD::PER.GradoInstruccion";--WITH READ ONLY; 
	GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarGradoInstruccion" TO  BCS_DESARROLLADOR WITH GRANT OPTION;	 
--end;

-- CLASE CLIENTE
set schema bscl;
	create column table "BSCL.SocioNegocio.BD::CLI.ClaseCliente"(
	"IdClaseCliente" 			NVARCHAR(40)	NOT NULL ,
	"Nombre"					NVARCHAR(100)	NOT NULL DEFAULT '',
	"IdUsuarioCreador"			BIGINT			NOT NULL DEFAULT 0,
	"FechaCreacion" 			SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',
	"FechaModificacion" 		SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',  
	"IdEmpresa" 				BIGINT  		NOT NULL,
	"IdEstado" 					NVARCHAR(3) 	DEFAULT 'ACT' NOT NULL,
	primary key("IdClaseCliente","IdEmpresa"));
	--agregar campo a cliente/enrolamiento 
	alter table "BSCL.SocioNegocio.BD::CLI.Cliente" add("IdClaseCliente" NVARCHAR(40) NOT NULL DEFAULT '');
	alter table "BSCL.SocioNegocio.BD::SOC.Enrolamiento" add("CLI_IdClaseCliente" NVARCHAR(40) DEFAULT '' NOT NULL);
	
	--RECREAR TIPO "BSCL.SocioNegocio.BD.Tipos::SOC.ConsultarEnrolamiento"; 
--end;

--###########################################
-- BLOQUEO MASIVO CLIENTES - ARCHIVO
--###########################################
set schema bscl;
	insert into "BSCL.Plataforma.BD::PLA.Archivo"("IdArchivo","IdEmpresa","CodigoArchivo","TieneSeparador","SeparadorColumnas","TieneCabecera")
	values(24,1,'ARCHIVO_BLOQUEO_CLIENTES_MASIVO_PE','S',';','S');
	
	update "BSCL.Plataforma.BD::PLA.Archivo" set "IdEmpresa"=1313 where "IdArchivo"=24;
	commit;
	---------- FORMATO ------------
	-- 219 : TIPOPERSONA
	--   9 : TIPODOCUMENTO
	-- 220 : DOCUMENTOIDENTIDAD
	-- 223 : CODIGOPORTAFOLIO
	--  98 : MOTIVOBLOCLI (OPCIONAL)
	--  99 : MOTIVOBLOPOR (OPCIONAL)
	--------------------------------
	--ArchivoFormato (mapeo)
	insert into "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato") values(235,24,219); 
	insert into "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato") values(236,24,9);
	insert into "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato") values(237,24,220);
	insert into "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato") values(238,24,223);
	
	insert into "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato") values(239,24,98);
	insert into "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato") values(240,24,99);
	commit;
	
	----archivo no recibirá el motivo de bloqueo (se inyectará al servicio a partir de la UI:
	--set schema bscl;
	--delete from "BSCL.Plataforma.BD::PLA.ArchivoFormato" where "IdArchivoFormato" in (239,240);
	--commit;

	select * from "BSCL.Plataforma.BD.Vistas::PLA.ConsultarArchivosFormato" where "CodigoArchivo"='ARCHIVO_BLOQUEO_CLIENTES_MASIVO_PE';
	--rollback;
commit;

--###########################################
-- DESBLOQUEO MASIVO CLIENTES - ARCHIVO
--###########################################
set schema bscl;
	insert into "BSCL.Plataforma.BD::PLA.Archivo"("IdArchivo","IdEmpresa","CodigoArchivo","TieneSeparador","SeparadorColumnas","TieneCabecera")
	values(25,1,'ARCHIVO_DESBLOQUEO_CLIENTES_MASIVO_PE','S',';','S');
	
	update "BSCL.Plataforma.BD::PLA.Archivo"set "IdEmpresa"=1313 where "IdArchivo"=25;
	commit;
	------------ FORMATO -----------
	-- 219 : TIPOPERSONA
	--   9 : TIPODOCUMENTO
	-- 220 : DOCUMENTOIDENTIDAD
	-- 223 : CODIGOPORTAFOLIO
	--  98 : MOTIVOBLOCLI (OPCIONAL)
	--  99 : MOTIVOBLOPOR (OPCIONAL)
	--------------------------------
	--ArchivoFormato (mapeo)
	insert into "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato") values(241,25,219); 
	insert into "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato") values(242,25,9);
	insert into "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato") values(243,25,220);
	insert into "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato") values(244,25,223);
	insert into "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato") values(245,25,98);
	insert into "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato") values(246,25,99);
	commit;	
	----archivo no recibirá el motivo de bloqueo (se inyectará al servicio a partir de la UI:
	--set schema bscl;
	--delete from "BSCL.Plataforma.BD::PLA.ArchivoFormato" where "IdArchivoFormato" in (245,246);
	--commit;
	select * from "BSCL.Plataforma.BD.Vistas::PLA.ConsultarArchivosFormato" where "CodigoArchivo"='ARCHIVO_DESBLOQUEO_CLIENTES_MASIVO_PE';
	select * from "BSCL.Plataforma.BD.Vistas::PLA.ConsultarArchivosFormato" where "CodigoArchivo"='ARCHIVO_BLOQUEO_CLIENTES_MASIVO';
	--rollback;
commit;

--CUENTA CUSTODIO
set schema bscl;
	--select * from "BSCL.Portafolio.BD::POR.TipoCustodia";
	--select * from "BSCL.Plataforma.BD::PLA.BancoCustodio"
	alter table  "BSCL.Portafolio.BD::POR.TipoCustodia" drop ("ValidaFlujoCustodia");
	alter table  "BSCL.Portafolio.BD::POR.TipoCustodia" drop ("CodigoBancoCustodioDefault");
	alter table  "BSCL.Portafolio.BD::POR.TipoCustodia" drop ("CodigoBancoLiquidadorDefault");
	alter table "BSCL.Portafolio.BD::POR.TipoCustodia" add ("ValidaFlujoCustodia" nvarchar(1) not null default 'N');
	alter table "BSCL.Portafolio.BD::POR.TipoCustodia" add ("CodigoBancoCustodioDefault" nvarchar(40) not null default '');
	alter table "BSCL.Portafolio.BD::POR.TipoCustodia" add ("CodigoBancoLiquidadorDefault" nvarchar(40) not null default '');
	--select * from "BSCL.Portafolio.BD::POR.TipoCustodia";
--end;	

/*****************************************************************************************************************
.______    __          ___   .___________.    ___       _______   ______   .______      .___  ___.      ___      
|   _  \  |  |        /   \  |           |   /   \     |   ____| /  __  \  |   _  \     |   \/   |     /   \     
|  |_)  | |  |       /  ^  \ `---|  |----`  /  ^  \    |  |__   |  |  |  | |  |_)  |    |  \  /  |    /  ^  \    
|   ___/  |  |      /  /_\  \    |  |      /  /_\  \   |   __|  |  |  |  | |      /     |  |\/|  |   /  /_\  \   
|  |      |  `----./  _____  \   |  |     /  _____  \  |  |     |  `--'  | |  |\  \----.|  |  |  |  /  _____  \  
| _|      |_______/__/     \__\  |__|    /__/     \__\ |__|      \______/  | _| `._____||__|  |__| /__/     \__\ 
                                                                                                                 
******************************************************************************************************************/

-- en requiere autorizacion se agrega idEmpresa y se utiliza como llave
set schema bscl;
	drop table "BSCL.Plataforma.BD::PLA.RequiereAutorizacionPASO";
	
	CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.RequiereAutorizacionPASO" (
	   "IdConcepto"				NVARCHAR(6)     not null,
	   "RequiereAutorizacion"	NVARCHAR(1)		not null,
	   "IdEmpresa"				BIGINT 			not null default 0,
	   PRIMARY KEY("IdConcepto","IdEmpresa")
	);

	insert into "BSCL.Plataforma.BD::PLA.RequiereAutorizacionPASO" 
	select "IdConcepto","RequiereAutorizacion",1 from "BSCL.Plataforma.BD::PLA.RequiereAutorizacion"; 
	commit;
	
	drop table "BSCL.Plataforma.BD::PLA.RequiereAutorizacion";
	CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.RequiereAutorizacion" (
	   "IdConcepto"				NVARCHAR(6)     not null,
	   "RequiereAutorizacion"	NVARCHAR(1)		not null,
	   "IdEmpresa"				BIGINT 			not null default 0,
	   PRIMARY KEY("IdConcepto","IdEmpresa")
	);
	insert into "BSCL.Plataforma.BD::PLA.RequiereAutorizacion" 
	select "IdConcepto","RequiereAutorizacion",1 from "BSCL.Plataforma.BD::PLA.RequiereAutorizacionPASO";
	commit;
	
	insert into "BSCL.Plataforma.BD::PLA.RequiereAutorizacion" 
	select "IdConcepto","RequiereAutorizacion",1313 from "BSCL.Plataforma.BD::PLA.RequiereAutorizacionPASO";
	commit;
	drop table "BSCL.Plataforma.BD::PLA.RequiereAutorizacionPASO";
	select * from "BSCL.Plataforma.BD::PLA.RequiereAutorizacion" 
--end;

--CAMBIAR PRECISION DATO EN GIRO
set schema bscl;
	alter table "BSCL.Plataforma.BD::REG.Giro" alter("Nombre" NVARCHAR(500) not null default '');
--end

--CODIGOS EN MODELO DIVISION PAIS PARA CONSTRUIR UBIGEO
set schema bscl;
	alter table "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais"
	add("Codigo" nvarchar(10) not null default '');
	
	alter table "BSCL.Plataforma.BD::REG.DivisionPais"
	add("Codigo" nvarchar(10) not null default '');
--end;

/*******************
CONSULTA CUSTODIA
********************/
set schema bscl;
	create column table "BSCL.Custodia.BD::CUS.TipoCuenta"(
		"IdEmpresa"		bigint,
		"IdTipoCuenta" 	nvarchar(10),
		"Nombre"		nvarchar(40) not null default '',
		primary key ("IdEmpresa","IdTipoCuenta")
	);
	
	insert into "BSCL.Custodia.BD::CUS.TipoCuenta"("IdEmpresa","IdTipoCuenta","Nombre")
	select 1,'CPRO','Cuenta propia' 	from dummy union all
	select 1,'REL', 'Relacionado' 		from dummy union all
	select 1,'TER', 'Cuenta terceros' 	from dummy;
commit;

-- VALOR DEFECTO SISTEMA
set schema bscl;
	CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.ValorDefectoSistemaRESP"(
		"IdValorDefectoSistema"	NVARCHAR(40)	NOT NULL,
		"Nombre"				NVARCHAR(255)	NOT NULL DEFAULT '',
		"Valor"					NVARCHAR(255)	NOT NULL DEFAULT '',
		"IdEmpresa"				BIGINT			NOT NULL DEFAULT 0,
		"IdUsuarioCreador"		BIGINT			NOT NULL DEFAULT 0,
		"FechaCreacion"			SECONDDATE		NOT NULL DEFAULT '0001-01-01 00:00:00',
		"FechaModificacion"		SECONDDATE		NOT NULL DEFAULT '0001-01-01 00:00:00',
		"IdEstado"				NVARCHAR(3)		DEFAULT 'ACT' NOT NULL,
	  	PRIMARY KEY ("IdValorDefectoSistema","IdEmpresa")
	);
	insert into "BSCL.Plataforma.BD::PLA.ValorDefectoSistemaRESP" select * from "BSCL.Plataforma.BD::PLA.ValorDefectoSistema";
	commit;
	--select * from "BSCL.Plataforma.BD::PLA.ValorDefectoSistemaRESP";
	drop table "BSCL.Plataforma.BD::PLA.ValorDefectoSistema";
	
	CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.ValorDefectoSistema"(
		"IdValorDefectoSistema"	NVARCHAR(40)	NOT NULL,
		"Nombre"				NVARCHAR(255)	NOT NULL DEFAULT '',
		"Valor"					NVARCHAR(255)	NOT NULL DEFAULT '',
		"IdEmpresa"				BIGINT			NOT NULL DEFAULT 0,
		"IdUsuarioCreador"		BIGINT			NOT NULL DEFAULT 0,
		"FechaCreacion"			SECONDDATE		NOT NULL DEFAULT '0001-01-01 00:00:00',
		"FechaModificacion"		SECONDDATE		NOT NULL DEFAULT '0001-01-01 00:00:00',
		"IdEstado"				NVARCHAR(3)		DEFAULT 'ACT' NOT NULL,
	  	PRIMARY KEY ("IdValorDefectoSistema","IdEmpresa")
	);
	
	insert into "BSCL.Plataforma.BD::PLA.ValorDefectoSistema" select * from "BSCL.Plataforma.BD::PLA.ValorDefectoSistemaRESP";
	commit;
	--select * from "BSCL.Plataforma.BD::PLA.ValorDefectoSistema";
	drop table "BSCL.Plataforma.BD::PLA.ValorDefectoSistemaRESP";
--end;

/***********************************************************************************************************************
   ____  _____  _____  ______ _   _ ______  _____     _____  ______ _   _ _______           ______ _____     _         
  / __ \|  __ \|  __ \|  ____| \ | |  ____|/ ____|   |  __ \|  ____| \ | |__   __|/\       |  ____|_   _|   | |  /\    
 | |  | | |__) | |  | | |__  |  \| | |__  | (___     | |__) | |__  |  \| |  | |  /  \      | |__    | |     | | /  \   
 | |  | |  _  /| |  | |  __| | . ` |  __|  \___ \    |  _  /|  __| | . ` |  | | / /\ \     |  __|   | | _   | |/ /\ \  
 | |__| | | \ \| |__| | |____| |\  | |____ ____) |   | | \ \| |____| |\  |  | |/ ____ \    | |     _| || |__| / ____ \ 
  \____/|_|  \_\_____/|______|_| \_|______|_____/    |_|  \_\______|_| \_|  |_/_/    \_\   |_|    |_____\____/_/    \_\
************************************************************************************************************************/

--FORMA DE NEGOCIACION. CAMBIAR LLAVE PRIMARIA E INSERTAR DATOS
set schema bscl;
	select * from "BSCL.Orden.BD::ORD.SistemaBursatil";
	
	CREATE COLUMN TABLE "BSCL.Orden.BD::ORD.PASOSistemaBursatil"(
	"IdSistemaBursatil" nvarchar(40),"NombreSistemaBursatil" nvarchar(100));
	
	insert into "BSCL.Orden.BD::ORD.PASOSistemaBursatil"("IdSistemaBursatil","NombreSistemaBursatil")
	select "IdSistemaBursatil","NombreSistemaBursatil" from "BSCL.Orden.BD::ORD.SistemaBursatil";
	commit;
	
	select * from "BSCL.Orden.BD::ORD.PASOSistemaBursatil";
	
	--RECREAR TABLA
	drop table "BSCL"."BSCL.Orden.BD::ORD.SistemaBursatil";
	CREATE COLUMN TABLE "BSCL"."BSCL.Orden.BD::ORD.SistemaBursatil" (
		"IdSistemaBursatil"					nvarchar(40),
		"NombreSistemaBursatil"				nvarchar(100),
		"IdClasificacionTipoInstrumento" 	nvarchar(3),
		"IdEmpresa" 						bigint,
		PRIMARY KEY ("IdSistemaBursatil","IdEmpresa")
	);
	
	insert into "BSCL"."BSCL.Orden.BD::ORD.SistemaBursatil"(
	"IdSistemaBursatil","NombreSistemaBursatil","IdClasificacionTipoInstrumento","IdEmpresa")
	select "IdSistemaBursatil","NombreSistemaBursatil",null,1 
	from "BSCL"."BSCL.Orden.BD::ORD.PASOSistemaBursatil";
	commit;

	insert into "BSCL"."BSCL.Orden.BD::ORD.SistemaBursatil"(
	"IdSistemaBursatil","NombreSistemaBursatil","IdClasificacionTipoInstrumento","IdEmpresa")
	select 'NEGOCIACIONCONTINUA','NEGOCIACIÓN CONTINUA','RF',1313 from dummy union all
	select 'MERCADODELDINERO','MERCADO DEL DINERO','RF',1313 from dummy;
	commit;
	
	drop table "BSCL.Orden.BD::ORD.PASOSistemaBursatil";
	
	
--end;

--IMPORTANTE: RECREAR VISTA: "BSCL.Orden.BD.Vistas::ConsultarSistemaBursatil";
SET SCHEMA "BSCL"; 
	DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarSistemaBursatil";
	CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarSistemaBursatil" AS 
		select
			"IdSistemaBursatil",
			"NombreSistemaBursatil",
			"IdClasificacionTipoInstrumento",
			"IdEmpresa"
		from "BSCL.Orden.BD::ORD.SistemaBursatil";
	GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarSistemaBursatil" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
--end;
