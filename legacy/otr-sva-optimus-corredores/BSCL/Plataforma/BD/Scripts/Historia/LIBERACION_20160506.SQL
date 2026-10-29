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
-- LIBERACION SEMANA: 06-05-2016 
--

--################################################
--    JG - BANCO
--################################################

ALTER TABLE "BSCL.Plataforma.BD::BAN.Banco"
ADD ("IdPersona" BIGINT	 not null default 0);



--################################################
--    CFlores- LIMITES
--################################################


SET SCHEMA "BSCL";

INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('EVALIM','LI','PLA','EVALUACION DE LIMITES','VIG');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('EVALIM','N');
INSERT INTO "BSCL.Plataforma.BD::AUT.TipoEvento" 
VALUES('EVALIM',1,'PLA','AUT_LIM_EVA','Evaluación de Límites',1,'S','' ,'' ,0,0,'VIG');


----Borrar tabla
SET SCHEMA "BSCL";
drop table "BSCL.Plataforma.BD::LIM.EvaluacionLimites"  cascade; 

----crear tabla
SET SCHEMA "BSCL";
create column table "BSCL.Plataforma.BD::LIM.EvaluacionLimites" (
   "IdEvaluacionLimite"          BIGINT not null,
   "IdSecuenciaEvaluacion"  	 BIGINT not null,
   "IdLimite"                    BIGINT not null,
   "IdTipoOperacion" 			 NVARCHAR(6) NOT NULL,
   "IdOperacion" 				 BIGINT NOT NULL DEFAULT 0,
   "IdUsuarioAutoriza" 			 BIGINT not null,
   "IdMoneda" 					 NVARCHAR(3) NOT NULL,
   "ConceptoExceso" 			 NVARCHAR(520) NOT NULL,
   "MontoExcedido"               DECIMAL(26,8) NOT NULL DEFAULT 0,
   "MontoUtilizado"              DECIMAL(26,8) NOT NULL DEFAULT 0,
   "FechaIngreso"       		 SECONDDATE          default '0001-01-01 00:00:00' not null,
   "FechaCreacion"     			 SECONDDATE          default '0001-01-01 00:00:00' not null,
   "IdEstado"                    NVARCHAR(3) 		 not null Default '',
   "FechaAutorizacion"  		 SECONDDATE          default '0001-01-01 00:00:00' not null,
   "IdEstadoAutorizacion" 		 NVARCHAR(3)         default 'PEN' not null,
   "IdUsuarioCreador"   		 BIGINT  DEFAULT 0,
primary key ("IdEvaluacionLimite","IdSecuenciaEvaluacion")
   );

----Agregar campos
SET SCHEMA "BSCL";
ALTER TABLE "BSCL.Plataforma.BD::LIM.Limite"
ADD ("UI_Limite"           		NVARCHAR(16) not null default '');
				
		
--Borrar secuancia
SET SCHEMA "BSCL";
drop sequence "BSCL.Plataforma.BD.Secuencias::Limite";		

--crear secuencias
SET SCHEMA "BSCL";
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::LIM.IdLimite"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;	
       
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::LIM.IdEvaluacionLimite"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;	
       
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::LIM.IdSecuenciaEvaluacion"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;	


--Valores Defecto Sistema: LIMITES - LINEA SOBREGIRO
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ValorDefectoSistema" ("IdValorDefectoSistema","Nombre","Valor","IdEmpresa","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEstado") 
VALUES('IdMonedaLimiteLineaSobregiro','Moneda Límite Línea Sobregiro','CLP',1,1,NOW(),NOW(),'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ValorDefectoSistema" ("IdValorDefectoSistema","Nombre","Valor","IdEmpresa","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEstado") 
VALUES('IdEstadoLimiteLineaSobregiro','Estado Límite Línea Sobregiro','ACT',1,1,NOW(),NOW(),'ACT');

-- TipoLimite para los limites de Linea de Sobregiro en Portafolio
INSERT INTO "BSCL.Plataforma.BD::LIM.TipoLimite"("IdTipoLimite","IdEmpresa","Nombre","EsCuentaPropia","IdComportamiento","IdGrupoLimite","Categoria","IdEstado")
VALUES('LIMPORTSOBREGIRO',1,'LÍMITE PORTAFOLIO SOBREGIRO','N','RZD','PORTAFOLIO','OPERACION','ACT');


--################################################
--  JG- MODELO ANTIGUO COND.OPERATIVAS DEPRECADO
-- AHORA EN PLA/REQ
--################################################		

drop table "BSCL.Plataforma.BD::COM.OperacionUsuario"	 					cascade;
drop table "BSCL.Plataforma.BD::COM.TipoEtapa"								cascade;
drop table "BSCL.Plataforma.BD::COM.EtapaOperacion"							cascade;
drop table "BSCL.Plataforma.BD::COM.EtapaCondicionOperativa" 				cascade;
drop table "BSCL.Plataforma.BD::COM.CondicionOperativa" 					cascade;
drop table "BSCL.Plataforma.BD::COM.TipoComportamientoCondicion" 			cascade;
drop table "BSCL.Plataforma.BD::COM.TipoCondicion" 							cascade;
drop table "BSCL.Plataforma.BD::COM.EtapaGrupoCondicionesOperativas" 		cascade;
drop table "BSCL.Plataforma.BD::COM.GrupoCondicionesOperativas"				cascade;
drop table "BSCL.Plataforma.BD::COM.DetallePlantillaCondicionesOperativas"	cascade;
drop table "BSCL.Plataforma.BD::COM.PlantillaCondicionesOperativas" 		cascade;

DROP VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarOperacionUsuario";
DROP VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarEtapaCondicionOperativa";
DROP VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarEtapaGrupoCondicionesOperativas"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarEtapaOperacion"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarGrupoCondicionesOperativas";
DROP VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarPlantillaCondicionesOperativas"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarCondicionOperativa";
DROP VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePlantillaCondicionesOperativas"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarPerfilCondicionesOperativas"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarTipoComportamientoCondicion";

DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::COM.CrearPlantillaCondicionesOperativas"; 

--################################################
--  JG- MODELO ANTIGUO FACTURACION
-- AHORA EN PLA/IND
--################################################

drop table "BSCL.Plataforma.BD::COM.ModeloFacturacion" CASCADE;
DROP VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarModeloFacturacion"; 

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::COM.CrearModeloFacturacion";
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::COM.ModificarModeloFacturacion";

--################################################
--  CSI - SE BORRAN - NO SE USAN
--################################################
Drop table BSCL."BSCL.Plataforma.BD::MRE.Regla";                                
drop table BSCL."BSCL.Plataforma.BD::MRE.ReglaEjecucion";                       
drop table BSCL."BSCL.Plataforma.BD::MRE.ReglaGrupo";                           
drop table BSCL."BSCL.Plataforma.BD::MRE.ReglaOperacion";                       
drop table BSCL."BSCL.Plataforma.BD::MRE.ReglaSalida";                          

--################################################
--  JG- SIN USO
--################################################
DROP TYPE "BSCL.Plataforma.BD::COM.Tipos.ConsultarPerfilCobros";

--################################################
-- JG. MODELO ANTIGUO USO PORTAFOLIO DEPRECADO
--################################################
DROP TABLE "BSCL.Plataforma.BD::PLA.ConfiguracionUsoPortafolio" cascade;
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracionUsoPortafolio";


--################################################
--  CESPINOSA - CIERRE MODULO
--################################################
UPDATE "BSCL.Plataforma.BD::PLA.Servicio"
SET "RutaServicio" = 'BSCL.Plataforma.BD.Servicios::CRE.CrearRequisitoAperturaCierreModulo'
WHERE "IdServicio" = 228;
UPDATE "BSCL.Plataforma.BD::PLA.Servicio"
SET "RutaServicio" = 'BSCL.Plataforma.BD.Servicios::CRE.SolicitudAperturaModulo'
WHERE "IdServicio" = 234;
UPDATE "BSCL.Plataforma.BD::PLA.Servicio"
SET "RutaServicio" = 'BSCL.Plataforma.BD.Servicios::CRE.SolicitudCierreModulo'
WHERE "IdServicio" = 240;
UPDATE "BSCL.Plataforma.BD::PLA.Servicio"
SET "RutaServicio" = 'BSCL.Plataforma.BD.Servicios::CRE.EliminarRequisitoAperturaCierreModulo'
WHERE "IdServicio" = 285;

DROP TABLE "BSCL.Plataforma.BD::PLA.CREModuloEmpresa"  cascade;
DROP TABLE "BSCL.Plataforma.BD::PLA.CREModuloFuncionProceso"   cascade;
DROP TABLE "BSCL.Plataforma.BD::PLA.CRETipoRequisito"    cascade;
DROP TABLE "BSCL.Plataforma.BD::PLA.CREModuloRequisitoAperturaCierre" cascade;
DROP TABLE "BSCL.Plataforma.BD::PLA.CREModuloFuncionProcesoDependencia" cascade;1

--################################################
--  CAPONTE - TIPOS DOCUMENTOS
--################################################

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (307, 'BSCL.Plataforma.BD.Servicios::DOC.ModificarTipoDocumento' , 1,  'P', ''); 
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (308, 'BSCL.SocioNegocio.BD.Servicios::DOC.ConsultarDocumentos' , 1,  'P', ''); 


                           
                                 
