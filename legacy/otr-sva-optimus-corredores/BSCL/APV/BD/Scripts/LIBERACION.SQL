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

///Cflores : SOlo incluir nuevos Scripts
  
  
/*==============================================================*/
/* Drop Tables: BSCL                                           */
/*==============================================================*/

/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/



/*==============================================================*/
/* Drop Sequence:                                               */
/*==============================================================*/
  
/*==============================================================*/
/* Create Sequence:                                             */
/*==============================================================*/


/*==============================================================*/
/* Insert Table Servicio:                                       */
/*==============================================================*/

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" 
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES (6058,'PLUGIN_APV.jar@cl.bcs.APV.CalculoMontosDesinversion',0,'J','Calcula los Montos y los devuelve para llenar la grilla.','','');


