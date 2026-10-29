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
-- LIBERACION SEMANA: 18-03-2016
--



--################################################
--       <MMACHMAR>.<Incorporar Ejecutivo en Ficha Cliente>
--################################################

ALTER TABLE "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" ADD ("IdPersonaEjecutivo" BIGINT CS_FIXED  DEFAULT 0);

--------------------------------------------------
--      FIN <MOTIVO>
--------------------------------------------------


-- ** LIBREACION  RELACIONADOS /  (Inicio) **

​ --Modificacion de estructura
SET SCHEMA "BSCL"; --Agregar campo
alter table "BSCL.SocioNegocio.BD::PER.Persona" add ( "FechaInicioConyuge" 				SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL,
													  "FechaInicioRepresentante" 		SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL);
													  
-- ** LIBREACION  RELACIONADOS /  (Termino) **

-- ** LIBREACION  GRUPO DIRECCIONES /  (Inicio) **

SET SCHEMA "BSCL"; --Crear tabla
CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.TipoDestinatarioCorreo" (
	 "IdTipoDestinatario" 	NVARCHAR(6) NOT NULL ,
	 "Nombre" 				NVARCHAR(100),
	 "IdEstado" 			NVARCHAR(3), 
	 PRIMARY KEY ("IdTipoDestinatario")
);

SET SCHEMA "BSCL"; --Cargar tabla
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoDestinatarioCorreo" ( "IdTipoDestinatario","Nombre","IdEstado")  
VALUES('PARA','Para','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoDestinatarioCorreo" ( "IdTipoDestinatario","Nombre","IdEstado")  
VALUES('CC','Copia','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoDestinatarioCorreo" ( "IdTipoDestinatario","Nombre","IdEstado")  
VALUES('CCO','Copia Oculta','ACT');




SET SCHEMA "BSCL"; --Crear tabla
CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::PER.GrupoDireccion" (
	 "IdGrupoDireccion" 	 BIGINT CS_FIXED NOT NULL ,
	 "Grupo" 				 NVARCHAR(100),
	 "Comentario" 			 NVARCHAR(100),
     "IdPersona" 			 BIGINT CS_FIXED NOT NULL ,--*	 
	 "FechaCreacion"     	 SECONDDATE          default '0001-01-01 00:00:00' not null,
	 "FechaModificacion"  	 SECONDDATE          default '0001-01-01 00:00:00' not null,
	 "FechaIngreso"       	 SECONDDATE          default '0001-01-01 00:00:00' not null,	 
	 "IdEstado" 	 		 NVARCHAR(3) 		 default 'ACT' NOT NULL ,
     "IdEstadoAutorizacion"  NVARCHAR(3)         default 'PEN' not null,
     "Borrado" 				 NVARCHAR(1) 		 default 'N',
     "IdUsuarioCreador"   	 BIGINT  			 default 0,
	 PRIMARY KEY ("IdGrupoDireccion")
);

SET SCHEMA "BSCL"; --Crear secuencia
create sequence "BSCL.SocioNegocio.BD.Secuencias::GrupoDireccion" increment by 1 start with 10000 minvalue 10000 maxvalue 4611686018427387903 no cycle ;



SET SCHEMA "BSCL"; --Crear tabla
CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::PER.GrupoDireccionDetalle" (
	 "IdGrupoDireccionDetalle" 	 BIGINT CS_FIXED NOT NULL ,
	 "IdDireccionElectronica" 	 BIGINT CS_FIXED NOT NULL ,
	 "IdTipoDestinatario" 		 NVARCHAR(6),
     "IdGrupoDireccion" 		 BIGINT CS_FIXED NOT NULL ,
	 "FechaCreacion"     		 SECONDDATE          default '0001-01-01 00:00:00' not null,
	 "FechaModificacion"  	 	 SECONDDATE          default '0001-01-01 00:00:00' not null,
	 "FechaIngreso"       	 	 SECONDDATE          default '0001-01-01 00:00:00' not null,	 
	 "IdEstado" 	 		 	 NVARCHAR(3) 		 default 'ACT' NOT NULL ,
     "IdEstadoAutorizacion"  	 NVARCHAR(3)         default 'PEN' not null,	 
     "Borrado" 				 	 NVARCHAR(1) 		 default 'N',
     "IdUsuarioCreador"   	 	 BIGINT  			 default 0,
	 PRIMARY KEY ("IdGrupoDireccionDetalle")
);

SET SCHEMA "BSCL"; --Crear secuencia
create sequence "BSCL.SocioNegocio.BD.Secuencias::GrupoDireccionDetalle" increment by 1 start with 10000 minvalue 10000 maxvalue 4611686018427387903 no cycle ;






-- ** LIBREACION  GRUPO DIRECCIONES /  (Termino) **




--################################################
--    JG - Integracion de Division Pais (Comuna en Chile)
--################################################

SET SCHEMA "BSCL"; 

ALTER TABLE "BSCL.SocioNegocio.BD::PER.DireccionFisica" 
ADD("IdDivisionPais" BIGINT CS_FIXED);

ALTER TABLE "BSCL.SocioNegocio.BD::SOC.Enrolamiento"
ADD("IdDivisionPais" BIGINT DEFAULT 0);


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
"Direccion" 				NVARCHAR(100),
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
--      Integracion de Division Pais 
--------------------------------------------------
