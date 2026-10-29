SET SCHEMA "BSCL";
	
/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/
drop table "BSCL.Plataforma.BD::ACC.Usuario" cascade;
drop table "BSCL.Plataforma.BD::ACC.Sesion" cascade;
drop table "BSCL.Plataforma.BD::ACC.Rol" cascade;
drop table "BSCL.Plataforma.BD::ACC.RolUsuario" cascade;
drop table "BSCL.Plataforma.BD::ACC.Perfil" cascade;
drop table "BSCL.Plataforma.BD::ACC.PerfilAcceso" cascade;
drop table "BSCL.Plataforma.BD::ACC.Acceso" cascade;
drop table "BSCL.Plataforma.BD::ACC.RolPerfil" cascade;
drop table "BSCL.Plataforma.BD::ACC.RecuperarClave" cascade;

/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/
create column table "BSCL.Plataforma.BD::ACC.Usuario" (
   "IdUsuario"  			BIGINT              not null,
   "IdEmpresa"         		BIGINT              not null Default '0', -- FK //[FK]ACC.Usuario;IdEmpresa;PLA.Empresa;IdEmpresa//
   "CodigoUsuario"     		NVARCHAR(40)       	not null,
   "Clave"          		NVARCHAR(520)       not null,
   "FechaIngreso"		 	DATE				not null,
   "FechaVigencia"		 	DATE				not null,
   "FechaUltimaConexion" 	DATE				not null,
   "DiasVencimientoClave"	DECIMAL(26,8) 		not null,
   "Conectado"				NVARCHAR(520)		not null,
   "Hora"					TIME 				not null,
   "Terminal"				NVARCHAR(520)		not null,
   "IdEstado"               NVARCHAR(3)			not null,
   "IntentosConexion"		INTEGER				not null Default '0',
   "FechaBloqueoTemporal"	TIMESTAMP 			not null default '0001-01-01',
   "TipoEntidad" 			NVARCHAR(40)		not null,
   "IdEntidad" 				BIGINT				not null,
   "EsAutorizado"			NVARCHAR(1) 		not null default 'N',
   primary key ("IdUsuario")
);
ALTER TABLE "BSCL"."BSCL.Plataforma.BD::ACC.Usuario" ADD CONSTRAINT const_key UNIQUE ("IdEmpresa","CodigoUsuario");

create column table "BSCL.Plataforma.BD::ACC.Sesion" (
   "IdTokenSesion"  		NVARCHAR(520)       not null,
   "IdUsuario"  			BIGINT              not null, --[FK]ACC.Sesion;IdUsario;ACC.Usuario;IdUsuario//
   "TokenValido"          	NVARCHAR(1)       	not null,
   "FechaUltimoUso"		 	TIMESTAMP			not null,
   "UsoToken"          		INTEGER            	default 0 not null,
   "FechaIngreso"		 	TIMESTAMP			not null,
   "FechaSalida"		 	TIMESTAMP			default '0001-01-01' not null,
   primary key ("IdTokenSesion")
);

create column table "BSCL.Plataforma.BD::ACC.Rol" (
   "IdRol" 					BIGINT     			not null,
   "IdEmpresa"         		BIGINT              not null Default '0', -- FK //[FK]ACC.Usuario;IdEmpresa;PLA.Empresa;IdEmpresa//
   "Nombre"        			NVARCHAR(100)       not null,
   "IdEstado"     			NVARCHAR(3)			not null,
  primary key ("IdRol")
);

create column table "BSCL.Plataforma.BD::ACC.RolUsuario" (
   "IdRolUsuario"       	BIGINT              not null,
   "IdUsuario"  			BIGINT             	not null, --[FK]ACC.Sesion;IdUsario;ACC.Usuario;IdUsuario//
   "IdRol"             		BIGINT         		not null, --//[FK]ACC.RolUsuario;IdRol;ACC.Rol;IdRol//    
   "EsAutorizado" 			NVARCHAR(1) 		DEFAULT 'N' NOT NULL,
   primary key ("IdRolUsuario")
);

create column table "BSCL.Plataforma.BD::ACC.Perfil" (
   "IdPerfil"       		BIGINT              not null,
   "IdEmpresa"         		BIGINT             	not null Default '0', -- FK //[FK]ACC.Usuario;IdEmpresa;PLA.Empresa;IdEmpresa//
   "Nombre"        			NVARCHAR(100)       not null,
   primary key ("IdPerfil")
);

create column table "BSCL.Plataforma.BD::ACC.Acceso" (
   "IdAcceso"  				BIGINT              not null, -- Id del acceso
   "NombreAcceso"			NVARCHAR(100)		not null, -- Nombre a desplegar en la pantalla de asignacion de accesos
   "TipoAcceso"				NVARCHAR(5)			not null, -- MENU1,MENU2,MENU3,BOTON,CMBX, INPT,SRV 
   "Padre"					BIGINT				not null, -- 0: Opcion de menu, IdAcceso en caso de ser hijo de algun nodo
   "Orden"			       	INT          	    not null, -- Orden por el cual se desplegaran las opciones
   "IdServicio"			  	BIGINT			    not null, -- el id del servicio que necesita el componente (entidad servicio)
   "IdElementoHtml"			NVARCHAR(200) 		unique	not null,
   "EsVisibleMenu"			NVARCHAR(1)			not null, -- solo para opciones de menu, valores 'S' o 'N'					
   primary key ("IdAcceso")
);

create column table "BSCL.Plataforma.BD::ACC.PerfilAcceso" (
   "IdAccesoPerfil"  		BIGINT              not null,
   "IdEmpresa"				BIGINT				not null,
   "IdPerfil"  				BIGINT              not null, 
   "IdAcceso"				BIGINT				not null,
   "IdEstado"		 		NVARCHAR(3)			not null,
   primary key ("IdAccesoPerfil")
);
   
create column table "BSCL.Plataforma.BD::ACC.RolPerfil" (
   "IdRolPerfil"       		BIGINT              not null,
   "IdRol"             		BIGINT         		not null, --//[FK]ACC.RolUsuario;IdRol;ACC.Rol;IdRol//    
   "IdPerfil"  				BIGINT              not null, --//[FK]ACC.Acceso;ACC.Perfil;IdPerfil// /
   "IdEstado"		 		NVARCHAR(3)			not null,
   primary key ("IdRolPerfil")
);

create column table "BSCL.Plataforma.BD::ACC.RecuperarClave" (
   "IdEmpresa"       		BIGINT              not null,
   "IdUsuario"				BIGINT				not null,
   "Token"             		NVARCHAR(10)        not null,
   "ValidoDesde"  			SECONDDATE			not null,
   "ValidoHasta"		 	SECONDDATE			not null,
   "IdEstado"				NVARCHAR(3)			not null,
   primary key ("IdEmpresa","IdUsuario","Token")
);

/*==============================================================*/
/* constraint:                                                  */
/*==============================================================*/

/* Table: */

alter table "BSCL.Plataforma.BD::ACC.Sesion"
   add constraint "FK_Usuario_Sesion" foreign key ("IdUsuario")
      references "BSCL.Plataforma.BD::ACC.Usuario" ("IdUsuario") on delete restrict on update restrict;

/* Table: */
alter table "BSCL.Plataforma.BD::ACC.RolUsuario"
   add constraint "FK_Usuario_RolUsuario" foreign key ("IdUsuario")
      references "BSCL.Plataforma.BD::ACC.Usuario" ("IdUsuario") on delete restrict on update restrict;

alter table "BSCL.Plataforma.BD::ACC.RolUsuario"
   add constraint "FK_Rol_RolUsuario" foreign key ("IdRol")
      references "BSCL.Plataforma.BD::ACC.Rol" ("IdRol") on delete restrict on update restrict;

/* Table: */
alter table "BSCL.Plataforma.BD::ACC.Acceso"
   add constraint "FK_Perfil_Acceso" foreign key ("IdPerfil")
      references "BSCL.Plataforma.BD::ACC.Perfil" ("IdPerfil") on delete restrict on update restrict;

/* Table: */
alter table "BSCL.Plataforma.BD::ACC.RolPerfil"
   add constraint "FK_Rol_RolPerfil" foreign key ("IdRol")
      references "BSCL.Plataforma.BD::ACC.Rol" ("IdRol") on delete restrict on update restrict;

alter table "BSCL.Plataforma.BD::ACC.RolPerfil"
   add constraint "FK_Perfil_RolPerfil" foreign key ("IdPerfil")
      references "BSCL.Plataforma.BD::ACC.Perfil" ("IdPerfil") on delete restrict on update restrict;
      
/*==============================================================*/
/* SECUENCES:                                                       */
/*==============================================================*/

DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::Usuario"	CASCADE;
CREATE SEQUENCE "BSCL"."BSCL.Plataforma.BD.Secuencias::Usuario"
       START     WITH 4
       INCREMENT BY 1
       MINVALUE  1;

DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::RolUsuario"	CASCADE;       
CREATE SEQUENCE "BSCL"."BSCL.Plataforma.BD.Secuencias::RolUsuario"
       START     WITH 4
       INCREMENT BY 1
       MINVALUE  1;

DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::Rol"	CASCADE;       
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::Rol"
       START     WITH 3
       INCREMENT BY 1
       MINVALUE  1; 

DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::RolPerfil"	CASCADE;       
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::RolPerfil"
       START     WITH 3
       INCREMENT BY 1
       MINVALUE  1;
               
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::Perfil"	CASCADE;
CREATE SEQUENCE "BSCL"."BSCL.Plataforma.BD.Secuencias::Perfil"
       START     WITH 3
       INCREMENT BY 1
       MINVALUE  1; 
       
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::PerfilAcceso"	CASCADE;
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::PerfilAcceso"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1; 
/*==============================================================*/
/* Index:                                                       */
/*==============================================================*/

/* Table: */

--create index "INX01_Evento" on "BSCL.Plataforma.BD::AUT.Evento"("IdTipoOperacion","IdConcepto","NumeroOperacionConcepto","FechaOperacionConcepto","IdEstado");

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit
