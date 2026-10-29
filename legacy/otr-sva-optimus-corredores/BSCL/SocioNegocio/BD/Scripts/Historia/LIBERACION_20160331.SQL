-- ########################################################################################
--
--  =========================================================
--  ESTE ARCHIVO CONTIEN LOS SCRIPT HISTORICOS DE LIBERACION
--  =========================================================
--  * Debe incluir los ALTER de Tablas
--  * Debe incluir nuevas tablas, indices, secuencuias, etc.
--  * Debe incluir insert de datos maestros necesarios para el funcionamiento 
--  NOTA: Este archivo no reemplaza los scripts de creación que deben ir en cada módulo.
-- 
-- ########################################################################################

SET SCHEMA BSCL;
--
--
-- LIBERACION SEMANA: 24-03-2016
--

--################################################
--       <JVILLAGRAN>.<Incorporación de IdBanco en contraparte(nexo con Banco de plataforma)>
--################################################

ALTER TABLE "BSCL"."BSCL.SocioNegocio.BD::CNT.Contraparte"
ADD ("IdBanco"      BIGINT      DEFAULT 0);

--------------------------------------------------
--      FIN <MOTIVO>
--------------------------------------------------

--################################################
--   JG. Cambios campos direccion en enrolamiento
--################################################

RENAME COLUMN "BSCL.SocioNegocio.BD::SOC.Enrolamiento"."Direccion" TO "DireccionCalle";
 
ALTER TABLE "BSCL.SocioNegocio.BD::SOC.Enrolamiento" 
ADD("DireccionNumero" NVARCHAR(40) not null default '');
 
ALTER TABLE "BSCL.SocioNegocio.BD::SOC.Enrolamiento" 
ADD("DireccionDepartamento" NVARCHAR(40) not null default '');

DROP TYPE "BSCL.SocioNegocio.BD.Tipos::SOC.ConsultarEnrolamiento";

CREATE TYPE "BSCL.SocioNegocio.BD.Tipos::SOC.ConsultarEnrolamiento" AS TABLE (
"IdEnrolamiento"			BIGINT,
"IdEmpresa" 				BIGINT,
"IdUsuario" 				BIGINT,
"IdTipoEntidad" 			NVARCHAR(100),
"IdEntidadEnrolada" 		BIGINT,
"IdTipoDocumentoIdentidad"  NVARCHAR(1),
"DocumentoIdentidad" 		NVARCHAR(40),
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
"Telefono" 					NVARCHAR(100),
"Email" 					NVARCHAR(100),
"DireccionCalle" 			NVARCHAR(100),
"DireccionNumero" 			NVARCHAR(40),
"DireccionDepartamento" 	NVARCHAR(40),
"FechaCreacion" 			SECONDDATE,
"FechaModificacion" 		SECONDDATE,
"CLI_IdCategoriaInversionista" NVARCHAR(40),
"POR_IdPerfilComercial" 	NVARCHAR(40), 
"EMP_CodigoEmpleado"			NVARCHAR(40),
"EMP_CodigoTipoCargoEmpleado"	NVARCHAR(6),
"EMP_IdEmpleadoSupervisor"		BIGINT,
"EMP_IdSucursal"				BIGINT,
"EMP_FechaInicioCargo"			DATE,
"EMP_FechaTerminoCargo" 		DATE,
"CNT_Codigo" 					NVARCHAR(40),
"CNT_Emisor" 					NVARCHAR(1),
"CNT_Depositos"					NVARCHAR(1),
"CNT_Bolsa" 					NVARCHAR(1),
"CNT_Corredora" 				NVARCHAR(1),
"CNT_CamaraDeCompensacion" 		NVARCHAR(1),
"CNT_Banco" 					NVARCHAR(1),
"PRO_IdAreaProveedor"			BIGINT
 );
--------------------------------------------------
--   FIN Cambios campos direccion en enrolamiento
--------------------------------------------------
--################################################
--       <CFLORES>.<Incorporación de grupos de Direcciones>
--################################################

SET SCHEMA BSCL;
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(37,'ACT','GRUPODIRECCIONELECTRONICA');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(38,'INA','GRUPODIRECCIONELECTRONICA');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(39,'PEN','GRUPODIRECCIONELECTRONICA');



insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('NUGDIE',1,'SOC','AUT_GDI_NUE','Nuevo Grupo de Direcciones',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('MOGDIE',1,'SOC','AUT_GDI_MOD','Modificación de Grupo Direcciones',1,'S','' ,'' ,0,0,'VIG');


INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('NUGDIE','MP','SOC','NUEVO GRUPO DIRECCIONES','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MOGDIE','MP','SOC','MODIFICACION DE GRUPO DIRECCIONES','VIG'); 


INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('CAECNT','MC','SOC','CAMBIO DE ESTADO CONTRAPARTE','VIG'); 



insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('NUEREL',1,'SOC','AUT_REL_NUE','Nueva Relación de Personas',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('MODREL',1,'SOC','AUT_REL_NUE','Modificación Relación de Personas',1,'S','' ,'' ,0,0,'VIG');

INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('NUEREL','MP','SOC','NUEVA RELACION DE PERSONAS','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MODREL','MP','SOC','MODIFICACION RELACION DE PERSONAS','VIG'); 



insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('NAUTOR',1,'SOC','AUT_AUT_NUE','Nuevo Autorizado a Dar Ordenes',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('CAEAUT',1,'SOC','AUT_AUT_CAE','Cambio de Estado Autorizado a Dar Ordenes',1,'S','' ,'' ,0,0,'VIG');

INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('NAUTOR','MP','SOC','NUEVO AUTORIZADO A DAR ORDENES','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('CAEAUT','MP','SOC','CAMBIO DE ESTADO AUTORIZADO A DAR ORDENES','VIG'); 

insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion")
values('CAEAUT','S');

UPDATE "BSCL.Plataforma.BD::PLA.Concepto" SET "Nombre"='CAMBIO DE ESTADO PERSONA' WHERE "IdConcepto"= 'CAEPER';

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(239,'BSCL.SocioNegocio.BD.Servicios::PER.CrearPersonaGrupoDireccionElectronica',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(241,'BSCL.SocioNegocio.BD.Servicios::PER.CrearPersonaGrupoDireccionElectronicaDetalle',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(242,'BSCL.SocioNegocio.BD.Servicios::PER.ModificarPersonaGrupoDireccionElectronica',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(243,'BSCL.SocioNegocio.BD.Servicios::PER.ModificarPersonaGrupoDireccionElectronicaDetalle',0,'P','');
--------------------------------------------------
--      FIN <MOTIVO>
--------------------------------------------------





--################################################
--       <CFLORES>.<Tabla tipo Persona relacionadas>
--################################################

 SET SCHEMA "BSCL";
 CREATE TYPE "BSCL.SocioNegocio.BD::REL.PersonasRelacionadas" AS TABLE  
 
									 ("IdPersonaOrigen"			BIGINT,
										 "IdRelacionPersona"		BIGINT, 
										 "Administracion" 			NVARCHAR(40),
   										 "IdTipoRelacion" 			NVARCHAR(6),
    									 "NombreTipoRelacion"		NVARCHAR(520),
    									 "FechaInicioRelacion" 		SECONDDATE,
    									 "FechaFinRelacion" 		SECONDDATE,
    									 "IdEstado" 				NVARCHAR(3),
    									 "NombreEstado" 			NVARCHAR(100),
    								     "IdSocioDeNegocio" 		BIGINT,
										 "IdPersona" 				BIGINT, 
										 "IdEmpresa" 				BIGINT,
										 "IdCliente" 				BIGINT,
										 "IdEmpleado" 				BIGINT,
										 "IdProveedor" 				BIGINT,
										 "IdContraparte" 			BIGINT,
										 "FechaIngreso" 			SECONDDATE,
										 "FechaCreacion" 			SECONDDATE,
										 "FechaModificacion" 		SECONDDATE,
										 "DocumentoIdentidadDefecto" NVARCHAR(40),
										 "CodigoTipoPersona"						NVARCHAR(2),
										 "NombreCompleto"			NVARCHAR(400),
										 "RazonSocial" 				NVARCHAR(100),
										 "Alias" 					NVARCHAR(100),    
										 "Nombres" 					NVARCHAR(100),
										 "ApellidoPaterno"			NVARCHAR(100),
										 "ApellidoMaterno" 			NVARCHAR(100),    
										 "FechaNacimiento" 			DAYDATE,
										 "IdPaisResidencia" 		BIGINT,
										 "IdTipoDocumentoIdentidadDefecto"				NVARCHAR(1),        
									     "NombreTipoDocumentoIdentidadDefecto" 			NVARCHAR(100),      
									     "NombreAbrTipoDocumentoIdentidadDefecto"		NVARCHAR(5),   
									     "TipoPersona" 				NVARCHAR(300),--desde constante
										 "CodigoPaisResidencia" 	NVARCHAR(40),
										 "NombrePaisResidencia" 	NVARCHAR(100), 
										 "EsCliente" 				NVARCHAR(6),
										 "EsEmpleado"				NVARCHAR(6),
										 "EsProveedor"				NVARCHAR(6),
										 "EsContraparte"			NVARCHAR(6),
										 "EstadoPersona"   			NVARCHAR(3), 
										 "EstadoCliente"   			NVARCHAR(3), 
										 "EstadoEmpleado"   		NVARCHAR(3), 
										 "EstadoProveedor"   		NVARCHAR(3), 
										 "EstadoContraparte"	    NVARCHAR(3),
										 "NombreEstadoPersona"   	NVARCHAR(100), 
										 "NombreEstadoCliente"   	NVARCHAR(100), 
										 "NombreEstadoEmpleado"   	NVARCHAR(100), 
										 "NombreEstadoProveedor"   	NVARCHAR(100), 
										 "NombreEstadoContraparte"	NVARCHAR(100) 	 	
									    );
									    
SET SCHEMA "BSCL";	
DELETE FROM "BSCL.SocioNegocio.BD::REL.TipoRelacion"WHERE "IdTipoRelacion"='CLIOPE';
	
INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" ( "IdEmpresa","IdTipoCargoEmpleado","CodigoTipoCargoEmpleado","NombreTipoCargoEmpleado" )  
VALUES(1,7,'OPEOTC','Operador');--Operador otra corredora
										    
SET SCHEMA "BSCL";	
UPDATE "BSCL.SocioNegocio.BD::REL.TipoRelacion" SET "NombreTipoRelacion"='Autoriza a dar órdenes'	WHERE "IdTipoRelacion"='AUTDOR';
	
	


--------------------------------------------------
--      FIN <MOTIVO>
--------------------------------------------------






