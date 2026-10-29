SET SCHEMA "BSCL";

/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/
--DROP TABLE  "BSCL.Normativa.BD::CIR.EstadoCircularizacion" CASCADE;

DROP TABLE  "BSCL.Normativa.BD::CIR.Parametro" CASCADE;
DROP TABLE  "BSCL.Normativa.BD::CIR.Nomina" CASCADE;

DROP TABLE  "BSCL.Normativa.BD::CIR.ResumenCliente" CASCADE;
DROP TABLE  "BSCL.Normativa.BD::CIR.ResumenInstrumento" CASCADE;



DROP SEQUENCE "BSCL.Normativa.BD.Secuencias::CIR.Parametro"	CASCADE;
DROP SEQUENCE "BSCL.Normativa.BD.Secuencias::CIR.Nomina"	CASCADE;

/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/




CREATE COLUMN TABLE "BSCL.Normativa.BD::CIR.Parametro" (
   "IdParametro"        		BIGINT		 		NOT NULL,
--   "Esquema" 					NVARCHAR(10) 		NOT NULL,
--   "Version" 					NVARCHAR(10) 		NOT NULL,
   "Periodo"  					NVARCHAR(6) 		NOT NULL,
   "Codigo" 					NVARCHAR(40) 		NOT NULL,
   "Descripcion"				NVARCHAR(100) 		DEFAULT '' NOT NULL,
   "Valor" 						nvarchar(100) 		DEFAULT '' NOT NULL,
   "FechaIngreso"       		SECONDDATE          DEFAULT '0001-01-01 00:00:00' NOT NULL,
   "FechaCreacion"     			SECONDDATE          DEFAULT '0001-01-01 00:00:00' NOT NULL,
   "FechaModificacion"  		SECONDDATE          DEFAULT '0001-01-01 00:00:00' NOT NULL,
   "IdEstado" 				 	NVARCHAR(3) 		DEFAULT 'ACT' NOT NULL,
   "IdEstadoAutorizacion" 		NVARCHAR(3)         DEFAULT 'PEN' NOT NULL,
   "IdUsuarioCreador"   		BIGINT  DEFAULT 0,
   PRIMARY KEY ("IdParametro")
);

 
 
 
CREATE COLUMN TABLE "BSCL.Normativa.BD::CIR.Nomina" (
   "IdNomina" 									BIGINT 				NOT NULL,
   "IdDocumento" 								BIGINT 				NOT NULL DEFAULT 0,
   "IdCliente" 									BIGINT 				NOT NULL DEFAULT 0,
   "IdPortafolio" 								BIGINT 				NOT NULL DEFAULT 0,
   "CodigoPortafolio" 							NVARCHAR(100)		NOT NULL DEFAULT '',
   "IdPersona" 									BIGINT 				NOT NULL DEFAULT 0,
   --Datos del Envio (Sistema)
   --"IdDireccionElectronica"						BIGINT NOT NULL DEFAULT 0,
   --"IdTelefonoContacto"							BIGINT NOT NULL DEFAULT 0,
   --"IdDireccionFisica"							BIGINT NOT NULL DEFAULT 0,
   --Datos del Envio (Nomina)
   "DireccionElectronica"		 				NVARCHAR(100)  NULL,
   "DireccionFisica"		 					NVARCHAR(400)  NULL,
   "FormaEnvio"		 							NVARCHAR(10)  NOT NULL DEFAULT '',   
   "Notificar"		 							NVARCHAR(2)  NOT NULL DEFAULT 'SI',
   "IdEstadoNotificacion"		 				NVARCHAR(3)  NOT NULL DEFAULT 'PEN',
   "IdEstadoRespuesta"		 					NVARCHAR(3)  NOT NULL DEFAULT '',
   "Motivo"		 								NVARCHAR(500)  NOT NULL DEFAULT '',
   "FechaIngreso"       						SECONDDATE          DEFAULT '0001-01-01 00:00:00' NOT NULL,
   "FechaCreacion"     							SECONDDATE          DEFAULT '0001-01-01 00:00:00' NOT NULL,
   "FechaModificacion"  						SECONDDATE          DEFAULT '0001-01-01 00:00:00' NOT NULL,
--   "IdEstado" 				 	NVARCHAR(3) 		DEFAULT 'ACT' NOT NULL,
   "IdUsuarioCreador"   						BIGINT  DEFAULT 0,
   PRIMARY KEY ("IdNomina")
);

 CREATE COLUMN TABLE "BSCL.Normativa.BD::CIR.ResumenCliente" (
   "IdDocumento" 								BIGINT 				NOT NULL,
   "IdCliente" 									BIGINT 				NOT NULL,
   "IdPortafolio" 								BIGINT 				NOT NULL,
   "CodigoPortafolio" 							NVARCHAR(100)		NOT NULL DEFAULT '',
   "IdPersona" 									BIGINT 				NOT NULL DEFAULT 0,
   "ValorPresente"								DECIMAL(26,8)		NOT NULL DEFAULT 0,
   "FechaIngreso"       						SECONDDATE          DEFAULT '0001-01-01 00:00:00' NOT NULL,
   "FechaCreacion"     							SECONDDATE          DEFAULT '0001-01-01 00:00:00' NOT NULL,
   "FechaModificacion"  						SECONDDATE          DEFAULT '0001-01-01 00:00:00' NOT NULL,
   "IdUsuarioCreador"   						BIGINT  DEFAULT 0,
   PRIMARY KEY ("IdDocumento","IdCliente","IdPortafolio")
);
 ---Agregar Compañia(IdEmidor)
 CREATE COLUMN TABLE "BSCL.Normativa.BD::CIR.ResumenInstrumento" (
   "IdDocumento" 								BIGINT 				NOT NULL DEFAULT 0,
   "IdCliente" 									BIGINT 				NOT NULL,
   "IdPortafolio" 								BIGINT 				NOT NULL,
   "CodigoPortafolio" 							NVARCHAR(100)		NOT NULL DEFAULT '',
   "IdInstrumento" 								BIGINT 				NOT NULL DEFAULT 0,
   "IdEmisor"									NVARCHAR(40)		NOT NULL DEFAULT '',
   "Nemotecnico"								NVARCHAR(40)		NOT NULL DEFAULT '',
   "IdClasificacionTipoInstrumento"				NVARCHAR(3)			NOT NULL DEFAULT '',
   "EsCtaPropia"								NVARCHAR(2)			NOT NULL DEFAULT '',
   "TotalValorPresente"							DECIMAL(26,8)		NOT NULL DEFAULT 0,
   "FechaIngreso"       						SECONDDATE          DEFAULT '0001-01-01 00:00:00' NOT NULL,
   "FechaCreacion"     							SECONDDATE          DEFAULT '0001-01-01 00:00:00' NOT NULL,
   "FechaModificacion"  						SECONDDATE          DEFAULT '0001-01-01 00:00:00' NOT NULL,
   "IdUsuarioCreador"   						BIGINT  DEFAULT 0,
   PRIMARY KEY ("IdDocumento","IdCliente","IdPortafolio","IdInstrumento","IdEmisor","IdClasificacionTipoInstrumento","EsCtaPropia")
);
  
									    								     									     
 /*==============================================================*/
/* Sequences:       DROP & CREATE                               */
/*==============================================================*/

CREATE SEQUENCE "BSCL.Normativa.BD.Secuencias::CIR.Parametro"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;

CREATE SEQUENCE "BSCL.Normativa.BD.Secuencias::CIR.Nomina"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
       
/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit






