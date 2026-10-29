SET SCHEMA "BSCL";

-- Para CHILE(43) se refiere a las camaras del CCLV

/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/
DROP TABLE "BSCL.Plataforma.BD::CAM.Camara"      CASCADE;
DROP TABLE "BSCL.Plataforma.BD::CAM.GrupoCamara" CASCADE;


/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/

create column table "BSCL.Plataforma.BD::CAM.Camara" (
  	"IdCamara"  		NVARCHAR(40)  		NOT NULL, -- RV/PM/PH/SM
  	"Nombre"            NVARCHAR(100)       NOT NULL default '',
  	"CodigoContraparte" NVARCHAR(40)  		NOT NULL default '',
	"IdEmpresa"         BIGINT        		NOT NULL default 0,
	"IdEstado"			NVARCHAR(3)		    NOT NULL default '',
   primary key ("IdCamara")
);


create column table "BSCL.Plataforma.BD::CAM.GrupoCamara" (
   "IdGrupoCamara"        		BIGINT        	NOT NULL,
   "SecuenciaAgrupacion"  		NVARCHAR(40)    NOT NULL,
   "GrupoCamara"                NVARCHAR(100)   NOT NULL default '' ,
   "CodigoGrupo"        		BIGINT        	NOT NULL,
   "IdCamara"  		            NVARCHAR(40)  	NOT NULL,
   "IdEstado"			        NVARCHAR(3)		NOT NULL default '',
   primary key ("IdGrupoCamara")
);

					
/*==============================================================*/
/* constraint:                                                  */
/*==============================================================*/

/* Table: */


/*==============================================================*/
/* Index:                                                       */
/*==============================================================*/

/* Table: */


/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit
