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
-- LIBERACION SEMANA: 28-04-2016
--

--################################################
-- Cflores   - Cambio estado Portafolio
--################################################


SET SCHEMA "BSCL";
UPDATE "BSCL"."BSCL.Plataforma.BD::PLA.Concepto" SET "IdModulo"='SOC'  WHERE "IdConcepto"='CAEPOR';



SET SCHEMA "BSCL";

ALTER TABLE "BSCL.Plataforma.BD::IND.Indicador" 
ADD( "FechaVencimiento"   SECONDDATE           default '0001-01-01 00:00:00' not null);

DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias.Limites.hdbsequence::Limites";

--################################################
-- JG - FATCA
--################################################
CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::PER.ClasificacionFatca"(
	"IdClasificacionFatca"			NVARCHAR(10) 				NOT NULL,
	"Nombre"						NVARCHAR(100)				NOT NULL DEFAULT '',
	"PorDefecto"					NVARCHAR(1)					NOT NULL DEFAULT 'N',
PRIMARY KEY ("IdClasificacionFatca"));	

CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::PER.ClasificacionEmpresaFatca"(
	"IdClasificacionEmpresaFatca"	NVARCHAR(10) 				NOT NULL,
	"Nombre"						NVARCHAR(100)				NOT NULL DEFAULT '',
	"PorDefecto"					NVARCHAR(1)					NOT NULL DEFAULT 'N',
 	"UsaNumeroGIIN"					NVARCHAR(1)					NOT NULL DEFAULT 'N',
PRIMARY KEY ("IdClasificacionEmpresaFatca"));	

CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::PER.Fatca" (
 	"IdPersona" 					BIGINT 						NOT NULL,
 	"IdEstado"  					NVARCHAR(3) 				NOT NULL DEFAULT '',
 	"IdClasificacionFatca"			NVARCHAR(10)				NOT NULL DEFAULT '',
 	"IdClasificacionEmpresaFatca"	NVARCHAR(10)				NOT NULL DEFAULT '',
 	"NumeroGIIN"					NVARCHAR(40)				NOT NULL DEFAULT '', -- depende de clasificacion empresa
	"FechaFirmaFormulario"			SECONDDATE CS_SECONDDATE 	NOT NULL DEFAULT '0001-01-01 00:00:00',
 	"AutorizacionInformarOrganismo"	NVARCHAR(1)					NOT NULL DEFAULT 'N',
 	--INDICIOS
	--IdPaisNacionalidad1, la de ficha persona!
	"TIN1"							NVARCHAR(40)				NOT NULL DEFAULT '', -- ANALIZAR SI MANTENER!
 	"IdPaisNacionalidad2"			BIGINT						NOT NULL DEFAULT 0,
	"TIN2"							NVARCHAR(40)				NOT NULL DEFAULT '',
 	"IdPaisNacionalidad3"			BIGINT						NOT NULL DEFAULT 0,
 	"TIN3"							NVARCHAR(40)				NOT NULL DEFAULT '',
 	"IdPaisNacionalidad4"			BIGINT						NOT NULL DEFAULT 0,
 	"TIN4"							NVARCHAR(40)				NOT NULL DEFAULT '',
 	"IdPaisNacionalidad5"			BIGINT						NOT NULL DEFAULT 0,
 	"TIN5"							NVARCHAR(40)				NOT NULL DEFAULT '',
 	--EXCEPCIONES
 	"ExcepcionResidente"			NVARCHAR(1)					NOT NULL DEFAULT 'N',
 	"ExcepcionNacimiento"			NVARCHAR(1)					NOT NULL DEFAULT 'N',
 	"ExcepcionDomicilio"			NVARCHAR(1)					NOT NULL DEFAULT 'N',
 	"ExcepcionTelefono"				NVARCHAR(1)					NOT NULL DEFAULT 'N',
 	"ExcepcionOrdenPermanente"		NVARCHAR(1)					NOT NULL DEFAULT 'N',	
 	"ExcepcionFirma"				NVARCHAR(1)					NOT NULL DEFAULT 'N',
 	--
 	"FechaIngreso" 					SECONDDATE CS_SECONDDATE 	NOT NULL DEFAULT '0001-01-01 00:00:00',
 	"FechaCreacion" 				SECONDDATE CS_SECONDDATE 	NOT NULL DEFAULT '0001-01-01 00:00:00',
 	"FechaModificacion" 			SECONDDATE CS_SECONDDATE 	NOT NULL DEFAULT '0001-01-01 00:00:00', 
 	"Borrado" 						NVARCHAR(1) 				DEFAULT 'N',
 	"IdUsuarioCreador"  			BIGINT  					DEFAULT 0,
 	"IdUsuarioModificador"  		BIGINT  					DEFAULT 0,
PRIMARY KEY ("IdPersona")) ;	


INSERT INTO "BSCL.SocioNegocio.BD::PER.ClasificacionFatca"("IdClasificacionFatca","Nombre","PorDefecto")
VALUES('PENDIENTE','Pendiente de Clasificar','N');
INSERT INTO "BSCL.SocioNegocio.BD::PER.ClasificacionFatca"("IdClasificacionFatca","Nombre","PorDefecto")
VALUES('USENTITY','US Entity','N');
INSERT INTO "BSCL.SocioNegocio.BD::PER.ClasificacionFatca"("IdClasificacionFatca","Nombre","PorDefecto")
VALUES('NOUSENTITY','Non US Entity','S');
INSERT INTO "BSCL.SocioNegocio.BD::PER.ClasificacionFatca"("IdClasificacionFatca","Nombre","PorDefecto")
VALUES('RECALCITR','Recalcitrante','N');


INSERT INTO "BSCL.SocioNegocio.BD::PER.ClasificacionEmpresaFatca"("IdClasificacionEmpresaFatca","Nombre","PorDefecto","UsaNumeroGIIN")
VALUES('FFI','FFI','N','S');
INSERT INTO "BSCL.SocioNegocio.BD::PER.ClasificacionEmpresaFatca"("IdClasificacionEmpresaFatca","Nombre","PorDefecto","UsaNumeroGIIN")
VALUES('FFIEXCEPT','FFI Excepted','N','S');
INSERT INTO "BSCL.SocioNegocio.BD::PER.ClasificacionEmpresaFatca"("IdClasificacionEmpresaFatca","Nombre","PorDefecto","UsaNumeroGIIN")
VALUES('NPFFI','NPFFI','N','S');
INSERT INTO "BSCL.SocioNegocio.BD::PER.ClasificacionEmpresaFatca"("IdClasificacionEmpresaFatca","Nombre","PorDefecto","UsaNumeroGIIN")
VALUES('NFFEEXCEPT','NFFE Excepted','N','S');
INSERT INTO "BSCL.SocioNegocio.BD::PER.ClasificacionEmpresaFatca"("IdClasificacionEmpresaFatca","Nombre","PorDefecto","UsaNumeroGIIN")
VALUES('NFFEPASSIV','NFFE Passive','N','S');
INSERT INTO "BSCL.SocioNegocio.BD::PER.ClasificacionEmpresaFatca"("IdClasificacionEmpresaFatca","Nombre","PorDefecto","UsaNumeroGIIN")
VALUES('NFFERECALC','NFFE Recalcitrante','N','N');
INSERT INTO "BSCL.SocioNegocio.BD::PER.ClasificacionEmpresaFatca"("IdClasificacionEmpresaFatca","Nombre","PorDefecto","UsaNumeroGIIN")
VALUES('PENDIENTE','Pendiente de clasificar','S','S');


INSERT INTO "BSCL.SocioNegocio.BD::PER.Fatca" ( 
	"IdPersona", "IdEstado", "IdClasificacionFatca", "IdClasificacionEmpresaFatca", "NumeroGIIN", 
	 "FechaIngreso","FechaCreacion","FechaModificacion","IdUsuarioCreador","IdUsuarioModificador")
SELECT 
	"IdPersona",'ACT','NOUSENTITY','','',CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,1
FROM "BSCL.SocioNegocio.BD::PER.Persona";

--################################################
-- JG - CONTRAPARTE, Se agrega Boveda.
--################################################
SET SCHEMA "BSCL";
ALTER TABLE "BSCL.SocioNegocio.BD::CNT.Contraparte"
ADD ("Boveda" NVARCHAR(1) DEFAULT 'N'); 
ALTER TABLE "BSCL.SocioNegocio.BD::CNT.Contraparte"
ADD ( "IdBoveda" BIGINT DEFAULT 0); 
SELECT * FROM "BSCL.SocioNegocio.BD::CNT.Contraparte"

ALTER TABLE "BSCL.SocioNegocio.BD::SOC.Enrolamiento"
ADD("CNT_Boveda" NVARCHAR(1) DEFAULT 'N');

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
"IdTipoTelefono" 			NVARCHAR(40),
"Telefono" 					NVARCHAR(100),
"Email" 					NVARCHAR(100),
"DireccionCalle" 			NVARCHAR(100),
"DireccionNumero" 			NVARCHAR(40),
"DireccionDepartamento" 	NVARCHAR(40),
"FechaCreacion" 			SECONDDATE,
"FechaModificacion" 		SECONDDATE,
"CLI_IdCategoriaInversionista" NVARCHAR(40),
"CLI_IdTipoCliente"				NVARCHAR(40),
"POR_IdPerfilComercial" 	NVARCHAR(40), 
"EMP_CodigoEmpleado"			NVARCHAR(40),
"EMP_CodigoTipoCargoEmpleado"	NVARCHAR(6),
"EMP_IdEmpleadoSupervisor"		BIGINT,
"EMP_IdSucursal"				BIGINT,
"EMP_FechaInicioCargo"			DATE,
"EMP_FechaTerminoCargo" 		DATE,
"CNT_CodigoInscripcion" 		NVARCHAR(40),
"CNT_Emisor" 					NVARCHAR(1),
"CNT_Depositos"					NVARCHAR(1),
"CNT_Bolsa" 					NVARCHAR(1),
"CNT_Corredora" 				NVARCHAR(1),
"CNT_CamaraDeCompensacion" 		NVARCHAR(1),
"CNT_Banco" 					NVARCHAR(1),
"CNT_Boveda" 					NVARCHAR(1),
"PRO_IdAreaProveedor"			BIGINT
--"CLI_IdTipoEnrolamiento"		NVARCHAR(40)
 );

