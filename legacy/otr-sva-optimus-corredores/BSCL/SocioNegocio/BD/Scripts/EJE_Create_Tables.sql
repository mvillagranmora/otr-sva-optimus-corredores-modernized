SET SCHEMA "BSCL";
	
/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/
DROP TABLE "BSCL.SocioNegocio.BD::EJE.Ejecutivo";
DROP TABLE "BSCL.SocioNegocio.BD::EJE.Supervisor"; 
DROP TABLE "BSCL.SocioNegocio.BD::EJE.NotificarCambioInformacionFichaCliente_Entrada";
DROP TABLE "BSCL.SocioNegocio.BD::EJE.NotificarCambioEjecutivoCliente_Entrada";

COMMIT

/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/

CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::EJE.Supervisor" (
 "IdEmpleadoSupervisor" 		BIGINT 		NOT NULL ,
 "FechaCreacion" 				SECONDDATE	NOT NULL,
 "IdUsuarioCreacion" 			BIGINT 		NOT NULL,
 "IdEstado" 					NVARCHAR(3) DEFAULT 'ACT' NOT NULL,
 "IdCarga"                      BIGINT DEFAULT 0 NOT NULL,
 PRIMARY KEY ("IdEmpleadoSupervisor")) ;
 
 CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::EJE.Ejecutivo" (
 "IdEmpleadoEjecutivo" 			BIGINT 		NOT NULL,
 "IdEmpleadoSupervisor" 		BIGINT 		NOT NULL,
 "FechaCreacion" 				SECONDDATE	NOT NULL,
 "IdUsuarioCreacion" 			BIGINT 		NOT NULL,
 "IdEstado" 					NVARCHAR(3) DEFAULT 'ACT' NOT NULL,
 "IdEmpleadoReemplaza" 			BIGINT 		,
 "FechaInicioReemplazo" 		SECONDDATE 	,
 "FechaFinReemplazo" 			SECONDDATE 	,
 "IdCarga"                      BIGINT DEFAULT 0 NOT NULL,
 PRIMARY KEY ("IdEmpleadoEjecutivo", "IdEmpleadoSupervisor")) ;
  
/*==============================================================*/
/* Secuences:                                                  */
/*==============================================================*/
CREATE SEQUENCE "BSCL.SocioNegocio.BD.Secuencias::EJE.SolicitudNotificacion" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
       
/*==============================================================*/
/* Entradas servicios:                                          */
/*==============================================================*/
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.SocioNegocio.BD::EJE.NotificarCambioEjecutivoCliente_Entrada" ("IdSolicitud" BIGINT, "IdCliente" BIGINT, "IdPersonaNuevoEjecutivo" BIGINT) ON COMMIT DELETE ROWS;
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.SocioNegocio.BD::EJE.NotificarCambioInformacionFichaCliente_Entrada" ("IdSolicitud" BIGINT, "IdCliente" BIGINT, "CampoModificado" NVARCHAR(255), "ValorAntiguo" NVARCHAR(255), "ValorNuevo" NVARCHAR(255)) ON COMMIT DELETE ROWS;

/*==============================================================*/
/* constraint:                                                  */
/*==============================================================*/
alter table "BSCL.SocioNegocio.BD::EJE.Supervisor"  drop constraint "FK_Empleado_Supervisor";
alter table "BSCL.SocioNegocio.BD::EJE.Ejecutivo"  drop constraint "FK_Empleado_Ejecutivo";
alter table "BSCL.SocioNegocio.BD::EJE.Ejecutivo"  drop constraint "FK_Empleado_Reemplaza";
alter table "BSCL.SocioNegocio.BD::EJE.Ejecutivo"  drop constraint "FK_Empleado_SupervisorEjecutivo";

alter table "BSCL.SocioNegocio.BD::EJE.Supervisor"
   add constraint "FK_Empleado_Supervisor" foreign key ("IdEmpleadoSupervisor")
      references "BSCL.SocioNegocio.BD::EMP.Empleado" ("IdEmpleado"); 
   
alter table "BSCL.SocioNegocio.BD::EJE.Ejecutivo"
   add constraint "FK_Empleado_Ejecutivo" foreign key ("IdEmpleadoEjecutivo")
      references "BSCL.SocioNegocio.BD::EMP.Empleado" ("IdEmpleado");
      
alter table "BSCL.SocioNegocio.BD::EJE.Ejecutivo"
   add constraint "FK_Empleado_Reemplaza" foreign key ("IdEmpleadoReemplaza")
      references "BSCL.SocioNegocio.BD::EMP.Empleado" ("IdEmpleado"); 
      
alter table "BSCL.SocioNegocio.BD::EJE.Ejecutivo"
   add constraint "FK_Empleado_SupervisorEjecutivo" foreign key ("IdEmpleadoSupervisor")
      references "BSCL.SocioNegocio.BD::EMP.Empleado" ("IdEmpleado"); 
 
ALTER TABLE "BSCL.SocioNegocio.BD::EJE.Ejecutivo"
 ADD ("Borrado" NVARCHAR(1) NOT NULL DEFAULT 'N');
                             
/*==============================================================*/
/* Index:                                                       */
/*==============================================================*/





/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;
